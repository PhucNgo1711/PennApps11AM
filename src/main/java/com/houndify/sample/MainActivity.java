package com.houndify.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.libphs.PhraseSpotterReader;
import com.hound.android.fd.Houndify;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.core.model.sdk.CommandResult;
import com.hound.core.model.sdk.HoundResponse;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    MapView mapView;
    static RequestQueue queue;

    public Runner runner = new Runner(this);

    public Runner getRunner() {
        return this.runner;
    }

    public static MainActivity instance;
    public static MultiThread smsThread;

    private PhraseSpotterReader phraseSpotterReader;
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    TextToSpeechMgr textToSpeechMgr;

    AccelListener acelListener;
    SensorManager mSensorManager;
    Sensor mAccelerometer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The activity_main layout contains the com.hound.android.fd.HoundifyButton which is displayed
        // as the black microphone. When press it will load the HoundifyVoiceSearchActivity.
        setContentView(R.layout.activity_main);
        instance = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        ((CustomViewPager)findViewById(R.id.viewpager)).setPagingEnabled(false);
//        viewPager = (ViewPager) findViewById(R.id.viewpager);
//        setupViewPager(viewPager);
//
//        tabLayout = (TabLayout) findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(viewPager);

        mapView = (MapView) findViewById(R.id.mapboxMapView);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setZoomLevel(14);
        mapView.onCreate(savedInstanceState);

