package com.weareholidays.bia.activities.journal;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.models.PlaceJSONParser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.actions.CheckInActivity;
import com.weareholidays.bia.activities.journal.photo.SetPhotoLocationActivity;
import com.weareholidays.bia.activities.journal.trip.TripFragment;
import com.weareholidays.bia.asyncTasks.CheckInAsync;
import com.weareholidays.bia.parse.models.CustomLocation;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.OfflineUtils;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.weareholidays.bia.activities.journal.actions.CheckInActivity.calculationByDistance;
import static com.weareholidays.bia.activities.journal.actions.CheckInActivity.makeCall;

public class AddTripNoteActivity extends AppCompatActivity implements AsyncResponse{

    public static final int NOTE_TYPE = 2;
    public Location location;
    public Geocoder gCoder;
    public EditText currentLocation;
    private CenterProgressDialog progressDialog;
    public EditText noteText;
    public Button saveButton;
    public String address;
    public String city;
    public String addressText;
    private CheckInAsync locationAsync;
    //public AutoCompleteTextView atvPlaces;
    private EditText atvPlaces;
    private TextWatcher myWatcher;
    private boolean isTyped = false;
    public LinearLayout list;
    public String reference;
    private boolean isSelected;
    private AddTripNoteActivity a;
    private Location selectedLocation;
    private PlacesTask placesTask;
    public List<CustomLocation> venuesList;
    private TripOperations tripOperations;
    public ParserTask parserTask;
    private long mLastClickTime = 0;
//    private TextWatcher noteWatcher;
//    private String wordString;

