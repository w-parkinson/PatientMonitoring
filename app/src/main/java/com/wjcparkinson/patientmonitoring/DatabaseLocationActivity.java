/*
Dimin Yang    s1829127
Ewireless assignment 2
Cloud DataExchange
*/
package com.wjcparkinson.patientmonitoring;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DatabaseLocationActivity extends AppCompatActivity {
    TextView textLat;
    TextView textLong;


    //register manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    //optimize the power consuming
    String bestprovider;
    Criteria criteria;

    //UPLOAD LOCATION
    private DatabaseReference mReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        textLat = (TextView) findViewById(R.id.textLat);
        textLong = (TextView) findViewById(R.id.textLong);

        //call the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //initialize the listener
        locationListener = new mylocationListener();

        //optimize power
        bestprovider = locationManager.getBestProvider(getcriteria(), true);

        //Update the current activity periodically
        try {
            Log.d("PATIENTAPP", "requesting updates: ");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }


    }

    //optimize power
    private Criteria getcriteria() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    //sycn with cloud
    double tLat;
    double tLong;
    //location listener class
    class mylocationListener implements LocationListener {

        //location changed and upload every 10s because the sensor setting
        @Override
        public void onLocationChanged(Location location) {
            Log.d("PATIENTAPP", "onLocationChanged: ");
            if (location != null) {
                tLat = location.getLatitude();
                tLong = location.getLongitude();
                textLat.setText(Double.toString(tLat));
                textLong.setText(Double.toString(tLong));

                //create database and send value to it
                mReference = FirebaseDatabase.getInstance().getReference();
                final DatabaseReference myLat = mReference.child("Latitude");
                final DatabaseReference myLong = mReference.child("Longitude");
                myLat.setValue(tLat);
                myLong.setValue(tLong);

                //set time on cloud
                //get the system local time
                SimpleDateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dff.setTimeZone(TimeZone.getTimeZone("GMT+00"));
                //create timestamp string
                String timestamp = dff.format(new Date());
                //upload time to the database
                final DatabaseReference myTimeStamp = mReference.child("TimeStamp");
                myTimeStamp.setValue(timestamp);

                // if current network is in list
                    // return location of hub

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    }


    /*
    // back to select
    public void backTo(View view) {
        startActivity(new Intent(DatabaseLocationActivity.this, DatabaseAccountActivity.class));
        //do not kill activity
        moveTaskTOBack(true);
    }
*/

    //the method keep activity at background
    private void moveTaskTOBack(boolean b) {

    }

    //Stop the listener to save power
    public void StopListener(View view) {
        locationManager.removeUpdates(locationListener);
        Toast.makeText(DatabaseLocationActivity.this, "Location Listener stopped.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(DatabaseLocationActivity.this, DatabaseAccountActivity.class));
    }

}
