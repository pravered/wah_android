package com.weareholidays.bia.social.facebook.models;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 07/06/15.
 */
public class FacebookMedia {

    private static String TAG = "FacebookMedia";

    private Type type;
    private String id;
    private int mediaHeight;
    private int mediaWidth;
    private String mediaSource;
    private Date createdTime;
    private String locationId;
    private String locationText;
    private  LatLng location;
    private String caption;

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMediaHeight() {
        return mediaHeight;
    }

    public void setMediaHeight(int mediaHeight) {
        this.mediaHeight = mediaHeight;
    }

    public int getMediaWidth() {
        return mediaWidth;
    }

    public void setMediaWidth(int mediaWidth) {
        this.mediaWidth = mediaWidth;
    }

    public String getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public enum Type{
        PHOTO,VIDEO,ALBUM
    }

    public static FacebookMedia parsePostMedia(JSONObject jsonObject){
        FacebookMedia media = new FacebookMedia();
        try {
            String type = jsonObject.getString("type");
            if("photo".equals(type)){
                media.setType(Type.PHOTO);
            }
            else if("video".equals(type)){
                media.setType(Type.VIDEO);
            }
            else if("album".equals(type)){
                media.setType(Type.ALBUM);
            }

            if(!jsonObject.isNull("media")){
                JSONObject md = jsonObject.getJSONObject("media");
                if(md.has("image")){
                    JSONObject img = md.getJSONObject("image");
                    media.setMediaSource(img.getString("src"));
                    media.setMediaHeight(img.getInt("height"));
                    media.setMediaWidth(img.getInt("width"));
                }
            }

            if(!jsonObject.isNull("target")){
                JSONObject target = jsonObject.getJSONObject("target");
                media.setId(target.getString("id"));
            }


        } catch (Exception e) {
            Log.e(TAG, "Error while parsing post media", e);
        }
        return media;
    }

    public static List<FacebookMedia> parsePostMedia(JSONArray postsArray){
        List<FacebookMedia> media = new ArrayList<>();
        try {
            for(int j = 0; j < postsArray.length(); j++){
                JSONObject pj = postsArray.getJSONObject(j);
                media.add(parsePostMedia(pj));
            }
        }
        catch (Exception e){
            Log.e(TAG, "Error while parsing post media", e);
        }
        return media;
    }

    public static FacebookMedia parseImageMedia(JSONObject jsonObject){
        FacebookMedia media = new FacebookMedia();
        try {
            media.setType(Type.PHOTO);
            Calendar createdTime = Calendar.getInstance();
            createdTime.setTimeInMillis(jsonObject.getLong("created_time") * 1000);
            media.setCreatedTime(createdTime.getTime());

            media.setId(jsonObject.getString("id"));

            //Empty String to avoid null pointers
            media.setLocationText("");
            media.setCaption("");

            if(!jsonObject.isNull("place")){
                JSONObject place = jsonObject.getJSONObject("place");
                media.setLocationId(place.getString("id"));
                media.setLocationText(place.getString("name"));

                if(!place.isNull("location")){
                    JSONObject location = place.getJSONObject("location");
                    LatLng latlng = new LatLng(location.getDouble("latitude"),location.getDouble("longitude"));
                    media.setLocation(latlng);
                }
            }

            if(!jsonObject.isNull("name")){
                media.setCaption(jsonObject.getString("name"));
            }

            if(!jsonObject.isNull("images")){
                JSONArray jsonArray = jsonObject.getJSONArray("images");
                if(jsonArray.length() > 0){
                    JSONObject imageObject = jsonArray.getJSONObject(0);
                    media.setMediaHeight(imageObject.getInt("height"));
                    media.setMediaWidth(imageObject.getInt("width"));
                    media.setMediaSource(imageObject.getString("source"));
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error while parsing image media", e);
        }
        return media;
    }

    public static List<FacebookMedia> parseImageMedia(JSONArray postsArray){
        List<FacebookMedia> media = new ArrayList<>();
        try {
            for(int j = 0; j < postsArray.length(); j++){
                JSONObject pj = postsArray.getJSONObject(j);
                media.add(parseImageMedia(pj));
            }
        }
        catch (Exception e){
            Log.e(TAG, "Error while parsing image media", e);
        }
        return media;
    }

    public static List<FacebookMedia> parseImageMediaList(JSONObject postsData){
        List<FacebookMedia> posts = new ArrayList<>();
        try {
            return parseImageMedia(postsData.getJSONArray("data"));
        }
        catch (Exception e){
            Log.e(TAG,"Error while parsing image media",e);
        }
        return posts;
    }
}
