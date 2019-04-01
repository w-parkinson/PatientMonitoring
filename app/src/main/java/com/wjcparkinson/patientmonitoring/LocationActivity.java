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
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class LocationActivity extends AppCompatActivity {
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


        //grant the permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }


        //optimize power
        bestprovider = locationManager.getBestProvider(getcriteria(), true);


        //Update the current activity periodically
        locationManager.requestLocationUpdates(bestprovider, 10000, 5, locationListener);


    }

    //REQUEST LOCATION PERMISSION

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                // Permission Denied
                //  displayFrameworkBugMessageAndExit();
                Toast.makeText(this, "Please give the permission in AppInfo.", Toast.LENGTH_LONG).show();
                finish();
            }
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
        locationManager.requestLocationUpdates(bestprovider, 10000, 0, locationListener);

    }


    //sycn with cloud
    double tLat;
    double tLong;
    //location listener class
    class mylocationListener implements LocationListener {

        //location changed and upload every 10s because the sensor setting
        @Override
        public void onLocationChanged(Location location) {
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
        startActivity(new Intent(LocationActivity.this, AccountActivity.class));
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
        Toast.makeText(LocationActivity.this, "Location Listener stopped.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(LocationActivity.this, AccountActivity.class));
    }

}
