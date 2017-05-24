package com.weareholidays.bia.background.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.weareholidays.bia.background.services.LocationService;

/**
 * Created by Teja on 05-06-2015.
 */
public class LocationServiceManager extends BroadcastReceiver {

    private SharedPreferences mPrefs;
    public static final String TAG = "LocationServiceManager";
//    public static final String LOCATION_TRACK_BROADCAST_INTENT = "com.beehyv.wah.action.location.track";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Location Service Manager Started");
        ComponentName comp = new ComponentName(context.getPackageName(), LocationService.class.getName());
        ComponentName service = context.startService(new Intent().setComponent(comp));

        if (null == service){
            // something really wrong here
            Log.e(TAG, "Could not start service " + comp.toString());
        }
    }
}
