package com.heroes.hack.travelgo.utils;

import android.text.TextUtils;
import android.util.Log;

import com.heroes.hack.travelgo.objects.Relic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    public static String fetchToken(String requestUrl, String authLoginData) {
        String token = "";

        if (TextUtils.isEmpty(requestUrl) || TextUtils.isEmpty(authLoginData)) {
            return token;
        }

        // Create url
        URL url = createUrl(requestUrl);

        if (url == null) {
            return token;
        }

        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept-Encoding", "identity");
            urlConnection.connect();

            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());

            writer.write(authLoginData);
            writer.flush();
            writer.close();

            int statusCode = urlConnection.getResponseCode();

            Log.d(TAG, "Response code when trying to log in is: " + statusCode);
            if (statusCode == 200) {
                token = urlConnection.getHeaderField("Authorization");
            }

        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the token JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        Log.d(TAG, "Token is: " + token);
        return token;
    }

    public static List<Relic> extractRelicsFromJson(String relicJSON) {

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
                int exp = currentRelicObject.getInt("exp");

                Relic relic = new Relic(id, identification, datingOfObj, placeName, districtName, voivodeshipName, latitude, longitude, exp);
                relics.add(relic);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the relics JSON results", e);
        }

        return relics;
    }

    // Make an HTTP request to fetch Token using user's credentials
    public static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }

    // Make an HTTP request to the given URL and return a String as the response.
    public static String makeHttpRequest(String token, URL url) throws IOException {

        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        Log.d(TAG, "Url in makeHtppRequest is: " + url);
        Log.d(TAG, "Token in makeHtppRequest is: " + token);
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");

            if (!TextUtils.isEmpty(token)) {
                urlConnection.addRequestProperty("Authorization", "Bearer " + token);
                Log.d(TAG, "Auth header: " + urlConnection.getHeaderField("Authorization"));
            }
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
    public static String readFromStream(InputStream inputStream) throws IOException {
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