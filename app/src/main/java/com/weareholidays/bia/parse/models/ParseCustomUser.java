package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by Teja on 07/06/15.
 */
@ParseClassName("_User")
public class ParseCustomUser extends ParseUser {

    public static String NAME = "name";
    public static String USERNAME = "username";
    public static String PHONE = "phone";
    public static String PLACE = "place";
    public static String GENDER = "gender";
    public static String PROFILE_IMAGE = "profileImage";
    public static String FEATURE_IMAGE = "featureImage";
    public static String TOTAL_TRIPS = "totalTrips";
    public static String TOTAL_PUBLISHED_TRIPS = "totalPublishedTrips";
    public static String SHARER = "sharer";

    public static String FACEBOOK_ID = "facebookId";

    public JSONObject getAuthData(){
        try {
            Field field = ParseUser.class.getDeclaredField("authData");
            field.setAccessible(true);
            Object value = field.get(this);
            field.setAccessible(false);

            if (value == null) {
                return null;
            } else if (JSONObject.class.isAssignableFrom(value.getClass())) {
                return (JSONObject) value;
            }
            throw new RuntimeException("Wrong value");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName(){
        return getString(NAME);
    }

    public void setName(String name){
        put(NAME,name);
    }

    public String getGender(){
        return getString(GENDER);
    }

    public void setGender(String gender){
        put(GENDER,gender);
    }

    public String getPhone(){
        return getString(PHONE);
    }

    public void setPhone(String phone){
        put(PHONE,phone);
    }

    public String getPlace() {
        return getString(PLACE);
    }

    public void setPlace(String place){
        put(PLACE,place);
    }

    public ParseFile getProfileImage(){
        return getParseFile(PROFILE_IMAGE);
    }

    public void setProfileImage(ParseFile parseFile){
        put(PROFILE_IMAGE,parseFile);
    }

    public ParseFile getFeatureImage(){
        return getParseFile(FEATURE_IMAGE);
    }

    public void setFeatureImage(ParseFile parseFile){
        put(FEATURE_IMAGE,parseFile);
    }

    public int getTotalTrips(){
        return getInt(TOTAL_TRIPS);
    }

    public int getTotalPublishedTrips(){
        return getInt(TOTAL_PUBLISHED_TRIPS);
    }

    public void addTrips(){
        increment(TOTAL_TRIPS);
    }

    public void addPublishedTrips(){
        increment(TOTAL_PUBLISHED_TRIPS);
    }

    public void removePublishedTrips(){
        increment(TOTAL_PUBLISHED_TRIPS,-1);
    }

    public String getFacebookId() {
        return getString(FACEBOOK_ID);
    }

    public void setFacebookId(String facebookId) {
        put(FACEBOOK_ID,facebookId);
    }

    public void deletedTrip(boolean published){
        if(published)
            increment(TOTAL_PUBLISHED_TRIPS, -1);
        increment(TOTAL_TRIPS, -1);
    }

    public static ParseCustomUser getCurrentUser(){
        return (ParseCustomUser) ParseUser.getCurrentUser();
    }

    public String getProfileUrl(){
        ParseFile userProfileImg = getProfileImage();
        if (userProfileImg != null) {
            return  userProfileImg.getUrl();
        }
        return "";
    }

    public String getSharer() {
       return getString(SHARER);
    }

    public void setSharer(String sharer) {
        put(SHARER,sharer);
    }
}
