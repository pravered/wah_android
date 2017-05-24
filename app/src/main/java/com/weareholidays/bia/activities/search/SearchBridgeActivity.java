package com.weareholidays.bia.activities.search;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;

public class SearchBridgeActivity extends AppCompatActivity {

    private EditText search;
    private ProgressBar spinner;
    private TextView noResultsText;
    private static final String TAG = "SEARCH_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bridge);
        search = (EditText) findViewById(R.id.search);
        search.setHintTextColor(getResources().getColor(R.color.search_hint_color));
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        noResultsText = (TextView) findViewById(R.id.no_results);
        //start search activity on pressing Done
        search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
        //start search activity if user presses search icon
        search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = search.getRight()
                            - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.activity_horizontal_margin);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // your action here
                        performSearch();
                        return true;
                    }
                    int rightEdgeOfLeftDrawable = search.getLeft() + search.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width();
                    //adjust padding
                    rightEdgeOfLeftDrawable += getResources().getDimension((R.dimen.app_padding));
                    if (event.getRawX() <= rightEdgeOfLeftDrawable) {
                        onBackPressed();
                    }
                }
                return false;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                search.requestFocusFromTouch();
            }
        }, 100);

    }

    public void performSearch() {
        String searchString = search.getText().toString();
        if (searchString.length() > 0) {
            noResultsText.setVisibility(View.GONE);
            ViewUtils.hideKeyboard(this);
            new SearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{searchString});
        } else {
            Toast.makeText(this, "No input was made", Toast.LENGTH_SHORT).show();
        }
    }

    private class SearchTask extends AsyncTask<String,Void,Void>{

        private int tripsCount = 0;
        private int usersCount = 0;
        private String searchString;

        @Override
        protected void onPreExecute(){
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            if(params != null && params.length > 0){
                searchString = params[0];
                try {
                    tripsCount = TripUtils.searchTrips(searchString).count();
                } catch (Exception e) {
                    Log.e(TAG, "Error searching trips", e);
                }

                try {
                    usersCount = TripUtils.searchUsers(searchString).count();
                } catch (Exception e) {
                    Log.e(TAG,"Error searching users",e);
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {

            try {
                if (tripsCount + usersCount > 0) {
                    Intent intent = new Intent(SearchBridgeActivity.this, SearchActivity.class);
                    intent.putExtra(SearchActivity.TRIP_RESULT_COUNT, tripsCount);
                    intent.putExtra(SearchActivity.USER_RESULT_COUNT, usersCount);
                    intent.putExtra(SearchActivity.SEARCH_STRING, searchString);
                    startActivity(intent);
                }
                else{
                    spinner.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.VISIBLE);
                }
                spinner.setVisibility(View.GONE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_bridge, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
