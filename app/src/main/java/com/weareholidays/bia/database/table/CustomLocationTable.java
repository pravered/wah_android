package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class CustomLocationTable implements BaseColumns{
    public CustomLocationTable() {
    }

    private static String tableName = "customLocation";
    private static String columnName = "name";
    private static String columnDistance = "distance";
    private static String columnCategory = "category";
    private static String columnPhotoreference = "photoReference";
    private static String columnReference = "reference";
    private static String columnGeopointLat = "geoPointLat";
    private static String columnGeoPointLong = "geoPointLong";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnName + " text," +
            columnDistance + " real," +
            columnCategory + " text," +
            columnPhotoreference + " text," +
            columnReference + " text," +
            columnGeopointLat + " text," +
            columnGeoPointLong + "text" +
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
