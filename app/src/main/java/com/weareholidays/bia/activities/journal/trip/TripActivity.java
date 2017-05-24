package com.weareholidays.bia.activities.journal.trip;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseUser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.MyMapFragment;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.activities.journal.timeline.TimeLineFragment;
import com.weareholidays.bia.activities.journal.timeline.TripSummaryFragment;
import com.weareholidays.bia.background.services.ServiceUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.utils.reorderUtils.DayTimeLine;

/**
 * Created by Teja on 23-06-2015.
 */
public class TripActivity extends TripBaseActivity implements TripFragment.OnFragmentInteractionListener
        , TripSummaryFragment.OnFragmentInteractionListener, TimeLineFragment.OnFragmentInteractionListener
        , MyMapFragment.OnFragmentInteractionListener{

    public static String CLEAR_CURRENT_TRIP = "CLEAR_CURRENT_TRIP";
    public static final String SELECTED_TRIP_TAB = "SELECTED_TRIP_TAB";
    public static final String SELECTED_TRIP_TAB_SCROLL = "SELECTED_TRIP_TAB_SCROLL";

    public TripFragment.TripMenuOptionsHandler getTripMenuOptionsHandler() {
        return tripMenuOptionsHandler;
    }

    private TripFragment.TripMenuOptionsHandler tripMenuOptionsHandler;

    private int selectedTripTab = -1;
    private int selectedTripTabScrollPosition = -1;

    @Override
    public void onTripLoaded(Bundle savedInstanceState){
        super.onTripLoaded(savedInstanceState);
        setContentView(R.layout.activity_route);

        TripFragment tripFragment = new TripFragment();
        String trip_key = TripOperations.CURRENT_TRIP_ID;
        Bundle arguments = new Bundle();
        if(getIntent() != null){
            trip_key = getIntent().getStringExtra(TripOperations.TRIP_KEY_ARG);
            arguments = getIntent().getExtras();
        }

        if(arguments == null)
            arguments = new Bundle();

        if(TextUtils.isEmpty(trip_key))
            trip_key = TripOperations.CURRENT_TRIP_ID;
        arguments.putString(TripOperations.TRIP_KEY_ARG, trip_key);

        if(savedInstanceState == null){
            selectedTripTab = NavigationUtils.getTripDayFromIntent(getIntent());
            selectedTripTabScrollPosition = NavigationUtils.getTimelineFromIntent(getIntent());
        }
        else{
            selectedTripTab = savedInstanceState.getInt(SELECTED_TRIP_TAB,-1);
            selectedTripTabScrollPosition = savedInstanceState.getInt(SELECTED_TRIP_TAB_SCROLL,-1);
        }

        arguments.putInt(TripFragment.SHOW_JOURNAL_DAY,selectedTripTab);

        if(selectedTripTabScrollPosition >= 0){
            arguments.putInt(TripFragment.TIMELINE_SCROLL_POSITION,selectedTripTabScrollPosition);
        }

        tripFragment.setArguments(arguments);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, tripFragment).commit();

        try{
            if(!tripOperations.canWrite()){
                if(!ParseUser.getCurrentUser().getUsername().equals(tripOperations.getTrip().getOwner().getUsername())){
                    tripOperations.getTrip().addView();
                }
            }
        } catch (Exception e){
            DebugUtils.logException(e);
        }

        if(getIntent() != null && getIntent().hasExtra(CLEAR_CURRENT_TRIP)){
            new ClearCurrentTripTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_TRIP_TAB,selectedTripTab);
        outState.putInt(SELECTED_TRIP_TAB_SCROLL,selectedTripTabScrollPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed(){
        handleBack();
    }

    private void handleBack(){
        if(getIntent() != null){
            if(getIntent().hasExtra(ViewUtils.PARENT_ACTIVITY)){
                Intent intent = new Intent(this,(Class)getIntent().getSerializableExtra(ViewUtils.PARENT_ACTIVITY));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean loadFromActivity() {
        return false;
    }

    @Override
    public DayTimeLine getDayTimeLines(int dayOrder) {
        return null;
    }

    @Override
    public boolean hasChanges() {
        return false;
    }

    @Override
    public void onTimelineScrollChanged(int scrollIndex, int dayOrder) {
        selectedTripTab = dayOrder;
        selectedTripTabScrollPosition = scrollIndex;
    }

    @Override
    public void setSupportToolbarInTripFragment(Toolbar toolbar,TripFragment.TripMenuOptionsHandler tripMenuOptionsHandler) {
        this.tripMenuOptionsHandler = tripMenuOptionsHandler;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onTripTabChanged(int pageIndex) {
        selectedTripTab = pageIndex - 1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == android.R.id.home){
            onBackPressed();
            return true;
        }

        if(tripMenuOptionsHandler != null){
            boolean handled = tripMenuOptionsHandler.onMenuItemClicked(item);
            if(handled)
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(tripOperations.getMenuLayout(),menu);
            this.menu = menu;
            return true;
        } catch (Exception e){

        }
        return false;
    }

    private class ClearCurrentTripTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                TripUtils.getInstance().getCurrentTripOperations().clearAll();
                ServiceUtils.setUploadTripStatus(ServiceUtils.UPLOAD_TRIP_STATUS_INVALID);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            return null;
        }
    }
}
