package com.parse;

import android.content.Context;

import com.parse.instagram.Instagram;
import com.parse.internal.AsyncCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import bolts.Task;

/**
 * Created by Teja on 10-06-2015.
 */
class InstagramAuthenticationProvider extends ParseAuthenticationProvider {
    public static final String AUTH_TYPE = "instagram";
    private static final String SCREEN_NAME_KEY = "screen_name";
    private static final String ID_KEY = "id";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String CONSUMER_KEY_KEY = "consumer_key";
    private static final String CONSUMER_SECRET_KEY = "consumer_secret";
    private WeakReference<Context> baseContext;
    private final Instagram instagram;
    private ParseAuthenticationCallback currentOperationCallback;

    public InstagramAuthenticationProvider(Instagram instagram){
        this.instagram = instagram;
    }

    public Instagram getInstagram() {
        return this.instagram;
    }

    @Override
    public String getAuthType() {
        return AUTH_TYPE;
    }

    private void authenticate(final ParseAuthenticationCallback callback) {
        if(this.currentOperationCallback != null) {
            this.cancel();
        }

        this.currentOperationCallback = callback;
        Context context = this.baseContext == null?null:(Context)this.baseContext.get();
        if(context == null) {
            throw new IllegalStateException("Context must be non-null for Twitter authentication to proceed.");
        } else {
            this.instagram.authorize(context, new AsyncCallback() {
                public void onCancel() {
                    InstagramAuthenticationProvider.this.handleCancel(callback);
                }

                public void onFailure(Throwable error) {
                    if(InstagramAuthenticationProvider.this.currentOperationCallback == callback) {
                        try {
                            callback.onError(error);
                        } finally {
                            InstagramAuthenticationProvider.this.currentOperationCallback = null;
                        }

                    }
                }

                public void onSuccess(Object result) {
                    if(InstagramAuthenticationProvider.this.currentOperationCallback == callback) {
                        try {
                            JSONObject authData;
                            try {
                                authData = InstagramAuthenticationProvider.this.getAuthData(InstagramAuthenticationProvider.this.instagram.getUserId(), InstagramAuthenticationProvider.this.instagram.getScreenName(), InstagramAuthenticationProvider.this.instagram.getAccessToken());
                            } catch (JSONException var7) {
                                callback.onError(var7);
                                return;
                            }

                            callback.onSuccess(authData);
                        } finally {
                            InstagramAuthenticationProvider.this.currentOperationCallback = null;
                        }
                    }
                }
            });
        }
    }

    private void handleCancel(ParseAuthenticationCallback callback) {
        if(this.currentOperationCallback == callback && callback != null) {
            try {
                callback.onCancel();
            } finally {
                this.currentOperationCallback = null;
            }

        }
    }

    public JSONObject getAuthData(String userId, String screenName, String accessToken) throws JSONException {
        JSONObject authData = new JSONObject();
        authData.put(ACCESS_TOKEN_KEY, accessToken);
        authData.put(ID_KEY, userId);
        authData.put(SCREEN_NAME_KEY, screenName);
        authData.put(CONSUMER_KEY_KEY, this.instagram.getConsumerKey());
        authData.put(CONSUMER_SECRET_KEY, this.instagram.getConsumerSecret());
        return authData;
    }

    @Override
    public Task<JSONObject> authenticateAsync() {
        final Task.TaskCompletionSource tcs = Task.create();
        this.authenticate(new ParseAuthenticationCallback() {
            public void onSuccess(JSONObject authData) {
                tcs.setResult(authData);
            }

            public void onCancel() {
                tcs.setCancelled();
            }

            public void onError(Throwable error) {
                tcs.setError(new ParseException(error));
            }
        });
        return tcs.getTask();
    }

    @Override
    public void deauthenticate() {
        this.instagram.setAccessToken((String) null);
        this.instagram.setScreenName((String)null);
        this.instagram.setUserId((String)null);
    }

    @Override
    public boolean restoreAuthentication(JSONObject authData) {
        if(authData == null) {
            this.instagram.setAccessToken((String) null);
            this.instagram.setScreenName((String)null);
            this.instagram.setUserId((String)null);
            return true;
        } else {
            try {
                this.instagram.setAccessToken(authData.getString(ACCESS_TOKEN_KEY));
                this.instagram.setUserId(authData.getString(ID_KEY));
                this.instagram.setScreenName(authData.getString(SCREEN_NAME_KEY));
                return true;
            } catch (Exception var3) {
                return false;
            }
        }
    }

    @Override
    public void cancel() {
        this.handleCancel(this.currentOperationCallback);
    }

    public InstagramAuthenticationProvider setContext(Context context) {
        this.baseContext = new WeakReference(context);
        return this;
    }
}
