package com.houndify.sample;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by akshay on 1/23/16.
 */
public class EmergencyNews extends News {
    String desc;

    public EmergencyNews(String id, String title, String location_name, LatLng coords, String desc) {
        super(id, title, location_name, coords);
        this.desc = desc;
    }
}
