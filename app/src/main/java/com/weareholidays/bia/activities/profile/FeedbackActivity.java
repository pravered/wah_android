package com.weareholidays.bia.activities.profile;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crittercism.app.Crittercism;
import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    EditText feedbackMessage;
    TextView feedbackId;
    Button feedbackSubmit;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Share your feedback");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        feedbackMessage = (EditText) findViewById(R.id.feedback_message);

        feedbackSubmit = (Button) findViewById(R.id.submit);
        feedbackSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendFeedback().execute(feedbackMessage.getText().toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class SendFeedback extends AsyncTask<String, Void, HttpResponse> {

        private CenterProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CenterProgressDialog(FeedbackActivity.this);
            progressDialog.show();
        }

        @Override
        protected HttpResponse doInBackground(String... params) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://localhost:1337/parse/");
            HttpResponse response = null;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(12);
                nameValuePairs.add(new BasicNameValuePair("action", "feedback"));
                nameValuePairs.add(new BasicNameValuePair("sender_id", ParseCustomUser.getCurrentUser().getObjectId()));
                nameValuePairs.add(new BasicNameValuePair("sender_name", ParseCustomUser.getCurrentUser().getName()));
                nameValuePairs.add(new BasicNameValuePair("sender_email", ParseCustomUser.getCurrentUser().getEmail()));
                nameValuePairs.add(new BasicNameValuePair("device", android.os.Build.DEVICE + " " + android.os.Build.MODEL));
                nameValuePairs.add(new BasicNameValuePair("os_name", "Android"));
                nameValuePairs.add(new BasicNameValuePair("os_version", Build.VERSION.RELEASE));
                PackageInfo pInfo = null;
                String version = "";
                try {
                    pInfo = FeedbackActivity.this.getPackageManager().getPackageInfo(FeedbackActivity.this.getPackageName(), 0);
                    version = pInfo.versionName;

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                nameValuePairs.add(new BasicNameValuePair("app_version", version));
                nameValuePairs.add(new BasicNameValuePair("feedback", params[0]));
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                nameValuePairs.add(new BasicNameValuePair("time", sdf.format(cal.getTime())));

                nameValuePairs.add(new BasicNameValuePair("phone", ParseCustomUser.getCurrentUser().getPhone()));
                nameValuePairs.add(new BasicNameValuePair("city", ParseCustomUser.getCurrentUser().getPlace()));


                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                response = httpclient.execute(httppost);

            } catch (ClientProtocolException e) {
                DebugUtils.logException(e);
            } catch (IOException e) {
                DebugUtils.logException(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(HttpResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            if (result != null) {
                try {
                    String s = EntityUtils.toString(result.getEntity());
                    JSONObject jsonObject = new JSONObject(s);
                    String responseResult = jsonObject.getString("result");
                    if (responseResult.equalsIgnoreCase("success")) {
                        showEditTripDialog();
                    } else {
                        Toast.makeText(FeedbackActivity.this, "Something went wrong, Try later.", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    DebugUtils.logException(e);
                } catch (JSONException e) {
                    DebugUtils.logException(e);
                }
            } else {
                Toast.makeText(FeedbackActivity.this, "Something went wrong, Try later.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showEditTripDialog() {
        View view = FeedbackActivity.this.getLayoutInflater().inflate(R.layout.image_layout, null);
        MaterialDialog editDialog = new MaterialDialog.Builder(FeedbackActivity.this)
                .customView(view, true)
//                .title(R.string.action_edit_trip_name)
                .positiveText("Okay")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        finish();
                    }
                }).show();
    }
}
