package com.weareholidays.bia.asyncTasks;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.journal.AsyncResponse;
import com.weareholidays.bia.activities.journal.ReturnLocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.weareholidays.bia.activities.journal.actions.CheckInActivity.makeCall;

/**
 * Created by challa on 19/6/15.
 */
public class CheckInAsync extends AsyncTask<String, Void, String> implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 1000;
    //ProgressDialog progDailog = null;
    Activity a;
    private Location mLastLocation;

    private Boolean servicesAvailable = false;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean isStopped;
    private double latitude;
    private double longitude;
    private String reference;
    private ReturnLocation returnLocation;
    private String addressString = "";
    public static AsyncResponse delegate=null;
    private String photoReference;

    public CheckInAsync(Activity a){
        this.a = a;
        setup();
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setup(){

        if (servicesConnected()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }
        mGoogleApiClient.connect();

    }

    private void displayLocation(Location location) {

        mLastLocation = location;

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        } else {
        }
    }

    @Override
    protected void onPreExecute() {
//        progDailog = new ProgressDialog(a);
//        progDailog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                CheckInAsync.this.cancel(true);
//                dialog.dismiss();
//                a.finish();
//            }
//        });
//        progDailog.setButton(DialogInterface.BUTTON_POSITIVE, "Skip", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                isStopped = true;
//                addressString = "";
//            }
//        });
//
//        progDailog.setMessage("Getting Location...");
//        progDailog.setIndeterminate(true);
//        progDailog.setCancelable(false);
//        progDailog.setCanceledOnTouchOutside(false);
//        progDailog.show();

    }

    @Override
    protected void onCancelled() {
        System.out.println("Cancelled by user!");
        //progDailog.dismiss();
        isStopped = true;
    }

    @Override
    protected void onPostExecute(String result) {

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub
//        this.latitude = 17.4636416;
//        this.longitude = 78.3445176;
        while (this.latitude == 0.0 && !isStopped) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        if (this.latitude != 0.0){
            if(isNetworkAvailable())
                new googleplaces().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return null;
    }

    private class googleplaces extends AsyncTask<View, Void, String> {

        String temp;
        @Override
        protected String doInBackground(View... urls) {
            // make Call to the url
            try {
                temp = makeCall("https://maps.googleapis.com/maps/api/place/search/json?location="
                        + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                        + ","
                        + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
                        + "&radius="
                        + URLEncoder.encode("1", "UTF-8")
                        + "&sensor="
                        + URLEncoder.encode("true", "UTF-8")
                        + "&key="
                        + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //print the call in the console
            return "";
        }

        @Override
        protected void onPreExecute() {
            // we can start a progress bar here
        }

        @Override
        protected void onPostExecute(String result) {
            if (temp == null) {
                // we have an error to the call
                // we can also stop the progress bar
            } else {
                parseGoogleParse(temp);
                //progDailog.dismiss();
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                removeUpdates();
                returnLocation = new ReturnLocation();
                returnLocation.setLocation(location);
                returnLocation.setNetworkPresent(isNetworkAvailable());
                returnLocation.setAddressString(addressString);
                returnLocation.setReference(reference);
                returnLocation.setPhotoReference(photoReference);
                returnLocation.setNetworkPresent(isNetworkAvailable());
                delegate.processFinish(returnLocation);
            }
        }
    }




    private void parseGoogleParse(final String response) {
        try {

            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // make an jsonObject in order to parse the response
            if (jsonObject.has("results")) {
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                    if (jsonArray.getJSONObject(0).has("name")) {
                        addressString = jsonArray.getJSONObject(0).optString("name");
                        if(jsonArray.getJSONObject(0).has("place_id")){
                            reference = jsonArray.getJSONObject(0).getString("place_id");
                        }
                        if (jsonArray.getJSONObject(0).has("photos")) {
                            JSONObject photo = new JSONObject(jsonArray.getJSONObject(0).getJSONArray("photos").get(0).toString());
                            photoReference = photo.getString("photo_reference");
                        }
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(a)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mLocationRequest.setInterval(UPDATE_INTERVAL);

        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        servicesAvailable = servicesConnected();
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(mGoogleApiClient != null){
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if(mLastLocation != null)
                startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        removeUpdates();
        // Displaying the new location on UI
        displayLocation(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        removeUpdates();
    }

    public void removeUpdates(){
        if(servicesAvailable && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            // Destroy the current location client
            mGoogleApiClient = null;
        }
    }
}
