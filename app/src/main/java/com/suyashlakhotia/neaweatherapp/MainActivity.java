package com.suyashlakhotia.neaweatherapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends Activity {
    Typeface weatherFont;

    TextView weatherIcon;
    TextView weatherDescriptor;
    TextView tempIcon;
    TextView currentTemp;
    TextView humidityIcon;
    TextView currentHumidity;

    ProgressDialog progress;

    Handler handler;

    private Context context;

    public String[] temperatureString = new String[]{
            "Now", "-", "-", "-",
            "-°C", "-°C", "-°C", "-°C"};

    public static String[] psiString = new String[]{
            "-", "-", "-", "Now",
            "-", "-", "-", "-"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        context = this;

        weatherIcon = (TextView) findViewById(R.id.WeatherIcon);
        weatherDescriptor = (TextView) findViewById(R.id.WeatherDescriptor);
        tempIcon = (TextView) findViewById(R.id.TempIcon);
        currentTemp = (TextView) findViewById(R.id.CurrentTemp);
        humidityIcon = (TextView) findViewById(R.id.HumidityIcon);
        currentHumidity = (TextView) findViewById(R.id.CurrentHumidity);

        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);
        tempIcon.setTypeface(weatherFont);
        humidityIcon.setTypeface(weatherFont);

        // Check Internet Connectivity:
        if (isNetworkAvailable() == false) {
            Toast.makeText(this, "Please check your Internet connection and try again.", Toast.LENGTH_LONG).show();
            finish();
        }

        // Display Progress Dialog:
        progress = ProgressDialog.show(context, "Loading Weather Data", "Please wait...", true);

        // Fetches & Renders Data:
        updateWeatherData();

        // Renders Recent Alerts List:
        ListView listView = (ListView) findViewById(R.id.RecentAlerts);
        RecentAlertsDB alertsDB = new RecentAlertsDB(this);
        ArrayList<HashMap<String, String>> alertsList = alertsDB.getAlertsList(6);
        ListAdapter adapter = new SimpleAdapter(this, alertsList, R.layout.list_view_element, new String[]{"time", "title"}, new int[]{R.id.alert_timestamp, R.id.alert_title});
        listView.setAdapter(adapter);
        justifyListViewHeightBasedOnChildren(listView);

        // Notification Service:
        int timePeriod = 60 * 60 * 1000; // Notification Service repeats every 1 hour.
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), timePeriod, pintent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void updateWeatherData() {
        new Thread() {
            public void run() {
                final JSONObject NEA_nowcast = RemoteFetch_NEA.fetchNEAData("nowcast");
                final JSONObject NEA_12hrs_forecast = RemoteFetch_NEA.fetchNEAData("12hrs_forecast");
                final JSONObject NEA_PSI = RemoteFetch_NEA.fetchNEAData("psi_update");
                final JSONObject OWM_forecast = RemoteFetch_OpenWeather.fetchOWMData(context);

                if (NEA_nowcast == null || NEA_12hrs_forecast == null || NEA_PSI == null || OWM_forecast == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Log.e("MainActivity", "updateWeatherData(): Error retrieving data.");
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderHomeScreen(NEA_nowcast, NEA_12hrs_forecast, NEA_PSI, OWM_forecast);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderHomeScreen(JSONObject NEA_nowcast, JSONObject NEA_12hrs_forecast, JSONObject NEA_PSI, JSONObject OWM_forecast) {
        try {
            // Initialize all Data Streams:
            JSONObject nowcastData = NEA_nowcast.getJSONObject("channel").getJSONObject("item").getJSONObject("weatherForecast").getJSONArray("area").getJSONObject(14);
            JSONObject metricsData = NEA_12hrs_forecast.getJSONObject("channel").getJSONObject("item");
            JSONArray PSIData = NEA_PSI.getJSONObject("channel").getJSONObject("item").getJSONArray("region");
            JSONArray forecastData = OWM_forecast.getJSONArray("list");


            // Weather Descriptor & Icon:
            weatherDescriptor.setText(nowcastData.getString("forecast").trim());
            setWeatherIcon(nowcastData.getString("icon"));


            // Current Temperature & Humidity:
            int tempHigh, tempLow, tempAvg;
            tempHigh = metricsData.getJSONObject("temperature").getInt("high");
            tempLow = metricsData.getJSONObject("temperature").getInt("low");
            tempAvg = (tempHigh + tempLow) / 2;
            currentTemp.setText(tempAvg + "℃");
            currentHumidity.setText(metricsData.getJSONObject("relativeHumidity").getInt("high") + "%");
            tempIcon.setText(R.string.temp_icon);
            humidityIcon.setText(R.string.humidity_icon);


            // Time Values for Grids:
            setTimeStrings();


            // Temperature Forecast Grid:
            temperatureString[4] = Integer.toString(tempAvg) + "°C";
            int hours[] = new int[3];
            int tempVals[] = new int[3];
            int diff, data_d, k;
            int index = 0;

            Calendar c = Calendar.getInstance();
            int x = c.get(Calendar.HOUR_OF_DAY);
            int d = c.get(Calendar.DAY_OF_MONTH);

            for (int j = 1; j < 4; j++) {
                hours[j - 1] = x + (j * 3);

                if (hours[j - 1] > 24) {
                    hours[j - 1] = hours[j - 1] - 24;
                }
            }

            for (k = 0; k < 10; k++) {
                diff = Integer.parseInt(forecastData.getJSONObject(k).getString("dt_txt").substring(11, 13)) - x;
                data_d = Integer.parseInt(forecastData.getJSONObject(k).getString("dt_txt").substring(8, 10));

                if (diff > 0 && data_d == d) {
                    break;
                }
            }

            for (int l = k; l < (k + 3); l++) {
                tempVals[index] = forecastData.getJSONObject(l).getJSONObject("main").getInt("temp");
                index++;
            }

            for (int v = 0; v < 3; v++) {
                temperatureString[v + 5] = Integer.toString(tempVals[v]) + "°C";
            }

            GridView temperatureGridView = (GridView) findViewById(R.id.TemperatureTimes);
            ArrayAdapter<String> temperatureAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(temperatureString, 0, 4));
            temperatureGridView.setAdapter(temperatureAdapter);

            GridView temperatureValueGridView = (GridView) findViewById(R.id.TemperatureValues);
            ArrayAdapter<String> temperatureValueAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(temperatureString, 4, 8));
            temperatureValueGridView.setAdapter(temperatureValueAdapter);


            // PSI History Grid:
            int psi[] = new int[4];
            for (int i = 0; i < 4; i++) {
                psi[i] = PSIData.getJSONObject(i).getJSONObject("record").getJSONArray("reading").getJSONObject(1).getInt("value");
            }
            for (int j = 0; j < 4; j++) {
                psiString[j + 4] = Integer.toString(psi[j]);
            }

            GridView psiGridView = (GridView) findViewById(R.id.PSITimes);
            ArrayAdapter<String> psiAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(psiString, 0, 4));
            psiGridView.setAdapter(psiAdapter);

            GridView psiValuesGridView = (GridView) findViewById(R.id.PSIValues);
            ArrayAdapter<String> psiValuesAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(psiString, 4, 8));
            psiValuesGridView.setAdapter(psiValuesAdapter);

            progress.dismiss();
        } catch (Exception e) {
            Log.e("MainActivity", "renderHomeScreen(): One or more fields not found in the JSON data.");
        }
    }

    private void setWeatherIcon(String NEA_icon) {
        String icon;

        switch (NEA_icon) {
            case "FD": // Fair Day
                icon = this.getString(R.string.weather_fair);
                break;
            case "FN": // Fair Night
                icon = this.getString(R.string.weather_fair_night);
                break;
            case "PC": // Partly Cloudy
                icon = this.getString(R.string.weather_partly_cloudy);
                break;
            case "CD": // Cloudy
                icon = this.getString(R.string.weather_cloudy);
                break;
            case "HZ": // Hazy
                icon = this.getString(R.string.weather_hazy);
                break;
            case "WD": // Windy
                icon = this.getString(R.string.weather_windy);
                break;
            case "RA": // Rainy
                icon = this.getString(R.string.weather_rainy);
                break;
            case "PS": // Passing Showers
                icon = this.getString(R.string.weather_passing_showers);
                break;
            case "SH": // Showers
                icon = this.getString(R.string.weather_showers);
                break;
            case "TS": // Thunderstorm
                icon = this.getString(R.string.weather_thundery_showers);
                break;
            default: // Weather Data N/A
                icon = this.getString(R.string.weather_na);
                break;
        }

        weatherIcon.setText(icon);
    }

    private void setTimeStrings() {
        Calendar c = Calendar.getInstance();
        int x = c.get(Calendar.HOUR_OF_DAY);
        int y; // Flag for AM/PM. 1 = PM, 0 = AM.
        int hour;

        for (int j = 1; j < 4; j++) {
            hour = x + (j * 3);

            if (hour > 11 && hour < 24) {
                if (hour != 12) {
                    hour = hour % 12;
                }
                y = 1;
            } else {
                if (hour > 24) {
                    hour = hour - 24;
                } else if (hour == 24) {
                    hour = 12;
                }
                y = 0;
            }

            if (y == 0) {
                temperatureString[j] = Integer.toString(hour) + "AM";
            } else {
                temperatureString[j] = Integer.toString(hour) + "PM";
            }
        }

        for (int k = 0; k < 3; k++) {
            hour = x - ((3 - k) * 3);

            if (hour > 11 && hour < 24) {
                if (hour != 12) {
                    hour = hour % 12;
                }
                y = 1;
            } else {
                if (hour < 0) {
                    hour = hour + 12;
                    y = 1;
                } else {
                    y = 0;
                }
            }

            if (y == 0) {
                psiString[k] = Integer.toString(hour) + "AM";
            } else {
                psiString[k] = Integer.toString(hour) + "PM";
            }
        }
    }

    public void justifyListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    public static String[] getPSIString() {
        return psiString;
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
}