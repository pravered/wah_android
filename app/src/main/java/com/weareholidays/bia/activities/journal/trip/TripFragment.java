package com.weareholidays.bia.activities.journal.trip;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.RouteActivity;
import com.weareholidays.bia.activities.journal.actions.CheckInActivity;
import com.weareholidays.bia.activities.journal.actions.NoteActivity;
import com.weareholidays.bia.activities.journal.base.TripOperationsLoader;
import com.weareholidays.bia.activities.journal.people.AddPeopleActivity;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.activities.journal.photo.PhotoGalleryActivity;
import com.weareholidays.bia.activities.journal.timeline.DeleteActivity;
import com.weareholidays.bia.activities.journal.timeline.ReorderActivity;
import com.weareholidays.bia.activities.journal.timeline.TimeLineFragment;
import com.weareholidays.bia.activities.journal.timeline.TripSummaryFragment;
import com.weareholidays.bia.activities.profile.FeedbackActivity;
import com.weareholidays.bia.adapters.SearchTripResultsAdapter;
import com.weareholidays.bia.adapters.WAHRecyclerAdapter;
import com.weareholidays.bia.background.receivers.TripServiceManager;
import com.weareholidays.bia.background.receivers.TripServiceStopManager;
import com.weareholidays.bia.background.receivers.UploadTripReceiver;
import com.weareholidays.bia.background.services.ServiceUtils;
import com.weareholidays.bia.background.services.UploadTripService;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.models.Coupon;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripAsyncCallback;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.weareholidays.bia.utils.BiaAppAPI;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.utils.Utils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//import twitter4j.auth.AccessToken;
import wahCustomViews.view.CircularProgressBar;
import wahCustomViews.view.WahImageView;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_EMPTYSTATE_DAY_PREF;
import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_EMPTYSTATE_SUMMARY_PREF;
import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_TO_PUBLISHED_TRIP;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TripFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripFragment extends Fragment implements WAHRecyclerAdapter.OnInteractionListener, View.OnClickListener {

    private static final String TAG = "TripFragment";
    public static String SHOW_JOURNAL_DAY = "SHOW_JOURNAL_DAY";
    public static String TIMELINE_SCROLL_POSITION = "TIMELINE_SCROLL_POSITION";
    public static String SHOW_FINISH_TRIP_LAYOUT = "SHOW_FINISH_TRIP_LAYOUT";

    public static int JOURNAL_SUMMARY_VIEW = 0;

    public static int CHANGE_FEATURE_IMAGE_REQUEST_CODE = 868;

    public static int UPDATED_POSTS_REQUEST_CODE = 3312;

    private static final int EDIT_PEOPLE = 22;

    private static final int SHARE_PUBLISHED_TRIP = 323;

    private OnFragmentInteractionListener mListener;

    private WahImageView featureImage;

    private ViewPager tripViewPager;

    private TabLayout tripTabs;

    private Toolbar tripToolbar;

    private CollapsingToolbarLayout collapsingToolbar;

    private AppBarLayout appBar;

    private TextView tripNameText;

    private View floatingMenu;

    private int selectedDayOrder = -1;

    private int defaultTab = JOURNAL_SUMMARY_VIEW;

    private OffsetChangeListener offsetChangeListener;

    private CenterProgressDialog progressDialog;

    private Trip currentTrip;

    private Button publishTripBtn, syncTripBtn, finishTripBtn, fragPublishTripButton, mErrorTryAgainButton, sharePublishedTrip;

    private MaterialDialog uploadTripWait;

    private TripOperations tripOperations;

    private Trip trip;

    private FrameLayout selectedPeoplePics;

    private RecyclerView rView;

    private WAHRecyclerAdapter peopleAdapter;

    private LinearLayout people;

    private List<PeopleContact> addedPeople;

    private View rootView;

    private TripUpdateReceiver tripUpdateReceiver;

    private ImageView backImage;

    private WAHRecyclerAdapter.OnInteractionListener listener;

    private TripMenuOptionsHandler tripMenuOptionsHandler;

    // private View trip_done_layout;

    private View trip_status_layout;

    //  private TextView trip_done_text;

    private TextView trip_date_text;

    private int scrollPosition = -1;

    private TextView textView;

    private ImageView tripStatusIcon;

    private ShowcaseView showcaseView;
    private TextView tripStatusText, skipFinishTrip, loaderText, loaderText2, skipErrorPage, reportIssue, skipPublish, skipeShare;

    private CoordinatorLayout mainLayout;

    private RelativeLayout loaderLayout;

    CircularProgressBar circularProgressBar;

    View publishTripLayout, finishTripLayout, errorLayout, publishSuccessLayout;

    private CheckBox facebook;

    private View tripWallMask;

    AccessTokenTracker mAccessTokenTracker;

    private boolean isShowFinishTripLayout = false;

    private String couponId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TripFragment.
     */
    public static TripFragment newInstance() {
        TripFragment fragment = new TripFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        offsetChangeListener = new OffsetChangeListener();
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                defaultTab = getArguments().getInt(SHOW_JOURNAL_DAY, JOURNAL_SUMMARY_VIEW);
                scrollPosition = getArguments().getInt(TIMELINE_SCROLL_POSITION, -1);
                isShowFinishTripLayout = getArguments().getBoolean(SHOW_FINISH_TRIP_LAYOUT);
                couponId = getArguments().getString(TripStartActivity.COUPON_ID, "");
                if (couponId != null && !TextUtils.isEmpty(couponId)) {
                    applyCoupon(couponId);
                }
            }
        } else {
            defaultTab = savedInstanceState.getInt(SHOW_JOURNAL_DAY, JOURNAL_SUMMARY_VIEW);
        }
        trip = tripOperations.getTrip();

        if (defaultTab > trip.getDays().size())
            defaultTab = trip.getDays().size();

        if (tripOperations.listenForUpdates())
            tripUpdateReceiver = new TripUpdateReceiver();
        listener = this;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SHOW_JOURNAL_DAY, selectedDayOrder);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.trip_fragment_collapse, container, false);
        setup(rootView);
        if (isShowFinishTripLayout) {
            setFinishTripLayoutVisible();
        }

        if (!TripOperations.CURRENT_TRIP_ID.equals(tripOperations.getTripKey())) {
            mListener.setSupportToolbarInTripFragment(tripToolbar, tripMenuOptionsHandler);
        }

        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (oldAccessToken.getPermissions().size() > currentAccessToken.getPermissions().size()) {
                    AccessToken.setCurrentAccessToken(oldAccessToken);
                }
            }
        };

        return rootView;
    }

    private void setup(View v) {
        currentTrip = trip;

        mainLayout = (CoordinatorLayout) v.findViewById(R.id.main_content);
        finishTripLayout = v.findViewById(R.id.finish_trip_layout);
        loaderLayout = (RelativeLayout) v.findViewById(R.id.loader_layout);
        publishTripLayout = v.findViewById(R.id.publish_trip_layout);
        errorLayout = v.findViewById(R.id.error_fragment);
        publishSuccessLayout = v.findViewById(R.id.publish_sucess);

        tripViewPager = (ViewPager) v.findViewById(R.id.trip_pager);

        tripWallMask = v.findViewById(R.id.trip_wall_mask);

        featureImage = (WahImageView) v.findViewById(R.id.trip_wall);

        publishTripBtn = (Button) v.findViewById(R.id.publish_trip_btn);

        syncTripBtn = (Button) v.findViewById(R.id.sync_trip_btn);

        circularProgressBar = (CircularProgressBar) v.findViewById(R.id.circular_progress_bar);

        loaderText = (TextView) v.findViewById(R.id.loader_text);

        loaderText2 = (TextView) v.findViewById(R.id.loader_text2);

        fragPublishTripButton = (Button) publishTripLayout.findViewById(R.id.publish_trip_btn);

        facebook = (CheckBox) publishTripLayout.findViewById(R.id.checkbox_fb);

        skipPublish = (TextView) publishTripLayout.findViewById(R.id.skip_publish);

        skipFinishTrip = (TextView) finishTripLayout.findViewById(R.id.skip_finish);

        finishTripBtn = (Button) finishTripLayout.findViewById(R.id.finish_trip_btn);

        mErrorTryAgainButton = (Button) errorLayout.findViewById(R.id.retry_trip_btn);

        skipErrorPage = (TextView) errorLayout.findViewById(R.id.skip_upload);

        reportIssue = (TextView) errorLayout.findViewById(R.id.report_error);

        sharePublishedTrip = (Button) publishSuccessLayout.findViewById(R.id.share_trip_btn);

        skipeShare = (TextView) publishSuccessLayout.findViewById(R.id.skip_share);

        if (tripOperations.canWrite() && currentTrip.isUploaded() && !currentTrip.isPublished()) {
            publishTripBtn.setVisibility(View.VISIBLE);
            drawPublishCoachMarks();
            tripViewPager.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        } else {
            publishTripBtn.setVisibility(View.GONE);
        }

        if (tripOperations.getTrip().isFinished() && !tripOperations.getTrip().isUploaded()) {
            syncTripBtn.setVisibility(View.VISIBLE);
            tripViewPager.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        } else {
            syncTripBtn.setVisibility(View.GONE);
        }
        textView = (TextView) v.findViewById(R.id.trip_name);

        String tripName = currentTrip.getName();
        textView.setText(tripName);

        trip_date_text = (TextView) v.findViewById(R.id.trip_date_text);

        Date tripPublishTime = currentTrip.getPublishTime();
        if (tripPublishTime != null) {
            trip_date_text.setText("Published on " + SearchTripResultsAdapter.simpleDateFormat.format(tripPublishTime));
        }

        people = (LinearLayout) v.findViewById(R.id.people);

        /*
        Not needed : added option in overflow menu
        trip_done_layout = v.findViewById(R.id.trip_done_btn);

        trip_done_text = (TextView)v.findViewById(R.id.trip_done_text);

        trip_done_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTripDoneClicked();
            }
        });
*/
        trip_status_layout = v.findViewById(R.id.trip_status_layout);
        selectedPeoplePics = (FrameLayout) v.findViewById(R.id.selected_people_pics);
        rView = (RecyclerView) v.findViewById(R.id.recycler_view);
        rView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rView.setVisibility(View.GONE);
        addedPeople = new ArrayList<>();

        tripOperations.getTripPeople(new TripAsyncCallback<List<PeopleContact>>() {
            @Override
            public void onCallBack(List<PeopleContact> result) {
                try {
                    addedPeople = result;
                    WahImageView first_contact_image = (WahImageView) selectedPeoplePics.findViewById(R.id.first_contact_image);
                    WahImageView second_contact_image = (WahImageView) selectedPeoplePics.findViewById(R.id.second_contact_image);
                    WahImageView third_contact_image = (WahImageView) selectedPeoplePics.findViewById(R.id.third_contact_image);
                    if (addedPeople.size() > 0) {
                        selectedPeoplePics.setVisibility(View.VISIBLE);
                        //getting people with images
                        int count = 0;
                        List<PeopleContact> peopleWithImage = new ArrayList<>();
                        for (int i = 0; i < addedPeople.size(); i++) {
                            if (addedPeople.get(i).getImageUri() != null) {
                                peopleWithImage.add(addedPeople.get(i));
                                count++;
                                //stop when we have 3  contacts that have profile images
                                if (count == 3)
                                    break;
                            }
                        }
                        //show contact icons based on size of list
                        if (addedPeople.size() == 1) {
                            first_contact_image.setVisibility(View.VISIBLE);
                        } else if (addedPeople.size() == 2) {
                            first_contact_image.setVisibility(View.VISIBLE);
                            second_contact_image.setVisibility(View.VISIBLE);
                        } else {
                            first_contact_image.setVisibility(View.VISIBLE);
                            second_contact_image.setVisibility(View.VISIBLE);
                            third_contact_image.setVisibility(View.VISIBLE);
                        }

                        if (peopleWithImage.size() > 0) {
                           /* Glide.with(TripFragment.this)
                                    .load(peopleWithImage.get(0).getImageUri())
                                    .centerCrop()
                                    .crossFade()
                                    .into(first_contact_image);*/
                            first_contact_image.setImageUrl(peopleWithImage.get(0).getImageUri());
                        }

                        if (peopleWithImage.size() > 1) {
                            /*Glide.with(TripFragment.this)
                                    .load(peopleWithImage.get(1).getImageUri())
                                    .centerCrop()
                                    .crossFade()
                                    .into(second_contact_image);*/
                            second_contact_image.setImageUrl(peopleWithImage.get(1).getImageUri());
                        }
                        if (peopleWithImage.size() > 2) {/*
                            Glide.with(TripFragment.this)
                                    .load(peopleWithImage.get(2).getImageUri())
                                    .centerCrop()
                                    .crossFade()
                                    .into(third_contact_image);*/
                            third_contact_image.setImageUrl(peopleWithImage.get(2).getImageUri());
                        }
//                        addedPeopleRecycler = new ArrayList<>();
//                        addedPeopleRecycler.addAll(addedPeople);
//                        if (tripOperations.canWrite()) {
//                            addedPeopleRecycler.add(new PhoneBookContact(true));
//                        }
                        selectedPeoplePics.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedPeoplePics.setVisibility(View.GONE);
                                if (peopleAdapter == null) {
                                    peopleAdapter = new WAHRecyclerAdapter(getActivity(), addedPeople, listener);
                                    rView.setAdapter(peopleAdapter);
                                }
                                rView.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    handleAppBarHeights();
                } catch (Exception e) {
                    DebugUtils.logException(e);
                }
            }
        });

        /*
        addPeople = (ImageView) v.findViewById(R.id.add_people_button);
        addPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddPeopleActivity.class);
                if (addedPeople.size() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("addedPeopleTrip", (ArrayList<PeopleContact>)addedPeople);
                    i.putExtras(bundle);
                }
                i.putExtra(TripOperations.TRIP_KEY_ARG,tripOperations.getTripKey());
                startActivity(i);
            }
        });

        if(tripOperations.canWrite()){
            addPeople.setVisibility(View.VISIBLE);
        }
        */

        tripStatusIcon = (ImageView) v.findViewById(R.id.trip_status_icon);
        tripStatusText = (TextView) v.findViewById(R.id.trip_status);

        setTripStatus();

        v.findViewById(R.id.btn_check_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn();
            }
        });

        v.findViewById(R.id.btn_notes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotes();
            }
        });

        v.findViewById(R.id.btn_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhoto();
            }
        });

