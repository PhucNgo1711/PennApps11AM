package com.houndify.sample;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by PhucNgo on 1/23/16.
 */
public class News {
    /*"id": 2217944,
      "title": "Burglary Alert! Weightman and Winona",
      "embed": null,
      "pub_date": "2016-01-20T18:49:33.236Z",
      "item_date": "2016-01-20",
      "location_name": "East Falls",
      "location_coordinates": [
        {
          "latitude": 40.0171061226651,
          "longitude": -75.1899186655489
        }*/

    String id, title, location_name;
    LatLng coords;

    public News(String id, String title, String location_name, LatLng coords) {
        this.id = id;
        this.title = title;
        this.location_name = location_name;
        this.coords = coords;
    }
    
    public News(){}
}
