package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by hament on 29/7/15.
 */

@ParseClassName("Coupon")
public class Coupon extends ParseObject {

    public static final String OBJECTID = "objectId";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String VALID_FROM = "validFrom";
    public static final String VALID_TILL = "validTill";
    public static final String ISACTIVE = "isActive";
    public static final String PUBLISHEDMESSAGE = "publishedMessage";

    public String getCode() {
        return getString(CODE);
    }

    public Date getValidTill() {
        return getDate(VALID_TILL);
    }

    public Date getValidFrom() {
        return getDate(VALID_FROM);
    }

    public String getMessage() {
        return getString(MESSAGE);
    }

    public boolean getStatus() {
        return getBoolean(ISACTIVE);
    }

    public String getPushlishedMessage() {
        return getString(PUBLISHEDMESSAGE);
    }
}
