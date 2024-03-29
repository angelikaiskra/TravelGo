package com.heroes.hack.travelgo.async_tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UserRegisterAsyncTask extends AsyncTask<String, Void, Integer> {

    public static final String TAG = UserRegisterAsyncTask.class.getSimpleName();
    private Context mContext;

    private SharedPreferences settings;

    public UserRegisterAsyncTask(Context context) {
        this.mContext = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        URL url = null;
        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
            return 400;
        }

        try {
            settings = PreferenceManager.getDefaultSharedPreferences(mContext);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept-Encoding", "identity");

            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());

            writer.write(params[1]);
            writer.flush();
            writer.close();
            int statusCode = urlConnection.getResponseCode();
            return urlConnection.getResponseCode();

        } catch (IOException e) {
            Log.d(TAG, "There was problem when trying to connect", e);
            return 400;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
    }

}
