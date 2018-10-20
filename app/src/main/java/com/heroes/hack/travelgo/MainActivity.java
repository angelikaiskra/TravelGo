package com.heroes.hack.travelgo;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.content.SharedPreferences;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, LocationEngineListener,
        LoaderManager.LoaderCallbacks<List<Relic>> {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int RELICS_LOADER_ID = 1;
    public static final String requestUrl = "http://51.38.134.214:8080/relics/53.46293098/14.54855329/1200";
    public static final String userUrl = "http://51.38.134.214:8080/travelgovisit/user/";

    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationLayerPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;
    private MapView mapView;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;

    private MarkerManager markerManager;

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
            actionBar.setTitle("Mapa zabytków");
        }

        markerManager = new MarkerManager();

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        String username = preferences.getString("username", "");
        Log.d(TAG, "Username is: " + username);
        mGetUserDataTask = new UserDataAsyncTask(getApplicationContext());
        mGetUserDataTask.execute(userUrl + username, preferences.getString("token", ""));
        try {
            User user = mGetUserDataTask.get();
            if (mGetUserDataTask.get() != null) {
                user.setUsername(preferences.getString("username", ""));
                user.saveUsersData(preferences);
                int percentOfLevelCompletion = (int) preferences.getInt("experience", 1) / preferences.getInt("leftExperience", 100);
                mProgressBar = findViewById(R.id.progress_bar);
                mProgressBar.setProgress(percentOfLevelCompletion);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
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
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void initRelicLoader() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(RELICS_LOADER_ID, null, this);
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

    }

    @Override
    public Loader<List<Relic>> onCreateLoader(int id, Bundle args) {
        return new RelicAsyncTaskLoader(this, requestUrl);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_view, menu);
        return true;
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
            default:
                return super.onOptionsItemSelected(item);
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
        Log.d(TAG, "validateToken method got token as an argument: " + token);

        if (token.length() == 0) return false;

        String[] splitedToken = token.split("\\.", 3);
        byte[] byteArray = android.util.Base64.decode(splitedToken[1], Base64.DEFAULT);
        String decodedToken = new String(byteArray);
        Log.d(TAG, "Decoded token: " + decodedToken);

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
}