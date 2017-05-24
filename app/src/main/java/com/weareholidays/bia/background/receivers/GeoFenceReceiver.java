package com.weareholidays.bia.background.receivers;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.weareholidays.bia.background.services.GeofenceTransitionsIntentService;
import com.weareholidays.bia.models.SimpleGeofence;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.utils.Constants;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MapUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wah on 16/9/15.
 */
public class GeoFenceReceiver extends BroadcastReceiver implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    List<Geofence> mGeofenceList;
    private SimpleGeofence mAndroidGeofence;
    private Context mContext;
    public static String ACTION_SET_GEO_FENCE = "com.weareholidays.bia.action.setgeofence";
    private boolean accessFineLocationAllowedAfterRequest = false;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        if (!SharedPrefUtils.checkAndSetSharedPref(context, SharedPrefUtils.Keys.GEO_FENCE_PREF_BROADCAST))
            return;

        SharedPrefUtils.setStringPreference(mContext, SharedPrefUtils.Keys.GEO_FENCE_PREF_KEY, "");

        if (isLocationModeAvailable(context) || isLocationServciesAvailable(context)) {
            startGeoFence();
        } else {
            SharedPrefUtils.setBooleanPreference(mContext, SharedPrefUtils.Keys.GEO_FENCE_PREF_BROADCAST, false);
        }
    }

    public void startGeoFence() {
        new getPlace().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void configureAndConnectGeoFence(double latitude, double longitude) {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mGeofenceList = new ArrayList<>();
        createGeofences(latitude, longitude);
        mGoogleApiClient.connect();

    }

    /**
     * In this sample, the geofences are predetermined and are hard-coded here. A real app might
     * dynamically create geofences based on the user's location.
     */
    private void createGeofences(double latitude, double longitude) {
        // Create internal "flattened" objects containing the geofence data.
        /*if (SharedPrefUtils.getStringPreference(mContext, SharedPrefUtils.Keys.GEO_FENCE_PREF_KEY).equalsIgnoreCase(Constants.GEOFENCE_ID))
            return;*/

        DebugUtils.LogD("Created Geofence");
        mAndroidGeofence = new SimpleGeofence(
                Constants.GEOFENCE_ID,                // geofenceId.
                latitude,
                longitude,
                Constants.GEOFENCE_RADIUS_METERS,
                Constants.GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        mGeofenceList.add(mAndroidGeofence.toGeofence());
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnected(Bundle bundle) {
        SharedPrefUtils.setStringPreference(mContext, SharedPrefUtils.Keys.GEO_FENCE_PREF_KEY, Constants.GEOFENCE_ID);
        if(locationAllowed()) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    mGeofenceList,
                    getGeofencePendingIntent()
            );
        }
        else {
//            Toast.makeText(mContext, "Locations need to be enabled", Toast.LENGTH_LONG).show();
        }
    }

    private boolean locationAllowed() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void stopGoogleApiClient() {
        mGoogleApiClient.disconnect();
    }

    private class getPlace extends AsyncTask<Void, Void, String> {

        public getPlace() {
        }

        @Override
        protected String doInBackground(Void... urls) {

            String temp = "";
            ParseCustomUser user = ParseCustomUser.getCurrentUser();
            if (user != null && user.getPlace() != null && !TextUtils.isEmpty(user.getPlace())) {
                try {
                    temp = MapUtils.getGooglePlaceTextSearch(ParseCustomUser.getCurrentUser().getPlace());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return temp;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String result) {
            SharedPrefUtils.setBooleanPreference(mContext, SharedPrefUtils.Keys.GEO_FENCE_PREF_BROADCAST, false);

            if (!TextUtils.isEmpty(result)) {
                Location location = getPlaceCoordinates(result);
                if (location != null) {
                    configureAndConnectGeoFence(location.getLatitude(), location.getLongitude());
                }
            }
        }
    }

    private Location getPlaceCoordinates(String response) {
        Location location = null;
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("results")) {
                JSONArray jsonarray = jsonObject.getJSONArray("results");
                if (jsonarray != null && jsonarray.length() > 0) {

                    JSONObject jsonobject = jsonarray.getJSONObject(0);
                    if (jsonobject.has("geometry")) {
                        JSONObject tempLocation = new JSONObject(jsonobject.getJSONObject("geometry").optString("location"));
                        location = new Location("");
                        location.setLatitude(tempLocation.getDouble("lat"));
                        location.setLongitude(tempLocation.getDouble("lng"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
    }

    private boolean isLocationModeAvailable(Context context) {

        if (Build.VERSION.SDK_INT >= 19 && getLocationMode(context) != Settings.Secure.LOCATION_MODE_OFF) {
            return true;
        } else return false;
    }

    public boolean isLocationServciesAvailable(Context context) {
        if (Build.VERSION.SDK_INT < 19) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        } else return false;
    }

    public int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
