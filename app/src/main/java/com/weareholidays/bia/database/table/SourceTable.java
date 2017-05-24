package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class SourceTable implements BaseColumns{

    public SourceTable() {
    }

    private static String tableName = "source";
    private static String columnFb = "fb";
    private static String columnTwitter = "twitter";
    private static String columnInstagram = "instagram";
    private static String columnWah = "wah";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnFb + " text," +
            columnTwitter + " text," +
            columnInstagram + " text," +
            columnWah + " text" +
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
