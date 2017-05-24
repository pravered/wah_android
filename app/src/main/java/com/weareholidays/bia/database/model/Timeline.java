package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class Timeline {
    private String albumContent;
    private String daySummaryDummyContent;
    private String intercityTravelLocationPin;
    private String dayLocationPin;
    private String checkInContent;
    private String noteContent;
    private String contentType;
    private long contentTimeStamp;
    private int displayOrder;
    private String source;
    private Day day;
    private Object content;
    private String thirdPartyId;
    private Trip trip;
    private int dayOrder;
    private long dateInMilli;

    public Timeline() {
    }

    public String getAlbumContent() {
        return albumContent;
    }

    public void setAlbumContent(String albumContent) {
        this.albumContent = albumContent;
    }

    public String getDaySummaryDummyContent() {
        return daySummaryDummyContent;
    }

    public void setDaySummaryDummyContent(String daySummaryDummyContent) {
        this.daySummaryDummyContent = daySummaryDummyContent;
    }

    public String getIntercityTravelLocationPin() {
        return intercityTravelLocationPin;
    }

    public void setIntercityTravelLocationPin(String intercityTravelLocationPin) {
        this.intercityTravelLocationPin = intercityTravelLocationPin;
    }

    public String getDayLocationPin() {
        return dayLocationPin;
    }

    public void setDayLocationPin(String dayLocationPin) {
        this.dayLocationPin = dayLocationPin;
    }

    public String getCheckInContent() {
        return checkInContent;
    }

    public void setCheckInContent(String checkInContent) {
        this.checkInContent = checkInContent;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentTimeStamp() {
        return contentTimeStamp;
    }

    public void setContentTimeStamp(long contentTimeStamp) {
        this.contentTimeStamp = contentTimeStamp;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public int getDayOrder() {
        return dayOrder;
    }

    public void setDayOrder(int dayOrder) {
        this.dayOrder = dayOrder;
    }

    public long getDateInMilli() {
        return dateInMilli;
    }

    public void setDateInMilli(long dateInMilli) {
        this.dateInMilli = dateInMilli;
    }
}
