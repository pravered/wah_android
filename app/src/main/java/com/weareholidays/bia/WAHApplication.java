package com.weareholidays.bia;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
//import com.localytics.android.LocalyticsActivityLifecycleCallbacks;
import com.localytics.android.LocalyticsActivityLifecycleCallbacks;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
//import com.parse.ParseInstagramUtils;
import com.parse.ParseObject;
//import com.parse.ParseTwitterUtils;
import com.parse.ParseTwitterUtils;

import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.CheckIn;
import com.weareholidays.bia.parse.models.Coupon;
import com.weareholidays.bia.parse.models.CustomLocation;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.DaySummary;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.InterCityTravelLocationPin;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Note;
import com.weareholidays.bia.parse.models.Notification;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.TripPeople;
import com.weareholidays.bia.parse.models.TripSettings;
import com.weareholidays.bia.parse.models.TripSummary;
import com.weareholidays.bia.parse.models.UserCoupon;
import com.weareholidays.bia.parse.models.Version;
import com.weareholidays.bia.parse.models.local.DayLocationPin;
import com.weareholidays.bia.parse.models.local.DaySummaryDummy;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.reorderUtils.URLs;

/**
 * Created by Teja on 21-05-2015.
 */
public class WAHApplication extends MultiDexApplication {

    private static Context applicationContext;

    private static String TWITTER_KEY = "com.twitter.sdk.consumer.key";
    private static String TWITTER_SECRET = "com.twitter.sdk.consumer.secret";
    private static String GOOGLE_KEY_METADATA = "com.weareholidays.google.SERVER_KEY";
    private static String twitterId = "ZGGmHHbp4pI3pRdU9urjl8C9M";
    private static String twitterSecret  = "HKuSlPI79h2cXRGWXDD13XEEAzZLzciRV0vno2X3R6osZ0WPxF";


    public static String GOOGLE_KEY = ""; // set in configureGoogleMaps

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        configureParse();
        configureGoogleMaps();
        configureGoogleAnalytics();
        // configureCrittercism();
//        configureLocalytics();
//        configureDebugUtils();
    }


    private void configureGoogleMaps() {
        String key = getMetadataForField(GOOGLE_KEY_METADATA);
        if(!TextUtils.isEmpty(key)){
            GOOGLE_KEY = key;
        }
    }


    private void configureParse(){

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        //Register Parse Objects
        ParseObject.registerSubclass(Album.class);
        ParseObject.registerSubclass(CheckIn.class);
        ParseObject.registerSubclass(Day.class);
        ParseObject.registerSubclass(DaySummary.class);
        ParseObject.registerSubclass(FileLocal.class);
        ParseObject.registerSubclass(Media.class);
        ParseObject.registerSubclass(Note.class);
        ParseObject.registerSubclass(ParseCustomUser.class);
        ParseObject.registerSubclass(RoutePoint.class);
        ParseObject.registerSubclass(Timeline.class);
        ParseObject.registerSubclass(Trip.class);
        ParseObject.registerSubclass(TripPeople.class);
        ParseObject.registerSubclass(TripSettings.class);
        ParseObject.registerSubclass(TripSummary.class);
        ParseObject.registerSubclass(Notification.class);
        ParseObject.registerSubclass(DaySummaryDummy.class);
        ParseObject.registerSubclass(InterCityTravelLocationPin.class);
        ParseObject.registerSubclass(DayLocationPin.class);
        ParseObject.registerSubclass(CustomLocation.class);
        ParseObject.registerSubclass(Version.class);
        ParseObject.registerSubclass(Coupon.class);
        ParseObject.registerSubclass(UserCoupon.class);
        // Add your initialization code here
        //  Parse.initialize(this);
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext()).
                applicationId("localId").enableLocalDataStore()
                    .server(URLs.applicationIP + "/parse")
                .build());
//        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext()).
//                applicationId("newAppId").enableLocalDataStore()
//                .server(URLs.applicationIP + ":8888/parse")
//                .build());
       ParseFacebookUtils.initialize(this, FacebookUtils.ACTIVITY_REQUEST_CODE_OFFSET);
       ParseTwitterUtils.initialize(getMetadataForField(TWITTER_KEY),getMetadataForField(TWITTER_SECRET));
        //ParseTwitterUtils.initialize(twitterId, twitterSecret);
        //  ParseInstagramUtils.initialize("bee91a4173b54325989a49dca812c97c", "b42e521b4c5f48348d3274dc397c3281", "http://localhost:1337/parse/");
    }

    private String getMetadataForField(String field){
        ApplicationInfo ai = null;
        try {
            ai = this.getPackageManager().getApplicationInfo(
                    this.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }

        if (ai == null || ai.metaData == null) {
            return "";
        }

        Object value = ai.metaData.get(field);
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }

    public static Context getWAHContext(){
        return applicationContext;
    }

    private void configureGoogleAnalytics() {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker(getString(R.string.google_analytics_tracker_id)); // Replace with actual tracker/property Id
        tracker.enableAutoActivityTracking(true);
    }

//   private void configureCrittercism() {
//        Crittercism.initialize(getApplicationContext(), getString(R.string.crittercism_app_key));
//        if(ParseUser.getCurrentUser()!=null) {
//            Crittercism.setUsername(ParseUser.getCurrentUser().getUsername() + "(" + ParseUser.getCurrentUser().getEmail() + ")");
//        }
//    }

//    private void   configureDebugUtils() {
//        DebugUtils.getInstance(this).initialize();
//    }
//
//    private void configureLocalytics(){
//        registerActivityLifecycleCallbacks(
//                new LocalyticsActivityLifecycleCallbacks(this));
//    }
}
