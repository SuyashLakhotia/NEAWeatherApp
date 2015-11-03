package com.suyashlakhotia.neaweatherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecentAlertsDBAdapter extends SQLiteOpenHelper {

    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION = 6;

    // Database Name
    private static final String DATABASE_NAME = "recent.db";

    public RecentAlertsDBAdapter(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here
        String CREATE_TABLE_ALERT = "CREATE TABLE " + RecentAlerts.TABLE  + "("
                + RecentAlerts.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + RecentAlerts.KEY_description + " TEXT )";

        db.execSQL(CREATE_TABLE_ALERT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + RecentAlerts.TABLE);
        // Create tables again
        onCreate(db);

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + RecentAlerts.TABLE);

        // Create tables again
        onCreate(db);

    }


}
