package com.wjcparkinson.patientmonitoring;

import android.app.FragmentManager;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class HomingPreferences extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homing_preferences);

        getFragmentManager().beginTransaction().replace(R.id.homing_pref_placeholder, new HomingPreferencesFragment()).commit();
    }

    public static class HomingPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load preferences from XML file
            addPreferencesFromResource(R.xml.homing_preferences);
        }
    }

}
