package com.houndify.sample;
/**
 * Created by PhucNgo on 1/22/16.
 */
public class Runner {
    public MainActivity mainActivity;

    public Runner(MainActivity activity){
        mainActivity = (MainActivity) activity;
    }

    public void Run() {
        SensorAccelerometer sensorAccelerometer = new SensorAccelerometer(mainActivity.getApplicationContext());

        String curLat = MyLocationListener.getLat();
        String curLong = MyLocationListener.getLon();

        Contact contact = new Contact(mainActivity);
        contact.getContactList();

//        SMS sms = new SMS();
//        sms.SendSMS("4136954636");
    }
}
