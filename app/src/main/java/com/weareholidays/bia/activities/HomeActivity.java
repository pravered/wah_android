package com.weareholidays.bia.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.MyMapFragment;
import com.weareholidays.bia.activities.journal.timeline.TimeLineFragment;
import com.weareholidays.bia.activities.journal.timeline.TripSummaryFragment;
import com.weareholidays.bia.activities.journal.trip.EmptyTripFragment;
import com.weareholidays.bia.activities.journal.trip.TripFragment;
import com.weareholidays.bia.activities.journal.trip.TripStartActivity;
import com.weareholidays.bia.activities.profile.EditProfileActivity;
import com.weareholidays.bia.activities.profile.MyAccount;
import com.weareholidays.bia.activities.profile.NotificationActivity;
import com.weareholidays.bia.activities.profile.NotificationFragment;
import com.weareholidays.bia.activities.profile.UserProfileFragment;
import com.weareholidays.bia.activities.search.ArticleFragment;
import com.weareholidays.bia.activities.search.DiscoverFragment;
import com.weareholidays.bia.activities.search.DiscoverOuterFragment;
import com.weareholidays.bia.activities.search.SearchFragment;
import com.weareholidays.bia.background.receivers.GeoFenceReceiver;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.Constants;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.utils.reorderUtils.DayTimeLine;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_TAB_PREF;


