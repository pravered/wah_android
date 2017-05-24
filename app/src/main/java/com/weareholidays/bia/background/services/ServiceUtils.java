package com.weareholidays.bia.background.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;

import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.background.receivers.DayTrackServiceReceiver;
import com.weareholidays.bia.background.receivers.UploadTripReceiver;

import java.util.Calendar;

/**
 * Created by Teja on 20/07/15.
 */
public class ServiceUtils {

    private static final String SYNC_SERVICE_PREFS = "SYNC_SERVICE_PREFS";
    private static final String SYNC_SERVICE_STATUS = "SYNC_SERVICE_STATUS";
    private static final String CAMERA_SYNCH_SERVICE = "CAMERA_SYNCH_SERVICE";

    private static final String UPLOAD_TRIP_PREFS = "UPLOAD_TRIP_PREFS";
    private static final String UPLOAD_TRIP_STATUS = "UPLOAD_TRIP_STATUS";
    private static final String UPLOAD_TRIP_START_AFTER_SYNC = "UPLOAD_TRIP_START_AFTER_SYNC";

    public static final int UPLOAD_TRIP_STATUS_INVALID = -1;
    public static final int UPLOAD_TRIP_RUNNING = 1;
    public static final int UPLOAD_TRIP_FAILED = 2;
    public static final int UPLOAD_TRIP_COMPLETED = 3;

    public static final String UPLOAD_TRIP_SHOW_STATUS = "UPLOAD_TRIP_SHOW_STATUS";

    public static void scheduleDayTrackAlarm(Context context){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent dayTrackIntent = new Intent(context, DayTrackServiceReceiver.class);
        PendingIntent dayTrackPendingIntent = PendingIntent.getBroadcast(context, 0, dayTrackIntent, 0);
        Calendar midnightCalendar = Calendar.getInstance();
        midnightCalendar.setTimeInMillis(System.currentTimeMillis());
        midnightCalendar.add(Calendar.DATE, 1);
        midnightCalendar.set(Calendar.SECOND, 0);
        midnightCalendar.set(Calendar.MINUTE, 0);
        midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, midnightCalendar.getTimeInMillis(), dayTrackPendingIntent);
        }
        else{
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, midnightCalendar.getTimeInMillis(), dayTrackPendingIntent);
        }
    }

    public static boolean isSyncServiceRunning(){
        return WAHApplication.getWAHContext().getSharedPreferences(SYNC_SERVICE_PREFS,Context.MODE_PRIVATE).getBoolean(SYNC_SERVICE_STATUS,false);
    }

    public synchronized static boolean checkAndSetCameraSyncService(){
        boolean status = WAHApplication.getWAHContext().getSharedPreferences(SYNC_SERVICE_PREFS,Context.MODE_PRIVATE).getBoolean(CAMERA_SYNCH_SERVICE,false);
        if(!status){
            WAHApplication.getWAHContext().getSharedPreferences(SYNC_SERVICE_PREFS,Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(CAMERA_SYNCH_SERVICE,true).commit();
            return true;
        }
        return false;
    }

    public static void setCameraSyncService(boolean status){
        WAHApplication.getWAHContext().getSharedPreferences(SYNC_SERVICE_PREFS,Context.MODE_PRIVATE)
                .edit()
                .putBoolean(CAMERA_SYNCH_SERVICE,status).commit();
    }

    public static void setSyncServiceStatus(boolean syncServiceStatus){
        WAHApplication.getWAHContext().getSharedPreferences(SYNC_SERVICE_PREFS,Context.MODE_PRIVATE)
                .edit()
                .putBoolean(SYNC_SERVICE_STATUS,syncServiceStatus).commit();
    }

    public static int getUploadTripStatus(){
        return WAHApplication.getWAHContext().getSharedPreferences(UPLOAD_TRIP_PREFS,Context.MODE_PRIVATE)
                .getInt(UPLOAD_TRIP_STATUS,UPLOAD_TRIP_STATUS_INVALID);
    }

    public static void setUploadTripStatus(int status){
        WAHApplication.getWAHContext().getSharedPreferences(UPLOAD_TRIP_PREFS,Context.MODE_PRIVATE)
                .edit()
                .putInt(UPLOAD_TRIP_STATUS,status).commit();
    }

    public static boolean showUploadTripStatus(){
        return WAHApplication.getWAHContext().getSharedPreferences(UPLOAD_TRIP_PREFS,Context.MODE_PRIVATE)
                .getBoolean(UPLOAD_TRIP_SHOW_STATUS,false);
    }

    public static void setShowUploadStatus(boolean status){
        WAHApplication.getWAHContext().getSharedPreferences(UPLOAD_TRIP_PREFS,Context.MODE_PRIVATE)
                .edit().putBoolean(UPLOAD_TRIP_SHOW_STATUS,status).commit();
    }

    public static void setStartUploadAfterSync(boolean status){
        WAHApplication.getWAHContext().getSharedPreferences(UPLOAD_TRIP_PREFS,Context.MODE_PRIVATE)
                .edit().putBoolean(UPLOAD_TRIP_START_AFTER_SYNC,status).commit();
    }

    private static boolean startUploadAfterSync(){
        return WAHApplication.getWAHContext().getSharedPreferences(UPLOAD_TRIP_PREFS,Context.MODE_PRIVATE)
                .getBoolean(UPLOAD_TRIP_START_AFTER_SYNC,false);
    }

    public static void checkAndStartUploadTrip(){
        if(startUploadAfterSync()){
            setStartUploadAfterSync(false);
            Intent i = new Intent(UploadTripReceiver.UPLOAD_FULL_TRIP_INTENT);
            i.putExtra("receiver", new ResultReceiver(new Handler()));
            WAHApplication.getWAHContext().sendBroadcast(i);
        }
    }
}
