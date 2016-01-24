package com.houndify.sample;

import android.app.Activity;
import android.os.AsyncTask;

/**
 * Created by devanshk on 10/3/14.
 */
public class MultiThread extends AsyncTask<Void,Integer,Void> {
    private final String TAG = "Async_LoadSoul";
    private Runnable runnable;
    private Activity parent;

    public MultiThread(Activity Parent, Runnable runnable){
        this.parent = Parent;
        this.runnable = runnable;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        parent.runOnUiThread(runnable);
        return null;
    }

    protected void onPostExecute(Void v){
    }
}
