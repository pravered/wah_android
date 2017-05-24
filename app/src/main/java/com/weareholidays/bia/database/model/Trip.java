package com.weareholidays.bia.database.model;

import java.util.List;

/**
 * Created by shankar on 12/5/17.
 */

public class Trip {
    private String name;
    private double startLocationLat;
    private double startLocationLong;
    private double endLocationLat;
    private double endLocationLong;
    private long startTime;
    private long endTime;
    private boolean isUploaded;
    private boolean isFinished;
    private boolean isPublished;
    private long publishTime;
    private boolean isDeleted;
    private TripSettings settings;
    private List<Day> days;
    private String featureImageLocalUri;
    private String featureImageRemoteUri;
    private User tripOwner;
    private int noOfDays;
    private String createdAt;
    private int viewCount;
    private TripSummary summary;
    private String secretKey;
    private int featured;
    private boolean isHidden;
    private Coupon coupon;

    public Trip() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStartLocationLat() {
        return startLocationLat;
    }

    public void setStartLocationLat(double startLocationLat) {
        this.startLocationLat = startLocationLat;
    }

    public double getStartLocationLong() {
        return startLocationLong;
    }

    public void setStartLocationLong(double startLocationLong) {
        this.startLocationLong = startLocationLong;
    }

    public double getEndLocationLat() {
        return endLocationLat;
    }

    public void setEndLocationLat(double endLocationLat) {
        this.endLocationLat = endLocationLat;
    }

    public double getEndLocationLong() {
        return endLocationLong;
    }

    public void setEndLocationLong(double endLocationLong) {
        this.endLocationLong = endLocationLong;
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

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public TripSettings getSettings() {
        return settings;
    }

    public void setSettings(TripSettings settings) {
        this.settings = settings;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public String getFeatureImageLocalUri() {
        return featureImageLocalUri;
    }

    public void setFeatureImageLocalUri(String featureImageLocalUri) {
        this.featureImageLocalUri = featureImageLocalUri;
    }

    public String getFeatureImageRemoteUri() {
        return featureImageRemoteUri;
    }

    public void setFeatureImageRemoteUri(String featureImageRemoteUri) {
        this.featureImageRemoteUri = featureImageRemoteUri;
    }

    public User getTripOwner() {
        return tripOwner;
    }

    public void setTripOwner(User tripOwner) {
        this.tripOwner = tripOwner;
    }

    public int getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(int noOfDays) {
        this.noOfDays = noOfDays;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public TripSummary getSummary() {
        return summary;
    }

    public void setSummary(TripSummary summary) {
        this.summary = summary;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getFeatured() {
        return featured;
    }

    public void setFeatured(int featured) {
        this.featured = featured;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }
}
