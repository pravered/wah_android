package com.weareholidays.bia.database.model;

import java.util.Map;

/**
 * Created by shankar on 12/5/17.
 */

public class User {

    private String sessionToken;
    // saving auth information for this user
    private Map<String, Map<String, String>> authData;
    private String username;
    private String password;
    private String email;

    //Fields from ParseCustomUser class
    public String name;
    public String phone;
    public String place;
    public String gender;
    public String profileImageLocalUrl;
    public String profileImageRemoteUrl;
    public String featureImageLocalUrl;
    public String featureImageRemoteUrl;
    public int totalTrips;
    public int totalPublishedTrips;
    public String sharer;

    public User() {
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Map<String, Map<String, String>> getAuthData() {
        return authData;
    }

    public void setAuthData(Map<String, Map<String, String>> authData) {
        this.authData = authData;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfileImageLocalUrl() {
        return profileImageLocalUrl;
    }

    public void setProfileImageLocalUrl(String profileImageLocalUrl) {
        this.profileImageLocalUrl = profileImageLocalUrl;
    }

    public String getProfileImageRemoteUrl() {
        return profileImageRemoteUrl;
    }

    public void setProfileImageRemoteUrl(String profileImageRemoteUrl) {
        this.profileImageRemoteUrl = profileImageRemoteUrl;
    }

    public String getFeatureImageLocalUrl() {
        return featureImageLocalUrl;
    }

    public void setFeatureImageLocalUrl(String featureImageLocalUrl) {
        this.featureImageLocalUrl = featureImageLocalUrl;
    }

    public String getFeatureImageRemoteUrl() {
        return featureImageRemoteUrl;
    }

    public void setFeatureImageRemoteUrl(String featureImageRemoteUrl) {
        this.featureImageRemoteUrl = featureImageRemoteUrl;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    public int getTotalPublishedTrips() {
        return totalPublishedTrips;
    }

    public void setTotalPublishedTrips(int totalPublishedTrips) {
        this.totalPublishedTrips = totalPublishedTrips;
    }

    public String getSharer() {
        return sharer;
    }

    public void setSharer(String sharer) {
        this.sharer = sharer;
    }
}
