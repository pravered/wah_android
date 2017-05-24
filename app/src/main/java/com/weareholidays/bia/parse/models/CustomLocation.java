package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by challa on 22/5/15.
 */

@ParseClassName("CustomLocation")
public class CustomLocation extends ParseObject{
    private static String NAME = "name";
    private static String DISTANCE = "distance";
    private static String CATEGORY = "category";
    private static String PHOTOREFERENCE = "photoReference";
    private static String REFERENCE = "reference";
    private static String GEOPOINT = "geoPoint";

    public ParseGeoPoint getGeoPoint(){
        return getParseGeoPoint(GEOPOINT);
    }

    public void setGeopPoint(double latitude, double longitude){
        ParseGeoPoint myPoint  = new ParseGeoPoint(latitude, longitude);
        put(GEOPOINT, myPoint);
    }

    public double getDistance() {
        return getDouble(DISTANCE);
    }

    public void setDistance(double distance) {
        put(DISTANCE, distance);
    }

    public void setName(String name){
        put(NAME, name);
    }

    public String getName(){
        return getString(NAME);
    }

    public List<String> getCategory() {
        return getList(CATEGORY);
    }

    public void setCategory(List<String> category) {
        put(CATEGORY, category);
    }

    public String getReference() {
        return getString(REFERENCE);
    }

    public void setReference(String reference) {
        put(REFERENCE, reference);
    }

    public String getPhotoReference() {
        return getString(PHOTOREFERENCE);
    }

    public void setPhotoReference(String photoReference) {
        put(PHOTOREFERENCE, photoReference);
    }

    @Override
    public String toString(){
        return getName();
    }
}
