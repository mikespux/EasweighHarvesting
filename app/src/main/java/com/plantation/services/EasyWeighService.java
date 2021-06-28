package com.plantation.services;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.plantation.activities.ScaleEasyWeighActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;


public class EasyWeighService extends Service {
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    // Message codes received from the UI client.
    // Register client with this service.
    public static final int MSG_REG_CLIENT = 200;
    // Unregister client from this service.
    public static final int MSG_UNREG_CLIENT = 201;
    public static final int RETRY = 500;
    public static final int RECONNECT = 501;
    public static final int PRINT_REQUEST = 503;
    public static final int INIT_WEIGHING = 505;
    public static final int ZERO_SCALE = 506;
    public static final int TARE_SCALE = 507;
    public static final int READING_PROBE = 510;
    public static final int DISCONNECT = 511;

    public static final String EASYWEIGH_VERSION_15 = "EW15";
    public static final String EASYWEIGH_VERSION_11 = "EW11";
    public static final String DR_150 = "DR-150";

    public static final String WEIGH_AND_TARE = "Discrete";
    public static final String FILLING = "Incremental";
    @SuppressLint("NewApi")
    // Debugging
    private static final String TAG = "EasyWeighService";
    private static final boolean D = true;
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "WeighingSecure";
    // Unique UUID for this application
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String MY_DB = "com.octagon.easyweigh_preferences";
    public static int mState;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ProgressDialog mProcessDialog;
    String deviceAddress, scaleVersion;

    SharedPreferences mSharedPrefs, prefs;
    boolean readingProbed = false;
    boolean printingInProgress = false;
    BluetoothSocket mmSocket = null;
    InputStream mmInStream;
    OutputStream mmOutStream;
    BluetoothDevice mmDevice;
    // Member fields
    //private final BluetoothAdapter mAdapter;
    //private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mChannelId;
    private Messenger mClient;
    private BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private String mSocketType;

    String stableReadingCounter;
    int milliSeconds = 0;
    int WeightCount = 0;

    /**
     * Make sure Bluetooth and health profile are available on the Android device.  Stop service
     * if they are not available.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ScaleEasyWeighActivity Service is running.");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Bluetooth adapter isn't available.  The client of the service is supposed to
            // verify that it is available and activate before invoking this service.
            EasyWeighService.this.stop();
            return;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Bundle b = intent.getExtras();
            deviceAddress = b.getString(ScaleEasyWeighActivity.EXTRA_DEVICE_ADDRESS);
            scaleVersion = b.getString(ScaleEasyWeighActivity.SCALE_VERSION);

        } catch (Exception e) {
            Log.e(TAG, "Failure on Bundle " + e.toString());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {

        return mMessenger.getBinder();
    }

    private void tumaMessage(Message msg) {
        if (mClient == null) {
            Log.d(TAG, "No Clients Registered");
            return;
        }

        try {
            mClient.send(msg);
        } catch (RemoteException e) {
            // Unable to reach client.
            e.printStackTrace();
        }
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        //mHandler.obtainMessage(ScaleEasyWeighActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        Message msg = Message.obtain(null, ScaleEasyWeighActivity.MESSAGE_STATE_CHANGE, state, -1);
        tumaMessage(msg);
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        //Message msg = mHandler.obtainMessage(ScaleEasyWeighActivity.MESSAGE_DEVICE_NAME);
        Message msg = Message.obtain(null, ScaleEasyWeighActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ScaleEasyWeighActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        tumaMessage(msg);

        setState(STATE_CONNECTED);

        if (mSharedPrefs.getString("scaleVersion", "EW15").equals(EASYWEIGH_VERSION_15)) {
            Log.i(TAG, "Algo is " + mSharedPrefs.getString("weighingAlgorithm", "Incremental"));
            if (mSharedPrefs.getString("weighingAlgorithm", "Incremental").equals(FILLING)) {
                Log.i(TAG, "Sending tare command");
                new Thread("TaringThread") {
                    @Override
                    public void run() {
                        try {
                            String message = "T";
                            byte[] send = message.getBytes();
                            write(send);
                            sleep(250);
                            //return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            interrupt();
                        }
                    }
                }
                        .start();
            }
        }

        new Thread("WeighingThread") {
            @Override
            public void run() {
                try {
                    stableReadingCounter = mSharedPrefs.getString("stabilityReadingCounter", "3");
                    //milliSeconds=mSharedPrefs.getString("milliSeconds", "200");
                    milliSeconds = 250;
                    SharedPreferences.Editor edit = prefs.edit();
                    //get scale version
                    switch (scaleVersion) {
                        case EASYWEIGH_VERSION_11:
                        case DR_150:
                            //  sending R to get readings from BT Scale
                            while (true) {
                                String message = "R" + "\r" + "\n";
                                byte[] send = message.getBytes();
                                write(send);
                                sleep(milliSeconds);

                                edit.putInt("WeightCount", WeightCount);
                                edit.apply();
                                WeightCount = WeightCount + 1;

                                if (WeightCount == Integer.parseInt(stableReadingCounter)) {
                                    WeightCount = 0;
                                }
                                //return;
                            }
                        case EASYWEIGH_VERSION_15:
                            //  sending R to get readings from BT Scale
                            while (true) {
                                String message = "R";
                                byte[] send = message.getBytes();
                                write(send);
                                sleep(milliSeconds);

                                edit.putInt("WeightCount", WeightCount);
                                edit.apply();
                                WeightCount = WeightCount + 1;

                                if (WeightCount == Integer.parseInt(stableReadingCounter)) {
                                    WeightCount = 0;
                                }
                                //return;
                            }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
                .start();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        setState(STATE_NONE);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {

            mBluetoothAdapter.cancelDiscovery();
        }
        connectionFailed();
        EasyWeighService.this.stopSelf();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(ScaleEasyWeighActivity.MESSAGE_TOAST);
        Message msg = Message.obtain(null, ScaleEasyWeighActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ScaleEasyWeighActivity.TOAST, "Unable to connect scale");
        msg.setData(bundle);
        tumaMessage(msg);
        //mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        EasyWeighService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(ScaleEasyWeighActivity.MESSAGE_TOAST);
        Message msg = Message.obtain(null, ScaleEasyWeighActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ScaleEasyWeighActivity.TOAST, "Scale Disconnected");
        msg.setData(bundle);
        tumaMessage(msg);

        setState(STATE_NONE);
        // Start the service overs to restart listening mode

    }

    /**
     * Constructor. Prepares a new ScaleEasyWeighActivity session.
     *
     * @param //context The UI Activity Context
     * @param //handler A Handler to send messages back to the UI Activity
     */

