package com.heroes.hack.travelgo;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Angelika Iskra on 13.10.2018.
 */

public final class QueryUtils {

    public static final String TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
        // Private constructor because no one should ever create a QueryUtils object
        // If we set class as abstract, then we cant make it final
    }

    public static List<Relic> fetchRelicData(String requestUrl, Double latitude, Double longitude, int objectsInMeters) {

        // Create URL object and pass user coordinates in GET method
        URL url = createUrl(requestUrl, latitude, longitude, objectsInMeters);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of relics
        List<Relic> relics = extractFeatureFromJson(jsonResponse);

        return relics;
    }

    public static List<Relic> extractFeatureFromJson(String relicJSON) {

        if (TextUtils.isEmpty(relicJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding relics to
        List<Relic> relics = new ArrayList<>();

        try {
            // Parse the response
            JSONArray baseJsonArray = new JSONArray(relicJSON);

            for (int i = 0; i < baseJsonArray.length(); i++) {
                JSONObject currentRelicObject = baseJsonArray.getJSONObject(i);

                int id = currentRelicObject.getInt("id");
                String identification = currentRelicObject.getString("identification");
                String datingOfObj = currentRelicObject.getString("datingOfObj");
                String placeName = currentRelicObject.getString("placeName");
                String districtName = currentRelicObject.getString("districtName");
                String voivodeshipName = currentRelicObject.getString("voivodeshipName");
                Double latitude = currentRelicObject.getDouble("latitude");
                Double longitude = currentRelicObject.getDouble("longitude");
                //TODO: Fetch real exp
//                int exp = currentRelicObject.getInt("exp");
                int exp = 200;

                Relic relic = new Relic(id, identification, datingOfObj, placeName, districtName, voivodeshipName, latitude, longitude, exp);
                relics.add(relic);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the relics JSON results", e);
        }

        return relics;
    }


    // Returns new URL object from the given string URL.
    private static URL createUrl(String stringUrl, Double latitude, Double longitude, int objectsInMeters) {
        URL url = null;

        String fullUrl = stringUrl + latitude + "/" + longitude + "/" + objectsInMeters;
        try {
            url = new URL(fullUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }

    // Make an HTTP request to the given URL and return a String as the response.
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the relic JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    // Convert the InputStream into a String which contains the
    // whole JSON response from the server.
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}