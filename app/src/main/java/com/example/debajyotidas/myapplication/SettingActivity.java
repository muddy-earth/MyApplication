package com.example.debajyotidas.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.database.FirebaseDatabase;

public class SettingActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public Preference.OnPreferenceChangeListener listener=new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SharedPreferences preferences=getSharedPreferences(Constants.SHARED_PREFS.NAME,MODE_PRIVATE);
            String UID=preferences.getString(Constants.SHARED_PREFS.UID,"no_uid");
            if (UID.equals("no_uid")) {
                return false;
            }
            if (preference.getKey().equals("switch_preference_beginner")){
                FirebaseDatabase.getInstance().getReference("users/"+UID+"/"+Constants.KEYS.USERS.BLOCK_REQUEST_FROM_BEGINNER)
                        .setValue(((boolean)newValue)?newValue:null);
            }else if (preference.getKey().equals("switch_preference_medium")){
                FirebaseDatabase.getInstance().getReference("users/"+UID+"/"+Constants.KEYS.USERS.BLOCK_REQUEST_FROM_MEDIUM)
                        .setValue(((boolean)newValue)?newValue:null);
            }else if (preference.getKey().equals("switch_preference_higher")){
                FirebaseDatabase.getInstance().getReference("users/"+UID+"/"+Constants.KEYS.USERS.BLOCK_REQUEST_FROM_HIGHER)
                        .setValue(((boolean)newValue)?newValue:null);
            }
            return true;
        }
    };

}
