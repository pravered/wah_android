package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class NoteTable implements BaseColumns {


    public NoteTable() {
    }

    private static String tableName = "note";
    private static String columnContent = "content";
    private static String columnLocationLat = "locationLat";
    private static String columnLocationLong = "locationLong";
    private static String columnLocationText = "locationText";
    private static String columnPlaceId = "placeId";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnContent + " text," +
            columnLocationLat + " real," +
            columnLocationLong + " real," +
            columnLocationText + " text," +
            columnPlaceId + " text" +
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
