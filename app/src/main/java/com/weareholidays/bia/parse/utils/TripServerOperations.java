package com.weareholidays.bia.parse.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.activities.journal.people.models.PhoneBookContact;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.DaySummary;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Note;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.TripPeople;
import com.weareholidays.bia.parse.models.TripSummary;
import com.weareholidays.bia.parse.models.local.MediaGroup;
import com.weareholidays.bia.social.facebook.models.FacebookContact;
import com.weareholidays.bia.utils.MapUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 23-06-2015.
 */
public class TripServerOperations implements TripOperations {

    private static final String TAG = "TripServerOps";
    private Timeline timeline;

    protected TripServerOperations(String tripId,boolean canWrite){
        this.tripId = tripId;
        this.canWrite = canWrite;
    }

    private String tripId;
    private Trip trip;
    private boolean tripLoaded;

    private boolean canWrite;

    private void eagerLoad(){
        if(!tripLoaded)
        {
            try {
                trip = ParseQuery.getQuery(Trip.class).include(Trip.DAYS).whereEqualTo("objectId",tripId)
                        .whereNotEqualTo(Trip.DELETED, true)
                        .include(Trip.SETTINGS).include(Trip.TRIP_OWNER).include(Trip.SUMMARY).include(Trip.DAYS + "." + Day.DAY_SUMMARY).getFirst();
                tripLoaded = true;
            } catch (ParseException e) {
                if(e != null && e.getCode() != ParseException.OBJECT_NOT_FOUND){
                    Log.w(TAG, "Error Loading current trip", e);
                }
            }
        }
    }

    @Override
    public Trip getTrip() {
        eagerLoad();
        return trip;
    }

    @Override
    public boolean isTripLoaded() {
        return tripLoaded;
    }

    @Override
    public boolean isTripAvailable() {
        eagerLoad();
        if(trip != null)
            return true;
        return false;
    }

    @Override
    public List<Timeline> getDayTimeLines(int dayOrder, int limit, int skip, String source, String contentType) throws ParseException {
        Day day = getTrip().getDay(dayOrder);
        if(day == null)
            throw TripUtils.createDayNotFoundException(dayOrder);
        return getDayTimeLines(day, limit, skip, source, contentType);
    }

    @Override
    public List<Timeline> getDayTimeLines(Day day, int limit, int skip, String source, String contentType) throws ParseException {
        ParseQuery<Timeline> returnQuery;

        returnQuery = ParseQuery.getQuery(Timeline.class).include(Timeline.CONTENT).whereEqualTo(Timeline.DAY, day)
                .orderByAscending(Timeline.DISPLAY_ORDER).setLimit(limit).setSkip(skip);
        if(source != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.SOURCE, source);
        if(contentType != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.CONTENT_TYPE, contentType);
        return returnQuery.find();
    }

    @Override
    public List<Timeline> getTripTimeLines(int limit, int skip, String source, String contentType) throws ParseException {
        ParseQuery<Timeline> returnQuery;

        returnQuery = ParseQuery.getQuery(Timeline.class).include(Timeline.CONTENT).whereEqualTo(Timeline.TRIP, getTrip())
                .addAscendingOrder(Timeline.DAY_ORDER).addAscendingOrder(Timeline.DISPLAY_ORDER).setLimit(limit).setSkip(skip);
        if(source != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.SOURCE, source);
        if(contentType != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.CONTENT_TYPE, contentType);
        return returnQuery.find();
    }

    @Override
    public List<Media> getAlbumMedia(Album album) throws ParseException {
        if(canWrite())
            return ParseQuery.getQuery(Media.class).whereEqualTo(Media.ALBUM, album).orderByAscending(Media.CONTENT_CREATION_TIME).find();
        else
            return ParseQuery.getQuery(Media.class).whereEqualTo(Media.ALBUM, album).whereNotEqualTo(Media.PRIVACY,true).orderByAscending(Media.CONTENT_CREATION_TIME).find();
    }

