package com.suyashlakhotia.neaweatherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecentAlertsDBAdapter extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 7; // DB Version Number. Needs to be increased when DB is edited.

    private static final String DATABASE_NAME = "recent.db"; // Database Name

    public RecentAlertsDBAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creates Table:
        String CREATE_TABLE_ALERT = "CREATE TABLE " + RecentAlerts.TABLE + "("
                + RecentAlerts.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + RecentAlerts.KEY_title + " TEXT ," + RecentAlerts.KEY_timestamp + " TEXT ,"
                + RecentAlerts.KEY_description + " TEXT )";

        db.execSQL(CREATE_TABLE_ALERT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drops Older Table (data is permanently deleted):
        db.execSQL("DROP TABLE IF EXISTS " + RecentAlerts.TABLE);

        // Create Tables Again:
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drops Older Table (data is permanently deleted):
        db.execSQL("DROP TABLE IF EXISTS " + RecentAlerts.TABLE);

        // Create Tables Again:
        onCreate(db);
    }
}
