package com.suyashlakhotia.neaweatherapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends Activity {
    Typeface weatherFont;

    TextView weatherIcon;
    TextView weatherDescriptor;
    TextView currentTemp;
    TextView currentHumidity;

    Handler handler;

    private Context context;

    public String[] temperatureString = new String[]{
            "Now", "XPM", "XPM", "XPM", "100°C",
            "100°C", "100°C", "100°C"};

    public static String[] psiString = new String[]{
            "XPM", "XPM", "XPM", "Now", "1",
            "1", "1", "1"};

    public static String[] getPSIString() {
        return psiString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        context = this;

        weatherIcon = (TextView) findViewById(R.id.WeatherIcon);
        weatherDescriptor = (TextView) findViewById(R.id.WeatherDescriptor);
        currentTemp = (TextView) findViewById(R.id.CurrentTemp);
        currentHumidity = (TextView) findViewById(R.id.CurrentHumidity);

        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);

        updateWeatherData();

        //Starting Notification Service
        //Notification Service repeats every 10 secs
        int timePeriod = 10 * 1000;
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), timePeriod, pintent);

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
                            Toast.makeText(MainActivity.this, "Error in retrieving data.", Toast.LENGTH_LONG);
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
            JSONObject nowcastData = NEA_nowcast.getJSONObject("channel").getJSONObject("item").getJSONObject("weatherForecast").getJSONArray("area").getJSONObject(0);
            JSONObject metricsData = NEA_12hrs_forecast.getJSONObject("channel").getJSONObject("item");
            JSONArray PSIData = NEA_PSI.getJSONObject("channel").getJSONObject("item").getJSONArray("region");
            JSONArray forecastData = OWM_forecast.getJSONArray("list");

            /*
                Weather Descriptor & Icon:
            */
            weatherDescriptor.setText(nowcastData.getString("forecast").trim());
            setWeatherIcon(nowcastData.getString("icon"));


            /*
                Current Temp & Humidity:
            */
            int tempHigh, tempLow, tempAvg;
            tempHigh = metricsData.getJSONObject("temperature").getInt("high");
            tempLow = metricsData.getJSONObject("temperature").getInt("low");
            tempAvg = (tempHigh + tempLow) / 2;
            currentTemp.setText(tempAvg + "℃");
            currentHumidity.setText(metricsData.getJSONObject("relativeHumidity").getInt("high") + "%");


            /*
                Time Values for Grids:
            */
            setTimeStrings();


            /*
                Temperature Forecast Grid:
            */
            temperatureString[4] = Integer.toString(tempAvg) + "°C";
            int hours[] = new int[3];
            int tempVals[] = new int[3];
            int diff;
            int k;
            int index = 0;

            Calendar c = Calendar.getInstance();
            int x = c.get(Calendar.HOUR_OF_DAY);

            for (int j = 1; j < 4; j++) {
                hours[j - 1] = x + (j * 3);

                if (hours[j - 1] > 24) {
                    hours[j - 1] = hours[j - 1] - 24;
                }
            }

            for (k = 0; k < 10; k++) {
                diff = Integer.parseInt(forecastData.getJSONObject(k).getString("dt_txt").substring(11, 12)) - x;

                if (diff > 0) {
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
            ArrayAdapter<String> temperatureAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(temperatureString, 0, 4));
            temperatureGridView.setAdapter(temperatureAdapter);

            GridView temperatureValueGridView = (GridView) findViewById(R.id.TemperatureValues);
            ArrayAdapter<String> temperatureValueAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(temperatureString, 4, 8));
            temperatureValueGridView.setAdapter(temperatureValueAdapter);


            /*
                PSI History Grid:
            */
            int psi[] = new int[4];
            for (int i = 0; i < 4; i++) {
                psi[i] = PSIData.getJSONObject(i).getJSONObject("record").getJSONArray("reading").getJSONObject(1).getInt("value");
            }
            for (int j = 0; j < 4; j++) {
                psiString[j + 4] = Integer.toString(psi[j]);
            }

            GridView psiGridView = (GridView) findViewById(R.id.PSITimes);
            ArrayAdapter<String> psiAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(psiString, 0, 4));
            psiGridView.setAdapter(psiAdapter);

            GridView psiValuesGridView = (GridView) findViewById(R.id.PSIValues);
            ArrayAdapter<String> psiValuesAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, Arrays.copyOfRange(psiString, 4, 8));
            psiValuesGridView.setAdapter(psiValuesAdapter);

        } catch (Exception e) {
            Log.e("NEAWeatherApp", "One or more fields not found in the JSON data.");
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

    private void setTimeStrings() {
        Calendar c = Calendar.getInstance();
        int x = c.get(Calendar.HOUR_OF_DAY);
        int y, hour;

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
