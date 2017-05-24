package com.weareholidays.bia.activities.journal.actions;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.AsyncResponse;
import com.weareholidays.bia.activities.journal.ReturnLocation;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.activities.journal.photo.SetPhotoLocationActivity;
import com.weareholidays.bia.activities.journal.trip.TripFragment;
import com.weareholidays.bia.asyncTasks.CheckInAsync;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.Note;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.GPSTracker;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;
import com.parse.ParseGeoPoint;

import java.util.Calendar;
import java.util.List;

public class NoteActivity extends TripBaseActivity implements AsyncResponse , GPSTracker.GPSListener{

    public static final String EDIT_NOTE_VIEW = "EDIT_NOTE_VIEW";

    public static final int NOTE_TYPE = 2;

    private boolean isEdit;

    private EditText noteText;

    private EditText atvPlaces;

    private TextView mTextLength;

    public static final int SELECT_NOTE_LOCATION = 2073;

    private ParseGeoPoint parseGeoPoint;

    private CheckInAsync locationAsync;

    private boolean savingNote;

    private long mLastClickTime = 0;
    GPSTracker gpsTracker;
    @Override
    public void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        setContentView(R.layout.activity_note);

        isEdit = getIntent().getBooleanExtra(EDIT_NOTE_VIEW,false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String tripName = tripOperations.getTrip().getName();
        if(tripName.length()>21)
            tripName = tripName.substring(0,20)+"....";
        getSupportActionBar().setTitle(tripName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(isEdit)
            getSupportActionBar().setSubtitle("EDITING NOTE");
        else
            getSupportActionBar().setSubtitle("ADDING NOTE");

        noteText = (EditText)findViewById(R.id.noteText);
        mTextLength = (TextView) findViewById(R.id.max_length);
        noteText.addTextChangedListener(new TextWatcher() {
            int TOTAL_CHARS = 300;
            int limit = TOTAL_CHARS;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                limit = TOTAL_CHARS - length;
                if(limit == 300)
                    mTextLength.setText(getString(R.string.max_length));
                else
                    mTextLength.setText(limit + " CHARACTERS LEFT");
            }
        });
        atvPlaces = (EditText) findViewById(R.id.atv_places);

        atvPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NoteActivity.this, SetPhotoLocationActivity.class);
                i.putExtra("NOTE", true);
                startActivityForResult(i, SELECT_NOTE_LOCATION);
            }
        });

        if(!isEdit){
            locationAsync = new CheckInAsync(this);
            CheckInAsync.delegate = this;
            locationAsync.execute();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    noteText.requestFocusFromTouch();
                }
            }, 100);
        }

        if(isEdit){
            Note note = (Note)tripOperations.getSelectedNote().getContent();
            noteText.setText(note.getContent());
            atvPlaces.setText(note.getLocationText());
        }

        Button saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                hideKeyboard();
                if (!noteText.getText().toString().equals("") && !savingNote){
                    savingNote = true;
                    new NoteSaveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                    Toast.makeText(getBaseContext(), "Cannot add empty note", Toast.LENGTH_SHORT).show();
            }
        });

        if(!isNetworkAvailable()){
            gpsTracker = new GPSTracker(this,this);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gpsTracker != null){
            gpsTracker.stopUsingGPS();
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
                    parseGeoPoint = new ParseGeoPoint();
                    parseGeoPoint.setLatitude(data.getDoubleExtra("latitude",0));
                    parseGeoPoint.setLongitude(data.getDoubleExtra("longitude",0));
                    if(isEdit){
                        tripOperations.saveNote(null,photo_location,parseGeoPoint,false);
                    }
                } catch (Exception e) {
                    DebugUtils.logException(e);
                    Toast.makeText(this, "Couldn't load location", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processFinish(ReturnLocation returnLocation) {
        if(returnLocation != null && returnLocation.getLocation() != null){
            parseGeoPoint = new ParseGeoPoint();
            parseGeoPoint.setLatitude(returnLocation.getLocation().getLatitude());
            parseGeoPoint.setLongitude(returnLocation.getLocation().getLongitude());
            atvPlaces.setText(returnLocation.getAddressString());
        }
    }

    @Override
    public void onGPSLocationChanged(Location location) {
        try {
            String photo_location = location.getLatitude()+" "+location.getLongitude();
            atvPlaces.setText(photo_location);
            parseGeoPoint = new ParseGeoPoint();
            parseGeoPoint.setLatitude(location.getLatitude());
            parseGeoPoint.setLongitude(location.getLongitude());
        } catch (Exception e) {
            DebugUtils.logException(e);
            Toast.makeText(this, "Couldn't load location", Toast.LENGTH_LONG).show();
        }
    }

    private class NoteSaveTask extends AsyncTask<Void,Void,Void>{

        private boolean saved = false;
        private Timeline timeline;
        private String content;
        private String addressText;
        private CenterProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            try{
                progressDialog = CenterProgressDialog.show(NoteActivity.this, null, null, true, false);

                content = noteText.getText().toString();
                addressText = atvPlaces.getText().toString();
                hideKeyboard();
            } catch (Exception e){

            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{

                if(isEdit){
                    tripOperations.saveNote(content, addressText, parseGeoPoint,true);
                    timeline = tripOperations.getSelectedNote();
                    saved = true;
                }
                else{
                    if(tripOperations instanceof TripLocalOperations){
                        //timeline = ((TripLocalOperations)tripOperations).addNote(content, addressText, parseGeoPoint);
                        // check if already saved (vijay: dirty hack for the time being)
                        boolean alreadySaved = false;
                        TripLocalOperations tripLocalOperations = (TripLocalOperations) tripOperations;
                        Calendar calendar = Calendar.getInstance();
                        Trip trip  = tripLocalOperations.getTrip();
                        Day currentDay = TripUtils.getDayFromTime(trip.getDays(), calendar);
                        if(currentDay == null){
                            throw TripUtils.createDayNotFoundException(calendar);
                        }
                        synchronized(NoteActivity.this){
                            List<Timeline> notes = tripLocalOperations.getDayTimeLines(currentDay, -1, 0, "WAH", "Note");
                            for (Timeline timeline : notes) {
                                Note note = (Note) timeline.getContent();
                                if (note.getContent().equals(content.trim())) {
                                    if (Math.abs(calendar.getTime().getTime() - timeline.getContentTime().getTime()) < 60000) {
                                        alreadySaved = true;
                                        break;
                                    }
                                }
                            }
                            if (!alreadySaved)
                                timeline = ((TripLocalOperations)tripOperations).addNote(content, addressText, parseGeoPoint);
                        }
                        saved = true;
                    }
                }
            } catch (Exception e){
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            savingNote = false;
            try{
                if(saved){
                    if(isEdit){
                        setResult(RESULT_OK);
                        finish();
                    }
                    else{
                        Intent intent = new Intent(NoteActivity.this, HomeActivity.class);
                        intent.putExtra(HomeActivity.SHOW_TAB,HomeActivity.JOURNAL_TAB);
                        intent.putExtra(TripFragment.SHOW_JOURNAL_DAY,timeline.getDayOrder()+1);
                        intent.putExtra(TripFragment.TIMELINE_SCROLL_POSITION,timeline.getDisplayOrder());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
                else{
                    Toast.makeText(NoteActivity.this, getString(R.string.toast_add_note_error), Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            } catch (Exception e){

            }
        }
    }

    @Override
    public int getType() {
        return NOTE_TYPE;
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();
        super.onBackPressed();
    }

    private void hideKeyboard(){
        ViewUtils.hideKeyboard(this);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(noteText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}

