package com.weareholidays.bia.activities.journal.views;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.MyMapFragment;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.parse.utils.TripOperations;

public class DurationMapActivity extends TripBaseActivity implements MyMapFragment.OnFragmentInteractionListener {

    public static String SHOW_JOURNAL_DAY = "SHOW_JOURNAL_DAY";
    public static String TYPE = "TYPE";

    @Override
    public void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        setContentView(R.layout.activity_duration_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Duration");
        String tripName = tripOperations.getTrip().getName();
        if(tripName.length()>21)
            tripName = tripName.substring(0,20)+"....";
        getSupportActionBar().setSubtitle(tripName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MyMapFragment tripFragment = new MyMapFragment();
        Bundle args = new Bundle();
        args.putString(TripOperations.TRIP_KEY_ARG,tripOperations.getTripKey());
        args.putString("TYPE","DURATIONMAP");
        tripFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, tripFragment).commit();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_duration_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public TripOperations getTripOperations() {
        return tripOperations;
    }

}
