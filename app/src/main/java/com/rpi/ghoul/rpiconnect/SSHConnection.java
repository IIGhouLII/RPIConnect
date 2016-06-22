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
    private JSch jsch;
    private Session session;
    private Channel channel;
    private String TAG = "SSHConnection";
    private IBinder mBinder = new MyBinder();
    private TextView ToastText;
    private Toast toast;
    private AsyncTask Task = null;



    @Override
    public void onCreate() {
        Log.i(TAG, "in onCreate");
        this.jsch=null;
        this.session=null;
        this.channel=null;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_layout, null);
        ToastText = (TextView) layout.findViewById(R.id.text);
        toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
    }

    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "in onUnbind");
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "in onStartCommand");
        // Let it continue running until it is stopped.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "in onDestroy");
        super.onDestroy();
    }


    public void Connect(String User, String Hostname, String Port){
        if (Task==null)
            Task = new SSHConnectTask().execute(User,Hostname,Port);
        else if (Task.getStatus() == AsyncTask.Status.FINISHED)
            Task = new SSHConnectTask().execute(User,Hostname,Port);
    }

    public void Disconnect(){
        ConnectionState State=CheckConnection();
        if ((State == ConnectionState.JschNull) || (State == ConnectionState.SessionNull)){
            ToastText.setText("No connection found"); toast.show();
        }else if (State==ConnectionState.SessionConnected) {
            session.disconnect();
            Log.i(TAG, "Session Connected="+Boolean.toString(session.isConnected()));
            ToastText.setText("Disconnected"); toast.show();
        }else{
            Log.i(TAG, "Session Connected="+Boolean.toString(session.isConnected()));
            ToastText.setText("Already Disconnected!"); toast.show();
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
        }else if (!session.isConnected()){
            return ConnectionState.SessionDisconnected;
        }else{
            return ConnectionState.SessionConnected;
        }
    }

    public int execCommand(String command){
        try {
            ConnectionState State =CheckConnection();
            if (State == ConnectionState.SessionConnected){
                channel =session.openChannel("exec");
                ((ChannelExec)channel).setCommand(command);
                channel.setInputStream(null);
                InputStream in=channel.getInputStream();
                channel.connect();
                channel.disconnect();
                return 0;
            }else{
                ToastText.setText("Connection is down"); toast.show();
                return 1;
            }
        }catch (Exception e) {
            ToastText.setText("Error: " + e.getMessage()); toast.show();
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
                        Log.i(TAG, Output);
                    }
                    if (channel.isClosed()) {
                        if (in.available() > 0) continue;
                        Log.i(TAG, "exit-status: " + channel.getExitStatus());
                        break;
                    }
                    try { Thread.sleep(SleepTime); } catch (Exception ee) { }
                }
                channel.disconnect();
                return Output;
            }else{
                ToastText.setText("Connection is down"); toast.show();
                return null;
            }
        }catch (Exception e) {
            ToastText.setText("Error: " + e.getMessage()); toast.show();
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
            ToastText.setText("Connecting..."); toast.show();
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
                    Log.i(TAG, "Session Connected="+Boolean.toString(session.isConnected()));
                    return "Connected to Pi";
                }else if ((State == ConnectionState.SessionNull) || (State == ConnectionState.SessionDisconnected)){
                    session = jsch.getSession(User, Hostname, Port);
                    session.connect(ConnectionTimeOut);
                    Log.i(TAG, "Session Connected="+Boolean.toString(session.isConnected()));
                    return "Connected to Pi";
                }else if (State == ConnectionState.SessionConnected) {
                    if (!session.getHost().equals(Hostname) || session.getPort()!=Port || !session.getUserName().equals(User)) {
                        session.disconnect();
                        session = jsch.getSession(User, Hostname, Port);
                        session.connect(ConnectionTimeOut);
                        Log.i(TAG, "Session Connected="+Boolean.toString(session.isConnected()));
                        return "Connected to Pi";
                    }else{
                        Log.i(TAG, "Session Connected="+Boolean.toString(session.isConnected()));
                        return "Already Connected";
                    }
                }else{
                    Log.i(TAG, "Session Connected="+Boolean.toString(session.isConnected()));
                    return "Error: This case is not studied";
                }
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
        protected void onCancelled(String result){
            ToastText.setText("Connection task canceled!");toast.show();
        }
        protected void onPostExecute(String result) {
            ToastText.setText(result);toast.show();
        }
    }
}
