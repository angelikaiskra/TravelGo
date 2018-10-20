package com.heroes.hack.travelgo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import static com.heroes.hack.travelgo.QueryUtils.fetchUserData;

public class UserDataAsyncTask  extends AsyncTask<String, Void, User> {
    public static final String TAG = UserDataAsyncTask.class.getSimpleName();
    public Context mContext;

    public UserDataAsyncTask(Context context) {
        this.mContext = context;
        Log.d(TAG, "UserDataAsyncTask initialized");
    }
    @Override
    protected User doInBackground(String ... params) {
        Log.d(TAG, "Working in Background");
        Log.d(TAG, "URL: " + params[0]);
        Log.d(TAG, "Token is: " + params[1]);
        return fetchUserData(params[0], params[1]);
    }

    @Override
    protected void onPostExecute(User result) {
        super.onPostExecute(result);
        Log.i(TAG, "UserData: " + result);
    }
}
