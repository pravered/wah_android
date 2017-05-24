package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class DayTable implements BaseColumns {
    public DayTable() {
    }

    private static String tableName = "daySummary";
    private static String columnDaySummary = "daySummaryId";
    private static String columnName = "name";
    private static String columnDisplayOrder = "displayOrder";
    private static String columnStartTime = "startTime";
    private static String columnEndTime = "endTime";
    private static String columnLocationLat = "locationLat";
    private static String columnLocationLong = "locationLong";
    private static String columnCity = "startCity";
    private static String columnCountry = "startCountry";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnDaySummary + " text," +
            columnName + " text," +
            columnDisplayOrder + " integer," +
            columnStartTime + " text," +
            columnEndTime + " text," +
            columnLocationLat + " real," +
            columnLocationLong + " real," +
            columnCity + " text," +
            columnCountry + " text" +
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
