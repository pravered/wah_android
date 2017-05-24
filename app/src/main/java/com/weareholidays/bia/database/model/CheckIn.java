package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class CheckIn {
    private double locationLat;
    private double locationLong;
    private String name;
    private String locationText;
    private String placeId;
    private String photoReference;
    private String mapImageLocalUri;
    private String mapImageRemoteUri;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public String getMapImageLocalUri() {
        return mapImageLocalUri;
    }

    public void setMapImageLocalUri(String mapImageLocalUri) {
        this.mapImageLocalUri = mapImageLocalUri;
    }

    public String getMapImageRemoteUri() {
        return mapImageRemoteUri;
    }

    public void setMapImageRemoteUri(String mapImageRemoteUri) {
        this.mapImageRemoteUri = mapImageRemoteUri;
    }
}
