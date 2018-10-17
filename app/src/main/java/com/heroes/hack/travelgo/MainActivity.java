package com.heroes.hack.travelgo;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;

import java.util.List;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, LocationEngineListener,
        LoaderManager.LoaderCallbacks<List<Relic>>, MapboxMap.OnMarkerClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int RELICS_LOADER_ID = 1;
    private static final int DIFFERENCE_DISTANCE_IN_METERS = 500; // load more markers after 1km
    public String requestUrl;

    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationLayerPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;
    private MapView mapView;
    private Location helperLocation;
    private Location userLocation;

    private MarkerManager markerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestUrl = getResources().getString(R.string.request_url);
        markerManager = new MarkerManager(IconFactory.getInstance(this));

        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.getUiSettings().setScrollGesturesEnabled(false);
        mapboxMap.setMaxZoomPreference(15);
        enableLocationPlugin();

        initRelicLoader();
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();

            LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap);

            // Set the plugin's camera mode
            locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
            getLifecycle().addObserver(locationLayerPlugin);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void initializeLocationEngine() {
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            helperLocation = lastLocation;

            if (userLocation == null)
                userLocation = new Location(lastLocation);

        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void initRelicLoader() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            Bundle loaderBundle = new Bundle();
            loaderBundle.putDouble("latitude", userLocation.getLatitude());
            loaderBundle.putDouble("longitude", userLocation.getLongitude());
            loaderManager.initLoader(RELICS_LOADER_ID, loaderBundle, this);
        } else {
            Log.d(TAG, "No internet connection");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "Location changed, new coordinates:" + location.getLatitude() + "  " + location.getLongitude());

        if (helperLocation.distanceTo(location) >= DIFFERENCE_DISTANCE_IN_METERS) {
            helperLocation = location;
            userLocation = location;

            Bundle loaderBundle = new Bundle();
            loaderBundle.putDouble("latitude", location.getLatitude());
            loaderBundle.putDouble("longitude", location.getLongitude());
            getLoaderManager().restartLoader(1, loaderBundle, this);
        }
    }

    @Override
    public Loader<List<Relic>> onCreateLoader(int id, Bundle args) {
        return new RelicAsyncTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Relic>> loader, List<Relic> data) {
        if (mapboxMap != null)
            markerManager.addMarkers(data, mapboxMap);
    }

    @Override
    public void onLoaderReset(Loader<List<Relic>> loader) {
        markerManager.clear();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        String snippet[] = marker.getSnippet().split(":");

        Log.d("MainActivity", "Marker Clicked!" + marker.getTitle());
        Intent intent = new Intent(this, RelicMarkerDialog.class);
        intent.putExtra("marker_title", marker.getTitle());
        intent.putExtra("marker_dating_object", snippet[0]);
        intent.putExtra("marker_place_name", snippet[1]);
        intent.putExtra("marker_latitude", marker.getPosition().getLatitude());
        intent.putExtra("marker_longitude", marker.getPosition().getLongitude());
        startActivity(intent);

        return true;
    }
}
