package com.weareholidays.bia.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.trip.TripActivity;
import com.weareholidays.bia.adapters.SearchTripResultsAdapter;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class BeenThereActivity extends AppCompatActivity {

    private ListView tripView;
    private ArrayAdapter<Trip> tripsItemArrayAdapter;
    private List<Trip> mTrips;
    private int count;
    public Context mcontext;
    private ProgressBar spinner;

    private Trip selectedTrip;

    public BeenThereActivity(){
        mcontext = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_been_there);
        spinner = (ProgressBar) findViewById(R.id.progressBar3);
        spinner.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle("Been There");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ParseQuery.getQuery(Trip.class).whereNotEqualTo(Trip.DELETED, true).whereEqualTo(Trip.TRIP_OWNER, ParseUser.getCurrentUser())
                .whereEqualTo(Trip.UPLOADED,true)
                .include(Trip.DAYS).setLimit(20).findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> trips, ParseException e) {
                if (e == null) {
                    ListView tripListView = (ListView) findViewById(R.id.trips_list);
                    if (trips.size() > 0) {
                        if (tripsItemArrayAdapter != null) {
                            mTrips.clear();
                            mTrips.addAll(trips);
                            tripsItemArrayAdapter.notifyDataSetChanged();
                        } else {
                            mTrips = trips;
                            tripsItemArrayAdapter = new SearchTripResultsAdapter(getApplicationContext(), mTrips);
                            tripListView.setAdapter(tripsItemArrayAdapter);
                        }

                        tripListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                selectedTrip = mTrips.get(position);
                                new TripLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });

                    } else {
                        tripListView.setVisibility(View.GONE);
                        TextView text = (TextView) findViewById(R.id.no_trips);
                        text.setVisibility(View.VISIBLE);
                    }
                    spinner.setVisibility(View.GONE);
                } else {
                    Log.e("Been There", "Error getting trips", e);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
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
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class TripLoadTask extends AsyncTask<String,Void,Void> {

        @Override
        protected Void doInBackground(String... params) {
            if(selectedTrip != null)
            {
                Log.i("DiscoverFragment", "Trip selected: " + selectedTrip.getObjectId());
                TripUtils.getInstance().loadServerFullTrip(selectedTrip.getObjectId());
            }
            return null;
        }

        public void onPostExecute(Void result){
            if(selectedTrip != null){
                Intent intent = new Intent(BeenThereActivity.this, TripActivity.class);
                intent.putExtra(TripOperations.TRIP_KEY_ARG,selectedTrip.getObjectId());
                startActivity(intent);
            }
        }
    }
}
