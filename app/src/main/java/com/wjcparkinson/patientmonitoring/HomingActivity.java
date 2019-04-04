package com.wjcparkinson.patientmonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomingActivity extends AppCompatActivity implements OnMapReadyCallback, HomingTaskLoadedCallback {

    // for debugging
    private final String TAG = "HOMING";

    // references to ui elements
    private GoogleMap mMap;
    private TextView durationTv;
    private TextView distanceTv;
    private Button getDirButton;

    // for getting the user's current location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // how far the map zooms in around the route
    private final int mPadding = 200;

    // for drawing the route on the map
    LatLng home;
    boolean liveTracking;
    long updateInterval;
    String directionMode;
    boolean hasZoomed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bh_activity);

        // get references to the ui elements
        durationTv = findViewById(R.id.durationTv);
        distanceTv = findViewById(R.id.distanceTv);
        getDirButton = findViewById(R.id.getDirButton);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // define behaviour when a location update is received
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) return;

                // get the most recent location result and request directions to home
                Location loc = locationResult.getLastLocation();
                LatLng current = new LatLng(loc.getLatitude(), loc.getLongitude());
                sendRouteRequest(current, home);
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();

        hasZoomed = false;

        // Find the saved preferences for homing
        SharedPreferences homingPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        float homeLat = Float.parseFloat(homingPrefs.getString("home_location_lat", "0.0"));
        float homeLon = Float.parseFloat(homingPrefs.getString("home_location_lon", "0.0"));
        home = new LatLng(homeLat, homeLon);
        liveTracking = homingPrefs.getBoolean("live_tracking", false);
        directionMode = homingPrefs.getString("direction_mode", "walking");
        updateInterval = 1000 * Integer.parseInt(homingPrefs.getString("update_interval", "5"));

        Log.d(TAG, "onResume: homeLat = " + homeLat);
        Log.d(TAG, "onResume: homeLon = " + homeLon);
        Log.d(TAG, "onResume: liveTracking = " + liveTracking);
        Log.d(TAG, "onResume: directionMode = " + directionMode);

        // Enable live tracking by requesting updates from the location client
        if (liveTracking) {
            // direction button is disabled in live tracking mode
            getDirButton.setEnabled(false);

            // Define the settings of the location request
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(updateInterval);
            locationRequest.setFastestInterval(updateInterval);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Send the Location Request
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        } else {
            getDirButton.setEnabled(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop sending location requests while the app is in the background
        fusedLocationClient.removeLocationUpdates(locationCallback);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_homing_settings) {
            Intent intent = new Intent(this, HomingPreferences.class);
            this.startActivity(intent);
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Toast.makeText(this, "App needs location permission to function", Toast.LENGTH_SHORT).show();
        }

    }

    // Called when user touches get directions button
    public void getDirections(View view) {

        // Define the settings of the location request
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);

        // Send the request
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    private void sendRouteRequest(LatLng current, LatLng home) {

        // clear the map
        mMap.clear();

        // Add markers
        mMap.addMarker(new MarkerOptions().position(current).title("Start"));
        mMap.addMarker(new MarkerOptions().position(home).title("Home"));

        // Move the camera to focus on the two markers (unless zoom has already happened)
        if (!hasZoomed) {
            LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
            boundBuilder.include(home);
            boundBuilder.include(current);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), mPadding));
            hasZoomed = true;
        }

        // Send a URL request to directions API
        String url_str = constructUrl(current, home, directionMode);
        new HomingFetchURL(HomingActivity.this).execute(url_str, directionMode);
        Log.d(TAG, "Requested URL: " + url_str);

    }

    private String constructUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String str_mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + str_mode;
        return "https://maps.googleapis.com/maps/api/directions/json" + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
    }

    @Override
    public void onTaskDone(HomingPathInfo pathInfo) {

        List<HashMap<String, String>> path = pathInfo.getPath();
        ArrayList<LatLng> points = new ArrayList<>();
        PolylineOptions lineOptions = new PolylineOptions();

        // Fetch all the points in the path
        for (int j = 0; j < path.size(); j++) {
            double lat = Double.parseDouble(path.get(j).get("lat"));
            double lon = Double.parseDouble(path.get(j).get("lon"));
            points.add(new LatLng(lat, lon));
        }

        // Add all the points in the route to LineOptions
        lineOptions.addAll(points);
        lineOptions.width(20);
        lineOptions.color(Color.GREEN);

        // Display the polyline on screen
        mMap.addPolyline(lineOptions);

        // Display additional route information
        distanceTv.setText("~" + pathInfo.getDistance());
        durationTv.setText("~" + pathInfo.getDuration());

    }
}
