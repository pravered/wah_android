package com.weareholidays.bia.background.receivers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.weareholidays.bia.background.services.DayTrackService;

/**
 * Created by Teja on 07/06/15.
 */
public class DayTrackServiceReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = "DayTrackServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Day track intent received");

        ComponentName comp = new ComponentName(context.getPackageName(), DayTrackService.class.getName());
        ComponentName service = startWakefulService(context,new Intent().setComponent(comp));

        if(service == null){
            Log.e(TAG, "Could not start day track service " + comp.toString());
        }
    }
}
