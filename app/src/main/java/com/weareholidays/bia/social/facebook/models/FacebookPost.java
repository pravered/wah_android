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
 * Created by Teja on 06/06/15.
 */
public class FacebookPost {

    private static String TAG = "FacebookPost";

    private String id;
    private String message;
    private String story;
    private Date createdTime;
    private Date updateTime;
    private Type type;
    private String from;
    private LatLng location;
    private String locationText;
    private String locationId;
    private List<FacebookMedia> media;

    public FacebookPost(){

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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public List<FacebookMedia> getMedia() {
        return media;
    }

    public void setMedia(List<FacebookMedia> media) {
        this.media = media;
    }

    public enum Type{
        LINK, PHOTO,STATUS,VIDEO,OFFER
    }

    public static FacebookPost parsePost(JSONObject jsonObject){
        FacebookPost post = new FacebookPost();
        try {
            post.setId(jsonObject.getString("id"));
            post.setFrom(jsonObject.getJSONObject("from").getString("id"));
            Calendar createdTime = Calendar.getInstance();
            createdTime.setTimeInMillis(jsonObject.getLong("created_time") * 1000);
            post.setCreatedTime(createdTime.getTime());
            Calendar updatedTime = Calendar.getInstance();
            updatedTime.setTimeInMillis(jsonObject.getLong("updated_time") * 1000);
            post.setUpdateTime(updatedTime.getTime());
            if(jsonObject.has("message"))
                post.setMessage(jsonObject.getString("message"));
            if(jsonObject.has("story"))
                post.setStory(jsonObject.getString("story"));

            String type = jsonObject.getString("type");
            if("link".equals(type)){
                post.setType(Type.LINK);
            }
            else if("photo".equals(type)){
                post.setType(Type.PHOTO);
            }
            else if("status".equals(type)){
                post.setType(Type.STATUS);
            }
            else if("video".equals(type)){
                post.setType(Type.VIDEO);
            }

            if(!jsonObject.isNull("place")){
                JSONObject place = jsonObject.getJSONObject("place");
                post.setLocationId(place.getString("id"));
                post.setLocationText(place.getString("name"));

                if(!place.isNull("location")){
                    JSONObject location = place.getJSONObject("location");
                    LatLng latlng = new LatLng(location.getDouble("latitude"),location.getDouble("longitude"));
                    post.setLocation(latlng);
                }
            }

        } catch (Exception e) {
            Log.e(TAG,"Error while parsing post",e);
        }
        return post;
    }

    public static List<FacebookPost> parsePosts(JSONArray postsArray){
        List<FacebookPost> posts = new ArrayList<>();
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

    public static List<FacebookPost> parsePosts(JSONObject postsData){
        List<FacebookPost> posts = new ArrayList<>();
        try {
            return parsePosts(postsData.getJSONArray("data"));
        }
        catch (Exception e){
            Log.e(TAG,"Error while parsing posts",e);
        }
        return posts;
    }
}
