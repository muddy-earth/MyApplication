package com.example.debajyotidas.myapplication;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;


/**
 * Created by overtatech-4 on 12/2/17.
 */

public class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        findPreference("switch_preference_beginner").setOnPreferenceChangeListener(this);
        findPreference("switch_preference_medium").setOnPreferenceChangeListener(this);
        findPreference("switch_preference_higher").setOnPreferenceChangeListener(this);
    }

    /**
     * Called when a Preference has been changed by the user. This is
     * called before the state of the Preference is about to be updated and
     * before the state is persisted.
     *
     * @param preference The changed Preference.
     * @param newValue   The new value of the Preference.
     * @return True to update the state of the Preference with the new value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}