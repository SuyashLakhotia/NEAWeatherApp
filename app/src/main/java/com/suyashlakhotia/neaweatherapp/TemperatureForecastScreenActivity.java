package com.suyashlakhotia.neaweatherapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.Calendar;

public class TemperatureForecastScreenActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.android.neaapitest.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature_forecast_screen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle app bar item clicks here. The app bar
        // automatically handles clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void showTemperatureForecast(View v){
        TimePicker tp = (TimePicker) findViewById(R.id.temp_TimePicker);
        String message = tp.getCurrentHour() + ":" + tp.getCurrentMinute();

        TextView title = new TextView(this);
        title.setText("Temperature at " + message);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(23);

        TextView msg = new TextView(this);
        msg.setText("28\u2103");
        msg.setPadding(10,50,10,0);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(18);

        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setCustomTitle(title);
        alert.setView(msg);
        alert.setCancelable(false);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    public void closeTemp(View v){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
