package com.example.hultinj.helloworld;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.parceler.Parcels;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LocationSearchActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE_ORIG = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST = 2;

    private static final String TAG = "LocationSearchActivity";

    @BindView(R.id.dest_location) TextView destLocation;
    @BindView(R.id.orig_location) TextView origLocation;
    @BindView(R.id.start_date) TextView calcDateLbl;

    private DateTime calcDate;
    private DatePickerDialog dpDialog;
    private LocationLookup locLookup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        ButterKnife.bind(this);

        locLookup = new LocationLookup();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DateTime today = DateTime.now();
        dpDialog = DatePickerDialog.newInstance(this,
                today.getYear(), today.getMonthOfYear() - 1, today.getDayOfMonth());

        calcDateLbl.setText(formatted(today));
        calcDate = today;
    }

    @OnClick(R.id.dest_location)
    public void locationDestPressed() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.start_date)
    public void datePressed() {
        dpDialog.show(getFragmentManager(), "daterangedialog");
    }

    @OnClick(R.id.orig_location)
    public void locationOrigPressed() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_ORIG);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.fab)
    public void FABPressed() {
        Intent result = new Intent();
        //currentTrip.setName(jname.getText().toString());
        //DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        //currentTrip.setStartDate(fmt.print(startDate));
        //currentTrip.setEndDate(fmt.print(endDate));
        // add more code to initialize the rest of the fields
        Parcelable parcel = Parcels.wrap(locLookup);
        result.putExtra("TRIP", parcel);
        setResult(RESULT_OK, result);
        finish();
    }

    private String formatted(DateTime d) {
        return d.monthOfYear().getAsShortText(Locale.getDefault()) + " " +
                d.getDayOfMonth() + ", " + d.getYear();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_ORIG) {
            if (resultCode == RESULT_OK) {
                Place pl = PlaceAutocomplete.getPlace(this, data);
                origLocation.setText(pl.getName());

                locLookup.setOrigLat(pl.getLatLng().latitude);
                locLookup.setOrigLng(pl.getLatLng().longitude);

                Log.i(TAG, "onActivityResult: " + pl.getName() + "/" + pl.getAddress());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status stat = PlaceAutocomplete.getStatus(this, data);
                Log.d(TAG, "onActivityResult: ");
            }
            else if (requestCode == RESULT_CANCELED){
                System.out.println("Cancelled by the user");
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST) {
            if (resultCode == RESULT_OK) {
                Place pl = PlaceAutocomplete.getPlace(this, data);
                destLocation.setText(pl.getName());
                locLookup.setEndLat(pl.getLatLng().latitude);
                locLookup.setEndLng(pl.getLatLng().longitude);

                Log.i(TAG, "onActivityResult: " + pl.getName() + "/" + pl.getAddress());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status stat = PlaceAutocomplete.getStatus(this, data);
                Log.d(TAG, "onActivityResult: ");
            }
            else if (requestCode == RESULT_CANCELED){
                System.out.println("Cancelled by the user");
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        DateTime date = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
        calcDateLbl.setText(formatted(date));
    }
}
