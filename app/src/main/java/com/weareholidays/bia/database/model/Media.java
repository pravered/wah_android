package com.weareholidays.bia.database.model;

import java.util.List;

/**
 * Created by shankar on 12/5/17.
 */

public class Media {

    private String contentLocalUrl;
    private String contentRemoteUrl;
    private String caption;
    private List<String> mediaTags;
    private double locationLat;
    private double locationLong;
    private boolean isPrivate;
    private Album album;
    private String address;
    private long contentCreationTime;
    private long contentSize;
    private int mediaWidth;
    private int mediaHeight;
    private String thirdPartyId;
    private String thirdPartyUrl;
    private String mediaSource;
    private boolean fetchingAddress;

    public Media() {
    }

    public String getContentLocalUrl() {
        return contentLocalUrl;
    }

    public void setContentLocalUrl(String contentLocalUrl) {
        this.contentLocalUrl = contentLocalUrl;
    }

    public String getContentRemoteUrl() {
        return contentRemoteUrl;
    }

    public void setContentRemoteUrl(String contentRemoteUrl) {
        this.contentRemoteUrl = contentRemoteUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<String> getMediaTags() {
        return mediaTags;
    }

    public void setMediaTags(List<String> mediaTags) {
        this.mediaTags = mediaTags;
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

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getContentCreationTime() {
        return contentCreationTime;
    }

    public void setContentCreationTime(long contentCreationTime) {
        this.contentCreationTime = contentCreationTime;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

    public int getMediaWidth() {
        return mediaWidth;
    }

    public void setMediaWidth(int mediaWidth) {
        this.mediaWidth = mediaWidth;
    }

    public int getMediaHeight() {
        return mediaHeight;
    }

    public void setMediaHeight(int mediaHeight) {
        this.mediaHeight = mediaHeight;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public String getThirdPartyUrl() {
        return thirdPartyUrl;
    }

    public void setThirdPartyUrl(String thirdPartyUrl) {
        this.thirdPartyUrl = thirdPartyUrl;
    }

    public String getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    public boolean isFetchingAddress() {
        return fetchingAddress;
    }

    public void setFetchingAddress(boolean fetchingAddress) {
        this.fetchingAddress = fetchingAddress;
    }
}
