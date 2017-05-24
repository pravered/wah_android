package com.weareholidays.bia.social.instagram.models;

import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Teja on 22/06/15.
 */
public class InstagramMedia {
    private static String TAG = "FacebookMedia";

    private Type type;
    private String id;
    private int mediaHeight;
    private int mediaWidth;
    private String mediaSource;
    private Date createdTime;

    public enum Type{
        PHOTO,VIDEO
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

    public static InstagramMedia parsePostMedia(JSONObject images){
        InstagramMedia media = new InstagramMedia();
        try {
            if(!images.isNull("standard_resolution")){
                JSONObject standard_image = images.getJSONObject("standard_resolution");
                media.setMediaSource(standard_image.getString("url"));
                media.setMediaHeight(standard_image.getInt("height"));
                media.setMediaWidth(standard_image.getInt("width"));
                media.setType(Type.PHOTO);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error while parsing post", e);
        }
        return media;
    }
}
