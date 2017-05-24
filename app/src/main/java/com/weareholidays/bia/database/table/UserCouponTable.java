package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class UserCouponTable implements BaseColumns {
    public UserCouponTable() {
    }

    private static String tableUserCoupon = "userCoupon";
    private static String columnCoupon = "couponId";
    private static String columnUser = "userId";

    private static String createStatement = "create table " +
            tableUserCoupon + " (" +
            _ID + " integer primary key autoincrement,'" +
            columnCoupon + " text," +
            columnUser + " text" +
            ");";
    private static String dropStatement = "drop table if exists " + tableUserCoupon;

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(createStatement);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL(dropStatement);
        onCreate(database);
    }
}
