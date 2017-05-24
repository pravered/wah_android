package com.weareholidays.bia.models;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by kapil on 2/6/15.
 */
public class GalleryImage implements Serializable {

    private boolean disabled;
    private boolean selected;
    private boolean addPhotoPlaceholder;
    private String uri;
    private long size;
    private long dateTaken;
    private String caption = "";
    private double latitude;
    private double longitude;
    private String address = "";
    private boolean privacy;
    private List<String> tags;
    private String bucketId;
    private int mediaWidth;
    private int mediaHeight;
    private boolean currentSelection;
    //this field saves the ids for photos from fb/instagram
    private String sourceId;
    private Type type;
    public enum Type{
        PHONE, FB, INSTAGRAM
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }


    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }


    public GalleryImage(boolean addPhotoPlaceholder){
        this.addPhotoPlaceholder = addPhotoPlaceholder;
    }


    public GalleryImage(){

    }

    public GalleryImage(String uri) {
        this.uri = uri;
        selected = false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getUri() {
        return uri;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public int getMediaWidth() {
        return mediaWidth;
    }

    public void setMediaWidth(int mediaWidth) {
        this.mediaWidth = mediaWidth;
    }

    public int getMediaHeight() {
        return mediaHeight;
    }

    public void setMediaHeight(int mediaHeight) {
        this.mediaHeight = mediaHeight;
    }

    public boolean isAddPhotoPlaceholder() {
        return addPhotoPlaceholder;
    }

    public boolean isCurrentSelection() {
        return currentSelection;
    }

    public void setCurrentSelection(boolean currentSelection) {
        this.currentSelection = currentSelection;
    }

    public String getName(){
        File file = new File(getUri());

        return file.getName();
    }
}

