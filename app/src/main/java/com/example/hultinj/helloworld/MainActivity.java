/**
 * Authors: Jaredt Hultink and Jie Tao
 * Date: 5/20/2018
 */


package com.example.hultinj.helloworld;

import android.content.Context;
        import android.content.Intent;
        import android.location.Location;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

import com.example.hultinj.helloworld.dummy.HistoryContent;

import org.joda.time.DateTime;

import java.nio.channels.FileLock;
        import java.text.ParseException;
        import java.time.Duration;

public class MainActivity extends AppCompatActivity {

    private float distanceInKilometers;
    private double bearingInDegrees;
    public static final int FEEDBACK = 100;
    public static final int HISTORY_RESULT = 101;
    private static String distanceUnit="Kilometers";
    private static String bearingUnit="Degrees";

    Button calculateBtn;
    Button clearBtn;


    EditText longitude1EditText;
    EditText longitude2EditText;
    EditText latitude1EditText;
    EditText latitude2EditText;

    TextView distanceLabel;
    TextView bearingLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.my_toolbar));

        calculateBtn = findViewById(R.id.calculateBtn);
        clearBtn = findViewById(R.id.clearBtn);
        longitude1EditText = findViewById(R.id.longitude1);
        longitude2EditText = findViewById(R.id.longitude2);
        latitude1EditText = findViewById(R.id.latitude1);
        latitude2EditText = findViewById(R.id.latitude2);

         distanceLabel = findViewById(R.id.distanceLbl);
         bearingLabel = findViewById(R.id.bearingLbl);

        calculateBtn.setOnClickListener(v -> {
            updateScreen();
        });


        clearBtn.setOnClickListener(v -> {

            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            longitude1EditText.setText("");
            longitude2EditText.setText("");
            latitude1EditText.setText("");
            latitude2EditText.setText("");

            distanceLabel.setText(getString(R.string.distanceLabel));
            bearingLabel.setText(getString(R.string.bearingLabel));

            this.distanceInKilometers = 0f;
            this.bearingInDegrees = 0f;
        });

    }

    private void redrawLabels() {
        TextView distanceLabel = findViewById(R.id.distanceLbl);
        TextView bearingLabel = findViewById(R.id.bearingLbl);

        double recalculatedDist = this.distanceInKilometers;
        double recalculatedBearing = this.bearingInDegrees;

        if (bearingUnit.equals(getString(R.string.mils))) {
            recalculatedBearing *= 17.7777777778;

        }

        if (distanceUnit.equals(getString(R.string.miles))) {
            recalculatedDist *= 0.621371;
        }

        // Round to 2 decimal places
        recalculatedDist = (double) Math.round(recalculatedDist * 100) / 100;
        recalculatedBearing = (float) Math.round(recalculatedBearing * 100) / 100;

        distanceLabel.setText(getString(R.string.distanceLabel) + " "
                + String.format("%.02f", recalculatedDist) + " "
                + distanceUnit);
        bearingLabel.setText(getString(R.string.bearingLabel) + " "
                + String.format("%.02f", recalculatedBearing) + " "
                + bearingUnit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            case R.id.action_setting:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent,FEEDBACK);
                return true;
            case R.id.action_history:
                intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivityForResult(intent, HISTORY_RESULT );
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==FEEDBACK){
            if(resultCode==RESULT_OK){
                //do unit swicth
                distanceUnit = data.getStringExtra("distanceUnit");
                bearingUnit = data.getStringExtra("bearingUnit");
                redrawLabels();
            }

        }
        else if(requestCode==HISTORY_RESULT){

            String[] vals = data.getStringArrayExtra("item");
            this.latitude1EditText.setText(vals[0]);
            this.longitude1EditText.setText(vals[1]);
            this.latitude2EditText.setText(vals[2]);
            this.longitude2EditText.setText(vals[3]);
            this.updateScreen();
        }
    }

    //recalculate the data and refresh screen
    private void updateScreen(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        String lat1 = latitude1EditText.getText().toString();
        String lon1 = longitude1EditText.getText().toString();
        String lat2 = latitude2EditText.getText().toString();
        String lon2 = longitude2EditText.getText().toString();

        if( lat1 == null || lat1.isEmpty() ||
                lon1 == null || lon1.isEmpty() ||
                lat2 == null || lat2.isEmpty() ||
                lon2 == null || lon2.isEmpty()) {

            Toast.makeText(this, R.string.enterValidValues, Toast.LENGTH_SHORT).show();
            return;
        }

        Double lat1D;
        Double lon1D;
        Double lat2D;
        Double lon2D;

        try {
            lat1D = (Double.parseDouble(lat1));
            lon1D = (Double.parseDouble(lon1));
            lat2D = (Double.parseDouble(lat2));
            lon2D = (Double.parseDouble(lon2));
        } catch (Exception parseException) {
            Toast.makeText(this, R.string.enterValidValues, Toast.LENGTH_SHORT).show();
            return;
        }

        Location loc1 = new Location("");
        loc1.setLatitude(lat1D);
        loc1.setLongitude(lon1D);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2D);
        loc2.setLongitude(lon2D);

        this.distanceInKilometers = loc1.distanceTo(loc2) / 1000.0f;
        this.bearingInDegrees = loc1.bearingTo(loc2);

        //double dLon = (lon2D-lon1D);
        //double y = Math.sin(dLon) * Math.cos(lat2D);
        //double x = Math.cos(lat1D)*Math.sin(lat2D) - Math.sin(lat1D)*Math.cos(lat2D)*Math.cos(dLon);
        //this.bearingInDegrees = Math.toDegrees((Math.atan2(y, x)));
        //this.bearingInDegrees =  ((brng + 360) % 360);

        redrawLabels();

        //presist history
        HistoryContent.HistoryItem item = new
                HistoryContent.HistoryItem(this.latitude1EditText.getText().toString(),
                this.longitude1EditText.getText().toString(), this.latitude2EditText.getText().toString(),
                this.longitude2EditText.getText().toString(), DateTime.now());
        HistoryContent.addItem(item);
    }
}
