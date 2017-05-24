package com.weareholidays.bia.asyncTasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by wah on 20/8/15.
 */
public class ShortenURLTask extends AsyncTask<String, Void, JSONObject> {

    private String GOOGLE_SHORTENER_API_URL = "https://www.googleapis.com/urlshortener/v1/url?key=";
    private String mSharedUrl;
    private String mTripName;
    private String mPhotoCaption;
    private boolean mIsPhotoShare;
    private boolean mIsAppShare;
    private Context mContext;

    public ShortenURLTask(Context context, String tripName, String photoCaption, boolean isPhotoShare, boolean mIsAppShare) {
        this.mContext = context;
        this.mTripName = tripName;
        this.mPhotoCaption = photoCaption;
        this.mIsPhotoShare = isPhotoShare;
        this.mIsAppShare = mIsAppShare;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        mSharedUrl = params[0];


        if (!ViewUtils.isNetworkAvailable(mContext)) {
            return null;
        }
        JSONObject results;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(GOOGLE_SHORTENER_API_URL + WAHApplication.GOOGLE_KEY);
            post.setEntity(new StringEntity("{\"longUrl\": \"" + mSharedUrl + "\"}"));
            post.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }
            results = new JSONObject(new JSONTokener(builder.toString()));
        } catch (Exception e) {
            DebugUtils.logException(e);
            return null;
        }
        return results;
    }

    /**
     * @param result JSONObject result received from Goo.gl
     */
    protected void onPostExecute(JSONObject result) {
        String shortenedURL = "";
        if (result != null) {
            try {
                shortenedURL = result.getString("id");
            } catch (JSONException e) {
                DebugUtils.logException(e);
                shortenedURL = mSharedUrl;
            }
        } else {
            shortenedURL = mSharedUrl; //return same url if no internet connection or response is null
        }

        String userName = ParseCustomUser.getCurrentUser().getName();
        String subject = null;
        String message = null;
        if (mIsPhotoShare) {
            if (mPhotoCaption == null) {
                mPhotoCaption = "";
            }
            if (!mPhotoCaption.isEmpty()) {
                subject = userName + " shared photo \"" + mPhotoCaption + "\" with you on Bia";
            } else {
                subject = userName + " shared a photo with you on Bia";
            }
            message = subject + ". To view the photo, visit " + shortenedURL;
        } else if (mIsAppShare) {
            subject = userName + " suggests you to use Bia";
            message = subject +" - Create beautiful trip journals & log your trips effortlessly with Bia. Download the app at " + shortenedURL;
        } else {
            subject = userName + " shared trip \"" + mTripName + "\" with you on Bia";
            message = subject + ". To view the trip, visit " + shortenedURL;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("text/plain");
        mContext.startActivity(Intent.createChooser(intent, mContext.getResources().getText(R.string.share2)));
    }
}