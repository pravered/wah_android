package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by challa on 10/7/15.
 */
@ParseClassName("InterCityTravelLocationPin")
public class InterCityTravelLocationPin extends ParseObject{

    private static final String CITY_NAME = "cityName";
    private static final String COUNTRY_NAME = "countryName";
    private static final String PIN_TYPE = "pinType";
    private static final String TRAVEL_TIME = "travelTime";
    private static final String TRAVEL_DISTANCE = "travelDistance";
    private static final String TRAVEL_MODE = "travelMode";

    public String getCityName() {
        return getString(CITY_NAME);
    }

    public void setCityName(String cityName) {
        put(CITY_NAME,cityName);
    }

    public String getCountryName() {
        return getString(COUNTRY_NAME);
    }

    public void setCountryName(String location) {
        put(COUNTRY_NAME,location);
    }

    public void setPinType(String pinType){
        put(PIN_TYPE,pinType);
    }

    public int getTravelTime(){
        return getInt(TRAVEL_TIME);
    }

    public double getTravelDistance(){
        return getDouble(TRAVEL_DISTANCE);
    }

    public String getTravelMode(){
        return getString(TRAVEL_MODE);
    }

    public void setTravelTime(int travelTime){
        put(TRAVEL_TIME, travelTime);
    }

    public void setTravelDistance(double distance){
        put(TRAVEL_DISTANCE, distance);
    }

    public void setTravelMode(String travelMode){
        put(TRAVEL_MODE, travelMode);
    }
}
