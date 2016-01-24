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
        SensorAccelerometer sensorAccelerometer = new SensorAccelerometer(mainActivity.getApplicationContext(), mainActivity);

    }
}
