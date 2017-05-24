package com.weareholidays.bia.utils;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.widget.Toast;

import com.weareholidays.bia.parse.models.ParseCustomUser;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wah on 15/9/15.
 */
public class Utils {

    /**
     * Convert Dp to Pixel
     */
    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static class CallServerApi extends AsyncTask<Void, Void, HttpResponse> {
        HashMap<String, String> apiParams;
        String baseUrl;

        public CallServerApi(HashMap<String, String> params, String url) {
            this.apiParams = params;
            this.baseUrl = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected HttpResponse doInBackground(Void... params) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(baseUrl);
            HttpResponse response = null;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
                for (Map.Entry<String, String> entry : apiParams.entrySet()) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }

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
            if (result != null) {
                try {
                    String s = EntityUtils.toString(result.getEntity());
                    JSONObject jsonObject = new JSONObject(s);
                    String responseResult = jsonObject.getString("result");
                    if (responseResult.equalsIgnoreCase("success")) {
                    } else {
                        DebugUtils.LogD("Something went wrong while registering user, Try later.");
                    }
                } catch (Exception e) {
                    DebugUtils.logException(e);
                }
            } else {
                DebugUtils.LogD("Something went wrong while registering user, Try later.");
            }
        }
    }
}
