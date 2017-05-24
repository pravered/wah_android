package com.parseInsta;

import android.content.Context;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.Toast;

import com.parse.ParseUser;

import org.json.JSONObject;

public class ParseInstagramUtil {

    public static final String AUTH_TYPE = "instagram";

    public static boolean isLinked(ParseUser user, Context context) {
        if(user.isLinked(AUTH_TYPE)){
            return true;
        }
        InstagramSession instagramSession = new InstagramSession(context);
        if (!TextUtils.isEmpty(instagramSession.getAccessToken())) {
            // caution: token might be expired, handle when request fails
            return true;
        }

        //Work around for Auth Data restriction
//        if(user.has("instagram_auth")){
//            provider.restoreAuthentication(user.getJSONObject("instagram_auth"));
//            if(!TextUtils.isEmpty(getInstagram().getUserId())){
//                return true;
//            }
//        }
        return false;
    }


    public static void requestUserLogin(final Context context) {
        InstagramApp instagramApp = new InstagramApp(context, InstagramConstants.CLIENT_ID, InstagramConstants.CLIENT_SECRET, InstagramConstants.REDIRECT_URL);
        instagramApp.setListener(new InstagramApp.OAuthAuthenticationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT)
                        .show();
            }
        });
        instagramApp.authorize();
    }

    public static JSONObject getUserData(Context context) {
        InstagramSession session = new InstagramSession(context);
        String userId = session.getId();
        String accessToken = session.getAccessToken();


        JSONObject jsonObject = new JSONParser().getJSONFromUrlByGet("https://api.instagram.com/v1/users/" +
                userId + "/media/recent/?access_token=" +
                accessToken + "&count=null");
        //    JSONObject dateFilteredJson = dataAfterTripStart(jsonObject);
        return jsonObject;
    }

}
