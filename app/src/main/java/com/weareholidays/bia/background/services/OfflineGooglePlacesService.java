package com.weareholidays.bia.background.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.parse.ParseGeoPoint;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.background.receivers.OfflineGooglePlacesReceiver;
import com.weareholidays.bia.parse.models.CustomLocation;
import com.weareholidays.bia.parse.utils.OfflineUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.weareholidays.bia.activities.journal.actions.CheckInActivity.makeCall;
import static com.weareholidays.bia.activities.journal.actions.CheckInActivity.parseGoogleParse;

/**
 * Created by challa on 13/7/15.
 */
public class OfflineGooglePlacesService extends Service {
    private static final String TAG = "OFFLINE_PLACES";
    public static final String END_LOCATION = "END_LOCATION";

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public OfflineGooglePlacesService getServerInstance() {
            return OfflineGooglePlacesService.this;
        }
    }

    private double latitude;
    private double longitude;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent != null && intent.hasExtra(END_LOCATION) && intent.getParcelableExtra(END_LOCATION) != null)
            new googleplaces(intent).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_STICKY;
    }

    private class googleplaces extends AsyncTask<View, Void, String> {

        List<String> temps = new ArrayList<String>();
        private HashSet<CustomLocation> venuesList = new HashSet<>();
        Intent mIntent;
        public googleplaces(Intent intent){
            mIntent = intent;
            Location location = intent.getParcelableExtra(END_LOCATION);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        protected String doInBackground(View... urls) {
            // make Call to the url
            getPlaces("point_of_interest");
            getPlaces("restaurant");
            getPlaces("establishment");
            getPlaces(ViewUtils.REMAINING_PLACES_TYPES);

            if (temps.size() <= 0) {
                // we have an error to the call
                // we can also stop the progress bar
            } else {
                // all things went right
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                // parse Google places search result
                HashSet<CustomLocation> venuesListTemp = new HashSet<>();
                HashSet<String> names = new HashSet<>();
                for(int i=0; i<temps.size(); i++)
                    venuesListTemp.addAll(parseGoogleParse(temps.get(i), location));

                for(CustomLocation temp: venuesListTemp){
                    if(!names.contains(temp.getName())) {
                        names.add(temp.getName());
                        venuesList.add(temp);
                    }
                }


                if(venuesList.size() > 0){
                    try {
                        new OfflineUtils().deleteCustomLocation();
                    } catch (Exception e) {
                        DebugUtils.logException(e);
                    }

                    try {
                        new OfflineUtils().saveCustomLocationList(new ArrayList<CustomLocation>(venuesList));
                    } catch (Exception e) {
                        DebugUtils.logException(e);
                    }
                    try {
                        new OfflineUtils().setSavedLocation(new ParseGeoPoint(latitude, longitude));
                    } catch (Exception e) {
                        DebugUtils.logException(e);
                    }
                }
            }

            return "";
        }

        public void getPlaces(String types){
            String tempFirst = null;
            try {
                tempFirst = makeCall("https://maps.googleapis.com/maps/api/place/search/json?location="
                        + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                        + ","
                        + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
                        + "&rankby="
                        + URLEncoder.encode("distance", "UTF-8")
                        + "&sensor="
                        + URLEncoder.encode("true", "UTF-8")
                        + "&types="
                        + URLEncoder.encode(types, "UTF-8")
                        + "&key="
                        + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8"));
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            temps.add(tempFirst);
            String next_token = null;
            int count = 0;
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(tempFirst);
                next_token = jsonObject.getString("next_page_token");
            } catch (Exception e) {
                Log.e("Places","",e);
            }

            try {
                while(next_token != null && !"".equals(next_token)) {
                    Thread.sleep(1500);
                    String tempRecords = null;
                    try {
                        tempRecords = makeCall("https://maps.googleapis.com/maps/api/place/search/json?location="
                                + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                                + ","
                                + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
                                + "&rankby="
                                + URLEncoder.encode("distance", "UTF-8")
                                + "&sensor="
                                + URLEncoder.encode("true", "UTF-8")
                                + "&pagetoken="
                                + URLEncoder.encode(next_token, "UTF-8")
                                + "&types="
                                + URLEncoder.encode(types, "UTF-8")
                                + "&key="
                                + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    temps.add(tempRecords);
                    jsonObject = new JSONObject(tempRecords);
                    if(jsonObject.has("next_page_token")) {
                        next_token = jsonObject.getString("next_page_token");
                    }
                    else
                        next_token = null;
                }
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }

        @Override
        protected void onPreExecute() {
            // we can start a progress bar here
        }

        @Override
        protected void onPostExecute(String result) {
            stopSelf();
            OfflineGooglePlacesReceiver.completeWakefulIntent(mIntent);
        }
    }
}
