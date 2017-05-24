package com.weareholidays.bia.background.receivers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.weareholidays.bia.background.services.SocialSyncService;

/**
 * Created by Teja on 07/06/15.
 */
public class SocialSyncServiceReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = "SocialSyncReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Social sync request received");

        ComponentName comp = new ComponentName(context.getPackageName(), SocialSyncService.class.getName());

        ComponentName service = startWakefulService(context, new Intent().setComponent(comp));


        if(service == null){
            Log.e(TAG, "Could not start social sync service " + comp.toString());
        }
    }
}
