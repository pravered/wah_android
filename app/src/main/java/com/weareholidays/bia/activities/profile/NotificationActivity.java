package com.weareholidays.bia.activities.profile;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.NotificationAdapter;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.models.Notification;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.weareholidays.bia.utils.SharedPrefUtils;

import java.util.List;



public class NotificationActivity extends AppCompatActivity {

    private List<Notification> notifications;
    public Context mcontext;

    public NotificationActivity(){
        mcontext = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notifications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ParseQuery.getQuery(Notification.class)
                .whereEqualTo(Notification.USERNAME, ParseUser.getCurrentUser().getUsername())
                .whereEqualTo(Notification.IS_READ, false).orderByDescending(Notification.CONTENT_TIME).findInBackground(new FindCallback<Notification>() {
            @Override
            public void done(List<Notification> list, ParseException e) {
                if (e == null) {
                    notifications = list;
                    if (notifications == null || notifications.size() == 0) {
                        TextView noNotification = (TextView) findViewById(R.id.no_notification);
                        noNotification.setVisibility(View.VISIBLE);
                    }
//                    notifyItemArrayAdapter = new NotificationAdapter(NotificationActivity.this, notifications);
//                    notificationListView = (ListView) findViewById(R.id.notificationList);
//                    notificationListView.setAdapter(notifyItemArrayAdapter);
                } else {
                    Log.e("Notifications", "error getting notifications", e);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_notification, menu);
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
}
