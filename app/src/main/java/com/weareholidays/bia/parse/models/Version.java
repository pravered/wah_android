package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by kapil on 17/7/15.
 */
@ParseClassName("Version")
public class Version extends ParseObject{

    private static String MIN_TRIP_IN_VERSION = "min_trip_in_version";
    private static String MIN_TRIP_OUT_VERSION = "min_trip_out_version";

    public int getMinTripInVersion(){
        return getInt(MIN_TRIP_IN_VERSION);
    }

    public int getMinTripOutVersion(){
        return getInt(MIN_TRIP_OUT_VERSION);
    }
}
