/*
Dimin Yang    s1829127
Ewireless assignment 2
Cloud DataExchange
*/
package com.wjcparkinson.patientmonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;

public class DatabaseAccountActivity extends AppCompatActivity {

    private static final String TAG = "pass" ;
    TextView tv_username;
    String passedUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);


        Intent intent = getIntent();
        tv_username = (TextView)findViewById(R.id.tv_username);
        tv_username.setText(intent.getStringExtra("email"));


        //save username for up load
        String passuserValue = intent.getStringExtra("email");
        SharedPreferences userInfo = getSharedPreferences(passedUser, MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//get Editor
        //write username
        editor.putString("CloudUsers", passuserValue);
        editor.commit();//submit info
        Log.i(TAG, "Saved pass");
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

    //get save username from previous activity
    private void getUserInfo(){
        SharedPreferences userInfo = getSharedPreferences(passedUser, MODE_PRIVATE);
        String passuserValue = userInfo.getString("CloudUsers", null);//read saved username
        Log.i(TAG, "read username!!!");
        tv_username.setText(passuserValue);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().signOut();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
    }

}