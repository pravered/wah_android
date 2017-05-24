package com.weareholidays.bia.background.receivers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.weareholidays.bia.background.services.OfflineGooglePlacesService;

/**
 * Created by challa on 15/7/15.
 */
public class OfflineGooglePlacesReceiver extends WakefulBroadcastReceiver {
    public static final String TAG = "OfflinePlacesReceiver";
    public static final String GET_OFFLINE_PLACES = "com.weareholidays.bia.action.getplaces";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "offline places intent received");
        Intent mIntent = new Intent();
        mIntent.putExtra(OfflineGooglePlacesService.END_LOCATION, intent.getExtras().getParcelable(OfflineGooglePlacesService.END_LOCATION));
        ComponentName comp = new ComponentName(context.getPackageName(), OfflineGooglePlacesService.class.getName());
        ComponentName service = startWakefulService(context,mIntent.setComponent(comp));

        if(service == null){
            Log.e(TAG, "Could not start offline places service " + comp.toString());
        }
    }
}
