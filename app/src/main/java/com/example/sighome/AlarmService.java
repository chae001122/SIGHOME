package com.example.sighome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.FileDescriptor;
import java.util.Locale;


public class AlarmService extends Service implements MqttCallback
{
    public static final String DEBUG_TAG = "MqttService"; // Debug TAG
    private static final String MQTT_THREAD_NAME = "MqttService[" + DEBUG_TAG + "]"; // Handler Thread ID
    private static final String MQTT_BROKER = "m2m.eclipse.org"; // Broker URL or IP Address
    private static final int MQTT_PORT = 1883; // Broker Port

    public static final int MQTT_QOS_0 = 0; // QOS Level 0 ( Delivery Once no confirmation )
    public static final int MQTT_QOS_1 = 1; // QOS Level 1 ( Delevery at least Once with confirmation )
    public static final int MQTT_QOS_2 = 2; // QOS Level 2 ( Delivery only once with confirmation with handshake )

    private static final int MQTT_KEEP_ALIVE = 240000; // KeepAlive Interval in MS
    private static final String MQTT_KEEP_ALIVE_TOPIC_FORAMT = "/users/%s/keepalive"; // Topic format for KeepAlives
    private static final byte[]     MQTT_KEEP_ALIVE_MESSAGE = { 0 }; // Keep Alive message to send
    private static final int MQTT_KEEP_ALIVE_QOS = MQTT_QOS_0; // Default Keepalive QOS

    private static final boolean MQTT_CLEAN_SESSION = true; // Start a clean session?

    private static final String MQTT_URL_FORMAT = "tcp://%s:%d"; // URL Format normally don't change

    private static final String ACTION_START  = DEBUG_TAG + ".START"; // Action to start
    private static final String ACTION_STOP   = DEBUG_TAG + ".STOP"; // Action to stop
    private static final String ACTION_KEEPALIVE= DEBUG_TAG + ".KEEPALIVE"; // Action to keep alive used by alarm manager
    private static final String ACTION_RECONNECT= DEBUG_TAG + ".RECONNECT"; // Action to reconnect
    private static final String ACTION_WINDOW = "window";
    private static final String ACTION_ENTER = "enter";
    private static final String ACTION_BELL = "bell";
    private static final String ACTION_FIRE = "fire";
    private static final String ACTION_GARAGE = "garage";

    private static final String DEVICE_ID_FORMAT = "andr_%s"; // Device ID Format, add any prefix you'd like

    private static boolean nonAalarm = false;

    private boolean mStarted = false;   // Is the Client started?
    private String mDeviceId;       // Device ID, Secure.ANDROID_ID
    private Handler mConnHandler;     // Seperate Handler thread for networking

    private MqttDefaultFilePersistence mDataStore; // Defaults to FileStore
    private MemoryPersistence mMemStore; // On Fail reverts to MemoryStore
    private MqttConnectOptions mOpts; // Connection Options

    private MqttTopic mKeepAliveTopic; // Instance Variable for Keepalive topic

    private MqttClient mClient; // Mqtt Client

    private AlarmManager mAlarmManager; // Alarm manager to perform repeating tasks
    private ConnectivityManager mConnectivityManager; // To check for connectivity changes




