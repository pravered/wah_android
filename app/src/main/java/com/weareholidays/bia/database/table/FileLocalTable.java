package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by shankar on 12/5/17.
 */

public class FileLocalTable implements BaseColumns {

    public FileLocalTable() {
    }

    private static String tableName = "fileLocal";
    private static String columnProxyClass = "proxyClass";
    private static String columnLocalUri = "localUri";
    private static String columnFileUploaded = "fileUploaded";
    private static String columnFileLinked = "fileLinked";
    private static String columnProxyField = "proxyField";
    private static String columnFileName = "fileName";

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnProxyClass +" text," +
            columnLocalUri + " text," +
            columnFileUploaded +  " integer," +
            columnFileLinked + " integer," +
            columnProxyField + " text," +
            columnFileName + " text" +
            ")";
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
