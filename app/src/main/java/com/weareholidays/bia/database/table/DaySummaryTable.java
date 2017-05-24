package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class DaySummaryTable implements BaseColumns {

    public DaySummaryTable() {
    }

    private static String TABLE_DAY_SUMMARY = "daySummary";
    private static String COLUMN_TWITTER = "twitter";
    private static String COLUMN_FB = "fb";
    private static String COLUMN_INSTAGRAM = "instagram";
    private static String COLUMN_PHOTOS = "photos";
    private static String COLUMN_PUBLIC_PHOTOS = "publicPhotos";
    private static String COLUMN_NOTES =  "notes";
    private static String COLUMN_VIDEOS = "videos";
    private static String COLUMN_CHECK_INS = "checkIns";
    private static String COLUMN_DISTANCE = "distance";

    private static String createStatement = "create table " +
            TABLE_DAY_SUMMARY + "(" +
            _ID + " integer primary key autoincrement," +
            COLUMN_TWITTER + " text," +
            COLUMN_FB + " text," +
            COLUMN_INSTAGRAM + " text," +
            COLUMN_PHOTOS + " text," +
            COLUMN_PUBLIC_PHOTOS + " text," +
            COLUMN_NOTES + " text," +
            COLUMN_VIDEOS + " text," +
            COLUMN_CHECK_INS + " text," +
            COLUMN_DISTANCE + " text" +
            ");";
    private static String dropStatement = "drop table if exists " + TABLE_DAY_SUMMARY;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(createStatement);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL(dropStatement);
        onCreate(database);
    }


}
