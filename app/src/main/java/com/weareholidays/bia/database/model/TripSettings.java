package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class TripSettings {
    private boolean facebook;
    private boolean twitter;
    private boolean instagram;
    private boolean location;
    private boolean sync;
    private boolean checkIn;
    private boolean camerRoll;
    private long cameraRollSyncTime;
    private long facebookSyncTime;
    private long twitterSinceId;
    private long instagramSyncTime;

    public TripSettings() {
    }

    public boolean isFacebook() {
        return facebook;
    }

    public void setFacebook(boolean facebook) {
        this.facebook = facebook;
    }

    public boolean isTwitter() {
        return twitter;
    }

    public void setTwitter(boolean twitter) {
        this.twitter = twitter;
    }

    public boolean isInstagram() {
        return instagram;
    }

    public void setInstagram(boolean instagram) {
        this.instagram = instagram;
    }

    public boolean isLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public boolean isCheckIn() {
        return checkIn;
    }

    public void setCheckIn(boolean checkIn) {
        this.checkIn = checkIn;
    }

    public boolean isCamerRoll() {
        return camerRoll;
    }

    public void setCamerRoll(boolean camerRoll) {
        this.camerRoll = camerRoll;
    }

    public long getCameraRollSyncTime() {
        return cameraRollSyncTime;
    }

    public void setCameraRollSyncTime(long cameraRollSyncTime) {
        this.cameraRollSyncTime = cameraRollSyncTime;
    }

    public long getFacebookSyncTime() {
        return facebookSyncTime;
    }

    public void setFacebookSyncTime(long facebookSyncTime) {
        this.facebookSyncTime = facebookSyncTime;
    }

    public long getTwitterSinceId() {
        return twitterSinceId;
    }

    public void setTwitterSinceId(long twitterSinceId) {
        this.twitterSinceId = twitterSinceId;
    }

    public long getInstagramSyncTime() {
        return instagramSyncTime;
    }

    public void setInstagramSyncTime(long instagramSyncTime) {
        this.instagramSyncTime = instagramSyncTime;
    }
}
