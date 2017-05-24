package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by challa on 17/6/15.
 */
@ParseClassName("Notification")
public class Notification extends ParseObject{
    public static String CONTENT_TIME = "contentTime";
    public static String IS_READ = "isRead";
    private static String CONTENT = "content";
    public static String USERNAME = "username";
    public static String ACTION_TYPE = "actionType";
    public static String ACTION_PARAMS = "actionParams";
    public static String NOTIFIER = "notifier";
    public static String ID_DELETED = "isDelete";

    public static String TRIP_OPEN_ACTION = "TRIP_OPEN_ACTION";
    public static String ALBUM_IMAGE_OPEN_ACTION = "ALBUM_IMAGE_OPEN_ACTION";
    public static String ARTICLE_OPEN_ACTION = "ARTICLE_OPEN_ACTION";

    public Boolean getIsDeleted() {
        return getBoolean(ID_DELETED);
    }

    public void setIsDeleted(Boolean isDeleted) {
        put(ID_DELETED, isDeleted);
    }

    public Boolean getIsRead() {
        return getBoolean(IS_READ);
    }

    public void setIsRead(Boolean isRead) {
        put(IS_READ, isRead);
    }
    public Date getContentTime() {
        return getDate(CONTENT_TIME);
    }

    public void setContentTime(Date contentTime) {
        put(CONTENT_TIME, contentTime);
    }

    public String getContent() {
        return getString(CONTENT);
    }

    public void setContent(String content) {
        put(CONTENT, content);
    }

    public String getUsername() {
        return getString(USERNAME);
    }

    public void setUser(String username) {
        put(USERNAME, username);
    }

    public ParseCustomUser getNotifier(){
        return (ParseCustomUser)getParseObject(NOTIFIER);
    }

    public void setNotifier(ParseCustomUser notifier){
        put(NOTIFIER, notifier);
    }

    public String getActionType() {
        return getString(ACTION_TYPE);
    }

    public void setActionType(String actionType) {
        put(ACTION_TYPE, actionType);
    }

    public String getActionParams() {
        return getString(ACTION_PARAMS);
    }

    public void setActionParams(String actionParams) {
        put(ACTION_PARAMS, actionParams);
    }

    private String userProfileImage;

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

}
