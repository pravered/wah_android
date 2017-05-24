package com.weareholidays.bia.activities.journal.trip;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
//import com.parse.ParseInstagramUtils;
//import com.parse.ParseTwitterUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parseInsta.InstagramApp;
import com.parseInsta.InstagramConstants;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.background.receivers.TripServiceManager;
import com.weareholidays.bia.background.receivers.TripServiceStopManager;
import com.weareholidays.bia.background.services.LocationService;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.TripSettings;
import com.weareholidays.bia.parse.models.local.TripLocal;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.social.facebook.models.FacebookPermission;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.List;

import com.parseInsta.ParseInstagramUtil;

//import twitter4j.auth.AccessToken;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_PERMISSION_PREF;

public class TripSettingsActivity extends AppCompatActivity implements
        ResultCallback<LocationSettingsResult>, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    protected static final int REQUEST_CHECK_SETTINGS = 50;
    private static final String TAG = "TRIP_SETTINGS";

    private TripLocal tripLocal;
    private CenterProgressDialog progressDialog;
    private CheckBox locationCheckBox;
    private CheckBox checkInCheckBox;
    private CheckBox cameraRollCheckBox;
    private CheckBox facebookCheckBox;
    private CheckBox twitterCheckBox;
    private CheckBox instagramCheckBox;
    private CheckBox syncCheckBox;
    private ParseUser currentUser;
    private List<FacebookPermission> facebookPermissions;
    private TripOperations tripOperations;

    private ShowcaseView showcaseView;
    private String couponId;

    Toolbar toolbar;

    private static int storageRequestCode = 118;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_settings);
        if (getIntent() != null) {
            if (getIntent().hasExtra(TripOperations.TRIP_KEY_ARG)) {
                tripOperations = TripUtils.getInstance().getTripOperations(getIntent().getStringExtra(TripOperations.TRIP_KEY_ARG));
                tripLocal = new TripLocal();
                TripSettings tripSettings = tripOperations.getTrip().getSettings();
                tripLocal.setAccessCameraRoll(tripSettings.isCameraRoll());
                tripLocal.setAccessCheckIn(tripSettings.isCheckIn());
                tripLocal.setAccessFacebook(tripSettings.isFacebook());
                tripLocal.setAccessInstagram(tripSettings.isInstagram());
                tripLocal.setAccessLocation(tripSettings.isLocation());
                tripLocal.setAccessTwitter(tripSettings.isTwitter());
                tripLocal.setAccessSync(tripSettings.isSync());
            } else
                tripLocal = (TripLocal) getIntent().getSerializableExtra(TripStartActivity.TRIP_CREATED);
        }
        if (tripLocal == null) {
            finish();
            return;
        }

        couponId = getIntent().getStringExtra(TripStartActivity.COUPON_ID);

        initialize();

        drawCoachMarks();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkStoragePermissions();
        }
    }

    private void checkStoragePermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, storageRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == storageRequestCode) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Read External Storage allowed", Toast.LENGTH_SHORT).show();
            }
            else {
//                Toast.makeText(this, "Read External Storage denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void initialize() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.permissions);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentUser = ParseUser.getCurrentUser();
        currentUser.saveInBackground();

        locationCheckBox = (CheckBox) findViewById(R.id.checkbox_location);
        checkInCheckBox = (CheckBox) findViewById(R.id.checkbox_checkin);
        cameraRollCheckBox = (CheckBox) findViewById(R.id.checkbox_camera);
        facebookCheckBox = (CheckBox) findViewById(R.id.checkbox_fb);
        twitterCheckBox = (CheckBox) findViewById(R.id.checkbox_twitter);
        instagramCheckBox = (CheckBox) findViewById(R.id.checkbox_insta);
        syncCheckBox = (CheckBox) findViewById(R.id.checkbox_sync);

        facebookCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facebookCheckBox.isChecked()) {
                    facebookCheckBox.setChecked(false);
                    verifyFacebookPermissions();
                }
            }
        });

        twitterCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    twitterCheckBox.setChecked(false);
                    verifyTwitterPermissions();
                }
            }
        });

        instagramCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    instagramCheckBox.setChecked(false);
                    verifyInstagramPermissions(TripSettingsActivity.this);
                }
            }
        });

        locationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationCheckBox.isChecked()) {
                    handleLocationClick();
                }
            }
        });

        Button continueBtn = (Button) findViewById(R.id.btn_continue);
        continueBtn.setText("Continue");
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tripOperations != null) {
                    updateTripSettings();
                } else
                    confirmCreateTrip();
            }
        });

        locationCheckBox.setEnabled(false);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        mGoogleApiClient.connect();
        checkLocationSettings();

        if (tripOperations != null) {
            setup();
        } else {
            progressDialog = CenterProgressDialog.show(this, null, null, true, false);
            new CheckSettingsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void verifyInstagramPermissions(final Context context) {
        try {
            if (!ParseInstagramUtil.isLinked(currentUser, context)) {
                InstagramApp instagramApp = new InstagramApp(context, InstagramConstants.CLIENT_ID, InstagramConstants.CLIENT_SECRET, InstagramConstants.REDIRECT_URL);
                instagramApp.setListener(new InstagramApp.OAuthAuthenticationListener() {
                    @Override
                    public void onSuccess() {
                        instagramCheckBox.setChecked(true);
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onFail(String error) {
                        instagramCheckBox.setChecked(false);
                        Toast.makeText(context, error, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                instagramApp.authorize();
            } else {
                instagramCheckBox.setChecked(true);
            }
        }
        catch (Exception e) {
            instagramCheckBox.setChecked(false);
        }

    }

//    private void verifyInstagramPermissions() {
//        if (!ParseInstagramUtil.isLinked(currentUser)) {
//            //progressDialog = CenterProgressDialog.show(TripSettingsActivity.this, null, null, true, false);
//            ParseInstagramUtil.link(currentUser, this, new SaveCallback() {
//                @Override
//                public void done(ParseException ex) {
//                    try {
//                        currentUser.save();
//                    } catch (Exception e) {
//                        Log.e(TAG, "Exception saving user", e);
//                    }
//                    if (ParseInstagramUtil.isLinked(currentUser)) {
//                        //Work around for Auth Data restriction
//                        try {
//                            ParseCustomUser customUser = (ParseCustomUser) currentUser;
//                            customUser.put("instagram_auth", customUser.getAuthData().getJSONObject("instagram"));
//                            customUser.getAuthData().remove("instagram");
//                            customUser.save();
//                            instagramCheckBox.setChecked(true);
//                        } catch (Exception e) {
//                            Log.e(TAG, "Exception saving user", e);
//                        }
//                    } else {
//                        instagramCheckBox.setChecked(false);
//                        Toast.makeText(TripSettingsActivity.this, R.string.instagram_permissions_error_text
//                                , Toast.LENGTH_LONG).show();
//                    }
//                    if (progressDialog != null) {
//                        progressDialog.dismiss();
//                    }
//                }
//            });
//        } else {
//            instagramCheckBox.setChecked(true);
//        }
//    }

    private void updateTripSettings() {
        progressDialog = CenterProgressDialog.show(this, null, null, true, false);

        tripLocal.setAccessLocation(locationCheckBox.isChecked());
        tripLocal.setAccessCameraRoll(cameraRollCheckBox.isChecked());
        tripLocal.setAccessCheckIn(checkInCheckBox.isChecked());
        tripLocal.setAccessFacebook(facebookCheckBox.isChecked());
        tripLocal.setAccessInstagram(instagramCheckBox.isChecked());
        tripLocal.setAccessTwitter(twitterCheckBox.isChecked());
        tripLocal.setAccessSync(syncCheckBox.isChecked());

        new UpdateTripSettingsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setup() {
        checkInCheckBox.setChecked(tripLocal.isAccessCheckIn());
        cameraRollCheckBox.setChecked(tripLocal.isAccessCameraRoll());
        facebookCheckBox.setChecked(tripLocal.isAccessFacebook());
        twitterCheckBox.setChecked(tripLocal.isAccessTwitter());
        instagramCheckBox.setChecked(tripLocal.isAccessInstagram());
        syncCheckBox.setChecked(tripLocal.isAccessSync());
    }

    private void confirmCreateTrip() {
        tripLocal.setAccessLocation(locationCheckBox.isChecked());
        tripLocal.setAccessCameraRoll(cameraRollCheckBox.isChecked());
        tripLocal.setAccessCheckIn(checkInCheckBox.isChecked());
        tripLocal.setAccessFacebook(facebookCheckBox.isChecked());
        tripLocal.setAccessInstagram(instagramCheckBox.isChecked());
        tripLocal.setAccessTwitter(twitterCheckBox.isChecked());
        tripLocal.setAccessSync(syncCheckBox.isChecked());
        createTrip();
       /* List<String> requiredPermission = new ArrayList<>();
        if(!tripLocal.isAccessFacebook()){
            requiredPermission.add("Facebook");
        }

        if(!tripLocal.isAccessTwitter()){
            requiredPermission.add("Twitter");
        }

        if(!tripLocal.isAccessInstagram()){
            requiredPermission.add("Instagram");
        }

        if(requiredPermission.size() > 0 && !tripLocal.isAccessFacebook()){
            String message = "We need ";
            if(requiredPermission.size() == 1){
                message += requiredPermission.get(0);
            }
            else if(requiredPermission.size() == 2){
                message += requiredPermission.get(0) + " and " + requiredPermission.get(1);
            }
            else{
                message += requiredPermission.get(0) + ", " + requiredPermission.get(1) + " and " + requiredPermission.get(2);
            }
            message += " permissions to include in your timeline";
            new MaterialDialog.Builder(this)
                    .content(message)
                    .positiveText(R.string.give_permission)
                    .negativeText(R.string.not_now)
                    .positiveColor(getResources().getColor(R.color.orange_primary))
                    .negativeColor(getResources().getColor(R.color.orange_primary))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            if(!tripLocal.isAccessFacebook()){
                                verifyFacebookPermissions();
                                return;
                            }

                            if(!tripLocal.isAccessTwitter()){
                                verifyTwitterPermissions();
                                return;
                            }

                            if(!tripLocal.isAccessInstagram()){
                                verifyInstagramPermissions();
                                return;
                            }
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            createTrip();
                        }
                    })
                    .show();
        }
        else{
            createTrip();
        }*/
    }

    private void createTrip() {
        tripLocal.setAccessLocation(locationCheckBox.isChecked());
        tripLocal.setAccessCameraRoll(cameraRollCheckBox.isChecked());
        tripLocal.setAccessCheckIn(checkInCheckBox.isChecked());
        tripLocal.setAccessFacebook(facebookCheckBox.isChecked());
        tripLocal.setAccessInstagram(instagramCheckBox.isChecked());
        tripLocal.setAccessTwitter(twitterCheckBox.isChecked());
        tripLocal.setAccessSync(syncCheckBox.isChecked());
        progressDialog = CenterProgressDialog.show(this, null, null, true, false);
        new CreateTripTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void verifyFacebookPermissions() {
        if (ParseFacebookUtils.isLinked(currentUser)) {
            if (facebookPermissions != null && FacebookUtils.permissionsAvailable(facebookPermissions)) {
                facebookCheckBox.setChecked(true);
                return;
            }
        }

        //progressDialog = CenterProgressDialog.show(TripSettingsActivity.this, null, null, true, false);

        ParseFacebookUtils.linkWithReadPermissionsInBackground(currentUser, this
                , FacebookUtils.getFacebookReadPermissions(), new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        if (e == null) {
                            ParseCustomUser customUser = (ParseCustomUser) currentUser;
                            FacebookUtils.saveFacebookId(customUser);
                            progressDialog = CenterProgressDialog.show(TripSettingsActivity.this, null, null, true, false);
                            new CheckFacebookPermissionsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            ParseFacebookUtils.unlinkInBackground(currentUser);
                            //   LoginManager.getInstance().logOut();
                            Log.e("Error", "Error while getting link permissions", e);
                            Toast.makeText(TripSettingsActivity.this,
                                    "An Error occurred while linking your facebook account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void verifyTwitterPermissions() {
        if (!ParseTwitterUtils.isLinked(currentUser)) {
            //progressDialog = CenterProgressDialog.show(TripSettingsActivity.this, null, null, true, false);
            ParseTwitterUtils.link(currentUser, this, new SaveCallback() {
                @Override
                public void done(ParseException ex) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    if (ex == null) {
                        if (ParseTwitterUtils.isLinked(currentUser)) {
                            twitterCheckBox.setChecked(true);
                        } else {
                            twitterCheckBox.setChecked(false);
                            Toast.makeText(TripSettingsActivity.this, R.string.twitter_permissions_error_text
                                    , Toast.LENGTH_LONG).show();
                        }
                    } else {
                        ParseTwitterUtils.unlinkInBackground(currentUser);
                        Log.e("Error", "Error while getting link permissions", ex);
                        Toast.makeText(TripSettingsActivity.this,
                                "An Error occurred while linking your twitter account: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            twitterCheckBox.setChecked(true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    locationEnabled = true;
                    break;
                case Activity.RESULT_CANCELED:
                    locationEnabled = false;
                    break;
            }
            updateLocationStatus();
            return;
        }
        if (requestCode >= FacebookUtils.ACTIVITY_REQUEST_CODE_OFFSET) {
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();
    }

    private class CheckFacebookPermissionsTask extends AsyncTask<Void, Void, Void> {

        private boolean hasAccess;

        @Override
        protected Void doInBackground(Void... params) {
            if (ParseFacebookUtils.isLinked(currentUser)) {
                GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), FacebookUtils.getPermissionsUrl(), new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        if (graphResponse.getError() == null) {
                            facebookPermissions = FacebookPermission.parsePermissions(graphResponse.getJSONObject());
                            hasAccess = FacebookUtils.permissionsAvailable(facebookPermissions);

                        }
                    }
                }).executeAndWait();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            if (hasAccess)
                facebookCheckBox.setChecked(true);
            else {
                Toast.makeText(TripSettingsActivity.this, R.string.facebook_permissions_error_text
                        , Toast.LENGTH_LONG).show();
            }
        }
    }

    private class CheckSettingsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (ParseFacebookUtils.isLinked(currentUser)) {
                GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), FacebookUtils.getPermissionsUrl(), new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        if (graphResponse.getError() == null) {
                            facebookPermissions = FacebookPermission.parsePermissions(graphResponse.getJSONObject());
                            tripLocal.setAccessFacebook(FacebookUtils.permissionsAvailable(facebookPermissions));
                        }
                    }
                }).executeAndWait();
            } else {
                tripLocal.setAccessFacebook(false);
            }

            if (ParseTwitterUtils.isLinked(currentUser)) {
                tripLocal.setAccessTwitter(true);
            } else {
                tripLocal.setAccessTwitter(false);
            }

//            if (ParseInstagramUtils.isLinked(currentUser)) {
//                tripLocal.setAccessInstagram(true);
//            } else {
//                tripLocal.setAccessInstagram(false);
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            setup();
        }
    }

    private class CreateTripTask extends AsyncTask<Void, Void, Void> {

        private boolean saved = false;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ((TripLocalOperations) TripUtils.getInstance().getCurrentTripOperations()).createTrip(tripLocal);
                saved = true;
            } catch (ParseException e) {
                Log.e(TripStartActivity.class.toString(), "Unable to create Trip", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (saved) {
                Intent intent = new Intent(TripServiceManager.TRIP_SERVICE_BROADCAST_INTENT);
                sendBroadcast(intent);
                next();
            } else {
                Toast.makeText(TripSettingsActivity.this, getString(R.string.toat_trip_start_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateTripSettingsTask extends AsyncTask<Void, Void, Void> {

        private boolean updated = false;

        @Override
        protected Void doInBackground(Void... params) {
            TripSettings tripSettings = tripOperations.getTrip().getSettings();
            tripSettings.setFacebook(tripLocal.isAccessFacebook());
            tripSettings.setCameraRoll(tripLocal.isAccessCameraRoll());
            tripSettings.setCheckIn(tripLocal.isAccessCheckIn());
            tripSettings.setLocation(tripLocal.isAccessLocation());
            tripSettings.setSync(tripLocal.isAccessSync());
            tripSettings.setTwitter(tripLocal.isAccessTwitter());
            tripSettings.setInstagram(tripLocal.isAccessInstagram());
            try {
                ((TripLocalOperations) tripOperations).save(tripSettings);
                //Stop Existing services
                Intent intent = new Intent(TripServiceStopManager.TRIP_SERVICE_STOP_BROADCAST_INTENT);
                sendBroadcast(intent);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent mIntent = new Intent(TripServiceManager.TRIP_SERVICE_BROADCAST_INTENT);
                sendBroadcast(mIntent);

                updated = true;

            } catch (ParseException e) {
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (updated) {
                Intent intent = new Intent(TripSettingsActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //this intent tells the Home Activity to show tab no 2 (position 1) when called from here
                intent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.JOURNAL_TAB);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(TripSettingsActivity.this, "Error updating trip settings", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void next() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.JOURNAL_TAB);
        intent.putExtra(TripFragment.SHOW_JOURNAL_DAY, 1);
        if(couponId != null) {
            intent.putExtra(TripStartActivity.COUPON_ID, couponId);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    //Handle Location
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private boolean locationEnabled;
    private boolean requireLocation;
    private Status locationSettingStatus;
    private Location mCurrentLocation;

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        locationSettingStatus = locationSettingsResult.getStatus();
        locationCheckBox.setEnabled(true);
        switch (locationSettingStatus.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                locationEnabled = true;
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                resolveSettings();
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                locationEnabled = false;
                locationCheckBox.setEnabled(false);
                break;
        }
        updateLocationStatus();
    }

    private void handleLocationClick() {
        locationCheckBox.setChecked(false);
        if (locationEnabled) {
            locationCheckBox.setChecked(true);
        } else {
            requireLocation = true;
            resolveSettings();
        }
    }

    private void updateLocationStatus() {
        if (locationEnabled) {
            locationCheckBox.setChecked(true);
            if (mGoogleApiClient != null)
                mGoogleApiClient.disconnect();
        } else {
            locationCheckBox.setChecked(false);
        }
    }

    private void resolveSettings() {
        try {
            if (requireLocation)
                locationSettingStatus.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            Log.i(TAG, "PendingIntent unable to execute request.");
        }
    }

    private synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(LocationService.Constants.UPDATE_INTERVAL);

        mLocationRequest.setFastestInterval(LocationService.Constants.FASTEST_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void drawCoachMarks() {

        if (!SharedPrefUtils.getBooleanPreference(TripSettingsActivity.this, COACH_PERMISSION_PREF)) {

            View view = toolbar.getChildAt(1);

            Target markLocationByView[] = new Target[1];
            markLocationByView[0] = new ViewTarget(view);

            Point markLocationByOffset[] = new Point[1];
            markLocationByOffset[0] = new Point(60,10);

            Point markTextPoint[] = new Point[1];
            markTextPoint[0] = new Point(-30,100);

            float circleRadius[] = new float[1];
            circleRadius[0] = 45f;

            String coachMarkTextArray[];

            coachMarkTextArray = getResources().getStringArray(R.array.coachmark_permissions);

            showcaseView = new ShowcaseView.Builder(TripSettingsActivity.this, true, true)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray,circleRadius)
                    .setOnClickListener(TripSettingsActivity.this)
                    .build();

            showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

            SharedPrefUtils.setBooleanPreference(TripSettingsActivity.this, COACH_PERMISSION_PREF, true);
        }
    }
}
