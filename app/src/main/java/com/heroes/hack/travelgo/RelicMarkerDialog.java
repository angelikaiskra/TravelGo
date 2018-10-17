package com.heroes.hack.travelgo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class RelicMarkerDialog extends Activity {

    public static final String TAG = RelicMarkerDialog.class.getSimpleName();

    private String title;
    private String datingOfObject;
    private String placeName;
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
            markerLatitude = extras.getInt("marker_latitude");
            markerLongitude = extras.getInt("marker_longitude");
        }

        setUpLayout();
    }

    private void setUpLayout() {
        TextView titleView = findViewById(R.id.titleView);
        TextView datingView = findViewById(R.id.datingView);
        TextView placeNameView = findViewById(R.id.placeNameView);

        titleView.setText(title);
        datingView.setText("Datowanie obiektu: " + datingOfObject);
        placeNameView.setText("Lokalizacja obiektu: " + placeName);

        Button buttonVisit = findViewById(R.id.buttonVisit);
        if (buttonVisit.isActivated()) {
            buttonVisit.setEnabled(false);
            buttonVisit.setText("ODWIEDZONO!");
        } else {
            buttonVisit.setText("ODWIEDŹ MNIE!");
        }
    }
}