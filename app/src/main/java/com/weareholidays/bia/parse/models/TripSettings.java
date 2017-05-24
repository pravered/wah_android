package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Teja on 02/06/15.
 */
@ParseClassName("TripSettings")
public class TripSettings extends ParseObject {

    private static String FACEBOOK = "facebook";
    private static String CAMERA_ROLL_SYNC = "cameraRollSync";
    private static String FACEBOOK_SYNC = "facebookSync";
    private static String TWITTER_SYNC = "twitterSync";
    private static String INSTAGRAM_SYNC = "instagramSync";
    private static String TWITTER = "twitter";
    private static String INSTAGRAM = "instagram";
    private static String LOCATION = "location";
    private static String SYNC = "sync";
    private static String CHECK_IN = "checkIn";
    private static String CAMERA_ROLL = "camera";


    public boolean isFacebook(){
        return getBoolean(FACEBOOK);
    }

    public void setFacebook(boolean facebook){
        put(FACEBOOK,facebook);
    }

    public boolean isTwitter(){
        return getBoolean(TWITTER);
    }

    public void setTwitter(boolean twitter){
        put(TWITTER,twitter);
    }

    public boolean isInstagram(){
        return getBoolean(INSTAGRAM);
    }

    public void setInstagram(boolean instagram){
        put(INSTAGRAM,instagram);
    }

    public boolean isCameraRoll(){
        return getBoolean(CAMERA_ROLL);
    }

    public void setCameraRoll(boolean cameraRoll){
        put(CAMERA_ROLL,cameraRoll);
    }

    public boolean isLocation(){
        return getBoolean(LOCATION);
    }

    public void setLocation(boolean location){
        put(LOCATION,location);
    }

    public boolean isCheckIn(){
        return getBoolean(CHECK_IN);
    }

    public void setCheckIn(boolean checkIn){
        put(CHECK_IN,checkIn);
    }

    public boolean isSync(){
        return getBoolean(SYNC);
    }

    public void setSync(boolean sync){
        put(SYNC,sync);
    }

    public Date getFacebookSyncTime(){
        return getDate(FACEBOOK_SYNC);
    }

    public void setFacebookSyncTime(Date time){
        put(FACEBOOK_SYNC,time);
    }

    public long getTwitterSinceId(){
        return getLong(TWITTER_SYNC);
    }

    public void setTwitterSinceId(long sinceId){
        put(TWITTER_SYNC,sinceId);
    }

    public Date getInstagramSyncTime(){
        return getDate(INSTAGRAM_SYNC);
    }

    public void setInstagramSyncTime(Date time){
        put(INSTAGRAM_SYNC,time);
    }

    public Date getCameraRollSyncTime(){
        return getDate(CAMERA_ROLL_SYNC);
    }

    public void setCameraRollSyncTime(Date time){
        put(CAMERA_ROLL_SYNC,time);
    }
}
