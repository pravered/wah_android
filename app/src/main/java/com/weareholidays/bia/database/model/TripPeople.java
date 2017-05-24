package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class TripPeople {
    private String phoneBookType;
    private String facebookType;
    private String name;
    private String email;
    private Trip trip;
    private String imageLocalUri;
    private String imageRemoteUri;
    private String type;
    private boolean inTrip;
    private String identifier;

    public TripPeople() {
    }

    public String getPhoneBookType() {
        return phoneBookType;
    }

    public void setPhoneBookType(String phoneBookType) {
        this.phoneBookType = phoneBookType;
    }

    public String getFacebookType() {
        return facebookType;
    }

    public void setFacebookType(String facebookType) {
        this.facebookType = facebookType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getImageLocalUri() {
        return imageLocalUri;
    }

    public void setImageLocalUri(String imageLocalUri) {
        this.imageLocalUri = imageLocalUri;
    }

    public String getImageRemoteUri() {
        return imageRemoteUri;
    }

    public void setImageRemoteUri(String imageRemoteUri) {
        this.imageRemoteUri = imageRemoteUri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInTrip() {
        return inTrip;
    }

    public void setInTrip(boolean inTrip) {
        this.inTrip = inTrip;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