    public static void actionStart(Context ctx) {
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_START);
        ctx.startService(i);
    }

    public static void actionStop(Context ctx) {
        Intent i = new Intent(ctx,MqttService.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    public static void actionKeepalive(Context ctx) {
        Intent i = new Intent(ctx,MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        ctx.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDeviceId = String.format(DEVICE_ID_FORMAT,
                Secure.getString(getContentResolver(), Secure.ANDROID_ID));

        HandlerThread thread = new HandlerThread(MQTT_THREAD_NAME);
        thread.start();

        mConnHandler = new Handler(thread.getLooper());

        mDataStore = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());

        mOpts = new MqttConnectOptions();
        mOpts.setCleanSession(MQTT_CLEAN_SESSION);
        // Do not set keep alive interval on mOpts we keep track of it with alarm's

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();

        Log.i(DEBUG_TAG,"Received action of " + action);

        if(action == null) {
            Log.i(DEBUG_TAG,"Starting service with no action\n Probably from a crash");
        } else {
            if(action.equals(ACTION_START)) {
                nonAalarm = false;
                Log.i(DEBUG_TAG,"Received ACTION_START");
                start();
            } else if(action.equals(ACTION_STOP)) {
                Log.i(DEBUG_TAG,"Received ACTION_STOP");
                nonAalarm = true;
                //stop();
            } else if(action.equals(ACTION_KEEPALIVE)) {
                try {
                    mClient.publish("/phone/turnoff/enter", new MqttMessage("android".getBytes()));
                    mClient.publish("/phone/turnoff/bell", new MqttMessage("android".getBytes()));
                    mClient.publish("/phone/turnoff/window", new MqttMessage("android".getBytes()));
                    mClient.publish("/phone/turnoff/fire", new MqttMessage("android".getBytes()));
                    mClient.publish("/phone/turnoff/garage", new MqttMessage("android".getBytes()));
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                //keepAlive();
            } else if(action.equals(ACTION_RECONNECT)) {
                if(isNetworkAvailable()) {
                    reconnectIfNecessary();
                }
            }
        }

        return START_REDELIVER_INTENT;
    }
    private void sendMessage()
    {
        Log.d("messageService", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("message", "This is my first message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private synchronized void start() {
        if(mStarted) {
            Log.i(DEBUG_TAG,"Attempt to start while already started");
            return;
        }

        if(hasScheduledKeepAlives()) {
            stopKeepAlives();
        }

        connect();

        registerReceiver(mConnectivityReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private synchronized void stop() {
        if(!mStarted) {
            Log.i(DEBUG_TAG,"Attemtpign to stop connection that isn't running");
            return;
        }

        if(mClient != null) {
            mConnHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mClient.disconnect();
                    } catch(MqttException ex) {
                        ex.printStackTrace();
                    }
                    mClient = null;
                    mStarted = false;

                    stopKeepAlives();
                }
            });
        }

        unregisterReceiver(mConnectivityReceiver);
    }

    private synchronized void connect() {
        String url = String.format("tcp://111.118.51.164:1883");
        Log.i(DEBUG_TAG,"Connecting with URL: " + url);
        try {
            if(mDataStore != null) {
                Log.i(DEBUG_TAG,"Connecting with DataStore");
                mClient = new MqttClient(url,mDeviceId,mDataStore);
            } else {
                Log.i(DEBUG_TAG,"Connecting with MemStore");
                mClient = new MqttClient(url,mDeviceId,mMemStore);
            }
        } catch(MqttException e) {
            e.printStackTrace();
        }

        mConnHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mClient.connect(mOpts);

                    mClient.subscribe("/ras/enter", 0);
                    mClient.subscribe("/ras/window", 0);
                    mClient.subscribe("/ras/bell", 0);
                    mClient.subscribe("/ras/fire", 0);
                    mClient.subscribe("/ras/garage", 0);

                    mClient.setCallback(AlarmService.this);

                    mStarted = true; // Service is now connected

                    Log.i(DEBUG_TAG,"Successfully connected and subscribed starting keep alives");

                    startKeepAlives();
                } catch(MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + MQTT_KEEP_ALIVE,
                MQTT_KEEP_ALIVE, pi);
    }

    private void stopKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        mAlarmManager.cancel(pi);
    }

    private synchronized void keepAlive() {
        if(isConnected()) {
            try {
                sendKeepAlive();
                return;
            } catch(MqttConnectivityException ex) {
                ex.printStackTrace();
                reconnectIfNecessary();
            } catch(MqttPersistenceException ex) {
                ex.printStackTrace();
                stop();
            } catch(MqttException ex) {
                ex.printStackTrace();
                stop();
            }
        }
    }

    private synchronized void reconnectIfNecessary() {
        if(mStarted && mClient == null) {
            connect();
        }
    }

    private boolean isNetworkAvailable() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

        return (info == null) ? false : info.isConnected();
    }

    private boolean isConnected() {
        if(mStarted && mClient != null && !mClient.isConnected()) {
            Log.i(DEBUG_TAG,"Mismatch between what we think is connected and what is connected");
        }

        if(mClient != null) {
            return (mStarted && mClient.isConnected()) ? true : false;
        }

        return false;
    }

    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(DEBUG_TAG,"Connectivity Changed...");
        }
    };

    private synchronized MqttDeliveryToken sendKeepAlive()
            throws MqttConnectivityException, MqttPersistenceException, MqttException {
        if(!isConnected())
            throw new MqttConnectivityException();

        if(mKeepAliveTopic == null) {
            mKeepAliveTopic = mClient.getTopic(
                    String.format(Locale.US, MQTT_KEEP_ALIVE_TOPIC_FORAMT,mDeviceId));
        }

        Log.i(DEBUG_TAG,"Sending Keepalive to " + MQTT_BROKER);

        MqttMessage message = new MqttMessage(MQTT_KEEP_ALIVE_MESSAGE);
        message.setQos(MQTT_KEEP_ALIVE_QOS);

        return mKeepAliveTopic.publish(message);
    }

    private synchronized boolean hasScheduledKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE);

        return (pi != null) ? true : false;
    }

    IBinder mBinder =  new MyBinder();
    class MyBinder extends Binder implements IBinder {
        AlarmService getService() { // 서비스 객체를 리턴
            return AlarmService.this;
        }

        @Nullable
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Nullable
        @Override
        public IInterface queryLocalInterface(@NonNull String descriptor) {
            return null;
        }

        @Override
        public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {

        }

        @Override
        public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {

        }

        @Override
        public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
            return false;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {

        return mBinder;
    }


    @Override
    public void connectionLost(Throwable arg0) {
        stopKeepAlives();

        mClient = null;

        if(isNetworkAvailable()) {
            reconnectIfNecessary();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(topic.equals("/ras/enter")) {
            Log.i(DEBUG_TAG,"I GOT THIS1");

            //wait(1000);
            //mClient.publish("/phone/turnoff/enter", new MqttMessage("android".getBytes()));
            Log.i(DEBUG_TAG,"I Publish THIS1" + nonAalarm);
            if(!nonAalarm) {
                Log.i(DEBUG_TAG,"I Publish THIS2" + nonAalarm);
                Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(ACTION_ENTER);
                intent.putExtra("data", "침입자가 감지되었습니다.");
                startActivity(intent);
            }
            Log.i(DEBUG_TAG,"I Publish THIS2" + nonAalarm);
        }
        else if(topic.equals("/ras/bell"))
        {
            Log.i(DEBUG_TAG,"I GOT THIS1");
            //wait(1000);
            //mClient.publish("/phone/turnoff/enter", new MqttMessage("android".getBytes()));
            Log.i(DEBUG_TAG,"I Publish THIS2");
            if(!nonAalarm) {
                Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(ACTION_BELL);
                intent.putExtra("data", "초인종이 눌렸습니다.");
                startActivity(intent);
            }
        }
        else if(topic.equals("/ras/window")) {
            Log.i(DEBUG_TAG,"I GOT THIS1");
            //wait(1000);
            //mClient.publish("/phone/turnoff/enter", new MqttMessage("android".getBytes()));
            Log.i(DEBUG_TAG,"I Publish THIS3");
            if(!nonAalarm) {
                Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(ACTION_WINDOW);
                intent.putExtra("data", "창문 흔들림이 감지되었습니다.");
                startActivity(intent);
            }
        }
        else if(topic.equals("/ras/fire"))
        {

            Log.i(DEBUG_TAG,"I GOT THIS1");
            //wait(1000);
            //mClient.publish("/phone/turnoff/enter", new MqttMessage("android".getBytes()));
            Log.i(DEBUG_TAG,"I Publish THIS2");
            if(!nonAalarm) {
                Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(ACTION_FIRE);
                intent.putExtra("data", "화재가 감지되었습니다. ");
                startActivity(intent);
            }
        }
        else if(topic.equals("/ras/garage"))
        {
            Log.i(DEBUG_TAG,"I GOT THIS1");
            //wait(1000);
            //mClient.publish("/phone/turnoff/enter", new MqttMessage("android".getBytes()));
            Log.i(DEBUG_TAG,"I Publish THIS2");
            if(!nonAalarm) {
                Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(ACTION_GARAGE);
                intent.putExtra("data", "챠량 도난이 감지되었습니다.");
                startActivity(intent);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private class MqttConnectivityException extends Exception {
        private static final long serialVersionUID = -7385866796799469420L;
    }

    private class Binder {
    }
}