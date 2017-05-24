package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by Teja on 03/06/15.
 */
@ParseClassName("Note")
public class Note extends ParseObject{

    private static String CONTENT = "content";
    private static String LOCATION = "location";
    private static String LOCATION_TEXT = "locationText";
    private static String PLACE_ID = "placeId";

    public String getContent(){
        return getString(CONTENT);
    }

    public void setContent(String content){
        put(CONTENT,content);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint(LOCATION);
    }

    public void setLocation(ParseGeoPoint geoPoint){
        put(LOCATION,geoPoint);
    }

    public String getLocationText(){
        return getString(LOCATION_TEXT);
    }

    public void setLocationText(String locationText){
        put(LOCATION_TEXT,locationText);
    }

    public void setPlaceId(String placeId){
        put(PLACE_ID,placeId);
    }
}
