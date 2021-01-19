package com.daftarirn.mahabhartiapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;


public class SplashActivity extends Activity {

    TextView loading;
    Button retry;
    private Tracker tracker;
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.

    }

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = analytics.newTracker(getString(R.string.analytics_property_id));
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        loading = (TextView)findViewById(R.id.loading);
        retry = (Button)findViewById(R.id.retry);

        // ---------------------- ANALYTICS ---------------------

        GoogleAnalytics.getInstance(this).newTracker(getString(R.string.analytics_property_id));
        GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        tracker = getTracker(TrackerName.APP_TRACKER);
        tracker.setScreenName("SplashActivity");
        tracker.send(new HitBuilders.AppViewBuilder().build());


        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (waited < Integer.parseInt(getString(R.string.splash_delay))) {
                        sleep(100);
                        waited += 100;
                    }

                } catch (InterruptedException e) {
                    // do nothing
                } finally {

                   connectionStatus();


                }
            }
        };
        splashThread.start();




    }

    public void connectionStatus () {

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            // notify user you are online

            // Toast.makeText(getApplicationContext(),"Connection Success ! महाराष्ट्रातील सर्व जॉब अपडेट्स ",Toast.LENGTH_LONG).show();
            finish();

            Intent i = new Intent(getBaseContext(), MainActivity.class);
            startActivity(i);


        } else if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            // notify user you are not online

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    // Stuff that updates the UI
                    loading.setText("Please Check Your Internet Connection !");
                    loading.setGravity(Gravity.CENTER_HORIZONTAL);
                    loading.setTextSize(20);
                    retry.setVisibility(View.VISIBLE);


                }
            });


        }
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionStatus();
            }
        });
    }
}