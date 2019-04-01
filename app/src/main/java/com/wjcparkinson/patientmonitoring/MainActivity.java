package com.wjcparkinson.patientmonitoring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button geofenceButton;
    Button alarmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    private void setup(){
        geofenceButton = findViewById(R.id.button);
        alarmButton = findViewById(R.id.button2);

        geofenceButton.setOnClickListener(this);
        alarmButton.setOnClickListener(this);
    }




    @Override
    public void onClick(View v) {
        Log.d("HEYO", "onclick: ");
        switch (v.getId()){
            case R.id.button:
                setupGeofence();
                break;
            case R.id.button2:
                setupAlarm();
                break;
            case R.id.button3:
                Log.d("HEYO", "button3: ");
                setupBh();
                break;
            case R.id.button4:
                setupCloud();
                break;
        }
    }

    private void setupGeofence(){
        Intent intent = new Intent(this,GeofenceCreator.class);
        startActivity(intent);
    }

    private void setupAlarm(){
        Intent intent = new Intent(this,AlarmActivity.class);
        startActivity(intent);
    }

    private void setupBh(){
        Log.d("HEYO", "setupBh: ");
        Intent intent = new Intent(this,BhMainActivity.class);
        startActivity(intent);
    }

    private void setupCloud(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}
