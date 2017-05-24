package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class CheckInTable implements BaseColumns{

    public CheckInTable() {
    }

    private static String tableName = "checkIn";
    private static String columnLocationLat = "locationLat";
    private static String columnLocationLong = "locationLong";
    private static String columnName = "name";
    private static String columnLocationText = "locationText";
    private static String columnPlaceId = "placeId";
    private static String columnPhotoReference = "photoReference";
    private static String columnMapImageLocalUri = "mapImageLocalUri";
    private static String columnMapImageRemoteUri = "mapImageRemoteUri";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnLocationLat + " real," +
            columnLocationLong + " real," +
            columnName + " text," +
            columnLocationText + " text," +
            columnPlaceId + " text," +
            columnPhotoReference + " text," +
            columnMapImageLocalUri + " text," +
            columnMapImageRemoteUri + " text" +
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
