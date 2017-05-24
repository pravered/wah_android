package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by Teja on 29/06/15.
 */
@ParseClassName("TripPeople")
public class TripPeople extends ParseObject {

    public static final String PHONE_BOOK_TYPE = "PHONE_BOOK";
    public static final String FACEBOOK_TYPE = "FACEBOOK";

    public static String NAME = "name";
    public static String EMAIL = "email";
    public static String TRIP = "trip";
    public static String IMAGE = "image";
    public static String IMAGE_URL = "imageUrl";
    public static String TYPE = "type";
    public static String IN_TRIP = "inTrip";
    public static String IDENTIFIER = "identifier";

    public String getName(){
        return getString(NAME);
    }

    public void setName(String name){
        put(NAME,name);
    }

    public String getEmail(){
        return getString(EMAIL);
    }

    public void setEmail(String email){
        put(EMAIL,email);
    }

    public void setTrip(Trip trip){
        put(TRIP,trip);
    }

    public String getImageUrl(){
        return getString(IMAGE_URL);
    }

    public void setImageUrl(String imageUrl){
        put(IMAGE_URL,imageUrl);
    }

    public String getType(){
        return getString(TYPE);
    }

    public void setType(String type){
        put(TYPE,type);
    }

    public String getIdentifier(){
        return getString(IDENTIFIER);
    }

    public void setIdentifier(String identifier){
        put(IDENTIFIER,identifier);
    }

    public boolean isInTrip(){
        return getBoolean(IN_TRIP);
    }

    public void setInTrip(boolean inTrip){
        put(IN_TRIP,inTrip);
    }

    public ParseFile getImage(){
        return getParseFile(IMAGE);
    }

    public void setImage(ParseFile parseFile){
        put(IMAGE,parseFile);
    }
}
