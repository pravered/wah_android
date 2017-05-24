package com.weareholidays.bia.background.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.weareholidays.bia.background.receivers.OfflineGooglePlacesReceiver;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.utils.OfflineUtils;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Teja on 05-06-2015.
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    IBinder mBinder = new LocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private Boolean servicesAvailable = false;

    public class LocalBinder extends Binder {
        public LocationService getServerInstance() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        // Set the smallest displacement
        mLocationRequest.setSmallestDisplacement(Constants.MINIMUM_DISTANCE_IN_METRES);

        servicesAvailable = servicesConnected();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;
        } else {

            return false;
        }
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if(!servicesAvailable || (mGoogleApiClient != null && mGoogleApiClient.isConnected()) || mInProgress)
            return START_STICKY;

        setUpLocationClientIfNeeded();
        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress)
        {
            appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Started", Constants.LOG_FILE, this);
            mInProgress = true;
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    /*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    private void setUpLocationClientIfNeeded()
    {
        if(mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mDateFormat.format(new Date());
    }

    public static void appendLog(String text, String filename, Context context)
    {
        try
        {
            String logDirectory = "logs";
            File myRepo = context.getExternalFilesDir(logDirectory);
            File logFile = new File(myRepo.getPath() +  File.separatorChar + filename);
            if (!logFile.exists())
            {
                logFile.createNewFile();
            }
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (Exception e)
        {
            Log.e("LocationService","exception writing logs",e);
        }
    }

    @Override
    public void onDestroy(){
        // Turn off the request flag
        mInProgress = false;
        if(servicesAvailable && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            // Destroy the current location client
            mGoogleApiClient = null;
        }
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Stopped", Constants.LOG_FILE, this);
        super.onDestroy();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

        // Request location updates using static settings
        if(mGoogleApiClient != null){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
                appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Connected", Constants.LOG_FILE, this);
            }
            else {
                //location was not permitted
            }
        }
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            // If no resolution is available, display an error dialog
        } else {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = DateFormat.getDateTimeInstance().format(new Date()) + " Location: " + Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        new SaveRoutePointTask(location).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        appendLog(msg, Constants.LOCATION_FILE, this);
    }

    private class SaveRoutePointTask extends AsyncTask<Void,Void,Void>{

        private Location location;

        public SaveRoutePointTask(Location location) {
            this.location = location;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                RoutePoint routePoint = null;
                ParseGeoPoint startPoint = null;
                Day day;
                ParseGeoPoint endPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                startPoint = new OfflineUtils().getSavedLocation();
                if(startPoint == null || startPoint.distanceInKilometersTo(endPoint) > 50){
                    Intent mIntent = new Intent(OfflineGooglePlacesReceiver.GET_OFFLINE_PLACES);
                    mIntent.putExtra(OfflineGooglePlacesService.END_LOCATION, location);
                    sendBroadcast(mIntent);
                    new OfflineUtils().setSavedLocation(endPoint);
                }

                day = ((TripLocalOperations)TripUtils.getInstance().getCurrentTripOperations()).addRoutePointFromLocationTracker(location);
                if(day != null){
                    setCityCountry(day, location);
                }
            } catch (Exception e) {
                Log.e("LocationTracker","Exception while storing route point",e);
            }
            return null;
        }
    }

    public void setCityCountry(Day day, Location location) throws IOException {
        // check if already set
        if(day.getCity() != null && day.getCountry() != null && day.getLocation() != null) {
            return;
        }
        if(day.getLocation() == null) {
            day.setLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(day.getLocation().getLatitude(), day.getLocation().getLongitude(), 1);
        String city = addresses.get(0).getLocality();
        String country = addresses.get(0).getCountryName();
        addresses = geocoder.getFromLocationName(city +","+ country, 1);
        if(addresses.size() > 0) {
            double latitude= addresses.get(0).getLatitude();
            double longitude= addresses.get(0).getLongitude();
            city = addresses.get(0).getLocality();
            country = addresses.get(0).getCountryName();
            ParseGeoPoint myGeoPoint = new ParseGeoPoint(latitude, longitude);
            day.setLocation(myGeoPoint);
            day.setCity(city);
            day.setCountry(country);
        }
        try {
            ((TripLocalOperations)TripUtils.getInstance().getCurrentTripOperations()).save(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public final class Constants {

        // Milliseconds per second
        private static final int MILLISECONDS_PER_SECOND = 1000;
        // Update frequency in seconds
        private static final int UPDATE_INTERVAL_IN_SECONDS = 60 * 30;//30 minutes
        // Update frequency in milliseconds
        public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
        // The fastest update frequency, in seconds
        private static final int FASTEST_INTERVAL_IN_SECONDS = 60;
        // A fast frequency ceiling in milliseconds
        public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
        // Minimum distance before we want a location update
        public static final long MINIMUM_DISTANCE_IN_METRES = 250; // 250m
        // Stores the lat / long pairs in a text file
        public static final String LOCATION_FILE = "location.txt";
        // Stores the connect / disconnect data in a text file
        public static final String LOG_FILE = "log.txt";


        /**
         * Suppress default constructor for noninstantiability
         */
        private Constants() {
            throw new AssertionError();
        }
    }
}
