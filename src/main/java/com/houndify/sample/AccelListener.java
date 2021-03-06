package com.houndify.sample;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by devanshk on 7/21/15.
 */
public class AccelListener implements SensorEventListener {
    private final int shakesNeeded = 4;
    private final int freeFallCountsNeeded = 9;
    private static SharedPreferences prefs;
    private static float averageDelta = 4f;

    private Date lastShake;
    private Date lastFreeFall;
    private int shakeCount = 0;
    private long shakeTimeThreshold = 130;

    /* Here we store the current values of acceleration, one for each axis */
    private float xAccel;
    private float yAccel;
    private float zAccel;

    /* And here the previous ones */
    private float xPreviousAccel;
    private float yPreviousAccel;
    private float zPreviousAccel;

    /* Let's ignore the shake triggered when we're setting up */
    private boolean firstUpdate = true;

    /*What acceleration difference would we assume as a rapid movement? */
    public static float shakeThreshold = 3.4f;

    /*What difference is acceptable between the force of gravity and the force on the device while being thrown*/
    public static float thrownThreshold = 3f;

    /* Has a shaking motion been started (one direction) */
    private boolean shakeInitiated = false;

    public static boolean startSMS = false;

    public AccelListener() { }

    public void executeShakeAction() { //Shake it off
        System.out.println("Detected a micro-shake");

        Date now = new Date();
        if (lastShake != null && now.getTime() - lastShake.getTime() < shakeTimeThreshold)
            shakeCount++;
        else {
            Log.e("AWARE_APP", "Logged " + shakeCount + " shakes.");
            shakeCount = 1;
        }

        lastShake = new Date();

        if (shakeCount == shakesNeeded) {
            //It's a shake! Do Something!

            startSMS = true;
            Toast.makeText(MainActivity.instance, "Phone is under extreme vibration!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        if (lastFreeFall == null) lastFreeFall = new Date();

        updateAccelParameters(se.values[0], se.values[1], se.values[2]);
        if ((!shakeInitiated) && isAccelerationChanged()) {
            shakeInitiated = true;
        } else if ((shakeInitiated) && isAccelerationChanged()) {
            executeShakeAction();
        } else if ((shakeInitiated) && (!isAccelerationChanged())) {
            shakeInitiated = false;
        }
    }

    /* Store the acceleration values given by the sensor */
    private void updateAccelParameters(float xNewAccel, float yNewAccel,
                                       float zNewAccel) {
        /* we have to suppress the first change of acceleration, it results from first values being initialized with 0 */
        if (firstUpdate) {
            xPreviousAccel = xNewAccel;
            yPreviousAccel = yNewAccel;
            zPreviousAccel = zNewAccel;
            firstUpdate = false;
        } else {
            xPreviousAccel = xAccel;
            yPreviousAccel = yAccel;
            zPreviousAccel = zAccel;
        }
        xAccel = xNewAccel;
        yAccel = yNewAccel;
        zAccel = zNewAccel;
    }

    /* If the values of acceleration have changed on at least two axises, we are probably in a shake motion */
    private boolean isAccelerationChanged() {
        float deltaX = Math.abs(xPreviousAccel - xAccel);
        float deltaY = Math.abs(yPreviousAccel - yAccel);
        float deltaZ = Math.abs(zPreviousAccel - zAccel);

        averageDelta = (deltaX+deltaY+deltaZ)/3;
        return (deltaX > shakeThreshold && deltaY > shakeThreshold)
                || (deltaX > shakeThreshold && deltaZ > shakeThreshold)
                || (deltaY > shakeThreshold && deltaZ > shakeThreshold);
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {}
}
