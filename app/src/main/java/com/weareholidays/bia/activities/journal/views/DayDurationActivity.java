package com.weareholidays.bia.activities.journal.views;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.MyMapFragment;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.activities.journal.timeline.TimeLineFragment;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.reorderUtils.DayTimeLine;

public class DayDurationActivity extends TripBaseActivity implements DayDurationActivityFragment.OnFragmentInteractionListener,
        TimeLineFragment.OnFragmentInteractionListener, MyMapFragment.OnFragmentInteractionListener {

    int dayOrder;
    int itemSelected;

    @Override
    public void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        dayOrder = getIntent().getIntExtra("dayOrder",0);
        itemSelected = getIntent().getIntExtra("itemSelected",0);
        setContentView(R.layout.activity_day_duration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.cross);
        ImageButton imageButton = (ImageButton)toolbar.findViewById(R.id.close);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DAY " + (dayOrder + 1));
        String tripName = tripOperations.getTrip().getName();
        if(tripName.length()>21)
            tripName = tripName.substring(0,20)+"....";
        getSupportActionBar().setSubtitle(tripName);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_day_duration, menu);
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

    public int getDayOrder() {
        return dayOrder;
    }

    public int getItemSelected(){
        return itemSelected;
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

    }
}

