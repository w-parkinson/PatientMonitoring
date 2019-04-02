//William Parkinson                 s1433610
package com.wjcparkinson.patientmonitoring;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class GeofenceCreator extends AppCompatActivity implements OnMapReadyCallback,Geofence {

    private GoogleMap mMap;     //Google map object
    private LatLng EDINBURGH = new LatLng(55.923688,-3.173722);     //Geographic coordinates for Kings buildings in Edinburgh.
    private Button confirm;     //Button for confirming Geofence selection.
    private String tag = "main activity";                 //Tag used for this activity.
    private String GEOFENCE_KEY = "451";            //Request ID used for creating geofence.
    private LatLng geoCentre;       //Coordinates of the selected of the chosen geofence.

    final int radius = 300;     //Radius of the geofence in meters. Currently set at 300.

    PendingIntent intent;       //Pending intent for when the user leaves or enters the geofence.
    GeofencingClient client;        //Client for the geofence.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_creator);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        confirm = findViewById(R.id.geofence_confirm);      //Get the button for confirming the geofence.

        intent = PendingIntent.getBroadcast(this,0,
                new Intent(".ACTION_RECEIVE_GEOFENCE"),PendingIntent.FLAG_UPDATE_CURRENT);      //Intent used for notifying the GeofenceReceiver when a geofence transition is detected.

        client = LocationServices.getGeofencingClient(this);        //Get the geofencing client
    }


    //Method used for initializing the map and detecting when the user selects a geofence area.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Set the the map location and zoom.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(EDINBURGH));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));

        //Create a circle to represent the selected geofence when the user clicks on the map.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                confirm.setVisibility(View.VISIBLE);        //Make the confirm button visible when the user clicks the map.
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(radius)
                        .fillColor(0x40ff0000)
                        .strokeColor(Color.TRANSPARENT)
                        .strokeWidth(2));
                geoCentre = latLng;
            }
        });

        //Call the method for setting up the geofence when the user clicks the confirm button.
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geofenceSetup();
            }
        });
    }

    //Used to setup the geofence.
    private void geofenceSetup(){
        Geofence.Builder geocreator = new Geofence.Builder();
        geocreator.setRequestId(GEOFENCE_KEY);
        geocreator.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT); //Trigger geofence for when the user either enters or leaves the geofence.
        geocreator.setCircularRegion(geoCentre.latitude, geoCentre.longitude, radius);
        geocreator.setExpirationDuration(Geofence.NEVER_EXPIRE);        //Set duration of the geofence as permanent.

        GeofencingRequest.Builder geoBuilder = new GeofencingRequest.Builder();
        geoBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);     //Trigger geofence if the user is initially inside or outside.
        geoBuilder.addGeofence(geocreator.build());

        //Check permission for accessing the location.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task task = client.addGeofences(geoBuilder.build(), intent);       //Add the geofence

        //Check result of adding geofence.
        task.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Log.i(tag,"Geofence created");
                messageMaker();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(tag,"Geofence failed to create");
            }
        });

        //After the geofence has been created return to the main activity.
        Intent intentMain = new Intent(this,MainActivity.class);
        startActivity(intentMain);
    }

    //Display message if geofence successfully created.
    private void messageMaker(){
        Toast.makeText(this,"Patient boundary successfully established",Toast.LENGTH_SHORT).show();
    }

    //Remove the geofence when app is closed.
    @Override
    protected void onDestroy(){
        super.onDestroy();
        client.removeGeofences(intent);
    }

    //Not used
    @Override
    public String getRequestId() {
        return null;
    }



}
