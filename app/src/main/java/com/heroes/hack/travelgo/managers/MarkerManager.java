package com.heroes.hack.travelgo.managers;

import android.util.Log;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.objects.Relic;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
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
    private IconFactory iconFactory;

    public MarkerManager(IconFactory iconFactory) {
        relics = new ArrayList<>();
        this.iconFactory = iconFactory;
    }

    public void addMarkers(List<Relic> data, MapboxMap mapboxMap) {
        relics.clear();

        if (data != null && !data.isEmpty()) {
            relics.addAll(data);

            for (int i = 0; i < relics.size(); i++) {
                String snippet = relics.get(i).getDatingOfObj() + ":" + relics.get(i).getPlaceName()
                        + ":" + relics.get(i).getExp() + ":" + relics.get(i).getId();

                mapboxMap.addMarker(createMarker(relics.get(i).getLatitude(), relics.get(i).getLongitude(),
                        relics.get(i).getIdentification(), snippet));

            }

        } else {
            Log.d(TAG, "No data");
        }
    }

    private MarkerOptions createMarker(Double latitude, Double longitude, String identification, String snippet) {

        String split[] = identification.split(" ", 2);
        String splitComas[] = split[0].split(",", 2);
        String objectName = splitComas[0].toLowerCase();

        Icon icon = getIcon(objectName);
        return new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(icon)
                .title(identification)
                .snippet(snippet);
    }

    private Icon getIcon(String name) {

        switch (name) {
            case "zespół": {
                return iconFactory.fromResource(R.drawable.ic_buildings);
            }
            case "kościół": {
                return iconFactory.fromResource(R.drawable.ic_church);
            }
            case "kościoł": {
                return iconFactory.fromResource(R.drawable.ic_church);
            }
            case "dom": {
                return iconFactory.fromResource(R.drawable.ic_house);
            }
            case "willa": {
                return iconFactory.fromResource(R.drawable.ic_house);
            }
            case "willi": {
                return iconFactory.fromResource(R.drawable.ic_house);
            }
            case "dwór": {
                return iconFactory.fromResource(R.drawable.ic_house);
            }
            case "teatr": {
                return iconFactory.fromResource(R.drawable.ic_theater);
            }
            case "budynek": {
                return iconFactory.fromResource(R.drawable.ic_building);
            }
            case "hala": {
                return iconFactory.fromResource(R.drawable.ic_building);
            }
            case "budynki": {
                return iconFactory.fromResource(R.drawable.ic_building);
            }
            case "sala": {
                return iconFactory.fromResource(R.drawable.ic_building);
            }
            case "szpital": {
                return iconFactory.fromResource(R.drawable.ic_hospital);
            }
            case "klinika": {
                return iconFactory.fromResource(R.drawable.ic_hospital);
            }
            case "szkoła": {
                return iconFactory.fromResource(R.drawable.ic_school);
            }
            case "ogród": {
                return iconFactory.fromResource(R.drawable.ic_park);
            }
            case "park": {
                return iconFactory.fromResource(R.drawable.ic_park);
            }
            case "kaplica": {
                return iconFactory.fromResource(R.drawable.ic_cementery);
            }
            case "cmentarz": {
                return iconFactory.fromResource(R.drawable.ic_cementery);
            }
            case "mur": {
                return iconFactory.fromResource(R.drawable.ic_wall);
            }
            case "ruiny": {
                return iconFactory.fromResource(R.drawable.ic_ruins);
            }
            default: {
                return iconFactory.fromResource(R.drawable.ic_marker);
            }
        }
    }

    public void clear() {
        relics.clear();
    }
}
