package com.weareholidays.bia.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.weareholidays.bia.database.table.AlbumTable;
import com.weareholidays.bia.database.table.CheckInTable;
import com.weareholidays.bia.database.table.CouponTable;
import com.weareholidays.bia.database.table.CustomLocationTable;
import com.weareholidays.bia.database.table.DaySummaryTable;
import com.weareholidays.bia.database.table.DayTable;
import com.weareholidays.bia.database.table.FileLocalTable;
import com.weareholidays.bia.database.table.InterCityTravelLocationPinTable;
import com.weareholidays.bia.database.table.MediaTable;
import com.weareholidays.bia.database.table.NoteTable;
import com.weareholidays.bia.database.table.NotificationTable;
import com.weareholidays.bia.database.table.RoutePointTable;
import com.weareholidays.bia.database.table.SourceTable;
import com.weareholidays.bia.database.table.TimeLineTable;
import com.weareholidays.bia.database.table.TripPeopleTable;
import com.weareholidays.bia.database.table.TripSettingsTable;
import com.weareholidays.bia.database.table.TripSummaryTable;
import com.weareholidays.bia.database.table.TripTable;
import com.weareholidays.bia.database.table.UserCouponTable;
import com.weareholidays.bia.database.table.UserTable;
import com.weareholidays.bia.database.table.VersionTable;

/**
 * Created by shankar on 15/5/17.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wah";
    private static DbHelper databaseHelper;
    private static Context context;
    private static int databaseVersion = 1;


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, databaseVersion);
        this.context = context;
    }

    public static DbHelper getInstance(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DbHelper(context);
        }
        return databaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        AlbumTable.onCreate(db);
        CheckInTable.onCreate(db);
        CouponTable.onCreate(db);
        CustomLocationTable.onCreate(db);
        DaySummaryTable.onCreate(db);
        DayTable.onCreate(db);
        FileLocalTable.onCreate(db);
        InterCityTravelLocationPinTable.onCreate(db);
        MediaTable.onCreate(db);
        NoteTable.onCreate(db);
        NotificationTable.onCreate(db);
        RoutePointTable.onCreate(db);
        SourceTable.onCreate(db);
        TimeLineTable.onCreate(db);
        TripPeopleTable.onCreate(db);
        TripSettingsTable.onCreate(db);
        TripSummaryTable.onCreate(db);
        TripTable.onCreate(db);
        UserCouponTable.onCreate(db);
        UserTable.onCreate(db);
        VersionTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        AlbumTable.onUpgrade(db , oldVersion, newVersion);
        CheckInTable.onUpgrade(db, oldVersion, newVersion);
        CouponTable.onUpgrade(db, oldVersion, newVersion);
        CustomLocationTable.onUpgrade(db, oldVersion, newVersion);
        DaySummaryTable.onUpgrade(db, oldVersion, newVersion);
        DayTable.onUpgrade(db, oldVersion, newVersion);
        FileLocalTable.onUpgrade(db, oldVersion, newVersion);
        InterCityTravelLocationPinTable.onUpgrade(db, oldVersion, newVersion);
        MediaTable.onUpgrade(db, oldVersion, newVersion);
        NoteTable.onUpgrade(db, oldVersion, newVersion);
        NotificationTable.onUpgrade(db, oldVersion, newVersion);
        RoutePointTable.onUpgrade(db, oldVersion, newVersion);
        SourceTable.onUpgrade(db, oldVersion, newVersion);
        TimeLineTable.onUpgrade(db, oldVersion, newVersion);
        TripPeopleTable.onUpgrade(db, oldVersion, newVersion);
        TripSettingsTable.onUpgrade(db, oldVersion, newVersion);
        TripSummaryTable.onUpgrade(db, oldVersion, newVersion);
        TripTable.onUpgrade(db, oldVersion, newVersion);
        UserCouponTable.onUpgrade(db, oldVersion, newVersion);
        UserTable.onUpgrade(db, oldVersion, newVersion);
        VersionTable.onUpgrade(db, oldVersion, newVersion);
    }
}
