package com.parse.instagram;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.CookieSyncManager;

import com.parse.internal.AsyncCallback;
import com.parse.oauth.OAuth1FlowDialog;
import com.parse.oauth.OAuth1FlowException;
import com.parse.signpost.commonshttp.CommonsHttpOAuthConsumer;
import com.parse.signpost.commonshttp.CommonsHttpOAuthProvider;
import com.parse.signpost.http.HttpParameters;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Teja on 10-06-2015.
 */
public class Instagram {
    private static final String USER_AGENT = "Parse Android SDK";
    private static final String AUTHORIZE_URL = "https://instagram.com/oauth/authorize";
    private static final String API_URL = "https://api.instagram.com/v1/";
    private static final String TOKEN_PARAM = "access_token";
    private static final String USER_ID_PARAM = "id";
    private static final String SCREEN_NAME_PARAM = "full_name";
    private final String CALLBACK_URL;
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String userId;
    private String screenName;

    public Instagram(String callbackUrl){
        CALLBACK_URL = callbackUrl;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public Instagram setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
        return this;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public Instagram setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    private String getRequestUrl(){
        return AUTHORIZE_URL + "?client_id=" + consumerKey + "&redirect_uri=" + CALLBACK_URL + "&response_type=token";
    }

    private String getUserDetailsUrl(String token){
        return API_URL + "users/self/?access_token=" + token;
    }

    public void authorize(final Context context, final AsyncCallback callback) {
        if(this.getConsumerKey() != null && this.getConsumerKey().length() != 0 && this.getConsumerSecret() != null && this.getConsumerSecret().length() != 0) {
            final ProgressDialog progress = new ProgressDialog(context);
            progress.setMessage("Loading...");
            CookieSyncManager.createInstance(context);
            OAuth1FlowDialog dialog = new OAuth1FlowDialog(context, getRequestUrl(), CALLBACK_URL, "instagram.com", new OAuth1FlowDialog.FlowResultHandler() {
                public void onError(int errorCode, String description, String failingUrl) {
                    callback.onFailure(new OAuth1FlowException(errorCode, description, failingUrl));
                }

                public void onComplete(String callbackUrl) {
                    CookieSyncManager.getInstance().sync();
                    String tempToken = null;
                    if(callbackUrl.startsWith(CALLBACK_URL)){
                        String[] splits = callbackUrl.split(CALLBACK_URL + "#" + TOKEN_PARAM + "=");
                        if(splits.length == 2){
                            tempToken = splits[1];
                            if(TextUtils.isEmpty(tempToken))
                                tempToken = null;
                        }
                    }
                    final String token = tempToken;
                    if(token == null) {
                        callback.onCancel();
                    } else {
                        AsyncTask<Void,Void,JSONObject> getTokenTask = new AsyncTask<Void,Void,JSONObject>() {
                            private Throwable error;

                            protected JSONObject doInBackground(Void... params) {
                                InputStream is = null;
                                int len = 5000;
                                try {
                                    URL url = new URL(getUserDetailsUrl(token));
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setReadTimeout(10000 /* milliseconds */);
                                    conn.setConnectTimeout(15000 /* milliseconds */);
                                    conn.setRequestMethod("GET");
                                    conn.setDoInput(true);
                                    // Starts the query
                                    conn.connect();

                                    int response = conn.getResponseCode();

                                    is = conn.getInputStream();

                                    // Convert the InputStream into a string
                                    String contentAsString = readIt(is, len);

                                    JSONObject jsonObject = new JSONObject(contentAsString);
                                    return jsonObject.getJSONObject("data");
                                } catch (Throwable var3) {
                                    this.error = var3;
                                }
                                finally {
                                    if(is != null){
                                        try {
                                            is.close();
                                        } catch (Exception e) {

                                        }
                                    }
                                }

                                return null;
                            }

                            public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
                                Reader reader = null;
                                reader = new InputStreamReader(stream, "UTF-8");
                                char[] buffer = new char[len];
                                reader.read(buffer);
                                return new String(buffer);
                            }

                            protected void onPreExecute() {
                                super.onPreExecute();
                                progress.show();
                            }

                            protected void onPostExecute(JSONObject result) {
                                super.onPostExecute(result);

                                try {
                                    if(this.error != null) {
                                        callback.onFailure(this.error);
                                        return;
                                    }

                                    try {
                                        Instagram.this.setAccessToken(token);
                                        Instagram.this.setScreenName(result.getString(SCREEN_NAME_PARAM));
                                        Instagram.this.setUserId(result.getString(USER_ID_PARAM));
                                    } catch (Throwable var6) {
                                        callback.onFailure(var6);
                                        return;
                                    }

                                    callback.onSuccess(Instagram.this);
                                } finally {
                                    progress.dismiss();
                                }

                            }
                        };
                        getTokenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }

                public void onCancel() {
                    callback.onCancel();
                }
            });
            dialog.show();
            return;
        } else {
            throw new IllegalStateException("Instagram must be initialized with a consumer key and secret before authorization.");
        }
    }
}
