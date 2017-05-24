package com.weareholidays.bia.parse.utils;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.weareholidays.bia.parse.models.CustomLocation;

import java.util.List;

/**
 * Created by challa on 13/7/15.
 */
public class OfflineUtils {

    public static final String OFFLINE_PLACES = "GET_OFFLINE_PLACES";
    public static final String LAST_SAVED_LOCATION = "LAST_SAVED_LOCATION";

    public void saveCustomLocation(CustomLocation customLocation) throws ParseException {
        customLocation.pin(OFFLINE_PLACES);
    }

    public void saveCustomLocationList(List<CustomLocation> customLocationList) throws ParseException {
        ParseObject.pinAll(OFFLINE_PLACES,customLocationList);
    }

    public List<CustomLocation> getofflineLocations() throws ParseException {
        return ParseQuery.getQuery(CustomLocation.class).fromPin(OFFLINE_PLACES).find();
    }

    public void deleteCustomLocation() throws ParseException {
        CustomLocation.unpinAll(OFFLINE_PLACES);
    }

    public void setSavedLocation(ParseGeoPoint geoPoint) throws ParseException {
        CustomLocation myLocation = new CustomLocation();
        myLocation.setGeopPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
        myLocation.pin(LAST_SAVED_LOCATION);
    }

    public ParseGeoPoint getSavedLocation() throws ParseException {
        List<CustomLocation> myLocation = ParseQuery.getQuery(CustomLocation.class).fromPin(LAST_SAVED_LOCATION).find();
        if(myLocation.size() > 0)
            return myLocation.get(0).getGeoPoint();
        else
            return null;
    }
}
