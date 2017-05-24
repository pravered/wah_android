package com.weareholidays.bia.parse.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
//import com.parse.codec.binary.Hex;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.activities.journal.photo.PhotoTimelineActivity;
import com.weareholidays.bia.activities.journal.trip.TripActivity;
import com.weareholidays.bia.activities.search.ArticleFragment;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Notification;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.utils.DebugUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Teja on 15/07/15.
 */
public class ShareUtils {

    public static final String ACTION_TYPE = "actionType";
    public static final String ACTION_PARAMS = "actionParams";
    public static final String SHARER_PREFS = "sharer_prefs";
    public static final String SHARER_ID = "sharer_id";


    public static String getTripShareUrl(Trip trip) {
        return "http://www.bia-app.com/app?type=trip&trip=" + trip.getObjectId() + "&sharer=" + ParseUser.getCurrentUser().getObjectId();
    }

    public static String generateHash(String key, String secretKey, long timeStamp) {
        String encode = key + secretKey + timeStamp;
        String encodedText = "";
        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = encode.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytesOfMessage);
            //encodedText = new String(Hex.encodeHex(digest));
            //encodedText = new String(Base64.encode(digest,Base64.DEFAULT));
        } catch (NoSuchAlgorithmException e) {
            DebugUtils.logException(e);
        } catch (UnsupportedEncodingException e) {
            DebugUtils.logException(e);
        }
        return encodedText;
    }

    public static boolean isValidHash(String key, long timeStamp, String secretKey, String md5) {
        String newMd5 = generateHash(key, secretKey, timeStamp);
        if (newMd5.equals(md5))
            return true;
        return false;
    }

    public static String getEncodedTripShareUrl(Trip trip) {
        StringBuilder sb = new StringBuilder();
        long timeInMillis = System.currentTimeMillis();
        sb.append(getTripShareUrl(trip));
        sb.append("&t=");
        sb.append(timeInMillis);
        sb.append("&hash=");
        sb.append(generateHash(trip.getObjectId(), trip.getSecretKey(), timeInMillis));
        return sb.toString();
    }

    public static String getEncodedPhotoShareUrl(Trip trip, Timeline timeline, Media media) {
        StringBuilder sb = new StringBuilder();
        long timeInMillis = System.currentTimeMillis();
        sb.append(getPhotoShareUrl(trip, timeline, media));
        sb.append("&t=");
        sb.append(timeInMillis);
        sb.append("&hash=");
        sb.append(generateHash(trip.getObjectId(), trip.getSecretKey(), timeInMillis));
        return sb.toString();
    }

    public static String getPhotoShareUrl(Trip trip, Timeline timeline, Media media) {
        return "http://www.bia-app.com/app?trip=" + trip.getObjectId() + "&type=photo&timeline="
                + timeline.getObjectId() + "&photo=" + media.getObjectId() + "&sharer=" + ParseUser.getCurrentUser().getObjectId();
    }

    public static ParseQuery<ParseCustomUser> getParseShareUser(String identifier, PeopleContact.Type type) {
        if (type == PeopleContact.Type.PHONE) {
            return ParseQuery.getQuery(ParseCustomUser.class).whereEqualTo(ParseCustomUser.PHONE, normalizePhone(identifier));
        }
        //Assuming it's Facebook Type as we only support PHONE and FB as of now.
        return ParseQuery.getQuery(ParseCustomUser.class).whereEqualTo(ParseCustomUser.FACEBOOK_ID, identifier);
    }

    public static boolean isValidPhone(String phone) {
        String normalizedPhone = normalizePhone(phone);
        if (!TextUtils.isEmpty(normalizedPhone) && normalizedPhone.length() == 10 && !normalizedPhone.startsWith("0"))
            return true;
        return false;
    }

    private static String normalizePhone(String phone) {
        if (TextUtils.isEmpty(phone))
            return "";
        String normalizedPhone = phone.replaceAll("[^\\d]", "");
        if (normalizedPhone.length() == 10)
            return normalizedPhone;
        //Remove first character (probably STD Code 0)
        if (normalizedPhone.length() == 11 && normalizedPhone.startsWith("0"))
            return normalizedPhone.substring(1);
        //Remove first 2 characters(probably country code)
        if (normalizedPhone.length() == 12) {
            return normalizedPhone.substring(2);
        }
        return phone;
    }

    public static void sendTripShareNotification(ParseCustomUser user, Trip trip) {

        ParseCustomUser currentUser = ParseCustomUser.getCurrentUser();

        String notificationText = "<b>" + currentUser.getName() + "</b>" + " shared trip <b>" + trip.getName() + "</b> with you.";

        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();

        query.whereEqualTo("username", user.getUsername());
        push.setQuery(query);
        Notification notification = new Notification();
        notification.setContent(notificationText);
        notification.setContentTime(Calendar.getInstance().getTime());
        notification.setIsRead(false);
        notification.setNotifier(currentUser);
        notification.setActionType(Notification.TRIP_OPEN_ACTION);
        notification.setActionParams(getTripNotificationActionParams(trip));
        notification.setUser(user.getUsername());
        notification.saveInBackground();

        JSONObject obj = new JSONObject();
        try {
//            obj.put("alert", Html.fromHtml(Html.fromHtml(notificationText).toString()));
            obj.put("alert", Html.fromHtml(notificationText).toString());
            obj.put(ACTION_TYPE, notification.getActionType());
            obj.put(ACTION_PARAMS, notification.getActionParams());
        } catch (Exception e) {

        }
        push.setData(obj);
        push.sendInBackground();
    }

    public static void sendLoginNotificationToSharer(String sharerId) {

        ParseCustomUser sharer = getSharerDetails(sharerId);
        if (sharer == null)
            return;

        ParseCustomUser currentUser = ParseCustomUser.getCurrentUser();

        String notificationText = "<b>" + currentUser.getName() + "</b>" + " logged in Bia with your refferal code.";

        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();

        query.whereEqualTo("username", sharer.getUsername());
        push.setQuery(query);
        Notification notification = new Notification();
        notification.setContent(notificationText);
        notification.setContentTime(Calendar.getInstance().getTime());
        notification.setIsRead(true);
        notification.setNotifier(currentUser);
        notification.setUser(sharer.getUsername());
        notification.saveInBackground();

        JSONObject obj = new JSONObject();
        try {
            obj.put("alert", Html.fromHtml(notificationText).toString());
            obj.put(ACTION_TYPE, notification.getActionType());
            obj.put(ACTION_PARAMS, notification.getActionParams());
        } catch (Exception e) {

        }
        push.setData(obj);
        push.sendInBackground();
    }

    public static void sendTripCompleteNotificationToSharer(String sharerId) {

        ParseCustomUser sharer = getSharerDetails(sharerId);
        if (sharer == null)
            return;
        ParseCustomUser currentUser = ParseCustomUser.getCurrentUser();

        String notificationText = "<b>" + currentUser.getName() + "</b>" + " completed trip.";

        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();

        query.whereEqualTo("username", sharer.getUsername());
        push.setQuery(query);
        Notification notification = new Notification();
        notification.setContent(notificationText);
        notification.setContentTime(Calendar.getInstance().getTime());
        notification.setIsRead(true);
        notification.setNotifier(currentUser);
        notification.setUser(sharer.getUsername());
        notification.saveInBackground();

        JSONObject obj = new JSONObject();
        try {
            obj.put("alert", Html.fromHtml(notificationText).toString());
            obj.put(ACTION_TYPE, notification.getActionType());
            obj.put(ACTION_PARAMS, notification.getActionParams());
        } catch (Exception e) {

        }
        push.setData(obj);
        push.sendInBackground();
    }

    public static void sendTripPeopleNotification(ParseCustomUser user, Trip trip) {

        ParseCustomUser currentUser = ParseCustomUser.getCurrentUser();

        String notificationText = "<b>" + currentUser.getName() + "</b>" + " added you to trip <b>" + trip.getName() + "</b>";

        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();

        query.whereEqualTo("username", user.getUsername());
        push.setQuery(query);
        Notification notification = new Notification();
        notification.setContent(notificationText);
        notification.setContentTime(Calendar.getInstance().getTime());
        notification.setIsRead(false);
        notification.setNotifier(currentUser);
        notification.setActionType(Notification.TRIP_OPEN_ACTION);
        notification.setActionParams(getTripNotificationActionParams(trip));
        notification.setUser(user.getUsername());
        notification.saveInBackground();

        JSONObject obj = new JSONObject();
        try {
            obj.put("alert", Html.fromHtml(notificationText).toString());
            obj.put(ACTION_TYPE, notification.getActionType());
            obj.put(ACTION_PARAMS, notification.getActionParams());
        } catch (Exception e) {

        }
        push.setData(obj);
        push.sendInBackground();
    }

    public static void sendTripPhotoShareNotification(ParseCustomUser user, Trip trip, Timeline timeline, Media media) {

        ParseCustomUser currentUser = ParseCustomUser.getCurrentUser();

        String notificationText = "<b>" + currentUser.getName() + "</b>" + " shared " + ((media.getCaption() != null && !media.getCaption().isEmpty()) ? "photo <b>" + media.getCaption() + "</b>" : "a photo") + " with you.";

        ParsePush push = new ParsePush();
        ParseQuery query = ParseInstallation.getQuery();

        query.whereEqualTo("username", user.getUsername());
        push.setQuery(query);
        Notification notification = new Notification();
        notification.setContent(notificationText);
        notification.setContentTime(Calendar.getInstance().getTime());
        notification.setIsRead(false);
        notification.setNotifier(currentUser);
        notification.setActionType(Notification.ALBUM_IMAGE_OPEN_ACTION);
        notification.setActionParams(getTripPhotoNotificationActionParams(trip, timeline, media));
        notification.setUser(user.getUsername());
        notification.saveInBackground();

        JSONObject obj = new JSONObject();
        try {
            obj.put("alert", Html.fromHtml(Html.fromHtml(notificationText).toString()));
            obj.put(ACTION_TYPE, notification.getActionType());
            obj.put(ACTION_PARAMS, notification.getActionParams());
        } catch (Exception e) {

        }
        push.setData(obj);
        push.sendInBackground();
    }

    public static String getTripNotificationActionParams(Trip trip) {
        return "trip=" + trip.getObjectId();
    }

    public static String getTripPhotoNotificationActionParams(Trip trip, Timeline timeline, Media media) {
        return "trip=" + trip.getObjectId() + "&timeline="
                + timeline.getObjectId() + "&photo=" + media.getObjectId();
    }

    public static HashMap<String, String> parseNotificationParams(String params) {
        HashMap<String, String> paramMap = new HashMap<>();
        if (TextUtils.isEmpty(params)) {
            return paramMap;
        }
        String[] splits = params.split("&");
        for (String split : splits) {
            if (!TextUtils.isEmpty(split)) {
                String[] pSplits = split.split("=");
                if (pSplits.length == 2) {
                    paramMap.put(pSplits[0], pSplits[1]);
                }
            }
        }
        return paramMap;
    }

    public static Intent getNotificationRedirectIntent(Notification notification, Context context) {
        return getNotificationRedirectIntent(notification.getActionType(), notification.getActionParams(), context);
    }

    public static Intent getNotificationRedirectIntent(String actionType1, String actionParams1, Context context) {
        if (Notification.TRIP_OPEN_ACTION.equals(actionType1)) {
            HashMap<String, String> actionParams = parseNotificationParams(actionParams1);
            String tripKey = "";
            if (actionParams.containsKey("trip"))
                tripKey = actionParams.get("trip");
            if (TextUtils.isEmpty(tripKey))
                return null;
            return getTripShowIntent(context, tripKey);
        }
        if (Notification.ALBUM_IMAGE_OPEN_ACTION.equals(actionType1)) {
            HashMap<String, String> actionParams = parseNotificationParams(actionParams1);
            String tripKey = "";
            String timelineKey = "";
            String mediaKey = "";
            if (actionParams.containsKey("trip"))
                tripKey = actionParams.get("trip");
            if (actionParams.containsKey("timeline"))
                timelineKey = actionParams.get("timeline");
            if (actionParams.containsKey("photo"))
                mediaKey = actionParams.get("photo");
            if (!TextUtils.isEmpty(tripKey) && !TextUtils.isEmpty(timelineKey) && !TextUtils.isEmpty(mediaKey))
                return getMediaShowIntent(context, tripKey, timelineKey, mediaKey);
            else
                return null;
        }
        if(Notification.ARTICLE_OPEN_ACTION.equals(actionType1)){
            HashMap<String, String> actionParams = parseNotificationParams(actionParams1);
            String url = "";

            if(actionParams.containsKey("url")){
                url = actionParams.get("url");
            }
            return getArticleIntent(context, url);
        }

        return null;
    }

    private static Intent getArticleIntent(Context context, String url) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(ArticleFragment.ARTICLE_URL,url);
        return intent;
    }

    public static Intent getMediaShowIntent(Context context, String tripId, String timelineKey, String mediaKey) {
        Intent intent = new Intent(context, PhotoTimelineActivity.class);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripId);
        intent.putExtra(TripOperations.TRIP_ALBUM_ARG, timelineKey);
        intent.putExtra(TripOperations.TRIP_MEDIA_ARG, mediaKey);
        return intent;
    }

    public static Intent getTripShowIntent(Context context, String tripId) {
        Intent intent = new Intent(context, TripActivity.class);
        intent.putExtra(TripOperations.TRIP_KEY_ARG, tripId);
        return intent;
    }

    public static String getPlayStoreUrl(Context context) {
        String appPackageName = context.getPackageName();
        return "http://bia-app.com/?action=download&sharer=" + ParseUser.getCurrentUser().getObjectId();
    }

    public static void openPlayStore(Context context) {
        String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static ParseCustomUser getSharerDetails(String objectId) {
        ParseCustomUser user = null;
        ParseQuery<ParseCustomUser> query = ParseQuery.getQuery(ParseCustomUser.class);
        try {
            List<ParseCustomUser> existingUsers = query.whereEqualTo("objectId", objectId).find();
            if (existingUsers != null && existingUsers.size() != 0) {
                user = existingUsers.get(0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return user;
    }
}
