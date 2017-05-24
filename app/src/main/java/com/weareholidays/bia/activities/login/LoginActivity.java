package com.weareholidays.bia.activities.login;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.utils.ViewUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button loginBtn;
    private long mLastClickTime = 0;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setup();
    }

    private void setup() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login with Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);

        loginBtn = (Button) findViewById(R.id.continue_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                login();
            }
        });


        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
    }

    public void hideKeyboard(){
        ViewUtils.hideKeyboard(this);
    }

    private void login() {
        progressBar.setVisibility(View.VISIBLE);
        String user = email.getText().toString();
        String pwd = password.getText().toString();
        boolean errorFound = false;
        if (user == null || user.length() < 1) {
            email.setError(getString(R.string.required));
            errorFound = true;
        }
        if (pwd == null || pwd.length() < 1) {
            password.setError(getString(R.string.required));
            errorFound = true;
        }

        if (errorFound) {
            progressBar.setVisibility(View.INVISIBLE);
            return;
        } else {
            hideKeyboard();
            email.setError(null);
            password.setError(null);
        }

        if (ViewUtils.isNetworkAvailable(LoginActivity.this)) {
            ParseUser.logInInBackground(user.toLowerCase(), pwd, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (e == null) {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        //Adding device to user for Parse Push
                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.put("username", parseUser.getUsername());
                        installation.getCurrentInstallation().saveInBackground();

                        progressBar.setVisibility(View.INVISIBLE);
                        startActivity(intent);
                        finish();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        email.setError(getString(R.string.login_error));
                        Toast.makeText(LoginActivity.this, "Check your credentials", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this, "No working internet connection found!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
