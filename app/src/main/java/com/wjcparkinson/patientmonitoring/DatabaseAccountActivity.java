/*
Dimin Yang    s1829127
Ewireless assignment 2
Cloud DataExchange
*/
package com.wjcparkinson.patientmonitoring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DatabaseAccountActivity extends AppCompatActivity {

    private FirebaseUser mAuth;
    TextView tv_username;
    private DatabaseReference mReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);


        Intent intent = getIntent();
        tv_username = (TextView)findViewById(R.id.tv_username);
        tv_username.setText(intent.getStringExtra("email"));


        //create database username object
        mReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference user = mReference.child("User");
        //upload user name
        user.setValue(intent.getStringExtra("email"));

    }

    // go to database activity
    public void startDB(View view){
        startActivity(new Intent(DatabaseAccountActivity.this,DatabaseActivity.class));
    }
    // go to location activity
    public void startLC(View view){
        startActivity(new Intent(DatabaseAccountActivity.this, DatabaseLocationActivity.class));
    }

    public void logOut(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(DatabaseAccountActivity.this, MainActivity.class));


    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().signOut();
    }
}