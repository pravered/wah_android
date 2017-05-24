package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class TripPeopleTable implements BaseColumns {
    public TripPeopleTable() {
    }

    private static String tableName = "tripPeople";
    private static String columnPhoneBookType = "PHONE_BOOK";
    private static String columnFacebookType = "FACEBOOK";

    private static String columnName = "name";
    private static String columnEmail = "email";
    private static String columnTrip = "tripId";
    private static String columnImageLocalUrl = "imageLocalUrl";
    private static String columnImageRemoteUrl = "imageRemoteUrl";
    private static String columnType = "type";
    private static String columnInTrip = "inTrip";
    private static String columnIdentifier = "identifier";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " intger primary key autoincrement," +
            columnFacebookType + " text," +
            columnPhoneBookType + " text," +
            columnName + " text," +
            columnEmail + " text." +
            columnTrip + " text," +
            columnImageLocalUrl + " text," +
            columnImageRemoteUrl + " text," +
            columnType + " text," +
            columnInTrip + " integer," +
            columnIdentifier + " text" +
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
