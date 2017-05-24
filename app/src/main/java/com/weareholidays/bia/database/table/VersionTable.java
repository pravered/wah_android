package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class VersionTable implements BaseColumns {
    public VersionTable() {
    }

    private static String tableName = "version";
    private static String columnMinTripInVersion = "min_trip_in_version";
    private static String columnMinTripOutVersion = "min_trip_out_version";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID  + " integer primary key autoincrement," +
            columnMinTripInVersion + " text," +
            columnMinTripOutVersion + " text" +
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
