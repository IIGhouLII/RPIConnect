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
import android.widget.ToggleButton;

/**
 * Created by ghoul on 6/17/16.
 */
public class CheckStatus extends AppCompatActivity {
    private static final String TAG = "CheckStatus";
    private static final boolean DEBUG = false;

    private SSHConnection Connection;
    boolean mServiceBound = false;

    private ToggleButton MOCP;
    private ToggleButton Stream;
    private ToggleButton Pulse;
    private ToggleButton Buzz;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Log.d(TAG,"onServiceConnected()");
            SSHConnection.MyBinder myBinder = (SSHConnection.MyBinder) service;
            Connection = myBinder.getService();
            mServiceBound = true;
            updateStatus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG,"onCreate()");
        setContentView(R.layout.activity_check_status);

        MOCP = (ToggleButton) findViewById(R.id.mocp);
        Pulse= (ToggleButton) findViewById(R.id.pulseaudio);
        Buzz= (ToggleButton) findViewById(R.id.buzz);
        Stream= (ToggleButton) findViewById(R.id.stream);

        MOCP.setOnLongClickListener( new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (DEBUG) Log.d(TAG, "MOCP: LongClick");
                if (MOCP.isChecked()) {
                    Intent intent = new Intent(getApplicationContext(), MusicController.class);
                    startActivity(intent);
                }
                return true;
            }
        });

        Stream.setOnLongClickListener( new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (DEBUG) Log.d(TAG, "Stream: LongClick");

//                if (Stream.isChecked()) {
                    Intent intent = new Intent(getApplicationContext(), LiveStreaming.class);
                    intent.putExtra("Hostname",Connection.getHostname());
                    startActivity(intent);
//                }
                return true;
            }
        });
    }

    private void updateStatus(){
        String Current_Status = Connection.interactiveCommand(getString(R.string.Command_status),0);
        if (DEBUG) Log.d(TAG, "Status= " + Current_Status);
        if (Current_Status != null) {
            if (Current_Status.charAt(0) == '1') {
                MOCP.setChecked(true);
                if (DEBUG) Log.d(TAG, "Mocp Toggled true");
            } else {
                MOCP.setChecked(false);
                if (DEBUG) Log.d(TAG, "Mocp Toggled false");
            }
            if (Current_Status.charAt(1) == '1') {
                Pulse.setChecked(true);
                if (DEBUG) Log.d(TAG, "Pulse Toggled true");
            } else {
                Pulse.setChecked(false);
                if (DEBUG) Log.d(TAG, "Pulse Toggled false");
            }
            if (Current_Status.charAt(2) == '1') {
                Buzz.setChecked(true);
                if (DEBUG) Log.d(TAG, "Buzz Toggled true");
            } else {
                Buzz.setChecked(false);
                if (DEBUG) Log.d(TAG, "Buzz Toggled false");
            }
            if (Current_Status.charAt(3) == '1') {
                Stream.setChecked(true);
                if (DEBUG) Log.d(TAG, "Stream Toggled true");
            } else {
                Stream.setChecked(false);
                if (DEBUG) Log.d(TAG, "Stream Toggled false");
            }
        }
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

    public void PulseonClick(View view){
        if (DEBUG) Log.d(TAG,"Pulse: Click");
        int  state;
        if (Pulse.isChecked()) {
            state = Connection.execCommand(getString(R.string.Command_pulseaudio_on));
            if (state != 0) Pulse.setChecked(false);
        } else {
            state = Connection.execCommand(getString(R.string.Command_pulseaudio_off));
            if (state != 0) Pulse.setChecked(true);
        }
    }
    public void MOCPonClick(View view){
        if (DEBUG) Log.d(TAG,"MOCP: Click");
        int  state;
        if (MOCP.isChecked()) {
            state = Connection.execCommand(getString(R.string.Command_mocp_on));
            if (state != 0) MOCP.setChecked(false);
        } else {
            state = Connection.execCommand(getString(R.string.Command_mocp_off));
            if (state != 0) MOCP.setChecked(true);
        }
    }

    public void Buzz(View view){
        if (DEBUG) Log.d(TAG,"Buzz: Click");
        int  state;
        if (Buzz.isChecked()) {
            state = Connection.execCommand(getString(R.string.Command_buzz_on));
            if (state != 0) Buzz.setChecked(false);
        } else {
            state = Connection.execCommand(getString(R.string.Command_buzz_off));
            if (state != 0) Buzz.setChecked(true);
        }
    }

    public void StreamOnClick(View view){
        if (DEBUG) Log.d(TAG,"Stream: Click");
        int  state;
        if (Stream.isChecked()) {
            state = Connection.execCommand(getString(R.string.Command_stream_on));
            if (state != 0) Stream.setChecked(false);
        } else {
            state = Connection.execCommand(getString(R.string.Command_stream_off));
            if (state != 0) Stream.setChecked(true);
        }
    }
}
