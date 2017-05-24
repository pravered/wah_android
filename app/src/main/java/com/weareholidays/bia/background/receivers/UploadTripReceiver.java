package com.weareholidays.bia.background.receivers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.weareholidays.bia.background.services.UploadTripService;

public class UploadTripReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = "UploadTripReceiver";

    public static final String UPLOAD_FULL_TRIP_INTENT = "com.weareholidays.bia.action.trip.finish";

    public static final String SYNC_TRIP_INTENT = "com.weareholidays.bia.action.trip.sync";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG,"Received intent to finish trip");

        ComponentName comp = new ComponentName(context.getPackageName(), UploadTripService.class.getName());
        Intent intent1 = new Intent();
        intent1.setComponent(comp);
        intent1.putExtra("receiver",intent.getParcelableExtra("receiver"));

        if(SYNC_TRIP_INTENT.equals(intent.getAction())){
            intent1.putExtra(UploadTripService.UPLOAD_TYPE_KEY,UploadTripService.UPLOAD_SYNC);
        }

        ComponentName service = startWakefulService(context,intent1);

        if(service == null){
            Log.e(TAG, "Could not start finish trip service " + comp.toString());
        }
    }
}
