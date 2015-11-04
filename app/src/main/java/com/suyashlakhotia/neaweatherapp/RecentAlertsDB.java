package com.suyashlakhotia.neaweatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class RecentAlertsDB {
    private RecentAlertsDBAdapter dbHelper;

    public RecentAlertsDB(Context context) {
        dbHelper = new RecentAlertsDBAdapter(context);
    }

    public int insert(RecentAlerts recentAlerts) {
        // Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RecentAlerts.KEY_description, recentAlerts.description);
        values.put(RecentAlerts.KEY_title, recentAlerts.title);

        // Inserting Row
        long recentAlerts_Id = db.insert(RecentAlerts.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) recentAlerts_Id;
    }

    public ArrayList<HashMap<String, String>> getAlertsList(int n) {
        // Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * " +
                " FROM " + RecentAlerts.TABLE;

        ArrayList<HashMap<String, String>> alertsList = new ArrayList<>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToLast()) {
            do {
                HashMap<String, String> alert = new HashMap<>();
                alert.put("id", cursor.getString(cursor.getColumnIndex(RecentAlerts.KEY_ID)));
                alert.put("description", cursor.getString(cursor.getColumnIndex(RecentAlerts.KEY_description)));
                alert.put("title", cursor.getString(cursor.getColumnIndex(RecentAlerts.KEY_title)));
                alertsList.add(alert);
                n--;
            } while (n > 0 && cursor.moveToPrevious());
        }

        cursor.close();
        db.close();
        return alertsList;
    }
}
