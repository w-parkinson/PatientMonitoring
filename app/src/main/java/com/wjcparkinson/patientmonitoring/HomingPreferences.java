package com.wjcparkinson.patientmonitoring;

import android.app.FragmentManager;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Activity that contains the preferences for the homing feature of the app
 *
 * Adam Harper, s1440298
 */
public class HomingPreferences extends AppCompatActivity {

    /**
     * On creation of preference activity, create a new HomingPreferencesFragment and replace the
     * homing_pref_placeholder in the layout file with the fragment.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homing_preferences);

        getFragmentManager().beginTransaction().replace(R.id.homing_pref_placeholder, new HomingPreferencesFragment()).commit();
    }

    /**
     * A preference fragment configured in homing_preferences.xml
     */
    public static class HomingPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load preferences from XML file
            addPreferencesFromResource(R.xml.homing_preferences);
        }
    }

}
