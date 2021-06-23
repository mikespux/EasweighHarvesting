package com.plantation.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.plantation.activities.ScaleSerialWeighActivity;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Michael on 20/03/2017.
 */
public class SerialService extends Service {

    private static final String TAG = "Serial Service";
    public int counter = 0;
    private boolean isRunning = false;
    private Timer timer;
    private TimerTask timerTask;


    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

        isRunning = true;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("com.plantation.services.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                String data = "P";
                if (data != null) {

                    if (ScaleSerialWeighActivity.isPrefSendCR == true) {
                        try {
                            ScaleSerialWeighActivity.mSerialManager.startDataTransfer(data);

                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    } else if (ScaleSerialWeighActivity.isPrefSendCR == false) {

                        try {
                            ScaleSerialWeighActivity.mSerialManager.startDataTransfer(data);

                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

									/*InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

									inputManager.hideSoftInputFromWindow(getCurrentFocus()
													.getWindowToken(),
											InputMethodManager.HIDE_NOT_ALWAYS);*/

                    if (ScaleSerialWeighActivity.isPrefClearTextAfterSending == true) {
                        //mInputBox.setText("");
                    } else {
                        // do not clear the text from the editText
                    }
                }
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
