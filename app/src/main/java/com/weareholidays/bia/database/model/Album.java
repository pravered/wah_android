package com.weareholidays.bia.database.model;

import java.util.List;

/**
 * Created by shankar on 12/5/17.
 */

public class Album {
    
    private String content;
    private int mediaCount;
    private int publicMediaCount;
    private double locationLat;
    private double locationLong;
    private String locationText;
    private long startTime;
    private long endTime;
    private String source;
    private String category;
    private List<Media> media;

    public Album() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public int getPublicMediaCount() {
        return publicMediaCount;
    }

    public void setPublicMediaCount(int publicMediaCount) {
        this.publicMediaCount = publicMediaCount;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(double locationLong) {
        this.locationLong = locationLong;
    }
}
