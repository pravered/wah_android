package com.weareholidays.bia.social.facebook.utils;

import android.os.Bundle;
import android.text.TextUtils;

import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.social.facebook.models.FacebookMedia;
import com.weareholidays.bia.social.facebook.models.FacebookPermission;
import com.weareholidays.bia.social.facebook.models.FacebookPost;
import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import twitter4j.auth.AccessToken;

/**
 * Created by Teja on 06/06/15.
 */
public class FacebookUtils {

    public static int ACTIVITY_REQUEST_CODE_OFFSET = 1000;

    private static String TAG = "FacebookUtils";

    public static List<String> getFacebookReadPermissions(){
        List<String> permissions = new ArrayList<>();
        permissions.add("email");
        permissions.add("public_profile");
        permissions.add("user_friends");
        permissions.add("user_posts");
        permissions.add("user_photos");
        //permissions.add("user_videos");
        return permissions;
    }

    public static List<String> getFacebookPublishPermissions(){
        List<String> permissions = new ArrayList<>();
        permissions.add("publish_actions");
        return permissions;
    }

    public static boolean hasPublishPermissions(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null && accessToken.getPermissions().contains("publish_actions")){
            return true;
        }
        return false;
    }

    public static String getPublishPostUrl(){
        return "/me/feed";
    }

    public static Bundle getPublishPostBundle(Trip trip){
        Bundle bundle = new Bundle();
        bundle.putString("message", "Completed a trip to : \"" + trip.getName() + "\"");
        bundle.putString("link", ShareUtils.getTripShareUrl(trip));
        return bundle;
    }

    public static String getUserPhotosUrl(){
        return "/me/photos";
    }

    public static Bundle getUserPhotosBundle(Date time){
        Bundle bundle = new Bundle();
        bundle.putString("type","uploaded");
        bundle.putString("fields","id,name,images,place,created_time");
        bundle.putString("date_format","U");
        bundle.putInt("limit",50);
        bundle.putLong("since",time.getTime()/1000);
        return bundle;
    }

    public static String getFriendsUrl(){
        return "/me/friends";
    }

    public static Bundle getUserFriendsBundle(){
        Bundle bundle = new Bundle();
        bundle.putString("fields","id,name,picture");
        return bundle;
    }

    public static String getPostsUrl(){
        return "/me/posts";
    }

    public static Bundle getPostsBundle(){
        Bundle bundle = new Bundle();
        bundle.putString("fields","id,message,story,created_time,type,updated_time,from,place");
        bundle.putString("date_format","U");
        return bundle;
    }

    public static Bundle getPostsBundle(Date date){
        Bundle bundle = getPostsBundle();
        bundle.putLong("since", date.getTime() / 1000);
        return bundle;
    }

    public static String getPostAttachments(String id){
        return "/" + id + "/attachments";
    }

    public static String getPermissionsUrl(){
        return "/me/permissions";
    }

    public static List<String> getAvailablePermissions(List<FacebookPermission> permissions){
        List<String> pts = new ArrayList<>();
        for(FacebookPermission permission : permissions){
            if(!TextUtils.isEmpty(permission.getPermission())){
                if(FacebookPermission.PERMISSION_GRANTED.equals(permission.getStatus())){
                    pts.add(permission.getPermission());
                }
            }
        }
        return pts;
    }

    public static List<String> missingPermissions(List<FacebookPermission> permissions){
        List<String> availablePermissions = getAvailablePermissions(permissions);
        List<String> missingPermissions = new ArrayList<>();
        for(String perm: getFacebookReadPermissions()){
            if(!availablePermissions.contains(perm))
                missingPermissions.add(perm);
        }
        return missingPermissions;
    }

    public static boolean permissionsAvailable(List<FacebookPermission> permissions){
        if(missingPermissions(permissions).size() == 0)
            return true;
        return false;
    }

    public static List<FacebookPost> getPosts(JSONObject jsonObject){
        return FacebookPost.parsePosts(jsonObject);
    }

    public static String getNextPageUrl(JSONObject jsonObject){
        try {
            JSONObject paging = jsonObject.getJSONObject("paging");
            if(paging != null){
                return paging.getString("next");
            }
        } catch (JSONException e) {

        }
        return null;
    }

    public static List<FacebookMedia> getPostAttachments(JSONObject jsonObject){
        List<FacebookMedia> media = new ArrayList<>();
        try {
            JSONArray data = jsonObject.getJSONArray("data");
            for(int i=0; i < data.length(); i++){
                JSONObject dt = data.getJSONObject(i);
                if(dt.has("media")){
                    JSONObject md = dt.getJSONObject("media");
                    media.add(FacebookMedia.parsePostMedia(dt));
                }
                if(dt.has("subattachments")){
                    JSONObject subAttachments = dt.getJSONObject("subattachments");
                    media.addAll(FacebookMedia.parsePostMedia(subAttachments.getJSONArray("data")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return media;
    }

    public static List<GalleryImage> getUserImages(JSONObject jsonObject){
        List<FacebookMedia> facebookMediaList = FacebookMedia.parseImageMediaList(jsonObject);
        List<GalleryImage> galleryImageList = new ArrayList<>();
        for(FacebookMedia facebookMedia : facebookMediaList){
            GalleryImage galleryImage = new GalleryImage();
            galleryImage.setCaption(facebookMedia.getCaption());
            galleryImage.setAddress(facebookMedia.getLocationText());
            if(facebookMedia.getLocation() != null){
                galleryImage.setLatitude(facebookMedia.getLocation().latitude);
                galleryImage.setLongitude(facebookMedia.getLocation().longitude);
            }
            galleryImage.setUri(facebookMedia.getMediaSource());
            galleryImage.setDateTaken(facebookMedia.getCreatedTime().getTime());
            galleryImage.setMediaHeight(facebookMedia.getMediaHeight());
            galleryImage.setMediaWidth(facebookMedia.getMediaWidth());
            galleryImage.setType(GalleryImage.Type.FB);
            galleryImage.setSourceId(facebookMedia.getId());
            galleryImageList.add(galleryImage);
        }
        return galleryImageList;
    }

    public static void saveFacebookId(ParseCustomUser customUser){
        try {
            JSONObject authData = customUser.getAuthData();
            String facebookId = "";
            if(authData != null && !authData.isNull("facebook")){
                try {
                    JSONObject facebookAuth = authData.getJSONObject("facebook");
                    facebookId = facebookAuth.getString("id");
                } catch (Exception e) {

                }
            }
            if(TextUtils.isEmpty(facebookId))
                facebookId = "";
            customUser.setFacebookId(facebookId);
            customUser.saveEventually();
        } catch (Exception e){

        }
    }
}
