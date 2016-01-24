package com.houndify.sample;

import android.os.Handler;

import java.util.List;

/**
 * Created by PhucNgo on 1/23/16.
 */
public class RepeatSMS implements Runnable {
    private Handler handler;
    private List<Person> contactList;

    public RepeatSMS(List<Person> contactList, Handler handler) {
        this.contactList = contactList;
        this.handler = handler;
    }

    public void run() {
        handler.postDelayed(this, 5000 * 60);
    }

    public void startRepeatingSMS() {
        this.run();
    }

    public void stopRepeatingTask() {
        handler.removeCallbacks(this);
    }
}
