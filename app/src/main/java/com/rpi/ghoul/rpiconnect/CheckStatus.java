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

public class CheckStatus extends AppCompatActivity {
    private SSHConnection Connection;
    boolean mServiceBound = false;
    private String TAG = "CheckStatus";
    private ToggleButton MOCP;
    private ToggleButton Pulse;
    private ToggleButton Buzz;

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
            updateStatus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_status);

        MOCP = (ToggleButton) findViewById(R.id.mocp);
        Pulse= (ToggleButton) findViewById(R.id.pulseaudio);
        Buzz= (ToggleButton) findViewById(R.id.buzz);

        MOCP.setOnLongClickListener( new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                Log.i(TAG, "MOCP button long pressed ");
                if (MOCP.isChecked()) {
                    Intent intent = new Intent(getApplicationContext(), MusicController.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    private void updateStatus(){
        String Current_Status = Connection.interactiveCommand("./RPIConnect -Check_Status",0);
        Log.i(TAG, "the received string is: " + Current_Status);
        if (Current_Status != null) {
            if (Current_Status.charAt(0) == '1') {
                MOCP.setChecked(true);
                Log.i(TAG, "Mocp Toggled true");
            } else {
                MOCP.setChecked(false);
                Log.i(TAG, "Mocp Toggled false");
            }
            if (Current_Status.charAt(1) == '1') {
                Pulse.setChecked(true);
                Log.i(TAG, "Pulse Toggled true");
            } else {
                Pulse.setChecked(false);
                Log.i(TAG, "Pulse Toggled false");
            }
            if (Current_Status.charAt(2) == '1') {
                Buzz.setChecked(true);
                Log.i(TAG, "Buzz Toggled true");
            } else {
                Buzz.setChecked(false);
                Log.i(TAG, "Buzz Toggled false");
            }
        }
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

    public void PulseonClick(View view){
        int  state;
        if (Pulse.isChecked()) {
            state = Connection.execCommand("pulseaudio -D");
            if (state > 0)
                Pulse.setChecked(false);
        } else {
            state = Connection.execCommand("pulseaudio -k");
            if (state > 0)
                Pulse.setChecked(true);
        }
    }
    public void MOCPonClick(View view){
        int  state;
        Log.i(TAG, "MOCP button clicked");
        if (MOCP.isChecked()) {
            state = Connection.execCommand("mocp -S \n sleep 2 \n mocp -p");
            if (state > 0) MOCP.setChecked(false);
        } else {
            state = Connection.execCommand("mocp -x");
            if (state > 0) MOCP.setChecked(true);
        }
    }

    public void Buzz(View view){
        int  state;
        Log.i(TAG, "Buzz button clicked");
        if (Buzz.isChecked()) {
            state = Connection.execCommand("./RPIConnect -Buzz Start");
            if (state > 0) Buzz.setChecked(false);
        } else {
            state = Connection.execCommand("./RPIConnect -Buzz Close");
            if (state > 0) Buzz.setChecked(true);
        }
    }
}
