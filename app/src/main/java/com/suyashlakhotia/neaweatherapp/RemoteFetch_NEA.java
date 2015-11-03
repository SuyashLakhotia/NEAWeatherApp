package com.suyashlakhotia.neaweatherapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.XML;

public class RemoteFetch_NEA {
    public static JSONObject fetchNEAData(String dataset) {
        String NEA_API = "http://www.nea.gov.sg/api/WebAPI?dataset=" + dataset + "&keyref=781CF461BB6606AD0308169EFFAA8231021BA33828C73DAE";
        try {
            URL obj = new URL(NEA_API);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }

            reader.close();

            JSONObject NEAData = XML.toJSONObject(response.toString());

            if (con.getResponseCode() != 200) {
                Log.e("NEAWeatherApp", "Error in fetching data from NEA Datasets.");
            }

            return NEAData;
        } catch (Exception e) {
            Log.e("NEAWeatherApp", "Exception in fetching data from NEA Datasets.");
            Log.e("NEAWeatherApp", e.getMessage());
            return null;
        }
    }

}
