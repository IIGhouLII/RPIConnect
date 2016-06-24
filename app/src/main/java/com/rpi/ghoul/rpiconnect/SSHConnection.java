package com.rpi.ghoul.rpiconnect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ghoul on 6/17/16.
 */
public class SSHConnection extends Service{
    private static final String TAG = "SSHConnection";
    private static final boolean DEBUG = false;

    private JSch jsch;
    private Session session;
    private Channel channel;

    private IBinder mBinder = new MyBinder();
    private TextView ToastText;
    private Toast Toast;
    private AsyncTask Task = null;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG,"onCreate()");
        this.jsch=null;
        this.session=null;
        this.channel=null;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_layout, null);
        ToastText = (TextView) layout.findViewById(R.id.text);
        Toast = new Toast(getApplicationContext());
        Toast.setDuration(Toast.LENGTH_SHORT);
        Toast.setView(layout);
    }

    public String getHostname() {
        return session.getHost();
    }

    public IBinder onBind(Intent arg0) {
        if (DEBUG) Log.d(TAG,"onBind()");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        if (DEBUG) Log.d(TAG,"onRebind()");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.d(TAG,"onUnbind()");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG,"onStartCommand()");
        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG,"onDestroy()");
        super.onDestroy();
    }


    public void Connect(String User, String Hostname, String Port){
        if ((Task==null) || (Task.getStatus() == AsyncTask.Status.FINISHED))
            Task = new SSHConnectTask().execute(User,Hostname,Port);
    }

    public void Disconnect(){
        ConnectionState State=CheckConnection();
        if ((State == ConnectionState.JschNull) || (State == ConnectionState.SessionNull)){
            if (DEBUG) Log.d(TAG,"Jsch=null or Session=null");
            ToastText.setText(R.string.NoConnection); Toast.show();
        }else if (State==ConnectionState.SessionConnected) {
            session.disconnect();
            if (DEBUG) Log.d(TAG, "Session isConnected="+Boolean.toString(session.isConnected()));
            ToastText.setText(R.string.Disconnected); Toast.show();
        }else{
            if (DEBUG) Log.d(TAG, "Session isConnected="+Boolean.toString(session.isConnected()));
            ToastText.setText(R.string.AlreadyDisconnected); Toast.show();
        }
    }

    public enum ConnectionState {
        JschNull,
        SessionNull,
        SessionDisconnected,
        SessionConnected,
    }

    public ConnectionState CheckConnection() {
        if (jsch == null){
            return ConnectionState.JschNull;
        }else if (session==null){
            return ConnectionState.SessionNull;
        }else if (session.isConnected()){
            return ConnectionState.SessionConnected;
        }else{
            return ConnectionState.SessionDisconnected;
        }
    }

    public int execCommand(String command){
        try {
            ConnectionState State =CheckConnection();
            if (State == ConnectionState.SessionConnected){
                channel =session.openChannel("exec");
                ((ChannelExec)channel).setCommand(command);
//                channel.setInputStream(null);
                channel.connect();
                channel.disconnect();
                return 0;
            }else{
                ToastText.setText(R.string.ConnectionDown); Toast.show();
                return 1;
            }
        }catch (Exception e) {
            ToastText.setText(R.string.Error + e.getMessage()); Toast.show();
            return 2;
        }
    }

    public String interactiveCommand(String command,int SleepTime){
        try {
            String Output=null;
            ConnectionState State =CheckConnection();
            if (State == ConnectionState.SessionConnected) {
                channel =session.openChannel("exec");
                ((ChannelExec)channel).setCommand(command);
                channel.setInputStream(null);
                InputStream in=channel.getInputStream();
                channel.connect();
                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        Output = new String(tmp, 0, i);
                        if (DEBUG) Log.d(TAG, "Received Output ="+Output);
                    }
                    if (channel.isClosed()) {
                        if (in.available() > 0) continue;
                        if (DEBUG) Log.d(TAG, "exit-status: " + channel.getExitStatus());
                        break;
                    }
                    try { Thread.sleep(SleepTime); } catch (Exception ee) { }
                }
                channel.disconnect();
                return Output;
            }else{
                if (DEBUG) Log.d(TAG, "Session is not connected");
                ToastText.setText(R.string.ConnectionDown); Toast.show();
                return null;
            }
        }catch (Exception e) {
            ToastText.setText(R.string.Error + e.getMessage()); Toast.show();
            return null;
        }
    }


    public class MyBinder extends Binder {
        SSHConnection getService() {
            return SSHConnection.this;
        }
    }

    public class SSHConnectTask extends AsyncTask<String, Context, String> {
        private String TAG = "SSHConnectTask";
        private int ConnectionTimeOut=3000;

        protected void onPreExecute() {
            ToastText.setText(R.string.Connecting); Toast.show();
        }

        protected String doInBackground(String... arg0) {
            try {
                String User = arg0[0];
                String Hostname = arg0[1];
                int Port = Integer.parseInt(arg0[2]);
                ConnectionState State = CheckConnection();
                Log.i(TAG,"State= " + State.toString());
                if (State == ConnectionState.JschNull) {
                    jsch = new JSch();
                    String IdentityFile = Environment.getExternalStorageDirectory() + "/.ssh/id_rsa";
                    String knownHosts =Environment.getExternalStorageDirectory() + "/.ssh/known_hosts";

                    jsch.setKnownHosts(knownHosts);
                    jsch.addIdentity(IdentityFile,"");

                    Properties props = new Properties();
                    props.put("PreferredAuthentications", "publickey");
                    if (jsch.getHostKeyRepository().getHostKey(Hostname, null) == null) {
                        props.put("StrictHostKeyChecking", "no");
                    }
                    session = jsch.getSession(User, Hostname, Port);
                    session.setConfig(props);
                    session.connect(ConnectionTimeOut);
                    if (DEBUG) Log.d(TAG, "Session isConnected="+Boolean.toString(session.isConnected()));
                    return getString(R.string.Connected);
                }else if (State == ConnectionState.SessionConnected) {
                    if (!session.getHost().equals(Hostname) || session.getPort()!=Port || !session.getUserName().equals(User)) {
                        session.disconnect();
                        session = jsch.getSession(User, Hostname, Port);
                        session.connect(ConnectionTimeOut);
                        if (DEBUG) Log.d(TAG, "Session isConnected="+Boolean.toString(session.isConnected()));
                        return getString(R.string.Connected);
                    }else{
                        if (DEBUG) Log.d(TAG, "Session isConnected="+Boolean.toString(session.isConnected()));
                        return getString(R.string.AlreadyConnected);
                    }
                }else{
                    session = jsch.getSession(User, Hostname, Port);
                    session.connect(ConnectionTimeOut);
                    if (DEBUG) Log.d(TAG, "Session isConnected="+Boolean.toString(session.isConnected()));
                    return getString(R.string.Connected);
                }
            } catch (Exception e) {
                return getString(R.string.Error) + e.getMessage();
            }
        }

        protected void onPostExecute(String result) {
            ToastText.setText(result);
            Toast.show();
        }
    }
}
