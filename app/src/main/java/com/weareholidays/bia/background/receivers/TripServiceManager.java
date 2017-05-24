package com.weareholidays.bia.background.receivers;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.weareholidays.bia.background.services.LocationService;
import com.weareholidays.bia.background.services.ServiceUtils;
import com.weareholidays.bia.background.services.SocialSyncService;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.TripSettings;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.parse.ParseUser;

/**
 * Created by Teja on 06/06/15.
 */
public class TripServiceManager extends BroadcastReceiver {

    public static final String TAG = "TripServiceManager";
    public static final String TRIP_SERVICE_BROADCAST_INTENT = "com.weareholidays.bia.action.trip.service";
    public static final String TRIP_UPDATE_BROADCAST_INTENT = "com.weareholidays.bia.action.trip.updated";
    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Trip Service Manager Started");
        if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) || TRIP_SERVICE_BROADCAST_INTENT.equals(intent.getAction())) {

            Log.i(TAG, "Valid Intent Found");

            if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
                // reset flags
                ServiceUtils.setSyncServiceStatus(false);
                ServiceUtils.setUploadTripStatus(ServiceUtils.UPLOAD_TRIP_STATUS_INVALID);
            }

            if(ParseUser.getCurrentUser() != null){
                Log.i(TAG, "Logged in user found : " + ParseUser.getCurrentUser().getUsername());

                Trip trip = TripUtils.getInstance().getCurrentTripOperations().getTrip();

                alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

                if(trip != null && !trip.isFinished()){
                    Log.i(TAG, "Trip in progress: " + trip.getName());

                    TripSettings tripSettings = trip.getSettings();

                    if(tripSettings.isLocation()){
                        Log.i(TAG, "Location tracking is enabled. Starting the location track service");
                        Intent locationTrackIntent = new Intent(context, LocationServiceManager.class);
                        PendingIntent locationTrackPendingIntent = PendingIntent.getBroadcast(context, 0, locationTrackIntent, 0);

                        long trackInterval = LocationService.Constants.UPDATE_INTERVAL;

                        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,trackInterval,
                                trackInterval,locationTrackPendingIntent);
                    }

                    if(tripSettings.isSync() || tripSettings.isCameraRoll() || tripSettings.isInstagram() || tripSettings.isFacebook() || tripSettings.isTwitter()){
                        Intent socialSyncIntent = new Intent(context, SocialSyncServiceReceiver.class);
                        PendingIntent socialSyncPendingIntent = PendingIntent.getBroadcast(context, 0, socialSyncIntent, 0);

                        int syncInterval = SocialSyncService.SYNC_INTERVAL;

                        Log.i(TAG, "Setting up service to sync social media periodically using alarm manager. Sync interval : "
                                + syncInterval + " (milliseconds).");

                        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,syncInterval,
                                syncInterval,socialSyncPendingIntent);
                    }


                    ServiceUtils.scheduleDayTrackAlarm(context);

                    //TODO: service to track time zone
                }
                else{
                    if(trip == null){
                        Log.i(TAG, "No Trip found. Exiting the service manager.");
                    }
                    else{
                        Log.i(TAG, "Trip has been completed. No services are required to run. Trip: " + trip.getName());
                    }
                }
            }
            else{
                Log.i(TAG, "No Logged in user found. Exiting the service manager");
            }

        } else {
            Log.e(TAG, "Received unexpected intent " + intent.toString());
        }
    }

    private boolean isLocationTrackServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
