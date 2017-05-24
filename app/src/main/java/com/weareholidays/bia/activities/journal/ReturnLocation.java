package com.weareholidays.bia.activities.journal;

import android.location.Location;

/**
 * Created by challa on 23/6/15.
 */
public class ReturnLocation {
    private Location location;
    private boolean networkPresent;
    private String addressString;
    private String reference;
    private String category;
    private String photoReference;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isNetworkPresent() {
        return networkPresent;
    }

    public void setNetworkPresent(boolean networkPresent) {
        this.networkPresent = networkPresent;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }
}
