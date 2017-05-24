package com.weareholidays.bia.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.trip.TripActivity;
import com.weareholidays.bia.activities.journal.trip.TripFragment;
import com.weareholidays.bia.activities.login.SignUpActivity;
import com.weareholidays.bia.activities.onboarding.OnboardingActivity;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.Version;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;

import java.util.List;


public class SplashActivity extends AppCompatActivity {

    private boolean shareLink;
    private Uri shareUrl;

    private int accessLocationRequestCode = 111;
    private int cameraRequestCode = 112;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String action = getIntent().getAction();
        if("android.intent.action.VIEW".equals(action)){
            shareUrl = getIntent().getData();
            if(shareUrl != null)
                shareLink = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isGooglePlayServicesAvailable())
            new InitialTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
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

        return super.onOptionsItemSelected(item);
    }

    private class InitialTask extends AsyncTask<Void,Void,Void> {

        private String tripKey;
        private boolean forceUpdate;
        @Override
        protected Void doInBackground(Void... params) {

//            if(ViewUtils.isNetworkAvailable(SplashActivity.this)){
//                try {
//                    List<Version> versionList = ParseQuery.getQuery(Version.class).setLimit(1).find();
//                    Version version = versionList.get(0);
//                    if(version != null) {
//                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//                        int appVersion = pInfo.versionCode;
//                        if(appVersion < version.getMinTripInVersion()){
//                            forceUpdate = true;
//                        }
//                        else if(appVersion < version.getMinTripOutVersion()){
//                            if(!TripUtils.getInstance().getCurrentTripOperations().isTripAvailable()){
//                                forceUpdate = true;
//                            }
//                        }
//                    }
//                    else{
//                        DebugUtils.LogE("Version object must be set in backend");
//                    }
//                } catch (Exception e) {
//                    DebugUtils.logException(e);
//                }
//            }

            if(ParseUser.getCurrentUser() != null) {
                TripUtils.getInstance().getCurrentTripOperations().loadTrip();
            }

            if(shareLink){
                tripKey = shareUrl.getQueryParameter("trip");
                String tStamp = shareUrl.getQueryParameter("t");
                if(tStamp != null && tripKey != null) {
                    long timestamp = Long.valueOf(tStamp).longValue();
                    String newMd5 = shareUrl.getQueryParameter("hash");
                    TripUtils.getInstance().loadServerViewTrip(tripKey);
                    Trip trip = TripUtils.getInstance().getCurrentTripOperations().getTrip();
                    if(trip != null){
                        if (!ShareUtils.isValidHash(tripKey, timestamp, trip.getSecretKey(), newMd5)) {
                            tripKey = "";
                        }
                    }
                    else{
                        tripKey = "";
                    }
                } else {
                    tripKey = "";
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            if(forceUpdate){
                MaterialDialog materialDialog = new MaterialDialog.Builder(SplashActivity.this)
                        .widgetColor(getResources().getColor(R.color.orange_primary))
                        .cancelable(false)
                        .autoDismiss(false)
                        .positiveText("Update")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                ShareUtils.openPlayStore(SplashActivity.this);
                            }
                        })
                        .title("A new version of the app is available ").show();
                return;
            }
            Intent intent = null;
            if(ParseUser.getCurrentUser() != null){
                intent = new Intent(SplashActivity.this,HomeActivity.class);
                if(TripUtils.getInstance().getCurrentTripOperations().isTripAvailable()){
                    Trip trip = TripUtils.getInstance().getCurrentTripOperations().getTrip();
                    intent.putExtra(HomeActivity.SHOW_TAB,HomeActivity.JOURNAL_TAB);
                    intent.putExtra(TripFragment.SHOW_JOURNAL_DAY,trip.getTotalDays());
                }
            }
            else{
                intent = new Intent(SplashActivity.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            if(!TextUtils.isEmpty(tripKey)){
                intent = new Intent(SplashActivity.this,TripActivity.class);
                intent.putExtra(TripOperations.TRIP_KEY_ARG,tripKey);
            }

            if(!getSharedPreferences("Onboard",MODE_PRIVATE).contains("Onboarded")){
                intent = new Intent(SplashActivity.this,OnboardingActivity.class);
                getSharedPreferences("Onboard",MODE_PRIVATE).edit().putBoolean("Onboarded",true).apply();
            }

            startActivity(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0,0);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
}
