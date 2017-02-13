package com.example.debajyotidas.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by overtatech-4 on 12/2/17.
 */

public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        findPreference("switch_preference_beginner").setOnPreferenceChangeListener(((SettingActivity)getActivity()).listener);
        findPreference("switch_preference_medium").setOnPreferenceChangeListener(((SettingActivity)getActivity()).listener);
        findPreference("switch_preference_higher").setOnPreferenceChangeListener(((SettingActivity)getActivity()).listener);
    }
}