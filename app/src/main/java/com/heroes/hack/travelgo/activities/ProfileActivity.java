package com.heroes.hack.travelgo.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.adapters.RelicAdapter;
import com.heroes.hack.travelgo.async_tasks.GetUserRelicTaskLoader;
import com.heroes.hack.travelgo.objects.Relic;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Relic>> {

    public static final String TAG = ProfileActivity.class.getSimpleName();
    private static final int USER_RELIC_LOADER_ID = 3;

    private String username;
    private String token;
    private String userUrl;
    private Integer level;
    private Integer exp;
    private Integer leftExp;
    private ArrayList<String> listRelicsId;

    private RelicAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userUrl = getResources().getString(R.string.user_url);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            token = extras.getString("token");
            level = extras.getInt("level");
            exp = extras.getInt("experience");
            leftExp = extras.getInt("leftExperience");
            level = extras.getInt("level");
            listRelicsId = extras.getStringArrayList("relicsList");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.d(TAG, "Action bar is not null");
            actionBar.setTitle("Mój profil");
        }

        TextView usernameView = findViewById(R.id.profile_username);
        usernameView.setText(username);

        TextView levelView = findViewById(R.id.my_profile_level);
        levelView.setText(level.toString() + " poziom");

        TextView pointsView = findViewById(R.id.my_profile_points);
        pointsView.setText(exp.toString() + "/" + leftExp.toString() + " punktów");

        loadListView();
    }

    private void loadListView() {
        mAdapter = new RelicAdapter(this, new ArrayList<>());
        ListView relicsListView = findViewById(R.id.relics_list);
        relicsListView.setAdapter(mAdapter);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();

            Bundle loaderBundle = new Bundle();

            loaderBundle.putString("username", username);
            loaderBundle.putString("token", token);

            loaderManager.initLoader(USER_RELIC_LOADER_ID, loaderBundle, this);
        } else {

            Log.d(TAG, "No data");
        }
    }

    @Override
    public Loader<List<Relic>> onCreateLoader(int id, Bundle args) {
        return new GetUserRelicTaskLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<Relic>> loader, List<Relic> relics) {
        mAdapter.clear();

        if (relics != null && !relics.isEmpty()) {
            mAdapter.addAll(relics);
        } else {
            Log.d(TAG, "No data");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Relic>> loader) {
        mAdapter.clear();
    }

}
