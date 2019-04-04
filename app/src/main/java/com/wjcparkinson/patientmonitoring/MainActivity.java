package com.wjcparkinson.patientmonitoring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

// Main home screen of the app, requests permissions and starts other activities when buttons are pressed

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "HOMESCREEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a list of all permissions required by app
        String permissions[] = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.SEND_SMS
        };

        // If any of the permissions aren't granted, request permissions
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
                break;
            }
        }

        // Save BSSID, LatLng pairs to shared preferences
        HashMap<String, String> bssidLocs = new HashMap<String, String>();
        bssidLocs.put("1234", (new LatLng(0f, 0f).toString()));
        try {
            File file = new File(getDir("data", MODE_PRIVATE), "bssidLocPairs");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(bssidLocs);
            outputStream.flush();
            outputStream.close();
            Log.d(TAG, "onCreate: wrote hashmap to storage");
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.toString());
        }

    }

    @Override
    public void onClick(View view) {
    }

    // Go to setup geofence activity
    public void setupGeofence(View v){
        Intent intent = new Intent(this,GeofenceCreator.class);
        startActivity(intent);
    }

    // Go to alarm activity
    public void setupAlarm(View v){
        Log.d("MYAPP", "setupAlarm: ");
        Intent intent = new Intent(this,AlarmActivity.class);
        startActivity(intent);
    }

    // Go to return home activity
    public void setupBh(View v){
        Intent intent = new Intent(this, HomingActivity.class);
        startActivity(intent);
    }

    // Go to cloud activity
    public void setupCloud(View v){
        Intent intent = new Intent(this, DatabaseLoginActivity.class);
        startActivity(intent);
    }

    // Called when user responds to permission request. If they accept permissions, continue, otherwise
    // close the app and inform the user.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                // Permission Denied
                Toast.makeText(this, "The app needs Location and SMS permissions to function.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
