package com.heroes.hack.travelgo.utils;

import android.text.TextUtils;
import android.util.Log;

import com.heroes.hack.travelgo.objects.Relic;
import com.heroes.hack.travelgo.objects.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by Angelika Iskra on 20.10.2018.
 */
public final class UserConnectionUtils {

    public static final String TAG = UserConnectionUtils.class.getSimpleName();

    public static User fetchUserData(String requestUrl, String token) {
        /*This method prepares User's data, such as
         *  User's level
         *  Points that user has
         *  Maximum points that user can get on that level
         *  List of user's relics
         * */
        // Create URL object
        Log.d(TAG, "URL: " + requestUrl);
        Log.d(TAG, "Token is: " + token);
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

        return extractUserDataFromJson(jsonResponse);
    }

    private static User extractUserDataFromJson(String stringUserData) {
        if (TextUtils.isEmpty(stringUserData)) {
            return null;
        }

        try {
            JSONObject userDataJSON = new JSONObject(stringUserData);
            String username = userDataJSON.getString("username");
            int level = userDataJSON.getInt("level");
            int experience = userDataJSON.getInt("currentExperience");
            int leftExperience = userDataJSON.getInt("leftExperience");

            JSONArray relicsJSONArray = userDataJSON.getJSONArray("relics");
            List<Relic> userRelics = QueryUtils.extractRelicsFromJson(relicsJSONArray.toString());

            return new User(username, level, experience, leftExperience, userRelics);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Couldn't get userDataJSON: ", e);
        }

        return null;
    }
}
