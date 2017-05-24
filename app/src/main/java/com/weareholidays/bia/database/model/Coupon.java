package com.weareholidays.bia.database.model;

import static com.weareholidays.bia.parse.models.Coupon.OBJECTID;

/**
 * Created by shankar on 12/5/17.
 */

public class Coupon {
    private String objectId;
    private String code;
    private String message;
    private long validFrom;
    private long validTill;
    private boolean isActive;
    private String publishedMessage;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(long validFrom) {
        this.validFrom = validFrom;
    }

    public long getValidTill() {
        return validTill;
    }

    public void setValidTill(long validTill) {
        this.validTill = validTill;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPublishedMessage() {
        return publishedMessage;
    }

    public void setPublishedMessage(String publishedMessage) {
        this.publishedMessage = publishedMessage;
    }
}
