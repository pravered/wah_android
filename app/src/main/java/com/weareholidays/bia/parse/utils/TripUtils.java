package com.weareholidays.bia.parse.utils;

import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Notification;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.local.MediaFrequency;
import com.weareholidays.bia.parse.models.local.MediaGroup;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Teja on 03/06/15.
 */
public class TripUtils {

    private static final String TAG = TripUtils.class.toString();

    private static TripUtils tripUtils;

    private Map<String,TripOperations> tripOperationsList =  new HashMap<>();

    private TripUtils(){
        tripOperationsList.put(TripOperations.CURRENT_TRIP_ID,new TripLocalOperations());
    }

    public TripOperations getTripOperations(String id){
        return tripOperationsList.get(id);
    }

    public TripOperations getCurrentTripOperations(){
        return tripOperationsList.get(TripOperations.CURRENT_TRIP_ID);
    }

    public TripOperations loadServerViewTrip(String trip_key){
        TripServerOperations operations = new TripServerOperations(trip_key,false);
        operations.loadTrip();
        tripOperationsList.put(trip_key,operations);
        return operations;
    }

    public TripOperations loadServerFullTrip(String trip_key){
        TripServerOperations operations = new TripServerOperations(trip_key,true);
        operations.loadTrip();
        tripOperationsList.put(trip_key,operations);
        return operations;
    }

    public static TripUtils getInstance(){
        if(tripUtils == null){
            tripUtils = new TripUtils();
        }
        return tripUtils;
    }

    //Static Helper Methods

    public static final String DAY_ORDER_FOR_INTENT = "CURRENT_TRIP_DAY_ORDER_FOR_INTENT";

    public static ParseException createDayNotFoundException(int dayOrder){
        return new ParseException(ParseException.OBJECT_NOT_FOUND,String.format("Day not found for the dayOrder : %d in current trip",dayOrder));
    }

    public static ParseException createDayNotFoundException(Calendar time){
        return new ParseException(ParseException.OBJECT_NOT_FOUND,String.format("Day not found for time : " + time.toString()));
    }

    public static Day getDayFromTime(List<Day> days, Calendar time){
        for(Day day: days){
            if(doesTimeFallsUnderDay(day,time))
                return day;
        }
        return null;
    }

    public static boolean doesTimeFallsUnderDay(Day day, Calendar time){
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = null;
        startTime.setTime(day.getStartTime());
        if(day.getEndTime() != null){
            endTime = Calendar.getInstance();
            endTime.setTime(day.getEndTime());
        }
        return doesTimeFallsUnderDay(startTime,endTime,time);
    }

    public static boolean doesTimeFallsUnderDay(Calendar startTime, Calendar endTime, Calendar time){
        if(startTime.before(time) && (endTime == null || endTime.after(time))){
            return true;
        }
        return false;
    }

    public static ParseQuery<Trip> getFeaturedTrips(){
        return ParseQuery.getQuery(Trip.class).whereEqualTo(Trip.PUBLISHED, true).whereNotEqualTo(Trip.DELETED,true).include(Trip.TRIP_OWNER).include(Trip.DAYS)
                .orderByDescending(Trip.VIEW_COUNT);
    }

    public static ParseQuery<Trip> searchTrips(String searchText){
        return ParseQuery.getQuery(Trip.class).whereMatches(Trip.NAME, searchText).whereEqualTo(Trip.PUBLISHED, true).whereNotEqualTo(Trip.DELETED, true).include(Trip.TRIP_OWNER).include(Trip.DAYS)
                .orderByDescending(Trip.VIEW_COUNT);
    }

    public static ParseQuery<Trip> getFeaturedTripsViewAll(){
        return ParseQuery.getQuery(Trip.class).whereEqualTo(Trip.PUBLISHED, true).whereNotEqualTo(Trip.DELETED, true).include(Trip.TRIP_OWNER).include(Trip.DAYS)
                /*.addDescendingOrder(Trip.CREATED_AT)*/.addDescendingOrder(Trip.VIEW_COUNT);
    }

    public static ParseQuery<Trip> getTripsPopular(){
        return ParseQuery.getQuery(Trip.class).whereEqualTo(Trip.PUBLISHED, true).whereNotEqualTo(Trip.HIDDEN, true).whereNotEqualTo(Trip.DELETED, true).whereGreaterThan(Trip.FEATURED, 0).include(Trip.TRIP_OWNER).include(Trip.DAYS)
                .addAscendingOrder(Trip.FEATURED).addDescendingOrder(Trip.VIEW_COUNT);
    }

    public static ParseQuery<Trip> getTripsRecentlyPublished(){
        return ParseQuery.getQuery(Trip.class).whereEqualTo(Trip.PUBLISHED, true).whereNotEqualTo(Trip.HIDDEN, true).whereNotEqualTo(Trip.DELETED, true).include(Trip.TRIP_OWNER).include(Trip.DAYS)
                .addDescendingOrder(Trip.PUBLISH_TIME).addDescendingOrder(Trip.VIEW_COUNT);
    }

