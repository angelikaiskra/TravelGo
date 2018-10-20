package com.heroes.hack.travelgo.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.heroes.hack.travelgo.R;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = ProfileActivity.class.getSimpleName();

    private TextView mProfileUsernameView;
    private TextView mLevelView;
    private TextView mPointsView;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.d(TAG, "Action bar is not null");
            actionBar.setTitle("Mój profil");
        }

        mProfileUsernameView = findViewById(R.id.profile_username);
        mProfileUsernameView.setText(preferences.getString("username", "Nazwa użytkownika"));

        mLevelView = findViewById(R.id.profile_level);
        mLevelView.setText(Integer.toString(preferences.getInt("level", 1)));

        String experienceStatus = preferences.getInt("experience", 1) + "/" + preferences.getInt("leftExperience", 100);
        mPointsView = findViewById(R.id.profile_points);
        mPointsView.setText(experienceStatus);
    }
}
