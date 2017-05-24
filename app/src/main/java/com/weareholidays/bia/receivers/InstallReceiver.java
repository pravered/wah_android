package com.weareholidays.bia.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;

import java.net.URLDecoder;
import java.util.HashMap;

/**
 * Created by wah on 20/8/15.
 */
public class InstallReceiver extends BroadcastReceiver {

    HashMap<String, String> values;
    private Uri url;
    String sharerObjectId;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            values = new HashMap<String, String>();
            try {
                if (intent.hasExtra("referrer")) {
                    String referrers[] = intent.getStringExtra("referrer").split("&");
                    for (String referrerValue : referrers) {
                        String keyValue[] = referrerValue.split("=");
                        values.put(URLDecoder.decode(keyValue[0]), URLDecoder.decode(keyValue[1]));
                    }
                    saveSharer(context, values.get("sharer"));
                }
            } catch (Exception e) {
                DebugUtils.logException(e);
            }

            new CampaignTrackingReceiver().onReceive(context, intent);
        }
    }

    private void saveSharer(Context context, String sharerObjectId) {
        SharedPrefUtils.setStringPreference(context,SharedPrefUtils.Keys.SHARER_ID,sharerObjectId);
    }
}