package com.heroes.hack.travelgo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.async_tasks.VisitAsyncTask;
import com.heroes.hack.travelgo.objects.Relic;
import com.heroes.hack.travelgo.objects.User;
import com.heroes.hack.travelgo.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.List;

public class RelicMarkerDialog extends Activity {

    public static final String TAG = RelicMarkerDialog.class.getSimpleName();
    public static final int MAX_DISTANCE_TO_MARKER = 100;

    private int relicId;
    private String title;
    private String datingOfObject;
    private String placeName;
    private int exp;
    private String token;
    private String requestUrl;
    private String username;

    private Double userLatitude;
    private Double userLongitude;
    private Double markerLatitude;
    private Double markerLongitude;

    private VisitAsyncTask asyncTask;
    private Button buttonVisit;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_relic_marker);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            relicId = extras.getInt("relicId");
            title = extras.getString("marker_title");
            datingOfObject = extras.getString("marker_dating_object");
            placeName = extras.getString("marker_place_name");
            exp = extras.getInt("marker_exp");
            token = extras.getString("token");
            username = extras.getString("username");

            userLatitude = extras.getDouble("userLatitude");
            userLongitude = extras.getDouble("userLongitude");
            markerLatitude = extras.getDouble("markerLatitude");
            markerLongitude = extras.getDouble("markerLongitude");
        }

        requestUrl = getResources().getString(R.string.request_url_visit);

        buttonVisit = findViewById(R.id.buttonVisit);
        buttonVisit.setOnClickListener(v -> visitButtonClicked());

        setUpLayout();
        checkIfButtonCanBeClicked();
    }

    private void setUpLayout() {
        TextView titleView = findViewById(R.id.titleView);
        TextView datingView = findViewById(R.id.datingView);
        TextView placeNameView = findViewById(R.id.placeNameView);
        TextView expViewValue = findViewById(R.id.expValueView);

        titleView.setText(title);
        expViewValue.setText(exp + " EXP!");

        datingOfObject = datingOfObject.replaceAll("\\s+", "");
        if (datingOfObject.length() != 0) {
            datingView.setText(getString(R.string.dating_of_object) + " " + datingOfObject);
        }

        placeNameView.setText(getString(R.string.place_name) + " " + placeName);
    }

    private void checkIfButtonCanBeClicked() {

        Location userLocation = new Location("");
        userLocation.setLatitude(userLatitude);
        userLocation.setLongitude(userLongitude);

        Location markerLocation = new Location("");
        markerLocation.setLatitude(markerLatitude);
        markerLocation.setLongitude(markerLongitude);

        if (userLocation.distanceTo(markerLocation) > MAX_DISTANCE_TO_MARKER) {
            Toast.makeText(this, "Jesteś za daleko od obiektu", Toast.LENGTH_LONG).show();
            buttonVisit.setEnabled(false);
            buttonVisit.setAlpha(.5f);

        } else {
            buttonVisit.setEnabled(true);
        }
    }

    private void visitButtonClicked() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected()) {

            //Register async task to send request to user
            // It should returns user data
            asyncTask = new VisitAsyncTask(token, username, relicId);
            asyncTask.execute(requestUrl);
            try {
                JSONObject updatedDataResponse = asyncTask.get();
                if (!TextUtils.isEmpty(updatedDataResponse.getString("error"))) {
                    Toast.makeText(this, "Ten obiekt został już odwiedzony: " + relicId, Toast.LENGTH_SHORT).show();
                    finish();

                } else if (!TextUtils.isEmpty(updatedDataResponse.getString("username"))) {
                    Toast.makeText(this, "Odwiedzono obiekt: " + relicId, Toast.LENGTH_SHORT).show();

                    JSONArray relicsJSONArray = updatedDataResponse.getJSONArray("relics");
                    String username = updatedDataResponse.getString("username");
                    int level = updatedDataResponse.getInt("level");
                    int experience = updatedDataResponse.getInt("currentExperience");
                    int leftExperience = updatedDataResponse.getInt("leftExperience");

                    List<Relic> userRelics = QueryUtils.extractRelicsFromJson(relicsJSONArray.toString());

                    User updatedUser = new User(username, level, experience, leftExperience, userRelics);
                    saveUserData(updatedUser);
                    finish();

                } else {
                    Toast.makeText(this, "Błąd połączenia", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "No internet connection");
        }
    }

    private void saveUserData(User user) {
        editor = preferences.edit();
        editor.putString("username", user.getUsername());
        editor.putInt("level", user.getLevel());
        editor.putInt("experience", user.getExperience());
        editor.putInt("leftExperience", user.getLeftExperience());
        editor.putStringSet("relicsList", user.getRelicsIdSet());
        editor.apply();
    }
}

