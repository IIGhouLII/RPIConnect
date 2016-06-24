package com.rpi.ghoul.rpiconnect;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by ghoul on 6/23/16.
 */
public class LiveStreaming extends AppCompatActivity {
    private static final String TAG = "LiveStreaming";
    private static final boolean DEBUG = false;

    final Handler handler = new Handler();
    private static MjpegView Stream;
    private String Hostname="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_streaming);
        Stream = (MjpegView) findViewById(R.id.frame);
        Intent i = getIntent();
        Hostname = i.getStringExtra("Hostname");
        Stream.init(this);
        new DoRead().execute("http://"+Hostname+":8090/?action=stream");
//        new DoRead().execute("http://trackfield.webcam.oregonstate.edu/axis-cgi/mjpg/video.cgi");
    }
    @Override
    public void onStop() {
        if (DEBUG) Log.d(TAG, "onStop()");
        Stream.stopPlayback();
        super.onStop();
    }
    @Override
    public void onPause() {
        if (DEBUG) Log.d(TAG, "Stream stopped");
        Stream.stopPlayback();
        super.onPause();
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {

        protected MjpegInputStream doInBackground(String... surl) {
            // TODO: if camera has authentication deal with it and don't just not work
            try {
                URL url = new URL(surl[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                return new MjpegInputStream(urlConnection.getInputStream());
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(MjpegInputStream result) {
            Stream.setSource(result);
            Stream.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            Stream.showFps(true);
        }
    }

    public void setImageError() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTitle("Error on preparing images");
                return;
            }
        });
    }
}