public class HomeActivity extends AppCompatActivity implements DiscoverOuterFragment.OnFragmentInteractionListener
        , MyAccount.OnFragmentInteractionListener, EmptyTripFragment.OnFragmentInteractionListener
        , TripFragment.OnFragmentInteractionListener, TripSummaryFragment.OnFragmentInteractionListener
        , TimeLineFragment.OnFragmentInteractionListener, SearchFragment.OnFragmentInteractionListener
        , MyMapFragment.OnFragmentInteractionListener, UserProfileFragment.OnFragmentInteractionListener
        , NotificationFragment.OnFragmentInteractionListener, ArticleFragment.OnFragmentInteractionListener
        , View.OnClickListener, DiscoverFragment.OnFragmentInteractionListener {


    public static final String SHOW_TAB = "SHOW_TAB";
    public static final String SELECTED_TRIP_TAB = "SELECTED_TRIP_TAB";
    public static final String SELECTED_TRIP_TAB_SCROLL = "SELECTED_TRIP_TAB_SCROLL";
    public static final int DISCOVER_TAB = 1;
    public static final int JOURNAL_TAB = 2;
    public static final int ARTICLE_TAB = 0;
    public static final int PROFILE_TAB = 3;
    public static final int ACCOUNT_TAB = 4;

    private ViewPager homeViewPager;
    private ScreenSlidePagerAdapter homePagerAdapter;
    private TabLayout tabLayout;
    private int selectedTab;
    private TripOperations tripOperations;

    private int selectedTripTab = 0;
    private int selectedTripTabScrollPosition = -1;

    private ShowcaseView showcaseView;
    MyAccount mAccountFragment;
    ArticleFragment mArticleFragment;
    DiscoverOuterFragment mDiscoverOuterFragment;
    String articleUrl;
    private boolean isShowFinishTripLayout = false;

    private String mCouponId;

    private static boolean locationAllowedAfterAccess = false;
    private static boolean locationAllowedBeforeAcess;
    private static int locationRequestCode = 110;
    private static int readContactsRequestCode = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tripOperations = TripUtils.getInstance().getCurrentTripOperations();
        super.onCreate(savedInstanceState);

        int version = Build.VERSION.SDK_INT;
        CalligraphyConfig.Builder calligraphyConfig = new CalligraphyConfig.Builder();
        calligraphyConfig.setFontAttrId(R.attr.fontPath);
//        if(version<17)
        calligraphyConfig.setDefaultFontPath("fonts/roboto/Roboto-Regular.ttf");
        CalligraphyConfig.initDefault(calligraphyConfig.build());

        setContentView(R.layout.activity_home);

        handleTripState(savedInstanceState);

        homeViewPager = (ViewPager) findViewById(R.id.pager);
        homeViewPager.setOffscreenPageLimit(5);
        homePagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        homeViewPager.setAdapter(homePagerAdapter);
        homeViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == ARTICLE_TAB) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawCoachMarks();
                        }
                    }, 200);
                } else if (position == DISCOVER_TAB) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mDiscoverOuterFragment != null)
                                mDiscoverOuterFragment.drawCoachMarks();
                        }
                    }, 100);
                } else if (position == ACCOUNT_TAB) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mAccountFragment != null)
                                mAccountFragment.drawCoachMarks(HomeActivity.this);
                        }
                    }, 100);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(homeViewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.setIcon(getTabIcon(tab.getPosition(), true));
                selectedTab = tab.getPosition();
                homeViewPager.setCurrentItem(tab.getPosition());

                View search_tab_view = findViewById(R.id.search_tab);
                if (search_tab_view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search_tab_view.getWindowToken(), 0);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setIcon(getTabIcon(tab.getPosition(), false));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        handleTabs(savedInstanceState);

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkContactsAllowed();
//        }

        //for tracking geofence
        if (!SharedPrefUtils.getStringPreference(this, SharedPrefUtils.Keys.GEO_FENCE_PREF_KEY).equalsIgnoreCase(Constants.GEOFENCE_ID)) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || locationOrContactsAllowed()) {
                    Intent intent = new Intent();
                    intent.setAction(GeoFenceReceiver.ACTION_SET_GEO_FENCE);
                    sendBroadcast(intent);
                }
        }


    }

    private boolean locationOrContactsAllowed() {
        List<String> requiredPermissions = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationAllowedBeforeAcess = true;
            locationAllowedAfterAccess = true;
        }
        else {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_CONTACTS);
        }
        if(requiredPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[requiredPermissions.size()]), locationRequestCode);
        }
        return locationAllowedAfterAccess;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == locationRequestCode) {
            if(grantResults.length == 1) { //either of location or contacts was not previous allowed
                if(locationAllowedBeforeAcess) { //the current request was for contacts
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(this, "contacts allowed", Toast.LENGTH_SHORT).show();
                    }
                    else {
//                        Toast.makeText(this, "contacts denied", Toast.LENGTH_SHORT).show();
                    }
                }
                else { //the current request was for location
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        locationAllowedAfterAccess = true;
//                        Toast.makeText(this, "location allowed", Toast.LENGTH_SHORT).show();
                    }
                    else {
//                        Toast.makeText(this, "location denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else if(grantResults.length == 2) { // both of location and contacts were requested (in the same order)
                if((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    locationAllowedAfterAccess = true;
//                    Toast.makeText(this, "location allowed", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(this, "location denied", Toast.LENGTH_SHORT).show();
                }
                if(grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "contacts allowed", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(this, "contacts denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

        private void handleTripState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getIntent() != null) {
                selectedTripTab = getIntent().getIntExtra(TripFragment.SHOW_JOURNAL_DAY
                        , TripFragment.JOURNAL_SUMMARY_VIEW);
                if (getIntent().hasExtra(TripFragment.TIMELINE_SCROLL_POSITION))
                    selectedTripTabScrollPosition = getIntent().getIntExtra(TripFragment.TIMELINE_SCROLL_POSITION, -1);

                if (NavigationUtils.showJournalInHome(getIntent())) {

                    selectedTripTab = NavigationUtils.getTripDayFromIntent(getIntent()) + 1;
                    selectedTripTabScrollPosition = NavigationUtils.getTimelineFromIntent(getIntent());
                }
            }
        } else {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    drawCoachMarks();
//                }
//            }, 200);
            selectedTripTab = savedInstanceState.getInt(SELECTED_TRIP_TAB, 0);
            selectedTripTabScrollPosition = savedInstanceState.getInt(SELECTED_TRIP_TAB_SCROLL, -1);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            articleUrl = intent.getStringExtra(ArticleFragment.ARTICLE_URL);
            if (articleUrl != null && !TextUtils.isEmpty(articleUrl)) {
                int selectedTabIndex = ARTICLE_TAB;
                //Select Required Tab
                TabLayout.Tab selectedTab = tabLayout.getTabAt(selectedTabIndex);
                selectedTab.setIcon(getTabIcon(selectedTabIndex, true));
                selectedTab.select();
                if (mArticleFragment != null) {
                    mArticleFragment.setArticle(articleUrl);
                }
            }
            intent.putExtra(ArticleFragment.ARTICLE_URL, "");
        }
    }

    private void handleTabs(Bundle savedInstanceState) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(getTabIcon(i, false));
        }

        int selectedTabIndex = ARTICLE_TAB;

        if (savedInstanceState == null) {
            if (getIntent() != null) {
                articleUrl = getIntent().getStringExtra(ArticleFragment.ARTICLE_URL);
                if (articleUrl != null && !TextUtils.isEmpty(articleUrl)) {
                    selectedTabIndex = ARTICLE_TAB;
                } else {
                    selectedTabIndex = getIntent().getIntExtra(SHOW_TAB, ARTICLE_TAB);
                    mCouponId = getIntent().getStringExtra(TripStartActivity.COUPON_ID);
                    isShowFinishTripLayout = getIntent().getBooleanExtra(TripFragment.SHOW_FINISH_TRIP_LAYOUT, false);
                }
            }

            if (NavigationUtils.showJournalInHome(getIntent())) {
                selectedTabIndex = JOURNAL_TAB;
            }
        } else {
            selectedTabIndex = savedInstanceState.getInt(SHOW_TAB, ARTICLE_TAB);
        }

        //Select Required Tab
        TabLayout.Tab selectedTab = tabLayout.getTabAt(selectedTabIndex);
        selectedTab.setIcon(getTabIcon(selectedTabIndex, true));
        selectedTab.select();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SHOW_TAB, selectedTab);
        savedInstanceState.putInt(SELECTED_TRIP_TAB, selectedTripTab);
        savedInstanceState.putInt(SELECTED_TRIP_TAB_SCROLL, selectedTripTabScrollPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    private int getTabIcon(int position, boolean active) {
        switch (position) {
            case DISCOVER_TAB:
                if (active)
                    return R.drawable.tab_discover_active;
                return R.drawable.tab_discover;
            case JOURNAL_TAB:
                if (active)
                    return R.drawable.tab_journal_active;
                return R.drawable.tab_journal;
            case ARTICLE_TAB:
                if (active)
                    return R.drawable.tab_article_active;
                return R.drawable.tab_article_inactive;
            case PROFILE_TAB:
                if (active)
                    return R.drawable.tab_profile_active;
                return R.drawable.tab_profile;
            case ACCOUNT_TAB:
                if (active)
                    return R.drawable.tab_overflow_active;
                return R.drawable.tab_overflow;
            default:
                return R.drawable.tab_discover;
        }
    }

    @Override
    public TripOperations getTripOperations() {
        return tripOperations;
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
        selectedTripTabScrollPosition = scrollIndex;
        selectedTripTab = dayOrder + 1;
    }

    @Override
    public void setSupportToolbarInTripFragment(Toolbar toolbar, TripFragment.TripMenuOptionsHandler tripMenuOptionsHandler) {
        //Just Empty Method. Need this in only Trip Activity.
    }

    @Override
    public void onTripTabChanged(int pageIndex) {
        selectedTripTab = pageIndex;
    }

    @Override
    public void setSupportToolbarInTripFragment(Toolbar toolbar) {

    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int NUM_PAGES = 5;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //TODO: Look into performance vs memory with retaining/destroy fragments
            switch (position) {
                case DISCOVER_TAB:
                    mDiscoverOuterFragment = (DiscoverOuterFragment) Fragment.instantiate(HomeActivity.this,
                            DiscoverOuterFragment.class.getName());
                    return mDiscoverOuterFragment;
                case JOURNAL_TAB:
                    if (tripOperations.isTripAvailable()) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(TripFragment.SHOW_JOURNAL_DAY, selectedTripTab);
                        bundle.putBoolean(TripFragment.SHOW_FINISH_TRIP_LAYOUT, isShowFinishTripLayout);
                        if(mCouponId != null){
                            bundle.putString(TripStartActivity.COUPON_ID, mCouponId);
                        }
                        if (selectedTripTabScrollPosition >= 0)
                            bundle.putInt(TripFragment.TIMELINE_SCROLL_POSITION, selectedTripTabScrollPosition);
                        return Fragment.instantiate(HomeActivity.this,
                                TripFragment.class.getName(), bundle);
                    }
                    return Fragment.instantiate(HomeActivity.this,
                            EmptyTripFragment.class.getName());
                case ARTICLE_TAB:
                    Bundle bundle = new Bundle();
                    bundle.putString(ArticleFragment.ARTICLE_URL, articleUrl);
                    mArticleFragment = (ArticleFragment) Fragment.instantiate(HomeActivity.this,
                            ArticleFragment.class.getName(), bundle);
                    return mArticleFragment;
                case PROFILE_TAB:
                    return Fragment.instantiate(HomeActivity.this,
                            UserProfileFragment.class.getName());
                case ACCOUNT_TAB:
                    mAccountFragment = (MyAccount) Fragment.instantiate(HomeActivity.this,
                            MyAccount.class.getName());
                    return mAccountFragment;

                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialog() {
        new MaterialDialog.Builder(this)
                .title("Enter Coupon Code")
                .customView(R.layout.fragment_coupon, true)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .negativeColor(getResources().getColor(R.color.orange_primary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        if (dialog != null && (dialog.getInputEditText().toString()).toLowerCase().equals("wah_beta")) {
                            Context context = getApplicationContext();
                            CharSequence text = "Invalid Coupon";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        } else {
                            Context context = getApplicationContext();
                            CharSequence text = "Valid Coupon";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    public void notifications(View v) {
        Intent nIntent = new Intent(this, NotificationActivity.class);
        startActivity(nIntent);
    }

    public void goToSettings(View v) {
        Intent sIntent = new Intent(this, SettingsActivity.class);
        startActivity(sIntent);
    }

    public void editProfile(View v) {
        Intent eIntent = new Intent(this, EditProfileActivity.class);
        startActivity(eIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tripOperations = null;
    }

    @Override
    public void onBackPressed() {

        boolean goback = false;

        if (mArticleFragment != null)
            goback = mArticleFragment.canGoBack();

        if (goback) {
            mArticleFragment.goBack();
        } else {
            if (selectedTab != ARTICLE_TAB) {
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    tabLayout.getTabAt(i).setIcon(getTabIcon(i, false));
                }
                TabLayout.Tab selectedTab = tabLayout.getTabAt(ARTICLE_TAB);
                selectedTab.setIcon(getTabIcon(ARTICLE_TAB, true));
                selectedTab.select();
                return;
            }
            super.onBackPressed();
        }
    }

    private void drawCoachMarks() {

        if (!SharedPrefUtils.getBooleanPreference(HomeActivity.this, COACH_TAB_PREF)) {

            View view1 = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(1);
            View view2 = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(2);
            View view3 = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(4);
//            View view4 = getWindow().getDecorView();

            Target markLocationByView[] = new Target[3];
            markLocationByView[0] = new ViewTarget(view1);
            markLocationByView[1] = new ViewTarget(view2);
            markLocationByView[2] = new ViewTarget(view3);
//            markLocationByView[3] = new ViewTarget(view4);

            Point markLocationByOffset[] = new Point[3];
            markLocationByOffset[0] = new Point(0, 0);
            markLocationByOffset[1] = new Point(0, 0);
            markLocationByOffset[2] = new Point(0, 0);
//            markLocationByOffset[3] = new Point(-110, 30);

            Point markTextPoint[] = new Point[3];
            markTextPoint[0] = new Point(-100, 50);
            markTextPoint[1] = new Point(-30, 50);
            markTextPoint[2] = new Point(-70, 50);
//            markTextPoint[3] = new Point(85, 30);

            float circleRadius[] = new float[3];
            circleRadius[0] = 30f;
            circleRadius[1] = 30f;
            circleRadius[2] = 30f;
//            circleRadius[3] = 70f;

            String coachMarkTextArray[];

            coachMarkTextArray = getResources().getStringArray(R.array.coachmark_disover);

            showcaseView = new ShowcaseView.Builder(HomeActivity.this, true, true)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                    .setOnClickListener(HomeActivity.this)
                    .build();

            showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

            SharedPrefUtils.setBooleanPreference(HomeActivity.this, COACH_TAB_PREF, true);

        }
    }
}
