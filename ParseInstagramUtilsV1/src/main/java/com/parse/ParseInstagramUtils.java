package com.parse;

import android.content.Context;
import android.text.TextUtils;

import com.parse.instagram.Instagram;

import bolts.Task;

/**
 * Created by Teja on 10-06-2015.
 */
public class ParseInstagramUtils {

    private static InstagramAuthenticationProvider provider;
    private static Instagram instagram;
    private static boolean isInitialized;
    private static String CALLBACK_URL = ":8888/parse/";

    private static InstagramAuthenticationProvider getAuthenticationProvider() {
        if(provider == null) {
            provider = new InstagramAuthenticationProvider(getInstagram());
            ParseUser.registerAuthenticationProvider(provider);
        }

        return provider;
    }

    public static Instagram getInstagram() {
        if(instagram == null) {
            instagram = new Instagram(CALLBACK_URL);
        }

        return instagram;
    }

    public static void initialize(String consumerKey, String consumerSecret, String callBackUrl) {
        CALLBACK_URL = callBackUrl;
        getInstagram().setConsumerKey(consumerKey).setConsumerSecret(consumerSecret);
        getAuthenticationProvider();
        isInitialized = true;
    }

    public static void initialize(String consumerKey, String consumerSecret) {
        initialize(consumerKey,consumerSecret,CALLBACK_URL);
    }

    public static boolean isLinked(ParseUser user) {
        if(user.isLinked(InstagramAuthenticationProvider.AUTH_TYPE)){
            return true;
        }

        //Work around for Auth Data restriction
        if(user.has("instagram_auth")){
            provider.restoreAuthentication(user.getJSONObject("instagram_auth"));
            if(!TextUtils.isEmpty(getInstagram().getUserId())){
                return true;
            }
        }
        return false;
    }


    private static void checkInitialization() {
        if(!isInitialized) {
            throw new IllegalStateException("You must call ParseTwitterUtils.initialize() before using ParseTwitterUtils");
        }
    }

    public static Task<Void> linkInBackground(Context context, ParseUser user) {
        checkInitialization();
        return getAuthenticationProvider().setContext(context).linkAsync(user);
    }

    public static void link(ParseUser user, Context context, SaveCallback callback) {
        ParseTaskUtils.callbackOnMainThreadAsync(linkInBackground(context, user), callback, true);
    }
}
