package com.weareholidays.bia.activities.journal.timeline;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripAsyncCallback;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.utils.reorderUtils.DayTimeLine;
import com.weareholidays.bia.widgets.CenterProgressDialog;
import com.weareholidays.bia.widgets.TabLayoutCustom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReorderActivity extends TripBaseActivity implements TimeLineFragment.OnFragmentInteractionListener{
    private ViewPager tripViewPager;
    public static String SHOW_JOURNAL_DAY = "SHOW_JOURNAL_DAY";
    public static String SOCIAL_TYPE = "SOCIAL_TYPE";
    public static String FROM_REORDER = "FROM_REORDER";

    public static int JOURNAL_SUMMARY_VIEW = -1;
    private TabLayout tripTabs;
    private int defaultTab = JOURNAL_SUMMARY_VIEW;

    private TripOperations tripOperations;

    private int selectedDayOrder;
    private Trip trip;
    private ImageView background;
    private ImageView blur;
    private LinearLayout done;

    private Map<Integer,DayTimeLine> timeLinesMap = new HashMap<>();

    @Override
    public void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        tripOperations = super.getTripOperations();
        trip = tripOperations.getTrip();
        if(savedInstanceState != null && savedInstanceState.containsKey(SHOW_JOURNAL_DAY))
            defaultTab = savedInstanceState.getInt(SHOW_JOURNAL_DAY, JOURNAL_SUMMARY_VIEW);
        defaultTab++;//increase the index
        setContentView(R.layout.activity_reorder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        done = (LinearLayout)toolbar.findViewById(R.id.reorder_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReorder();
            }
        });
        toolbar.setNavigationIcon(R.drawable.timeline_reorderpost_cross);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reorder Posts");
        String tripName = tripOperations.getTrip().getName();
        if(tripName.length()>21)
            tripName = tripName.substring(0,20)+"....";
        getSupportActionBar().setSubtitle(tripName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setup();
    }

    private void saveReorder(){
        new ReorderTask(tripOperations,timeLinesMap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setup() {

        tripTabs = (TabLayout) findViewById(R.id.trip_tabs);

        tripViewPager = (ViewPager) findViewById(R.id.trip_pager);

        tripViewPager.setAdapter(new TripSlidePagerAdapter(getSupportFragmentManager()));

        tripTabs.setupWithViewPager(tripViewPager);

        background = (ImageView)findViewById(R.id.background_trip);
        blur = (ImageView)findViewById(R.id.background_blur);

        tripOperations.getTripFeatureImage(new TripAsyncCallback<String>() {
            @Override
            public void onCallBack(String result) {
                /*final Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable drawable = new BitmapDrawable(getResources(), ViewUtils.blurfast(bitmap, 8));
                        background.setImageDrawable(drawable);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };
                background.setTag(target);*/

                if(!TextUtils.isEmpty(result)) {
                    Glide.with(ReorderActivity.this)
                            .load(result)
                            .asBitmap()
                            .into(new BitmapImageViewTarget(background) {
                                protected void setResource(Bitmap resource) {
                                    Drawable drawable = new BitmapDrawable(getResources(), ViewUtils.blurfast(resource, 8));
                                    background.setImageDrawable(drawable);
                                }
                            });
                    // Picasso.with(ReorderActivity.this).load(result).into(target);
                }
                else{
                    // Picasso.with(ReorderActivity.this)
                    //           .load(R.drawable.reorder_background)
                    //          .into(background);
                    Glide.with(ReorderActivity.this)
                            .load(R.drawable.reorder_background)
                            .into(background);
                }
            }
        });



        tripViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedDayOrder = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tripTabs.getTabAt(defaultTab).select();
        if(trip.getDays().size() > 3)
            tripTabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        if(trip.getDays().size() == 1){
            ((TabLayoutCustom)tripTabs).setOverrideMaxWidth(true);
        }
    }

    @Override
    public boolean loadFromActivity() {
        return true;
    }

    @Override
    public DayTimeLine getDayTimeLines(int dayOrder) {
        if(!timeLinesMap.containsKey(dayOrder)){
            DayTimeLine dayTimeLine = new DayTimeLine();
            timeLinesMap.put(dayOrder,dayTimeLine);
        }
        return timeLinesMap.get(dayOrder);
    }

    @Override
    public boolean hasChanges() {
        return false;
    }

    @Override
    public void onTimelineScrollChanged(int scrollIndex, int dayOrder) {

    }

    private class TripSlidePagerAdapter extends FragmentStatePagerAdapter {

        public TripSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putString(TimeLineFragment.TIMELINE_TYPE, TimeLineFragment.DAY_TIMELINE);
            args.putInt(TimeLineFragment.DAY_ORDER, position);
            args.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
            args.putBoolean(FROM_REORDER, true);
            return Fragment.instantiate(getBaseContext(),TimeLineFragment.class.getName(),args);
        }

        @Override
        public int getCount() {
            return  trip.getDays().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return trip.getDays().get(position).getName();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reorder, menu);
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

        if(id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public TripOperations getTripOperations() {
        return tripOperations;
    }

    private class ReorderTask extends AsyncTask<Void,Void,Void>{

        private CenterProgressDialog progressDialog;
        private TripOperations tripOperationsTask;
        private Map<Integer,DayTimeLine> timeLinesMapTask;

        public ReorderTask(TripOperations tripOperations, Map<Integer,DayTimeLine> timeLinesMap) {
            this.tripOperationsTask = tripOperations;
            this.timeLinesMapTask = timeLinesMap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressDialog = CenterProgressDialog.show(ReorderActivity.this,null,null,true);
            } catch (Exception e){

            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
                setResult(RESULT_OK);
                finish();
            } catch (Exception e){

            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                Set<Integer> days = timeLinesMapTask.keySet();
                for(Integer dayOrder : days){
                    DayTimeLine dayTimeLine = timeLinesMapTask.get(dayOrder);
                    if(dayTimeLine.isReordered()){
                        List<Timeline> timelines = dayTimeLine.getTimelines();
                        if(timelines.size() > 0){
                            int displayOrder = 0;
                            for(Timeline timeline: timelines){
                                timeline.setDisplayOrder(displayOrder++);
                            }
                            List<Timeline> pendingTimelines = tripOperationsTask.getDayTimeLines(dayOrder,1000,dayTimeLine.getSkip(),null,null);
                            tripOperationsTask.saveAll(timelines);
                            if(pendingTimelines.size() > 0){
                                for (Timeline timeline: pendingTimelines){
                                    timeline.setDisplayOrder(displayOrder++);
                                }
                                tripOperationsTask.saveAll(pendingTimelines);
                            }
                        }
                    }
                }
            }catch (Exception e){

            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Set<Integer> days = timeLinesMap.keySet();
        //flag = true indicates changes were made
        boolean flag = false;
        for(Integer dayOrder : days) {
            DayTimeLine dayTimeLine = timeLinesMap.get(dayOrder);
            if(dayTimeLine.isReordered()) {
                flag = true;
                new MaterialDialog.Builder(this)
                        .widgetColor(getResources().getColor(R.color.orange_primary))
                        .positiveText("Keep Changes")
                        .negativeText("Discard Changes")
                        .positiveColor(getResources().getColor(R.color.orange_primary))
                        .negativeColor(getResources().getColor(R.color.orange_primary))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                dialog.dismiss();
                                finish();
                            }

                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                dialog.dismiss();
                            }
                        }).title("You have unsaved changes!! Do you still want to continue?").show();
                break;
            }
        }
        if (!flag)
            super.onBackPressed();
    }

}
