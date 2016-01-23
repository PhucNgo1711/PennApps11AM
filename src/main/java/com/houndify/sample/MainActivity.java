package com.houndify.sample;

import android.app.Activity;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public Runner runner = new Runner(this);
    public Runner getRunner(){
        return this.runner;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The activity_main layout contains the com.hound.android.fd.HoundifyButton which is displayed
        // as the black microphone. When press it will load the HoundifyVoiceSearchActivity.
        setContentView(R.layout.activity_main);

        // Text view for displaying written result
        textView = (TextView)findViewById(R.id.textView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        runner.Run();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentMap(), "Map");
        adapter.addFragment(new FragmentInteract(this, textView), "TWO");
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
