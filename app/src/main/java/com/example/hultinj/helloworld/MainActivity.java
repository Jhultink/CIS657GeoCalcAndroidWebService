/**
 * Authors: Jaredt Hultink and Jie Tao
 * Date: 5/20/2018
 */


package com.example.hultinj.helloworld;

import android.content.BroadcastReceiver;
import android.content.Context;
        import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Button;
        import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
        import android.widget.Toast;

import com.example.hultinj.helloworld.webservice.WeatherService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static com.example.hultinj.helloworld.webservice.WeatherService.BROADCAST_WEATHER;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference topRef;
    public static List<LocationLookup> allHistory;

    private float distanceInKilometers;
    private double bearingInDegrees;
    public static final int FEEDBACK = 100;
    public static final int HISTORY_RESULT = 101;
    public static final int LOCATION_SEARCH = 102;

    private static String distanceUnit="Kilometers";
    private static String bearingUnit="Degrees";

    Button calculateBtn;
    Button clearBtn;
    Button searchBtn;

    EditText longitude1EditText;
    EditText longitude2EditText;
    EditText latitude1EditText;
    EditText latitude2EditText;

    TextView distanceLabel;
    TextView bearingLabel;

    ImageView p1Icon;
    ImageView p2Icon;
    TextView p1Temp;
    TextView p2Temp;
    TextView p1Summary;
    TextView p2Summary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.my_toolbar));

        calculateBtn = findViewById(R.id.calculateBtn);
        clearBtn = findViewById(R.id.clearBtn);
        searchBtn = findViewById(R.id.searchBtn);
        longitude1EditText = findViewById(R.id.longitude1);
        longitude2EditText = findViewById(R.id.longitude2);
        latitude1EditText = findViewById(R.id.latitude1);
        latitude2EditText = findViewById(R.id.latitude2);

        distanceLabel = findViewById(R.id.distanceLbl);
        bearingLabel = findViewById(R.id.bearingLbl);

        p1Icon = findViewById(R.id.p1Icon);
        p2Icon = findViewById(R.id.p2Icon);
        p1Temp = findViewById(R.id.p1Temp);
        p2Temp = findViewById(R.id.p2Temp);
        p1Summary = findViewById(R.id.p1Summary);
        p2Summary = findViewById(R.id.p2Summary);

        allHistory = new ArrayList<LocationLookup>();

        calculateBtn.setOnClickListener(v -> {
            updateScreen();
        });

        searchBtn.setOnClickListener(v ->{
            Intent intent = new Intent(this, LocationSearchActivity.class);
            startActivityForResult(intent, LOCATION_SEARCH);
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

            this.setWeatherViews(View.INVISIBLE);
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
    public void onResume(){
        super.onResume();
        allHistory.clear();
        topRef = FirebaseDatabase.getInstance().getReference("history");
        topRef.addChildEventListener (chEvListener);
        IntentFilter weatherFilter = new IntentFilter(BROADCAST_WEATHER);
        LocalBroadcastManager.getInstance(this).registerReceiver(weatherReceiver, weatherFilter);
        this.setWeatherViews(View.INVISIBLE);
    }

    @Override
    public void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(weatherReceiver);
        topRef.removeEventListener(chEvListener);
    }

    private void setWeatherViews(int visible) {
        p1Icon.setVisibility(visible);
        p2Icon.setVisibility(visible);
        p1Summary.setVisibility(visible);
        p2Summary.setVisibility(visible);
        p1Temp.setVisibility(visible);
        p2Temp.setVisibility(visible);
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

            if(data == null)
                return;

            String[] vals = data.getStringArrayExtra("item");
            this.latitude1EditText.setText(vals[0]);
            this.longitude1EditText.setText(vals[1]);
            this.latitude2EditText.setText(vals[2]);
            this.longitude2EditText.setText(vals[3]);
            this.updateScreen();
        } else if(requestCode == LOCATION_SEARCH) {
            Parcelable par = data.getParcelableExtra("TRIP");
            LocationLookup locationLookup = Parcels.unwrap(par);

            this.latitude1EditText.setText(String.valueOf(locationLookup.getOrigLat()));
            this.longitude1EditText.setText(String.valueOf(locationLookup.getOrigLng()));
            this.latitude2EditText.setText(String.valueOf(locationLookup.getEndLat()));
            this.longitude2EditText.setText(String.valueOf(locationLookup.getEndLng()));
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

        LocationLookup entry = new LocationLookup();
        entry.setOrigLat(lat1D);
        entry.setOrigLng(lon1D);
        entry.setEndLat(lat2D);
        entry.setEndLng(lon2D);
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        entry.setTimestamp(fmt.print(DateTime.now()));
        topRef.push().setValue(entry);

        WeatherService.startGetWeather(this, Double.toString(lat1D), Double.toString(lon1D), "p1");
        WeatherService.startGetWeather(this, Double.toString(lat2D), Double.toString(lon2D), "p2");

        redrawLabels();
    }

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            double temp = bundle.getDouble("TEMPERATURE");
            String summary = bundle.getString("SUMMARY");
            String icon = bundle.getString("ICON").replaceAll("-", "_");
            String key = bundle.getString("KEY");
            int resID = getResources().getIdentifier(icon , "drawable",
                    getPackageName());
            setWeatherViews(View.VISIBLE);
            if (key.equals("p1")) {
                p1Summary.setText(summary);
                p1Temp.setText(Double.toString(temp));
                p1Icon.setImageResource(resID);

                p1Icon.setVisibility(View.INVISIBLE);
            } else {
                p2Summary.setText(summary);
                p2Temp.setText(Double.toString(temp));
                p2Icon.setImageResource(resID);
            }
        }
    };

    private ChildEventListener chEvListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            LocationLookup entry = (LocationLookup)
                    dataSnapshot.getValue(LocationLookup.class);
            entry.setKey(dataSnapshot.getKey());
            allHistory.add(entry);
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            LocationLookup entry = (LocationLookup)
                    dataSnapshot.getValue(LocationLookup.class);
            List<LocationLookup> newHistory = new ArrayList<LocationLookup>();
            for (LocationLookup t : allHistory) {
                if (!t.getKey().equals(dataSnapshot.getKey())) {
                    newHistory.add(t);
                }
            }
            allHistory = newHistory;
        }
        @Override

        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
}
