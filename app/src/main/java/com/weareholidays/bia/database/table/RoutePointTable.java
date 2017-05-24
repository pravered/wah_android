package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class RoutePointTable implements BaseColumns{
    public RoutePointTable() {
    }

    private static String tableName = "routePoint";
    private static int COLUMN_HIGH_ACCURACY = 1; //not storing this one
    private static String columnRecordedTime = "recordedTime";
    private static String columnPriority = "priority";
    private static String columnLocationLat = "locationLat";
    private static String columnLocationLong = "locationLong";
    private static String columnSource = "source";
    private static String columnDay = "dayId";
    private static String columnTrip = "tripId";
    private static String columnDayOrder = "dayOrder";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnRecordedTime + " text," +
            columnPriority + " integer," +
            columnLocationLat + " real," +
            columnLocationLong + " real," +
            columnSource + " text," +
            columnDay + " text," +
            columnTrip + " text," +
            columnDayOrder + " integer" +
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
