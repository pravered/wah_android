package com.weareholidays.bia.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.photo.EditPhotoDetailsActivity;
import com.weareholidays.bia.activities.journal.photo.PhotoDetailsActivity;
import com.weareholidays.bia.activities.journal.photo.PhotoTimelineActivity;
import com.weareholidays.bia.activities.journal.trip.TripActivity;
import com.weareholidays.bia.activities.journal.views.CheckInActivity;
import com.weareholidays.bia.activities.journal.views.DistanceActivity;
import com.weareholidays.bia.activities.journal.views.DurationMapActivity;
import com.weareholidays.bia.activities.journal.views.FbActivity;
import com.weareholidays.bia.activities.journal.views.InstActivity;
import com.weareholidays.bia.activities.journal.views.NotesActivity;
import com.weareholidays.bia.activities.journal.views.PhotoActivity;
import com.weareholidays.bia.activities.journal.views.TimelineEditActivity;
import com.weareholidays.bia.activities.journal.views.TwitterActivity;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.TripOperations;

/**
 * Created by Teja on 16/07/15.
 */
public class NavigationUtils {

    public static final String BACK_HANDLER_TYPE = "BACK_HANDLER_TYPE";

    public static final String NORMAL_BACK_HANDLER = "NORMAL_BACK_HANDLER";

    public static final String NAV_UTILS_BACK_HANDLER = "NAV_UTILS_BACK_HANDLER";

    public static final String TRIP_ACTIVITY = "TRIP_ACTIVITY";

    public static final String TRIP_CHILD_REDIRECT_ACTIVITY = "TRIP_CHILD_REDIRECT_ACTIVITY";

    public static final String SHOW_ALBUM_VIEW = "SHOW_ALBUM_VIEW";

    public static final String TRIP_DAY_PAGE = "TRIP_DAY_PAGE";

    public static final String TIMELINE_POSITION = "TIMELINE_POSITION";

    public static Intent getDiscoverTripIntent(Context context, String tripKey){
        Intent intent = new Intent(context, TripActivity.class);
        intent.putExtra(TripOperations.TRIP_KEY_ARG,tripKey);
        intent.putExtra(BACK_HANDLER_TYPE,NORMAL_BACK_HANDLER);
        intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY, TripActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getSearchTripIntent(Context context, String tripKey){
        Intent intent = new Intent(context, TripActivity.class);
        intent.putExtra(TripOperations.TRIP_KEY_ARG,tripKey);
        intent.putExtra(BACK_HANDLER_TYPE,NORMAL_BACK_HANDLER);
        intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,TripActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getViewAllTripIntent(Context context, String tripKey){
        Intent intent = new Intent(context, TripActivity.class);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NORMAL_BACK_HANDLER);
        intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,TripActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getTimelineEditIntent(Context context, String tripKey, Intent parentIntent){
        Intent intent = new Intent(context, TimelineEditActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY));
        }
        else{
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,context.getClass());
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        return intent;
    }

