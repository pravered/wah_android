package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class NotificationTable implements BaseColumns{
    public NotificationTable() {
    }

    private static String tableName = "notification";
    private static String columnContentTime = "contentTime";
    private static String columnIsRead = "isRead";
    private static String columnContent = "content";
    private static String columnUsername = "username";
    private static String columnActionType = "actionType";
    private static String columnActionParams = "actionParams";
    private static String columnNotifier = "notifiedUserId";
    private static String columnIsDeleted = "isDeleted";

    private static String columnTripOpenAction = "tripOpenAction";
    private static String columnAlbumImageOpenAction = "albumImageOpenAction";
    private static String columnArticleOpenAction = "articleOpenAction";


    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnContentTime + " text," +
            columnIsRead + " integer," +
            columnIsDeleted + " integer," +
            columnContent + " text," +
            columnUsername + " text," +
            columnActionType + " text," +
            columnActionParams + " text," +
            columnNotifier + " text," +
            columnTripOpenAction + " text," +
            columnAlbumImageOpenAction + " text," +
            columnArticleOpenAction + " text"+
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
