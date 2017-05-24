package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class TripTable implements BaseColumns{
    public TripTable() {
    }

    private static String tableName = "trip";
    private static String columnName = "name";
    private static String columnStartLocationLat = "startLocLat";
    private static String columnStartLocationLong = "startLocLong";
    private static String columnEndLocationLat = "endLocLat";
    private static String columnEndLocationLong = "endLocLong";
    private static String columnStartTime = "startTime";
    private static String columnEndTime = "endTime";
    private static String columnUploaded = "uploaded";
    private static String columnFinished = "finished";
    private static String columnPublished = "published";
    private static String columnPublishTime = "publishTime";
    private static String columnDeleted = "deleted";
    private static String columnSettings = "tripSettingsId";
    private static String columnDay = "days";
    private static String columnFeatureImageLocalUrl = "featureImageLocalUrl";
    private static String columnFeatureImageRemoteUrl = "featureImageRemoteUrl";
    private static String columnTripOwner = "userOwnerId";
    private static String columnTripDaysNumber = "noDays";
    private static String columnCreatedAt = "createdAt";
    private static String columnViewCount = "views";
    private static String columnSummary = "tripSummaryId";
    private static String columnSecretKey = "secretKey";
    private static String columnFeatured = "featured";
    private static String columnHidden = "hidden";
    private static String columnCoupon = "couponId";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnName + " text," +
            columnStartLocationLat + " real," +
            columnStartLocationLong + " real," +
            columnEndLocationLat + " real," +
            columnEndLocationLong + " real," +
            columnStartTime + " text," +
            columnEndTime + " text," +
            columnUploaded + " integer," +
            columnFinished + " integer," +
            columnPublished + " integer," +
            columnPublishTime + " text," +
            columnDeleted + " integer," +
            columnSettings + " text," +
            columnDay + " text," +
            columnFeatureImageLocalUrl + " text," +
            columnFeatureImageRemoteUrl + " text," +
            columnTripOwner + " text," +
            columnTripDaysNumber + " integer," +
            columnCreatedAt + " text." +
            columnViewCount + " integer," +
            columnSummary + " text," +
            columnSecretKey + " text," +
            columnFeatured + " integer," +
            columnHidden + " integer," +
            columnCoupon + " text" +
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
