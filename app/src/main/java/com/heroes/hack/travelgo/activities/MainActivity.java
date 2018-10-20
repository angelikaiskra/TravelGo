package com.heroes.hack.travelgo.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.async_tasks.RelicAsyncTaskLoader;
import com.heroes.hack.travelgo.managers.MarkerManager;
import com.heroes.hack.travelgo.objects.Relic;
import com.heroes.hack.travelgo.utils.EncryptionClass;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, LocationEngineListener,
        LoaderManager.LoaderCallbacks<List<Relic>>, MapboxMap.OnMarkerClickListener,
        MapboxMap.OnCameraMoveStartedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int RELICS_LOADER_ID = 1;
    private static final int DIFFERENCE_DISTANCE_IN_METERS = 100; // load more markers after 1km
    private static final int MAX_CAMERA_ZOOM = 13; // best - 13

    public String requestUrl;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationLayerPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;
    private MapView mapView;
    private Toolbar mToolbar;
    private NavigationView navigationView;
    private Location helperLocation;
    private Location userLocation;
    private MarkerManager markerManager;
    private DrawerLayout mDrawerLayout;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        isLoggedIn();

        // Set default app bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Mapa zabytków");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        requestUrl = getResources().getString(R.string.request_url);
        markerManager = new MarkerManager(IconFactory.getInstance(this));

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);

                        switch (menuItem.getItemId()) {
                            case R.id.my_account:
                                openUserProfile();
                                break;
                            case R.id.logout_item:
                                logout();
                                break;
                        }

                        mDrawerLayout.closeDrawer(GravityCompat.START);

                        return true;
                    }
                }
        );

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.getUiSettings().setScrollGesturesEnabled(false);
        mapboxMap.setOnCameraMoveStartedListener(this);
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
            String token = preferences.getString("token", "");

            if (userLocation == null) {
                Toast.makeText(this, R.string.error_during_localization, Toast.LENGTH_SHORT).show();
            } else {
                loaderBundle.putDouble("latitude", userLocation.getLatitude());
                loaderBundle.putDouble("longitude", userLocation.getLongitude());
                loaderBundle.putString("token", token);
                loaderManager.initLoader(RELICS_LOADER_ID, loaderBundle, this);
            }
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

        if (userLocation.distanceTo(location) >= DIFFERENCE_DISTANCE_IN_METERS) {
            userLocation = location;

            Bundle loaderBundle = new Bundle();
            String token = preferences.getString("token", "");

            loaderBundle.putDouble("latitude", location.getLatitude());
            loaderBundle.putDouble("longitude", location.getLongitude());
            loaderBundle.putString("token", token);
            getLoaderManager().restartLoader(RELICS_LOADER_ID, loaderBundle, this);
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
    public void onBackPressed() {
        super.onBackPressed();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void openUserProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void logout() {
        editor = preferences.edit();
        editor.remove("username");
        editor.remove("token");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void isLoggedIn() {
        String token = preferences.getString("token", "");

        if (!validateToken(token)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private boolean validateToken(String token) {

        String decodedToken = EncryptionClass.getDecodedToken(token);

        try {
            JSONObject decodedTokenJson = new JSONObject(decodedToken);
            long currentDate = new Date().getTime();
            long expirationDate = Long.parseLong(decodedTokenJson.get("exp").toString().concat("000"));

            Log.d(TAG, "CurrentTime: " + currentDate);
            Log.d(TAG, "Expiration token time: " + expirationDate);
            if (expirationDate > currentDate) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean onMarkerClick(@NonNull Marker marker) {

        String snippet[] = marker.getSnippet().split(":");

        Log.d("MainActivity", "Marker Clicked!" + marker.getTitle());
        Intent intent = new Intent(this, RelicMarkerDialog.class);

        intent.putExtra("marker_title", marker.getTitle());
        intent.putExtra("marker_dating_object", snippet[0]);
        intent.putExtra("marker_place_name", snippet[1]);
        intent.putExtra("marker_exp", Integer.valueOf(snippet[2]));
        intent.putExtra("relicId", Integer.valueOf(snippet[3]));
        intent.putExtra("marker_latitude", marker.getPosition().getLatitude());
        intent.putExtra("marker_longitude", marker.getPosition().getLongitude());
        startActivity(intent);

        return true;
    }

    @Override
    public void onCameraMoveStarted(int motionCode) {

        if (mapboxMap.getCameraPosition().zoom <= MAX_CAMERA_ZOOM) {
            CameraPosition position = new CameraPosition.Builder()
                    .zoom(MAX_CAMERA_ZOOM)
                    .build();

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position));
        }
    }

}

