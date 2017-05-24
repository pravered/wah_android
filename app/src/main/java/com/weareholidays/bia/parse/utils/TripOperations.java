package com.weareholidays.bia.parse.utils;

import android.net.Uri;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;

import java.util.List;

/**
 * Created by Teja on 23-06-2015.
 */
public interface TripOperations {

    String TRIP_KEY_ARG = "TRIP_KEY_ARG";

    String CURRENT_TRIP_ID = "CURRENT_TRIP_ID";

    String TRIP_ALBUM_ARG = "TRIP_ALBUM_ARG";

    String TRIP_MEDIA_ARG = "TRIP_MEDIA_ARG";

    Trip getTrip();

    boolean isTripLoaded();

    boolean isTripAvailable();

    List<Timeline> getDayTimeLines(int dayOrder, int limit, int skip, String source, String contentType) throws ParseException;

    List<Timeline> getDayTimeLines(Day day, int limit, int skip, String source, String contentType) throws ParseException;

    List<Timeline> getTripTimeLines(int limit, int skip, String source, String contentType) throws ParseException;

    List<Media> getAlbumMedia(Album album) throws ParseException;

    List<RoutePoint> getTripRoutePoints(int skip, int limit) throws ParseException;

    List<RoutePoint> getDayRoutePoints(int dayOrder,int skip) throws ParseException;

    List<RoutePoint> getDayRoutePoints(Day day, int skip, int limit) throws ParseException;

    Timeline getTimeLine();

    RoutePoint getLatestRoutePoint()  throws ParseException;

    void setSelectedPhotosList(List<GalleryImage> galleryImages);

    void setTimeLine(Timeline timeline);

    List<GalleryImage> getSelectedPhotosList();

    void setSelectedPhoto(GalleryImage galleryImage);

    GalleryImage getSelectedPhoto();

    void loadTrip();

    void clearAll() throws ParseException;

    String getTripKey();

    void populateMediaSource(List<Media> mediaList) throws ParseException;

    boolean canWrite();

    void publish() throws ParseException;

    void unpublish() throws ParseException;

    void getTripPeople(TripAsyncCallback<List<PeopleContact>> tripAsyncCallback);

    void getTripFeatureImage(TripAsyncCallback<String> callback);

    boolean listenForUpdates();

    void updateTripImage(Uri image) throws ParseException;

    void deleteTimeLine(Timeline timeline) throws ParseException;

    void deleteTimelineOnly(Timeline timeline) throws ParseException;

    void deleteMedia(Timeline timeline, Media media) throws ParseException;

    void saveTripPeople(List<PeopleContact> peopleContacts) throws ParseException;

    void setSelectedMedia(Media media);

    Media getSelectedMedia();

    void save(ParseObject parseObject) throws ParseException;

    void saveAll(List<? extends ParseObject> parseObjectList) throws ParseException;

    int getMenuLayout();

    Timeline loadTimeline(String timelineId);

    void saveSelectedMediaChanges(boolean isPrivate, List<String> tags, String caption, String address, ParseGeoPoint parseGeoPoint) throws ParseException;

    void saveSelectedMediaLocation(String address, ParseGeoPoint parseGeoPoint);

    void saveSelectedMediaAddress(String address, Media media, Timeline timeline);

    void setSelectedNote(Timeline note);

    Timeline getSelectedNote();

    void saveNote(String note, String address, ParseGeoPoint parseGeoPoint,boolean sync) throws ParseException;
}
