package com.weareholidays.bia.parse.utils;

import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.activities.journal.people.models.PhoneBookContact;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.CheckIn;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.DaySummary;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Note;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.TripPeople;
import com.weareholidays.bia.parse.models.TripSettings;
import com.weareholidays.bia.parse.models.TripSummary;
import com.weareholidays.bia.parse.models.local.MediaGroup;
import com.weareholidays.bia.parse.models.local.PrivateMedia;
import com.weareholidays.bia.parse.models.local.TripLocal;
import com.weareholidays.bia.social.facebook.models.FacebookContact;
import com.weareholidays.bia.social.facebook.models.FacebookMedia;
import com.weareholidays.bia.social.facebook.models.FacebookPost;
import com.weareholidays.bia.social.instagram.models.InstagramMedia;
import com.weareholidays.bia.social.instagram.models.InstagramPost;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MapUtils;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Teja on 23-06-2015.
 */
public class TripLocalOperations implements TripOperations, TripCreateOperations {

    private static final String TAG = "TripLocalOps";

    public static final String CURRENT_TRIP = "OFFLINE_CURRENT_TRIP_S";

    private List<GalleryImage> galleryImages;

    private GalleryImage galleryImage;

    private Timeline timeline;

    protected TripLocalOperations() {

    }

    private boolean tripLoaded;

    private Trip currentTrip;

    private void eagerLoadTrip() {
        if (!tripLoaded) {
            try {
                currentTrip = ParseQuery.getQuery(Trip.class).fromPin(CURRENT_TRIP).include(Trip.DAYS)
                        .include(Trip.SETTINGS).include(Trip.TRIP_OWNER).include(Trip.SUMMARY).include(Trip.DAYS + "." + Day.DAY_SUMMARY).getFirst();
                tripLoaded = true;
            } catch (ParseException e) {
                if (e != null && e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                    Log.w(TAG, "Error Loading current trip", e);
                }
            }
        }
    }

    public void loadTrip() {
        tripLoaded = false;
        eagerLoadTrip();
    }

    @Override
    public void clearAll() throws ParseException {
        ParseObject.unpinAll(CURRENT_TRIP);
        ParseFileUtils.deleteImageDirectory();
        currentTrip = null;
        tripLoaded = false;
    }

    @Override
    public String getTripKey() {
        return TripOperations.CURRENT_TRIP_ID;
    }

    public Trip getTrip() {
        eagerLoadTrip();
        return currentTrip;
    }

    @Override
    public boolean isTripLoaded() {
        return tripLoaded;
    }

    public boolean isTripAvailable() {
        eagerLoadTrip();
        return currentTrip != null;
    }

    public void createTrip(TripLocal tripLocal) throws ParseException {
        Trip trip;
        Day day;
        DaySummary daySummary;
        try {
            ParseCustomUser user = (ParseCustomUser) ParseUser.getCurrentUser();
            daySummary = new DaySummary();
            daySummary.init();
            daySummary.pin(CURRENT_TRIP);

            Calendar startTime = Calendar.getInstance();
//            startTime.set(Calendar.DATE, 1);
//            startTime.set(Calendar.MONTH, Calendar.JULY);

            day = new Day();
            day.setName("Day 1");
            day.setDaySummary(daySummary);
            day.setStartTime(startTime.getTime());
            day.setDisplayOrder(0);
            day.pin(CURRENT_TRIP);

            TripSettings tripSettings = new TripSettings();
            tripSettings.setFacebook(tripLocal.isAccessFacebook());
            tripSettings.setCameraRoll(tripLocal.isAccessCameraRoll());
            tripSettings.setCheckIn(tripLocal.isAccessCheckIn());
            tripSettings.setLocation(tripLocal.isAccessLocation());
            tripSettings.setSync(tripLocal.isAccessSync());
            tripSettings.setTwitter(tripLocal.isAccessTwitter());
            tripSettings.setInstagram(tripLocal.isAccessInstagram());
            tripSettings.pin(CURRENT_TRIP);

            TripSummary tripSummary = new TripSummary();
            tripSummary.init();
            tripSummary.pin(CURRENT_TRIP);


            trip = new Trip();
            trip.setStartTime(startTime.getTime());
            trip.setName(tripLocal.getName());
            trip.setSettings(tripSettings);
            trip.setOwner(user);
            //trip.setStartLocation();
            trip.addDay(day);
            trip.setSummary(tripSummary);
            trip.setDeleted(false);
            trip.setHidden(false);
            trip.setPublished(tripLocal.isAccessPublic());

            try {
                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                //Create array for salt
                byte[] salt = new byte[32];
                //Get a random salt
                sr.nextBytes(salt);

                trip.setSecretKey(salt.toString());
            } catch (NoSuchAlgorithmException e) {
                DebugUtils.logException(e);
                Random r = new Random();
                trip.setSecretKey(r.nextLong() + "");
            }

            trip.pin(CURRENT_TRIP);

            if (tripLocal.getPeople() != null && tripLocal.getPeople().size() > 0) {
                List<TripPeople> people = new ArrayList<>();
                for (PeopleContact peopleContact : tripLocal.getPeople()) {
                    TripPeople tripPeople = new TripPeople();
                    tripPeople.setName(peopleContact.getName());
                    tripPeople.setInTrip(true);
                    tripPeople.setIdentifier(peopleContact.getIdentifier());
                    tripPeople.setTrip(trip);
                    String imageUri = peopleContact.getImageUri();
                    if (peopleContact.getType() == PeopleContact.Type.FB) {
                        tripPeople.setType(TripPeople.FACEBOOK_TYPE);
                        if (!TextUtils.isEmpty(imageUri))
                            tripPeople.setImageUrl(peopleContact.getImageUri());
                        people.add(tripPeople);
                    } else if (peopleContact.getType() == PeopleContact.Type.PHONE) {
                        PhoneBookContact phoneBookContact = (PhoneBookContact) peopleContact;
                        tripPeople.setType(TripPeople.PHONE_BOOK_TYPE);
                        if (!TextUtils.isEmpty(phoneBookContact.getEmail()))
                            tripPeople.setEmail(phoneBookContact.getEmail());
                        tripPeople.pin(CURRENT_TRIP);
                        if (!TextUtils.isEmpty(imageUri))
                            ParseFileUtils.pinFileLocally(tripPeople, TripPeople.IMAGE, Uri.parse(imageUri), "");
                    }
                }
                if (people.size() > 0)
                    ParseObject.pinAll(CURRENT_TRIP, people);
            }

            if (!TextUtils.isEmpty(tripLocal.getFeatureImage()))
                ParseFileUtils.pinFileLocally(trip, Trip.FEATURE_IMAGE, Uri.parse(tripLocal.getFeatureImage()), "trip_feature_image.jpeg");

            loadTrip();

        } catch (ParseException e) {
            ParseObject.unpinAll(CURRENT_TRIP);
            throw e;
        }
    }

    public Timeline addCheckIn(String name, ParseGeoPoint parseGeoPoint, String photoReference) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Trip trip = getTrip();
        Day currentDay = TripUtils.getDayFromTime(trip.getDays(), calendar);
        if (currentDay == null) {
            throw TripUtils.createDayNotFoundException(calendar);
        }
        CheckIn checkIn = new CheckIn();
        checkIn.setName(name);
        if (photoReference != null)
            checkIn.setPhotoReference(photoReference);
        if (parseGeoPoint != null)
            checkIn.setLocation(parseGeoPoint);
        checkIn.pin(CURRENT_TRIP);

        currentDay.getDaySummary().addCheckIns(1);
        getTrip().getSummary().addCheckIns(1);
        getTrip().getSummary().pinInBackground(CURRENT_TRIP);

        Timeline timeline = new Timeline();
        timeline.setContent(checkIn);
        timeline.setSource(Source.WAH);
        timeline.setDay(currentDay);
        timeline.setContentTime(Calendar.getInstance().getTime());
        timeline.setDisplayOrder(getNextTimeLineDisplayOrder(currentDay));
        timeline.setDayOrder(currentDay.getDisplayOrder());
        timeline.setTrip(getTrip());
        timeline.pin(CURRENT_TRIP);

