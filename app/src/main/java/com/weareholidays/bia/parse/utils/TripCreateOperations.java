package com.weareholidays.bia.parse.utils;

import android.location.Location;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.local.TripLocal;
import com.weareholidays.bia.social.facebook.models.FacebookPost;
import com.weareholidays.bia.social.instagram.models.InstagramPost;

import java.util.List;

/**
 * Created by Teja on 23-06-2015.
 */
public interface TripCreateOperations {

    void createTrip(TripLocal tripLocal) throws ParseException;

    Timeline addCheckIn(String name, ParseGeoPoint parseGeoPoint, String PhotoReference) throws ParseException;

    Timeline addNote(String notes, String locationText, ParseGeoPoint parseGeoPoint) throws ParseException;

    Timeline addPhotos(List<GalleryImage> galleryImages) throws ParseException;

    void endDay() throws ParseException;

    Day addRoutePointFromLocationTracker(Location latLng) throws ParseException;

    void addFacebookPosts(List<FacebookPost> posts) throws ParseException;

    void addTwitterPosts(List<twitter4j.Status> posts) throws java.text.ParseException, ParseException;

    void addInstagramPosts(List<InstagramPost> posts) throws ParseException;
}
