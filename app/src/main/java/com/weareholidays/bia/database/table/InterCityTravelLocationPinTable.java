package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class InterCityTravelLocationPinTable implements BaseColumns{

    public InterCityTravelLocationPinTable() {
    }

    private static String tableName = "interCityTravelLocationPin";
    private static String columnCityName = "cityName";
    private static String columnCountryName = "countryName";
    private static String columnPinType = "pinType";
    private static String columnTravelTime = "travelTime";
    private static String columnTravelDistance = "travelDistance";
    private static String columnTravelMode = "travelMode";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnCityName + " text," +
            columnCountryName + " text," +
            columnPinType + " text," +
            columnTravelTime + " integer," +
            columnTravelDistance + " real," +
            columnTravelTime + " text" +
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
