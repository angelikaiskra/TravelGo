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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.async_tasks.RelicAsyncTaskLoader;
import com.heroes.hack.travelgo.async_tasks.UserDataAsyncTask;
import com.heroes.hack.travelgo.managers.MarkerManager;
import com.heroes.hack.travelgo.objects.Relic;
import com.heroes.hack.travelgo.objects.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, LocationEngineListener,
        LoaderManager.LoaderCallbacks<List<Relic>>, MapboxMap.OnMarkerClickListener,
        MapboxMap.OnCameraMoveStartedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int RELICS_LOADER_ID = 1;
    private static final int DIFFERENCE_DISTANCE_IN_METERS = 200; // load more markers after 200m
    private static final int MAX_CAMERA_ZOOM = 13; // best - 13

    public String userUrl;
    public String requestUrl;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationLayerPlugin;
    private LocationEngine locationEngine;
    private MapView mapView;
    private Toolbar mToolbar;
    private Location userLocation;
    private MarkerManager markerManager;
    private ProgressBar mProgressBar;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private UserDataAsyncTask mGetUserDataTask;

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
            actionBar.setTitle("Travel Go");
        }

        ImageView profilePictureView = findViewById(R.id.user_profile_picture);
        profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserProfile();
            }
        });

        requestUrl = getString(R.string.request_url);
        userUrl = getString(R.string.user_url);
        markerManager = new MarkerManager(IconFactory.getInstance(this));

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        initUserDataLoader();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener(this);
        mapboxMap.getUiSettings().setScrollGesturesEnabled(false);
        mapboxMap.setOnCameraMoveStartedListener(this);

        enableLocationPlugin();
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();

            LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
            locationLayerPlugin.setLocationLayerEnabled(true);

            // Set the plugin's camera mode
            locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
            getLifecycle().addObserver(locationLayerPlugin);

            initRelicLoader();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(1000);
        locationEngine.setFastestInterval(500);
        locationEngine.addLocationEngineListener(this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        locationEngine.requestLocationUpdates();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            userLocation = new Location(lastLocation);

        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void initUserDataLoader() {

        String token = preferences.getString("token", "");
        String username = preferences.getString("username", "");

        mGetUserDataTask = new UserDataAsyncTask(this, token, username);
        mGetUserDataTask.execute(userUrl + username, preferences.getString("token", ""));
        try {
            User user = mGetUserDataTask.get();
            if (mGetUserDataTask.get() != null) {
                saveUserData(user);
                double experience = preferences.getInt("experience", 1);
                double leftExperience = preferences.getInt("leftExperience", 100);
                double percentOfLevelCompletion = experience / leftExperience * 100;
                mProgressBar = findViewById(R.id.progress_bar);
                mProgressBar.setProgress((int) percentOfLevelCompletion);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_view, menu);
        return true;
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if (locationEngine != null) {
            locationEngine.addLocationEngineListener(this);
            if (locationEngine.isConnected()) {
                locationEngine.requestLocationUpdates();
            } else {
                locationEngine.activate();
            }
        }
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
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
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
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (userLocation.distanceTo(location) >= DIFFERENCE_DISTANCE_IN_METERS) {
            userLocation = location;

            Log.d("MainActivity", "Location changed, loading new markers");

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.my_account:
                openUserProfile();
                return true;
            case R.id.logout_item:
                logout();
            case R.id.add_relic_item:
                openAddRelic();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openUserProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("username", preferences.getString("username", ""));
        intent.putExtra("token", preferences.getString("token", ""));
        intent.putExtra("level", preferences.getInt("level", 0));
        intent.putExtra("experience", preferences.getInt("experience", 0));
        intent.putExtra("leftExperience", preferences.getInt("leftExperience", 0));

        Set<String> relicsIdSet = preferences.getStringSet("relicsList", null);
        ArrayList<String> arrayList = new ArrayList<>(relicsIdSet);
        intent.putStringArrayListExtra("relicsList", arrayList);

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

    public void openAddRelic() {
        Intent intent = new Intent(this, AddRelicActivity.class);
        intent.putExtra("username", preferences.getString("username", ""));
        intent.putExtra("token", preferences.getString("token", ""));
        intent.putExtra("level", preferences.getInt("level", 0));
        intent.putExtra("latitude", userLocation.getLatitude());
        intent.putExtra("longitude", userLocation.getLongitude());
        startActivity(intent);
    }

    private void isLoggedIn() {
        String token = preferences.getString("token", "");

        if (!EncryptionClass.validateToken(token)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public boolean onMarkerClick(@NonNull Marker marker) {

        String snippet[] = marker.getSnippet().split(":");

        Intent intent = new Intent(this, RelicMarkerDialog.class);
        intent.putExtra("marker_title", marker.getTitle());
        intent.putExtra("marker_dating_object", snippet[0]);
        intent.putExtra("marker_place_name", snippet[1]);
        intent.putExtra("marker_exp", Integer.valueOf(snippet[2]));
        intent.putExtra("relicId", Integer.valueOf(snippet[3]));
        intent.putExtra("token", preferences.getString("token", ""));
        intent.putExtra("username", preferences.getString("username", ""));
        intent.putExtra("userLatitude", userLocation.getLatitude());
        intent.putExtra("userLongitude", userLocation.getLongitude());
        intent.putExtra("markerLatitude", marker.getPosition().getLatitude());
        intent.putExtra("markerLongitude", marker.getPosition().getLongitude());
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

