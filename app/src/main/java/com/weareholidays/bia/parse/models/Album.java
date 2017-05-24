package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 03/06/15.
 */
@ParseClassName("Album")
public class Album extends ParseObject{

    private static String CONTENT = "content";
    private static String MEDIA_COUNT = "mediaCount";
    private static String PUBLIC_MEDIA_COUNT = "publicMediaCount";
    private static String LOCATION = "Location";
    private static String LOCATION_TEXT = "locationText";
    public static String START_TIME = "startTime";
    public static String END_TIME = "endTime";
    public static String SOURCE = "source";
    public static String CATEGORY = "category";


    private List<Media> media;

    public void setCategory(String category){
        put(CATEGORY, category);
    }

    public List<String> getCategory(){
        return getList(CATEGORY);
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public int getMediaCount(){
        return getInt(MEDIA_COUNT);
    }

    public void addMediaCount(int mediaCount){
        increment(MEDIA_COUNT,mediaCount);
    }

    public int getPublicMediaCount(){
        return getInt(PUBLIC_MEDIA_COUNT);
    }

    public void addPublicMediaCount(int mediaCount){
        increment(PUBLIC_MEDIA_COUNT,mediaCount);
    }

    public String getContent(){
        return getString(CONTENT);
    }

    public void setContent(String content){
        put(CONTENT,content);
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

    public Date getStartTime(){
        return getDate(START_TIME);
    }

    public void setStartTime(Date date){
        put(START_TIME, date);
    }

    public Date getEndTime(){
        return getDate(END_TIME);
    }

    public void setEndTime(Date date){
        put(END_TIME, date);
    }

    public String getSource(){
        return getString(SOURCE);
    }

    public void setSource(String source){
        put(SOURCE,source);
    }
}
