package com.heroes.hack.travelgo.async_tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Angelika Iskra on 20.10.2018.
 */
public class VisitAsyncTask extends AsyncTask<String, Void, Integer> {

    private static final String TAG = VisitAsyncTask.class.getSimpleName();

    private String token;
    private String username;
    private Integer relicId;

    public VisitAsyncTask(String token, String username, Integer relicId) {
        this.token = token;
        this.username = username;
        this.relicId = relicId;
    }

    @Override
    protected Integer doInBackground(String... params) {
        if (params[0] == null || username == null || relicId == null) { // params[0] = requestUrl
            Log.d(TAG, "Detected null url or username during loading async task");
            return null;
        }

        if (token == null) {
            Log.d(TAG, "Incorrect token");
            return null;
        }

        HttpURLConnection urlConnection = null;
        URL url = null;
        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
            return 400;
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept-Encoding", "identity");
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);

            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());

            String requestBody = createJson().toString();
            writer.write(requestBody);
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
        Log.i(TAG, "Received UserData: " + result);
    }

    private JSONObject createJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("relicId", relicId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
