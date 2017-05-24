package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Teja on 07/06/15.
 */
@ParseClassName("RoutePoints")
public class RoutePoint extends ParseObject {

    public static int HIGH_ACCURACY = 1;

    public static String RECORDED_TIME = "recordedTime";
    private static String PRIORITY = "priority";
    private static String LOCATION = "location";
    private static String SOURCE = "source";
    public static String DAY = "day";
    public static String TRIP = "trip";
    public static String DAY_ORDER = "dayOrder";

    public RoutePoint(double lat,double log,int y,int m,int d,int h,int min,int s) {
        ParseGeoPoint x = new ParseGeoPoint(lat, log);
        setLocation(x);
        Calendar cal = Calendar.getInstance();
        cal.set(y,m,d,h,min,s);
        Date date = cal.getTime();
        setRecordedTime(date);
    }

    public RoutePoint(){

    }


    public Date getRecordedTime(){
        return getDate(RECORDED_TIME);
    }

    public void setRecordedTime(Date date){
        put(RECORDED_TIME,date);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint(LOCATION);
    }

    public void setLocation(ParseGeoPoint geoPoint){
        put(LOCATION,geoPoint);
    }

    public String getSource(){
        return getString(SOURCE);
    }

    public void setSource(String source){
        put(SOURCE,source);
    }

    public void setDay(Day day){
        put(DAY,day);
        put(DAY_ORDER,day.getDisplayOrder());
    }

    public int getPriority(){
        return getInt(PRIORITY);
    }

    public void setPriority(int priority){
        put(PRIORITY,priority);
    }

    public void setTrip(Trip trip){
        put(TRIP,trip);
    }

}
