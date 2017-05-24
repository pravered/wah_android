package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 21-05-2015.
 */
@ParseClassName("Trip")
public class Trip extends ParseObject {

    public static String NAME = "name";
    private static String START_LOCATION = "startLoc";
    private static String END_LOCATION = "endLoc";
    private static String START_TIME = "startTime";
    public static String END_TIME = "endTime";
    public static String UPLOADED = "uploaded";
    public static String FINISHED = "finished";
    public static String PUBLISHED = "published";
    public static String PUBLISH_TIME = "publishTime";
    public static String DELETED = "deleted";
    public static String SETTINGS = "settings";
    public static String DAYS = "days";
    public static String FEATURE_IMAGE = "featureImage";
    public static String TRIP_OWNER = "owner";
    public static String TRIP_DAYS_NUMBER = "noDays";
    public static String CREATED_AT = "createdAt";
    public static String VIEW_COUNT = "views";
    public static String SUMMARY = "summary";
    public static String SECRET_KEY = "secretKey";
    public static String FEATURED = "featured";
    public static String HIDDEN = "hidden";
    public static String COUPON = "coupon";

    private boolean viewed = false;

    public void setHidden(boolean hidden){
        put(HIDDEN, hidden);
    }

    public boolean isHidden(){
        return getBoolean(HIDDEN);
    }

    public String getName(){
        String name = getString(NAME);
        String cname = name; //name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        return cname;
    }

    public void setName(String name){
        String cname = name; //name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        put(NAME, cname);
    }

    public ParseGeoPoint getStartLocation(){
        return getParseGeoPoint(START_LOCATION);
    }

    public void setStartLocation(ParseGeoPoint geoPoint){
        put(START_LOCATION,geoPoint);
    }

    public ParseGeoPoint getEndLocation(){
        return getParseGeoPoint(END_LOCATION);
    }

    public void setEndLocation(ParseGeoPoint geoPoint){
        put(END_LOCATION,geoPoint);
    }

    public Date getStartTime(){
        return getDate(START_TIME);
    }

    public void setStartTime(Date date){
        put(START_TIME,date);
    }

    public Date getEndTime(){
        return getDate(END_TIME);
    }

    public void setEndTime(Date date){
        put(END_TIME,date);
    }

    public boolean isUploaded(){
        return getBoolean(UPLOADED);
    }

    public void setUploaded(boolean completed){
        put(UPLOADED,completed);
    }

    public TripSettings getSettings(){
        return  (TripSettings)getParseObject(SETTINGS);
    }

    public void setSettings(TripSettings tripSettings){
        put(SETTINGS,tripSettings);
    }

    public TripSummary getSummary(){
        return  (TripSummary)getParseObject(SUMMARY);
    }

    public void setSummary(TripSummary tripSummary){
        put(SUMMARY,tripSummary);
    }

    public List<Day> getDays(){
        return getList(DAYS);
    }

    public void addDay(Day day){
        addUnique(DAYS,day);
        increment(TRIP_DAYS_NUMBER);
    }

    public ParseFile getFeatureImage(){
        return getParseFile(FEATURE_IMAGE);
    }

    public void setFeatureImage(ParseFile parseFile){
        put(FEATURE_IMAGE, parseFile);
    }

    public boolean isPublished(){
        return getBoolean(PUBLISHED);
    }

    public void setPublished(boolean published){
        put(PUBLISHED,published);
    }

    public Date getPublishTime(){
        return getDate(PUBLISH_TIME);
    }

    public void setPublishTime(Date date){
        put(PUBLISH_TIME,date);
    }

    public Day getDay(int displayOrder){
        for(Day day: getDays()){
            if(day.getDisplayOrder() == displayOrder)
                return day;
        }
        return null;
    }

    public ParseCustomUser getOwner(){
        return (ParseCustomUser)getParseObject(TRIP_OWNER);
    }

    public void setOwner(ParseUser user){
        put(TRIP_OWNER,user);
    }

    public void setDeleted(boolean deleted){
        put(DELETED,deleted);
    }

    public int getTotalDays(){
        return getInt(TRIP_DAYS_NUMBER);
    }

    public void setFinished(boolean uploaded){
        put(FINISHED,uploaded);
    }

    public boolean isFinished(){
        return getBoolean(FINISHED);
    }

    public String getSecretKey(){
        return getString(SECRET_KEY);
    }

    public void setSecretKey(String secretKey){
        put(SECRET_KEY, secretKey);
    }

    public int getFeatured(){
        return getInt(FEATURED);
    }

    public void addView(){
        if(!viewed){
            increment(VIEW_COUNT);
            saveEventually();
            viewed = true;
        }
    }

    public Coupon getCoupon(){
        return  (Coupon)getParseObject(COUPON);
    }

    public void setCoupon(Coupon coupon){
        put(COUPON,coupon);
    }

/**
   //parcelable classes should implement the following methods
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeValue(getName());
        parcel.writeValue(getStartLocation());
        parcel.writeValue(getEndLocation());
        parcel.writeValue(getStartTime());
        parcel.writeValue(getEndTime());
        parcel.writeValue(isPublic());
        parcel.writeValue(isUploaded());
        parcel.writeValue(getFeatureImage());
    }

    public static final Parcelable.Creator<Trip> CREATOR = new Creator<Trip>() {
        public Trip createFromParcel(Parcel source) {
            Trip mTrip = new Trip();
            mTrip.setName((String) source.readValue(String.class.getClassLoader()));
            mTrip.setStartLocation((ParseGeoPoint) source.readValue(ParseGeoPoint.class.getClassLoader()));
            mTrip.setEndLocation((ParseGeoPoint) source.readValue(ParseGeoPoint.class.getClassLoader()));
            mTrip.setStartTime((Date) source.readValue(Date.class.getClassLoader()));
            mTrip.setEndTime((Date) source.readValue(Date.class.getClassLoader()));
            mTrip.setPublic((boolean) source.readValue(boolean.class.getClassLoader()));
            mTrip.setUploaded((boolean) source.readValue(boolean.class.getClassLoader()));
            mTrip.setFeatureImage((ParseFile) source.readValue(ParseFile.class.getClassLoader()));
            return mTrip;
        }
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
**/
}
