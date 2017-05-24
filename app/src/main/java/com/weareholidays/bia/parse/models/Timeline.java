package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.weareholidays.bia.parse.models.local.DayLocationPin;
import com.weareholidays.bia.parse.models.local.DaySummaryDummy;

import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 03/06/15.
 */
@ParseClassName("TimeLine")
public class Timeline extends ParseObject {

    public static String ALBUM_CONTENT = Album.class.getSimpleName();
    public static String DAY_SUMMARY_DUMMY_CONTENT = DaySummaryDummy.class.getSimpleName();
    public static String INTERCITY_TRAVEL_LOCATION_PIN = InterCityTravelLocationPin.class.getSimpleName();
    public static String DAY_LOCATION_PIN = DayLocationPin.class.getSimpleName();
    public static String CHECK_IN_CONTENT = CheckIn.class.getSimpleName();
    public static String NOTE_CONTENT = Note.class.getSimpleName();
    public static String CONTENT_TYPE = "contentType";
    public static String CONTENT_TIME_STAMP = "contentTimeStamp";
    public static String DISPLAY_ORDER = "displayOrder";
    public static String SOURCE = "source";
    public static String DAY = "day";
    public static String CONTENT = "content";
    public static String THIRD_PARTY_ID = "thirdPartyId";
    public static String TRIP = "trip";
    public static String DAY_ORDER = "dayNo";
    private long dateInMilli;

    public String getContentType(){
        return getString(CONTENT_TYPE);
    }

    private void setContentType(ParseObject parseObject){
        put(CONTENT_TYPE,parseObject.getClassName());
    }

    public void setDay(Day day){
        put(DAY,day);
    }

    public String getSource(){
        return getString(SOURCE);
    }

    public void setSource(String source){
        put(SOURCE,source);
    }

    public void setContent(ParseObject parseObject){
        setContentType(parseObject);
        addUnique(CONTENT, parseObject);
    }

    public ParseObject getContent(){
        List<ParseObject> list = getList(CONTENT);
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return  null;
    }

    public Date getContentTime(){
        return getDate(CONTENT_TIME_STAMP);
    }

    public void setContentTime(Date date){
        put(CONTENT_TIME_STAMP,date);
    }

    public int getDisplayOrder(){
        return getInt(DISPLAY_ORDER);
    }

    public void setDisplayOrder(int order){
        put(DISPLAY_ORDER,order);
    }

    public long getDateInMilli() {
        return dateInMilli;
    }

    public void setDateInMilli(long dateInMilli) {
        this.dateInMilli = dateInMilli;
    }

    public String getThirdPartyId(){
        return getString(THIRD_PARTY_ID);
    }

    public void setThirdPartyId(String id){
        put(THIRD_PARTY_ID,id);
    }

    public void setTrip(Trip trip){
        put(TRIP,trip);
    }

    public void setDayOrder(int dayOrder){
        put(DAY_ORDER,dayOrder);
    }

    public int getDayOrder(){
        return getInt(DAY_ORDER);
    }
}