//        mapView.setCenterCoordinate(new LatLng(MyLocationListener.getLat(), MyLocationListener.getLon()));
        mapView.setCenterCoordinate(new LatLng(39.952271, -75.191273));
        IconFactory iconFactory = mapView.getIconFactory();
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.blue_marble);
        Icon icon = iconFactory.fromDrawable(drawable);
        mapView.addMarker(new MarkerOptions()
                .position(new LatLng(39.952271, -75.191273))
                .icon(icon));

        queryEveryBlock();

        // Setup TextToSpeech
        textToSpeechMgr = new TextToSpeechMgr(this);

        // Normally you'd only have to do this once in your Application#onCreate
        Houndify.get(this).setClientId(Constants.CLIENT_ID);
        Houndify.get(this).setClientKey(Constants.CLIENT_KEY);
        Houndify.get(this).setRequestInfoFactory(StatefulRequestInfoFactory.get(this));

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        acelListener = new AccelListener();

        mSensorManager.registerListener(acelListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        startSMSThread();

        //runner.Run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPhraseSpotting();
    }

    /**
     * Called to start the Phrase Spotter
     */
    private void startPhraseSpotting() {
        if (phraseSpotterReader == null) {
            phraseSpotterReader = new PhraseSpotterReader(new SimpleAudioByteStreamSource());
            phraseSpotterReader.setListener(phraseSpotterListener);
            phraseSpotterReader.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if we don't, we must still be listening for "ok hound" so teardown the phrase spotter
        if (phraseSpotterReader != null) {
            stopPhraseSpotting();
        }
    }

    /**
     * Called to stop the Phrase Spotter
     */
    private void stopPhraseSpotting() {
        if (phraseSpotterReader != null) {
            phraseSpotterReader.stop();
            phraseSpotterReader = null;
        }
    }

    /**
     * Implementation of the PhraseSpotterReader.Listener interface used to handle PhraseSpotter
     * call back.
     */
    private final PhraseSpotterReader.Listener phraseSpotterListener = new PhraseSpotterReader.Listener() {
        @Override
        public void onPhraseSpotted() {

            // It's important to note that when the phrase spotter detects "Ok Hound" it closes
            // the input stream it was provided.
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopPhraseSpotting();
                    // Now start the HoundifyVoiceSearchActivity to begin the search.

                    Houndify.get(MainActivity.this).voiceSearch(MainActivity.this);
                }
            });
        }

        @Override
        public void onError(final Exception ex) {

            // for this sample we don't care about errors from the "Ok Hound" phrase spotter.

        }
    };

    /**
     * The HoundifyVoiceSearchActivity returns its result back to the calling Activity
     * using the Android's onActivityResult() mechanism.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Houndify.REQUEST_CODE) {
            final HoundSearchResult result = Houndify.get(this).fromActivityResult(resultCode, data);

            if (result.hasResult()) {
                onResponse(result.getResponse());
            } else if (result.getErrorType() != null) {
                onError(result.getException(), result.getErrorType());
            } else {
//                textView.setText("Aborted search");
            }
        }
    }

    /**
     * Called from onActivityResult() above
     *
     * @param response
     */
    private void onResponse(final HoundResponse response) {
        if (response.getResults().size() > 0) {
            // Required for conversational support
            StatefulRequestInfoFactory.get(this).setConversationState(response.getResults().get(0).getConversationState());

            try {
//                textView.setText("Received response\n\n" + response.getResults().get(0).getActionSucceedResponse().getWrittenResponse());
            } catch (Exception ex) {
//                textView.setText("Received response\n\n" + response.getResults().get(0).getWrittenResponse());
            }

            textToSpeechMgr.speak(response.getResults().get(0).getSpokenResponse());

            /**
             * "Client Match" demo code.
             *
             * Houndify client apps can specify their own custom phrases which they want matched using
             * the "Client Match" feature. This section of code demonstrates how to handle
             * a "Client Match phrase".  To enable this demo first open the
             * StatefulRequestInfoFactory.java file in this project and and uncomment the
             * "Client Match" demo code there.
             *
             * Example for parsing "Client Match"
             */
            if (response.getResults().size() > 0) {
                CommandResult commandResult = response.getResults().get(0);
                if (commandResult.getCommandKind().equals("ClientMatchCommand")) {
                    JsonNode matchedItemNode = commandResult.getJsonNode().findValue("MatchedItem");
                    String intentValue = matchedItemNode.findValue("Intent").textValue();

                    if (intentValue.equals("Okay")) {
                        textToSpeechMgr.speak("Great to hear that you are safe.");
                    } else if (intentValue.equals("Help")) {
                        textToSpeechMgr.speak("Help is on the way!");
                    }
                }
            }
        } else {
//            textView.setText("Received empty response!");
        }
    }

    /**
     * Called from onActivityResult() above
     *
     * @param ex
     * @param errorType
     */
    private void onError(final Exception ex, final VoiceSearchInfo.ErrorType errorType) {
//        textView.setText(errorType.name() + "\n\n" + exceptionToString(ex));
    }

    private static String exceptionToString(final Exception ex) {
        try {
            final StringWriter sw = new StringWriter(1024);
            final PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.close();
            return sw.toString();
        } catch (final Exception e) {
            return "";
        }
    }


    /**
     * Helper class used for managing the TextToSpeech engine
     */
    class TextToSpeechMgr implements TextToSpeech.OnInitListener {
        private TextToSpeech textToSpeech;

        public TextToSpeechMgr(Activity activity) {
            textToSpeech = new TextToSpeech(activity, this);
        }

        @Override
        public void onInit(int status) {
            // Set language to use for playing text
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
            }
        }

        /**
         * Play the text to the device speaker
         *
         * @param textToSpeak
         */
        public void speak(String textToSpeak) {
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public static void startSMSThread() {
        //Start a new thread to send SMSs every 5 minutes
        if (smsThread != null) {
            stopSMSThread();
        }
        smsThread = new MultiThread(instance, new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        //Send SMS
                        String phoneNumber = "1234567890";
                        SMS.SendSMS(phoneNumber);

                        Thread.sleep(5 * 60 * 1000); //5 minutes * 60 seconds * 1000 milliseconds
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        smsThread.executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    public static void stopSMSThread() {
        if (smsThread != null) {
            smsThread.cancel(true);
        }
        smsThread = null;
    }

//    private void setupViewPager(ViewPager viewPager) {
//        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//        adapter.addFragment(new FragmentMap(), "Map");
//        adapter.addFragment(new FragmentInteract(this), "TWO");
//        viewPager.setAdapter(adapter);
//    }
//
//    class ViewPagerAdapter extends FragmentPagerAdapter {
//        private final List<Fragment> mFragmentList = new ArrayList<>();
//        private final List<String> mFragmentTitleList = new ArrayList<>();
//
//        public ViewPagerAdapter(FragmentManager manager) {
//            super(manager);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return mFragmentList.get(position);
//        }
//
//        @Override
//        public int getCount() {
//            return mFragmentList.size();
//        }
//
//        public void addFragment(Fragment fragment, String title) {
//            mFragmentList.add(fragment);
//            mFragmentTitleList.add(title);
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return mFragmentTitleList.get(position);
//        }
//    }

    void queryEveryBlock() {
        Globals.pageCount = 0;
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);
        String url = "https://api.everyblock.com/content/philly/topnews/?schema=crime-posts&token=2882c513284b03351c39cb893825a3afad37e6e1";

        // Get the crime news reports
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            System.out.println("response" + response);
                            JSONObject a = new JSONObject(response);
                            JSONArray res = (JSONArray) a.get("results");

                            for (int i = 0; i < res.length(); i++) {
                                JSONObject cur = res.getJSONObject(i);
                                //String id, String title, String location_name, LatLng coords
                                JSONObject rawCoords = ((JSONArray) cur.get("location_coordinates")).getJSONObject(0);
                                LatLng coords = new LatLng(rawCoords.getDouble("latitude"), rawCoords.getDouble("longitude"));
                                News extraExtra = new News(cur.getString("id"), cur.getString("title"), cur.getString("location_name"),
                                        coords);
                                Globals.curNewsStories.add(extraExtra);
                            }

                            for (News n : Globals.curNewsStories) {
                                mapView.addMarker(new MarkerOptions()
                                        .position(n.coords)
                                        .title(n.title));
                            }

                            System.out.println("size = " + res.length());

                        } catch (Exception e) {
                            System.out.println("you made a boo boo");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work because " + error);
            }
        });


        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        getCrimes("https://api.everyblock.com/content/philly/locations/19104/timeline/?schema=crime&token=2882c513284b03351c39cb893825a3afad37e6e1");
    }

    void getCrimes(String url) {
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

                            JSONArray res = (JSONArray) a.get("results");

                            for (int i = 0; i < res.length(); i++) {
                                JSONObject cur = res.getJSONObject(i);
                                //String id, String title, String location_name, LatLng coords
                                JSONObject rawCoords = ((JSONArray) cur.get("location_coordinates")).getJSONObject(0);
                                LatLng coords = new LatLng(rawCoords.getDouble("latitude"), rawCoords.getDouble("longitude"));
                                News extraExtra = new News(cur.getString("id"), cur.getString("title"), cur.getString("location_name"),
                                        coords);
                                Globals.crimes.add(extraExtra);
                            }

                            /** Use SpriteFactory, Drawable, and Sprite to load our marker icon
                             * and assign it to a marker */
                            IconFactory iconFactory = mapView.getIconFactory();
                            Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.blue_marble);
                            Icon icon = iconFactory.fromDrawable(drawable);

                            for (News n : Globals.crimes) {
                                mapView.addMarker(new MarkerOptions()
                                        .position(n.coords)
                                        .title(n.title));
                            }

                            System.out.println("size2 = " + res.length());

                            System.out.println("NextLink = " + nextLink);
                            if (nextLink != null && Globals.pageCount < Globals.maxPages) {
                                Log.e("YO", "Calling GetCrimes on " + nextLink);
                                getCrimes("https" + nextLink.toString().substring(4));
                            }

                        } catch (Exception e) {
                            System.out.println("you made a boo boo");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work because " + error);
            }
        });

        System.out.println("Adding StringRequest2 to queue");
        queue.add(stringRequest2);
    }

    void getEmergencies(String url) {
        Log.e("YO", "Getting Emergencies from " + url);
        Globals.pageCount += 1;
        //Get all the random crimes too
        String url2 = url;

        // Request a string response from the provided URL.
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("YO", "Emergencies being downloaded!");
                        // Display the first 500 characters of the response string.
                        try {
                            Log.e("YO", "response2" + response);
                            JSONObject a = new JSONObject(response);
                            Object nextLink = a.get("next");

                            JSONArray res = (JSONArray) a.get("results");

                            for (int i = 0; i < res.length(); i++) {
                                JSONObject cur = res.getJSONObject(i);
                                if (cur.getString("title").toLowerCase().contains("emergency")) {
                                    //String id, String title, String location_name, LatLng coords
                                    JSONObject rawCoords = ((JSONArray) cur.get("location_coordinates")).getJSONObject(0);
                                    LatLng coords = new LatLng(rawCoords.getDouble("latitude"), rawCoords.getDouble("longitude"));
                                    EmergencyNews extraExtra = new EmergencyNews(cur.getString("id"), cur.getString("title"), cur.getString("location_name"),
                                            coords, cur.getString("description"));
                                    Globals.emergencies.add(extraExtra);
                                }
                            }

                            /** Use SpriteFactory, Drawable, and Sprite to load our marker icon
                             * and assign it to a marker */
                            IconFactory iconFactory = mapView.getIconFactory();
                            Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.report_problem);
                            Icon icon = iconFactory.fromDrawable(drawable);

                            for (EmergencyNews n : Globals.emergencies) {
                                mapView.addMarker(new MarkerOptions()
                                        .position(n.coords)
                                        .title(n.title)
                                        .snippet(n.desc)
                                        .icon(icon));
                            }

                            System.out.println("size2 = " + res.length());

                            System.out.println("NextLink = " + nextLink);
                            if (nextLink != null && Globals.pageCount < Globals.maxPages) {
                                Log.e("YO", "Calling GetEmergencies on " + nextLink);
                                getEmergencies("https" + nextLink.toString().substring(4));
                            }

                        } catch (Exception e) {
                            System.out.println("you made a boo boo");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work because " + error);
            }
        });

        System.out.println("Adding StringRequest2 to queue");
        queue.add(stringRequest2);
    }

}

//*---------- Listener class to get coordinates ------------- */
class MyLocationListener implements LocationListener {
    private MainActivity mainActivity;
    private static Double lon;
    private static Double lat;

    public MyLocationListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLocationChanged(Location loc) {
        lon = loc.getLongitude();
        lat = loc.getLatitude();

        for(EmergencyNews newsItem : Globals.emergencies) {
            float[] results = new float[1];
            Location.distanceBetween(lat, lon, newsItem.coords.getLatitude(), newsItem.coords.getLongitude(), results);
            if (results[0]<=1000) {
                Toast.makeText(MainActivity.instance, "An emergency has occurred near you.  Mark yourself as safe by saying 'I'm okay'", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public static Double getLon() {
        return lon;
    }

    public static Double getLat() {
        return lat;
    }
}
