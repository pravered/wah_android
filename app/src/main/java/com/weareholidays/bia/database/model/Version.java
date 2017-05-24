package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class Version {
    private String minTripInVersion;
    private String minTripOutVersion;

    public Version() {
    }

    public String getMinTripInVersion() {
        return minTripInVersion;
    }

    public void setMinTripInVersion(String minTripInVersion) {
        this.minTripInVersion = minTripInVersion;
    }

    public String getMinTripOutVersion() {
        return minTripOutVersion;
    }

    public void setMinTripOutVersion(String minTripOutVersion) {
        this.minTripOutVersion = minTripOutVersion;
    }
}
