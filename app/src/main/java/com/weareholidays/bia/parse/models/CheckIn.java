package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by Teja on 03/06/15.
 */
@ParseClassName("CheckIn")
public class CheckIn extends ParseObject{
    private static String LOCATION = "location";
    private static String NAME = "name";
    private static String LOCATION_TEXT = "locationText";
    private static String PLACE_ID = "placeId";
    private static String PHOTO_REFERENCE = "photoReference";
    public static String MAP_IMAGE = "mapImage";


    public String getName(){
        return getString(NAME);
    }

    public void setName(String name){
        put(NAME,name);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint(LOCATION);
    }

    public void setLocation(ParseGeoPoint parseGeoPoint){
        put(LOCATION,parseGeoPoint);
    }

    public String getLocationText(){
        return getString(LOCATION_TEXT);
    }

    public void setLocationText(String locationText){
        put(LOCATION_TEXT,locationText);
    }

    public void setPhotoReference(String photoReference){
        put(PHOTO_REFERENCE, photoReference);
    }

    public String getPhotoReference(){
        return getString(PHOTO_REFERENCE);
    }

    public void setPlaceId(String placeId){
        put(PLACE_ID,placeId);
    }

    public ParseFile getMapImage(){
        return getParseFile(MAP_IMAGE);
    }
}
