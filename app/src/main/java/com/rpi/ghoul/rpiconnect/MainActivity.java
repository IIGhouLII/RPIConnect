package com.rpi.ghoul.rpiconnect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = false;

    private EditText User, Hostname, Port;
    private SSHConnection Connection;
    private TextView ToastText;
    private Toast Toast;
    private boolean mServiceBound = false;

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
        setContentView(R.layout.activity_main);
        User = (EditText) findViewById(R.id.user);
        Hostname = (EditText) findViewById(R.id.hostname);
        Port = (EditText) findViewById(R.id.port);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
        ToastText = (TextView) layout.findViewById(R.id.text);
        Toast = new Toast(getApplicationContext());
        Toast.setDuration(Toast.LENGTH_SHORT);
        Toast.setView(layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG,"onStart()");
        Intent intent = new Intent(this, SSHConnection.class);
        startService(intent);
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

    public void Connect(View view){
        if (DEBUG) Log.d(TAG,"Connect()");
        if (mServiceBound) {
            Connection.Connect(User.getText().toString(), Hostname.getText().toString(), Port.getText().toString());
        }
    }

    public void Disconnect(View view){
        if (DEBUG) Log.d(TAG,"Disconnect()");
        if (mServiceBound) {
            Connection.Disconnect();
        }
    }

    public void StatusIntent(View view){
        if (DEBUG) Log.d(TAG,"StatusIntent()");
        if (Connection.CheckConnection()== SSHConnection.ConnectionState.SessionConnected){
            Intent intent = new Intent(this, CheckStatus.class);
            startActivity(intent);
        }else{
            ToastText.setText(R.string.PleaseConnect);
            Toast.show();
        }
    }
}

