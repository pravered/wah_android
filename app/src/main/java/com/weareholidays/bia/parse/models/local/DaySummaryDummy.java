package com.weareholidays.bia.parse.models.local;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by challa on 9/7/15.
 */

@ParseClassName("DaySummaryDummy")
public class DaySummaryDummy extends ParseObject {
    private double distance;
    private int photos;
    private int checkins;
    private int dayOrder;

    public DaySummaryDummy(){

    }

    public DaySummaryDummy(double distance, int photos, int checkins) {
        this.distance = distance;
        this.photos = photos;
        this.checkins = checkins;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getPhotos() {
        return photos;
    }

    public void setPhotos(int photos) {
        this.photos = photos;
    }

    public int getCheckins() {
        return checkins;
    }

    public void setCheckins(int checkins) {
        this.checkins = checkins;
    }

    public int getDayOrder() {
        return dayOrder;
    }

    public void setDayOrder(int dayOrder) {
        this.dayOrder = dayOrder;
    }
}
