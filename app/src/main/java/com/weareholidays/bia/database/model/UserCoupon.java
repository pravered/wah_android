package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class UserCoupon {
    public Coupon coupon;
    public User user;

    public UserCoupon() {
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
