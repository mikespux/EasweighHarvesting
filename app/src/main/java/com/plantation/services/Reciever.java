package com.plantation.services;

/**
 * Created by Michael on 20/03/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Reciever extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Reciever.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, SerialService.class));
    }
}

