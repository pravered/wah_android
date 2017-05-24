package com.weareholidays.bia.social.instagram.models;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 22/06/15.
 */
public class InstagramPost {

    private static String TAG = "InstagramPost";

    private String id;
    private String message;
    private Date createdTime;
    private Type type;
    private String from;
    private LatLng location;
    private String locationText;
    private String locationId;
    private List<InstagramMedia> media;

    public enum Type{
        PHOTO,VIDEO
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public List<InstagramMedia> getMedia() {
        return media;
    }

    public void setMedia(List<InstagramMedia> media) {
        this.media = media;
    }

    public static InstagramPost parsePost(JSONObject jsonObject){
        InstagramPost post = new InstagramPost();
        try {
            post.setId(jsonObject.getString("id"));
            post.setFrom(jsonObject.getJSONObject("user").getString("id"));
            Calendar createdTime = Calendar.getInstance();
            createdTime.setTimeInMillis(jsonObject.getLong("created_time") * 1000);
            post.setCreatedTime(createdTime.getTime());
            if(!jsonObject.isNull("caption")){
                JSONObject caption = jsonObject.getJSONObject("caption");
                if(caption.has("text"))
                    post.setMessage(caption.getString("text"));
            }
            String type = jsonObject.getString("type");
            if("image".equals(type)){
                post.setType(Type.PHOTO);
            }
            else if("video".equals(type)){
                post.setType(Type.VIDEO);
            }

            if(!jsonObject.isNull("location")){
                JSONObject place = jsonObject.getJSONObject("location");
                if(place.has("latitude") && place.has("longitude")){
                    LatLng latlng = new LatLng(place.getDouble("latitude"),place.getDouble("longitude"));
                    post.setLocation(latlng);
                }
                if(place.has("id"))
                    post.setLocationId(place.getString("id"));
                if(place.has("name"))
                    post.setLocationText(place.getString("name"));
            }

            if(!jsonObject.isNull("images")){
                List<InstagramMedia> media = new ArrayList<>();
                JSONObject images = jsonObject.getJSONObject("images");
                media.add(InstagramMedia.parsePostMedia(images));
                post.setMedia(media);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error while parsing post", e);
        }
        return post;
    }

    public static List<InstagramPost> parsePosts(JSONArray postsArray){
        List<InstagramPost> posts = new ArrayList<>();
        try {
            for(int j = 0; j < postsArray.length(); j++){
                JSONObject pj = postsArray.getJSONObject(j);
                posts.add(parsePost(pj));
            }
        }
        catch (Exception e){
            Log.e(TAG,"Error while parsing posts",e);
        }
        return posts;
    }

    public static List<InstagramPost> parsePosts(JSONObject postsData){
        List<InstagramPost> posts = new ArrayList<>();
        try {
            return parsePosts(postsData.getJSONArray("data"));
        }
        catch (Exception e){
            Log.e(TAG,"Error while parsing posts",e);
        }
        return posts;
    }
}
