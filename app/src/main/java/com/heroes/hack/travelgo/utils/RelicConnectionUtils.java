package com.heroes.hack.travelgo.utils;

import android.text.TextUtils;
import android.util.Log;

import com.heroes.hack.travelgo.objects.Relic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Angelika Iskra on 20.10.2018.
 */
public final class RelicConnectionUtils {

    public static final String TAG = RelicConnectionUtils.class.getSimpleName();

    public static List<Relic> fetchRelicData(String requestUrl, String token, Double latitude, Double longitude, int objectsInMeters) {

        // Create URL object and pass user coordinates in GET method
        URL url = createUrl(requestUrl, latitude, longitude, objectsInMeters);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = QueryUtils.makeHttpRequest(token, url);

        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of relics
        List<Relic> relics = QueryUtils.extractRelicsFromJson(jsonResponse);

        return relics;
    }

    public static List<Relic> fetchUserRelicData(String requestUrl, String token) {

        URL url = QueryUtils.createUrl(requestUrl);

        String jsonResponse = null;

        if (TextUtils.isEmpty(token)) {
            return null;
        }

        try {
            jsonResponse = QueryUtils.makeHttpRequest(token, url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request when trying to fetch User's data.", e);
        }

        return QueryUtils.extractRelicsFromJson(jsonResponse);
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

}
