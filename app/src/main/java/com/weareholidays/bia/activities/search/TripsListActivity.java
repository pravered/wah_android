package com.weareholidays.bia.activities.search;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.weareholidays.bia.activities.profile.UserProfileActivity;
import com.weareholidays.bia.adapters.SearchTripResultsAdapter;
import com.weareholidays.bia.adapters.SearchUserResultsAdapter;
import com.weareholidays.bia.listeners.EndlessScrollListener;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class TripsListActivity extends AppCompatActivity {

    public static final String TAG = "TRIPS_LIST";

    public static String LIST_TYPE = "LIST_TYPE";
    public static String ITEM_TYPE = "ITEM_TYPE";
    public static int ITEM_TRIP = 1;
    public static int ITEM_USER = 2;
    public static int SEARCH_TYPE = 1;
    public static int DISCOVER_TYPE = 2;

    public static String SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY";
    public static String TOTAL_COUNT = "TOTAL_COUNT";

    private View placeHolderView;
    private View listRootView;
    private ListView tripsListView;
    private ProgressBar progressBar;
    private TextView noResultsText;
    private ArrayAdapter<Trip> tripsItemArrayAdapter;
    private ArrayAdapter<ParseCustomUser> usersItemArrayAdapter;

    private int listType;
    private int itemType;
    private String searchText;

    private List<Trip> tripsList;
    private List<ParseCustomUser> usersList;

    private EndlessScrollListener scrollListener;
    private volatile boolean loading = false;
    private volatile boolean loading_done = false;
    private volatile int page;
    private TextView heading;
//    private volatile int totalCount = 0;
    private boolean loadingTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_list);
        setup();
    }


    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

//    public int getTotalCount() {
//        return totalCount;
//    }
//
//    public void setTotalCount(int totalCount) {
//        this.totalCount = totalCount;
//    }

    private void setup() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listType = getIntent().getIntExtra(LIST_TYPE,DISCOVER_TYPE);
        itemType = getIntent().getIntExtra(ITEM_TYPE,ITEM_TRIP);

        if(listType == SEARCH_TYPE){
            String searchText = getIntent().getStringExtra(SEARCH_TEXT_KEY);
            int resultsCount = getIntent().getIntExtra(TOTAL_COUNT,0);
            getSupportActionBar().setTitle(searchText);
            getSupportActionBar().setSubtitle("" + resultsCount + " RESULTS");
            this.searchText = searchText;
        }

        if(listType == DISCOVER_TYPE){
            if(itemType == ITEM_TRIP)
                getSupportActionBar().setTitle("Featured Trips");
            else
                getSupportActionBar().setTitle(getResources().getString(R.string.title_feature_user));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        heading = (TextView) findViewById(R.id.heading);
        noResultsText = (TextView) findViewById(R.id.no_trips);
        placeHolderView = findViewById(R.id.placeholder_root);
        listRootView = findViewById(R.id.trips_list_root);
        tripsListView = (ListView) findViewById(R.id.trips_list);

        if(itemType == ITEM_USER){
            noResultsText.setText(getResources().getString(R.string.no_users_text));
            heading.setText(getResources().getString(R.string.user_list));
            usersList = new ArrayList<>();
            usersItemArrayAdapter = new SearchUserResultsAdapter(this,usersList);
            tripsListView.setAdapter(usersItemArrayAdapter);
            tripsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ParseCustomUser parseCustomUser = usersList.get(position);
                    TripUtils.setSelectedUser(parseCustomUser);
                    Intent intent = new Intent(TripsListActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                }
            });
        }
        else if(itemType == ITEM_TRIP){
            tripsList = new ArrayList<>();
            tripsItemArrayAdapter = new SearchTripResultsAdapter(this, tripsList);
            tripsListView.setAdapter(tripsItemArrayAdapter);
            tripsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!loadingTrip) {
                        loadingTrip = true;
                        Trip trip = tripsList.get(position);
                        new TripLoadTask(trip).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            });
        }

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                setPage(page);
//                setTotalCount(totalItemsCount);
                if (!loading && !loading_done) {
                    new TripsLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,page);
                    return true;
                }
                return false;
            }
        };
        tripsListView.setOnScrollListener(scrollListener);
        new TripsLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trips_list, menu);
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

    private class TripsLoadTask extends AsyncTask<Integer,Void,Void> {

        List<Trip> trips;
        List<ParseCustomUser> users;
        int limit = 20;

        @Override
        protected void onPreExecute(){
            loading = true;
            try{
                progressBar.setVisibility(View.VISIBLE);
                placeHolderView.setVisibility(View.VISIBLE);
                listRootView.setVisibility(View.GONE);
                noResultsText.setVisibility(View.GONE);
            }
            catch (Exception e){
                DebugUtils.logException(e);
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int skip_items = (getPage()-1)*limit;
            if(itemType == ITEM_TRIP){
                if(listType == DISCOVER_TYPE){
                    try {
                        trips = TripUtils.getFeaturedTripsViewAll().setLimit(limit).setSkip(skip_items).find();
                    } catch (Exception e) {
                        Log.e(TAG, "Error searching trips", e);
                    }
                }
                else{
                    try {
                        trips = TripUtils.searchTrips(searchText).setLimit(limit).setSkip(skip_items).find();
                    } catch (Exception e) {
                        Log.e(TAG, "Error searching trips", e);
                    }
                }
            }
            if(itemType == ITEM_USER){
                if(listType == DISCOVER_TYPE){
                    try {
                        users = TripUtils.getFeaturedUsersViewAll().setLimit(limit).setSkip(skip_items).find();
                    } catch (Exception e) {
                        Log.e(TAG,"Error searching users",e);
                    }
                }
                else{
                    try {
                        users = TripUtils.searchUsers(searchText).setLimit(limit).setSkip(skip_items).find();
                    } catch (Exception e) {
                        Log.e(TAG,"Error searching users",e);
                    }
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result){
            loading = false;
            if(isCancelled())
                return;
            try {
                if(itemType == ITEM_USER){
                    if((users.size() == 0 && page>0)||(users.size()>0)){
                        usersList.addAll(users);
                        usersItemArrayAdapter.notifyDataSetChanged();
                        placeHolderView.setVisibility(View.GONE);
                        listRootView.setVisibility(View.VISIBLE);
                        if(users.size()<limit)
                            loading_done=true;
                    }else{
                        placeHolderView.setVisibility(View.VISIBLE);
                        listRootView.setVisibility(View.GONE);
                        noResultsText.setVisibility(View.VISIBLE);
                    }
                }
                else if(itemType == ITEM_TRIP){
                    if((trips.size() == 0 && page>0)||(trips.size() > 0)){
                        tripsList.addAll(trips);
                        tripsItemArrayAdapter.notifyDataSetChanged();
                        placeHolderView.setVisibility(View.GONE);
                        listRootView.setVisibility(View.VISIBLE);
                        if(trips.size()<limit)
                            loading_done = true;
                    }
                    else{
                        placeHolderView.setVisibility(View.VISIBLE);
                        listRootView.setVisibility(View.GONE);
                        noResultsText.setVisibility(View.VISIBLE);
                    }
                }
                progressBar.setVisibility(View.GONE);
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
            progressDialog = CenterProgressDialog.show(TripsListActivity.this, "Loading Trip...", null, true, false);
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
                startActivity(NavigationUtils.getViewAllTripIntent(TripsListActivity.this,selectedTrip.getObjectId()));
            }
        }
    }
}
