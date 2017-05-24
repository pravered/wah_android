package com.weareholidays.bia.activities.journal.trip;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;

public class ChangeImageActivity extends TripBaseActivity {

    @Override
    public void onTripLoaded(Bundle savedBundle) {
        super.onTripLoaded(savedBundle);
        setContentView(R.layout.activity_change_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_image, menu);
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
