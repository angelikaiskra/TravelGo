package com.heroes.hack.travelgo.async_tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import static com.heroes.hack.travelgo.utils.QueryUtils.fetchToken;

public class UserLoginAsyncTask extends AsyncTask<String, Void, String> {
    public static final String TAG = UserLoginAsyncTask.class.getSimpleName();
    private Context mContext;

    private SharedPreferences settings;

    public UserLoginAsyncTask(Context context) {
        this.mContext = context;
        Log.d(TAG, "UserLoginAsyncTask initialized");
    }

    @Override
    protected String doInBackground(String ... params) {
        Log.d(TAG, "Working in Background");

        return fetchToken(params[0], params[1]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i(TAG, "POST RESPONSE: " + result);
    }
}
