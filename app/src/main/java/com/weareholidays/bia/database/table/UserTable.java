package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class UserTable implements BaseColumns{

    public UserTable() {
    }

    private static String tableName = "user";
    //Fields from ParseUser class
    private static String columnKeySessionToken = "sessionTokenKey";
    private static String columnKeyAuthData = "authDataKey";
    private static String columnKeyUsername = "usernameKey";
    private static String columnKeyPassword = "passwordKey";
    private static String columnKeyEmail = "emailKey";

    //Fields from ParseCustomUser class
    public static String columnName = "name";
    public static String columnUsername = "username";
    public static String columnPhone = "phone";
    public static String columnPlace = "place";
    public static String columnGender = "gender";
    public static String columnProfileImageLocalUrl = "profileImageLocalUrl";
    public static String columnProfileImageRemoteUrl = "profileImageRemoteUrl";
    public static String columnFeatureImageLocalUrl = "featureImageLocalUrl";
    public static String columnFeatureImageRemoteUrl = "featureImageRemoteUrl";
    public static String columnTotalTrips = "totalTrips";
    public static String columnTotalPublishedTrips = "totalPublishedTrips";
    public static String columnSharer = "sharer";

    private static String createStatement = " create table" +
            tableName + "(" +
            _ID + " integer primary key autonincremet," +
            columnKeySessionToken + " text," +
            columnKeyAuthData + " text," +
            columnKeyUsername + " text," +
            columnKeyPassword + " text," +
            columnKeyEmail + " text," +
            columnName + " text," +
            columnUsername +  " text," +
            columnPhone + " text," +
            columnPlace + " text," +
            columnGender + " text," +
            columnProfileImageLocalUrl + " text," +
            columnProfileImageRemoteUrl + " text," +
            columnFeatureImageLocalUrl + " text," +
            columnFeatureImageRemoteUrl + " text," +
            columnTotalTrips + " integer," +
            columnTotalPublishedTrips + " integer," +
            columnSharer + " text" +
            ");";
    private static String dropStatement = "drop table if exists " + tableName;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(createStatement);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL(dropStatement);
        onCreate(database);
    }
}
