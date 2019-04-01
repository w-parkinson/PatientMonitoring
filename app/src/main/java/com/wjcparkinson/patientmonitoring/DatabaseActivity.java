/*
Dimin Yang    s1829127
Ewireless assignment 2
Cloud DataExchange
*/
package com.wjcparkinson.patientmonitoring;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DatabaseActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "DatabaseActivity";
    private TextView tv_user;
    private TextView tv_latitude;
    private TextView tv_longitude;
    private TextView tv_timestamp;
    private DatabaseReference mReference;

    double tLat;
    double tLong;
    String time;

    private GoogleMap mMap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        updateUI();
        basicReadData();

    }



    public void basicReadData() {
        // Create database
        mReference = FirebaseDatabase.getInstance().getReference();
        // Create the info we need
        final DatabaseReference user = mReference.child("User");
        DatabaseReference latitude = mReference.child("Latitude");
        DatabaseReference longitude = mReference.child("Longitude");
        final DatabaseReference timestamp = mReference.child("TimeStamp");


        // Read from the database

        //username
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                tv_user.setText("Username：" + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        //latitude
        latitude.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 tLat = dataSnapshot.getValue(double.class);
                tv_latitude.setText( "Latitude: " + tLat);
                if(time != null){
                    updateMap();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        //longitude
        longitude.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 tLong = dataSnapshot.getValue(double.class);
                tv_longitude.setText("Longitude: " + tLong);
                updateMap();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        //timestamp
        timestamp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                time = dataSnapshot.getValue(String.class);
                tv_timestamp.setText("Time: " + time);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    private void updateUI() {
        tv_user = (TextView) findViewById(R.id.tv_user);
        tv_latitude = (TextView) findViewById(R.id.tv_latitude);
        tv_longitude = (TextView) findViewById(R.id.tv_longitude);
        tv_timestamp = (TextView) findViewById(R.id.tv_timestamp);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in edinburgh and move the camera
        LatLng edinburgh = new LatLng(55.953251, -3.188267);
        mMap.addMarker(new MarkerOptions().position(edinburgh).title("Marker in Edinburgh"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edinburgh));
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        //UI setting
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);

        /*
        mMap = googleMap;
        // ... get a map.
        // Add a thin red line from London to New York.
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(55.953251, -3.188267))
                .width(5)
                .color(Color.RED));
                */
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //called anytime if have new location
    public void updateMap(){

        //update location
        LatLng latLng = new LatLng(tLat, tLong);

        Circle circle= mMap.addCircle(new CircleOptions()
                .center(new LatLng(tLat, tLong))
                .radius(1.5)
                .strokeWidth(5)
                .strokeColor(Color.argb(100,102,204,255))
                .fillColor(Color.argb(100,51,102,255))
        );

        //update marker through time
        String markertime = (String) tv_timestamp.getText();
        mMap.addMarker(new MarkerOptions().position(latLng).title(markertime));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // draw a circle current position



    }


    //called if activity resume
    public void setUpMapIfNeed(){
        if(mMap == null){
            //try to obtain the map from the SupportMapFragment
            //obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }


    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeed();
    }
}
