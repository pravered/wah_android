package com.weareholidays.bia.activities.journal.actions;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.adapters.LazyAdapter;
import com.weareholidays.bia.models.PlaceJSONParser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.AsyncResponse;
import com.weareholidays.bia.activities.journal.ReturnLocation;
import com.weareholidays.bia.activities.journal.trip.TripFragment;
import com.weareholidays.bia.asyncTasks.CheckInAsync;
import com.weareholidays.bia.parse.models.CheckIn;
import com.weareholidays.bia.parse.models.CustomLocation;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.OfflineUtils;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.GPSTracker;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public class CheckInActivity extends AppCompatActivity implements AsyncResponse, LocationListener, GPSTracker.GPSListener {

    public static final int CHECK_IN_TYPE = 1;
    public GoogleMap googleMap;
    public List<CustomLocation> venuesList;
    public Location location;
    public ListView list;
    public LazyAdapter adapter;
    public AutoCompleteTextView atvPlaces;
    public PlacesTask placesTask;
    public ParserTask parserTask;
    public String reference;
    public Location selectedLocation;
    public Button submitButton;
    private TextWatcher myWatcher;
    private CenterProgressDialog progressDialog;
    private CheckInAsync locationAsync;
    private TripOperations tripOperations;
    private boolean isSelected;
    private String photoReference;
    private boolean isTyped = false;
    private boolean isSave = false;
    private CheckInActivity a;
    private TextView nearby;
    private GoogleMap map;
    private long cLastClickTime = 0;
    private boolean enableCheckIn = true;
    GPSTracker gpsTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        nearby = (TextView) findViewById(R.id.nearby);
        tripOperations = TripUtils.getInstance().getCurrentTripOperations();
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap)).getMap();
        setSupportActionBar(toolbar);
        String tripName = tripOperations.getTrip().getName();
        if (tripName.length() > 21)
            tripName = tripName.substring(0, 20) + "....";
        getSupportActionBar().setTitle(tripName);
        getSupportActionBar().setSubtitle("CHECK IN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CheckInAsync.delegate = this;
        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                CheckInActivity.this.googleMap = googleMap;
                googleMap.setMyLocationEnabled(true);
                setup();
            }

        });

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                setup();
                return false;
            }
        });

        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        myWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isSelected = false;
                isTyped = true;
                if (isNetworkAvailable()) {
                    placesTask = new PlacesTask();
                    placesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s.toString());
                } else {
                    if (adapter != null) {
                        adapter.getFilter().filter(s);
                    }
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
        atvPlaces.addTextChangedListener(myWatcher);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        submitButton = (Button) findViewById(R.id.submit_btn);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1 second
                if (SystemClock.elapsedRealtime() - cLastClickTime < 1000) {
                    return;
                }
                if (!isNetworkAvailable() && isSelected){
                    if(gpsTracker != null){
                        gpsTracker.stopUsingGPS();
                    }
                    saveCheckIn();
                    return;
                }

                cLastClickTime = SystemClock.elapsedRealtime();
                isSave = true;
                if (isSelected) {
                    getPlaceMethod();
                } else {
                    Toast.makeText(getBaseContext(), "Select a location from the list", Toast.LENGTH_SHORT).show();
                }
//                if(enableCheckIn){
//                    isSave = true;
//                    enableCheckIn = false;
//                    if (isSelected) {
//                        getPlaceMethod();
//                    } else{
//                        Toast.makeText(getBaseContext(), "Select a location from the list", Toast.LENGTH_SHORT).show();
//                        enableCheckIn = true;
//                    }
            }
