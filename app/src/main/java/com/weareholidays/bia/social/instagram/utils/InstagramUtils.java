package com.weareholidays.bia.social.instagram.utils;

import android.text.TextUtils;

import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.social.instagram.models.InstagramMedia;
import com.weareholidays.bia.social.instagram.models.InstagramPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 22/06/15.
 */
public class InstagramUtils {

    public static String getUserPostsUrl(Date time, String accessToken){
        return "https://api.instagram.com/v1/users/self/media/recent/?access_token="
                + accessToken + "&min_timestamp=" + (time.getTime()/1000) + "&count=20";
    }

    public static List<InstagramPost> getInstagramPosts(JSONObject jsonObject) throws JSONException {
        List<InstagramPost> posts = new ArrayList<>();
        if(jsonObject.has("data")){
            List<InstagramPost> allPosts = InstagramPost.parsePosts(jsonObject.getJSONArray("data"));
            for(InstagramPost post : allPosts){
                if(post.getType() == InstagramPost.Type.PHOTO){
                    posts.add(post);
                }
            }
        }
        return posts;
    }

    public static List<GalleryImage> getInstagramGalleryImages(List<InstagramPost> instagramPosts){
        List<GalleryImage> galleryImageList = new ArrayList<>();
        for(InstagramPost post: instagramPosts){
            GalleryImage galleryImage = new GalleryImage();
            if(!TextUtils.isEmpty(post.getMessage()))
                galleryImage.setCaption(post.getMessage());
            if(!TextUtils.isEmpty(post.getLocationText()))
                galleryImage.setAddress(post.getLocationText());
            if(post.getLocation() != null){
                galleryImage.setLongitude(post.getLocation().longitude);
                galleryImage.setLatitude(post.getLocation().latitude);
            }
            galleryImage.setDateTaken(post.getCreatedTime().getTime());
            if(post.getMedia() != null && post.getMedia().size() > 0){
                InstagramMedia media = post.getMedia().get(0);
                galleryImage.setUri(media.getMediaSource());
                galleryImage.setMediaHeight(media.getMediaHeight());
                galleryImage.setMediaWidth(media.getMediaWidth());
                galleryImage.setType(GalleryImage.Type.INSTAGRAM);
                galleryImage.setSourceId(post.getId());
                galleryImageList.add(galleryImage);
            }
        }
        return galleryImageList;
    }
}
