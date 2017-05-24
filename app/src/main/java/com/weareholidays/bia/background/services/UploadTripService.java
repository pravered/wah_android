package com.weareholidays.bia.background.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.background.receivers.UploadTripReceiver;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.TripPeople;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripAsyncCallback;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.crittercism.app.Crittercism;
import com.parse.ParseObject;
import com.weareholidays.bia.utils.DebugUtils;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by Teja on 23/06/15.
 */
public class UploadTripService extends IntentService {


    private static final String TAG = "UploadTripService";
    ResultReceiver resultReceiver;
    public static String UPLOAD_TYPE_KEY = "UPLOAD_TYPE_KEY";
    public static String UPLOAD_SYNC = "UPLOAD_SYNC";
    public static int UPLOAD_COMPLETED = 200;
    public static int UPLOAD_FAILED = 500;
    public static int UPLOAD_PROGRESS = 100;
    public static String UPLOAD_PROGRESS_KEY = "UPLOAD_PROGRESS_KEY";

    private boolean isSync = false;

    public UploadTripService() {
        super("FinishTrip");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadTripService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = 8273;
        boolean wifiConnected = false;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        wifiConnected = mWifi.isConnected();
        resultReceiver = intent.getParcelableExtra("receiver");
        String uploadSync = intent.getStringExtra(UPLOAD_TYPE_KEY);
        if (UPLOAD_SYNC.equals(uploadSync)) {
            isSync = true;
        }
        if (!isSync) {
            ServiceUtils.setUploadTripStatus(ServiceUtils.UPLOAD_TRIP_RUNNING);
        }
        final TripLocalOperations tripLocalOperations = (TripLocalOperations) TripUtils.getInstance().getCurrentTripOperations();
        final Trip trip = tripLocalOperations.getTrip();

        NotificationManagerCompat mNotifyManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        int smallIconDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            smallIconDrawable = R.drawable.logo;
        } else {
            smallIconDrawable = R.drawable.launch_icon;
        }
        try {
            mBuilder.setContentTitle(trip.getName())
                    .setSmallIcon(smallIconDrawable)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.launch_icon))
                    .setContentText("Sync in progress");
//        mBuilder.setProgress(100, 0, false);
//        if(!isSync)
//            mNotifyManager.notify(id, mBuilder.build());

            mBuilder.setProgress(100, 0, false);
            if (!isSync)
                mNotifyManager.notify(id, mBuilder.build());

            int dayProgress = 30;
            int timeLineProgress = 30;
            int routePointProgress = 40;
            List<Day> days = trip.getDays();
            int totalDaysSize = days.size();
            int completedDays = 0;
