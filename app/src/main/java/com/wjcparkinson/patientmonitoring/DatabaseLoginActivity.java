/*
Dimin Yang    s1829127
Ewireless assignment 2
Cloud DataExchange
*/
package com.wjcparkinson.patientmonitoring;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DatabaseLoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";

    private EditText mEmail;
    private EditText mPassword;

    private Button btnLogin;
    private Button btnSignup;

    private CheckBox chkUser;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEmail = (EditText) findViewById(R.id.emailfield);
        mPassword = (EditText) findViewById(R.id.passwordfield);

        btnLogin = (Button) findViewById(R.id.btnlogin);
        btnSignup = (Button) findViewById(R.id.btnsignup);

        chkUser = (CheckBox)findViewById(R.id.chk_save);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    //pass the value and goto next activity
                    Intent intent = new Intent();
                    intent.setClass(DatabaseLoginActivity.this, DatabaseAccountActivity.class);
                    //username
                    intent.putExtra("email",mEmail.getText().toString());
                    startActivity(intent);
                }
            }
        };

        //Set buttons method
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }


    //signin method
    private void SignIn(){

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        //if check box checked, save the username
        if(chkUser.isChecked()){
            saveUserInfo();
        }else{
            removeUserInfo(); // if unchecked, remove save username
        }



        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(DatabaseLoginActivity.this, "Field cannot be empty.", Toast.LENGTH_LONG).show();
        }
        else{
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DatabaseLoginActivity.this, "Sign in failed.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }
    //registration method
    private void createAccount(){
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Field cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Field cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        //create user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(DatabaseLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }


    // function for saving the username
    String username;
    private void saveUserInfo(){
        String email = mEmail.getText().toString();
        SharedPreferences userInfo = getSharedPreferences(username, MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//get Editor
        //write username
        editor.putString("username",email);
        editor.commit();//submit info
        Log.i(TAG, "Saved successfully");
    }


    //read username method
    private void getUserInfo(){
        SharedPreferences userInfo = getSharedPreferences(username, MODE_PRIVATE);
        String username = userInfo.getString("username", null);//read saved username
        Log.i(TAG, "read username");
        mEmail.setText(username);
    }

    //remove saved value
    private void removeUserInfo(){
        SharedPreferences userInfo = getSharedPreferences(username, MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//get Editor
        editor.remove("username");
        editor.commit();
        Log.i(TAG, "remove saved value");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();// recover username every resume
    }
}


