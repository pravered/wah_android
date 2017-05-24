package com.weareholidays.bia.parse.models.local;

import com.weareholidays.bia.parse.models.Media;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 23/07/15.
 */
public class MediaGroup {

    private List<Media> mediaList = new ArrayList<>();

    private Date startTime;

    private Date endTime;

    private ParseGeoPoint groupLocation;

    private List<MediaFrequency> mediaFrequencyList = new ArrayList<>();

    private int mediaCount;

    private int publicMediaCount;

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    public ParseGeoPoint getGroupLocation() {
        return groupLocation;
    }

    public void setGroupLocation(ParseGeoPoint groupLocation) {
        this.groupLocation = groupLocation;
    }

    public List<MediaFrequency> getMediaFrequencyList() {
        return mediaFrequencyList;
    }

    public void setMediaFrequencyList(List<MediaFrequency> mediaFrequencyList) {
        this.mediaFrequencyList = mediaFrequencyList;
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
}
