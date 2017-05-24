package com.weareholidays.bia.parse.models.local;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by Teja on 12/07/15.
 */
@ParseClassName("DayLocationPin")
public class DayLocationPin extends ParseObject {

    private String cityName;
    private String countryName;
    private boolean currentLocation;
    private boolean ended;
    private ParseGeoPoint parseGeoPoint;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public boolean isCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(boolean currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public ParseGeoPoint getParseGeoPoint() {
        return parseGeoPoint;
    }

    public void setParseGeoPoint(ParseGeoPoint parseGeoPoint) {
        this.parseGeoPoint = parseGeoPoint;
    }
}