    @Override
    public List<RoutePoint> getDayRoutePoints(Day day, int skip, int limit) throws ParseException {
        return ParseQuery.getQuery(RoutePoint.class)
                .whereEqualTo(RoutePoint.DAY,day).setLimit(limit).setSkip(skip).orderByAscending(RoutePoint.RECORDED_TIME).find();
    }

    @Override
    public List<RoutePoint> getTripRoutePoints(int skip, int limit) throws ParseException {
        return ParseQuery.getQuery(RoutePoint.class)
                .whereEqualTo(RoutePoint.TRIP, getTrip()).setLimit(limit).setSkip(skip).addAscendingOrder(RoutePoint.DAY_ORDER).addAscendingOrder(RoutePoint.RECORDED_TIME).find();
    }

    @Override
    public Timeline getTimeLine(){
        return timeline;
    }

    @Override
    public List<RoutePoint> getDayRoutePoints(int dayOrder, int skip) throws ParseException {
        Day day = getTrip().getDay(dayOrder);
        if(day == null)
            throw TripUtils.createDayNotFoundException(dayOrder);
        return getDayRoutePoints(day, skip, 1000);
    }

    @Override
    public RoutePoint getLatestRoutePoint() throws ParseException {
        return null;
    }

    @Override
    public void setSelectedPhotosList(List<GalleryImage> galleryImages) {

    }

