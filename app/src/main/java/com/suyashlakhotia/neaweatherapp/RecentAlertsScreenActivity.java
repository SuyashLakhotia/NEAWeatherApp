package com.suyashlakhotia.neaweatherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class RecentAlertsScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recent_alerts_screen);

        ListView listView = (ListView) findViewById(R.id.recent_list);

        RecentAlertsDB alertsDB = new RecentAlertsDB(this);
        ArrayList<HashMap<String, String>> alertsList = alertsDB.getAlertsList(20);

        ListAdapter adapter = new SimpleAdapter(this, alertsList, R.layout.list_view_element, new String[]{"id", "title"}, new int[]{R.id.alert_Id, R.id.alert_title});
        listView.setAdapter(adapter);
    }

    public void closeRecent(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
