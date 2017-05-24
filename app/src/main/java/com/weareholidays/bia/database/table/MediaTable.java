package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class MediaTable implements BaseColumns {

    public MediaTable() {
    }

    private static String tableName = "media";
    private static String columnContenLocalUrl = "contentLocalUrl";
    private static String columnContentRemoteUrl = "contentRemoteUrl";
    private static String columnCaption = "caption";
    private static String columnMediaTags = "mediaTags";
    private static String columnLocationLat = "locationLat";
    private static String columnLocationLong = "locationLong";
    private static String columnPrivacy = "private";
    private static String columnAlbum = "albumId";
    private static String columnAddress = "address";
    private static String columnContentCreationTime = "contentCreationTime";
    private static String columnContentSize = "contentSize";
    private static String columnMediaWidth = "mediaWidth";
    private static String columnMediaHeight = "mediaHeight";
    private static String columnThirdPartyId = "thirdPartyId";
    private static String columnThirdPartyUrl = "thirdPartyUrl";
    private static String columnMediaSource = "mediaSource";
    private static String columnFetchingAddress = "fetchingAddress";

    private static String createStatement = "create table " +
            tableName + " (" +
            _ID + " integer primary key autoincrement," +
            columnContenLocalUrl + " text," +
            columnContentRemoteUrl + " text," +
            columnCaption + " text," +
            columnMediaTags + " text," +
            columnLocationLat + " real," +
            columnLocationLong + " real," +
            columnPrivacy + " integer," +
            columnAlbum + " text," +
            columnAddress + " text," +
            columnContentCreationTime + " text," +
            columnContentSize + " text," +
            columnMediaWidth + " integer," +
            columnMediaHeight + " integer," +
            columnThirdPartyId + " text," +
            columnThirdPartyUrl + " text," +
            columnMediaSource + " text," +
            columnFetchingAddress + " integer" +
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
