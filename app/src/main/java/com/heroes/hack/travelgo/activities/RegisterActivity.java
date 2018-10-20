package com.heroes.hack.travelgo.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.heroes.hack.travelgo.R;
import com.heroes.hack.travelgo.async_tasks.UserRegisterAsyncTask;
import com.heroes.hack.travelgo.utils.EncryptionClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * A login screen that offers login via username/password.
 */
public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = RegisterActivity.class.getSimpleName();
    private static final String requestUrl = "http://51.38.134.214:8080/travelgovisit/user/register";
    View focusView;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmationView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mSignInButton;
    private Toolbar toolbar;
    private JSONObject authDataJson;
    private UserRegisterAsyncTask mAuthTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set default app bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Zarejestruj się");
        }

        // Set up the register form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordConfirmationView = (EditText) findViewById(R.id.confirm_password);

        mPasswordConfirmationView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfirmationView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        focusView = null;

        // Check for a invalid email address and if so, cancel sending request.
        if (!isUsernameValid(mPasswordConfirmationView)) cancel = true;

        // Check for invalid password and if so, cancel sending request.
        if (!arePasswordsValid(mPasswordView, mPasswordConfirmationView)) cancel = true;

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

//            showProgress(true);

            // Check internet connection
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }

            if (networkInfo != null && networkInfo.isConnected()) {
                // Create valid data
                authDataJson = createJson(username, password);

                //Register async task to send request to user
                mAuthTask = new UserRegisterAsyncTask(getApplicationContext());
                mAuthTask.execute(requestUrl, authDataJson.toString());
                try {
                    if (mAuthTask.get() == 200) {
                        Toast.makeText(this, "Zarejestrowano, zaloguj się", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(this, "Błąd rejestracji", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Log.d(TAG, "No internet connection");
            }

        }
    }

    private boolean isUsernameValid(EditText usernameView) {
        String username = usernameView.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            return false;
        }
        if (username.length() <= 4) {
            usernameView.setError(getString(R.string.error_too_short_username));
            focusView = usernameView;
            return false;
        }

        return true;
    }

    private boolean arePasswordsValid(EditText passwordView, EditText passwordConfirmationView) {
        String password = passwordView.getText().toString();
        String passwordConfirmation = passwordConfirmationView.getText().toString();

        // Check that password field is empty
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_password_required));
            focusView = passwordView;
            return false;
        }
        if (TextUtils.isEmpty(passwordConfirmation)) {
            passwordConfirmationView.setError(getString(R.string.error_password_confirmation_required));
            focusView = passwordConfirmationView;
            return false;
        }
        //Check that passwords are equal and contains more than 8 characters
        if (!(password.length() >= 8)) {
            passwordView.setError(getString(R.string.error_password_too_short));
            focusView = passwordView;
            return false;
        }
        if (!password.matches(passwordConfirmation)) {
            passwordConfirmationView.setError(getString(R.string.error_passwords_not_match));
            focusView = passwordConfirmationView;
            return false;
        }

        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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