package com.weareholidays.bia.database.table;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.weareholidays.bia.database.model.Album;
import com.weareholidays.bia.database.model.CheckIn;
import com.weareholidays.bia.database.model.InterCityTravelLocationPin;
import com.weareholidays.bia.database.model.Note;
import com.weareholidays.bia.parse.models.local.DayLocationPin;
import com.weareholidays.bia.parse.models.local.DaySummaryDummy;

/**
 * Created by shankar on 12/5/17.
 */

public class TimeLineTable implements BaseColumns {
    public TimeLineTable() {
    }

    private static String tableName = "timeline";
    private static String columnAlbumContent = Album.class.getSimpleName();
    private static String columnDaySummaryDummyContent = DaySummaryDummy.class.getSimpleName();
    private static String columnIntercityTravelLocationPin = InterCityTravelLocationPin.class.getSimpleName();
    private static String columnDayLocationPin = DayLocationPin.class.getSimpleName();
    private static String columnCheckInContent = CheckIn.class.getSimpleName();
    private static String columnNoteContent = Note.class.getSimpleName();
    private static String columnContentType = "contentType";
    private static String columnContentTimeStamp = "contentTimeStamp";
    private static String columnDisplayOrder = "displayOrder";
    private static String columnSource = "source";
    private static String columnDay = "dayId";
    private static String columnContent = "contentId";
    private static String columnThirdPartyId = "thirdPartyId";
    private static String columnTrip = "tripId";
    private static String columnDayOrder = "dayNo";
    private static long dateInMilli;

    private static String createStatement = "create table " +
            tableName + "(" +
            _ID + " integer primary key autoincrement," +
            columnAlbumContent + " text," +
            columnDaySummaryDummyContent + " text," +
            columnIntercityTravelLocationPin + " text," +
            columnDayLocationPin + " text," +
            columnCheckInContent + " text," +
            columnNoteContent + " text," +
            columnContentType + " text," +
            columnContentTimeStamp + " text," +
            columnDisplayOrder + " integer," +
            columnSource + " text," +
            columnDay + " text," +
            columnContent + " text," +
            columnThirdPartyId + " text," +
            columnTrip + " text," +
            columnDayOrder + " integer" +
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
