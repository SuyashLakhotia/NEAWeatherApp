package com.suyashlakhotia.neaweatherapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    Handler handler;

    public static String psiLocation[] = new String[5]; // NCEWS

    private GoogleApiClient mGoogleApiClient;

    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        initListeners();
    }

    private void initListeners() {
        getMap().setOnMarkerClickListener(this);
        getMap().setOnMapLongClickListener(this);
        getMap().setOnInfoWindowClickListener(this);
        getMap().setOnMapClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        handler = new Handler();
        fetchPSIData();

        initCamera();
    }

    private void fetchPSIData() {
        new Thread() {
            public void run() {
                final JSONObject NEA_PSI = RemoteFetch_NEA.fetchNEAData("psi_update");

                if (NEA_PSI == null) {
                    Log.e("NEAWeatherApp", "Error retrieving data.");
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            getMapPSIValues(NEA_PSI);
                        }
                    });
                }
            }
        }.start();
    }

    private void getMapPSIValues(JSONObject NEA_PSI) {
        int vals[] = new int[5]; // NCEWS

        try {
            JSONArray PSIData = NEA_PSI.getJSONObject("channel").getJSONObject("item").getJSONArray("region");

            vals[0] = PSIData.getJSONObject(0).getJSONObject("record").getJSONArray("reading").getJSONObject(0).getInt("value");
            vals[1] = PSIData.getJSONObject(2).getJSONObject("record").getJSONArray("reading").getJSONObject(0).getInt("value");
            vals[2] = PSIData.getJSONObject(3).getJSONObject("record").getJSONArray("reading").getJSONObject(0).getInt("value");
            vals[3] = PSIData.getJSONObject(4).getJSONObject("record").getJSONArray("reading").getJSONObject(0).getInt("value");
            vals[4] = PSIData.getJSONObject(5).getJSONObject("record").getJSONArray("reading").getJSONObject(0).getInt("value");

            for (int i = 0; i < 5; i++) {
                psiLocation[i] = Integer.toString(vals[i]);
            }

            setPSIMarker("North", psiLocation[0], 1.428671, 103.822158);
            setPSIMarker("Central", psiLocation[1], 1.345901, 103.822158);
            setPSIMarker("East", psiLocation[2], 1.345901, 103.936937);
            setPSIMarker("West", psiLocation[3], 1.345901, 103.708236);
            setPSIMarker("South", psiLocation[4], 1.273726, 103.822158);
        } catch (JSONException e) {
            Log.e("NEAWeatherApp", "One or more fields not found in the JSON data.");
        }
    }

    private void initCamera() {
        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(1.3500, 103.8000))
                .zoom(10f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();
        getMap().animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);
        getMap().getUiSettings().setRotateGesturesEnabled(false);
        getMap().getUiSettings().setScrollGesturesEnabled(false);
        getMap().getUiSettings().setZoomGesturesEnabled(false);
        getMap().getUiSettings().setMapToolbarEnabled(false);
        getMap().setMapType(MAP_TYPES[1]);
    }

    private void setPSIMarker(String title, String val, double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(title);
        options.snippet("PSI: " + val);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.info28));

        getMap().addMarker(options);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}