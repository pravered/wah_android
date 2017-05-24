package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by hament on 29/7/15.
 */

@ParseClassName("UserCoupon")
public class UserCoupon extends ParseObject {

    public static String COUPON = "coupon";
    public static String USER = "user";

    public void setCoupon(Coupon coupon) {
        put(COUPON, coupon);
    }

    public Coupon getCoupon() {
        return (Coupon) getParseObject(COUPON);
    }

    public void setUser(ParseUser user) {
        put(USER, user);
    }

    public ParseCustomUser getUser() {
        return (ParseCustomUser)getParseObject(USER);
    }
}