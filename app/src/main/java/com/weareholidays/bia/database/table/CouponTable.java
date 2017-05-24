package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class CouponTable implements BaseColumns {


    public CouponTable() {
    }

    private static String tableName = "coupon";
    private static String columnObjectId = "objectId";
    private static String columnCode = "code";
    private static String columnMessage = "message";
    private static String columnValidFrom = "validFrom";
    private static String columnValidTill = "validTill";
    private static String columnIsActive = "isActive";
    private static String columnPublishedmessage = "publishedMessage";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnObjectId + " text," +
            columnCode + " text," +
            columnMessage + " text," +
            columnValidFrom + " text," +
            columnValidTill + " text," +
            columnIsActive + " int," +
            columnPublishedmessage + " text" +
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
