package com.weareholidays.bia.activities.journal.trip;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;
//import twitter4j.auth.AccessToken;
import wahCustomViews.view.WahImageView;

public class PublishTripActivity extends TripBaseActivity {

    private static final int SHARE_PUBLISHED_TRIP = 323;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy");
    private Trip trip;
    private CheckBox facebook;
    private CheckBox twitter;
    private MaterialDialog materialDialog;

    @Override
    public void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        setContentView(R.layout.activity_publish_trip);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Publish Journal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        trip = tripOperations.getTrip();

        WahImageView trip_pic = (WahImageView) findViewById(R.id.picture_holder);
        if (trip.getFeatureImage() != null) {
            trip_pic.setImageUrl(trip.getFeatureImage().getUrl());
            /*Glide.with(this)
                    .load(trip.getFeatureImage().getUrl())
                    .centerCrop()
                    .crossFade()
                    .into(trip_pic);*/
        }

        TextView tripName = (TextView)findViewById(R.id.trip_name);

        String tripN = trip.getName();
        if(tripName.length()>21)
            tripN = tripN.substring(0,20)+"....";
        tripName.setText(tripN);

        WahImageView userImage = (WahImageView)findViewById(R.id.trip_user_image);
        if(trip.getOwner().getProfileImage() != null){
            /*Glide.with(this)
                    .load(trip.getOwner().getProfileImage().getUrl())
                    .centerCrop()
                    .crossFade()
                    .into(userImage);*/
            userImage.setImageUrl(trip.getOwner().getProfileImage().getUrl());
        }

        TextView tripDays = (TextView)findViewById(R.id.trip_days);
        String dayText = " Days";
        if (trip.getDays().size() == 1)
            dayText = " Day";
        tripDays.setText("\u2022 " + trip.getDays().size() + dayText);

        TextView tripDate = (TextView)findViewById(R.id.trip_date);
        tripDate.setText("\u2022 " + simpleDateFormat.format(trip.getStartTime()));

        facebook = (CheckBox) findViewById(R.id.checkbox_fb);
        //twitter = (CheckBox) findViewById(R.id.checkbox_twitter);

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(facebook.isChecked()){
                    if(ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())){
                        if(FacebookUtils.hasPublishPermissions()){
                            return;
                        }
                    }
                    facebook.setChecked(false);
                    ParseFacebookUtils.linkWithPublishPermissionsInBackground(ParseUser.getCurrentUser()
                            , PublishTripActivity.this, FacebookUtils.getFacebookPublishPermissions(), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        if(FacebookUtils.hasPublishPermissions())
                                            facebook.setChecked(true);
                                    }
                                    else{
                                        Log.e("Error","Error while getting link permissions",e);
                                        Toast.makeText(PublishTripActivity.this,
                                                "An Error occured while linking facebook account: " + e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        final Button button = (Button) findViewById(R.id.publish_trip_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                publishTrip();
            }
        });
    }

    public void publishTrip() {
        new PublishTripTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class PublishTripTask extends AsyncTask<Void,Void,Void>{

        private boolean failed;

        private boolean failedFacebook;

        private boolean postToFacebook;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String tripName = tripOperations.getTrip().getName();
            if(tripName.length()>21)
                tripName = tripName.substring(0,20)+"....";
            materialDialog = new MaterialDialog.Builder(PublishTripActivity.this)
                    .content("\"" + tripName + "\"")
                    .progress(true,0)
                    .widgetColor(getResources().getColor(R.color.orange_primary))
                    .cancelable(false)
                    .autoDismiss(false)
                    .title("Publishing Trip").show();
            if(facebook.isChecked())
                postToFacebook = true;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(isCancelled())
                return;
            if(materialDialog != null){
                materialDialog.dismiss();
                if(failed)
                    Toast.makeText(PublishTripActivity.this,"Error while publishing Trip",Toast.LENGTH_LONG).show();
                if(failedFacebook)
                    Toast.makeText(PublishTripActivity.this,"Error while posting the trip to facebook",Toast.LENGTH_LONG).show();
            }
            View dialogView = getLayoutInflater().inflate(R.layout.tickmark, null);

            String tripName = tripOperations.getTrip().getName();
            if(tripName.length()>21)
                tripName = tripName.substring(0,20)+"....";
            ((TextView) dialogView.findViewById(R.id.trip_name)).setText("\"" + tripName + "\"");
            new MaterialDialog.Builder(PublishTripActivity.this)
                    .title("Published")
                    //.content("\"" + trip.getName() + "\"")
                    .customView(dialogView, true)
                    .widgetColor(getResources().getColor(R.color.orange_primary))
                    .neutralText("CONTINUE")
                    .positiveText("SHARE")
                    .cancelable(false)
                    .autoDismiss(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            Intent intent = new Intent(PublishTripActivity.this, ShareTripActivity.class);
                            intent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                            startActivityForResult(intent,SHARE_PUBLISHED_TRIP);

                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            super.onNeutral(dialog);
                            goToHome();
                        }
                    })
                    .show();



        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                tripOperations.publish();

                if(postToFacebook){
                    new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            FacebookUtils.getPublishPostUrl(),
                            FacebookUtils.getPublishPostBundle(tripOperations.getTrip()),
                            HttpMethod.POST,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    if(response.getError() != null){
                                        failedFacebook = true;
                                    }
                                }
                            }
                    ).executeAndWait();
                }

            } catch (Exception e) {
                failed = true;
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_publish_trip, menu);
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

    private void goToHome(){
        Intent intent = new Intent(PublishTripActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SHARE_PUBLISHED_TRIP){
            goToHome();
            return;
        }
        if(requestCode >= FacebookUtils.ACTIVITY_REQUEST_CODE_OFFSET){
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
