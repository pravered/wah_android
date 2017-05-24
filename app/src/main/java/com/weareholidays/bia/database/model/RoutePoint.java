package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class RoutePoint {
    private static int highAccuracy = 1;
    private long recordedTime;
    private int priority;
    private double locationLat;
    private double locationLong;
    private String source;
    private Day day;
    private Trip trip;
    private int dayOrder;

    public RoutePoint() {
    }

    public static int getHighAccuracy() {
        return highAccuracy;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static void setHighAccuracy(int highAccuracy) {
        RoutePoint.highAccuracy = highAccuracy;
    }

    public long getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(long recordedTime) {
        this.recordedTime = recordedTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(double locationLong) {
        this.locationLong = locationLong;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public int getDayOrder() {
        return dayOrder;
    }

    public void setDayOrder(int dayOrder) {
        this.dayOrder = dayOrder;
    }
}
