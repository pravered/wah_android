package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class TripSummary {
    private int twitter;
    private int fb;
    private int instagram;
    private int photos;
    private int publicPhotos;
    private int notes;
    private int videos;
    private int checkIns;
    private double distance;

    public TripSummary() {
    }

    public int getTwitter() {
        return twitter;
    }

    public void setTwitter(int twitter) {
        this.twitter = twitter;
    }

    public int getFb() {
        return fb;
    }

    public void setFb(int fb) {
        this.fb = fb;
    }

    public int getInstagram() {
        return instagram;
    }

    public void setInstagram(int instagram) {
        this.instagram = instagram;
    }

    public int getPhotos() {
        return photos;
    }

    public void setPhotos(int photos) {
        this.photos = photos;
    }

    public int getPublicPhotos() {
        return publicPhotos;
    }

    public void setPublicPhotos(int publicPhotos) {
        this.publicPhotos = publicPhotos;
    }

    public int getNotes() {
        return notes;
    }

    public void setNotes(int notes) {
        this.notes = notes;
    }

    public int getVideos() {
        return videos;
    }

    public void setVideos(int videos) {
        this.videos = videos;
    }

    public int getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(int checkIns) {
        this.checkIns = checkIns;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
