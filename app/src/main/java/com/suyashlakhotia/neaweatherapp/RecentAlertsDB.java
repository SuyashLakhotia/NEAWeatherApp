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

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RecentAlerts.KEY_description, recentAlerts.description);

        // Inserting Row
        long recentAlerts_Id = db.insert(RecentAlerts.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) recentAlerts_Id;
    }

    public void delete(int recentalerts_Id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(RecentAlerts.TABLE, RecentAlerts.KEY_ID + "= ?", new String[] { String.valueOf(recentalerts_Id) });
        db.close();
    }

    public void update(RecentAlerts recentAlerts) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(RecentAlerts.KEY_description, recentAlerts.description);

        db.update(RecentAlerts.TABLE, values, RecentAlerts.KEY_ID + "= ?", new String[]{String.valueOf(recentAlerts.alert_ID)});
        db.close();
    }


    public ArrayList<HashMap<String, String>> getAlertsList(int n) {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT " +
                RecentAlerts.KEY_ID + ", " +
                RecentAlerts.KEY_description +
                " FROM " + RecentAlerts.TABLE;

        ArrayList<HashMap<String, String>> alertsList = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> alert = new HashMap<String, String>();
                alert.put("id", cursor.getString(cursor.getColumnIndex(RecentAlerts.KEY_ID)));
                alert.put("description", cursor.getString(cursor.getColumnIndex(RecentAlerts.KEY_description)));
                alertsList.add(alert);
                n--;
            } while (n>0 && cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return alertsList;

    }

    public RecentAlerts getAlertById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                RecentAlerts.KEY_ID + "," +
                RecentAlerts.KEY_description +
                " FROM " + RecentAlerts.TABLE
                + " WHERE " +
                RecentAlerts.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        RecentAlerts recentAlerts = new RecentAlerts();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                recentAlerts.alert_ID =cursor.getInt(cursor.getColumnIndex(RecentAlerts.KEY_ID));
                recentAlerts.description =cursor.getString(cursor.getColumnIndex(RecentAlerts.KEY_description));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return recentAlerts;
    }
}
