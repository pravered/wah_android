package com.weareholidays.bia.activities.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.models.PlaceJSONParser;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.utils.BiaAppAPI;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.utils.Utils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wahCustomViews.view.WahImageView;

public class SignUpEmailActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 1;

    private Button uploadButton;
    private Uri selectedImageUri;
    private WahImageView img;
    private EditText name;
    private EditText email;
    private AutoCompleteTextView place;
    private EditText phone;
    private EditText password;
    private Button nextStep;
    private Spinner gender;
    private CenterProgressDialog progressDialog;
    private TextWatcher myWatcher;
    private long mLastClickTime = 0;

    private boolean savingProfile;

    String sharerObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_email);
        setup();
    }

    private void setup() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.signup_email);

        img = (WahImageView) findViewById(R.id.image_profile);
        name = (EditText) findViewById(R.id.profile_name);
        email = (EditText) findViewById(R.id.profile_email);
        place = (AutoCompleteTextView) findViewById(R.id.profile_location);
        myWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PlacesTask placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        };
        place.addTextChangedListener(myWatcher);
        phone = (EditText) findViewById(R.id.profile_phone);
        password = (EditText) findViewById(R.id.profile_password);
        nextStep = (Button) findViewById(R.id.continue_btn);
        gender = (Spinner) findViewById(R.id.gender);

        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());

        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To open up a gallery browser
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (ViewUtils.isNetworkAvailable(SignUpEmailActivity.this)) {
                    boolean isValid = validate();
                    if (isValid) {
                        hideKeyboard();
                        ParseCustomUser user = new ParseCustomUser();
                        user.setUsername(email.getText().toString().toLowerCase());
                        user.setPassword(password.getText().toString());
                        user.setEmail(email.getText().toString().toLowerCase());
                        user.setPhone(phone.getText().toString());
                        user.setPlace(place.getText().toString());
                        user.setName(name.getText().toString());
                        user.setGender(gender.getSelectedItem().toString());

                        final String sharerObjectId = SharedPrefUtils.getStringPreference(getApplicationContext(), SharedPrefUtils.Keys.SHARER_ID);

                        if(TextUtils.isEmpty(user.getSharer()) && !TextUtils.isEmpty(sharerObjectId)){
                            user.setSharer(sharerObjectId);
                        }

                        boolean isDeleted = SharedPrefUtils.removePreferenceByKey(getApplicationContext(), SharedPrefUtils.Keys.SHARER_ID);

                        if(!isDeleted)
                            SharedPrefUtils.setStringPreference(getApplicationContext(), SharedPrefUtils.Keys.SHARER_ID,"");

                        progressDialog = CenterProgressDialog.show(SignUpEmailActivity.this, "", "Loading...");

                        savingProfile = true;

                        user.signUpInBackground(new SignUpCallback() {
                            public void done(ParseException e) {
                                if (e == null) {

                                    if (sharerObjectId != null && !TextUtils.isEmpty(sharerObjectId)) {
                                        ShareUtils.sendLoginNotificationToSharer(sharerObjectId);
                                    }
                                    sendEmailNotification(ParseUser.getCurrentUser());

                                    if (selectedImageUri != null) {
                                        final ParseFile file = new ParseFile("picture_profile.jpeg", ParseFileUtils.convertImageToBytes(selectedImageUri, SignUpEmailActivity.this));
                                        file.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException ex) {
                                                dismissDialog();
                                                if (ex == null) {
                                                    ParseUser.getCurrentUser().put(ParseCustomUser.PROFILE_IMAGE, file);
                                                    ParseUser.getCurrentUser().saveEventually();
                                                }
                                                launchHome();
                                                savingProfile = false;
                                            }
                                        });
                                    } else {
                                        savingProfile = false;
                                        launchHome();
                                    }
                                } else {
                                    savingProfile = false;
                                    dismissDialog();
                                    Toast.makeText(SignUpEmailActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                } else {
                    Toast.makeText(SignUpEmailActivity.this, "No working internet connection found!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendEmailNotification(ParseUser user) {
        HashMap<String,String> params = new HashMap<>();
        params.put(BiaAppAPI.POST_PARAM_ACTION, "signup");
        params.put(BiaAppAPI.POST_PARAM_USER_ID, user.getObjectId());
        params.put(BiaAppAPI.POST_PARAM_USER_EMAIL, user.getEmail());
        params.put(BiaAppAPI.POST_PARAM_USER_NAME, name.getText().toString());

        new Utils.CallServerApi(params, BiaAppAPI.URL_BIA_API).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private ParseCustomUser getSharerDetails(String objectId) {
        ParseCustomUser user = null;
        ParseQuery<ParseCustomUser> query = ParseQuery.getQuery(ParseCustomUser.class);
        try {
            List<ParseCustomUser> existingUsers = query.whereEqualTo("objectId", objectId).find();
            if (existingUsers != null && existingUsers.size() != 0) {
                user = existingUsers.get(0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return user;
    }

    public void hideKeyboard() {
        ViewUtils.hideKeyboard(this);
    }

    private byte[] readInFile(String path) throws IOException {
        // TODO Auto-generated method stub
        byte[] data = null;
        File file = new File(path);
        InputStream input_stream = new BufferedInputStream(new FileInputStream(
                file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();

    }

    private boolean validate() {
        final String userName = name.getText().toString();
        final String userEmail = email.getText().toString();
        final String userPlace = place.getText().toString();
        final String userPhone = phone.getText().toString();
        final String pass = password.getText().toString();
        boolean isValid = true;

        if (userName == null || userName.trim().length() == 0) {
            name.setError("Required");
            isValid = false;
        }
        if (userPlace == null || userPlace.trim().length() == 0) {
            place.setError("Required");
            isValid = false;
        }
        if (userEmail == null || userEmail.trim().length() == 0) {
            email.setError("Required");
            isValid = false;
        } else if (!isValidEmail(userEmail)) {
            email.setError("Invalid Email");
            isValid = false;
        } else if (isRegisteredEmail(userEmail)) {
            email.setError("Email already registered");
            isValid = false;
        }
        if (userPhone == null || userPhone.trim().length() == 0) {
            phone.setError("Required");
            isValid = false;
        } else if (!isValidPhone(userPhone)) {
            phone.setError("Invalid Phone");
            isValid = false;
        }
        if (pass == null || pass.trim().length() == 0) {
            Toast.makeText(SignUpEmailActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                progressDialog = CenterProgressDialog.show(this, "", "Loading..");
                selectedImageUri = data.getData();
                selectedImageUri = ParseFileUtils.saveToPrivateLocation(selectedImageUri);
              /*  Glide.with(this).load(selectedImageUri).into(img);*/
                img.setImageUrl(selectedImageUri);
                dismissDialog();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up_email, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // validating email id
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isRegisteredEmail(String email) {
        boolean result = false;
        ParseQuery<ParseCustomUser> query = ParseQuery.getQuery(ParseCustomUser.class);
        try {
            List<ParseCustomUser> existingUsers = query.whereEqualTo("email", email).find();
            if (existingUsers != null && existingUsers.size() != 0) {
                result = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isValidPhone(String phone) {
        String PHONE_PATTERN = "[1-9][0-9]{9}";

        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog == null && savingProfile) {
            progressDialog = CenterProgressDialog.show(SignUpEmailActivity.this, "", "Loading...");
        }
    }

    private void dismissDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e) {

        }
    }

    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=" + WAHApplication.GOOGLE_KEY;

            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            // place type to be searched
            String types = "types=(cities)";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + types + "&" + sensor + "&" + key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

            try {
                // Fetching the data from web service in background
                data = downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            ParserTask parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            //Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            try {
                place.setAdapter(adapter);
            } catch (Exception e) {

            }
        }
    }
}
