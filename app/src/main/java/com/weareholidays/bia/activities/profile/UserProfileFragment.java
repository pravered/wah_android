package com.weareholidays.bia.activities.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.trip.TripActivity;
import com.weareholidays.bia.activities.login.PasswordActivity;
import com.weareholidays.bia.activities.login.SignUpActivity;
import com.weareholidays.bia.adapters.TripRecyclerAdapter;
import com.weareholidays.bia.listeners.EndlessRecyclerOnScrollListener;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.ArrayList;
import java.util.List;

import wahCustomViews.view.WahImageView;

public class UserProfileFragment extends Fragment implements TripRecyclerAdapter.OnItemInteraction {

    public static final String TAG = "TRIPS_LIST";

    public static final String USER_PUBLIC = "USER_PUBLIC";
    private static final int SELECT_COVER_PICTURE = 11;

    private WahImageView imageProfile;
    private WahImageView tripwall;
    private TextView userName;
    private TextView userLocation;
    private TextView tripCount;
    private RecyclerView tripsListView;
    private TextView noTripsText;
    private TripRecyclerAdapter tripRecyclerAdapter;
    private ParseCustomUser currentUser;
    private Toolbar userToolbar;
    private View tripWallMask;

    private int tripLimit = 5;

    private List<Trip> tripsList;

    private CenterProgressDialog progressDialog;
    private EndlessRecyclerOnScrollListener scrollListener;
    private volatile boolean loading = false;
    private volatile boolean loading_done = false;
    private int skip = 0;
    private volatile boolean loading_trip = false;
    private boolean currentuser = true;

    private OnFragmentInteractionListener mListener;
    private CoordinatorLayout actualLayout;
    private LinearLayout noInternet;

