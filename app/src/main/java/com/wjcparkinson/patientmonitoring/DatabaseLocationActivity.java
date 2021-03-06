/*
Dimin Yang    s1829127
Ewireless assignment 2
Cloud DataExchange
*/
package com.wjcparkinson.patientmonitoring;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class DatabaseLocationActivity extends AppCompatActivity {
    TextView textLat;
    TextView textLong;

    //for debugging
    private final String TAG = "DBLOCATION";

    //register manager and listener
    LocationManager locationManager;
    LocationListener locationListener;

    //optimize the power consuming
    String bestprovider;
    Criteria criteria;

    //UPLOAD LOCATION
    private DatabaseReference mReference;
    String PassedUsername;
    String passedAccount;

    //BBSID/latLng pairs
    private HashMap<String, String> knownLocs;
    WifiManager wifiManager;
    IntentFilter scanFilter;
    BroadcastReceiver receiver;
    List<ScanResult> latestResults;

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
            Log.d(TAG, "requesting updates: ");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // --------------- INDOOR LOCALISATION -----------------------------------

        // register wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // define behaviour when wifi is updated
        scanFilter = new IntentFilter();
        scanFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        scanFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // restart scan and update latest results
                Log.d(TAG, "onReceive: ");
                latestResults = wifiManager.getScanResults();
                wifiManager.startScan();
            }
        };

        // -----------------------------------------------------------------------

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
        unregisterReceiver(receiver);
        //locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUserInfo();

        // Start requesting updates again
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        } catch (SecurityException e) {
            Log.d(TAG, "onResume: " + e.toString());
        }

        // --------------- INDOOR LOCALISATION -----------------------------------

        // Reload the BSSID, LatLng hashmap from storage
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "bssidLocPairs");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            knownLocs = (HashMap<String, String>) ois.readObject();
            Log.d(TAG, "onResume: Loaded bssid/location data");
            for (String key : knownLocs.keySet()) {
                Log.d(TAG, "onResume: " + key + ", (" + knownLocs.get(key) + ")");
            }
        } catch (Exception e) {
            Log.d(TAG, "onResume: " + e.toString());
        }

        // register the receiver
        registerReceiver(receiver, scanFilter);
        wifiManager.startScan();

        // -----------------------------------------------------------------------
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

                // --------------- INDOOR LOCALISATION -----------------------------------

                // Get the strongest signal in list
                ScanResult bestSignal = null;
                int count = 1;

                if (latestResults != null) {
                    for (ScanResult result : latestResults) {
                        if (bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0) {
                            bestSignal = result;
                        }
                    }

                    // Get location of bssid with strongest signal
                    if (bestSignal != null) {
                        String bestBssid = bestSignal.BSSID;
                        String indoorLatLng = knownLocs.get(bestBssid);
                        if (indoorLatLng != null) {
                            Log.d(TAG, "onLocationChanged: Strongest bssid matched stored: " + bestBssid);
                            String[] latLngSplit = indoorLatLng.split(",");
                            tLat = Double.parseDouble(latLngSplit[0]);
                            tLong = Double.parseDouble(latLngSplit[1]);
                            Log.d(TAG, "onLocationChanged: new lat,lon: " + tLat + "," + tLong);
                        } else {
                            Log.d(TAG, "onLocationChanged: Strongest bssid not in stored list: " + bestBssid);
                        }
                    } else {
                        Log.d(TAG, "onLocationChanged: No bssids found");
                    }
                }

                // -----------------------------------------------------------------------

                textLat.setText(Double.toString(tLat));
                textLong.setText(Double.toString(tLong));



                //create database and send value to it
                mReference = FirebaseDatabase.getInstance().getReference();

                //set time on cloud
                //get the system local time
                SimpleDateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dff.setTimeZone(TimeZone.getTimeZone("GMT+01"));
                //create timestamp string
                String timestamp = dff.format(new Date());

                //call the upload method upload all info to cloud
                writeNewUser(PassedUsername,tLat,tLong,timestamp);
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

    //the method keep activity at background
    private void moveTaskTOBack(boolean b) {

    }

    //Stop the listener to save power
    public void StopListener(View view) {
        locationManager.removeUpdates(locationListener);
        Toast.makeText(DatabaseLocationActivity.this, "Location Listener stopped.", Toast.LENGTH_LONG).show();
        startActivity(new Intent(DatabaseLocationActivity.this, DatabaseAccountActivity.class));
    }

    //database structure
    @IgnoreExtraProperties
    public static class User {

        public String Username;
        public double Latitude;
        public double Longitude;
        public String Time;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String Username, double Latitude, double Longitude, String Time ) {
            this.Username = Username;
            this.Latitude = Latitude;
            this.Longitude = Longitude;
            this.Time = Time;
        }

    }

    //the method send info to database
    private void writeNewUser( String Username, double Latitude, double Longitude,String Time) {

        User user = new User(Username,Latitude,Longitude,Time);

        mReference.child(passedAccount).setValue(user);
    }

    //read passed username from previous
    String passedUser;
    private void getUserInfo(){
        SharedPreferences userInfo = getSharedPreferences(passedUser, MODE_PRIVATE);
        PassedUsername = userInfo.getString("CloudUsers", null);//read saved username
        Log.i(TAG, "location username");
        //take user account before @ for the database path name
        passedAccount = PassedUsername.substring(0,PassedUsername.indexOf("@"));
    }

}