//            int limitDays = 1;
//            List<Day> limitedDays = new ArrayList<>();
//            for(Day day: days){
//                if(limitedDays.size() == limitDays){
//                    Log.i(TAG,"Saving days: " + limitedDays.size());
//                    ParseObject.saveAll(limitedDays);
//                    limitedDays.clear();
//                    updateProgress(((dayProgress * completedDays)/totalDaysSize),mBuilder);
//                    mNotifyManager.notify(id, mBuilder.build());
//                }
//                day.getDaySummary().save();
//                limitedDays.add(day);
//                completedDays++;
//            }
//
//            if(limitedDays.size() > 0){
//                Log.i(TAG,"Saving days: " + limitedDays.size());
//                ParseObject.saveAll(limitedDays);
//            }

            Log.i(TAG, "Saving Trip");
            trip.save();

            Log.i(TAG, "Saved Trip");

            if (!isSync || wifiConnected)
                ParseFileUtils.uploadParseFile(trip);

            completedDays = 0;

            int routePointLimit = 100;
            for (Day day : trip.getDays()) {
                Log.i(TAG, "Saving Route points for day: " + day.getDisplayOrder());
                int skip = 0;
                boolean skipWhile = false;
                while (!skipWhile) {
                    skipWhile = true;
                    List<RoutePoint> routePoints = tripLocalOperations.getDayRoutePoints(day, skip, routePointLimit);
                    skip += routePointLimit;
                    if (routePoints.size() > 0)
                        ParseObject.saveAll(routePoints);
                    if (routePoints.size() == routePointLimit) {
                        skipWhile = false;
                    }
                }
                completedDays++;
                updateProgress(dayProgress + ((routePointProgress * completedDays) / totalDaysSize), mBuilder);
            }

            Log.i(TAG, "Completed saving routepoints");
            completedDays = 0;

            for (Day day : trip.getDays()) {
                Log.i(TAG, "Saving timelines for day: " + day.getName());
                int timelineLimit = 1;
                int skip = 0;
                boolean skipWhile = false;
                int compProgress = dayProgress + routePointProgress + ((timeLineProgress * completedDays) / totalDaysSize);
                while (!skipWhile) {
                    skipWhile = true;
                    List<Timeline> timeLines = tripLocalOperations.getDayTimeLines(day, timelineLimit, skip, null, null);
                    if (timeLines.size() > 0)
                        ParseObject.saveAll(timeLines);
                    Log.i(TAG, "Saved " + (skip + timelineLimit) + " timelines for day: " + day.getName());
                    if (!isSync)
                        mNotifyManager.notify(id, mBuilder.build());
                    int completedTimelines = 0;
                    for (Timeline timeline : timeLines) {
                        if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
                            List<Media> media = tripLocalOperations.getAlbumMedia((Album) timeline.getContent());
                            if (media.size() > 0)
                                ParseObject.saveAll(media);
                            if (Source.WAH.equals(timeline.getSource())) {
                                ;
                                for (Media md : media) {
                                    if (!isSync || wifiConnected)
                                        ParseFileUtils.uploadParseFile(md);
                                }
                            }
                        } else if (Timeline.CHECK_IN_CONTENT.equals(timeline.getContentType())) {
                            if (!isSync || wifiConnected)
                                ParseFileUtils.uploadParseFile(timeline.getContent());
                        }
                        completedTimelines++;
                        updateProgress(compProgress + (timeLineProgress * (skip + completedTimelines)) / ((skip + timelineLimit) * totalDaysSize), mBuilder);
                    }
                    skip += timelineLimit;
                    if (timeLines.size() == timelineLimit) {
                        skipWhile = false;
                    }
                }
                completedDays++;
                updateProgress(dayProgress + routePointProgress + ((timeLineProgress * completedDays) / totalDaysSize), mBuilder);
                if (!isSync)
                    mNotifyManager.notify(id, mBuilder.build());
            }

            Log.i(TAG, "Completed saving timelines");

            //Save Trip People

            List<TripPeople> tripPeopleList = tripLocalOperations.getTripPeopleList();
            if (tripPeopleList != null && tripPeopleList.size() > 0) {
                Log.i(TAG, "Saving trip people");
                ParseObject.saveAll(tripPeopleList);
                for (TripPeople tripPeople : tripPeopleList) {
                    if (TripPeople.PHONE_BOOK_TYPE.equals(tripPeople.getType())) {
                        if (!isSync)
                            ParseFileUtils.uploadParseFile(tripPeople);
                    }
                }
                Log.i(TAG, "completed saving trip people");
            }

            if (!isSync) {

                tripLocalOperations.getTripFeatureImage(new TripAsyncCallback<String>() {
                    @Override
                    public void onCallBack(String result) {
                        if (result != null && !TextUtils.isEmpty(result)) {
                            Uri uri = Uri.parse(result);
                            byte[] data = ParseFileUtils.convertImageToBytes(uri, UploadTripService.this);

                            ParseFile parseFile = new ParseFile(data, "trip_feature_image.jpeg");
                            try {
                                parseFile.save();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            trip.setFeatureImage(parseFile);
                        }
                    }
                });

                trip.setUploaded(true);
                trip.save();

                try {
                    for (TripPeople tripPeople : tripPeopleList) {
                        if (TripPeople.PHONE_BOOK_TYPE.equals(tripPeople.getType())
                                && !ShareUtils.isValidPhone(tripPeople.getIdentifier()))
                            continue;
                        PeopleContact.Type type = PeopleContact.Type.PHONE;
                        if (TripPeople.FACEBOOK_TYPE.equals(tripPeople.getType()))
                            type = PeopleContact.Type.FB;
                        List<ParseCustomUser> selectedUsers = ShareUtils.getParseShareUser(tripPeople.getIdentifier(), type).setLimit(1).find();
                        if (selectedUsers != null && selectedUsers.size() == 1) {
                            ShareUtils.sendTripPeopleNotification(selectedUsers.get(0), trip);
                        } else {
                            //TODO: send email notification
                        }
                    }
                } catch (Exception e) {
                    DebugUtils.logException(e);
                }

                ParseCustomUser user = ParseCustomUser.getCurrentUser();
                user.addTrips();
                user.saveEventually();

                mBuilder.setProgress(0, 0, false).setContentText("Sync complete");
                if (!isSync)
                    mNotifyManager.notify(id, mBuilder.build());
                TripUtils.getInstance().loadServerFullTrip(trip.getObjectId());
                Bundle bundle = new Bundle();
                bundle.putString(TripOperations.TRIP_KEY_ARG, trip.getObjectId());
                //tripLocalOperations.clearAll();
                ServiceUtils.setUploadTripStatus(ServiceUtils.UPLOAD_TRIP_COMPLETED);
                if (resultReceiver != null)
                    resultReceiver.send(UPLOAD_COMPLETED, bundle);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error uploading trip", e);
            DebugUtils.logException(e);
            if (!isSync) {
                ServiceUtils.setUploadTripStatus(ServiceUtils.UPLOAD_TRIP_FAILED);
            }
            mBuilder.setContentText("Error Syncing trip")
                    .setProgress(0, 0, false);
            if (!isSync)
                mNotifyManager.notify(id, mBuilder.build());
            if (resultReceiver != null)
                resultReceiver.send(UPLOAD_FAILED, null);
        } finally {
            if (isSync) {
                ServiceUtils.setSyncServiceStatus(false);
                ServiceUtils.checkAndStartUploadTrip();
            }
            stopSelf();
            UploadTripReceiver.completeWakefulIntent(intent);
        }
    }

    private void updateProgress(int progress, NotificationCompat.Builder mBuilder) {
        mBuilder.setProgress(100, progress, false);
        Bundle bundle = new Bundle();
        bundle.putInt(UPLOAD_PROGRESS_KEY, progress);
        if (resultReceiver != null)
            resultReceiver.send(UPLOAD_PROGRESS, bundle);
    }

}
