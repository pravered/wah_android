package com.weareholidays.bia.activities.journal.base;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Teja on 23-06-2015.
 */
public class TripBaseActivity extends AppCompatActivity implements TripOperationsLoader {

    public static String IGNORE_TRIP_BASE;

    protected TripOperations tripOperations;

    private CenterProgressDialog progressDialog;

    private LoadTripTask task;

    private String trip_key;

    private String timeline_key;

    private String media_key = "test_key";

    @Override
    public void onCreate(Bundle savedBundle){

        int version = Build.VERSION.SDK_INT;
        CalligraphyConfig.Builder calligraphyConfig = new CalligraphyConfig.Builder();
        calligraphyConfig.setFontAttrId(R.attr.fontPath);
//        if(version<17)
        calligraphyConfig.setDefaultFontPath("fonts/roboto/Roboto-Regular.ttf");
        CalligraphyConfig.initDefault(calligraphyConfig.build());

        if(getIntent().hasExtra(IGNORE_TRIP_BASE)){
            super.onCreate(savedBundle);
            onTripLoaded(savedBundle);
            return;
        }

        trip_key = TripOperations.CURRENT_TRIP_ID;
        if(getIntent() != null){
            trip_key = getIntent().getStringExtra(TripOperations.TRIP_KEY_ARG);
            timeline_key = getIntent().getStringExtra(TripOperations.TRIP_ALBUM_ARG);
            media_key = getIntent().getStringExtra(TripOperations.TRIP_MEDIA_ARG);
        }
        if(TextUtils.isEmpty(trip_key))
            trip_key = TripOperations.CURRENT_TRIP_ID;
        tripOperations = TripUtils.getInstance().getTripOperations(trip_key);
        if(tripOperations != null && tripOperations.isTripLoaded() && tripOperations.isTripAvailable()
                && (TextUtils.isEmpty(timeline_key) || tripOperations.getTimeLine() != null)){
            super.onCreate(savedBundle);
            onTripLoaded(savedBundle);
        }
        else{
            //TODO: Resolve activity recreation on tab clicks
            task = new LoadTripTask(savedBundle);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            super.onCreate(null);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        if(task != null && !task.isCancelled() && task.getStatus() == AsyncTask.Status.RUNNING){
            task.cancel(true);
        }
    }

    @CallSuper
    protected void onTripLoaded(Bundle savedBundle){
        if(task != null)
        {
            task = null;
        }
    }

    @Override
    public TripOperations getTripOperations() {
        return tripOperations;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        tripOperations = null;
        task = null;
    }

    private class LoadTripTask extends AsyncTask<Void,Void,Void>{

        private Bundle savedInstance;

        private boolean albumMediaLoaded;

        LoadTripTask(Bundle savedInstance){
            this.savedInstance = savedInstance;
        }

        @Override
        protected void onPreExecute(){
            progressDialog = CenterProgressDialog.show(TripBaseActivity.this,"Loading Trip...",null,true,true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(tripOperations == null){
                tripOperations = TripUtils.getInstance().loadServerViewTrip(trip_key);
            }
            if(!tripOperations.isTripLoaded())
                tripOperations.loadTrip();
            if(!TextUtils.isEmpty(timeline_key)){
                Timeline timeline = tripOperations.loadTimeline(timeline_key);
                if(timeline != null){
                    if(Timeline.ALBUM_CONTENT.equals(timeline.getContentType())){
                        try {
                            Album album = (Album) timeline.getContent();
                            List<Media> mediaList = tripOperations.getAlbumMedia(album);
                            if(mediaList != null && mediaList.size() > 0){
                                tripOperations.populateMediaSource(mediaList);
                                for(Media md: mediaList){
                                    if(media_key.equals(md.getObjectId())){
                                        tripOperations.setSelectedMedia(md);
                                        albumMediaLoaded = true;
                                    }
                                }
                                album.setMedia(mediaList);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            if(progressDialog != null){
                progressDialog.dismiss();
                if(tripOperations.isTripAvailable()){
                    if(!TextUtils.isEmpty(timeline_key) && !albumMediaLoaded){
                        Toast.makeText(TripBaseActivity.this,"Something went wrong!. Unable to find the album photo",Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    onTripLoaded(savedInstance);
                }
                else{
                    Toast.makeText(TripBaseActivity.this,"Something went wrong!. Unable to find the trip",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(NavigationUtils.handleTripBackNavigation(getIntent()) && tripOperations != null){
            startActivity(NavigationUtils.getTripInent(this,tripOperations.getTripKey(),getIntent()));
            NavigationUtils.closeAnimation(this);
            finish();
            return;
        }
        super.onBackPressed();
    }
}
