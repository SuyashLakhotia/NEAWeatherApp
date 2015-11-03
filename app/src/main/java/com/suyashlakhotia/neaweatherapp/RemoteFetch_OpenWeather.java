package com.suyashlakhotia.neaweatherapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemoteFetch_OpenWeather {

    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/forecast?q=%s&units=metric";

    public static JSONObject fetchOWMData(Context context) {
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API, "Singapore"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("x-api-key", context.getString(R.string.OpenWeather_key));

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not successful.
            if (data.getInt("cod") != 200) {
                Log.e("NEAWeatherApp","Error: OpenWeatherMap Data");
                return null;
            }

            return data;
        } catch (Exception e) {
            return null;
        }
    }
}