package com.wjcparkinson.patientmonitoring;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PhoneSetter extends AppCompatActivity implements View.OnClickListener {

    Button confirm;
    EditText number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_setter);

        confirm = findViewById(R.id.confirmNumber);
        number = findViewById(R.id.phoneNumber);
    }

    @Override
    public void onClick(View v) {

    }
}
