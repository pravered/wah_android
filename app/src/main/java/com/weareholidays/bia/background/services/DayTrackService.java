package com.weareholidays.bia.background.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.weareholidays.bia.background.receivers.DayTrackServiceReceiver;
import com.weareholidays.bia.background.receivers.TripServiceManager;
import com.weareholidays.bia.background.receivers.TripServiceStopManager;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * Created by Teja on 06/06/15.
 */
public class DayTrackService extends Service {

    private static final String TAG = "DAY_TRACK_SERVICE";

    IBinder mBinder = new LocalBinder();

    Intent mIntent;

    public class LocalBinder extends Binder {
        public DayTrackService getServerInstance() {
            return DayTrackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mIntent = intent;
        Log.i(TAG,"Day track Service called");
        new DayTrackTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_NOT_STICKY;
    }

    private class DayTrackTask extends AsyncTask<Void,Void,Void>{

        private boolean stopTrackService;

        @Override
        protected Void doInBackground(Void... params) {
            if(ParseUser.getCurrentUser() == null){
                Log.e(TAG,"NO Logged in user found");
                stopTrackService = true;
                return null;
            }

            Trip trip = TripUtils.getInstance().getCurrentTripOperations().getTrip();

            if(trip == null){
                Log.e(TAG,"No Current trip found");
                stopTrackService = true;
                return null;
            }

            if(trip.isFinished()){
                Log.e(TAG,"Trip has been completed. Trip: " + trip.getName());
                stopTrackService = true;
                return null;
            }

            try {
                Log.i(TAG,"Ending Day");
                ((TripLocalOperations)TripUtils.getInstance().getCurrentTripOperations()).endDay();
            } catch (ParseException e) {
                Log.e(TAG,"Unable to end day for trip",e);
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result){
            if(stopTrackService){
                Intent intent = new Intent(TripServiceStopManager.TRIP_SERVICE_STOP_BROADCAST_INTENT);
                sendBroadcast(intent);
                Log.i(TAG, "Sending Intent to stop trip services");
            }
            stopSelf();
            Log.i(TAG, "Stopping day track service");
            DayTrackServiceReceiver.completeWakefulIntent(mIntent);
            Intent updateIntent = new Intent(TripServiceManager.TRIP_UPDATE_BROADCAST_INTENT);
            sendBroadcast(updateIntent);
            Log.i(TAG, "Sending trip update broadcast");
            if(!stopTrackService){
                ServiceUtils.scheduleDayTrackAlarm(getBaseContext());
            }
            DayTrackServiceReceiver.completeWakefulIntent(mIntent);
        }
    }
}