//            }
        });

        list = (ListView) findViewById(R.id.listView_items);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                CustomLocation customLocation = ((CustomLocation) list.getAdapter().getItem(position));
                hideKeyboard();
                reference = customLocation.getReference();
                photoReference = customLocation.getPhotoReference();
                atvPlaces.removeTextChangedListener(myWatcher);
                atvPlaces.setText(customLocation.getName());
                isSelected = true;
                if (customLocation.getGeoPoint() != null) {
                    Location myLocation = new Location("");
                    myLocation.setLatitude(customLocation.getGeoPoint().getLatitude());
                    myLocation.setLongitude(customLocation.getGeoPoint().getLongitude());
                    location = myLocation;
                }
                atvPlaces.addTextChangedListener(myWatcher);
                if (isNetworkAvailable())
                    new getPlace(a).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        if (!isNetworkAvailable()) {
            getOfflineData();
            gpsTracker = new GPSTracker(this,this);
        }
    }

    public void getOfflineData() {
        venuesList = new ArrayList<>();
        try {
            venuesList = new OfflineUtils().getofflineLocations();
            if (location != null)
                Collections.sort(venuesList, new CustomComparator());
            setMyAdapter();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gpsTracker != null){
            gpsTracker.stopUsingGPS();
        }
    }

    @Override
    public void onGPSLocationChanged(Location myLocation) {
        this.location = myLocation;

        if (myWatcher != null)
            atvPlaces.removeTextChangedListener(myWatcher);
        atvPlaces.setText(myLocation.getLatitude()+"  "+myLocation.getLongitude());
        if (myWatcher != null)
            atvPlaces.addTextChangedListener(myWatcher);
        isSelected = true;
    }

    public class CustomComparator implements Comparator<CustomLocation> {
        @Override
        public int compare(CustomLocation o1, CustomLocation o2) {
            double o1Dist = calculationByDistance(o1.getGeoPoint().getLatitude(), o1.getGeoPoint().getLongitude(), location.getLatitude(), location.getLongitude());
            double o2Dist = calculationByDistance(o2.getGeoPoint().getLatitude(), o2.getGeoPoint().getLongitude(), location.getLatitude(), location.getLongitude());
            return (o1Dist < o2Dist) ? -1 : (o1Dist > o2Dist) ? 1 : 0;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void hideKeyboard() {
        ViewUtils.hideKeyboard(this);
    }

    public void getPlaceMethod() {
        new getPlace(this).execute();
    }

    private void saveCheckIn() {
        progressDialog = CenterProgressDialog.show(this, null, null, true, false);
        new CheckInSaveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void processFinish(ReturnLocation returnLocation) {
        this.location = new Location("");
        this.location.setLatitude(returnLocation.getLocation().getLatitude());
        this.location.setLongitude(returnLocation.getLocation().getLongitude());
        if (!isTyped) {
            new googleplaces(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            onLocationChanged(returnLocation.getLocation());
            if (!returnLocation.isNetworkPresent()) {
                nearby.setText("No Places Found");
            } else {
                if (myWatcher != null)
                    atvPlaces.removeTextChangedListener(myWatcher);
                atvPlaces.setText(returnLocation.getAddressString());
                if (myWatcher != null)
                    atvPlaces.addTextChangedListener(myWatcher);
                reference = returnLocation.getReference();
                isSelected = true;
            }
        }
    }

    @Override
    public int getType() {
        return CHECK_IN_TYPE;
    }

    private class CheckInSaveTask extends AsyncTask<Void, Void, Void> {

        private boolean saved = false;
        private Timeline timeline;
        private String content;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            content = atvPlaces.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                parseGeoPoint.setLatitude(location.getLatitude());
                parseGeoPoint.setLongitude(location.getLongitude());
                if (tripOperations instanceof TripLocalOperations) {
                    //timeline = ((TripLocalOperations) tripOperations).addCheckIn(content, parseGeoPoint, photoReference);
                    // check if already saved (vijay: dirty hack for the time being)
                    boolean alreadySaved = false;
                    TripLocalOperations tripLocalOperations = (TripLocalOperations) tripOperations;
                    Calendar calendar = Calendar.getInstance();
                    Trip trip  = tripLocalOperations.getTrip();
                    Day currentDay = TripUtils.getDayFromTime(trip.getDays(),calendar);
                    if(currentDay == null){
                        throw TripUtils.createDayNotFoundException(calendar);
                    }
                    synchronized(CheckInActivity.this){
                        List<Timeline> checkins = tripLocalOperations.getDayTimeLines(currentDay, -1, 0, "WAH", "CheckIn");
                        for (Timeline tline : checkins) {
                            CheckIn checkin = (CheckIn) tline.getContent();
                            if (checkin.getName().equals(content)) {
                                if (Math.abs(calendar.getTime().getTime() - tline.getContentTime().getTime()) < 60000) {
                                    timeline = tline;
                                    alreadySaved = true;
                                    break;
                                }
                            }
                        }
                        if (!alreadySaved)
                            timeline = ((TripLocalOperations) tripOperations).addCheckIn(content, parseGeoPoint, photoReference);
                    }
                }
                saved = true;
            } catch (ParseException e) {
                Log.e(CheckInActivity.class.getName(), "Error saving check in", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (saved)
                next(timeline);
            else {
                Toast.makeText(CheckInActivity.this, getString(R.string.toast_add_check_in_error), Toast.LENGTH_LONG).show();
            }
            enableCheckIn = true;
        }
    }

    private void next(Timeline timeline) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.JOURNAL_TAB);
        intent.putExtra(TripFragment.SHOW_JOURNAL_DAY, timeline.getDayOrder()+1);
        intent.putExtra(TripFragment.TIMELINE_SCROLL_POSITION, timeline.getDisplayOrder());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setup() {
        locationAsync = new CheckInAsync(this);
        locationAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_add_trip_note, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.timeline_trip_location)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class googleplaces extends AsyncTask<View, Void, String> {

        String temp;

        public googleplaces(CheckInActivity act) {
            a = act;
        }

        @Override
        protected String doInBackground(View... urls) {
            // make Call to the url
            try {
                temp = makeCall("https://maps.googleapis.com/maps/api/place/search/json?location="
                        + URLEncoder.encode(String.valueOf(location.getLatitude()), "UTF-8")
                        + ","
                        + URLEncoder.encode(String.valueOf(location.getLongitude()), "UTF-8")
                        + "&rankby="
                        + URLEncoder.encode("distance", "UTF-8")
                        + "&sensor="
                        + URLEncoder.encode("true", "UTF-8")
                        + "&types="
                        + URLEncoder.encode(ViewUtils.PLACES_TYPES, "UTF-8")
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
            if (temp == null) {
                // we have an error to the call
                // we can also stop the progress bar
            } else {
                // all things went right

                // parse Google places search result
                venuesList = (ArrayList<CustomLocation>) parseGoogleParse(temp, location);
                setMyAdapter();

            }

        }
    }

    private void setMyAdapter() {
        adapter = new LazyAdapter(this, venuesList);
        list.setAdapter(adapter);
        list.setClickable(true);
    }

    private class getPlace extends AsyncTask<View, Void, String> {

        String temp;
        CheckInActivity a;

        public getPlace(CheckInActivity a) {
            this.a = a;
        }

        @Override
        protected String doInBackground(View... urls) {
            // make Call to the url
            if (reference != null) {
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
            if (temp == null || temp.isEmpty()) {
                enableCheckIn = true;
                // we have an error to the call
                // we can also stop the progress bar
            } else {
                // all things went right

                // parse Google places search result
                if (isNetworkAvailable()) {
                    String tempString = getPlaceCordinates(temp);
                    LatLng latLng = new LatLng(selectedLocation.getLatitude(), selectedLocation.getLongitude());
                    addMapMarker(latLng);
                }
                if (isSave) {
                    saveCheckIn();
                }

            }
        }


        private void addMapMarker(LatLng latLng) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.timeline_trip_location)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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
                if (jsonobject.has("photos")) {
                    JSONObject photo = new JSONObject(jsonobject.getJSONArray("photos").get(0).toString());
                    photoReference = photo.getString("photo_reference");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnString;
    }


    public static String makeCall(String urlStr) {

        // string buffers the url
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlStr);

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
            try {
                iStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }

        // trim the whitespaces
        return data.trim();
    }

    public static ArrayList<CustomLocation> parseGoogleParse(final String response, Location location) {

        ArrayList<CustomLocation> temp = new ArrayList<CustomLocation>();
        try {

            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // make an jsonObject in order to parse the response
            if (jsonObject.has("results")) {

                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    CustomLocation poi = new CustomLocation();
                    if (jsonArray.getJSONObject(i).has("name")) {
                        poi.setName(jsonArray.getJSONObject(i).optString("name"));
                        if (jsonArray.getJSONObject(i).has("types")) {
                            JSONArray typesArray = jsonArray.getJSONObject(i).getJSONArray("types");
                            ArrayList<String> savedCategories = new ArrayList<String>();
                            for (int j = 0; j < typesArray.length(); j++) {
                                savedCategories.add(typesArray.getString(j));
                            }
                            poi.setCategory(savedCategories);
                            if (jsonArray.getJSONObject(i).has("geometry")) {
                                JSONObject tempLocation = new JSONObject(jsonArray.getJSONObject(i).getJSONObject("geometry").optString("location"));
                                poi.setGeopPoint(tempLocation.getDouble("lat"), tempLocation.getDouble("lng"));
                                if (location.getLatitude() != 0.0)
                                    poi.setDistance((double) round(calculationByDistance(location.getLatitude(), location.getLongitude(), tempLocation.getDouble("lat"), tempLocation.getDouble("lng")) * 100) / 100);
                            }
                            if (jsonArray.getJSONObject(i).has("place_id")) {
                                poi.setReference(jsonArray.getJSONObject(i).getString("place_id"));
                            }
                            if (jsonArray.getJSONObject(i).has("photos")) {
                                JSONObject photo = new JSONObject(jsonArray.getJSONObject(i).getJSONArray("photos").get(0).toString());
                                poi.setPhotoReference(photo.getString("photo_reference"));
                            }
                        }
                    }
                    temp.add(poi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<CustomLocation>();
        }
        return temp;

    }

    public static double calculationByDistance(double lat1, double lon1, double lat2, double lon2) {
        int Radius = 6371;//radius of earth in Km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        double miles = 0.621371 * km;
        return miles;
    }

    public static String downloadUrl(String strUrl) throws IOException {
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

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=" + WAHApplication.GOOGLE_KEY;
            // parameter use to filter places type to be search based on location
            String biasedLocation = "";
            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            if(location!=null) {
                biasedLocation = "location=" + location.getLatitude() + "," + location.getLongitude() + "&radius=" + 1000;
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled (Not needed in latest version)
            String sensor = "sensor=false";

            // Building the parameters to the web service
//            String parameters = input + "&" + types + "&" + sensor + "&" + key;
            /* vijay: don't pass `types` to retain all types of search results
               (e.g. hotel Vivanta is categorized as `establishment` and hence doesn't appear
               if we have types=geocode */
            String parameters = input + "&" + /*types + "&" +*/ biasedLocation + "&" + sensor + "&" + key;

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
            if (venuesList == null)
                venuesList = new ArrayList<CustomLocation>();
            venuesList.clear();
            List<String> categories = new ArrayList<>();
            categories.add("mosque");
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    final Map dataSet = result.get(i);
                    CustomLocation myCustomLocation = new CustomLocation();
                    myCustomLocation.setName(dataSet.get(from[0]).toString());
                    myCustomLocation.setReference(dataSet.get(from[1]).toString());
                    myCustomLocation.setCategory(categories);
                    myCustomLocation.setDistance(-1);
                    venuesList.add(myCustomLocation);
                }
                if (venuesList.size() > 0)
                    nearby.setVisibility(View.GONE);
                setMyAdapter();
            }

        }
    }
}
