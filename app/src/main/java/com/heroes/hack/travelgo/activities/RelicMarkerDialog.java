package com.heroes.hack.travelgo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.heroes.hack.travelgo.R;

public class RelicMarkerDialog extends Activity {

    public static final String TAG = RelicMarkerDialog.class.getSimpleName();

    private String title;
    private String datingOfObject;
    private String placeName;
    private int exp;
    private int markerLatitude;
    private int markerLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_relic_marker);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString("marker_title");
            datingOfObject = extras.getString("marker_dating_object");
            placeName = extras.getString("marker_place_name");
            exp = extras.getInt("marker_exp");
            markerLatitude = extras.getInt("marker_latitude");
            markerLongitude = extras.getInt("marker_longitude");
        }

        setUpLayout();
    }

    private void setUpLayout() {
        TextView titleView = findViewById(R.id.titleView);
        TextView datingView = findViewById(R.id.datingView);
        TextView placeNameView = findViewById(R.id.placeNameView);
        TextView expViewValue = findViewById(R.id.expValueView);

        titleView.setText(title);
        expViewValue.setText(exp + " EXP!");

        datingOfObject = datingOfObject.replaceAll("\\s+","");
        if (datingOfObject.length() != 0) {
            datingView.setText(getString(R.string.dating_of_object) + " " + datingOfObject);
        }

        placeNameView.setText(getString(R.string.place_name) + " " + placeName);

        Button buttonVisit = findViewById(R.id.buttonVisit);
        if (buttonVisit.isActivated()) {
            buttonVisit.setEnabled(false);
            buttonVisit.setText(R.string.visited);
        } else {
            buttonVisit.setText(R.string.visit_me);
        }
    }
}