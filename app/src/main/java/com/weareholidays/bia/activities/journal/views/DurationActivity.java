package com.weareholidays.bia.activities.journal.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.adapters.DurationAdapter;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.List;

public class DurationActivity extends TripBaseActivity {

    private CenterProgressDialog progressDialog;
    private ArrayAdapter<Day> daysItemArrayAdapter;
    private List<Day> mdays;
    private Day selectedDay;

    @Override
    protected void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        setContentView(R.layout.activity_duration);
        tripOperations.getTrip().getDays();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trip Duration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onActivityCreated();
    }


    public void onActivityCreated() {
        //show the progress dialog
        progressDialog = CenterProgressDialog.show(this, "Loading", "Please wait...", true, false);
        //query for trips
        List<Day> days = tripOperations.getTrip().getDays();
        ListView dayListView = (ListView) findViewById(R.id.days_list);
        if (days.size() > 0) {
            if (daysItemArrayAdapter != null) {
                mdays.clear();
                mdays.addAll(days);
                daysItemArrayAdapter.notifyDataSetChanged();
                dayListView.setAdapter(daysItemArrayAdapter);
            } else {
                mdays = days;
                daysItemArrayAdapter = new DurationAdapter(this.getApplicationContext(), mdays);
                dayListView.setAdapter(daysItemArrayAdapter);
            }

        }
        dayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDay = mdays.get(position);
//                new DayLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Intent intent = new Intent(DurationActivity.this, DayDurationActivity.class);
                intent.putExtra("dayOrder",  position);
                intent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                startActivity(intent);
            }
        });
        progressDialog.dismiss();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_duration, menu);
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

//    private class DayLoadTask extends AsyncTask<String,Void,Void> {
//
//        @Override
//        protected Void doInBackground(String... params) {
//            if(selectedDay != null)
//            {
//                Log.i("DurationActivity", "Day selected: " + selectedDay.getObjectId());
//                TripUtils.getInstance().loadServerViewTrip(selectedDay.getObjectId());
//            }
//            return null;
//        }
//
//        public void onPostExecute(Void result){
//            if(selectedDay != null){
//                Intent intent = new Intent(getParent(),DayDurationActivity.class);
//                intent.putExtra(TripOperations.TRIP_KEY_ARG,selectedDay.getObjectId());
//                startActivity(intent);
//            }
//        }
//    }
}
