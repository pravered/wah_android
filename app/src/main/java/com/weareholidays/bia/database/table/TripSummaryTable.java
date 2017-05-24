package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class TripSummaryTable implements BaseColumns {

    public TripSummaryTable() {
    }

    private static String tableName = "tripSummary";
    private static String columnTwitter = "twitter";
    private static String columnFb = "fb";
    private static String columnInstagram = "instagram";
    private static String columnPhotos = "photos";
    private static String columnPublicPhotos = "publicPhotos";
    private static String columnNotes =  "notes";
    private static String columnVideos = "videos";
    private static String columnCheckIns = "checkIns";
    private static String columnDistance = "distance";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnTwitter + " integer," +
            columnFb + " integer," +
            columnInstagram  +" integer," +
            columnPhotos + " integer," +
            columnPublicPhotos + " integer," +
            columnNotes + " integer," +
            columnVideos + " integer," +
            columnCheckIns + " integer," +
            columnDistance + " real" +
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
