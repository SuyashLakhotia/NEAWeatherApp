package com.suyashlakhotia.neaweatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.Arrays;

public class PSIScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psi_screen);

        // Setting the PSI Grid Values:
        GridView psiGridView = (GridView) findViewById(R.id.psi_PSITimes);
        ArrayAdapter<String> psiAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, Arrays.copyOfRange(MainActivity.getPSIString(), 0, 4));
        psiGridView.setAdapter(psiAdapter);

        GridView psiValuesGridView = (GridView) findViewById(R.id.psi_PSIValues);
        ArrayAdapter<String> psiValuesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, Arrays.copyOfRange(MainActivity.getPSIString(), 4, 8));
        psiValuesGridView.setAdapter(psiValuesAdapter);
    }

    public void closePSI(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}