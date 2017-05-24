package com.weareholidays.bia.background.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.weareholidays.bia.background.services.LocationService;

/**
 * Created by Teja on 06/06/15.
 */
public class TripServiceStopManager extends BroadcastReceiver {

    public static final String TAG = "TripServiceManager";
    public static final String TRIP_SERVICE_STOP_BROADCAST_INTENT = "com.weareholidays.bia.action.trip.service.stop";
    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Trip stop service Manager Started");
        if(TRIP_SERVICE_STOP_BROADCAST_INTENT.equals(intent.getAction())){

            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            //Stop Location service
            ComponentName comp = new ComponentName(context.getPackageName(), LocationService.class.getName());
            context.stopService(new Intent().setComponent(comp));

            //Stop DayTrack alarms
            Intent dayTrackIntent = new Intent(context, DayTrackServiceReceiver.class);
            PendingIntent dayTrackPendingIntent = PendingIntent.getBroadcast(context, 0, dayTrackIntent, 0);
            alarmMgr.cancel(dayTrackPendingIntent);

            //Stop Social alarms
            Intent socialSyncIntent = new Intent(context, SocialSyncServiceReceiver.class);
            PendingIntent socialSyncPendingIntent = PendingIntent.getBroadcast(context, 0, socialSyncIntent, 0);
            alarmMgr.cancel(socialSyncPendingIntent);

            //Stop Location Track alarms
            Intent locationTrackIntent = new Intent(context, LocationServiceManager.class);
            PendingIntent locationTrackPendingIntent = PendingIntent.getBroadcast(context, 0, locationTrackIntent, 0);
            alarmMgr.cancel(locationTrackPendingIntent);
        }
    }
}
