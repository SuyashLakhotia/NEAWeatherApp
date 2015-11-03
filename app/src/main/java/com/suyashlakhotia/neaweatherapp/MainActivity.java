package com.suyashlakhotia.neaweatherapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.logging.SocketHandler;

public class MainActivity extends Activity {
    Typeface weatherFont;

    TextView weatherIcon;
    TextView weatherDescriptor;
    TextView currentTemp;
    TextView currentHumidity;

    Handler handler;

    public String[] temperatureString = new String[]{
            "Now", "1PM", "2PM", "3PM", "32\u00b0C",
            "32°C", "32°C", "32°C"};

    public static String[] psiString = new String[]{
            "Now", "1PM", "2PM", "3PM", "108",
            "108", "108", "108"};

    public static String[] getPSIString() {
        return psiString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        weatherIcon = (TextView) findViewById(R.id.WeatherIcon);
        weatherDescriptor = (TextView) findViewById(R.id.WeatherDescriptor);
        currentTemp = (TextView) findViewById(R.id.CurrentTemp);
        currentHumidity = (TextView) findViewById(R.id.CurrentHumidity);

        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);

        updateWeatherData();

        //Setting temperature grid view
        //uncomment the block below and edit
        /*temperatureString[0] = "";
        temperatureString[1] = "";
        temperatureString[2] = "";
        temperatureString[3] = "";
        temperatureString[4] = "";
        temperatureString[5] = "";
        temperatureString[6] = "";
        temperatureString[7] = ""; */

        GridView temperatureGridView = (GridView) findViewById(R.id.TemperatureTimes);
        ArrayAdapter<String> temperatureAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Arrays.copyOfRange(temperatureString, 0, 4));
        temperatureGridView.setAdapter(temperatureAdapter);

        GridView temperatureValueGridView = (GridView) findViewById(R.id.TemperatureValues);
        ArrayAdapter<String> temperatureValueAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Arrays.copyOfRange(temperatureString, 4, 8));
        temperatureValueGridView.setAdapter(temperatureValueAdapter);

        //Setting PSI level grid view
        //uncomment the block below to edit
        /*psiString[0] = "";
        psiString[1] = "";
        psiString[2] = "";
        psiString[3] = "";
        psiString[4] = "";
        psiString[5] = "";
        psiString[6] = "";
        psiString[7] = ""; */

        GridView psiGridView = (GridView) findViewById(R.id.PSITimes);
        ArrayAdapter<String> psiAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Arrays.copyOfRange(psiString, 0, 4));
        psiGridView.setAdapter(psiAdapter);

        GridView psiValuesGridView = (GridView) findViewById(R.id.PSIValues);
        ArrayAdapter<String> psiValuesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Arrays.copyOfRange(psiString, 4, 8));
        psiValuesGridView.setAdapter(psiValuesAdapter);
    }

    private void updateWeatherData() {
        new Thread() {
            public void run() {
                final JSONObject NEA_nowcast = RemoteFetch_NEA.fetchNEAData("nowcast");
                final JSONObject NEA_12hrs_forecast = RemoteFetch_NEA.fetchNEAData("12hrs_forecast");
                if (NEA_nowcast == null || NEA_12hrs_forecast == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error in retrieving data.", Toast.LENGTH_LONG);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(NEA_nowcast, NEA_12hrs_forecast);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject NEA_nowcast, JSONObject NEA_12hrs_forecast) {
        try {
            JSONObject nowcastData = NEA_nowcast.getJSONObject("channel").getJSONObject("item").getJSONObject("weatherForecast").getJSONArray("area").getJSONObject(0);
            JSONObject metricsData = NEA_12hrs_forecast.getJSONObject("channel").getJSONObject("item");

            weatherDescriptor.setText(nowcastData.getString("forecast").trim());
            setWeatherIcon(nowcastData.getString("icon"));

            int tempHigh, tempLow, tempAvg;
            tempHigh = metricsData.getJSONObject("temperature").getInt("high");
            tempLow = metricsData.getJSONObject("temperature").getInt("low");
            tempAvg = (tempHigh + tempLow) / 2;

            currentTemp.setText(tempAvg + "℃");
            currentHumidity.setText(metricsData.getJSONObject("relativeHumidity").getInt("high") + "%");

        } catch (Exception e) {
            Log.e("SimpleWeatherApp", "One or more fields not found in the JSON data.");
        }
    }

    private void setWeatherIcon(String NEA_icon) {
        String icon;

        switch (NEA_icon) {
            case "FD":
                icon = this.getString(R.string.weather_fair);
                break;
            case "FN":
                icon = this.getString(R.string.weather_fair_night);
                break;
            case "PC":
                icon = this.getString(R.string.weather_partly_cloudy);
                break;
            case "CD":
                icon = this.getString(R.string.weather_cloudy);
                break;
            case "HZ":
                icon = this.getString(R.string.weather_hazy);
                break;
            case "WD":
                icon = this.getString(R.string.weather_windy);
                break;
            case "RA":
                icon = this.getString(R.string.weather_rainy);
                break;
            case "PS":
                icon = this.getString(R.string.weather_passing_showers);
                break;
            case "SH":
                icon = this.getString(R.string.weather_showers);
                break;
            case "TS":
                icon = this.getString(R.string.weather_thundery_showers);
                break;
            default:
                icon = this.getString(R.string.weather_na);
                break;
        }

        weatherIcon.setText(icon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void displayPSIScreen(View v) {
        Intent intent = new Intent(this, PSIScreenActivity.class);
        startActivity(intent);
    }

    public void displayTempForecastScreen(View v) {
        Intent intent = new Intent(this, TemperatureForecastScreenActivity.class);
        startActivity(intent);
    }

    public void displayRecentAlertsScreen(View v) {
        Intent intent = new Intent(this, RecentAlertsScreenActivity.class);
        startActivity(intent);
    }

    public void justifyListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }
}
