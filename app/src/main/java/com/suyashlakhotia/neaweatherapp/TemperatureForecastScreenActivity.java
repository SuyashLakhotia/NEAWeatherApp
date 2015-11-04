package com.suyashlakhotia.neaweatherapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.Calendar;

public class TemperatureForecastScreenActivity extends Activity {
    private String temp = "X";
    private Handler handler;
    private Context context;

    TimePicker tp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature_forecast_screen);

        handler = new Handler();
        context = this;

        tp = (TimePicker) findViewById(R.id.temp_TimePicker);
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
        String message = tp.getCurrentHour() + ":" + tp.getCurrentMinute();

        TextView title = new TextView(this);
        title.setText("Temperature at " + message);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(23);

        getTempForecast();

        TextView msg = new TextView(this);
        msg.setText(temp + "°C");
        msg.setPadding(20, 10, 10, 10);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(35);

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

    private void getTempForecast() {
        new Thread() {
            public void run() {
                final JSONObject OWM_forecast = RemoteFetch_OpenWeather.fetchOWMData(context);

                if (OWM_forecast == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Error in retrieving data.", Toast.LENGTH_LONG);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            getValue(OWM_forecast);
                        }
                    });
                }
            }
        }.start();
    }

    private void getValue(JSONObject OWM_forecast) {
        try {
            JSONArray forecastData = OWM_forecast.getJSONArray("list");
            int target_hour = tp.getCurrentHour();
            Calendar c = Calendar.getInstance();
            int cur_hour = c.get(Calendar.HOUR_OF_DAY);
            int next_index = 0, diff = 0;

            for (next_index = 0; next_index < 10; next_index++) {
                diff = Integer.parseInt(forecastData.getJSONObject(next_index).getString("dt_txt").substring(11, 13)) - cur_hour;

                System.out.println(forecastData.getJSONObject(next_index).getString("dt_txt").substring(11, 13));

                if (diff > 0) {
                    System.out.println(next_index);
                    break;
                }
            }

            for (int i = next_index; i < 36; i++) {
                diff = target_hour - Integer.parseInt(forecastData.getJSONObject(i).getString("dt_txt").substring(11, 13));

                if (diff < 3 && diff >= 0) {
                    System.out.println(i);
                    temp = Integer.toString(forecastData.getJSONObject(i).getJSONObject("main").getInt("temp"));
                    break;
                }
            }

        } catch (JSONException e) {
            Log.e("NEAWeatherApp", "One or more fields not found in JSON data.");
        }
    }

    public void closeTemp(View v){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