    public static final int SELECT_NOTE_LOCATION = 2073;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip_note);
        tripOperations = TripUtils.getInstance().getCurrentTripOperations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String tripName = tripOperations.getTrip().getName();
        if(tripName.length()>21)
            tripName = tripName.substring(0,20)+"....";
        getSupportActionBar().setTitle(tripName);
        getSupportActionBar().setSubtitle("ADDING NOTE");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CheckInAsync.delegate = this;
        noteText = (EditText)findViewById(R.id.noteText);
        atvPlaces = (EditText) findViewById(R.id.atv_places);
        myWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isTyped = true;
                if(isNetworkAvailable()) {
                    placesTask = new PlacesTask();
                    placesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s.toString());
                }
                else{
                    startSearch(s.toString());
                    }
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
        //atvPlaces.addTextChangedListener(myWatcher);
        atvPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AddTripNoteActivity.this, SetPhotoLocationActivity.class);
                i.putExtra("NOTE", true);
                startActivityForResult(i, SELECT_NOTE_LOCATION);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                hideKeyboard();
                if (!noteText.getText().toString().equals(""))
                    saveNote();
                else
                    Toast.makeText(getBaseContext(), "Cannot add empty note", Toast.LENGTH_SHORT).show();
            }
        });
        locationAsync = new CheckInAsync(this);
        locationAsync.execute();
        list = (LinearLayout) findViewById(R.id.listView_items);
        if(!isNetworkAvailable()){
            getOfflineData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SELECT_NOTE_LOCATION) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    String photo_location = data.getStringExtra("photo_location");
                    atvPlaces.setText(photo_location);
                } catch (Exception e) {
                    DebugUtils.logException(e);
                    Toast.makeText(this, "Couldn't load location", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void startSearch(String searchString){
        List<CustomLocation> myList = new ArrayList<>();
        for(CustomLocation venue: venuesList){
            if(venue.getName().toLowerCase().contains(searchString.toLowerCase())){
                myList.add(venue);
            }
        }
        inflateVenuesInLayout(myList);
    }
    public void getOfflineData(){
        venuesList = new ArrayList<>();
        try {
            venuesList = new OfflineUtils().getofflineLocations();
            if(location != null)
                Collections.sort(venuesList, new CustomComparator());
            inflateVenuesInLayout(venuesList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class CustomComparator implements Comparator<CustomLocation> {
        @Override
        public int compare(CustomLocation o1, CustomLocation o2) {
            double o1Dist = calculationByDistance(o1.getGeoPoint().getLatitude(), o1.getGeoPoint().getLongitude(), location.getLatitude(), location.getLongitude());
            double o2Dist = calculationByDistance(o2.getGeoPoint().getLatitude(), o2.getGeoPoint().getLongitude(), location.getLatitude(), location.getLongitude());
            return (o1Dist < o2Dist) ? -1 : (o1Dist > o2Dist) ? 1 : 0;
        }
    }

    public void hideKeyboard(){
        ViewUtils.hideKeyboard(this);
    }

    private class getPlace extends AsyncTask<View, Void, String> {

        String temp;
        AddTripNoteActivity a;

        public getPlace(AddTripNoteActivity a) {
            this.a = a;
        }

        @Override
        protected String doInBackground(View... urls) {

            // make Call to the url
            try {
                temp = makeCall("https://maps.googleapis.com/maps/api/place/details/json?location="
                        + "&sensor="
                        + URLEncoder.encode("true", "UTF-8")
                        + "&placeid="
                        + URLEncoder.encode(reference, "UTF-8")
                        + "&key="
                        + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //print the call in the console
            return "";
        }

        @Override
        protected void onPreExecute() {
            // we can start a progress bar here
        }

        @Override
        protected void onPostExecute(String result) {
            String tempString = getPlaceCordinates(temp);
        }
    }

    private String getPlaceCordinates(String response) {
        String returnString = "";
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("result")) {

                JSONObject jsonobject = jsonObject.getJSONObject("result");
                if (jsonobject.has("geometry")) {
                    JSONObject tempLocation = new JSONObject(jsonobject.getJSONObject("geometry").optString("location"));
                    selectedLocation = new Location("");
                    selectedLocation.setLatitude(tempLocation.getDouble("lat"));
                    selectedLocation.setLongitude(tempLocation.getDouble("lng"));
                    location = selectedLocation;
                }
                if (jsonobject.has("name") && jsonobject.has("formatted_address")) {
                    returnString = jsonobject.getString("name");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnString;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_trip_note, menu);
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

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNote(){
        progressDialog = CenterProgressDialog.show(this, null, null, true, false);
        new NoteSaveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void processFinish(ReturnLocation returnLocation) {
        if(!isTyped) {
            this.location = new Location("");
            this.location.setLatitude(returnLocation.getLocation().getLatitude());
            this.location.setLongitude(returnLocation.getLocation().getLongitude());
            //new googleplaces(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            gCoder = new Geocoder(this);
            ArrayList<Address> addresses = null;
            try {
                addresses = (ArrayList<Address>) gCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                addressText = address + ',' + city;
                atvPlaces.removeTextChangedListener(myWatcher);
                atvPlaces.setText(returnLocation.getAddressString());
                atvPlaces.addTextChangedListener(myWatcher);
            }
        }
    }

    @Override
    public int getType() {
        return NOTE_TYPE;
    }

    private class NoteSaveTask extends AsyncTask<Void,Void,Void>{

        private boolean saved = false;
        private Timeline timeline;
        private String content;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            content = noteText.getText().toString();
            addressText = atvPlaces.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ParseGeoPoint parseGeoPoint= new ParseGeoPoint();
                if (location != null) {
                    parseGeoPoint.setLatitude(location.getLatitude());
                    parseGeoPoint.setLongitude(location.getLongitude());
                }
                if(tripOperations instanceof TripLocalOperations)
                    timeline = ((TripLocalOperations)tripOperations).addNote(content, addressText, parseGeoPoint);
                saved = true;
            } catch (ParseException e) {
                Log.e(CheckInActivity.class.getName(), "Error saving photos", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            dismissDialog();
            if(saved)
                next(timeline);
            else{
                Toast.makeText(AddTripNoteActivity.this, getString(R.string.toast_add_note_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void next(Timeline timeline){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.SHOW_TAB,HomeActivity.JOURNAL_TAB);
        intent.putExtra(TripFragment.SHOW_JOURNAL_DAY,timeline.getDayOrder()+1);
        intent.putExtra(TripFragment.TIMELINE_SCROLL_POSITION,timeline.getDisplayOrder());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPause(){
        super.onPause();
        dismissDialog();
    }

    public void dismissDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        progressDialog = null;
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
            String types = "types=establishment";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + sensor + "&" + key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

            try {
                // Fetching the data from web service in background
                data = CheckInActivity.downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result);
        }
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

            String[] from = new String[]{"description", "place_id"};

            // Creating a SimpleAdapter for the AutoCompleteTextView
            if(venuesList == null)
                venuesList = new ArrayList<CustomLocation>();
            venuesList.clear();
            List<String> categories = new ArrayList<>();
            categories.add("mosque");
            if(result != null) {
                for (int i = 0; i < result.size(); i++) {
                    final Map dataSet = result.get(i);
                    CustomLocation myCustomLocation = new CustomLocation();
                    myCustomLocation.setName(dataSet.get(from[0]).toString());
                    myCustomLocation.setReference(dataSet.get(from[1]).toString());
                    myCustomLocation.setCategory(categories);
                    myCustomLocation.setDistance(-1);
                    venuesList.add(myCustomLocation);
                }
                inflateVenuesInLayout(venuesList);
            }

        }
    }

    private String[] iconsNames = {"mosque", "museum"};

    public String checkCategory(List<String> categories){
        for(String category : categories){
            for(String iconName: iconsNames){
                if (category.equals(iconName))
                    return category;
            }
        }
        return "None";
    }

    private void inflateVenuesInLayout(List<CustomLocation> venuesList){
        int index = 0;
        LayoutInflater inflater = getLayoutInflater();
        list.removeAllViews();
        for(final CustomLocation venue: venuesList){
            View vi = inflater.inflate(R.layout.list_row, list, false);
            TextView text_name=(TextView)vi.findViewById(R.id.placeName);
            TextView text_distance=(TextView)vi.findViewById(R.id.placeDistance);
            ImageView image=(ImageView)vi.findViewById(R.id.placeImage);
            text_name.setText(venue.getName());
            if (venue.getDistance() < 0) {
                text_distance.setVisibility(View.GONE);
            }
            else
                text_distance.setText(String.valueOf(venue.getDistance()) + " miles away");

            List<String> categories = venue.getCategory();
            String place_type = checkCategory(categories);

            if (place_type.equals("mosque")) {
                image.setImageResource(R.drawable.timeline_landmark);

            } else if (place_type.equals("museum")) {
                image.setImageResource(R.drawable.museum);

            } else {
                image.setImageResource(R.drawable.timeline_restaurant);
            }
            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateSelectedVenueinView(venue);
                }
            });

            list.setPadding(0,10,0,0);
            list.addView(vi);
            if(++index < venuesList.size()){
                View dividerView = inflater.inflate(R.layout.padding_divider,list,false);
                list.addView(dividerView);
            }
        }
    }

    private void updateSelectedVenueinView(CustomLocation venue){
        CustomLocation customLocation = venue;
        reference = customLocation.getReference();
        atvPlaces.removeTextChangedListener(myWatcher);
        atvPlaces.setText(customLocation.getName());
        isSelected = true;
        atvPlaces.addTextChangedListener(myWatcher);
        new getPlace(a).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
