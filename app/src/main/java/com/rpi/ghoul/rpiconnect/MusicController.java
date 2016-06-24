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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MusicController extends AppCompatActivity {
    private static final String TAG = "MusicController";
    private static final boolean DEBUG = false;

    private SSHConnection Connection;
    boolean mServiceBound = false;


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
        if (DEBUG) Log.d(TAG,"onCreate()");
        setContentView(R.layout.activity_music_controller);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG,"onStart()");
        Intent intent = new Intent(this, SSHConnection.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.d(TAG,"onStop()");
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(TAG,"onDestroy()");
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        Intent intent = new Intent(this, SSHConnection.class);
        stopService(intent);
        super.onDestroy();
    }

    public void Play_Pause(View view){
        if (DEBUG) Log.d(TAG,"Play_Pause");
        Connection.execCommand(getString(R.string.Command_mocp_play_pause));
    }

    public void Next(View view){
        if (DEBUG) Log.d(TAG,"Next");
        Connection.execCommand(getString(R.string.Command_mocp_next));
    }
    public void Previous(View view){
        if (DEBUG) Log.d(TAG,"Previous");
        Connection.execCommand(getString(R.string.Command_mocp_prev));
    }

    public void Vol_Up(View view){
        if (DEBUG) Log.d(TAG,"Vol_Up");
        Connection.execCommand(getString(R.string.Command_vol_up));
    }

    public void Vol_Down(View view){
        if (DEBUG) Log.d(TAG,"Vol_Down");
        Connection.execCommand(getString(R.string.Command_vol_down));
    }
}
