package com.heroes.hack.travelgo.activities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.async_tasks.VisitAsyncTask;

public class RelicMarkerDialog extends Activity {

    public static final String TAG = RelicMarkerDialog.class.getSimpleName();

    private int relicId;
    private String title;
    private String datingOfObject;
    private String placeName;
    private int exp;
    private String token;
    private String requestUrl;
    private String username;

    private VisitAsyncTask asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_relic_marker);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            relicId = extras.getInt("relicId");
            title = extras.getString("marker_title");
            datingOfObject = extras.getString("marker_dating_object");
            placeName = extras.getString("marker_place_name");
            exp = extras.getInt("marker_exp");
            token = extras.getString("token");
            username = extras.getString("username");
        }

        requestUrl = getResources().getString(R.string.request_url_visit);

        Button buttonVisit = findViewById(R.id.buttonVisit);
        buttonVisit.setOnClickListener(v -> visitButtonClicked());

        setUpLayout();
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

    private void visitButtonClicked() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected()) {

            //Register async task to send request to user
            asyncTask = new VisitAsyncTask(token, username, relicId);
            asyncTask.execute(requestUrl);
            try {
                if (asyncTask.get() == 200) {
                    Toast.makeText(this, "Odwiedzono obiekt: " + relicId, Toast.LENGTH_SHORT).show();
                    finish();

                } else if (asyncTask.get() == 406) {
                    Toast.makeText(this, "Ten obiekt został już odwiedzony: " + relicId, Toast.LENGTH_SHORT).show();
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
}

