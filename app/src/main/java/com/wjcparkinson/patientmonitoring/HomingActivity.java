package com.wjcparkinson.patientmonitoring;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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

public class HomingActivity extends FragmentActivity implements OnMapReadyCallback, HomingTaskLoadedCallback {

    // references to ui elements
    private GoogleMap mMap;
    private TextView durationTv;
    private TextView distanceTv;

    private FusedLocationProviderClient fusedLocationClient;
    private final int mPadding = 200;
    Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bh_activity);

        // get references to the ui elements
        durationTv = findViewById(R.id.durationTv);
        distanceTv = findViewById(R.id.distanceTv);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        // clear the map
        mMap.clear();

        // Add a marker for current location
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // Get the current latitude and longitude, add a marker
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(current).title("Start"));

                        // Add a marker for home
                        LatLng home = new LatLng(55.953251, -3.188267);
                        mMap.addMarker(new MarkerOptions().position(home).title("Home"));

                        // Move the camera to focus on the two markers
                        LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
                        boundBuilder.include(home);
                        boundBuilder.include(current);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), mPadding));

                        // Send a URL request to directions API
                        String url_str = constructUrl(current, home, "walking");
                        new HomingFetchURL(HomingActivity.this).execute(url_str, "walking");
                        Log.d("MAPPO", "Requested URL: " + url_str);
                    }
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(this, "App needs location permission to function", Toast.LENGTH_SHORT).show();
        }

    }

    private String constructUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String str_mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + str_mode;
        return "https://maps.googleapis.com/maps/api/directions/json" + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
    }


    @Override
    public void onTaskDone(PolylineOptions polylineOptions, int duration, int distance) {
        currentPolyline = mMap.addPolyline(polylineOptions);
        distanceTv.setText("~" + distance + "m");
        durationTv.setText("~" + duration/60 + " minutes" );
        Log.d("MAPPO ", "duration " + duration);
        Log.d("MAPPO", "distance " + distance);
    }
}
