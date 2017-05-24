package com.weareholidays.bia.activities.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.login.SignUpActivity;
import com.weareholidays.bia.models.PlaceJSONParser;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
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

import de.hdodenhof.circleimageview.CircleImageView;
import wahCustomViews.view.WahImageView;


public class EditProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int SELECT_PICTURE = 1;
    private Uri selectedImageUri;
    private Uri coverImageUri;
    private WahImageView img;
    private WahImageView coverImg;
    private View coverImgMask;
    private ImageView editCoverImg;
    private ImageView editProfilePic;
    private Spinner spinner1;
    private String[] gender;
    ParseCustomUser currentUser;
    private Button submitButton;
    private Button changePhoto;
    private EditText name;
    private EditText email;
    private AutoCompleteTextView place;
    private EditText phone;
    private TextWatcher myWatcher;
    private CenterProgressDialog progressDialog;

    private static final int SELECT_COVER_PICTURE = 121;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");
        img = (WahImageView) findViewById(R.id.image_profile);
        coverImg = (WahImageView) findViewById(R.id.user_cover);
        coverImgMask = findViewById(R.id.user_cover_mask);

        editCoverImg = (ImageView) findViewById(R.id.edit_cover_photo);
        editProfilePic = (ImageView) findViewById(R.id.change_profile_pic);
        name = (EditText) findViewById(R.id.profile_name);
        email = (EditText) findViewById(R.id.profile_email);
        place = (AutoCompleteTextView) findViewById(R.id.profile_place);
        editCoverImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_COVER_PICTURE);
            }
        });
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
        Resources res = getResources();
        gender = res.getStringArray(R.array.gender_array);
        spinner1 = (Spinner) findViewById(R.id.gender);

        ArrayAdapter<String> adapter_state = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gender);
        adapter_state.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter_state);
        spinner1.setOnItemSelectedListener(this);
        currentUser = (ParseCustomUser)ParseUser.getCurrentUser();
        if (currentUser != null) {
            setProfileData();
        } else {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
        setCoverImage();
        submitButton = (Button)findViewById(R.id.edit_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = validate();

                if (isValid) {
                    hideKeyboard();
                    if (ViewUtils.isNetworkAvailable(EditProfileActivity.this)) {
                        progressDialog = CenterProgressDialog.show(EditProfileActivity.this, "", "      Saving...");

                        currentUser.put(ParseCustomUser.PHONE, phone.getText().toString());
                        currentUser.put(ParseCustomUser.PLACE, place.getText().toString());
                        currentUser.put(ParseCustomUser.GENDER, spinner1.getSelectedItem().toString());
                        currentUser.put(ParseCustomUser.NAME, name.getText().toString());
                        currentUser.saveInBackground(new SaveCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    dismissDialog();
                                    launchHome();

                                } else {
                                    dismissDialog();
                                    Toast.makeText(EditProfileActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(EditProfileActivity.this, "No working internet connection found!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        /*
        changePhoto = (Button)findViewById(R.id.button_photo);*/
        editProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_COVER_PICTURE) {
            if (data.getData() != null && ViewUtils.isNetworkAvailable(this)) {
                coverImageUri = data.getData();
                coverImageUri = ParseFileUtils.saveToPrivateLocation(coverImageUri);
                progressDialog = CenterProgressDialog.show(this, null, null, true, false);
                final ParseFile file = new ParseFile("picture_cover.jpeg", ParseFileUtils.convertImageToBytes(coverImageUri, this));
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException ex) {
                        if (ex == null) {
                            currentUser.setFeatureImage(file);
                            currentUser.saveEventually();
                            updateCoverImage();
                        }
                        else{
                            Toast.makeText(EditProfileActivity.this, "Error Uploading Cover photo", Toast.LENGTH_SHORT).show();
                        }
                        dismissDialog();
                    }
                });

            } else {
                Toast.makeText(this, "No working internet connection found!!", Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_PICTURE) {
            if (data.getData() != null && ViewUtils.isNetworkAvailable(this)) {
                selectedImageUri = data.getData();
                selectedImageUri = ParseFileUtils.saveToPrivateLocation(selectedImageUri);
                progressDialog = CenterProgressDialog.show(this, null, null, true, false);
                final ParseFile file = new ParseFile("picture_profile.jpeg", ParseFileUtils.convertImageToBytes(selectedImageUri, this));
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException ex) {
                        if (ex == null) {
                            currentUser.setProfileImage(file);
                            currentUser.saveEventually();
                            updateProfileImage();
                        }
                        else{
                            Toast.makeText(EditProfileActivity.this, "Error Uploading profile photo", Toast.LENGTH_SHORT).show();
                        }
                        dismissDialog();
                    }

                });

            } else {
                Toast.makeText(this, "No working internet connection found!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setCoverImage() {
        if (currentUser.getFeatureImage() != null) {
           /* Glide.with(this)
                    .load(currentUser.getFeatureImage().getUrl())
                    .centerCrop()
                    .crossFade()
                    .into(coverImg);*/
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,coverImgMask.getLayoutParams().height);
            params.setMargins(0,0,0,0);
            coverImg.setLayoutParams(params);
            coverImg.setImageUrl(currentUser.getFeatureImage().getUrl());
        }
    }

    public void updateCoverImage(){
    /*    Glide.with(this)
                .load(coverImageUri)
                .centerCrop()
                .crossFade()
                .into(coverImg);*/
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,coverImgMask.getLayoutParams().height);
        params.setMargins(0,0,0,0);
        coverImg.setLayoutParams(params);
        coverImg.setImageUrl(coverImageUri);
    }

    public void updateProfileImage(){
        if(selectedImageUri != null) {
            /*Glide.with(this).load(selectedImageUri).into(img);*/
            img.setImageUrl(selectedImageUri);
        }
    }

    public void hideKeyboard(){
        ViewUtils.hideKeyboard(this);
    }

    private void launchHome() {
        Toast.makeText(this,"Profile Updated",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.PROFILE_TAB);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void setProfileData(){
        WahImageView civ = (WahImageView) findViewById(R.id.image_profile);

        name.setText(currentUser.getName());
        place.setText(currentUser.getPlace());
        phone.setText(currentUser.getPhone());
        email.setText(currentUser.getEmail());

        String genderVal = currentUser.getGender();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gender);
        if (genderVal != null) {
            int spinnerPostion = adapter.getPosition(genderVal);
            spinner1.setSelection(spinnerPostion);
            spinnerPostion = 0;
        }

        try {
            FileLocal fileLocal = ParseFileUtils.getLocalFileFromPin(currentUser);
            if(fileLocal != null){
                /*Glide.with(this)
                        .load(fileLocal.getLocalUri())
                        .asBitmap()
                        .placeholder(R.drawable.user_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new BitmapImageViewTarget(civ) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                Log.i("Profile", "Profile Picture Loaded");
                                super.setResource(resource);
                            }
                        });*/
                civ.setImageUrl(fileLocal.getLocalUri());
              /*  Picasso.with(this)
                        .load(fileLocal.getLocalUri())
                        .placeholder(R.drawable.user_placeholder)
                        .into(civ);*/
            }
            else{
                ParseFile userProfileImg = currentUser.getProfileImage();
                if(userProfileImg != null){
                   /*Glide.with(this)
                            .load(userProfileImg.getUrl())
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .placeholder(R.drawable.user_placeholder)
                            .into(new BitmapImageViewTarget(civ) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    Log.i("Profile", "Profile Picture Loaded");
                                    super.setResource(resource);
                                }
                            });*/
                    civ.setImageUrl(userProfileImg.getUrl());
                    /*Picasso.with(this)
                            .load(userProfileImg.getUrl())
                            .placeholder(R.drawable.user_placeholder)
                            .into(civ);*/
                }
            }
        } catch (Exception e) {
            Log.e("EditProfile","Error showing picture",e);
        }
    }

    private boolean validate(){
        final String userName = name.getText().toString();
        final String userPlace = place.getText().toString();
        final String userPhone = phone.getText().toString();
        boolean isValid = true;

        if(userName == null || userName.trim().length()==0){
            name.setError("Required");
            isValid = false;
        }
        if(userPlace == null || userPlace.trim().length()==0){
            place.setError("Required");
            isValid = false;
        }
//        if(userEmail == null || userEmail.trim().length()==0){
//            email.setError("Required");
//            isValid = false;
//        }else if(!isValidEmail(userEmail)){
//            email.setError("Invalid Email");
//            isValid = false;
//        }
        if(userPhone == null || userPhone.trim().length()==0){
            phone.setError("Required");
            isValid = false;
        }else if(!isValidPhone(userPhone)){
            phone.setError("Invalid Phone");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidPhone(String phone) {
        String PHONE_PATTERN = "[1-9][0-9]{9}";

        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        spinner1.setSelection(position);
        String selState = (String) spinner1.getSelectedItem();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                progressDialog = CenterProgressDialog.show(this,"","Loading..");
                selectedImageUri = data.getData();
                selectedImageUri = ParseFileUtils.saveToPrivateLocation(selectedImageUri);
                Glide.with(this).load(selectedImageUri).into(img);
                dismissDialog();
            }
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
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

        if(id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
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
    public void onPause(){
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void dismissDialog(){
        try{
            if(progressDialog != null)
                progressDialog.dismiss();
            progressDialog = null;
        } catch (Exception e){
            DebugUtils.logException(e);
        }
    }

    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key="+ WAHApplication.GOOGLE_KEY;

            String input="";

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
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

            try{
                // Fetching the data from web service in background
                data = downloadUrl(url);
            }catch(Exception e){
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
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            place.setAdapter(adapter);


        }
    }
}

