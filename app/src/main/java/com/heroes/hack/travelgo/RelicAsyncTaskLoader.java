package com.heroes.hack.travelgo;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * Created by Angelika Iskra on 12.10.2018.
 */
public class RelicAsyncTaskLoader extends AsyncTaskLoader<List<Relic>> {

    private static final String TAG = RelicAsyncTaskLoader.class.getSimpleName();

    private String requestUrl;
    private int objectsInMeters;
    private Double latitude;
    private Double longitude;

    public RelicAsyncTaskLoader(Context context, Bundle bundle) {
        super(context);

        requestUrl = context.getResources().getString(R.string.request_url);
        objectsInMeters = context.getResources().getInteger(R.integer.objects_in_meters);

        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Relic> loadInBackground() {
        if (requestUrl == null || latitude == null || longitude == null) {
            Log.d(TAG, "Detected null url or location during loading async task");
            return null;
        }

        List<Relic> relics = QueryUtils.fetchRelicData(requestUrl, latitude, longitude, objectsInMeters);
        return relics;
    }
}
