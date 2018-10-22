package com.heroes.hack.travelgo.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.adapters.RelicAdapter;
import com.heroes.hack.travelgo.async_tasks.GetUserRelicTaskLoader;
import com.heroes.hack.travelgo.objects.Relic;

import java.util.ArrayList;
import java.util.List;

public class AddRelicActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Relic>> {

    public static final String TAG = AddRelicActivity.class.getSimpleName();
    private static final int ADDED_RELICS_LOADER_ID = 4;

    private String userUrl;
    private String token;

    private String username;
    private Integer level;
    private Double latitude;
    private Double longitude;

    private RelicAdapter mAdapter;
    private TextView noDataText;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_relic);

        userUrl = getResources().getString(R.string.user_url);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            token = extras.getString("token");
            level = extras.getInt("level");
            latitude = extras.getDouble("latitude");
            longitude = extras.getDouble("longitude");
        }

        noDataText = findViewById(R.id.no_data_text);

        Toolbar toolbar = findViewById(R.id.add_relic_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Dodaj zabytek");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Funkcjonalność dodawania zabytków zostanie dodana w przyszłych wersjach aplikacji",
                        Toast.LENGTH_LONG).show();
            }
        });

        TextView canAddText = findViewById(R.id.text_can_add);
        canAddText.setText("Możesz dodać jeszcze " + level + " obiekty");

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

            String fullUrl = userUrl + username + "/customs";

            loaderBundle.putString("requestUrl", fullUrl);
            loaderBundle.putString("username", username);
            loaderBundle.putString("token", token);

            loaderManager.initLoader(ADDED_RELICS_LOADER_ID, loaderBundle, this);
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
            noDataText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Relic>> loader) {
        mAdapter.clear();
    }
}
