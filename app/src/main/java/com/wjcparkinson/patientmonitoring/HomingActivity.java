package com.wjcparkinson.patientmonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * HomingActivity.java Activity that controls the behaviour of the "Return Home" section of the app,
 * including detecting user interaction with the app, constructing the url that gets sent to the
 * Google Directions API, plotting the returned route on the map.
 *
 * Adam Harper, s1440298
 */
public class HomingActivity extends AppCompatActivity implements OnMapReadyCallback, HomingTaskLoadedCallback {

    // for debugging
    private final String TAG = "HOMING";

    // references to ui elements
    private GoogleMap map;
    private TextView durationTv;
    private TextView distanceTv;
    private Button getDirButton;

    // for getting the user's current location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // how far the map zooms in around the route
    private final int mPadding = 200;

    // distance at which the user is considered to be home
    private final float homeDistance = 20f;

    // for drawing the route on the map
    LatLng home;
    boolean liveTracking;
    long updateInterval;
    String directionMode;
    boolean hasZoomed;


    /**
     * Called when the activity is first created. Establishes references the UI elements and defines
     * behaviour that should be displayed on a location callback.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homing);

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

    /**
     * Called when the app is restored from the background. Checks if any of the preferences have
     * changed since it went to the background, and sets the class attributes accordingly. If live
     * tracking is enabled, request regular updates from the location manager.
     */
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

    /**
     * When the app is sent to the background, stop the location client from receiving updates
     */
    @Override
    protected void onPause() {
        super.onPause();

        // stop sending location requests while the app is in the background
        fusedLocationClient.removeLocationUpdates(locationCallback);

    }

    /**
     * Populate the options menu in the action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homing_menu, menu);
        return true;
    }

    /**
     * Open the preferences activity when the preferences button of the options menu is pressed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_homing_settings) {
            Intent intent = new Intent(this, HomingPreferences.class);
            this.startActivity(intent);
        }
        return true;
    }

    /**
     * Get the reference to the map once it has been set up
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Toast.makeText(this, "App needs location permission to function", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Called when the user presses the get directions button. Requests a single location update from
     * from the location client
     */
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


    /**
     * Checks if the current location is within 'homeDistance' of home. If not, places markers at current
     * and home, focuses on the region if necessary, and instantiates a HomingFetchUrl to send a request to
     * the Directions API
     */
    private void sendRouteRequest(LatLng current, LatLng home) {

        // clear the map
        map.clear();

        // Check if the user has reached their home location
        float[] distanceResult = new float[1];
        Location.distanceBetween(current.latitude, current.longitude, home.latitude, home.longitude, distanceResult);
        if (distanceResult[0] < homeDistance) {
            Toast.makeText(this, "Reached home!", Toast.LENGTH_SHORT).show();
            distanceTv.setText(R.string.distance_tv_text);
            durationTv.setText(R.string.duration_tv_text);
            return;
        }

        // Add markers
        map.addMarker(new MarkerOptions().position(current).title("Current"));
        map.addMarker(new MarkerOptions().position(home).title("Home"));

        // Move the camera to focus on the two markers (unless zoom has already happened)
        if (!hasZoomed) {
            LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
            boundBuilder.include(home);
            boundBuilder.include(current);
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), mPadding));
            hasZoomed = true;
        }

        // Send a URL request to directions API
        String url_str = constructUrl(current, home, directionMode);
        new HomingFetchURL(HomingActivity.this).execute(url_str, directionMode);
        Log.d(TAG, "Requested URL: " + url_str);

    }


    /**
     * Given the lat-lon of an origin and destination, constructs a URL that can be used to query
     * Google's Directions API
     */
    private String constructUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String str_mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + str_mode;
        return "https://maps.googleapis.com/maps/api/directions/json" + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
    }

    /**
     * Called when the path between the source and destination has been determined and is ready to
     * be displayed on screen. Plots the route on the map, updates the duration and direction text
     * field, and make a toast notification.
     */
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
        lineOptions.color(Color.BLUE);

        // Display the polyline on screen
        map.addPolyline(lineOptions);

        // Display additional route information
        distanceTv.setText("~" + pathInfo.getDistance());
        durationTv.setText("~" + pathInfo.getDuration());

        Toast toast = Toast.makeText(this, "Route Updated!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.LEFT, 10, 185);
        toast.show();
    }
}
