/**
 * Authors: Jaredt Hultink and Jie Tao
 * Date: 5/20/2018
 */

package com.example.hultinj.helloworld;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {

    private String selectedDistance;
    private String selectedBearing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(v-> {
                Intent intent = getIntent();
                intent.putExtra("distanceUnit",selectedDistance);
                intent.putExtra("bearingUnit",selectedBearing);
                setResult(RESULT_OK,intent);
                finish();
            }
        );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner distanceSpinner = findViewById(R.id.distanceSpinner);

        ArrayList<String> distanceUnits = new ArrayList<>(Arrays.asList(new String[] {getString(R.string.kilometers), getString(R.string.miles) }));
        ArrayAdapter distAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, distanceUnits);
        distAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        distanceSpinner.setAdapter(distAdapter);
        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDistance = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistance = null;
            }
        });

        Spinner bearingSpinner = findViewById(R.id.bearingSpinner);

        ArrayList<String> bearingUnits = new ArrayList<>(Arrays.asList(new String[] {getString(R.string.degrees), getString(R.string.mils) }));
        ArrayAdapter bearingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bearingUnits);
        bearingAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        bearingSpinner.setAdapter(bearingAdapter);
        bearingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBearing = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBearing = null;
            }
        });
    }
}
