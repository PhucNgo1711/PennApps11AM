package com.houndify.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.libphs.PhraseSpotterReader;
import com.hound.android.fd.Houndify;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.core.model.sdk.CommandResult;
import com.hound.core.model.sdk.HoundResponse;

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

    public static MainActivity instance;
    public static MultiThread smsThread;

    public Runner runner = new Runner(this);
    public Runner getRunner(){
        return this.runner;
    }

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

        ((CustomViewPager)findViewById(R.id.viewpager)).setPagingEnabled(false);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        acelListener = new AccelListener();

        mSensorManager.registerListener(acelListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        startSMSThread();

        //runner.Run();
    }

    public static void startSMSThread(){
        //Start a new thread to send SMSs every 5 minutes
        if (smsThread != null){
            stopSMSThread();
        }
        smsThread = new MultiThread(instance, new Runnable() {
            @Override
            public void run() {
                try{
                    while (true){
                        //Send SMS
                        String phoneNumber = "1234567890";
                        SMS.sendSMS(phoneNumber);
                        Thread.sleep(5*60*1000); //5 minutes * 60 seconds * 1000 milliseconds
                    }
                } catch (Exception e){e.printStackTrace();}
            }
        });
        smsThread.executeOnExecutor(Executors.newSingleThreadExecutor());
    }
    public static void stopSMSThread(){
        if (smsThread!=null){
            smsThread.cancel(true);
        }
        smsThread = null;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentMap(), "Map");
        adapter.addFragment(new FragmentInteract(this), "TWO");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        startPhraseSpotting();
//    }

}

//*---------- Listener class to get coordinates ------------- */
class MyLocationListener implements LocationListener {
    private MainActivity mainActivity;
    private static String lon;
    private static String lat;

    public MyLocationListener (MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLocationChanged(Location loc) {
        lon = Double.toString(loc.getLongitude());
        lat = Double.toString(loc.getLatitude());
        for(EmergencyNews newsItem : Globals.emergencies) {
            float[] results = new float[1];
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), newsItem.coords.getLatitude(), newsItem.coords.getLongitude(), results);
            if (results[0]<=1000) {
                Toast.makeText(MainActivity.instance, "An emergency has occurred near you.  Mark yourself as safe by saying 'I'm okay'", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public static String getLon() {
        return lon;
    }

    public static String getLat() {
        return lat;
    }
}