    @Override
    public void setTimeLine(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public List<GalleryImage> getSelectedPhotosList() {
        return null;
    }

    @Override
    public void setSelectedPhoto(GalleryImage galleryImage) {

    }

    @Override
    public GalleryImage getSelectedPhoto() {
        return null;
    }

    @Override
    public void loadTrip() {
        tripLoaded = false;
        eagerLoad();
    }

    @Override
    public void clearAll() throws ParseException {
        trip = null;
        tripLoaded = false;
    }

    @Override
    public String getTripKey() {
        return tripId;
    }

    @Override
    public void populateMediaSource(List<Media> mediaList) throws ParseException {
        for(Media media: mediaList){
            if(media.getContent() != null){
                media.setMediaSource(media.getContent().getUrl());
            }
            else{
                media.setMediaSource(media.getThirdPartyUrl());
            }
        }
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    @Override
    public void publish() throws ParseException {
        Trip trip = getTrip();
        trip.setPublished(true);
        trip.setPublishTime(Calendar.getInstance().getTime());
        trip.save();
        ParseCustomUser user = (ParseCustomUser)ParseCustomUser.getCurrentUser();
        user.addPublishedTrips();
        user.saveEventually();
    }

    @Override
    public void unpublish() throws ParseException {
        Trip trip = getTrip();
        if(!canWrite() || !trip.isPublished()){
            return;
        }
        trip.setPublished(false);
        trip.save();
        ParseCustomUser user = (ParseCustomUser)ParseCustomUser.getCurrentUser();
        user.removePublishedTrips();
        user.saveEventually();
    }

    @Override
    public void getTripPeople(final TripAsyncCallback<List<PeopleContact>> tripAsyncCallback) {
        ParseQuery.getQuery(TripPeople.class).whereEqualTo(TripPeople.TRIP,getTrip())
                .whereEqualTo(TripPeople.IN_TRIP,true).findInBackground(new FindCallback<TripPeople>() {
            @Override
            public void done(List<TripPeople> tripPeoples, ParseException e) {
                if (e == null) {
                    List<PeopleContact> tripPeopleList = new ArrayList<>();
                    if (tripPeoples != null) {
                        for (TripPeople tripPeople : tripPeoples) {
                            if (TripPeople.FACEBOOK_TYPE.equals(tripPeople.getType())) {
                                FacebookContact fbContact = new FacebookContact();
                                fbContact.setName(tripPeople.getName());
                                fbContact.setImageUri(tripPeople.getImageUrl());
                                fbContact.setId(tripPeople.getIdentifier());
                                tripPeopleList.add(fbContact);
                            } else if (TripPeople.PHONE_BOOK_TYPE.equals(tripPeople.getType())) {
                                PhoneBookContact phContact = new PhoneBookContact();
                                phContact.setContactName(tripPeople.getName());
                                phContact.setNumber(tripPeople.getIdentifier());
                                if (tripPeople.getImage() != null)
                                    phContact.setContactImagePath(tripPeople.getImage().getUrl());
                                tripPeopleList.add(phContact);
                            }
                        }
                    }
                    tripAsyncCallback.onCallBack(tripPeopleList);
                }
            }
        });
    }

    @Override
    public void getTripFeatureImage(TripAsyncCallback<String> callback) {
        Trip trip = getTrip();
        String result = "";
        if(trip.getFeatureImage() != null)
            result = trip.getFeatureImage().getUrl();
        callback.onCallBack(result);
    }

    @Override
    public boolean listenForUpdates() {
        return false;
    }

    @Override
    public void updateTripImage(Uri image) throws ParseException {
        byte[] data = ParseFileUtils.convertImageToBytes(image, WAHApplication.getWAHContext());
        ParseFile parseFile = new ParseFile(data,"trip_feature_image.jpeg");
        parseFile.save();
        getTrip().setFeatureImage(parseFile);
        getTrip().save();
    }

    private void deleteTimelineMappingHelper(Timeline timeline) throws ParseException {
        Day day = getTrip().getDay(timeline.getDayOrder());
        DaySummary daySummary = day.getDaySummary();
        TripSummary tripSummary = getTrip().getSummary();

        //TODO: see if this can be optimized
        if(Timeline.ALBUM_CONTENT.equals(timeline.getContentType())){
            List<Media> media = getAlbumMedia((Album)timeline.getContent());
            ParseObject.deleteAll(media);
        }

        if(Source.FB.equals(timeline.getSource())){
            daySummary.addFacebook(-1);
            tripSummary.addFacebook(-1);
        }
        else if(Source.INSTAGRAM.equals(timeline.getSource())){
            daySummary.addInstagram(-1);
            tripSummary.addInstagram(-1);
        }
        else if(Source.TWITTER.equals(timeline.getSource())){
            daySummary.addTwitter(-1);
            tripSummary.addTwitter(-1);
        }
        else if(Source.WAH.equals(timeline.getSource())){
            if(Timeline.CHECK_IN_CONTENT.equals(timeline.getContentType())){
                daySummary.addCheckIns(-1);
                tripSummary.addCheckIns(-1);
            }
            else if(Timeline.NOTE_CONTENT.equals(timeline.getContentType())){
                daySummary.addNotes(-1);
                tripSummary.addNotes(-1);
            }
            else if(Timeline.ALBUM_CONTENT.equals(timeline.getContentType())){
                Album album = (Album)timeline.getContent();
                daySummary.addPhotos(-album.getMediaCount());
                tripSummary.addPhotos(-album.getMediaCount());
                daySummary.addPublicPhotos(-album.getPublicMediaCount());
                tripSummary.addPublicPhotos(-album.getPublicMediaCount());
            }
        }
        daySummary.save();
        tripSummary.save();
    }

    @Override
    public void deleteTimeLine(Timeline timeline) throws ParseException{
        Day day = getTrip().getDay(timeline.getDayOrder());
        deleteTimelineMappingHelper(timeline);
        int timelineOrder = timeline.getDisplayOrder();

        List<Timeline> timeLinesList = getDayTimeLines(day,1000,timelineOrder + 1, null, null);

        for(Timeline timeline1 : timeLinesList){
            timeline1.setDisplayOrder(timeline1.getDisplayOrder() - 1);
        }

        ParseObject.saveAll(timeLinesList);

        timeline.getContent().delete();
        timeline.delete();
    }

    @Override
    public void deleteTimelineOnly(Timeline timeline) throws ParseException {
        deleteTimelineMappingHelper(timeline);

        timeline.getContent().delete();
        timeline.delete();
    }

    @Override
    public void deleteMedia(Timeline timeline, Media media) throws ParseException {
        media.delete();
        Album album = (Album)timeline.getContent();
        album.addMediaCount(-1);
        if(!media.isPrivate())
            album.addPublicMediaCount(-1);
        album.save();
        if(Source.WAH.equals(timeline.getSource())){
            DaySummary daySummary = getTrip().getDay(timeline.getDayOrder()).getDaySummary();
            if(Timeline.ALBUM_CONTENT.equals(timeline.getContentType())){
                daySummary.addPhotos(-1);
                getTrip().getSummary().addPhotos(-1);

                if(!media.isPrivate()){
                    daySummary.addPublicPhotos(-1);
                    getTrip().getSummary().addPublicPhotos(-1);
                }

                daySummary.save();
                getTrip().getSummary().save();
            }
        }
    }

    @Override
    public void saveTripPeople(List<PeopleContact> peopleContacts) throws ParseException {
        List<TripPeople> oldPeople = ParseQuery.getQuery(TripPeople.class).whereEqualTo(TripPeople.TRIP, getTrip())
                .whereEqualTo(TripPeople.IN_TRIP, true).find();
        if(oldPeople != null && oldPeople.size() > 0){
            ParseObject.deleteAll(oldPeople);
        }
        List<TripPeople> people = new ArrayList<>();
        for(PeopleContact peopleContact: peopleContacts){
            TripPeople tripPeople = new TripPeople();
            tripPeople.setName(peopleContact.getName());
            tripPeople.setInTrip(true);
            tripPeople.setIdentifier(peopleContact.getIdentifier());
            tripPeople.setTrip(getTrip());
            String imageUri = peopleContact.getImageUri();
            if(peopleContact.getType() == PeopleContact.Type.FB){
                tripPeople.setType(TripPeople.FACEBOOK_TYPE);
                if(!TextUtils.isEmpty(imageUri))
                    tripPeople.setImageUrl(peopleContact.getImageUri());
                people.add(tripPeople);
            }
            else if(peopleContact.getType() == PeopleContact.Type.PHONE){
                tripPeople.setType(TripPeople.PHONE_BOOK_TYPE);
                tripPeople.save();
                if(!TextUtils.isEmpty(imageUri)){
                    ParseFileUtils.pinFileLocally(tripPeople, TripPeople.IMAGE, Uri.parse(imageUri), "");
                    ParseFileUtils.uploadParseFile(tripPeople);
                }
            }
        }

        if(people.size() > 0)
            ParseObject.saveAll(people);
    }
    private Media selectedMedia;
    @Override
    public void setSelectedMedia(Media media) {
        selectedMedia = media;
    }

    @Override
    public Media getSelectedMedia() {
        return selectedMedia;
    }

    @Override
    public void save(ParseObject parseObject) throws ParseException {
        parseObject.save();
    }

    @Override
    public void saveAll(List<? extends ParseObject> parseObjectList) throws ParseException {
        ParseObject.saveAll(parseObjectList);
    }

    @Override
    public int getMenuLayout() {
        if(canWrite()) {
            if(getTrip().isPublished())
                return R.menu.menu_trip_published;
            else {
                return R.menu.menu_trip_completed;
            }
        }
        return R.menu.menu_trip_public;
    }

    @Override
    public Timeline loadTimeline(String timelineId) {
        try {
            Timeline timeline = ParseQuery.getQuery(Timeline.class).get(timelineId);
            setTimeLine(timeline);
            return timeline;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeline;
    }

    @Override
    public void saveSelectedMediaChanges(boolean isPrivate, List<String> tags, String caption, String address, ParseGeoPoint parseGeoPoint) throws ParseException {
        Media media = getSelectedMedia();
        Timeline timeline = getTimeLine();

        ParseGeoPoint previousGeoPoint = media.getLocation();

        boolean originalPrivacy = media.isPrivate();

        media.setCaption(caption);
        media.setAddress(address);
        media.setPrivate(isPrivate);
        media.setTags(tags);

        if(parseGeoPoint != null)
            media.setLocation(parseGeoPoint);

        media.saveEventually();

        Day day = getTrip().getDay(timeline.getDayOrder());
        DaySummary daySummary = day.getDaySummary();
        TripSummary tripSummary = getTrip().getSummary();

        Album album = (Album)timeline.getContent();

        if(originalPrivacy != isPrivate){
            if(originalPrivacy && !isPrivate)
            {
                daySummary.addPublicPhotos(1);
                tripSummary.addPublicPhotos(1);
                album.addPublicMediaCount(1);
            }
            else if(!originalPrivacy && isPrivate){
                daySummary.addPublicPhotos(-1);
                tripSummary.addPublicPhotos(-1);
                album.addPublicMediaCount(-1);
            }
            daySummary.saveEventually();
            tripSummary.saveEventually();
        }

        if(parseGeoPoint != null){
            MediaGroup mediaGroup = new MediaGroup();
            for(Media media1 : album.getMedia()){
                TripUtils.addMediaToFrequency(mediaGroup, media1);
            }
            TripUtils.addLocationToAlbum(album, mediaGroup);
            if(!MapUtils.isEqual(previousGeoPoint,parseGeoPoint))
                addRoutePoint(parseGeoPoint,day,media.getContentCreatedDate());
        }
        album.saveEventually();
    }

    public void addRoutePoint(ParseGeoPoint parseGeoPoint, Day day, Date time) {

        RoutePoint routePoint = new RoutePoint();
        routePoint.setPriority(RoutePoint.HIGH_ACCURACY);
        routePoint.setRecordedTime(time);
        routePoint.setSource(Source.WAH);
        if(getTrip() != null)
            routePoint.setTrip(getTrip());
        if(day != null)
            routePoint.setDay(day);
        routePoint.setLocation(parseGeoPoint);
        routePoint.saveEventually();
    }

    public void saveSelectedMediaLocation(String address, ParseGeoPoint parseGeoPoint){
        Media media = getSelectedMedia();
        Timeline timeline = getTimeLine();

        ParseGeoPoint previousGeoPoint = media.getLocation();

        media.setAddress(address);
        media.setLocation(parseGeoPoint);

        media.saveEventually();

        Album album = (Album)timeline.getContent();

        Day day = getTrip().getDay(timeline.getDayOrder());

        MediaGroup mediaGroup = new MediaGroup();
        for(Media media1 : album.getMedia()){
            TripUtils.addMediaToFrequency(mediaGroup, media1);
        }
        TripUtils.addLocationToAlbum(album, mediaGroup);
        if(!MapUtils.isEqual(previousGeoPoint, parseGeoPoint))
            addRoutePoint(parseGeoPoint, day, media.getContentCreatedDate());

        album.saveEventually();
    }

    @Override
    public void saveSelectedMediaAddress(String address, Media media, Timeline timeline) {

        media.setAddress(address);

        media.saveEventually();

        Album album = (Album)timeline.getContent();

        if(album.getLocation() != null && media.getLocation() != null){
            if(album.getLocation().getLatitude() == media.getLocation().getLatitude() &&
                    album.getLocation().getLongitude() == media.getLocation().getLongitude()){
                album.setLocationText(address);
                album.saveEventually();
            }
        }
    }

    private Timeline selectedNote;

    @Override
    public Timeline getSelectedNote() {
        return selectedNote;
    }

    @Override
    public void setSelectedNote(Timeline selectedNote) {
        this.selectedNote = selectedNote;
    }

    @Override
    public void saveNote(String note, String address, ParseGeoPoint parseGeoPoint, boolean inSync) throws ParseException {
        Timeline timeline = getSelectedNote();

        Note note1 = (Note)timeline.getContent();

        Day day = getTrip().getDay(timeline.getDayOrder());

        if(!TextUtils.isEmpty(note))
            note1.setContent(note);

        if(!TextUtils.isEmpty(address))
            note1.setLocationText(address);

        ParseGeoPoint previousPoint = note1.getLocation();

        if(parseGeoPoint != null)
            note1.setLocation(parseGeoPoint);

        if(MapUtils.isEqual(previousPoint,parseGeoPoint))
            addRoutePoint(parseGeoPoint, day, timeline.getContentTime());

        if(inSync)
            note1.save();
        else
            note1.saveEventually();
    }
}