    public static UserProfileFragment newInstance(boolean currentuser) {
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        Bundle bundle = new Bundle();
        if(!currentuser)
            bundle.putString(USER_PUBLIC,"YES");
        userProfileFragment.setArguments(bundle);
        return userProfileFragment;
    }

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            String publicUser = getArguments().getString(USER_PUBLIC);
            if(!TextUtils.isEmpty(publicUser))
                currentuser = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, null, false);
	    userToolbar = (Toolbar) v.findViewById(R.id.user_tool_bar);
        if(!currentuser)
            mListener.setSupportToolbarInTripFragment(userToolbar);

        //menuButton.setOnClickListener(new UserProfileFragment.OverflowMenuClickListener());
        return v;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actualLayout = (CoordinatorLayout) view.findViewById(R.id.with_internet);
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
    }


    @Override
    public void onItemClicked(Trip selectedTrip) {
        if(!loading_trip){
            loading_trip = true;
            new TripLoadTask(selectedTrip).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void setup(View v) {

        tripwall = (WahImageView) v.findViewById(R.id.trip_wall);
        tripWallMask = v.findViewById(R.id.trip_wall_mask);
        noTripsText = (TextView) v.findViewById(R.id.no_trips);
        tripsListView = (RecyclerView) v.findViewById(R.id.trips_list);
        imageProfile = (WahImageView) v.findViewById(R.id.image_profile);
        userName = (TextView) v.findViewById(R.id.user_name);
        userLocation = (TextView) v.findViewById(R.id.user_location);
        tripCount = (TextView) v.findViewById(R.id.trip_count);
        if(currentuser)
            currentUser = (ParseCustomUser) ParseUser.getCurrentUser();
        else
            currentUser = TripUtils.getSelectedUser();

        userName.setText(currentUser.getName());
        if(TextUtils.isEmpty(currentUser.getPlace())){
            userLocation.setVisibility(View.GONE);
        }
        else{
            userLocation.setVisibility(View.VISIBLE);
            userLocation.setText(currentUser.getPlace());
        }

        // @TODO: @vijay: in the list below, we show 'uploaded trips' but here we show
        // total/published trips, which creates visual discrepancy
        if(currentuser)
            tripCount.setText("" + currentUser.getTotalTrips());
        else
            tripCount.setText("" + currentUser.getTotalPublishedTrips());

        try {
            FileLocal fileLocal = ParseFileUtils.getLocalFileFromPin(currentUser);
            if (fileLocal != null) {
                imageProfile.setImageUrl(fileLocal.getLocalUri());
                /*Glide.with(this)
                        .load(fileLocal.getLocalUri())
                        .asBitmap()
                        .placeholder(R.drawable.user_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new BitmapImageViewTarget(imageProfile) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                Log.i("Profile", "Profile Picture Loaded");
                                super.setResource(resource);
                            }
                        });*/
            } else {
                ParseFile userProfileImg = currentUser.getProfileImage();
                if (userProfileImg != null) {
                imageProfile.setImageUrl(userProfileImg.getUrl());
                /*
                    Glide.with(this)
                            .load(userProfileImg.getUrl())
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .placeholder(R.drawable.user_placeholder)
                            .into(new BitmapImageViewTarget(imageProfile) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    Log.i("Profile", "Profile Picture Loaded");
                                    super.setResource(resource);
                                }
                            });*/
                }
            }
        } catch (Exception e) {
            Log.e("UserProfile", "Error showing picture", e);
        }
        setFeatureImage();
        tripsList = new ArrayList<>();
        new TripsLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        tripsListView.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager,tripLimit) {

            @Override
            public void onLoadMore(int current_page) {
                skip = current_page * tripLimit;
                if (!loading && !loading_done) {
                    new TripsLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        };

        tripsListView.addOnScrollListener(scrollListener);
        tripRecyclerAdapter = new TripRecyclerAdapter(getActivity(),tripsList,this,this);
        tripsListView.setAdapter(tripRecyclerAdapter);
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)v.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                collapsingToolbar.setMinimumHeight(0);
            }
        });

        userToolbar = (Toolbar) v.findViewById(R.id.user_tool_bar);
        if(currentuser){
            if(userToolbar.getMenu() != null)
                userToolbar.getMenu().clear();
            if(currentUser.getUsername().equals(currentUser.getEmail()))
                userToolbar.inflateMenu(R.menu.menu_email_user);
            else
                userToolbar.inflateMenu(R.menu.menu_profile);
            userToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

//                    if (id == R.id.edit_cover_photo) {
//                        Intent intent = new Intent();
//                        intent.setType("image/*");
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
//                        startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_COVER_PICTURE);
//                    }

                    if(id == R.id.edit_profile){
                        Intent eIntent = new Intent(getActivity(), EditProfileActivity.class);
                        startActivity(eIntent);
                        return true;
                    }

                    if(id == R.id.change_password){
                        Intent intent = new Intent(getActivity(), PasswordActivity.class);
                        startActivity(intent);
                        return true;
                    }

                    if(id== R.id.logout){
                        if (!TripUtils.getInstance().getCurrentTripOperations().isTripAvailable()) {
                            progressDialog = CenterProgressDialog.show(getActivity(), "Logging Out", null, true);
                            ParseCustomUser.logOut();
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put("username", "");
                            installation.saveInBackground();
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "You have been logged out", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), SignUpActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            //do nothing here
                            //v.setEnabled(false);
                            Trip trip = TripUtils.getInstance().getCurrentTripOperations().getTrip();
                            if(trip.isFinished() && !trip.isUploaded())
                                Toast.makeText(getActivity(), R.string.logout_warning_upload_message, Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getActivity(), "You have not finished your trip!", Toast.LENGTH_LONG).show();
                        }
                    }

                    return false;
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_COVER_PICTURE) {
            if (data.getData() != null && ViewUtils.isNetworkAvailable(getActivity())) {
                progressDialog = CenterProgressDialog.show(getActivity(), null, null, true, false);
                final ParseFile file = new ParseFile("picture_cover.jpeg", ParseFileUtils.convertImageToBytes(data.getData(), getActivity()));
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException ex) {
                        if (ex == null) {
                            currentUser.put(ParseCustomUser.FEATURE_IMAGE, file);
                            currentUser.setFeatureImage(file);
                            currentUser.saveEventually();
                        }
                        if (progressDialog != null)
                            try {
                                progressDialog.dismiss();
                            } catch (Exception e){
                                DebugUtils.logException(e);
                            }
                    }

                });
                setFeatureImage();

            } else {
                Toast.makeText(getActivity(), "No working internet connection found!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setFeatureImage() {
        if (currentUser.getFeatureImage() != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,tripWallMask.getLayoutParams().height);
            params.setMargins(0,0,0,0);
            tripwall.setLayoutParams(params);
            tripwall.setImageUrl(currentUser.getFeatureImage().getUrl());
           /* Glide.with(this)
                    .load(currentUser.getFeatureImage().getUrl())
                    .centerCrop()
                    .crossFade()
                    .into(tripwall);*/
        }
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

    public interface OnFragmentInteractionListener {
        void setSupportToolbarInTripFragment(Toolbar toolbar);
    }

    private class TripsLoadTask extends AsyncTask<Integer,Void,Void> {

        List<Trip> trips;

        @Override
        protected void onPreExecute(){
            loading = true;
            try{
                noTripsText.setVisibility(View.GONE);
            }
            catch (Exception e){
                DebugUtils.logException(e);
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {
            Log.i("SKIP_ITEMS",skip + "");
            try {
                if(currentuser)
                    trips = TripUtils.getCurrentUserTripsViewAll().setLimit(tripLimit).setSkip(skip).find();
                else
                    trips = TripUtils.getUserPublishedTripsViewAll(currentUser).setSkip(skip).find();
            } catch (Exception e) {
                Log.e(TAG, "Error searching trips", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result){
            loading = false;
            if(isCancelled())
                return;
            try {
                if((trips.size() == 0 && skip > 0) || trips.size() > 0){
                    tripsList.addAll(trips);
                    //tripsItemArrayAdapter.notifyDataSetChanged();
                    tripRecyclerAdapter.notifyDataSetChanged();
                    if(trips.size() < tripLimit)
                        loading_done = true;
                }
                else{
                    noTripsText.setVisibility(View.GONE);
                }
            } catch (Exception e){
                DebugUtils.logException(e);
            }
        }
    }

    private class TripLoadTask extends AsyncTask<String,Void,Void> {

        private Trip selectedTrip;

        public TripLoadTask(Trip trip){
            this.selectedTrip = trip;
        }

        protected void onPreExecute(){
            progressDialog = CenterProgressDialog.show(getActivity(), "Loading Trip...", null, true, false);
        }

        @Override
        protected Void doInBackground(String... params) {
            if(selectedTrip != null)
            {
                if(currentuser)
                    TripUtils.getInstance().loadServerFullTrip(selectedTrip.getObjectId());
                else
                    TripUtils.getInstance().loadServerViewTrip(selectedTrip.getObjectId());
            }
            return null;
        }

        public void onPostExecute(Void result){
            loading_trip = false;
            if(selectedTrip != null){
                Intent intent = new Intent(getActivity(), TripActivity.class);
                intent.putExtra(TripOperations.TRIP_KEY_ARG,selectedTrip.getObjectId());
                startActivity(intent);
            }

            try{
                if(progressDialog != null){
                    progressDialog.dismiss();
                }
            } catch (Exception e){
                DebugUtils.logException(e);
            }
        }
    }
}
