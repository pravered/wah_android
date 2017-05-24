package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class Day {
    private DaySummary daySummary;
    private String name;
    private int displayOrder;
    private long startTime;
    private long endTime;
    private double locationLat;
    private double locationLong;
    private String city;
    private String country;

    public DaySummary getDaySummary() {
        return daySummary;
    }

    public void setDaySummary(DaySummary daySummary) {
        this.daySummary = daySummary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
