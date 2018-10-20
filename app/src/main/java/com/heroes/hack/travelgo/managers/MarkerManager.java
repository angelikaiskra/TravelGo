package com.heroes.hack.travelgo.managers;

import android.util.Log;

import com.heroes.hack.travelgo.objects.Relic;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Angelika Iskra on 13.10.2018.
 */
public class MarkerManager {

    public static final String TAG = MarkerManager.class.getSimpleName();

    private List<Relic> relics;

    public MarkerManager() {
        relics = new ArrayList<>();
    }

    public void addMarkers(List<Relic> data, MapboxMap mapboxMap) {
        relics.clear();

        if (data != null && !data.isEmpty()) {
            relics.addAll(data);

            for (int i = 0; i < relics.size(); i++) {
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(relics.get(i).getLatitude(), relics.get(i).getLongitude()))
                        .title(relics.get(i).getIdentification()));

                Log.d(TAG, relics.get(i).getIdentification());
            }

        } else {
            Log.d(TAG, "No data");
        }
    }

    public void clear() {
        relics.clear();
    }
}