        if (parseGeoPoint != null) {
            addRoutePoint(parseGeoPoint, currentDay, timeline.getContentTime());
        }

        return timeline;
    }

    public Timeline addNote(String notes, String locationText, ParseGeoPoint parseGeoPoint) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        Trip trip = getTrip();
        Day currentDay = TripUtils.getDayFromTime(trip.getDays(), calendar);
        if (currentDay == null) {
            throw TripUtils.createDayNotFoundException(calendar);
        }
        if (!TextUtils.isEmpty(notes)) {
            notes = notes.trim();
        }
        Note note = new Note();
        note.setContent(notes);
        note.setLocationText(locationText);
        if (parseGeoPoint != null)
            note.setLocation(parseGeoPoint);
        note.pin(CURRENT_TRIP);

        currentDay.getDaySummary().addNotes(1);
        getTrip().getSummary().addNotes(1);
        getTrip().getSummary().pinInBackground(CURRENT_TRIP);

        Timeline timeline = new Timeline();
        timeline.setContent(note);
        timeline.setSource(Source.WAH);
        timeline.setDay(currentDay);
        timeline.setDayOrder(currentDay.getDisplayOrder());
        timeline.setTrip(getTrip());
        timeline.setContentTime(Calendar.getInstance().getTime());
        timeline.setDisplayOrder(getNextTimeLineDisplayOrder(currentDay));
        timeline.pin(CURRENT_TRIP);

        if (parseGeoPoint != null) {
            addRoutePoint(parseGeoPoint, currentDay, timeline.getContentTime());
        }
        return timeline;
    }

    public Timeline addPhotosOld(List<GalleryImage> galleryImages) throws ParseException {

        //Handle Race Condition where photo is taken through Add photo workflow but before it is saved,
        //camera roll sync completes.
        Set<String> fileLocalSet = new HashSet<>();
        for (FileLocal fileLocal : ParseFileUtils.storedGalleryImages()) {
            fileLocalSet.add(fileLocal.getLocalUri());
        }

        Timeline returnTimeline = null;
        for (GalleryImage image : galleryImages) {
            if (!image.getUri().startsWith("http") && fileLocalSet.contains(image.getUri()))
                continue;

            long time = image.getDateTaken();
            ParseGeoPoint imageLocation = null;
            if (!(image.getLatitude() == 0.0 && image.getLongitude() == 0.0)) {
                imageLocation = new ParseGeoPoint();
                imageLocation.setLatitude(image.getLatitude());
                imageLocation.setLongitude(image.getLongitude());
            }

            Calendar imageTime = Calendar.getInstance();
            imageTime.setTimeInMillis(time);

            Calendar imageTimePlus30 = Calendar.getInstance();
            imageTimePlus30.setTimeInMillis(time);
            imageTimePlus30.add(Calendar.MINUTE, 30);

            Calendar imageTimeMinus30 = Calendar.getInstance();
            imageTimeMinus30.setTimeInMillis(time);
            imageTimeMinus30.add(Calendar.MINUTE, -30);

            Album alb = null;

            String caption = image.getCaption();
            String address = image.getAddress();

            if (TextUtils.isEmpty(caption))
                caption = "";

            if (TextUtils.isEmpty(address))
                address = "";
            long difference = 30 * 60 * 1000;

            List<Album> albums = ParseQuery.getQuery(Album.class).whereEqualTo(Album.SOURCE, Source.WAH).whereLessThan(Album.START_TIME, imageTimePlus30.getTime()).whereGreaterThan(Album.END_TIME, imageTimeMinus30.getTime())
                    .orderByAscending(Album.START_TIME).fromPin(CURRENT_TRIP).setLimit(10).find();
            if (imageLocation != null) {
                double distance = 1;
                for (Album album : albums) {
                    if (album.getLocation() != null) {
                        double tmp = imageLocation.distanceInKilometersTo(album.getLocation());
                        if (tmp < distance) {
                            alb = album;
                            distance = tmp;
                        }
                    } else {
                        long diff = Math.abs(album.getStartTime().getTime() - time);
                        if (diff < difference) {
                            alb = album;
                            difference = diff;
                        }
                    }
                }
            } else {
                for (Album album : albums) {
                    long diff = Math.abs(album.getStartTime().getTime() - time);
                    if (diff < difference) {
                        alb = album;
                        difference = diff;
                    }
                }
            }

            Day day = TripUtils.getDayFromTime(getTrip().getDays(), imageTime);

            if (day == null)
                throw TripUtils.createDayNotFoundException(imageTime);

            day.getDaySummary().addPhotos(1);
            getTrip().getSummary().addPhotos(1);

            if (!image.isPrivacy()) {
                day.getDaySummary().addPublicPhotos(1);
                getTrip().getSummary().addPublicPhotos(1);
            }

            getTrip().getSummary().pin(CURRENT_TRIP);

            if (alb == null) {
                alb = new Album();
                alb.setContent(caption);
                alb.setSource(Source.WAH);
                alb.setStartTime(imageTime.getTime());
                alb.setEndTime(imageTime.getTime());
                alb.setLocationText(address);
                alb.addMediaCount(1);

                if (imageLocation != null)
                    alb.setLocation(imageLocation);

                if (!image.isPrivacy())
                    alb.addPublicMediaCount(1);

                alb.pin(CURRENT_TRIP);

                Timeline timeline = new Timeline();
                timeline.setContent(alb);
                timeline.setSource(Source.WAH);
                timeline.setDay(day);
                timeline.setDayOrder(day.getDisplayOrder());
                timeline.setTrip(getTrip());
                timeline.setContentTime(imageTime.getTime());
                timeline.setDisplayOrder(getTimeLineDisplayOrder(day, imageTime.getTime()));
                timeline.pin(CURRENT_TRIP);

                returnTimeline = timeline;
            } else {
                boolean isFirstPhoto = false;
                if (alb.getStartTime().getTime() > time) {
                    isFirstPhoto = true;
                    alb.setStartTime(imageTime.getTime());
                }
                if (alb.getEndTime().getTime() < time)
                    alb.setEndTime(imageTime.getTime());

                if ((alb.getLocation() == null || isFirstPhoto) && imageLocation != null) {
                    alb.setLocation(imageLocation);
                }

                if (!TextUtils.isEmpty(caption) && (TextUtils.isEmpty(alb.getContent()) || isFirstPhoto)) {
                    alb.setContent(caption);
                }

                if (!TextUtils.isEmpty(address) && TextUtils.isEmpty(alb.getLocationText()) || isFirstPhoto) {
                    alb.setLocationText(address);
                }

                alb.addMediaCount(1);
                if (!image.isPrivacy())
                    alb.addPublicMediaCount(1);
                alb.pin(CURRENT_TRIP);

                List<Timeline> timelinesList = ParseQuery.getQuery(Timeline.class).whereContainsAll(Timeline.CONTENT, Collections.singletonList(alb))
                        .fromPin(CURRENT_TRIP).setLimit(1).find();

                if (timelinesList != null && timelinesList.size() > 0) {
                    returnTimeline = timelinesList.get(0);
                }
            }

            day.pin(CURRENT_TRIP);

            if (imageLocation != null) {
                addRoutePoint(imageLocation, day, imageTime.getTime());
            }

            Media media = new Media();
            media.setAlbum(alb);
            media.setContentSize(image.getSize());
            media.setCaption(caption);
            media.setAddress(address);
            media.setThirdPartyUrl(image.getUri());
            media.setMediaHeight(image.getMediaHeight());
            media.setMediaWidth(image.getMediaWidth());
            media.setContentCreatedDate(imageTime.getTime());
            if (image.getTags() != null && image.getTags().size() > 0) {
                media.setTags(image.getTags());
            }
            media.setPrivate(image.isPrivacy());
            if (imageLocation != null)
                media.setLocation(imageLocation);

            media.pin(CURRENT_TRIP);

            ParseFileUtils.pinFileLocally(media, Media.CONTENT, Uri.parse(image.getUri()), "");
        }

        return returnTimeline;
    }

    public Timeline addPhotos(List<GalleryImage> galleryImages) throws ParseException {

        //Handle Race Condition where photo is taken through Add photo workflow but before it is saved,
        //camera roll sync completes.
        Set<String> fileLocalSet = new HashSet<>();
        for (FileLocal fileLocal : ParseFileUtils.storedGalleryImages()) {
//            fileLocalSet.add(fileLocal.getLocalUri());
            fileLocalSet.add(fileLocal.getFileName());

        }

        List<MediaGroup> mediaGroupList = new ArrayList<>();

        for (GalleryImage image : galleryImages) {
            if (!image.getUri().startsWith("http") && (fileLocalSet.contains(image.getName()) || image.getUri().contains("BIA_")))
                continue;
            long time = image.getDateTaken();

            Calendar imageTime = Calendar.getInstance();
            imageTime.setTimeInMillis(time);

            Calendar imageTimePlus30 = Calendar.getInstance();
            imageTimePlus30.setTimeInMillis(time);
            imageTimePlus30.add(Calendar.MINUTE, 30);

            Calendar imageTimeMinus30 = Calendar.getInstance();
            imageTimeMinus30.setTimeInMillis(time);
            imageTimeMinus30.add(Calendar.MINUTE, -30);

            ParseGeoPoint imageLocation = null;
            if (!(image.getLatitude() == 0.0 && image.getLongitude() == 0.0)) {
                imageLocation = new ParseGeoPoint();
                imageLocation.setLatitude(image.getLatitude());
                imageLocation.setLongitude(image.getLongitude());
            }

            MediaGroup selectedMediaGroup = null;
            for (MediaGroup group : mediaGroupList) {
                if (group.getStartTime().getTime() <= imageTimePlus30.getTime().getTime() &&
                        group.getEndTime().getTime() >= imageTimeMinus30.getTime().getTime()) {
                    if (imageLocation != null && group.getGroupLocation() != null) {
                        if (imageLocation.distanceInKilometersTo(group.getGroupLocation()) < 1) {
                            selectedMediaGroup = group;
                        }
                    } else {
                        selectedMediaGroup = group;
                        break;
                    }
                }
            }

            if (selectedMediaGroup == null) {
                selectedMediaGroup = new MediaGroup();
                selectedMediaGroup.setEndTime(imageTime.getTime());
                selectedMediaGroup.setStartTime(imageTime.getTime());
                mediaGroupList.add(selectedMediaGroup);
            }

            if (selectedMediaGroup.getGroupLocation() == null && imageLocation != null) {
                selectedMediaGroup.setGroupLocation(imageLocation);
            }

            String caption = image.getCaption();
            String address = image.getAddress();

            if (TextUtils.isEmpty(caption))
                caption = "";

            if (TextUtils.isEmpty(address))
                address = "";

            PrivateMedia privateMedia = ParseFileUtils.copyImageToPrivateLocation
                    (image.getUri(), WAHApplication.getWAHContext());

            Media media = new Media();
            media.setCaption(caption);
            media.setAddress(address);
            if (privateMedia != null) {
                media.setContentSize(privateMedia.getSize());
                media.setMediaWidth(privateMedia.getWidth());
                media.setMediaHeight(privateMedia.getHeight());
                media.setThirdPartyUrl(privateMedia.getUrl());
            } else {
                media.setContentSize(image.getSize());
                media.setMediaHeight(image.getMediaHeight());
                media.setMediaWidth(image.getMediaWidth());
                media.setThirdPartyUrl(image.getUri());
            }
            media.setContentCreatedDate(imageTime.getTime());
            if (image.getTags() != null && image.getTags().size() > 0) {
                media.setTags(image.getTags());
            }
            media.setPrivate(image.isPrivacy());
            if (imageLocation != null)
                media.setLocation(imageLocation);

            selectedMediaGroup.getMediaList().add(media);

            if (selectedMediaGroup.getStartTime().getTime() > imageTime.getTime().getTime()) {
                selectedMediaGroup.setStartTime(imageTime.getTime());
            }

            if (selectedMediaGroup.getEndTime().getTime() < imageTime.getTime().getTime()) {
                selectedMediaGroup.setEndTime(imageTime.getTime());
            }

            selectedMediaGroup.setMediaCount(selectedMediaGroup.getMediaCount() + 1);

            if (!image.isPrivacy())
                selectedMediaGroup.setPublicMediaCount(selectedMediaGroup.getPublicMediaCount() + 1);

            if (imageLocation != null) {
                TripUtils.addMediaToFrequency(selectedMediaGroup, media);
            }
        }

        Timeline returnTimeline = null;

        for (MediaGroup mediaGroup : mediaGroupList) {

            Calendar mediaGroupTime = Calendar.getInstance();
            mediaGroupTime.setTime(mediaGroup.getStartTime());

            Day day = TripUtils.getDayFromTime(getTrip().getDays(), mediaGroupTime);

            if (day == null)
                throw TripUtils.createDayNotFoundException(mediaGroupTime);

            Calendar mediaGroupTimePlus30 = Calendar.getInstance();
            mediaGroupTimePlus30.setTime(mediaGroup.getStartTime());
            mediaGroupTimePlus30.add(Calendar.MINUTE, 30);

            Calendar mediaGroupTimeMinus30 = Calendar.getInstance();
            mediaGroupTimeMinus30.setTime(mediaGroup.getEndTime());
            mediaGroupTimeMinus30.add(Calendar.MINUTE, -30);

            Album alb = null;

            List<Album> albums = ParseQuery.getQuery(Album.class).whereEqualTo(Album.SOURCE, Source.WAH).whereLessThan(Album.START_TIME, mediaGroupTimePlus30.getTime()).whereGreaterThan(Album.END_TIME, mediaGroupTimeMinus30.getTime())
                    .orderByAscending(Album.START_TIME).fromPin(CURRENT_TRIP).setLimit(10).find();

            long difference = 30 * 60 * 1000;

            if (mediaGroup.getGroupLocation() != null) {
                double distance = 1;
                for (Album album : albums) {
                    if (album.getLocation() != null) {
                        double tmp = mediaGroup.getGroupLocation().distanceInKilometersTo(album.getLocation());
                        if (tmp < distance) {
                            alb = album;
                            distance = tmp;
                        }
                    } else {
                        long diff = Math.abs(album.getStartTime().getTime() - mediaGroup.getStartTime().getTime());
                        if (diff < difference) {
                            alb = album;
                            difference = diff;
                        }
                    }
                }
            } else {
                for (Album album : albums) {
                    long diff = Math.abs(album.getStartTime().getTime() - mediaGroup.getStartTime().getTime());
                    if (diff < difference) {
                        alb = album;
                        difference = diff;
                    }
                }
            }

            if (alb == null) {

                alb = new Album();
                alb.setContent("");
                alb.setSource(Source.WAH);
                alb.setStartTime(mediaGroup.getStartTime());
                alb.setEndTime(mediaGroup.getEndTime());
                alb.addMediaCount(mediaGroup.getMediaCount());
                alb.addPublicMediaCount(mediaGroup.getPublicMediaCount());

                TripUtils.addLocationToAlbum(alb, mediaGroup);

                alb.pin(CURRENT_TRIP);

                Timeline timeline = new Timeline();
                timeline.setContent(alb);
                timeline.setSource(Source.WAH);
                timeline.setDay(day);
                timeline.setDayOrder(day.getDisplayOrder());
                timeline.setTrip(getTrip());
                timeline.setContentTime(mediaGroupTime.getTime());
                timeline.setDisplayOrder(getTimeLineDisplayOrder(day, mediaGroupTime.getTime()));
                timeline.pin(CURRENT_TRIP);

                returnTimeline = timeline;
            } else {
                if (mediaGroup.getGroupLocation() != null) {
                    if (alb.getLocation() == null) {
                        TripUtils.addLocationToAlbum(alb, mediaGroup);
                    } else {
                        List<Media> existingMediaList = ParseQuery.getQuery(Media.class).whereEqualTo(Media.ALBUM, alb)
                                .whereExists(Media.LOCATION).fromPin(CURRENT_TRIP).find();

                        for (Media existingMedia : existingMediaList) {
                            TripUtils.addMediaToFrequency(mediaGroup, existingMedia);
                        }

                        TripUtils.addLocationToAlbum(alb, mediaGroup);
                    }
                }

                if (alb.getStartTime().getTime() > mediaGroup.getStartTime().getTime()) {
                    alb.setStartTime(mediaGroup.getStartTime());
                }
                if (alb.getEndTime().getTime() < mediaGroup.getEndTime().getTime())
                    alb.setEndTime(mediaGroup.getEndTime());

                alb.addMediaCount(mediaGroup.getMediaCount());
                alb.addPublicMediaCount(mediaGroup.getPublicMediaCount());
                alb.pin(CURRENT_TRIP);

                List<Timeline> timelinesList = ParseQuery.getQuery(Timeline.class).whereContainsAll(Timeline.CONTENT, Collections.singletonList(alb))
                        .fromPin(CURRENT_TRIP).setLimit(1).find();

                if (timelinesList != null && timelinesList.size() > 0) {
                    returnTimeline = timelinesList.get(0);
                }
            }

            day.getDaySummary().addPhotos(mediaGroup.getMediaCount());
            getTrip().getSummary().addPhotos(mediaGroup.getMediaCount());
            day.getDaySummary().addPublicPhotos(mediaGroup.getPublicMediaCount());
            getTrip().getSummary().addPublicPhotos(mediaGroup.getPublicMediaCount());

            getTrip().getSummary().pinInBackground(CURRENT_TRIP);
            day.pinInBackground(CURRENT_TRIP);

            for (Media media : mediaGroup.getMediaList()) {
                media.setAlbum(alb);
            }

            ParseObject.pinAll(CURRENT_TRIP, mediaGroup.getMediaList());

            for (Media media : mediaGroup.getMediaList()) {
                String fileName = (new File(media.getThirdPartyUrl())).getName();
                ParseFileUtils.pinFileLocallyInBackground(media, Media.CONTENT, Uri.parse(media.getThirdPartyUrl()), fileName);
                if (media.getLocation() != null)
                    addRoutePoint(media.getLocation(), day, media.getContentCreatedDate());
            }
        }

        return returnTimeline;
    }

    private int getNextTimeLineDisplayOrder(Day day) throws ParseException {
        List<Timeline> timelines = ParseQuery.getQuery(Timeline.class).whereEqualTo(Timeline.DAY, day).fromPin(CURRENT_TRIP)
                .orderByDescending(Timeline.DISPLAY_ORDER).setLimit(1).find();
        if (timelines != null && timelines.size() > 0) {
            return timelines.get(0).getDisplayOrder() + 1;
        }
        return 0;
    }

    private int getTimeLineDisplayOrder(Day day, Date time) throws ParseException {
        List<Timeline> beforeTimelines = ParseQuery.getQuery(Timeline.class).whereEqualTo(Timeline.DAY, day).fromPin(CURRENT_TRIP)
                .whereLessThan(Timeline.CONTENT_TIME_STAMP, time).orderByDescending(Timeline.DISPLAY_ORDER).setLimit(1).find();
        List<Timeline> shiftedTimelines = null;
        int displayOrder = 0;
        if (beforeTimelines != null && beforeTimelines.size() > 0) {
            Timeline timeline = beforeTimelines.get(0);
            shiftedTimelines = ParseQuery.getQuery(Timeline.class).whereEqualTo(Timeline.DAY, day).fromPin(CURRENT_TRIP)
                    .whereGreaterThan(Timeline.DISPLAY_ORDER, timeline.getDisplayOrder()).orderByDescending(Timeline.DISPLAY_ORDER).setLimit(1000).find();
            displayOrder = timeline.getDisplayOrder() + 1;
        } else {
            List<Timeline> afterTimelines = ParseQuery.getQuery(Timeline.class).whereEqualTo(Timeline.DAY, day).fromPin(CURRENT_TRIP)
                    .setLimit(1000).find();
            if (afterTimelines != null && afterTimelines.size() > 0) {
                shiftedTimelines = afterTimelines;
            }
        }
        if (shiftedTimelines != null) {
            for (Timeline shiftedTimeline : shiftedTimelines) {
                shiftedTimeline.setDisplayOrder(shiftedTimeline.getDisplayOrder() + 1);
            }
            ParseObject.pinAll(CURRENT_TRIP, shiftedTimelines);
        }
        return displayOrder;
    }

    public void endDay() throws ParseException {
        Trip trip = getTrip();
        Day day = null;
        int displayOrder = -1;
        for (Day ds : trip.getDays()) {
            if (ds.getDisplayOrder() > displayOrder) {
                displayOrder = ds.getDisplayOrder();
                day = ds;
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Date startTimedate = day.getStartTime();
        Calendar c = Calendar.getInstance();
        c.setTime(startTimedate);
        String startDate = sdf.format(c.getTime());

        Calendar tempCurrentTime = Calendar.getInstance();
        String currentDate = sdf.format(tempCurrentTime.getTime());

        if (!startDate.equals(currentDate)) {
            day.setEndTime(Calendar.getInstance().getTime());
            day.pin(CURRENT_TRIP);

            DaySummary daySummary = new DaySummary();
            daySummary.init();
            daySummary.pin(CURRENT_TRIP);

            Day nextDay = new Day();
            nextDay.setName("Day " + (day.getDisplayOrder() + 2));
            nextDay.setDaySummary(daySummary);
            nextDay.setStartTime(Calendar.getInstance().getTime());
            nextDay.setDisplayOrder(day.getDisplayOrder() + 1);
            nextDay.pin(CURRENT_TRIP);

            trip.addDay(nextDay);
            trip.pin(CURRENT_TRIP);
        }
    }

    public void addFacebookPosts(List<FacebookPost> posts) throws ParseException {
        List<Day> days = getTrip().getDays();
        Day currentDay = null;
        Calendar dayStart = null;
        Calendar dayEnd = null;
        for (FacebookPost post : posts) {
            Calendar postTime = Calendar.getInstance();
            postTime.setTime(post.getCreatedTime());
            if (currentDay == null || !TripUtils.doesTimeFallsUnderDay(dayStart, dayEnd, postTime)) {
                dayStart = null;
                dayEnd = null;
                currentDay = TripUtils.getDayFromTime(days, postTime);
                if (currentDay == null) {
                    Log.i(TAG, "Post skipped as a corresponding day not found : " + post.getId()
                            + " ," + post.getCreatedTime());
                    continue;
                } else {
                    dayStart = Calendar.getInstance();
                    dayStart.setTime(currentDay.getStartTime());
                    if (currentDay.getEndTime() != null) {
                        dayEnd = Calendar.getInstance();
                        dayEnd.setTime(currentDay.getEndTime());
                    }
                }
            }

            ParseObject content = null;
            String message = post.getMessage();
            if (TextUtils.isEmpty(message)) {
                message = post.getStory();
            }
            //If there is no message in post and it has location, it's assumed to be a checkin
            if (post.getType() == FacebookPost.Type.STATUS) {
                if (post.getLocation() != null && TextUtils.isEmpty(post.getMessage())) {
                    // check if already saved (vijay: dirty hack for the time being)
                    boolean alreadySaved = false;
                    synchronized (this) {
                        List<Timeline> checkins = getDayTimeLines(currentDay, -1, 0, "FB", "CheckIn");
                        for (Timeline timeline : checkins) {
                            CheckIn checkin = (CheckIn) timeline.getContent();
                            if (checkin.getName().equals(message)) {
                                //if (Math.abs(calendar.getTime().getTime() - timeline.getContentTime().getTime()) < 60000) {
                                alreadySaved = true;
                                break;
                                //}
                            }
                        }
                        if (alreadySaved)
                            continue;
                    }

                    CheckIn checkIn = new CheckIn();
                    checkIn.setLocationText(post.getLocationText());
                    checkIn.setName(message);
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                    parseGeoPoint.setLatitude(post.getLocation().latitude);
                    parseGeoPoint.setLongitude(post.getLocation().longitude);
                    checkIn.setLocation(parseGeoPoint);
                    checkIn.pin(CURRENT_TRIP);
                    content = checkIn;
                } else {
                    // check if already saved (vijay: dirty hack for the time being)
                    boolean alreadySaved = false;
                    synchronized (this) {
                        List<Timeline> notes = getDayTimeLines(currentDay, -1, 0, "FB", "Note");
                        for (Timeline timeline : notes) {
                            Note note = (Note) timeline.getContent();
                            if (note.getContent().equals(message.trim())) {
                                //if (Math.abs(calendar.getTime().getTime() - timeline.getContentTime().getTime()) < 60000) {
                                alreadySaved = true;
                                break;
                                //}
                            }
                        }
                        if (alreadySaved)
                            continue;
                    }

                    Note note = new Note();
                    note.setContent(message);
                    if (post.getLocation() != null) {
                        note.setLocationText(post.getLocationText());
                        ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                        parseGeoPoint.setLatitude(post.getLocation().latitude);
                        parseGeoPoint.setLongitude(post.getLocation().longitude);
                        note.setLocation(parseGeoPoint);
                    }
                    note.pin(CURRENT_TRIP);
                    content = note;
                }
            } else if (post.getType() == FacebookPost.Type.PHOTO) {
                Album album = new Album();
                album.setSource(Source.FB);
                album.setContent(message);
                album.addMediaCount(post.getMedia().size());
                album.addPublicMediaCount(post.getMedia().size());

                currentDay.getDaySummary().addPublicPhotos(post.getMedia().size());
                currentDay.getDaySummary().addPhotos(post.getMedia().size());
                getTrip().getSummary().addPublicPhotos(post.getMedia().size());
                getTrip().getSummary().addPhotos(post.getMedia().size());

                if (post.getLocation() != null) {
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                    parseGeoPoint.setLatitude(post.getLocation().latitude);
                    parseGeoPoint.setLongitude(post.getLocation().longitude);
                    album.setLocation(parseGeoPoint);
                    album.setLocationText(post.getLocationText());
                }
                album.pin(CURRENT_TRIP);
                content = album;
            }

            if (content != null) {
                currentDay.getDaySummary().addFacebook(1);
                getTrip().getSummary().addFacebook(1);
                getTrip().getSummary().pin(CURRENT_TRIP);

                Timeline timeline = new Timeline();
                timeline.setContent(content);
                timeline.setDayOrder(currentDay.getDisplayOrder());
                timeline.setTrip(getTrip());
                timeline.setSource(Source.FB);
                timeline.setDay(currentDay);
                timeline.setContentTime(post.getCreatedTime());//TODO: adjust display orders
                timeline.setDisplayOrder(getTimeLineDisplayOrder(currentDay, post.getCreatedTime()));
                timeline.setThirdPartyId(post.getId());
                timeline.pin(CURRENT_TRIP);

                if (post.getType() == FacebookPost.Type.PHOTO) {
                    for (FacebookMedia md : post.getMedia()) {
                        Media media = new Media();
                        media.setAlbum((Album) content);
                        media.setMediaHeight(md.getMediaHeight());
                        media.setMediaWidth(md.getMediaWidth());
                        media.setThirdPartyId(md.getId());
                        media.setThirdPartyUrl(md.getMediaSource());
                        media.setContentCreatedDate(post.getCreatedTime());
                        //TODO: add other fields

                        media.pin(CURRENT_TRIP);

                        //save image to private location
                        ParseFileUtils.pinFileLocally(media, Media.CONTENT, Uri.parse(md.getMediaSource()), "");
                    }
                }
            }
        }

    }

    public void addTwitterPosts(List<twitter4j.Status> posts) throws java.text.ParseException, ParseException {
        List<Day> days = getTrip().getDays();
        Day currentDay = null;
        Calendar dayStart = null;
        Calendar dayEnd = null;
        for (int i = 0; i < posts.size(); i++) {
            twitter4j.Status post = posts.get(i);
            Calendar postTime = Calendar.getInstance();
            postTime.setTime(post.getCreatedAt());
            if (currentDay == null || !TripUtils.doesTimeFallsUnderDay(dayStart, dayEnd, postTime)) {
                dayStart = null;
                dayEnd = null;
                currentDay = TripUtils.getDayFromTime(days, postTime);
                if (currentDay == null) {
                    Log.i(TAG, "Post skipped as a corresponding day not found : " + post.getCreatedAt());
                    continue;
                } else {
                    dayStart = Calendar.getInstance();
                    dayStart.setTime(currentDay.getStartTime());
                    if (currentDay.getEndTime() != null) {
                        dayEnd = Calendar.getInstance();
                        dayEnd.setTime(currentDay.getEndTime());
                    }
                }
            }

            ParseObject content = null;
            String message = post.getText();
            if (TextUtils.isEmpty(message)) {
                message = "";
            }
            //TODO: clarify logic between check in and post/note
            if (post.getExtendedMediaEntities().length == 0) {
                // check if already saved (vijay: dirty hack for the time being)
                boolean alreadySaved = false;
                synchronized (this) {
                    List<Timeline> notes = getDayTimeLines(currentDay, -1, 0, "TWITTER", "Note");
                    for (Timeline timeline : notes) {
                        Note note = (Note) timeline.getContent();
                        if (note.getContent().equals(message.trim())) {
                            //if (Math.abs(calendar.getTime().getTime() - timeline.getContentTime().getTime()) < 60000) {
                            alreadySaved = true;
                            break;
                            //}
                        }
                    }
                    if (alreadySaved)
                        continue;
                }

                Note note = new Note();
                note.setContent(message);
                note.pin(CURRENT_TRIP);
                if (post.getPlace() != null) {
                    note.setLocationText(post.getPlace().getName());
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                    double lat = 0, lng = 0;
                    for (int j = 0; j < post.getPlace().getBoundingBoxCoordinates()[0].length; j++) {
                        lng += post.getPlace().getBoundingBoxCoordinates()[0][j].getLongitude() / 4.0;
                        lat += post.getPlace().getBoundingBoxCoordinates()[0][j].getLatitude() / 4.0;
                    }
                    parseGeoPoint.setLatitude(lat);
                    parseGeoPoint.setLongitude(lng);
                }
                content = note;
            } else if (post.getExtendedMediaEntities().length > 0) {
                Album album = new Album();
                album.setSource(Source.TWITTER);
                String url = post.getExtendedMediaEntities()[0].getURL();
                message = message.replace(url, "");
                album.setContent(message);
                album.addMediaCount(post.getExtendedMediaEntities().length);
                album.addPublicMediaCount(post.getExtendedMediaEntities().length);
                if (post.getPlace() != null) {
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                    double lat = 0, lng = 0;
                    for (int j = 0; j < post.getPlace().getBoundingBoxCoordinates()[0].length; j++) {
                        lng += post.getPlace().getBoundingBoxCoordinates()[0][j].getLongitude() / 4.0;
                        lat += post.getPlace().getBoundingBoxCoordinates()[0][j].getLatitude() / 4.0;
                    }
                    parseGeoPoint.setLatitude(lat);
                    parseGeoPoint.setLongitude(lng);
                    album.setLocation(parseGeoPoint);
                    album.setLocationText(post.getPlace().getName());
                }
                album.pin(CURRENT_TRIP);
                content = album;
            }


            if (content != null) {
                currentDay.getDaySummary().addTwitter(1);
                getTrip().getSummary().addTwitter(1);
                getTrip().getSummary().pin(CURRENT_TRIP);

                Timeline timeline = new Timeline();
                timeline.setContent(content);
                timeline.setSource(Source.TWITTER);
                timeline.setDay(currentDay);
                timeline.setDayOrder(currentDay.getDisplayOrder());
                timeline.setTrip(getTrip());
                timeline.setThirdPartyId("" + post.getId());
                timeline.setContentTime(postTime.getTime());//TODO: adjust display orders
                timeline.setDisplayOrder(getTimeLineDisplayOrder(currentDay, postTime.getTime()));
                timeline.pin(CURRENT_TRIP);

                if (post.getExtendedMediaEntities().length > 0) {
                    Album album = (Album) content;
                    for (int j = 0; j < post.getExtendedMediaEntities().length; j++) {
                        if (post.getExtendedMediaEntities()[j].getType().equals("photo")) {
                            Media media = new Media();
                            media.setAlbum(album);
                            media.setMediaHeight(post.getExtendedMediaEntities()[j].getSizes().get(3).getHeight());
                            media.setMediaWidth(post.getExtendedMediaEntities()[j].getSizes().get(3).getWidth());
                            media.setThirdPartyId("" + post.getExtendedMediaEntities()[j].getId());
                            media.setThirdPartyUrl("" + post.getExtendedMediaEntities()[j].getMediaURL());
                            media.setContentCreatedDate(postTime.getTime());
                            if (album.getLocation() != null) {
                                media.setLocation(album.getLocation());
                                if (!TextUtils.isEmpty(album.getLocationText()))
                                    media.setAddress(album.getLocationText());
                            }
                            media.pin(CURRENT_TRIP);
                            //save image to private location
                            ParseFileUtils.pinFileLocally(media, Media.CONTENT, Uri.parse(post.getExtendedMediaEntities()[j].getMediaURL()), "");
                        }
                    }
                }

            }
        }
    }

    public void addInstagramPosts(List<InstagramPost> posts) throws ParseException {
        List<Day> days = getTrip().getDays();
        Day currentDay = null;
        Calendar dayStart = null;
        Calendar dayEnd = null;
        for (InstagramPost post : posts) {
            Calendar postTime = Calendar.getInstance();
            postTime.setTime(post.getCreatedTime());
            if (currentDay == null || !TripUtils.doesTimeFallsUnderDay(dayStart, dayEnd, postTime)) {
                dayStart = null;
                dayEnd = null;
                currentDay = TripUtils.getDayFromTime(days, postTime);
                if (currentDay == null) {
                    Log.i(TAG, "Post skipped as a corresponding day not found : " + post.getId()
                            + " ," + post.getCreatedTime());
                    continue;
                } else {
                    dayStart = Calendar.getInstance();
                    dayStart.setTime(currentDay.getStartTime());
                    if (currentDay.getEndTime() != null) {
                        dayEnd = Calendar.getInstance();
                        dayEnd.setTime(currentDay.getEndTime());
                    }
                }
            }

            ParseObject content = null;
            String message = post.getMessage();
            if (TextUtils.isEmpty(message)) {
                message = "";
            }
            if (post.getType() == InstagramPost.Type.PHOTO) {
                Album album = new Album();
                album.setSource(Source.INSTAGRAM);
                album.setContent(message);
                album.addMediaCount(post.getMedia().size());
                album.addPublicMediaCount(post.getMedia().size());
                if (post.getLocation() != null) {
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                    parseGeoPoint.setLatitude(post.getLocation().latitude);
                    parseGeoPoint.setLongitude(post.getLocation().longitude);
                    album.setLocation(parseGeoPoint);
                    if (!TextUtils.isEmpty(post.getLocationText()))
                        album.setLocationText(post.getLocationText());
                }
                album.pin(CURRENT_TRIP);
                content = album;
            }

            if (content != null) {
                currentDay.getDaySummary().addInstagram(1);
                getTrip().getSummary().addInstagram(1);
                getTrip().getSummary().pin(CURRENT_TRIP);

                Timeline timeline = new Timeline();
                timeline.setContent(content);
                timeline.setSource(Source.INSTAGRAM);
                timeline.setDay(currentDay);
                timeline.setDayOrder(currentDay.getDisplayOrder());
                timeline.setTrip(getTrip());
                timeline.setContentTime(post.getCreatedTime());//TODO: adjust display orders
                timeline.setDisplayOrder(getTimeLineDisplayOrder(currentDay, post.getCreatedTime()));
                timeline.setThirdPartyId(post.getId());
                timeline.pin(CURRENT_TRIP);

                if (post.getType() == InstagramPost.Type.PHOTO) {
                    Album album = (Album) content;
                    for (InstagramMedia md : post.getMedia()) {
                        Media media = new Media();
                        media.setAlbum(album);
                        media.setMediaHeight(md.getMediaHeight());
                        media.setMediaWidth(md.getMediaWidth());
                        media.setThirdPartyUrl(md.getMediaSource());
                        media.setCaption(message);
                        media.setContentCreatedDate(post.getCreatedTime());
                        if (album.getLocation() != null) {
                            media.setLocation(album.getLocation());
                            if (!TextUtils.isEmpty(album.getLocationText()))
                                media.setAddress(album.getLocationText());
                        }

                        media.pin(CURRENT_TRIP);

                        //save image to private location
                        ParseFileUtils.pinFileLocally(media, Media.CONTENT, Uri.parse(md.getMediaSource()), "");
                    }
                }
            }
        }
    }

    public Day addRoutePointFromLocationTracker(Location latLng) throws ParseException {
        boolean isReturnDay = false;
        Calendar time = Calendar.getInstance();

        Day day = TripUtils.getDayFromTime(getTrip().getDays(), time);

        if (day != null && day.getLocation() == null) {
            isReturnDay = true;
            ParseGeoPoint geoPoint = new ParseGeoPoint();
            geoPoint.setLatitude(latLng.getLatitude());
            geoPoint.setLongitude(latLng.getLongitude());
            day.setLocation(geoPoint);
            day.pin(CURRENT_TRIP);

        }

        RoutePoint routePoint = new RoutePoint();
        routePoint.setPriority(RoutePoint.HIGH_ACCURACY);
        routePoint.setRecordedTime(time.getTime());
        routePoint.setSource(Source.WAH);
        if (getTrip() != null)
            routePoint.setTrip(getTrip());
        if (day != null)
            routePoint.setDay(day);
        ParseGeoPoint geoPoint = new ParseGeoPoint();
        geoPoint.setLatitude(latLng.getLatitude());
        geoPoint.setLongitude(latLng.getLongitude());
        routePoint.setLocation(geoPoint);
        routePoint.pin(CURRENT_TRIP);
        if (isReturnDay)
            return day;
        else
            return null;
    }

    public void addRoutePoint(ParseGeoPoint parseGeoPoint, Day day, Date time) {

        RoutePoint routePoint = new RoutePoint();
        routePoint.setPriority(RoutePoint.HIGH_ACCURACY);
        routePoint.setRecordedTime(time);
        routePoint.setSource(Source.WAH);
        if (getTrip() != null)
            routePoint.setTrip(getTrip());
        if (day != null)
            routePoint.setDay(day);
        routePoint.setLocation(parseGeoPoint);
        routePoint.pinInBackground(CURRENT_TRIP);
    }

    public List<Timeline> getDayTimeLines(int dayOrder, int limit, int skip, String source, String contentType) throws ParseException {
        Day day = getTrip().getDay(dayOrder);
        if (day == null)
            throw TripUtils.createDayNotFoundException(dayOrder);
        return getDayTimeLines(day, limit, skip, source, contentType);
    }

    public List<Timeline> getDayTimeLines(Day day, int limit, int skip, String source, String contentType) throws ParseException {

        ParseQuery<Timeline> returnQuery;

        returnQuery = ParseQuery.getQuery(Timeline.class).include(Timeline.CONTENT).whereEqualTo(Timeline.DAY, day).fromPin(CURRENT_TRIP)
                .orderByAscending(Timeline.DISPLAY_ORDER).setLimit(limit).setSkip(skip);
        if (source != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.SOURCE, source);
        if (contentType != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.CONTENT_TYPE, contentType);
        return returnQuery.find();
    }

    @Override
    public List<Timeline> getTripTimeLines(int limit, int skip, String source, String contentType) throws ParseException {
        ParseQuery<Timeline> returnQuery;

        returnQuery = ParseQuery.getQuery(Timeline.class).include(Timeline.CONTENT).whereEqualTo(Timeline.TRIP, getTrip()).fromPin(CURRENT_TRIP)
                .addAscendingOrder(Timeline.DAY_ORDER).addAscendingOrder(Timeline.DISPLAY_ORDER).setLimit(limit).setSkip(skip);
        if (source != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.SOURCE, source);
        if (contentType != null)
            returnQuery = returnQuery.whereEqualTo(Timeline.CONTENT_TYPE, contentType);
        return returnQuery.find();
    }

    public List<Media> getAlbumMedia(Album album) throws ParseException {
        return ParseQuery.getQuery(Media.class).whereEqualTo(Media.ALBUM, album).orderByAscending(Media.CONTENT_CREATION_TIME).fromPin(CURRENT_TRIP).find();
    }

    public List<RoutePoint> getDayRoutePoints(Day day, int skip, int limit) throws ParseException {
        return ParseQuery.getQuery(RoutePoint.class).fromPin(CURRENT_TRIP)
                .whereEqualTo(RoutePoint.DAY, day).setLimit(limit).setSkip(skip).orderByAscending(RoutePoint.RECORDED_TIME).find();
    }

    @Override
    public List<RoutePoint> getTripRoutePoints(int skip, int limit) throws ParseException {
        return ParseQuery.getQuery(RoutePoint.class).fromPin(CURRENT_TRIP)
                .whereEqualTo(RoutePoint.TRIP, getTrip()).setLimit(limit).setSkip(skip).addAscendingOrder(RoutePoint.DAY_ORDER).addAscendingOrder(RoutePoint.RECORDED_TIME).find();
    }

    @Override
    public Timeline getTimeLine() {
        return timeline;
    }

    public List<RoutePoint> getDayRoutePoints(int dayOrder, int skip) throws ParseException {
        Day day = getTrip().getDay(dayOrder);
        if (day == null)
            throw TripUtils.createDayNotFoundException(dayOrder);

        return getDayRoutePoints(day, skip, 1000);
    }

    public RoutePoint getLatestRoutePoint() throws ParseException {
        List<RoutePoint> points = ParseQuery.getQuery(RoutePoint.class).fromPin(CURRENT_TRIP).setLimit(1).orderByDescending(RoutePoint.RECORDED_TIME).find();
        if (points != null && points.size() > 0)
            return points.get(0);
        return null;
    }

    public void save(ParseObject parseObject) throws ParseException {
        parseObject.pin(CURRENT_TRIP);
    }

    @Override
    public void setSelectedPhotosList(List<GalleryImage> galleryImages) {
        this.galleryImages = galleryImages;
    }

    @Override
    public void setTimeLine(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public List<GalleryImage> getSelectedPhotosList() {
        return galleryImages;
    }

    @Override
    public void setSelectedPhoto(GalleryImage galleryImage) {
        this.galleryImage = galleryImage;
    }

    @Override
    public GalleryImage getSelectedPhoto() {
        return galleryImage;
    }

    @Override
    public void populateMediaSource(List<Media> mediaList) throws ParseException {
        for (Media media : mediaList) {
            media.setMediaSource(media.getThirdPartyUrl());
//            FileLocal filelocal = ParseFileUtils.getLocalFileFromPin(media);
//            if(filelocal != null)
//                media.setMediaSource(filelocal.getLocalUri());
        }
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public void publish() throws ParseException {

    }

    @Override
    public void unpublish() throws ParseException {

    }

    @Override
    public void getTripPeople(final TripAsyncCallback<List<PeopleContact>> tripAsyncCallback) {
        ParseQuery.getQuery(TripPeople.class).whereEqualTo(TripPeople.TRIP, getTrip())
                .whereEqualTo(TripPeople.IN_TRIP, true).fromPin(CURRENT_TRIP).findInBackground(new FindCallback<TripPeople>() {
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
                                FileLocal fileLocal = null;
                                try {
                                    fileLocal = ParseFileUtils.getLocalFileFromPin(tripPeople);
                                } catch (Exception e1) {

                                }
                                if (fileLocal != null)
                                    phContact.setContactImagePath(fileLocal.getLocalUri());
                                tripPeopleList.add(phContact);
                            }
                        }
                    }
                    tripAsyncCallback.onCallBack(tripPeopleList);
                }
            }
        });
    }

    public List<TripPeople> getTripPeopleList() throws ParseException {
        return ParseQuery.getQuery(TripPeople.class).whereEqualTo(TripPeople.TRIP, getTrip()).fromPin(CURRENT_TRIP).find();
    }

    @Override
    public void getTripFeatureImage(final TripAsyncCallback<String> callback) {
        ParseFileUtils.getLocalFileFromPinInBackground(getTrip(), new GetCallback<FileLocal>() {
            @Override
            public void done(FileLocal fileLocal, ParseException e) {
                if (fileLocal != null) {
                    callback.onCallBack(fileLocal.getLocalUri());
                } else {
                    callback.onCallBack("");
                }
            }
        });
    }

    @Override
    public boolean listenForUpdates() {
        return true;
    }

    @Override
    public void updateTripImage(Uri image) throws ParseException {
        ParseFileUtils.pinFileLocally(getTrip(), Trip.FEATURE_IMAGE, image, "trip_feature_image.jpeg");
    }

    private void deleteTimelineMappingsHelper(Timeline timeline) throws ParseException {
        Day day = getTrip().getDay(timeline.getDayOrder());
        DaySummary daySummary = day.getDaySummary();
        TripSummary tripSummary = getTrip().getSummary();

        if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
            List<Media> media = getAlbumMedia((Album) timeline.getContent());
            for (Media media1 : media) {
                ParseFileUtils.clearLocalFiles(media1);
                media1.unpin(CURRENT_TRIP);
                if (!TextUtils.isEmpty(media1.getObjectId())) {
                    media1.deleteEventually();
                }
            }
        } else if (Timeline.CHECK_IN_CONTENT.equals(timeline.getContentType())) {
            ParseFileUtils.clearLocalFiles(timeline.getContent());
        }

        if (Source.FB.equals(timeline.getSource())) {
            daySummary.addFacebook(-1);
            tripSummary.addFacebook(-1);
        } else if (Source.INSTAGRAM.equals(timeline.getSource())) {
            daySummary.addInstagram(-1);
            tripSummary.addInstagram(-1);
        } else if (Source.TWITTER.equals(timeline.getSource())) {
            daySummary.addTwitter(-1);
            tripSummary.addTwitter(-1);
        } else if (Source.WAH.equals(timeline.getSource())) {
            if (Timeline.CHECK_IN_CONTENT.equals(timeline.getContentType())) {
                daySummary.addCheckIns(-1);
                tripSummary.addCheckIns(-1);
            } else if (Timeline.NOTE_CONTENT.equals(timeline.getContentType())) {
                daySummary.addNotes(-1);
                tripSummary.addNotes(-1);
            } else if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
                Album album = (Album) timeline.getContent();
                daySummary.addPhotos(-album.getMediaCount());
                tripSummary.addPhotos(-album.getMediaCount());
                daySummary.addPublicPhotos(-album.getPublicMediaCount());
                tripSummary.addPublicPhotos(-album.getPublicMediaCount());
            }
        }
        daySummary.pin(CURRENT_TRIP);
        tripSummary.pin(CURRENT_TRIP);
    }

    @Override
    public void deleteTimelineOnly(Timeline timeline) throws ParseException {
        deleteTimelineMappingsHelper(timeline);

        timeline.getContent().unpin(CURRENT_TRIP);
        timeline.unpin(CURRENT_TRIP);
        if (!TextUtils.isEmpty(timeline.getObjectId())) {
            timeline.deleteEventually();
        }
    }

    @Override
    public void deleteTimeLine(Timeline timeline) throws ParseException {

        Day day = getTrip().getDay(timeline.getDayOrder());

        deleteTimelineMappingsHelper(timeline);

        int timelineOrder = timeline.getDisplayOrder();

        List<Timeline> timeLinesList = getDayTimeLines(day, 1000, timelineOrder + 1, null, null);

        for (Timeline timeline1 : timeLinesList) {
            timeline1.setDisplayOrder(timeline1.getDisplayOrder() - 1);
        }

        ParseObject.pinAll(CURRENT_TRIP, timeLinesList);

        timeline.getContent().unpin(CURRENT_TRIP);
        timeline.unpin(CURRENT_TRIP);
        if (!TextUtils.isEmpty(timeline.getObjectId())) {
            timeline.deleteEventually();
        }
    }

    @Override
    public void deleteMedia(Timeline timeline, Media media) throws ParseException {
        ParseFileUtils.clearLocalFiles(media);
        media.unpin(CURRENT_TRIP);
        if (!TextUtils.isEmpty(media.getObjectId())) {
            media.deleteEventually();
        }
        Album album = (Album) timeline.getContent();
        album.addMediaCount(-1);
        if (!media.isPrivate())
            album.addPublicMediaCount(-1);
        //TODO: handle Album start/end time and location
        album.pin(CURRENT_TRIP);

        if (Source.WAH.equals(timeline.getSource())) {
            DaySummary daySummary = getTrip().getDay(timeline.getDayOrder()).getDaySummary();
            if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
                daySummary.addPhotos(-1);
                getTrip().getSummary().addPhotos(-1);

                if (!media.isPrivate()) {
                    daySummary.addPublicPhotos(-1);
                    getTrip().getSummary().addPublicPhotos(-1);
                }

                daySummary.pinInBackground(CURRENT_TRIP);
                getTrip().getSummary().pinInBackground(CURRENT_TRIP);
            }
        }
    }

    @Override
    public void saveTripPeople(List<PeopleContact> peopleContacts) throws ParseException {
        if (peopleContacts != null && peopleContacts.size() > 0) {
            List<TripPeople> oldPeople = ParseQuery.getQuery(TripPeople.class).whereEqualTo(TripPeople.TRIP, getTrip())
                    .whereEqualTo(TripPeople.IN_TRIP, true).fromPin(CURRENT_TRIP).find();
            if (oldPeople != null && oldPeople.size() > 0) {
                ParseObject.unpinAll(CURRENT_TRIP, oldPeople);
            }
            List<TripPeople> people = new ArrayList<>();
            for (PeopleContact peopleContact : peopleContacts) {
                TripPeople tripPeople = new TripPeople();
                tripPeople.setName(peopleContact.getName());
                tripPeople.setInTrip(true);
                tripPeople.setIdentifier(peopleContact.getIdentifier());
                tripPeople.setTrip(getTrip());
                String imageUri = peopleContact.getImageUri();
                if (peopleContact.getType() == PeopleContact.Type.FB) {
                    tripPeople.setType(TripPeople.FACEBOOK_TYPE);
                    if (!TextUtils.isEmpty(imageUri))
                        tripPeople.setImageUrl(peopleContact.getImageUri());
                    people.add(tripPeople);
                } else if (peopleContact.getType() == PeopleContact.Type.PHONE) {
                    tripPeople.setType(TripPeople.PHONE_BOOK_TYPE);
                    tripPeople.pin(CURRENT_TRIP);
                    if (!TextUtils.isEmpty(imageUri))
                        ParseFileUtils.pinFileLocally(tripPeople, TripPeople.IMAGE, Uri.parse(imageUri), "");
                }
            }
            if (people.size() > 0)
                ParseObject.pinAll(CURRENT_TRIP, people);
        }
    }

    private Media selectedMedia;

    public Media getSelectedMedia() {
        return selectedMedia;
    }

    public void setSelectedMedia(Media selectedMedia) {
        this.selectedMedia = selectedMedia;
    }

    @Override
    public void saveAll(List<? extends ParseObject> parseObjectList) throws ParseException {
        ParseObject.pinAll(CURRENT_TRIP, parseObjectList);
    }

    @Override
    public int getMenuLayout() {
        if (getTrip().isFinished()) {
            return R.menu.menu_trip_finished;
        }
        return R.menu.menu_trip;
    }

    @Override
    public Timeline loadTimeline(String timelineId) {
        return null;
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

        if (parseGeoPoint != null) {
            media.setLocation(parseGeoPoint);
        }

        media.pin(CURRENT_TRIP);

        Day day = getTrip().getDay(timeline.getDayOrder());

        DaySummary daySummary = day.getDaySummary();
        TripSummary tripSummary = getTrip().getSummary();

        Album album = (Album) timeline.getContent();

        if (originalPrivacy != isPrivate) {
            if (originalPrivacy && !isPrivate) {
                daySummary.addPublicPhotos(1);
                tripSummary.addPublicPhotos(1);
                album.addPublicMediaCount(1);
            } else if (!originalPrivacy && isPrivate) {
                daySummary.addPublicPhotos(-1);
                tripSummary.addPublicPhotos(-1);
                album.addPublicMediaCount(-1);
            }
            daySummary.pinInBackground(CURRENT_TRIP);
            tripSummary.pinInBackground(CURRENT_TRIP);
        }

        if (parseGeoPoint != null) {
            MediaGroup mediaGroup = new MediaGroup();
            for (Media media1 : album.getMedia()) {
                TripUtils.addMediaToFrequency(mediaGroup, media1);
            }
            TripUtils.addLocationToAlbum(album, mediaGroup);
            if (!MapUtils.isEqual(previousGeoPoint, parseGeoPoint))
                addRoutePoint(parseGeoPoint, day, media.getContentCreatedDate());
        }
        album.pin(CURRENT_TRIP);
    }

    public void saveSelectedMediaLocation(String address, ParseGeoPoint parseGeoPoint) {
        Media media = getSelectedMedia();
        Timeline timeline = getTimeLine();

        ParseGeoPoint previousGeoPoint = media.getLocation();

        media.setAddress(address);
        media.setLocation(parseGeoPoint);

        media.pinInBackground(CURRENT_TRIP);

        Album album = (Album) timeline.getContent();

        Day day = getTrip().getDay(timeline.getDayOrder());

        MediaGroup mediaGroup = new MediaGroup();
        for (Media media1 : album.getMedia()) {
            TripUtils.addMediaToFrequency(mediaGroup, media1);
        }
        TripUtils.addLocationToAlbum(album, mediaGroup);
        if (!MapUtils.isEqual(previousGeoPoint, parseGeoPoint))
            addRoutePoint(parseGeoPoint, day, media.getContentCreatedDate());

        album.pinInBackground(CURRENT_TRIP);
    }

    public void saveSelectedMediaAddress(String address, Media media, Timeline timeline) {

        media.setAddress(address);

        media.pinInBackground(CURRENT_TRIP);

        Album album = (Album) timeline.getContent();

        if (album.getLocation() != null && media.getLocation() != null) {
            if (album.getLocation().getLatitude() == media.getLocation().getLatitude() &&
                    album.getLocation().getLongitude() == media.getLocation().getLongitude()) {
                album.setLocationText(address);
                album.pinInBackground(CURRENT_TRIP);
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

        Note note1 = (Note) timeline.getContent();

        Day day = getTrip().getDay(timeline.getDayOrder());

        if (!TextUtils.isEmpty(note))
            note1.setContent(note);

        if (!TextUtils.isEmpty(address))
            note1.setLocationText(address);

        ParseGeoPoint previousPoint = note1.getLocation();

        if (parseGeoPoint != null)
            note1.setLocation(parseGeoPoint);

        if (MapUtils.isEqual(previousPoint, parseGeoPoint))
            addRoutePoint(parseGeoPoint, day, timeline.getContentTime());

        if (inSync)
            note1.pin(CURRENT_TRIP);
        else
            note1.pinInBackground(CURRENT_TRIP);
    }
}
