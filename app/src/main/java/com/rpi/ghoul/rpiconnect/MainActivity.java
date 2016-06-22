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
    private EditText User, Hostname, Port;
    private SSHConnection Connection;
    private TextView ToastText;
    private Toast toast;
    private boolean mServiceBound = false;
    private String TAG = "MainActivity";


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
        Log.i(TAG,"Created Activity");
        setContentView(R.layout.activity_main);
        User = (EditText) findViewById(R.id.user);
        Hostname = (EditText) findViewById(R.id.hostname);
        Port = (EditText) findViewById(R.id.port);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
        ToastText = (TextView) layout.findViewById(R.id.text);
        toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"Started Activity");
        Intent intent = new Intent(this, SSHConnection.class);
        startService(intent);
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

    public void Connect(View view){
        Log.i(TAG, "Button Connect pressed");
        if (mServiceBound) {
            Connection.Connect(User.getText().toString(), Hostname.getText().toString(), Port.getText().toString());
        }
    }

    public void Disconnect(View view){
        Log.i(TAG, "Button Disconnect pressed");
        if (mServiceBound) {
            Connection.Disconnect();
        }
    }

    public void StatusIntent(View view){
        Log.i(TAG, "Button Status pressed");
        if (Connection.CheckConnection()== SSHConnection.ConnectionState.SessionConnected){
            Intent intent = new Intent(this, CheckStatus.class);
            startActivity(intent);
        }else{
            ToastText.setText("Please connect!");
            toast.show();
        }
    }
}

