package com.weareholidays.bia.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.PREFS;

/**
 * Created by wah on 9/9/15.
 */
public class SharedPrefUtils {

    private SharedPrefUtils() {
    }

    public static class Keys {
        public static final String PREFS = "bia_pref";
        public static final String SHARER_ID = "sharer_id";
        public static final String COACH_TAB_PREF = "coach_discover_pref";
        public static final String COACH_PERMISSION_PREF = "coach_permission_pref";
        public static final String COACH_CREATE_TRIP_PREF = "coach_create_trip_pref";
        public static final String COACH_EMPTYSTATE_SUMMARY_PREF = "coach_emptystate_summary_pref";
        public static final String COACH_EMPTYSTATE_DAY_PREF = "coach_emptystate_day_pref";
        public static final String COACH_PUBLISHED_TRIP_PREF = "coach_published_trip_pref";
        public static final String COACH_MORE_TAB_PREF = "coach_more_tab_pref";
        public static final String COACH_TO_PUBLISHED_TRIP = "coach_published_trip";
        public static final String GEO_FENCE_PREF_KEY = "geofence_pref";
        public static final String GEO_FENCE_PREF_BROADCAST = "geofence_broadcast";
        public static final String COACH_NOTIFICATION = "coach_notification";
        public static final String COACH_TRIP_TAB_PREF = "coach_trip_tab";
    }

    public static String getStringPreference(Context context, String key) {
        String value = "";
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(key, "");
        }
        return value;
    }

    public static boolean getBooleanPreference(Context context, String key) {
        boolean value = false;
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null) {
            value = preferences.getBoolean(key, false);
        }
        return value;
    }

    public static float getFloatPreference(Context context, String key) {
        float value = 0;
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null) {
            value = preferences.getFloat(key, 0);
        }
        return value;
    }

    public static float getLongPreference(Context context, String key) {
        long value = 0;
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null) {
            value = preferences.getLong(key, 0);
        }
        return value;
    }

    public static int getIntPreference(Context context, String key) {
        int value = 0;
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null) {
            value = preferences.getInt(key, 0);
        }
        return value;
    }

    public static boolean setStringPreference(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            return editor.commit();
        }
        return false;
    }

    public static boolean setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            return editor.commit();
        }
        return false;
    }

    public static boolean setFloatPreference(Context context, String key, float value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(key, value);
            return editor.commit();
        }
        return false;
    }

    public static boolean setIntPreference(Context context, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            return editor.commit();
        }
        return false;
    }

    public static boolean setLongPreference(Context context, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(key, value);
            return editor.commit();
        }
        return false;
    }

    public static boolean removePreferenceByKey(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(key);
            editor.apply();
            return editor.commit();
        }
        return false;
    }

    public static boolean hasKey(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            return preferences.contains(key);
        }
        return false;
    }

    public synchronized static boolean checkAndSetSharedPref(Context context,String key){
        boolean status = getBooleanPreference(context,key);
        if(!status){
            setBooleanPreference(context,key,true);
            return true;
        }
        return false;
    }
}
