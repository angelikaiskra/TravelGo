package com.heroes.hack.travelgo.async_tasks;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.objects.Relic;
import com.heroes.hack.travelgo.utils.RelicConnectionUtils;

import java.util.List;

/**
 * Created by Angelika Iskra on 20.10.2018.
 */
public class GetUserRelicTaskLoader extends AsyncTaskLoader<List<Relic>> {

    private static final String TAG = GetUserRelicTaskLoader.class.getName();

    private String requestUrl;
    private String username;
    private String token;

    public GetUserRelicTaskLoader(Context context, Bundle bundle) {
        super(context);
        requestUrl = getContext().getResources().getString(R.string.user_url);
        username = bundle.getString("username");
        token = bundle.getString("token");
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

        String fullUrl = requestUrl + username + "/relics";

        List<Relic> relics = RelicConnectionUtils.fetchUserRelicData(fullUrl, token);
        return relics;
    }

}