    // Handles events sent by {@link HealthHDPActivity}.
    @SuppressLint("HandlerLeak")
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Register UI client to this service so the client can receive messages.
                case MSG_REG_CLIENT:
                    Log.d(TAG, "Activity client registered");
                    mClient = msg.replyTo;

                    Log.i(TAG, "Got Address [" + ScaleEasyWeighActivity.getAddress() + "]");
                    //String deviceAddress = ScaleEasyWeighActivity.getAddress().toString();

                    if (deviceAddress != null && deviceAddress.length() > 0) { //Check if we have a valid bluetooth Mac Address
                        try {//Attempting to connect using got address
                            Log.i(TAG, "mState is " + mState);
                            if (mState != STATE_CONNECTED) {
                                mDevice = mBluetoothAdapter.getRemoteDevice(ScaleEasyWeighActivity.getAddress());
                                connect(mDevice, false);
                            }

                        } catch (Exception e) { //we failed so fire device list activity
                            Log.e(TAG, "Error During Connection " + e.getMessage());
                        }
                    } else { //if we do not have one the get one via DeviceListActivity
                        //Send a message to UI activity to request DeviceListActivity
                        Log.i(TAG, "Sending Message to UI activity");
                        try {
                            Message msg2 = Message.obtain(null, ScaleEasyWeighActivity.REQUEST_DEVICEADDRESS);
                            tumaMessage(msg2);
                        } catch (Exception e2) {
                            Log.e(TAG, "Error Sending Message " + e2.getMessage());
                        }
                    }

                    break;
                // Unregister UI client from this service.
                case MSG_UNREG_CLIENT:
                    mClient = null;
                    break;
                case RETRY:
                    try {

                        deviceAddress = msg.getData().getString(ScaleEasyWeighActivity.EXTRA_DEVICE_ADDRESS);

                        mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                        connect(mDevice, false);

                    } catch (Exception e) {
                        Log.e(TAG, "Unable to reach client to request address " + e.getMessage());
                    }
                    break;
                case RECONNECT:
                    deviceAddress = msg.getData().getString(ScaleEasyWeighActivity.EXTRA_DEVICE_ADDRESS);

                    if (deviceAddress.equals("") || deviceAddress.equals(null)) {
                        //Complete failure
                        Message totalFailure = Message.obtain(null, ScaleEasyWeighActivity.COMPLETE_FAILURE);
                        tumaMessage(totalFailure);
                    } else {
                        mDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                        connect(mDevice, false);
                    }
                    break;

                case TARE_SCALE:
                    new Thread("TaringThread") {
                        @Override
                        public void run() {
                            try {
                                String message = "T";
                                byte[] send = message.getBytes();
                                write(send);
                                sleep(250);
                                //return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                interrupt();
                            }
                        }
                    }
                            .start();
                    break;
                case ZERO_SCALE:
                    new Thread("TaringThread") {
                        @Override
                        public void run() {
                            try {
                                String message = "Z";
                                byte[] send = message.getBytes();
                                write(send);
                                sleep(250);
                                //return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                interrupt();
                            }
                        }
                    }
                            .start();
                    break;
                case READING_PROBE:
                    String message = "R";
                    byte[] send = message.getBytes();

                    try {
                        readingProbed = true;
                        write(send);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {


        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    try {
                        Method m = device.getClass().getMethod("createRfcommSocket", int.class);
                        tmp = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
                    } catch (Exception e) {
                        Log.e(TAG, "Error at HTC/createRfcommSocket: " + e);
                        e.printStackTrace();
                    }
                } else {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            //mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (EasyWeighService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {


        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    //bytes = mmInStream.read(buffer, 0, buffer.length);
                    // Send the obtained bytes to the UI Activity
					/*mHandler.obtainMessage(ScaleEasyWeighActivity.MESSAGE_READ, bytes, -1, buffer)
					.sendToTarget();*/
                    Message msg = Message.obtain(null, ScaleEasyWeighActivity.MESSAGE_READ, bytes, -1, buffer);
                    tumaMessage(msg);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                Message msg = null;
                if (readingProbed) {
                    msg = Message.obtain(null, ScaleEasyWeighActivity.READING_PROBE, -1, -1, buffer);
                } else {
                    msg = Message.obtain(null, ScaleEasyWeighActivity.MESSAGE_WRITE, -1, -1, buffer);
                }
                // Share the sent message back to the UI Activity

                tumaMessage(msg);
                readingProbed = false;
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


}

