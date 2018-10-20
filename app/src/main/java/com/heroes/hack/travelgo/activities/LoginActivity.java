package com.heroes.hack.travelgo.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.async_tasks.UserLoginAsyncTask;
import com.heroes.hack.travelgo.utils.EncryptionClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity {

    public static final String TAG = RegisterActivity.class.getSimpleName();

    private String requestUrl;

    // UI references.
    private Toolbar mToolbar;
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private Button mLoginButton;
    private View mFocusView;
    private TextView mRegistrationLink;

    private JSONObject authLoginDataJson;

    private UserLoginAsyncTask mAuthTask;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestUrl = getString(R.string.request_login_url);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Set default app bar
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Zaloguj się!");
        }


        // Set up login form
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.login_username);
        mPasswordView = (EditText) findViewById(R.id.login_password);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mRegistrationLink = (TextView) findViewById(R.id.register_link);
        mRegistrationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private void attemptLogin() {
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        mFocusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError("Musisz podać nazwę użytkownika!");
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Musisz podać hasło!");
            cancel = true;
        }

        if (cancel) {
            mFocusView.requestFocus();
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }

            if (networkInfo != null && networkInfo.isConnected()) {

                authLoginDataJson = createJson(username, password);

                mAuthTask = new UserLoginAsyncTask(getApplicationContext());
                mAuthTask.execute(requestUrl, authLoginDataJson.toString());
                try {
                    if (!TextUtils.isEmpty(mAuthTask.get())) {

                        editor = preferences.edit();
                        editor.putString("username", username);
                        editor.putString("token", mAuthTask.get());
                        editor.apply();

                        Toast.makeText(this, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this, "Nieudana próba logowania. Spróbuj ponownie", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private JSONObject createJson(String username, String password) {

        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("password", EncryptionClass.SHA1(password));

        } catch (JSONException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return json;
    }

}
