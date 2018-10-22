package com.heroes.hack.travelgo.async_tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.heroes.hack.travelgo.utils.QueryUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Angelika Iskra on 20.10.2018.
 */
public class VisitAsyncTask extends AsyncTask<String, Void, JSONObject> {

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
    protected JSONObject doInBackground(String... params) {
        if (params[0] == null || username == null || relicId == null) { // params[0] = requestUrl
            Log.d(TAG, "Detected null url or username during loading async task");
            return null;
        }

        if (token == null) {
            Log.d(TAG, "Incorrect token");
            return null;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        URL url = null;


        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
            return null;
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

            String requestBody = createJsonRequestBody().toString();
            writer.write(requestBody);
            writer.flush();
            writer.close();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                inputStream = urlConnection.getInputStream();
                String response = QueryUtils.readFromStream(inputStream);

                return createJSONResponse(response);

            } else if (statusCode == 406) {
                return createJSONResponse("{error: \"place already visited\"}");
            }
            return null;

        } catch (IOException e) {
            Log.d(TAG, "There was problem when trying to connect", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
        Log.i(TAG, "Received UserData: " + result);
    }

    private JSONObject createJsonRequestBody() {
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("relicId", relicId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    private JSONObject createJSONResponse(String response) {
        JSONObject responseJson = null;
        try{
            responseJson = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return responseJson;
    }
}
