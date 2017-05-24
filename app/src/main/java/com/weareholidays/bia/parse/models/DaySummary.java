package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Teja on 02/06/15.
 */
@ParseClassName("DaySummary")
public class DaySummary extends ParseObject {

    private static String TWITTER = "twitter";
    private static String FB = "fb";
    private static String INSTAGRAM = "instagram";
    private static String PHOTOS = "photos";
    private static String PUBLIC_PHOTOS = "publicPhotos";
    private static String NOTES =  "notes";
    private static String VIDEOS = "videos";
    private static String CHECK_INS = "checkIns";
    private static String DISTANCE = "distance";

    public int getTwitter() {
        return getInt(TWITTER);
    }

    public void addTwitter(int amount) {
        increment(TWITTER, amount);
    }

    public int getFacebook() {
        return getInt(FB);
    }

    public void addFacebook(int amount) {
        increment(FB, amount);
    }

    public int getInstagram() {
        return getInt(INSTAGRAM);
    }

    public void addInstagram(int instagram) {
        increment(INSTAGRAM, instagram);
    }

    public int getPhotos() {
        return getInt(PHOTOS);
    }

    public void addPhotos(int photos) {
        increment(PHOTOS, photos);
    }

    public int getPublicPhotos() {
        return getInt(PUBLIC_PHOTOS);
    }

    public void addPublicPhotos(int photos) {
        increment(PUBLIC_PHOTOS, photos);
    }

    public int getNotes() {
        return getInt(NOTES);
    }

    public void addNotes(int notes) {
        increment(NOTES, notes);
    }

    public int getVideos() {
        return getInt(VIDEOS);
    }

    public void addVideos(int videos) {
        increment(VIDEOS, videos);
    }

    public int getCheckIns() {
        return getInt(CHECK_INS);
    }

    public void addCheckIns(int checkIns) {
        increment(CHECK_INS, checkIns);
    }

    public double getDistance(){
        return getDouble(DISTANCE);
    }

    public void setDistance(double distance){
        put(DISTANCE,distance);
    }

    public void init(){
        put(FB,0);
        put(INSTAGRAM,0);
        put(TWITTER,0);
        put(CHECK_INS,0);
        put(NOTES,0);
        put(PHOTOS,0);
        put(VIDEOS,0);
        setDistance(0);
    }
}
