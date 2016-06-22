package com.rpi.ghoul.rpiconnect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MusicController extends AppCompatActivity {
    private SSHConnection Connection;
    boolean mServiceBound = false;
    private String TAG = "MusicController";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SSHConnection.MyBinder myBinder = (SSHConnection.MyBinder) service;
            Connection = myBinder.getService();
            mServiceBound = true;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_controller);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"Started Activity");
        Intent intent = new Intent(this, SSHConnection.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"Stopped Activity");
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"Destroyed Activity");
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        Intent intent = new Intent(this, SSHConnection.class);
        stopService(intent);
    }

    public void Play_Pause(View view){
        Log.i(TAG, "Button Play pressed");
        Connection.execCommand("mocp -G > /dev/null");
    }

    public void Next(View view){
        Log.i(TAG, "Button Next pressed");
        Connection.execCommand("mocp -f > /dev/null");
    }
    public void Previous(View view){
        Log.i(TAG, "Button Previous pressed");
        Connection.execCommand("mocp -r > /dev/null");
    }

    public void Vol_Up(View view){
        Log.i(TAG, "Button Vol_Up pressed");
        Connection.execCommand("amixer -c 0 set PCM  1dB+ > /dev/null");
    }

    public void Vol_Down(View view){
        Log.i(TAG, "Button Vol_Down pressed");
        Connection.execCommand("amixer -c 0 set PCM  1dB- > /dev/null");
    }
}
