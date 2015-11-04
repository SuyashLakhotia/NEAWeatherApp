package com.suyashlakhotia.neaweatherapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public void showTemperatureForecast(View v) {
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
                            Log.e("TempForecastActivity", "getTempForecast(): Error retrieving data.");
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            getTempValue(OWM_forecast);
                        }
                    });
                }
            }
        }.start();
    }

    private void getTempValue(JSONObject OWM_forecast) {
        try {
            JSONArray forecastData = OWM_forecast.getJSONArray("list");
            int target_hour = tp.getCurrentHour();
            Calendar c = Calendar.getInstance();
            int cur_hour = c.get(Calendar.HOUR_OF_DAY);
            int cur_date = c.get(Calendar.DAY_OF_MONTH);
            int next_index, diff, data_d;

            for (next_index = 0; next_index < 10; next_index++) {
                diff = Integer.parseInt(forecastData.getJSONObject(next_index).getString("dt_txt").substring(11, 13)) - cur_hour;
                data_d = Integer.parseInt(forecastData.getJSONObject(next_index).getString("dt_txt").substring(8, 10));

                if (diff > 0 && data_d == cur_date) {
                    break;
                }
            }

            for (int i = next_index; i < 36; i++) {
                diff = target_hour - Integer.parseInt(forecastData.getJSONObject(i).getString("dt_txt").substring(11, 13));

                if (diff < 3 && diff >= 0) {
                    temp = Integer.toString(forecastData.getJSONObject(i).getJSONObject("main").getInt("temp"));
                    break;
                }
            }

        } catch (JSONException e) {
            Log.e("TempForecastActivity", "getTempValue(): One or more fields not found in the JSON data.");
        }
    }

    public void closeTemp(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
