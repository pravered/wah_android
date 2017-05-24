package com.weareholidays.bia.database.model;

import java.util.List;

/**
 * Created by shankar on 12/5/17.
 */

public class CustomLocation {
    private String name;
    private double distance;
    private List<String> category;
    private String photoReference;
    private String reference;
    private double geoPointLat;
    private double geoPointLong;

    public CustomLocation() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public double getGeoPointLat() {
        return geoPointLat;
    }

    public void setGeoPointLat(double geoPointLat) {
        this.geoPointLat = geoPointLat;
    }

    public double getGeoPointLong() {
        return geoPointLong;
    }

    public void setGeoPointLong(double geoPointLong) {
        this.geoPointLong = geoPointLong;
    }
}
