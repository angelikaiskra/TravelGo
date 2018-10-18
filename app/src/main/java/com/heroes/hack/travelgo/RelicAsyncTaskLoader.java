package com.heroes.hack.travelgo;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by Angelika Iskra on 12.10.2018.
 */
public class RelicAsyncTaskLoader extends AsyncTaskLoader<List<Relic>> {

    private static final String TAG = RelicAsyncTaskLoader.class.getSimpleName();

    private String requestUrl;

    public RelicAsyncTaskLoader(Context context, String requestUrl) {
        super(context);

        this.requestUrl = requestUrl;
//        requestUrl = context.getResources().getString(R.string.request_url);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Relic> loadInBackground() {
        if (requestUrl == null) {
            return null;
        }

        List<Relic> relics = QueryUtils.fetchRelicData(requestUrl);
        return relics;
    }
}