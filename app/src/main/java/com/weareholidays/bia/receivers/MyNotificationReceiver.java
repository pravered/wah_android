package com.weareholidays.bia.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParsePushBroadcastReceiver;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.utils.DebugUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by challa on 16/6/15.
 */
public class MyNotificationReceiver extends ParsePushBroadcastReceiver {

    private static String CONTENT_TITLE = "content_title";
    private static String CONTENT_SUBTITLE = "content_subtitle";
    private static String IMG_TITLE = "img_title";
    private static String IMG_SUBTITLE = "img_desc";
    private static String IMG_URL = "img_url";
    private static String TICKER_MSG = "ticker_msg";


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null) {
            DebugUtils.LogD("Receiver intent null");
        }
    }

    public void sendCustomNotification(final Context context, final Intent intent, Bitmap resource, final int notificationId) {

        String contentTitle = "", contentText = "", imgTitle, imgText, tickerMsg, imgUrl = null;

        //building notification and setting style and other default parameters
        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getSmallIconId(context, intent))
                        .setLargeIcon(getLargeIcon(context, intent))
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setDefaults(-1);

        //Adding pending intents
        Bundle extras = intent.getExtras();
        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();
        String packageName = context.getPackageName();


        Intent contentIntent = new Intent("com.parse.push.intent.OPEN");
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);

        Intent deleteIntent = new Intent("com.parse.push.intent.DELETE");
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pContentIntent)
                .setDeleteIntent(pDeleteIntent);

        //setting title and content
        final NotificationCompat.BigPictureStyle bigPicStyle = new
                NotificationCompat.BigPictureStyle();
        JSONObject pushData = fetchObjectFromIntent(intent);
        if (pushData != null) {
            imgUrl = pushData.optString(IMG_URL);
            contentTitle = pushData.optString(CONTENT_TITLE);
            contentText = pushData.optString(CONTENT_SUBTITLE);
            imgText = pushData.optString(IMG_SUBTITLE);
            imgTitle = pushData.optString(IMG_TITLE);
            tickerMsg = pushData.optString(TICKER_MSG);

            bigPicStyle.setBigContentTitle(TextUtils.isEmpty(imgTitle) ? contentTitle : imgTitle);
            bigPicStyle.setSummaryText(TextUtils.isEmpty(imgText) ? contentText : imgText);

            mBuilder.setTicker(TextUtils.isEmpty(tickerMsg) ? contentTitle : tickerMsg)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setStyle(bigPicStyle);
        }

        if (resource == null && imgUrl != null && !TextUtils.isEmpty(imgUrl)) {
            final String finalContentTitle = contentText;
            Glide.with(context)
                    .load(imgUrl)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(400, 400) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            bigPicStyle.bigPicture(resource);

                            NotificationManager notificationManager = (NotificationManager) context
                                    .getSystemService(Context.NOTIFICATION_SERVICE);
                            Notification notification = mBuilder.build();
                            try {
                                notificationManager.notify(notificationId, notification);
                            } catch (SecurityException var6) {
                                notification.defaults = 5;
                                notificationManager.notify(notificationId, notification);
                            }

                            addToNotificationTab(intent, finalContentTitle);
                        }
                    });
        }
    }

    private void addToNotificationTab(Intent intent, String content) {
        com.weareholidays.bia.parse.models.Notification notification = new com.weareholidays.bia.parse.models.Notification();
        notification.setContent(content);
        notification.setContentTime(Calendar.getInstance().getTime());
        notification.setIsRead(false);
        notification.setNotifier(ParseCustomUser.getCurrentUser());


        JSONObject myObj = fetchObjectFromIntent(intent);
        if (myObj != null && myObj.optBoolean("custom_msg")) {
            String actionType = myObj.optString(ShareUtils.ACTION_TYPE);
            String actionParams = myObj.optString(ShareUtils.ACTION_PARAMS);

            notification.setActionType(actionType);
            notification.setActionParams(actionParams);
        }
        notification.setUser(ParseCustomUser.getCurrentUser().getUsername());
        notification.saveInBackground();
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        String actionType;
        String actionParams;
        Intent homeIntent = new Intent(context, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            JSONObject myObj = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            actionType = myObj.optString(ShareUtils.ACTION_TYPE);
            actionParams = myObj.optString(ShareUtils.ACTION_PARAMS);
            Intent myIntent = ShareUtils.getNotificationRedirectIntent(actionType, actionParams, context);
            if (myIntent != null) {
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(myIntent);
            } else {
                context.startActivity(homeIntent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            context.startActivity(homeIntent);
        }
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        int notificationId = (int) System.currentTimeMillis();
        boolean isCustomMessage = false;

        JSONObject pushData = fetchObjectFromIntent(intent);

        if (pushData != null) {
            isCustomMessage = pushData.optBoolean("custom_msg");
        }

        if (!isCustomMessage) {
            super.onPushReceive(context, intent);
            addToNotificationTab(intent,pushData.optString("alert"));
            return;
        }

        String action = null;
        if (pushData != null) {
            action = pushData.optString("action", (String) null);
        }
        if (action != null) {
            Bundle notification = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(notification);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        sendCustomNotification(context, intent, null, notificationId);

    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return R.drawable.logo;
        } else {
            return R.drawable.launch_icon;
        }
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.launch_icon);
    }

    public JSONObject fetchObjectFromIntent(Intent intent) {
        JSONObject pushData = null;
        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException var7) {
            DebugUtils.LogD("com.parse.ParsePushReceive Unexpected JSONException when receiving push data: " + var7);
        }

        return pushData;
    }
}