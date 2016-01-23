package com.houndify.sample;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.annotations.IconFactory;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by PhucNgo on 1/23/16.
 */
public class FragmentMap extends Fragment {
    public FragmentMap() {

    }

    MapView mapView;
    static RequestQueue queue;

    public FragmentMap() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mapView = (MapView) v.findViewById(R.id.mapboxMapView);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setZoomLevel(11);
        mapView.onCreate(savedInstanceState);

        mapView.setCenterCoordinate(new LatLng(39.952271, -75.191273));
        IconFactory iconFactory = mapView.getIconFactory();
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.blue_marble);
        Icon icon = iconFactory.fromDrawable(drawable);
        mapView.addMarker(new MarkerOptions()
                .position(new LatLng(39.952271, -75.191273))
                .icon(icon));

        queryEveryBlock();

        return v;
    }

    void queryEveryBlock(){
        Globals.pageCount = 0;
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(getActivity());
        String url ="https://api.everyblock.com/content/philly/topnews/?schema=crime-posts&token=2882c513284b03351c39cb893825a3afad37e6e1";

        // Get the crime news reports
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            System.out.println("response"+response);
                            JSONObject a = new JSONObject(response);
                            JSONArray res = (JSONArray)a.get("results");

                            for (int i = 0; i < res.length(); i++) {
                                JSONObject cur = res.getJSONObject(i);
                                //String id, String title, String location_name, LatLng coords
                                JSONObject rawCoords = ((JSONArray)cur.get("location_coordinates")).getJSONObject(0);
                                LatLng coords = new LatLng(rawCoords.getDouble("latitude"), rawCoords.getDouble("longitude"));
                                News extraExtra = new News(cur.getString("id"), cur.getString("title"), cur.getString("location_name"),
                                        coords);
                                Globals.curNewsStories.add(extraExtra);
                            }

                            for (News n : Globals.curNewsStories){
                                mapView.addMarker(new MarkerOptions()
                                        .position(n.coords)
                                        .title(n.title));
                            }

                            System.out.println("size = "+res.length());

                        } catch(Exception e){
                            System.out.println("you made a boo boo");
                            e.printStackTrace();}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work because "+error);
            }
        });



        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        getCrimes("https://api.everyblock.com/content/philly/topnews/?schema=crime&token=2882c513284b03351c39cb893825a3afad37e6e1");
    }

    void getCrimes(String url){
        Log.e("YO", "Getting Crimes from " + url);
        Globals.pageCount += 1;
        //Get all the random crimes too
        String url2 = url;

        // Request a string response from the provided URL.
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("YO", "YEAH, I'M ACTUALLY DOING SOMETHING!!");
                        // Display the first 500 characters of the response string.
                        try {
                            Log.e("YO", "response2" + response);
                            JSONObject a = new JSONObject(response);
                            Object nextLink = a.get("next");

                            JSONArray res = (JSONArray)a.get("results");

                            for (int i = 0; i < res.length(); i++) {
                                JSONObject cur = res.getJSONObject(i);
                                //String id, String title, String location_name, LatLng coords
                                JSONObject rawCoords = ((JSONArray)cur.get("location_coordinates")).getJSONObject(0);
                                LatLng coords = new LatLng(rawCoords.getDouble("latitude"), rawCoords.getDouble("longitude"));
                                News extraExtra = new News(cur.getString("id"), cur.getString("title"), cur.getString("location_name"),
                                        coords);
                                Globals.crimes.add(extraExtra);
                            }

                            /** Use SpriteFactory, Drawable, and Sprite to load our marker icon
                             * and assign it to a marker */
                            IconFactory iconFactory = mapView.getIconFactory();
                            Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.blue_marble);
                            Icon icon = iconFactory.fromDrawable(drawable);

                            for (News n : Globals.crimes){
                                mapView.addMarker(new MarkerOptions()
                                        .position(n.coords)
                                        .title(n.title));
                            }

                            System.out.println("size2 = "+res.length());

                            System.out.println("NextLink = "+nextLink);
                            if (nextLink != null && Globals.pageCount < Globals.maxPages) {
                                Log.e("YO","Calling GetCrimes on " + nextLink);
                                getCrimes("https" + nextLink.toString().substring(4));
                            }

                        } catch(Exception e){
                            System.out.println("you made a boo boo");
                            e.printStackTrace();}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work because "+error);
            }
        });

        System.out.println("Adding StringRequest2 to queue");
        queue.add(stringRequest2);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause()  {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
