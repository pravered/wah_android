package com.weareholidays.bia.activities.journal.trip;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.activities.journal.people.AddPeopleActivity;
import com.weareholidays.bia.asyncTasks.ShortenURLTask;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripOperations;

public class ShareTripActivity extends TripBaseActivity {

    public static String REDIRECT_ACTIVITY = "REDIRECT_ACTIVITY";

    private static final int SHARE_WITH_FRIENDS_REQUEST_CODE = 9854;

    private Class<?> redirectActivity;

    private LinearLayout shareWithFriends;
    private LinearLayout shareViaApps;
    private boolean redirectToHome;
    private ShareActionProvider mShareActionProvider;

    private boolean photoShare;
    private boolean appShare;
    private ShortenURLTask mShortenURLTask;

    @Override
    protected void onTripLoaded(Bundle savedBundle) {
        super.onTripLoaded(savedBundle);
        setContentView(R.layout.activity_share_trip);

        if(getIntent().hasExtra(REDIRECT_ACTIVITY)){
            redirectActivity = (Class<?>)getIntent().getSerializableExtra(REDIRECT_ACTIVITY);
        }
        else{
            redirectActivity = HomeActivity.class;
        }

        getSupportActionBar().setTitle("Share Trip");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent().hasExtra(AddPeopleActivity.SHARE_IMAGE_VIEW)){
            photoShare = true;
            getSupportActionBar().setTitle("Share Photo");

        }

        if(getIntent().hasExtra(AddPeopleActivity.SHARE_APP_VIEW)){
            appShare = true;
            getSupportActionBar().setTitle("Share App");
        }

        shareWithFriends = (LinearLayout) findViewById(R.id.share1);
        shareViaApps = (LinearLayout) findViewById(R.id.share2);

        shareWithFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShareTripActivity.this, AddPeopleActivity.class);
                Bundle bundle = new Bundle();
                if(!appShare)
                    bundle.putString(TripOperations.TRIP_KEY_ARG,tripOperations.getTripKey());
                if(photoShare)
                    bundle.putString(AddPeopleActivity.SHARE_IMAGE_VIEW,"yes");
                else if(appShare)
                    bundle.putString(AddPeopleActivity.SHARE_APP_VIEW,"yes");
                else
                    bundle.putString(AddPeopleActivity.SHARE_PEOPLE_VIEW,"yes");
                intent.putExtras(bundle);
                startActivityForResult(intent, SHARE_WITH_FRIENDS_REQUEST_CODE);
            }
        });

        shareViaApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareViaOtherApps();
            }
        });

        if(appShare){
            shareViaOtherApps();
            shareWithFriends.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SHARE_WITH_FRIENDS_REQUEST_CODE && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void shareViaOtherApps(){
        if(photoShare) {
            String sendUrl = ShareUtils.getEncodedPhotoShareUrl(tripOperations.getTrip(),
                    tripOperations.getTimeLine(), tripOperations.getSelectedMedia());
            mShortenURLTask = new ShortenURLTask(ShareTripActivity.this,tripOperations.getTrip().getName(), tripOperations.getSelectedMedia().getCaption(), true, false);
            mShortenURLTask.execute(sendUrl);
        } else if(appShare) {
            String sendUrl = ShareUtils.getPlayStoreUrl(ShareTripActivity.this);
            mShortenURLTask = new ShortenURLTask(ShareTripActivity.this,tripOperations.getTrip().getName(), null, false, true);
            mShortenURLTask.execute(sendUrl);
        } else {  //share trips
            String sendUrl = ShareUtils.getEncodedTripShareUrl(tripOperations.getTrip());
            mShortenURLTask = new ShortenURLTask(ShareTripActivity.this,tripOperations.getTrip().getName(), null, false, false);
            mShortenURLTask.execute(sendUrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_trip, menu);
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
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        if(tripOperations != null){
//            Intent intent = new Intent(this, redirectActivity);
//            intent.putExtra(TripOperations.TRIP_KEY_ARG,tripOperations.getTripKey());
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//        }
//        else{
//            super.onBackPressed();
//        }
//    }
}