    public static ParseQuery<Trip> getTripsRecentlyTravelled(){
        return ParseQuery.getQuery(Trip.class).whereEqualTo(Trip.PUBLISHED, true).whereNotEqualTo(Trip.HIDDEN, true).whereNotEqualTo(Trip.DELETED, true).include(Trip.TRIP_OWNER).include(Trip.DAYS)
                .addDescendingOrder(Trip.END_TIME).addDescendingOrder(Trip.VIEW_COUNT);
    }

    public static ParseQuery<Trip> getUserPublishedTripsViewAll(ParseCustomUser customUser){
        return ParseQuery.getQuery(Trip.class).whereNotEqualTo(Trip.DELETED, true).whereEqualTo(Trip.TRIP_OWNER, customUser).whereEqualTo(Trip.PUBLISHED, true)
                .include(Trip.DAYS).addDescendingOrder(Trip.CREATED_AT);
    }

    public static ParseQuery<Trip> getCurrentUserTripsViewAll(){
        return ParseQuery.getQuery(Trip.class).whereNotEqualTo(Trip.DELETED, true).whereEqualTo(Trip.TRIP_OWNER, ParseUser.getCurrentUser()).whereEqualTo(Trip.UPLOADED, true)
                .include(Trip.DAYS).addDescendingOrder(Trip.CREATED_AT);
    }

    public static ParseQuery<Notification> getNotificationViewAll() {
        return ParseQuery.getQuery(Notification.class).whereEqualTo(Notification.USERNAME, ParseUser.getCurrentUser().getUsername()).include(Notification.NOTIFIER).orderByDescending(Notification.CONTENT_TIME);
    }

    public static ParseQuery<Notification> getNotification() {
        return ParseQuery.getQuery(Notification.class).whereEqualTo(Notification.USERNAME, ParseUser.getCurrentUser().getUsername()).whereNotEqualTo(Notification.ID_DELETED,true).include(Notification.NOTIFIER).orderByDescending(Notification.CONTENT_TIME);
    }

    public static ParseQuery<ParseCustomUser> getFeaturedUsers(){
        return ParseQuery.getQuery(ParseCustomUser.class)
                .whereGreaterThan(ParseCustomUser.TOTAL_PUBLISHED_TRIPS, 0)
                .addDescendingOrder(ParseCustomUser.TOTAL_PUBLISHED_TRIPS);
    }

    public static ParseQuery<ParseCustomUser> getFeaturedUsersViewAll(){
        return ParseQuery.getQuery(ParseCustomUser.class).whereGreaterThan(ParseCustomUser.TOTAL_PUBLISHED_TRIPS, 0)
                .orderByAscending(ParseCustomUser.NAME);
    }

    public static ParseQuery<ParseCustomUser> searchUsers(String searchText){
        return ParseQuery.getQuery(ParseCustomUser.class)
                .whereGreaterThan(ParseCustomUser.TOTAL_PUBLISHED_TRIPS, 0)
                .whereMatches(ParseCustomUser.NAME, searchText);
    }

    private static ParseCustomUser selectedUser;

    public static ParseCustomUser getSelectedUser() {
        return selectedUser;
    }

    public static void setSelectedUser(ParseCustomUser selectedUser) {
        TripUtils.selectedUser = selectedUser;
    }

    public static void addMediaToFrequency(MediaGroup mediaGroup, Media existingMedia){
        boolean addedToFrequency = false;

        if(existingMedia.getLocation() == null)
            return;

        for(MediaFrequency mediaFrequency : mediaGroup.getMediaFrequencyList()){

            if(mediaFrequency.getMedia().getLocation().distanceInKilometersTo(existingMedia.getLocation()) <= 0.1){
                if(TextUtils.isEmpty(mediaFrequency.getMedia().getAddress()) && !TextUtils.isEmpty(existingMedia.getAddress())){
                    mediaFrequency.getMedia().setAddress(existingMedia.getAddress());
                }
                mediaFrequency.setFrequency(mediaFrequency.getFrequency() + 1);
                addedToFrequency = true;
            }
        }

        if(!addedToFrequency){
            MediaFrequency mediaFrequency = new MediaFrequency();
            mediaFrequency.setMedia(existingMedia);
            mediaFrequency.setFrequency(1);
            mediaGroup.getMediaFrequencyList().add(mediaFrequency);
        }
    }

    public static void addLocationToAlbum(Album album, MediaGroup mediaGroup){
        String address = "";
        ParseGeoPoint mediaLocation = null;

        int freq = 0;

        for(MediaFrequency mediaFrequency: mediaGroup.getMediaFrequencyList()){
            if(mediaFrequency.getFrequency() > freq){
                freq = mediaFrequency.getFrequency();
                mediaLocation = mediaFrequency.getMedia().getLocation();
                address = mediaFrequency.getMedia().getAddress();
            }
        }

        if(!TextUtils .isEmpty(address))
            album.setLocationText(address);

        if(mediaLocation != null)
            album.setLocation(mediaLocation);
    }
}
