package com.heroes.hack.travelgo.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.objects.User;
import com.heroes.hack.travelgo.utils.UserConnectionUtils;

public class UserDataAsyncTask extends AsyncTask<String, Void, User> {
    public static final String TAG = UserDataAsyncTask.class.getSimpleName();

    private User user;
    private String requestUrl;
    private String token;
    private String username;

    public UserDataAsyncTask(Context context, String token, String username) {

        requestUrl = context.getResources().getString(R.string.user_url);
        this.token = token;
        this.username = username;
        user = new User(username);
    }

    @Override
    protected User doInBackground(String... params) {

        if (requestUrl == null || username == null) {
            Log.d(TAG, "Detected null url or username during executing async task");
            return null;
        }

        if (token == null) {
            Log.d(TAG, "Incorrect token");
            return null;
        }

        String fullUrl = requestUrl + username;

        User receivedUser = UserConnectionUtils.fetchUserData(fullUrl, token);
        return receivedUser;
    }

    @Override
    protected void onPostExecute(User user) {
        super.onPostExecute(user);
        Log.d(TAG, "Received User Data: " + user.toString());
    }
}
