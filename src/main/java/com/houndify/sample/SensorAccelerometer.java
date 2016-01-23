package com.houndify.sample;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by PhucNgo on 1/22/16.
 */
public class SensorAccelerometer implements SensorEventListener {

    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView timelabel;

    private float mLastX, mLastY, mLastZ, mAccelLast, mAccelCurrent, mAccel;
    private final float NOISE = (float) 3.0;

    public SensorAccelerometer(Context context) {
        // TODO Auto-generated constructor stub

        this.context = context;

        initialiseSensor();
    }


    public void initialiseSensor() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ALL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor() {
        sensorManager.unregisterListener(this);
        Toast.makeText(context, "Sensor Stopped..", Toast.LENGTH_SHORT).show();
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        mAccelLast = mAccelCurrent;

        mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
//        float mAccel = mAccel * 0.9f + mAccelCurrent * 0.1f;

        if (mAccel > 100) {
            String tmp = "";
        }
    }
}
