package com.weareholidays.bia.activities.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.weareholidays.bia.utils.BiaAppAPI;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.utils.Utils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import wahCustomViews.view.WahImageView;

//import twitter4j.auth.AccessToken;

public class SignUpActivity extends AppCompatActivity {

    TextView terms;
    TextView privacy;
    LinearLayout mSharerLayout;
    WahImageView mSharerImage;
    private Button signUpEmail;
    private Button signUpFacebook;
    private Button loginEmail;
    private ImageView background;
    private TextView forgotPassword, mSharerText, mHeadText;
    private CenterProgressDialog progressDialog;
    private boolean accessLocationAllowedAfterRequest = false;
    private int locationAndReadExtStorageRequestCode = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.Builder calligraphyConfig = new CalligraphyConfig.Builder();
        calligraphyConfig.setFontAttrId(R.attr.fontPath);
//        if(version<17)
        calligraphyConfig.setDefaultFontPath("fonts/roboto/Roboto-Regular.ttf");
        CalligraphyConfig.initDefault(calligraphyConfig.build());
        setContentView(R.layout.activity_sign_up);
        setup();
        verifyAndShowSharerDetails();
    }

    private void verifyAndShowSharerDetails() {
        SharedPreferences prefs = WAHApplication.getWAHContext().getSharedPreferences
                (ShareUtils.SHARER_PREFS, Context.MODE_PRIVATE);
        String sharerObjectId = prefs.getString(ShareUtils.SHARER_ID, "");
        if (sharerObjectId != null && !TextUtils.isEmpty(sharerObjectId)) {
            ParseCustomUser user = ShareUtils.getSharerDetails(sharerObjectId);
            if (user != null) {
                mSharerLayout.setVisibility(View.VISIBLE);
                mHeadText.setVisibility(View.GONE);

                if (user.getProfileImage() != null && user.getProfileImage().getUrl() != null) {
                    mSharerImage.setImageUrl(user.getProfileImage().getUrl());
                }
                mSharerText.setText(String.format(getResources().getString(R.string.sign_up_sharer_name), user.getName()));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (progressDialog == null) {
            progressDialog = CenterProgressDialog.show(SignUpActivity.this, null, null, true, false);
        }
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setup() {
        signUpEmail = (Button) findViewById(R.id.signup_email);
        signUpEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignUpEmailActivity.class);
                startActivity(intent);
            }
        });

        loginEmail = (Button) findViewById(R.id.login_email);
        loginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signUpFacebook = (Button) findViewById(R.id.signup_facebook);
        signUpFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ViewUtils.isNetworkAvailable(SignUpActivity.this)) {
                    progressDialog = CenterProgressDialog.show(SignUpActivity.this, null, null, true, false);
                    ParseFacebookUtils.logInWithReadPermissionsInBackground(SignUpActivity.this, FacebookUtils.getFacebookReadPermissions(), new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (progressDialog != null)
                                progressDialog.hide();
                            if (parseUser == null) {
                                Log.d("Login", "Uh oh. The user cancelled the Facebook login.");

                            } else if (parseUser.isNew()) {
                                handleSignUp();
                            } else {
                                launchHome();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignUpActivity.this, "No working internet connection found!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        terms = (TextView) findViewById(R.id.terms);
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, TermsActivity.class);
                startActivity(intent);
            }
        });

        privacy = (TextView) findViewById(R.id.privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });

        mHeadText = (TextView) findViewById(R.id.head);
        mSharerLayout = (LinearLayout) findViewById(R.id.referral_layout);
        mSharerImage = (WahImageView) findViewById(R.id.referrar_img);
        mSharerText = (TextView) findViewById(R.id.referrar_name);
    }

    private void handleSignUp() {
        progressDialog = CenterProgressDialog.show(SignUpActivity.this, null, null, true, false);
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                if (graphResponse.getError() == null) {
                    ParseCustomUser user = (ParseCustomUser) ParseUser.getCurrentUser();
                    try {
                        if (TextUtils.isEmpty(user.getEmail()) && jsonObject.has("email")) {
                            user.setEmail(jsonObject.getString("email"));
                        }

                        if (jsonObject.has("name")) {
                            user.setName(jsonObject.getString("name"));
                        }

                        if (jsonObject.has("gender")) {
                            String gender = jsonObject.getString("gender");
                            if ("male".equals(gender)) {
                                user.setGender("Male");
                            } else if ("female".equals(gender)) {
                                user.setGender("Female");
                            }
                        }

                        if (jsonObject.has("hometown")) {
                            user.setPlace(jsonObject.getJSONObject("hometown").getString("name"));
                        }

                        String sharerObjectId = SharedPrefUtils.getStringPreference(getApplicationContext(), SharedPrefUtils.Keys.SHARER_ID);

                        if (TextUtils.isEmpty(user.getSharer()) && !TextUtils.isEmpty(sharerObjectId)) {
                            user.setSharer(sharerObjectId);
                        }

                        boolean isDeleted = SharedPrefUtils.removePreferenceByKey(getApplicationContext(), SharedPrefUtils.Keys.SHARER_ID);

                        if (!isDeleted)
                            SharedPrefUtils.setStringPreference(getApplicationContext(), SharedPrefUtils.Keys.SHARER_ID, "");

                        if (sharerObjectId != null && !TextUtils.isEmpty(sharerObjectId)) {
                            ShareUtils.sendLoginNotificationToSharer(sharerObjectId);
                        }

                        user.saveEventually();
                        FacebookUtils.saveFacebookId(user);

                        if (user.getProfileImage() == null && jsonObject.has("picture")) {
                            String url = jsonObject.getJSONObject("picture").getJSONObject("data").getString("url");
                            //TODO: Async task to save this to parse.
                            new UploadProfileImageTask(user, url).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            ParseFileUtils.pinFileLocallyPrivate(user, ParseCustomUser.PROFILE_IMAGE, Uri.parse(url), "profilePicture.jpeg");
                        }

                        sendEmailNotification(user);
                    } catch (Exception e) {
                        Log.e("Sign Up", "Error saving facebook info", e);
                    }
                }
                if (progressDialog != null)
                    progressDialog.hide();
                launchHome();
            }
        });
        Bundle params = new Bundle();
        params.putString("fields", "picture.width(200).heigth(200),id,name,email,gender,hometown");
        request.setParameters(params);
        request.executeAsync();

    }

    private void sendEmailNotification(ParseCustomUser user) {
        HashMap<String, String> params = new HashMap<>();
        params.put(BiaAppAPI.POST_PARAM_ACTION, "signup");
        params.put(BiaAppAPI.POST_PARAM_USER_ID, user.getObjectId());
        params.put(BiaAppAPI.POST_PARAM_USER_EMAIL, user.getEmail());
        params.put(BiaAppAPI.POST_PARAM_USER_NAME, user.getName());

        new Utils.CallServerApi(params, BiaAppAPI.URL_BIA_API).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void launchHome() {

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("username", ParseUser.getCurrentUser().getUsername());
        installation.saveInBackground();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private class UploadProfileImageTask extends AsyncTask<Void, Void, Void> {

        private ParseCustomUser parseUser;
        private String downloadUrl;

        UploadProfileImageTask(ParseUser parseUser, String downloadUrl) {
            this.parseUser = (ParseCustomUser) parseUser;
            this.downloadUrl = downloadUrl;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String data = "";
            InputStream iStream = null;

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(downloadUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = iStream.read(buf)))
                    baos.write(buf, 0, n);

                byte[] imageBytes = baos.toByteArray();

                ParseFile parseFile = new ParseFile(imageBytes, "profile_picture.jpeg");
                parseFile.save();
                parseUser.setProfileImage(parseFile);
                parseUser.save();
            } catch (Exception e) {

            } finally {
                try {
                    iStream.close();
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

}
