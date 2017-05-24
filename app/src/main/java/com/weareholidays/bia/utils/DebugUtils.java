package com.weareholidays.bia.utils;

import android.content.Context;
import android.util.Log;

import com.crittercism.app.Crittercism;
import com.google.android.gms.drive.internal.CreateFileIntentSenderRequest;
import com.parse.ParseUser;
import com.weareholidays.bia.BuildConfig;
import com.weareholidays.bia.R;

/**
 * Created by wah on 27/8/15.
 */
public class DebugUtils {

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = BuildConfig.APPLICATION_ID;
    private static DebugUtils debugUtils;
    private static Context mContext;

    public static DebugUtils getInstance(Context context) {
        if (debugUtils == null) {
            mContext = context;
            debugUtils = new DebugUtils();
        }
        return debugUtils;
    }

    public void initialize() {
//        Crittercism.initialize(mContext, mContext.getResources().getString(R.string.crittercism_app_key));
        if (ParseUser.getCurrentUser() != null) {
            Crittercism.setUsername(ParseUser.getCurrentUser().getUsername() + "(" + ParseUser.getCurrentUser().getEmail() + ")");
        }
    }

    /*public static void setUserName(String username){
        Crittercism.setUsername(username);
    }*/

    public static void logException(Exception e){
        Crittercism.logHandledException(e);
    }

    public static void logException(Exception e,String msg){
        Crittercism.logHandledException(e);
        Log.e(TAG, msg);
    }

    public static void LogD(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }

    public static void LogE(String msg){
        if(DEBUG)
            Log.e(TAG, msg);
    }

    public static void LogV(String msg){
        if(DEBUG)
            Log.v(TAG, msg);
    }

    public static void LogW(String msg){
        if (DEBUG)
            Log.w(TAG, msg);
    }
}
