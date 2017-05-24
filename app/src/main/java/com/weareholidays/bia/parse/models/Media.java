package com.weareholidays.bia.parse.models;

import android.text.TextUtils;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 04/06/15.
 */
@ParseClassName("Media")
public class Media  extends ParseObject{

    public static String CONTENT = "content";
    private static String CAPTION = "caption";
    public static String MEDIA_TAGS = "mediaTags";
    public static String LOCATION = "location";
    public static String PRIVACY = "private";
    public static String ALBUM = "album";
    private static String ADDRESS = "address";
    public static String CONTENT_CREATION_TIME = "contentCreationTime";
    private static String CONTENT_SIZE = "contentSize";
    private static String MEDIA_WIDTH = "mediaWidth";
    private static String MEDIA_HEIGHT = "mediaHeight";
    private static String THIRD_PARTY_ID = "thirdPartyId";
    private static String THIRD_PARTY_URL = "thirdPartyUrl";

    private String mediaSource;

    private boolean fetchingAddress;

    public boolean isFetchingAddress() {
        return fetchingAddress;
    }

    public void setFetchingAddress(boolean fetchingAddress) {
        this.fetchingAddress = fetchingAddress;
    }

    public String getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    public String getCaption(){
        return getString(CAPTION);
    }

    public void setCaption(String caption){
        put(CAPTION,caption);
    }

    public ParseFile getContent(){
        return getParseFile(CONTENT);
    }

    public void setContent(ParseFile parseFile){
        put(CONTENT,parseFile);
    }

    public List<String> getTags(){
        List<String> tagsList = new ArrayList<>();
        String mediaTags = getString(MEDIA_TAGS);
        if(!TextUtils.isEmpty(mediaTags)){
            String[] tagSplits = mediaTags.split(" ");
            for(String split : tagSplits){
                if(!TextUtils.isEmpty(split))
                    tagsList.add(split);
            }
        }
        return tagsList;
    }

    public void setTags(List<String> tags){
        if(tags == null || tags.size() == 0){
            put(MEDIA_TAGS,"");
            return;
        }
        String tag = "";

        for(String t: tags){
            tag += t + " ";
        }
        put(MEDIA_TAGS,tag);
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint(LOCATION);
    }

    public void setLocation(ParseGeoPoint parseGeoPoint){
        put(LOCATION, parseGeoPoint);
    }

    public boolean isPrivate(){
        return getBoolean(PRIVACY);
    }

    public void setPrivate(boolean privacy){
        put(PRIVACY,privacy);
    }

    public void setAlbum(Album album){
        put(ALBUM,album);
    }

    public String getAddress(){
        return getString(ADDRESS);
    }

    public void setAddress(String address){
        put(ADDRESS,address);
    }

    public Date getContentCreatedDate(){
        return getDate(CONTENT_CREATION_TIME);
    }

    public void setContentCreatedDate(Date date){
        put(CONTENT_CREATION_TIME,date);
    }

    public long getContentSize(){
        return getLong(CONTENT_SIZE);
    }

    public void setContentSize(long contentSize){
        put(CONTENT_SIZE,contentSize);
    }

    public int getMediaHeight(){
        return getInt(MEDIA_HEIGHT);
    }

    public void setMediaHeight(int height){
        put(MEDIA_HEIGHT,height);
    }

    public int getMediaWidth(){
        return getInt(MEDIA_WIDTH);
    }

    public void setMediaWidth(int width){
        put(MEDIA_WIDTH,width);
    }

    public String getThirdPartyId(){
        return getString(THIRD_PARTY_ID);
    }

    public void setThirdPartyId(String id){
        put(THIRD_PARTY_ID,id);
    }

    public String getThirdPartyUrl(){
        return getString(THIRD_PARTY_URL);
    }

    public void setThirdPartyUrl(String url){
        put(THIRD_PARTY_URL,url);
    }
}
