package com.weareholidays.bia.activities.search;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.profile.UserProfileActivity;
import com.weareholidays.bia.adapters.DiscoverPagerAdapter;
import com.weareholidays.bia.adapters.DiscoverTripsAdapter;
import com.weareholidays.bia.listeners.EndlessScrollListener;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment implements DiscoverPagerAdapter.OnInteractionListener {

    private TextView noTripsText;
    private ProgressBar spinner;
    private View tripsLoaderLayout;
    private RelativeLayout actualLayout;
    private LinearLayout noInternet;

    private OnFragmentInteractionListener mListener;
    private boolean loadingTrip = false;

    private static final String TAG = "DISCOVER_FRAGMENT";
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private EndlessScrollListener scrollListener;
    private volatile boolean loading = false;
    private volatile boolean loading_done = false;
    private volatile int page;
    private List<Trip> tripsList;
    private ListView tripsListView;
    private ArrayAdapter<Trip> tripsItemArrayAdapter;
    private int listType;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        listType = DiscoverOuterFragment.POPULAR_TAB;
        Bundle args = getArguments();
        if (args != null) {
            listType = getArguments().getInt("listType", DiscoverOuterFragment.POPULAR_TAB);
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);

        // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);
        // Inflate this layout
        return rootView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actualLayout = (RelativeLayout) view.findViewById(R.id.with_internet);
        noInternet = (LinearLayout) view.findViewById(R.id.no_internet);

        if (ViewUtils.isNetworkAvailable(getActivity())) {
            setup(view);
        } else {
            actualLayout.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
            //set retry button functionality
            LinearLayout retryButton = (LinearLayout) view.findViewById(R.id.retry_button);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ViewUtils.isNetworkAvailable(getActivity())) {
                        actualLayout.setVisibility(View.VISIBLE);
                        noInternet.setVisibility(View.GONE);
                        setup(view);
                    }
                }
            });
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });
    }

    private void initiateRefresh() {
        new RefreshTripsLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);
    }

    private void setup(View rootView) {
        tripsListView = (ListView) rootView.findViewById(R.id.trips_list);
        noTripsText = (TextView) rootView.findViewById(R.id.no_trips);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar2);
        tripsLoaderLayout = rootView.findViewById(R.id.trips_loader_layout);

        tripsList = new ArrayList<>();
        tripsItemArrayAdapter = new DiscoverTripsAdapter(getActivity(), tripsList);
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
        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                setPage(page);
                if (!loading && !loading_done) {
                    new TripsLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);
                    return true;
                }
                return false;
            }
        };
        tripsListView.setOnScrollListener(scrollListener);
        new TripsLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);

    }

    public void startSearchBridgeActivity() {
        Intent i = new Intent(getActivity(), SearchBridgeActivity.class);
        startActivity(i);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClicked(Trip trip) {
        if (!loadingTrip) {
            loadingTrip = true;
            new TripLoadTask(trip).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

    }

    private class TripsLoadTask extends AsyncTask<Integer, Void, Void> {

        List<Trip> trips;
        int limit = 20;

        @Override
        protected void onPreExecute() {
            loading = true;
            try {
                spinner.setVisibility(View.VISIBLE);
                tripsLoaderLayout.setVisibility(View.VISIBLE);
                noTripsText.setVisibility(View.GONE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int skip_items = (getPage() - 1) * limit;
            try {
                switch (listType) {
                    case DiscoverOuterFragment.PUBLISHED_TAB:
                        trips = TripUtils.getTripsRecentlyPublished().setLimit(limit).setSkip(skip_items).find();
                        break;
                    case DiscoverOuterFragment.TRAVELLED_TAB:
                        trips = TripUtils.getTripsRecentlyTravelled().setLimit(limit).setSkip(skip_items).find();
                        break;
                    default:
                        trips = TripUtils.getTripsPopular().setLimit(limit).setSkip(skip_items).find();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching trips", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            loading = false;
            if (isCancelled())
                return;
            try {
                if ((trips.size() == 0 && page > 0) || (trips.size() > 0)) {
                    tripsList.addAll(trips);
                    tripsItemArrayAdapter.notifyDataSetChanged();
                    tripsLoaderLayout.setVisibility(View.GONE);
                    if (trips.size() < limit)
                        loading_done = true;
                } else {
                    tripsLoaderLayout.setVisibility(View.VISIBLE);
                    noTripsText.setVisibility(View.VISIBLE);
                }
                spinner.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }
    }

    private class RefreshTripsLoadTask extends AsyncTask<Integer, Void, Void> {

        List<Trip> trips;
        int limit = 20;

        @Override
        protected void onPreExecute() {
            loading = true;
            try {
                noTripsText.setVisibility(View.GONE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                switch (listType) {
                    case DiscoverOuterFragment.PUBLISHED_TAB:
                        trips = TripUtils.getTripsRecentlyPublished().setLimit(limit).find();
                        break;
                    case DiscoverOuterFragment.TRAVELLED_TAB:
                        trips = TripUtils.getTripsRecentlyTravelled().setLimit(limit).find();
                        break;
                    default:
                        trips = TripUtils.getTripsPopular().setLimit(limit).find();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching trips", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            loading = false;
            if (isCancelled())
                return;
            try {
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
                tripsList.clear();
                if ((trips != null && trips.size() > 0)) {
                    tripsList.addAll(trips);
                    tripsItemArrayAdapter.notifyDataSetChanged();
                    if (trips.size() < limit)
                        loading_done = true;
                } else {
                    noTripsText.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }
    }

    private void onUserClicked(ParseCustomUser user) {
        TripUtils.setSelectedUser(user);
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        startActivity(intent);
    }

    private class TripLoadTask extends AsyncTask<String, Void, Void> {

        private Trip selectedTrip;

        private CenterProgressDialog progressDialog;

        public TripLoadTask(Trip trip) {
            this.selectedTrip = trip;
        }

        protected void onPreExecute() {
            progressDialog = CenterProgressDialog.show(getActivity(), "Loading Trip...", null, true, false);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (selectedTrip != null) {
                TripUtils.getInstance().loadServerViewTrip(selectedTrip.getObjectId());
            }
            return null;
        }

        public void onPostExecute(Void result) {
            loadingTrip = false;
            try {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                DebugUtils.logException(e);
            }

            if (selectedTrip != null) {
                if (selectedTrip.isPublished()) {
                    startActivity(NavigationUtils.getDiscoverTripIntent(getActivity(), selectedTrip.getObjectId()));
                } else {
                    Toast.makeText(getActivity(), "Selected trip does not exist. Kindly refresh", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
