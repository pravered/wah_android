package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Teja on 02/06/15.
 */
@ParseClassName("Day")
public class Day extends ParseObject {
    public static String DAY_SUMMARY = "daySummary";
    private static String NAME = "name";
    private static String DISPLAY_ORDER = "displayOrder";
    private static String START_TIME = "startTime";
    private static String END_TIME = "endTime";
    private static String LOCATION = "location";
    private static String CITY = "startCity";
    private static String COUNTRY = "startCountry";

    public int getDisplayOrder(){
        return getInt(DISPLAY_ORDER);
    }

    public void setDisplayOrder(int displayOrder){
        put(DISPLAY_ORDER,displayOrder);
    }

    public String getName(){
        return getString(NAME);
    }

    public void setName(String name){
        put(NAME,name);
    }

    public DaySummary getDaySummary(){
        return (DaySummary) getParseObject(DAY_SUMMARY);
    }

    public void setDaySummary(DaySummary daySummary){
        put(DAY_SUMMARY,daySummary);
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

    public boolean hasEnded(){
        return getDate(END_TIME) != null;
    }

    public String getCity() {
        return getString(CITY);
    }

    public void setCity(String city) {
        put(CITY,city);
    }

    public String getCountry() {
        return getString(COUNTRY);
    }

    public void setCountry(String country) {
        put(COUNTRY,country);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint(LOCATION);
    }

    public void setLocation(ParseGeoPoint location){
        put(LOCATION,location);
    }
}
