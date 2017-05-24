package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class TripSettingsTable implements BaseColumns {

    public TripSettingsTable() {
    }

    private static String tableName = "tripSettings";
    private static String columnFacebook = "facebook";
    private static String columnTwitter = "twitter";
    private static String columnInstagram = "instagram";
    private static String columnLocation = "location";
    private static String columnSync = "sync";
    private static String columnCheckIn = "checkIn";
    private static String columnCameraRoll = "camera";
    private static String columnCameraRollSync = "cameraRollSyncTime";
    private static String columnFacebookSync = "facebookSyncTime";
    private static String columnTwitterSync = "twitterSinceId";
    private static String columnInstagramSync = "instagramSyncTime";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnFacebook + " integer," +
            columnTwitter + " integer," +
            columnInstagram + " integer," +
            columnLocation + " integer," +
            columnSync + " integer," +
            columnCheckIn + " integer," +
            columnCameraRoll + " integer," +
            columnCameraRollSync + " text," +
            columnFacebookSync + " text," +
            columnTwitterSync + " text," +
            columnInstagramSync + " text" +
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
