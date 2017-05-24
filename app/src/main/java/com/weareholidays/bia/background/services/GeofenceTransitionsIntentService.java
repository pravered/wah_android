package com.weareholidays.bia.background.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.trip.TripFragment;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by wah on 16/9/15.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    public GeofenceTransitionsIntentService() {
        super("GeoFenceReceiver");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        DebugUtils.LogD("GeoFence Intent Received");
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            DebugUtils.LogE("Location Services error: " + errorCode);
        } else {
            DebugUtils.LogE("Location Services broadcast received ");
            int transitionType = geoFenceEvent.getGeofenceTransition();
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                DebugUtils.LogD("Entered GeoFence");
                sendGeoFenceNotification(false);

            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                DebugUtils.LogD("Exited GeoFence");
                sendGeoFenceNotification(true);
            }
        }
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final Context context, final String resourceId) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resourceId, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void sendGeoFenceNotification(boolean isExitingCity) {

        String notificationText;

        if (!isExitingCity && (TripUtils.getInstance().getCurrentTripOperations() == null || !(TripUtils.getInstance().getCurrentTripOperations().isTripAvailable() && !TripUtils.getInstance().getCurrentTripOperations().getTrip().isFinished())))
            return;

        //Use case - when there is an ongoing trip and the traveller crosses homtown.
        if (isExitingCity && TripUtils.getInstance().getCurrentTripOperations() != null && TripUtils.getInstance().getCurrentTripOperations().isTripAvailable() && !TripUtils.getInstance().getCurrentTripOperations().getTrip().isFinished()) {
            return;
        }
        
        if (isExitingCity) {
            notificationText = getResources().getString(R.string.geofence_start_trip);
        } else {
            notificationText = getResources().getString(R.string.geofence_return_trip);
        }

        DebugUtils.LogD("isExitingCity : " + isExitingCity);

        Intent resultIntent = new Intent(this, HomeActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.JOURNAL_TAB);
        if (!isExitingCity) {
            resultIntent.putExtra(TripFragment.SHOW_FINISH_TRIP_LAYOUT, true);
        }
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        //building notification and setting style and other default parameters
        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(GeofenceTransitionsIntentService.this)
                        .setSmallIcon(getSmallIconId())
                        .setLargeIcon(getLargeIcon())
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setContentText(notificationText)
                        .setContentTitle("Bia")
                        .setTicker("Bia Notification received.")
                        .setContentIntent(resultPendingIntent)
                        .setDefaults(-1);

        if (notificationText != null && notificationText.length() > 38) {
            mBuilder.setStyle((new NotificationCompat.BigTextStyle()).bigText(notificationText));
        }

        addToNotificationTab(notificationText);
        NotificationManager notificationManager = (NotificationManager) GeofenceTransitionsIntentService.this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        android.app.Notification notification = mBuilder.build();
        notificationManager.notify(1111, notification);
    }

    private void addToNotificationTab(String content) {
        com.weareholidays.bia.parse.models.Notification notification = new com.weareholidays.bia.parse.models.Notification();
        notification.setContent(content);
        notification.setContentTime(Calendar.getInstance().getTime());
        notification.setIsRead(false);
        notification.setNotifier(ParseCustomUser.getCurrentUser());

        notification.setUser(ParseCustomUser.getCurrentUser().getUsername());
        notification.saveInBackground();
    }

    protected int getSmallIconId() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return R.drawable.logo;
        } else {
            return R.drawable.launch_icon;
        }
    }

    protected Bitmap getLargeIcon() {
        return BitmapFactory.decodeResource(GeofenceTransitionsIntentService.this.getResources(), R.drawable.launch_icon);
    }

}
