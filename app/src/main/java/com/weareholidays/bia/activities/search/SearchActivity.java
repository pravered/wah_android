package com.weareholidays.bia.activities.search;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.profile.UserProfileActivity;
import com.weareholidays.bia.adapters.SearchTripResultsAdapter;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.List;

import wahCustomViews.view.WahImageView;

public class SearchActivity extends AppCompatActivity {

    public static final String SEARCH_STRING = "SEARCH_STRING";
    public static final String TRIP_RESULT_COUNT = "TRIP_RESULT_COUNT";
    public static final String USER_RESULT_COUNT = "USER_RESULT_COUNT";
    private static final String TAG = "SEARCH_ACTIVITY";
    private static final int MAX_SHOW_ITEM = 5;

    private View loaderLayout;
    private View resultsLayout;
    private View tripResultsView;
    private View userResultsView;
    private LinearLayout tripsListLayout;
    private LinearLayout usersListLayout;
    private ProgressBar searchProgressBar;

    private SearchTask searchTask;
    private boolean loadingTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setup();
    }

    private void setup() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String searchText = getIntent().getStringExtra(SEARCH_STRING);
        final int tripResultCount = getIntent().getIntExtra(TRIP_RESULT_COUNT,0);
        final int userResultCount = getIntent().getIntExtra(USER_RESULT_COUNT,0);

        getSupportActionBar().setTitle(searchText);
        getSupportActionBar().setSubtitle((tripResultCount + userResultCount) + " RESULTS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loaderLayout = findViewById(R.id.loader_layout);
        resultsLayout = findViewById(R.id.results_layout);
        searchProgressBar = (ProgressBar) findViewById(R.id.searchProgressBar);

        tripResultsView = findViewById(R.id.trip_results);
        tripsListLayout = (LinearLayout) findViewById(R.id.trip_list_view);

        if(tripResultCount > 0){
            ((TextView)findViewById(R.id.trip_journals_header)).setText("TRIP JOURNALS (" + tripResultCount + ")");
        }
        else{
            tripResultsView.setVisibility(View.GONE);
        }

        userResultsView = findViewById(R.id.user_results);
        usersListLayout = (LinearLayout) findViewById(R.id.users_list_view);

        if(userResultCount > 0){
            ((TextView)findViewById(R.id.users_header)).setText("USERS (" + userResultCount + ")");
        }
        else{
            userResultsView.setVisibility(View.GONE);
        }

        ImageView trips_all_btn = (ImageView) findViewById(R.id.trips_view_all);
        ImageView users_all_btn = (ImageView) findViewById(R.id.users_view_all);

        if(tripResultCount > 0 && userResultCount > 0){
            findViewById(R.id.results_divider).setVisibility(View.VISIBLE);
        }

        if(tripResultCount<MAX_SHOW_ITEM){
            trips_all_btn.setVisibility(View.GONE);
        }
        if(userResultCount<MAX_SHOW_ITEM){
            users_all_btn.setVisibility(View.GONE);
        }

        searchTask = new SearchTask();

        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,searchText);

        trips_all_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, TripsListActivity.class);
                intent.putExtra(TripsListActivity.ITEM_TYPE, TripsListActivity.ITEM_TRIP);
                intent.putExtra(TripsListActivity.LIST_TYPE, TripsListActivity.SEARCH_TYPE);
                intent.putExtra(TripsListActivity.SEARCH_TEXT_KEY, searchText);
                intent.putExtra(TripsListActivity.TOTAL_COUNT, tripResultCount);
                startActivity(intent);
            }
        });

        users_all_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, TripsListActivity.class);
                intent.putExtra(TripsListActivity.ITEM_TYPE, TripsListActivity.ITEM_USER);
                intent.putExtra(TripsListActivity.LIST_TYPE, TripsListActivity.SEARCH_TYPE);
                intent.putExtra(TripsListActivity.SEARCH_TEXT_KEY, searchText);
                intent.putExtra(TripsListActivity.TOTAL_COUNT, userResultCount);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showTripsInList(List<Trip> trips){
        int index = 0;
        LayoutInflater inflater = getLayoutInflater();
        for(final Trip trip: trips){
            View convertView = inflater.inflate(R.layout.search_trip_row, tripsListLayout,false);
            WahImageView featureImage = (WahImageView) convertView.findViewById(R.id.search_trip_image);
            if(trip.getFeatureImage() != null){
               /* Glide.with(this)
                        .load(trip.getFeatureImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(featureImage);*/
                featureImage.setImageUrl(trip.getFeatureImage().getUrl());
            }
            /*else{
                Glide.with(this)
                        .load(R.drawable.trip_placeholder)
                        .into(featureImage);
            }*/

            WahImageView tripUserImage = (WahImageView) convertView.findViewById(R.id.trip_user_image);
            if (trip.getOwner().getProfileImage() != null) {
                /*Glide.with(this)
                        .load(trip.getOwner().getProfileImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(tripUserImage);*/
                tripUserImage.setImageUrl(trip.getOwner().getProfileImage().getUrl());
            }

            TextView tripName = (TextView) convertView.findViewById(R.id.search_trip_name);
            String tripN = trip.getName();
            if(tripN.length()>21)
                tripN = tripN.substring(0,20)+"....";
            tripName.setText(tripN);

            TextView tripDays = (TextView) convertView.findViewById(R.id.search_trip_days);
            String dayText = " Days";
            if (trip.getDays().size() == 1)
                dayText = " Day";
            tripDays.setText("\u2022 " + trip.getDays().size() + dayText);

            TextView tripDate = (TextView) convertView.findViewById(R.id.search_trip_date);
            tripDate.setText("\u2022 " + SearchTripResultsAdapter.simpleDateFormat.format(trip.getStartTime()));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTripClicked(trip);
                }
            });
            tripsListLayout.addView(convertView);
            if(++index < trips.size()){
                View dividerView = inflater.inflate(R.layout.padding_divider,tripsListLayout,false);
                tripsListLayout.addView(dividerView);
            }
        }
    }

    private void showUsersInList(List<ParseCustomUser> users){
        int index = 0;
        LayoutInflater inflater = getLayoutInflater();
        for(final ParseCustomUser user: users){
            View convertView = inflater.inflate(R.layout.search_user_row, usersListLayout,false);
            WahImageView userImage = (WahImageView) convertView.findViewById(R.id.user_image);
            if (user.getProfileImage() != null) {
               /* Glide.with(this)
                        .load(user.getProfileImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.default_user)
                        .crossFade()
                        .into(userImage);*/
                userImage.setImageUrl(user.getFeatureImage().getUrl());
            }
            TextView userName = (TextView) convertView.findViewById(R.id.user_name);
            userName.setText(user.getName());

            TextView userLocation = (TextView) convertView.findViewById(R.id.user_location);
            if (user.getPlace() != null) {
                if (user.getPlace().length() > 0)
                    userLocation.setText(user.getPlace());
            } else {
                userLocation.setVisibility(View.GONE);
            }

            TextView userTrips = (TextView) convertView.findViewById(R.id.user_trips);
            userTrips.setText(user.getTotalPublishedTrips() + " Trip Journals");
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUserClicked(user);
                }
            });
            usersListLayout.addView(convertView);
            if(++index < users.size()){
                View dividerView = inflater.inflate(R.layout.padding_divider,usersListLayout,false);
                usersListLayout.addView(dividerView);
            }
        }
    }

    private void onTripClicked(Trip trip){
        if(!loadingTrip){
            loadingTrip = true;
            new TripLoadTask(trip).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void onUserClicked(ParseCustomUser parsUser){
        TripUtils.setSelectedUser(parsUser);
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }

    private class SearchTask extends AsyncTask<String,Void,Void> {

        List<Trip> trips;

        List<ParseCustomUser> users;

        @Override
        protected void onPreExecute(){
            try{
                searchProgressBar.setVisibility(View.VISIBLE);
                loaderLayout.setVisibility(View.VISIBLE);
                resultsLayout.setVisibility(View.GONE);
            }
            catch (Exception e){
                DebugUtils.logException(e);
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            if(params != null && params.length > 0){
                String searchString = params[0];
                try {
                    trips = TripUtils.searchTrips(searchString).setLimit(MAX_SHOW_ITEM).find();
                } catch (Exception e) {
                    Log.e(TAG, "Error searching trips", e);
                }

                try {
                    users = TripUtils.searchUsers(searchString).setLimit(MAX_SHOW_ITEM).find();
                } catch (Exception e) {
                    Log.e(TAG,"Error searching users",e);
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result){
            if(isCancelled())
                return;
            try {
                if (trips != null && trips.size() > 0) {
                    showTripsInList(trips);
                } else {
                    tripResultsView.setVisibility(View.GONE);
                }

                if (users != null && users.size() > 0) {
                    showUsersInList(users);
                } else {
                    userResultsView.setVisibility(View.GONE);
                }
                loaderLayout.setVisibility(View.GONE);
                resultsLayout.setVisibility(View.VISIBLE);
            } catch (Exception e){
                DebugUtils.logException(e);
            }
        }
    }

    private class TripLoadTask extends AsyncTask<String,Void,Void> {

        private Trip selectedTrip;

        private CenterProgressDialog progressDialog;

        public TripLoadTask(Trip trip){
            this.selectedTrip = trip;
        }

        protected void onPreExecute(){
            progressDialog = CenterProgressDialog.show(SearchActivity.this, "Loading Trip...", null, true, false);
        }

        @Override
        protected Void doInBackground(String... params) {
            if(selectedTrip != null)
            {
                TripUtils.getInstance().loadServerViewTrip(selectedTrip.getObjectId());
            }
            return null;
        }

        public void onPostExecute(Void result){
            loadingTrip = false;
            try{
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
            } catch (Exception e){
                DebugUtils.logException(e);
            }
            if(selectedTrip != null){
                startActivity(NavigationUtils.getSearchTripIntent(SearchActivity.this,selectedTrip.getObjectId()));
            }
        }
    }
}