//        v.findViewById(R.id.btn_video).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showRoute();
//            }
//        });

        floatingMenu = v.findViewById(R.id.floating_menu);

        tripTabs = (TabLayout) v.findViewById(R.id.trip_tabs);


        tripViewPager.setAdapter(new TripSlidePagerAdapter(getChildFragmentManager()));

        tripTabs.setupWithViewPager(tripViewPager);

        floatingMenu.setVisibility(View.INVISIBLE);

        tripViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (!currentTrip.isFinished()) {
                    if (position == 0) {
                        floatingMenu.setVisibility(View.INVISIBLE);
                    } else {
                        floatingMenu.setVisibility(View.VISIBLE);
                    }
                } else {
                    floatingMenu.setVisibility(View.INVISIBLE);
                }
                selectedDayOrder = position - 1;
                mListener.onTripTabChanged(position);
                drawCoachMarks(position);

                defaultTab = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        backImage = (ImageView) v.findViewById(R.id.back_image);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FloatingActionsMenu) floatingMenu).collapse();
            }
        });

        ((FloatingActionsMenu) floatingMenu).setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                backImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                backImage.setVisibility(View.INVISIBLE);
            }
        });

        tripToolbar = (Toolbar) v.findViewById(R.id.trip_tool_bar);
        collapsingToolbar = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(tripName);
        collapsingToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                collapsingToolbar.setMinimumHeight(tripToolbar.getHeight() + tripTabs.getHeight());
            }
        });
        if (currentTrip.getDays().size() > 2)
            tripTabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        tripNameText = (TextView) v.findViewById(R.id.trip_name_in_tool_bar);

        tripNameText.setText(tripName);

        appBar = (AppBarLayout) v.findViewById(R.id.trip_app_bar);

        appBar.addOnOffsetChangedListener(offsetChangeListener);

        //featureImage.setImageResource(R.drawable.placeholder_coverimage);
        //   tripWallMask.setVisibility(View.GONE);
        tripOperations.getTripFeatureImage(new TripAsyncCallback<String>() {
            @Override
            public void onCallBack(String result) {
                if (!TextUtils.isEmpty(result)) {
/*                    Glide.with(TripFragment.this)
                            .load(Uri.parse(result))
                            .asBitmap()
                            .fitCenter()
                            .centerCrop()
                            .into(new BitmapImageViewTarget(featureImage) {
                                      @Override
                                      protected void setResource(Bitmap resource) {
                                          super.setResource(resource);
                                      }
                                  }
                            );*/
                    featureImage.getLayoutParams().height = tripWallMask.getLayoutParams().height;
                    featureImage.getLayoutParams().width = tripWallMask.getLayoutParams().width;
                    // featureImage.requestLayout();
                    featureImage.setImageUrl(Uri.parse(result));
                }
            }
        });
        tripMenuOptionsHandler = new TripMenuOptionsHandler();
        tripToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return tripMenuOptionsHandler.onMenuItemClicked(item);
            }
        });

        tripToolbar.getMenu().clear();
        tripToolbar.inflateMenu(tripOperations.getMenuLayout());

        publishTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), PublishTripActivity.class);
                intent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                startActivity(intent);*/
                fragPublishTripButton.setEnabled(false);
                publishTripKey = tripOperations.getTripKey();
                publishTrip();
            }
        });

        syncTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTripDoneClicked();
            }
        });

        mErrorTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTripDoneClicked();
            }
        });

        finishTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndFinishUploadTrip();
            }
        });

        skipFinishTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMainLayoutVisible();
            }
        });

        skipErrorPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMainLayoutVisible();
            }
        });

        reportIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FeedbackActivity.class);
                startActivity(intent);
            }
        });

        skipPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });

        fragPublishTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragPublishTripButton.setEnabled(false);
                publishTrip();
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (facebook.isChecked()) {
                    if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
                        if (FacebookUtils.hasPublishPermissions()) {
                            return;
                        }
                    }
                    facebook.setChecked(false);
                    ParseFacebookUtils.linkWithPublishPermissionsInBackground(ParseUser.getCurrentUser()
                            , TripFragment.this, FacebookUtils.getFacebookPublishPermissions(), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        if (FacebookUtils.hasPublishPermissions())
                                            facebook.setChecked(true);
                                    } else {
                                        Log.e("Error", "Error while getting link permissions", e);
                                        Toast.makeText(getActivity(),
                                                "An Error occured while linking facebook account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        sharePublishedTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShareTripActivity.class);
                intent.putExtra(TripOperations.TRIP_KEY_ARG, publishTripKey);
                startActivityForResult(intent, SHARE_PUBLISHED_TRIP);
            }
        });

        skipeShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });

        if (defaultTab == -1) {
            tripTabs.getTabAt(0).select();
        } else {
            tripTabs.getTabAt(defaultTab).select();
        }

        handleMenuItemDisplay();

        handleAppBarHeights();
    }

    void setMainLayoutVisible() {
        mainLayout.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        publishTripLayout.setVisibility(View.GONE);
        finishTripLayout.setVisibility(View.GONE);
        loaderLayout.setVisibility(View.GONE);
        publishSuccessLayout.setVisibility(View.GONE);
    }

    void setFinishTripLayoutVisible() {
        mainLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        publishTripLayout.setVisibility(View.GONE);
        finishTripLayout.setVisibility(View.VISIBLE);
        loaderLayout.setVisibility(View.GONE);
        publishSuccessLayout.setVisibility(View.GONE);
    }

    void setErrorLayoutVisible() {
        mainLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        publishTripLayout.setVisibility(View.GONE);
        finishTripLayout.setVisibility(View.GONE);
        loaderLayout.setVisibility(View.GONE);
        publishSuccessLayout.setVisibility(View.GONE);
    }


    void setPublishTripLayoutVisible() {
        mainLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        publishTripLayout.setVisibility(View.VISIBLE);
        finishTripLayout.setVisibility(View.GONE);
        loaderLayout.setVisibility(View.GONE);
        publishSuccessLayout.setVisibility(View.GONE);
    }

    void setLoaderLayoutVisible() {
        mainLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        publishTripLayout.setVisibility(View.GONE);
        finishTripLayout.setVisibility(View.GONE);
        loaderLayout.setVisibility(View.VISIBLE);
        publishSuccessLayout.setVisibility(View.GONE);
    }

    void setPublishSuccessLayout() {
        mainLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        publishTripLayout.setVisibility(View.GONE);
        finishTripLayout.setVisibility(View.GONE);
        loaderLayout.setVisibility(View.GONE);
        publishSuccessLayout.setVisibility(View.VISIBLE);
    }

    private void handleAppBarHeights() {
        boolean showPeople = false;
        boolean showDate = false;

        if (addedPeople.size() > 0) {
            showPeople = true;
        }

        if (currentTrip.isUploaded()) {
            showDate = true;
        }

        int appBarDimen = R.dimen.trip_fragment_app_bar_height_without_people_and_date;
        int translateDimen = R.dimen.trip_fragment_tab_layout_translation_y_without_people_and_date;

        people.setVisibility(View.GONE);
        trip_date_text.setVisibility(View.GONE);

        if (showPeople && showDate) {
            appBarDimen = R.dimen.trip_fragment_app_bar_height_with_date_and_people;
            translateDimen = R.dimen.trip_fragment_tab_layout_translation_y_with_date_and_people;
            people.setVisibility(View.VISIBLE);
            trip_date_text.setVisibility(View.VISIBLE);
        } else if (showPeople) {
            appBarDimen = R.dimen.trip_fragment_app_bar_height_with_people;
            translateDimen = R.dimen.trip_fragment_tab_layout_translation_y_with_people;
            people.setVisibility(View.VISIBLE);
        } else if (showDate) {
            appBarDimen = R.dimen.trip_fragment_app_bar_height_with_date;
            translateDimen = R.dimen.trip_fragment_tab_layout_translation_y_with_date;
            trip_date_text.setVisibility(View.VISIBLE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1)
            tripTabs.setTranslationY(getResources()
                    .getDimensionPixelSize(translateDimen));

        CoordinatorLayout.LayoutParams appBarParams = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
        appBarParams.height = getResources()
                .getDimensionPixelOffset(appBarDimen);
        appBar.setLayoutParams(appBarParams);
    }

    private void handleMenuItemDisplay() {
        if (tripOperations.getTrip().isFinished() && tripOperations.getTrip().isUploaded()) {
//            if(tripToolbar.getMenu().findItem(R.id.action_upload_trip) != null)
//                tripToolbar.getMenu().removeItem(R.id.action_upload_trip);
        }
    }

/* private void toggleTripDoneDisplay(){
        if(tripOperations.getTrip().isUploaded()){
            return;
        }
        if(!tripOperations.getTrip().isFinished()){
            //trip_done_layout.setVisibility(View.VISIBLE);
            return;
        }

        if(!tripOperations.getTrip().isUploaded()){
            //trip_done_layout.setVisibility(View.VISIBLE);
            return;
        }
    } */

    private void checkAndFinishUploadTrip() {
        if (!tripOperations.getTrip().isFinished()) {
            startFinishTrip();
            return;
        }

        if (!tripOperations.getTrip().isUploaded()) {
            startUploadTrip();
            return;
        }
    }

    private void onTripDoneClicked() {

        //mListener.onFinishTripClicked();

        if (!tripOperations.getTrip().isFinished()) {
            setFinishTripLayoutVisible();
            return;
        }

        if (!tripOperations.getTrip().isUploaded()) {
            startUploadTrip();
            return;
        }
    }

    @Override
    public void itemClicked() {
        Intent i = new Intent(getActivity(), AddPeopleActivity.class);
        if (addedPeople.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("addedPeopleTrip", (ArrayList<PeopleContact>) addedPeople);
            i.putExtras(bundle);
        }
        i.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
        startActivity(i);
    }

    private void showFinishTrip() {
        if (currentTrip.isFinished()) {
            startUploadTrip();
            return;
        }
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.finish_trip_lower) + "\n\n" + "\"" + currentTrip.getName() + "\"")
                .content(R.string.finish_trip_warning)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .negativeColor(getResources().getColor(R.color.orange_primary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        startFinishTrip();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    private void startFinishTrip() {
        setLoaderLayoutVisible();
        new FinishTripTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showUploadTripDialog() {
        View finishTripView = getActivity().getLayoutInflater().inflate(R.layout.finishing_trip, null);
        ((TextView) finishTripView.findViewById(R.id.trip_name)).setText("\"" + currentTrip.getName() + "\"");

        ProgressBar progressBar = ((ProgressBar) finishTripView.findViewById(R.id.finish_trip));

        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        ServiceUtils.setShowUploadStatus(true);

        uploadTripWait = new MaterialDialog.Builder(getActivity())
                .customView(finishTripView, false)
                .widgetColor(getResources().getColor(R.color.orange_primary))
                .cancelable(false)
                .negativeText(R.string.dismiss)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                        ServiceUtils.setShowUploadStatus(false);
                        uploadTripWait = null;
                    }
                })
                .title("Sync in progress").show();
    }

    private void startUploadTrip() {
        if (ServiceUtils.isSyncServiceRunning()) {
            ServiceUtils.setStartUploadAfterSync(true);
            new MaterialDialog.Builder(getActivity())
                    .widgetColor(getResources().getColor(R.color.orange_primary))
                    .content("Sync service is running.")
                    .negativeText(R.string.dismiss)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            dialog.dismiss();
                        }
                    })
                    .title("Syncing").show();
            return;
        }
        if (ServiceUtils.getUploadTripStatus() == ServiceUtils.UPLOAD_TRIP_RUNNING) {
            new MaterialDialog.Builder(getActivity())
                    .widgetColor(getResources().getColor(R.color.orange_primary))
                    .content("Trip is already being synced.")
                    .negativeText(R.string.dismiss)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            dialog.dismiss();
                        }
                    })
                    .title("Syncing").show();
            return;
        }
         showUploadTripDialog();
        setLoaderLayoutVisible();

        circularProgressBar.setProgress(30);
        circularProgressBar.setTitle("30%");
        loaderText.setText("Saving Trip Details...");
        ServiceUtils.setUploadTripStatus(ServiceUtils.UPLOAD_TRIP_RUNNING);
        Intent i = new Intent(UploadTripReceiver.UPLOAD_FULL_TRIP_INTENT);
        i.putExtra("receiver", new FinishTripReceiver(new Handler()));
        getActivity().sendBroadcast(i);
    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();
    }

    private class FinishTripReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        public FinishTripReceiver(Handler handler) {
            super(handler);
        }

        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if (resultCode == UploadTripService.UPLOAD_COMPLETED) {
                if (uploadTripWait != null && !uploadTripWait.isCancelled()) {
                    uploadTripWait.dismiss();
                }
                finishTrip(resultData.getString(TripOperations.TRIP_KEY_ARG, ""));
            } else if (resultCode == UploadTripService.UPLOAD_PROGRESS) {

                if (circularProgressBar != null && circularProgressBar.getVisibility() == View.VISIBLE) {
                    int percentage = resultData.getInt(UploadTripService.UPLOAD_PROGRESS_KEY, 0);
                    circularProgressBar.setProgress(percentage);
                    circularProgressBar.setTitle(percentage + "%");
                    if (percentage > 50) {
                        loaderText.setText("Saving trip photos...");
                        loaderText2.setText("It might take some time.Hold tight!");
                    }
                    /*ProgressBar progressBar = (ProgressBar) uploadTripWait.findViewById(R.id.finish_trip);
                    if (progressBar != null) {
                        progressBar.setProgress(resultData.getInt(UploadTripService.UPLOAD_PROGRESS_KEY, 0));
                    }*/
                }
            } else {
                if (uploadTripWait != null && !uploadTripWait.isCancelled()) {
                    uploadTripWait.dismiss();
                }
                failedUploadTrip();
            }
        }
    }

    private void failedUploadTrip() {
        setErrorLayoutVisible();
        View uploadTripFailedView = getActivity().getLayoutInflater().inflate(R.layout.upload_trip_failed, null);
        ((TextView) uploadTripFailedView.findViewById(R.id.trip_name)).setText("\"" + currentTrip.getName() + "\"");

       new MaterialDialog.Builder(getActivity())
                .customView(uploadTripFailedView, false)
                .widgetColor(getResources().getColor(R.color.orange_primary))
                .positiveText(R.string.retry_trip)
                .negativeText(R.string.view_trip)
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .negativeColor(getResources().getColor(R.color.orange_primary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        startUploadTrip();
                    }
                }).title("Sync Trip").show();
    }

    private String publishTripKey;

    private void finishTrip(String id) {
        publishTripKey = id;

        String sharerId = ParseCustomUser.getCurrentUser().getSharer();
        if (sharerId != null && !TextUtils.isEmpty(sharerId) && ParseCustomUser.getCurrentUser().getTotalTrips() == 1) {
            ShareUtils.sendTripCompleteNotificationToSharer(sharerId);
        }

        new ClearCurrentTripTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        setPublishTripLayoutVisible();

        /* Activity activity = getActivity();
        if (activity != null) {
            if (!TextUtils.isEmpty(id)) {
                Intent intent = new Intent(activity, TripActivity.class);
                intent.putExtra(TripOperations.TRIP_KEY_ARG, id);
                intent.putExtra(TripActivity.CLEAR_CURRENT_TRIP, true);
                intent.putExtra(ViewUtils.PARENT_ACTIVITY, HomeActivity.class);
                startActivity(intent);
            }
        }*/
    }

    private class OffsetChangeListener implements AppBarLayout.OnOffsetChangedListener {

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (tripNameText != null) {
                if (Math.abs(i) == appBarLayout.getTotalScrollRange()) {
                    tripNameText.setVisibility(View.VISIBLE);
                    collapsingToolbar.setContentScrimColor(getResources().getColor(R.color.orange_primary));
                } else {
                    tripNameText.setVisibility(View.GONE);
                    //toggleTripDoneDisplay();
                    trip_status_layout.setVisibility(View.VISIBLE);
                    collapsingToolbar.setContentScrimColor(getResources().getColor(android.R.color.transparent));
                }

                if (Math.abs(i) + tripToolbar.getHeight() >= appBarLayout.getTotalScrollRange()) {
                    //trip_done_layout.setVisibility(View.GONE);
                    trip_status_layout.setVisibility(View.GONE);
                }
            }
        }
    }

    private class TripSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int COUNT = 1;

        public TripSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            COUNT = 1 + currentTrip.getDays().size();
        }


        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                Bundle args = new Bundle();
                args.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                return Fragment.instantiate(getActivity(), TripSummaryFragment.class.getName(), args);
            } else {
                Bundle args = new Bundle();
                args.putString(TimeLineFragment.TIMELINE_TYPE, TimeLineFragment.DAY_TIMELINE);
                args.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                args.putInt(TimeLineFragment.DAY_ORDER, position - 1);
                if (defaultTab == position && scrollPosition >= 0) {
                    args.putInt(TimeLineFragment.TIMELINE_SCROLL_POSITION, scrollPosition);
                }
                return Fragment.instantiate(getActivity(), TimeLineFragment.class.getName(), args);
            }
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "Summary";
            else {
                // this additional hack has been added
                //when trip is uploaded but some day data is corrupted
                try {
                    return currentTrip.getDays().get(position - 1).getName();
                } catch (Exception e) {
                    currentTrip.getDays().remove(position - 1);
                    return "";
                }
            }
        }
    }

    public void checkIn() {
        if (selectedDayOrder == -1)
            return;
        backImage.setVisibility(View.INVISIBLE);
        ((FloatingActionsMenu) floatingMenu).collapse();
        Intent intent = new Intent(getActivity(), CheckInActivity.class);
        intent.putExtra(TripUtils.DAY_ORDER_FOR_INTENT, selectedDayOrder);
        startActivity(intent);
    }

    public void addPhoto() {
        if (selectedDayOrder == -1)
            return;
        backImage.setVisibility(View.INVISIBLE);
        ((FloatingActionsMenu) floatingMenu).collapse();
        Intent intent = new Intent(getActivity(), PhotoGalleryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripUtils.DAY_ORDER_FOR_INTENT, selectedDayOrder);
        startActivity(intent);
    }

    public void showRoute() {
        if (selectedDayOrder == -1)
            return;
        Intent intent = new Intent(getActivity(), RouteActivity.class);
        ((FloatingActionsMenu) floatingMenu).collapse();
        intent.putExtra(TripUtils.DAY_ORDER_FOR_INTENT, selectedDayOrder);
        startActivity(intent);
    }

    public void addNotes() {
        if (selectedDayOrder == -1)
            return;
        backImage.setVisibility(View.INVISIBLE);
        ((FloatingActionsMenu) floatingMenu).collapse();
        Intent intent = new Intent(getActivity(), NoteActivity.class);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
        startActivity(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            tripOperations = mListener.getTripOperations();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        dismissDialog();
    }

    @Override
    public void onResume() {
        if (mAccessTokenTracker != null && !mAccessTokenTracker.isTracking()) {
            mAccessTokenTracker.startTracking();
        }

        if (!tripOperations.isTripAvailable() && !(publishTripLayout.getVisibility() == View.VISIBLE)) {
            tripUpdateReceiver = null;
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            super.onResume();
            getActivity().finish();
            return;
        }
        if (tripOperations.listenForUpdates() && ServiceUtils.getUploadTripStatus() != ServiceUtils.UPLOAD_TRIP_STATUS_INVALID) {
            if (ServiceUtils.getUploadTripStatus() == ServiceUtils.UPLOAD_TRIP_COMPLETED) {
                finishTrip(tripOperations.getTrip().getObjectId());
                super.onResume();
                tripUpdateReceiver = null;
                getActivity().finish();
                return;
            }
            if (ServiceUtils.showUploadTripStatus()) {
                if (ServiceUtils.getUploadTripStatus() == ServiceUtils.UPLOAD_TRIP_RUNNING) {
                    showUploadTripDialog();
                } else if (ServiceUtils.getUploadTripStatus() == ServiceUtils.UPLOAD_TRIP_FAILED) {
                    failedUploadTrip();
                }
            }
        }
        super.onResume();
        if (tripUpdateReceiver != null) {
            getActivity().registerReceiver(tripUpdateReceiver, new IntentFilter(TripServiceManager.TRIP_UPDATE_BROADCAST_INTENT));
        }
        if (tripOperations.getTrip() != null && tripViewPager.getAdapter().getCount() != tripOperations.getTrip().getDays().size() + 1) {
            refreshTrip();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tripUpdateReceiver != null)
            getActivity().unregisterReceiver(tripUpdateReceiver);
        if (uploadTripWait != null) {
            uploadTripWait.dismiss();
            uploadTripWait = null;
        }

        mAccessTokenTracker.stopTracking();
        dismissDialog();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        backImage.setVisibility(View.INVISIBLE);
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
    public interface OnFragmentInteractionListener extends TripOperationsLoader {
        void setSupportToolbarInTripFragment(Toolbar toolbar, TripMenuOptionsHandler tripMenuOptionsHandler);

        void onTripTabChanged(int pageIndex);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismissDialog();
    }

    private void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void refreshTrip() {
        if (rootView != null) {
            setup(rootView);
        }
    }

    private class TripUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTrip();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGE_FEATURE_IMAGE_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            loadImage(data.getData());
            return;
        } else if (requestCode == UPDATED_POSTS_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            refreshTrip();
        } else if (requestCode == SHARE_PUBLISHED_TRIP) {
            goToHome();
            return;
        } else if (requestCode >= FacebookUtils.ACTIVITY_REQUEST_CODE_OFFSET) {
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
            //  return;
        } else if (requestCode == EDIT_PEOPLE && resultCode == getActivity().RESULT_OK) {
            try {
                addedPeople = (List<PeopleContact>) data.getSerializableExtra("addedPeopleTrip");
                peopleAdapter = new WAHRecyclerAdapter(getActivity(), addedPeople, listener);
                rView.setAdapter(peopleAdapter);
                rView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                DebugUtils.logException(e);
                Toast.makeText(getActivity(), "Couldn't get people", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadImage(Uri uri) {
        progressDialog = CenterProgressDialog.show(getActivity(), null, null, true, false);
        new LoadImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
    }

    private class LoadImageTask extends AsyncTask<Uri, Void, Void> {

        private Uri selectedImageUri;

        @Override
        protected Void doInBackground(Uri... params) {
            if (params != null && params[0] != null) {
                selectedImageUri = ParseFileUtils.saveToPrivateLocation(params[0]);
                try {
                    tripOperations.updateTripImage(selectedImageUri);
                } catch (Exception e) {

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (selectedImageUri != null) {
                featureImage.getLayoutParams().height = tripWallMask.getLayoutParams().height;
                featureImage.getLayoutParams().width = tripWallMask.getLayoutParams().width;
                featureImage.setImageUrl(selectedImageUri);
              /*  Glide.with(TripFragment.this)
                        .load(selectedImageUri)
                        .asBitmap()
                        .fitCenter()
                        .centerCrop()
                        .into(new BitmapImageViewTarget(featureImage) {
                                  @Override
                                  protected void setResource(Bitmap resource) {
                                      super.setResource(resource);
                                  }
                              }
                        );*/
            }
        }
    }

    private void showEditTripDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_trip, null);
        final EditText editTripText = (EditText) view.findViewById(R.id.edit_trip_name_text);
        MaterialDialog editDialog = new MaterialDialog.Builder(getActivity())
                .customView(view, true)
                .title(R.string.action_edit_trip_name)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        /*EditText text = ((EditText)dialog.findViewById(R.id.edit_trip_name_text));*/
                        if (editTripText.getText().length() != 0) {
                            trip.setName(editTripText.getText().toString());
                            trip.pinInBackground();
                            textView.setText(trip.getName());
                            tripNameText.setText(trip.getName());
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                }).show();

        editTripText.setText(trip.getName());
        editTripText.setSelection(0, trip.getName().length());
    }

    private class FinishTripTask extends AsyncTask<Void, Integer, Void> {

        private MaterialDialog materialDialog;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Date endTime = Calendar.getInstance().getTime();
                currentTrip.setFinished(true);
                publishProgress(20);
                currentTrip.setEndTime(endTime);
                Day day = currentTrip.getDay(currentTrip.getTotalDays() - 1);
                publishProgress(40);
                day.setEndTime(endTime);
                tripOperations.save(day);
                publishProgress(60);
                tripOperations.save(currentTrip);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                View finishTripView = getActivity().getLayoutInflater().inflate(R.layout.finishing_trip, null);
                ((TextView) finishTripView.findViewById(R.id.trip_name)).setText("\"" + currentTrip.getName() + "\"");

                materialDialog = new MaterialDialog.Builder(getActivity())
                        .customView(finishTripView, false)
                        .widgetColor(getResources().getColor(R.color.orange_primary))
                        .cancelable(false)
                        .autoDismiss(false)
                        .title("Finishing").show();
                circularProgressBar.setProgress(1);
                loaderText.setText("Finishing trip...");
            } catch (Exception e) {

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            circularProgressBar.setProgress(50);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                Intent stoppingintent = new Intent(TripServiceStopManager.TRIP_SERVICE_STOP_BROADCAST_INTENT);
                getActivity().sendBroadcast(stoppingintent);
                if (materialDialog != null) {
                    materialDialog.dismiss();
                }
                circularProgressBar.setProgress(100);
                tripToolbar.getMenu().clear();
                tripToolbar.inflateMenu(tripOperations.getMenuLayout());
                handleMenuItemDisplay();
                floatingMenu.setVisibility(View.INVISIBLE);
                setTripStatus();
                startUploadTrip();
            } catch (Exception e) {

            }
        }
    }

    public class TripMenuOptionsHandler {

        public boolean onMenuItemClicked(MenuItem item) {
            int itemId = item.getItemId();
//            if(itemId == R.id.action_finish_trip || itemId == R.id.action_finish_trip_icon){
//                showFinishTrip();
//                return true;
//            }
//
//            if(itemId == R.id.action_change_trip_feature_remove_image){
//
//            }

            if (itemId == R.id.action_edit_trip_name) {
                showEditTripDialog();
                return true;
            }
            if (itemId == R.id.action_finish_trip /*|| itemId == R.id.action_upload_trip*/) {
                onTripDoneClicked();
                return true;
            }

            if (itemId == R.id.action_reorder_posts) {
                Intent intent = new Intent(getActivity(), ReorderActivity.class);
                Bundle args = new Bundle();
                args.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                intent.putExtras(args);
                startActivityForResult(intent, UPDATED_POSTS_REQUEST_CODE);
                return true;
            }

            if (itemId == R.id.action_edit_people) {
                Intent pIntent = new Intent(getActivity(), AddPeopleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("addedPeopleTrip", (ArrayList<PeopleContact>) addedPeople);
                bundle.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                pIntent.putExtras(bundle);
                startActivityForResult(pIntent, EDIT_PEOPLE);
                return true;
            }

            if (itemId == R.id.action_delete_posts) {
                Intent intent = new Intent(getActivity(), DeleteActivity.class);
                Bundle args = new Bundle();
                args.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                intent.putExtras(args);
                startActivityForResult(intent, UPDATED_POSTS_REQUEST_CODE);
                return true;
            }

            if (itemId == R.id.action_settings) {
                Intent intent = new Intent(getActivity(), TripSettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }

            if (itemId == R.id.action_share_journal) {
                Intent intent = new Intent(getActivity(), ShareTripActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }

            if (itemId == R.id.action_change_trip_feature_image) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHANGE_FEATURE_IMAGE_REQUEST_CODE);
                return true;
            }

            if (itemId == R.id.action_delete_journal) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.action_delete_journal)
                        .content("Are you sure you want to delete the Trip \"" + trip.getName() + "\"")
                        .positiveText(R.string.ok)
                        .negativeText(R.string.cancel)
                        .positiveColor(getResources().getColor(R.color.orange_primary))
                        .negativeColor(getResources().getColor(R.color.orange_primary))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                progressDialog = CenterProgressDialog.show(getActivity(), null, null, true);
                                trip.setDeleted(true);
                                trip.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        try {
                                            if (progressDialog != null) {
                                                progressDialog.dismiss();
                                                progressDialog = null;
                                            }
                                            if (e == null) {
                                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.PROFILE_TAB);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                getActivity().finish();
                                                ParseCustomUser.getCurrentUser()
                                                        .deletedTrip(trip.isPublished());
                                            } else {
                                                Toast.makeText(getActivity(), "An Error occure while deleting the trip", Toast.LENGTH_LONG);
                                            }
                                        } catch (Exception ex) {
                                            DebugUtils.logException(ex);
                                        }
                                    }
                                });
                            }
                        }).show();
                return true;
            }

            if (itemId == R.id.action_unpublish_journal) {
                unPublishTrip();
                return true;
            }

            if (itemId == R.id.action_coupon) {
                              showDialog();
                return true;
            }
            return false;
        }

    }

    public void applyCoupon(String couponId) {
        Calendar c = Calendar.getInstance();
        try {
            ParseQuery.getQuery(Coupon.class).whereEqualTo(Coupon.OBJECTID, couponId).whereLessThanOrEqualTo(Coupon.VALID_FROM, c.getTime())
                    .whereGreaterThanOrEqualTo(Coupon.VALID_TILL, c.getTime()).whereEqualTo(Coupon.ISACTIVE, true)
                    .findInBackground(new FindCallback<Coupon>() {
                        @Override
                        public void done(List<Coupon> coupons, ParseException e) {
                            if (coupons.size() != 0) {
                                tripOperations.getTrip().setCoupon(coupons.get(0));
                                callApplyCouponApi("apply_coupon", coupons.get(0), tripOperations);
                            }
                        }
                    });
        } catch (Exception e) {
            DebugUtils.logException(e);
        }
    }

    public void showDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title("Enter Coupon Code")
                .customView(R.layout.fragment_coupon, true)
                .positiveText(R.string.apply)
                .negativeText(R.string.cancel)
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .negativeColor(getResources().getColor(R.color.orange_primary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        hideKeyboard(dialog);
                        Calendar c = Calendar.getInstance();
                        String codeEntered = ((EditText) dialog.findViewById(R.id.coupon)).getText().toString().toUpperCase();
                        progressDialog = CenterProgressDialog.show(getActivity(), "Applying coupon", null, true);
                        try {
                            ParseQuery.getQuery(Coupon.class).whereEqualTo(Coupon.CODE, codeEntered).whereLessThanOrEqualTo(Coupon.VALID_FROM, c.getTime())
                                    .whereGreaterThanOrEqualTo(Coupon.VALID_TILL, c.getTime()).whereEqualTo(Coupon.ISACTIVE, true)
                                    .findInBackground(new FindCallback<Coupon>() {
                                        @Override
                                        public void done(List<Coupon> coupons, ParseException e) {
                                            if (coupons.size() != 0) {
                                                progressDialog.dismiss();

                                                if (TripUtils.getInstance().getCurrentTripOperations().isTripAvailable()) {
                                                    // Trip trip = TripUtils.getInstance().getCurrentTripOperations().getTrip();
                                                    //if no coupon has been applied , then show success msg
                                                    // if (trip.getCoupon() == null) {
                                                    TripUtils.getInstance().getCurrentTripOperations().getTrip().setCoupon(coupons.get(0));
                                                    new MaterialDialog.Builder(getActivity())
                                                            .title(R.string.applied)
                                                            .content(coupons.get(0).getMessage())
                                                            .positiveText(R.string.ok)
                                                            .positiveColor(getResources().getColor(R.color.orange_primary))
                                                            .show();

                                                    callApplyCouponApi("apply_coupon", coupons.get(0), tripOperations);
                                                    //    }
                                                    //if you have already used one coupon
                                                   /* else {
                                                        new MaterialDialog.Builder(getActivity())
                                                                .title(R.string.already_applied)
                                                                .content(R.string.coupon_already_applied)
                                                                .positiveText(R.string.ok)
                                                                .positiveColor(getResources().getColor(R.color.orange_primary))
                                                                .show();
                                                    }*/

                                                } else {
                                                    //if there is no ongoing trip, dont apply coupon
                                                    new MaterialDialog.Builder(getActivity())
                                                            .content(R.string.no_ongoing_trip)
                                                            .positiveText(R.string.ok)
                                                            .positiveColor(getResources().getColor(R.color.orange_primary))
                                                            .show();
                                                }
                                            } else {
                                                //no matching coupon code found
                                                progressDialog.dismiss();
                                                new MaterialDialog.Builder(getActivity())
                                                        .title(R.string.invalid_coupon)
                                                        .content(R.string.enter_valid)
                                                        .positiveText(R.string.ok)
                                                        .positiveColor(getResources().getColor(R.color.orange_primary))
                                                        .show();
                                            }
                                        }
                                    });
                        } catch (Exception e) {
                            DebugUtils.logException(e);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        hideKeyboard(dialog);
                    }
                })
                .show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void callApplyCouponApi(String action, Coupon coupon, TripOperations operations) {
        HashMap<String, String> params = new HashMap<>();
        ParseCustomUser user = ParseCustomUser.getCurrentUser();
        params.put(BiaAppAPI.POST_PARAM_ACTION, action);
        params.put(BiaAppAPI.POST_PARAM_USER_ID, user.getObjectId());
        params.put(BiaAppAPI.POST_PARAM_USER_EMAIL, user.getEmail());
        params.put(BiaAppAPI.POST_PARAM_USER_NAME, user.getName());
        params.put(BiaAppAPI.POST_PARAM_TRIP_ID, operations.getTrip().getObjectId() == null ? "empty_trip" : operations.getTrip().getObjectId());
        params.put(BiaAppAPI.POST_PARAM_COUPON_ID, coupon.getObjectId());
        params.put(BiaAppAPI.POST_PARAM_COUPON_CODE, coupon.getCode());

        new Utils.CallServerApi(params, BiaAppAPI.URL_BIA_API).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void hideKeyboard(MaterialDialog dialog) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((EditText) dialog.findViewById(R.id.coupon)).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void unPublishTrip() {
        new MaterialDialog.Builder(getActivity())
                .title("Are you sure you want to un-publish this?")
                .content("This trip will no longer be discoverable or feature in any search.  You may alternatively opt to make some posts/pics private while keeping the trip published")
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .negativeColor(getResources().getColor(R.color.orange_primary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        new unPublishTripTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    private MaterialDialog materialDialog;

    private class unPublishTripTask extends AsyncTask<Void, Void, Void> {

        private boolean failed;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String tripName = tripOperations.getTrip().getName();
            if (tripName.length() > 21)
                tripName = tripName.substring(0, 20) + "....";
            materialDialog = new MaterialDialog.Builder(getActivity())
                    .content("\"" + tripName + "\"")
                    .progress(true, 0)
                    .widgetColor(getResources().getColor(R.color.orange_primary))
                    .cancelable(false)
                    .autoDismiss(false)
                    .title("unPublishing Trip").show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isCancelled())
                return;
            if (materialDialog != null) {
                materialDialog.dismiss();
                if (failed)
                    Toast.makeText(getActivity(), "Error while un-publishing Trip", Toast.LENGTH_LONG).show();
                else {
                    refreshTrip();
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                tripOperations.unpublish();
            } catch (Exception e) {
                failed = true;
            }
            return null;
        }
    }

    public void setTripStatus() {
        if (tripOperations != null) {
            if (tripOperations.getTrip().isUploaded()) {
                tripStatusIcon.setVisibility(View.GONE);
                tripStatusText.setVisibility(View.GONE);
                syncTripBtn.setVisibility(View.GONE);
            } else if (tripOperations.getTrip().isFinished()) {
                tripStatusIcon.setVisibility(View.GONE);
                tripStatusText.setText("FINISHED");
                syncTripBtn.setVisibility(View.VISIBLE);
            } else {
                tripStatusIcon.setVisibility(View.VISIBLE);
                tripStatusText.setText("ONGOING");
                syncTripBtn.setVisibility(View.GONE);
            }
        }
    }

    private void drawCoachMarks(int position) {

        if (!tripOperations.getTrip().isFinished()) {

            View view1;
            View view2;
            View view3;
            View view4;

            Target markLocationByView[];
            Point markLocationByOffset[];
            Point markTextPoint[];
            String coachMarkTextArray[];

            switch (position) {
                case 0:
                    if (!SharedPrefUtils.getBooleanPreference(getActivity(), COACH_EMPTYSTATE_SUMMARY_PREF)) {

                        view1 = tripToolbar.getChildAt(1);
                        view2 = ((ViewGroup) tripTabs.getChildAt(0)).getChildAt(1);
                        view3 = ((ViewGroup) tripTabs.getChildAt(0)).getChildAt(0);
                        view4 = ((ViewGroup) tripTabs.getChildAt(0)).getChildAt(0);

                        markLocationByView = new Target[4];
                        markLocationByView[0] = new ViewTarget(view1);
                        markLocationByView[1] = new ViewTarget(view2);
                        markLocationByView[2] = new ViewTarget(view3);
                        markLocationByView[3] = new ViewTarget(view4);

                        markLocationByOffset = new Point[4];
                        markLocationByOffset[0] = new Point(0, 0);
                        markLocationByOffset[1] = new Point(0, 0);
                        markLocationByOffset[2] = new Point(-30, 50);
                        markLocationByOffset[3] = new Point(-30, 318);

                        markTextPoint = new Point[4];
                        markTextPoint[0] = new Point(-160, -50);
                        markTextPoint[1] = new Point(-140, -80);
                        markTextPoint[2] = new Point(50, 40);
                        markTextPoint[3] = new Point(50, -90);

                        float circleRadius[] = new float[4];
                        circleRadius[0] = 30f;
                        circleRadius[1] = 40f;
                        circleRadius[2] = 40f;
                        circleRadius[3] = 40;


                        coachMarkTextArray = getResources().getStringArray(R.array.coachmark_journal_empty_summary_tab);

                        showcaseView = new ShowcaseView.Builder(getActivity(), true, true)
                                .setStyle(R.style.CustomShowcaseTheme2)
                                .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                                .setOnClickListener(TripFragment.this)
                                .build();

                        showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

                        SharedPrefUtils.setBooleanPreference(getActivity(), COACH_EMPTYSTATE_SUMMARY_PREF, true);
                    }
                    break;

                case 1:

                    if (!SharedPrefUtils.getBooleanPreference(getActivity(), COACH_EMPTYSTATE_DAY_PREF)) {

                        view1 = tripToolbar.getChildAt(1);
                        view2 = ((ViewGroup) tripTabs.getChildAt(0)).getChildAt(0);
                        view3 = floatingMenu;

                        markLocationByView = new Target[3];
                        markLocationByView[0] = new ViewTarget(view1);
                        markLocationByView[1] = new ViewTarget(view2);
                        markLocationByView[2] = new ViewTarget(view3);

                        markLocationByOffset = new Point[3];
                        markLocationByOffset[0] = new Point(0, 0);
                        markLocationByOffset[1] = new Point(0, 0);
                        markLocationByOffset[2] = new Point(45, 152);

                        markTextPoint = new Point[3];
                        markTextPoint[0] = new Point(-120, 50);
                        markTextPoint[1] = new Point(10, 55);
                        markTextPoint[2] = new Point(-155, -90);

                        float circleRadius[] = new float[3];
                        circleRadius[0] = 35f;
                        circleRadius[1] = 40f;
                        circleRadius[2] = 45f;


                        coachMarkTextArray = getResources().getStringArray(R.array.coachmark_journal_empty_day);

                        showcaseView = new ShowcaseView.Builder(getActivity(), true, true)
                                .setStyle(R.style.CustomShowcaseTheme2)
                                .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                                .setOnClickListener(TripFragment.this)
                                .build();

                        showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

                        SharedPrefUtils.setBooleanPreference(getActivity(), COACH_EMPTYSTATE_DAY_PREF, true);
                    }
                    break;
            }
        }
    }

    public void drawPublishCoachMarks() {

        View view1;

        Target markLocationByView[];
        Point markLocationByOffset[];
        Point markTextPoint[];
        String coachMarkTextArray[];

        if (!SharedPrefUtils.getBooleanPreference(getActivity(), COACH_TO_PUBLISHED_TRIP)) {

            view1 = publishTripBtn;

            markLocationByView = new Target[1];
            markLocationByView[0] = new ViewTarget(view1);

            markLocationByOffset = new Point[1];
            markLocationByOffset[0] = new Point(0, 0);

            markTextPoint = new Point[1];
            markTextPoint[0] = new Point(-70, -160);

            float circleRadius[] = new float[1];
            circleRadius[0] = 60f;


            coachMarkTextArray = getResources().getStringArray(R.array.coachmark_publish_trip);

            showcaseView = new ShowcaseView.Builder(getActivity(), true, false)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                    .setOnClickListener(TripFragment.this)
                    .build();

            showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

            SharedPrefUtils.setBooleanPreference(getActivity(), COACH_TO_PUBLISHED_TRIP, true);
        }
    }

    private class ClearCurrentTripTask extends AsyncTask<Void, Void, Void> {

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


    public void publishTrip() {
        new PublishTripTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class PublishTripTask extends AsyncTask<Void, Void, Void> {

        private boolean failed;

        private boolean failedFacebook;

        private boolean postToFacebook;

        private TripOperations localTripOperations;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            localTripOperations = TripUtils.getInstance().getTripOperations(publishTripKey);
            setLoaderLayoutVisible();
            loaderText.setText("Publishing Trip...");
            circularProgressBar.setProgress(10);
            if (facebook.isChecked()) {
                postToFacebook = true;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            circularProgressBar.setProgress(100);

            if (isCancelled())
                return;
            if (materialDialog != null) {
                materialDialog.dismiss();
                if (failed)
                    Toast.makeText(getActivity(), "Error while publishing Trip", Toast.LENGTH_LONG).show();
                if (failedFacebook)
                    Toast.makeText(getActivity(), "Error while posting the trip to facebook", Toast.LENGTH_LONG).show();
            }
            setPublishSuccessLayout();
            if (localTripOperations.getTrip().getCoupon() != null) {
                new MaterialDialog.Builder(getActivity())
                        .content(localTripOperations.getTrip().getCoupon().getPushlishedMessage())
                        .positiveText(R.string.ok)
                        .positiveColor(getResources().getColor(R.color.orange_primary))
                        .show();
                callApplyCouponApi("publish_trip", localTripOperations.getTrip().getCoupon(), localTripOperations);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                localTripOperations.publish();

                if (postToFacebook) {
                    String tripCoverUrl = "http://bia-app.com/images/bia_logo_for_fb_share_in_app.png";
                    String tripName = null;
                    String tripUrl = null;

                    if (localTripOperations.getTrip() != null) {
                        if (localTripOperations.getTrip().getFeatureImage() != null) {
                            tripCoverUrl = localTripOperations.getTrip().getFeatureImage().getUrl();
                        }
                        tripName = localTripOperations.getTrip().getName();
                        tripUrl = ShareUtils.getTripShareUrl(localTripOperations.getTrip());
                    }


                    JSONObject tripInfo = new JSONObject();
                    try {
                        tripInfo.put("fb:app_id", "666846350116637");
                        tripInfo.put("og:title", tripName);
                        tripInfo.put("og:image", tripCoverUrl);
                        tripInfo.put("og:url", tripUrl);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Bundle action_params = new Bundle();
                    action_params.putString("trip", tripInfo.toString());
                    action_params.putBoolean("fb:explicitly_shared", true);
                    GraphRequest request = new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "me/bia-app:share",
                            action_params,
                            HttpMethod.POST
                    );

                    GraphResponse response = request.executeAndWait();
                    if (response.getError() != null) {
                        failedFacebook = true;
                    }
                }
            } catch (Exception e) {
                failed = true;
            }
            return null;
        }
    }

    private void goToHome() {
        Intent intent = new Intent(getActivity(), TripActivity.class);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, publishTripKey);
        intent.putExtra(TripActivity.CLEAR_CURRENT_TRIP, true);
        intent.putExtra(ViewUtils.PARENT_ACTIVITY, HomeActivity.class);
        startActivity(intent);
    }
}
