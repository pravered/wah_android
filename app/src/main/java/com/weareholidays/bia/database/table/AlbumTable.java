package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class AlbumTable implements BaseColumns {

    public AlbumTable() {
    }

    private static String tableName = "album";
    private static String columnContent = "content";
    private static String columnMediaCount = "mediaCount";
    private static String columnPublicMediaCount = "publicMediaCount";
    private static String columnLocationLat = "locationLat";
    private static String columnLocationLong = "locationLong";
    private static String columnLocationText = "locationText";
    private static String columnStartTime = "startTime";
    private static String columnEndTime = "endTime";
    private static String columnSource = "source";
    private static String columnCategory = "category";

    private static String createStatement = "create table " +
            tableName +
            "(" +
            _ID + " integer primary key autoincrement," +
            columnContent + " text," +
            columnMediaCount + " integer," +
            columnPublicMediaCount + " integer," +
            columnLocationLat + " real," +
            columnLocationLong + " real," +
            columnLocationText + " text," +
            columnStartTime + " text," +
            columnEndTime + " text," +
            columnSource + " text," +
            columnCategory + " text" +
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