    public static Intent getPhotoViewIntent(Context context, String tripKey, Intent parentIntent){
        Intent intent = new Intent(context, PhotoTimelineActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY));
        }
        else{
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,context.getClass());
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        return intent;
    }

    public static Intent getPhotoViewFromAlbumIntent(Context context, String tripKey, Intent parentIntent){
        Intent intent = new Intent(context, PhotoTimelineActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY));
        }
        else{
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,context.getClass());
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE,NAV_UTILS_BACK_HANDLER);
        intent.putExtra(SHOW_ALBUM_VIEW,true);
        return intent;
    }

    public static boolean handleBackNavigation(Intent parentIntent){
        if(parentIntent != null && NAV_UTILS_BACK_HANDLER.equals(parentIntent.getStringExtra(BACK_HANDLER_TYPE)))
            return true;
        return false;
    }

    public static boolean handleTripBackNavigation(Intent parentIntent){
        if(parentIntent != null && NAV_UTILS_BACK_HANDLER.equals(parentIntent.getStringExtra(BACK_HANDLER_TYPE))
                && parentIntent.hasExtra(TRIP_ACTIVITY))
            return true;
        return false;
    }

    public static Intent photoViewBackIntent(Context context, Intent parentIntent, String tripKey, Timeline timeline){
        Intent intent = new Intent(context, HomeActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(SHOW_ALBUM_VIEW)){
            return getTimelineEditIntent(context,tripKey,parentIntent);
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            Class<?> cls = (Class<?>)parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY);
            intent = new Intent(context, cls);
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
            intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        }
        intent.putExtra(TRIP_DAY_PAGE, timeline.getDayOrder()+1);
        intent.putExtra(TIMELINE_POSITION, timeline.getDisplayOrder());
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        return intent;
    }

    public static Intent albumViewBackIntent(Context context, Intent parentIntent, String tripKey, Timeline timeline){
        Intent intent = new Intent(context, HomeActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            Class<?> cls = (Class<?>)parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY);
            intent = new Intent(context, cls);
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
            intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        }
        intent.putExtra(TRIP_DAY_PAGE, timeline.getDayOrder()+1);
        intent.putExtra(TIMELINE_POSITION, timeline.getDisplayOrder());
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        return intent;
    }

    public static Intent getOpenPhotoEditIntent(Context context, String tripKey, Intent parentIntent){
        Intent intent = new Intent(context, EditPhotoDetailsActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY));
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        if(parentIntent != null && parentIntent.hasExtra(SHOW_ALBUM_VIEW))
            intent.putExtra(SHOW_ALBUM_VIEW,true);
        return intent;
    }

    public static Intent getClosePhotoEditIntent(Context context, String tripKey, Intent parentIntent){
        Intent intent = new Intent(context, PhotoTimelineActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY));
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        if(parentIntent != null && parentIntent.hasExtra(SHOW_ALBUM_VIEW))
            intent.putExtra(SHOW_ALBUM_VIEW,true);
        return intent;
    }

    public static Intent getOpenPhotoInfoIntent(Context context, String tripKey, Intent parentIntent){
        Intent intent = new Intent(context, PhotoDetailsActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY));
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        if(parentIntent != null && parentIntent.hasExtra(SHOW_ALBUM_VIEW))
            intent.putExtra(SHOW_ALBUM_VIEW,true);
        return intent;
    }

    public static Intent getClosePhotoInfoIntent(Context context, String tripKey, Intent parentIntent){
        Intent intent = new Intent(context, PhotoTimelineActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_CHILD_REDIRECT_ACTIVITY)){
            intent.putExtra(TRIP_CHILD_REDIRECT_ACTIVITY,parentIntent.getSerializableExtra(TRIP_CHILD_REDIRECT_ACTIVITY));
        }
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            intent.putExtra(TRIP_ACTIVITY,parentIntent.getSerializableExtra(TRIP_ACTIVITY));
        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        if(parentIntent != null && parentIntent.hasExtra(SHOW_ALBUM_VIEW))
            intent.putExtra(SHOW_ALBUM_VIEW,true);
        return intent;
    }

    public static boolean showJournalInHome(Intent intent){
        if(intent != null && intent.hasExtra(TRIP_DAY_PAGE)){
            return true;
        }
        return false;
    }

    public static int getTripDayFromIntent(Intent intent){
        if(intent != null && intent.hasExtra(TRIP_DAY_PAGE)){
            return intent.getIntExtra(TRIP_DAY_PAGE,0);
        }
        return 0;
    }

    public static int getTimelineFromIntent(Intent intent){
        if(intent != null && intent.hasExtra(TIMELINE_POSITION)){
            return intent.getIntExtra(TIMELINE_POSITION,-1);
        }
        return -1;
    }

    public static void openAnimation(Activity activity){
        try{
            int openEnterAnim = animateTransition(android.R.attr.activityOpenEnterAnimation,activity);
            int openExitAnim = animateTransition(android.R.attr.activityOpenExitAnimation, activity);
            if(openEnterAnim >= 0 && openExitAnim >= 0){
                activity.overridePendingTransition(openEnterAnim,openExitAnim);
            }
            activity.finish();
        } catch (Exception e){

        }
    }

    private static int animateTransition(int animAttributeId,Activity activity)
    {
        TypedValue animations = new TypedValue();
        Resources.Theme theme = activity.getTheme();

        theme.resolveAttribute(android.R.attr.windowAnimationStyle, animations, true);
        TypedArray animationArray = activity.obtainStyledAttributes(animations.resourceId,
                new int[] {animAttributeId});

        int animResId = animationArray.getResourceId(0, 0);
        animationArray.recycle();
        return animResId;
    }

    public static void closeAnimation(Activity activity){
        try{
            int openEnterAnim = animateTransition(android.R.attr.activityCloseEnterAnimation,activity);
            int openExitAnim = animateTransition(android.R.attr.activityCloseExitAnimation, activity);
            if(openEnterAnim >= 0 && openExitAnim >= 0){
                activity.overridePendingTransition(openEnterAnim,openExitAnim);
            }
            activity.finish();
        } catch (Exception e){

        }
    }

    public static Intent getDistanceIntent(Context context,String tripKey){
        Intent intent = new Intent(context, DistanceActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static Intent getDurationIntent(Context context,String tripKey){
        Intent intent = new Intent(context, DurationMapActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static Intent getCheckInIntent(Context context,String tripKey){
        Intent intent = new Intent(context, CheckInActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static Intent getPhotoIntent(Context context,String tripKey){
        Intent intent = new Intent(context, PhotoActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static Intent getFbIntent(Context context,String tripKey){
        Intent intent = new Intent(context, FbActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static Intent getTwitterIntent(Context context,String tripKey){
        Intent intent = new Intent(context, TwitterActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static Intent getInstIntent(Context context,String tripKey){
        Intent intent = new Intent(context, InstActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static Intent getNotesIntent(Context context,String tripKey){
        Intent intent = new Intent(context, NotesActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(BACK_HANDLER_TYPE, NAV_UTILS_BACK_HANDLER);
        intent.putExtra(TRIP_ACTIVITY,context.getClass());
        return intent;
    }

    public static  Intent getTripInent(Context context, String tripKey,Intent parentIntent){
        Intent intent = new Intent(context, HomeActivity.class);
        if(parentIntent != null && parentIntent.hasExtra(TRIP_ACTIVITY)){
            Class<?> cls = (Class<?>)parentIntent.getSerializableExtra(TRIP_ACTIVITY);
            intent = new Intent(context, cls);
        }
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripKey);
        intent.putExtra(HomeActivity.SHOW_TAB,HomeActivity.JOURNAL_TAB);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }
}


