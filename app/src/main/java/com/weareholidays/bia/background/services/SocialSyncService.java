package com.weareholidays.bia.background.services;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.parse.ParseTwitterUtils;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.background.receivers.SocialSyncServiceReceiver;
import com.weareholidays.bia.background.receivers.TripServiceManager;
import com.weareholidays.bia.background.receivers.TripServiceStopManager;
import com.weareholidays.bia.background.receivers.UploadTripReceiver;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.TripSettings;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.social.facebook.models.FacebookPost;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.weareholidays.bia.social.instagram.models.InstagramPost;
import com.weareholidays.bia.social.instagram.utils.InstagramUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MediaUtils;
import com.crittercism.app.Crittercism;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.parse.ParseException;
//import com.parse.ParseInstagramUtils;
//import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
//import com.parse.instagram.Instagram;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
//import twitter4j.auth.AccessToken;

import static com.weareholidays.bia.R.string.instagram;


/**
 * Created by Teja on 06/06/15.
 */
public class SocialSyncService extends Service {

    //TODO : Change to 15 mins
    public static int SYNC_INTERVAL = 1000 * 60 * 15;//15 minutes

    private static final String TAG = "SOCIAL_SYNC_SERVICE";

    IBinder mBinder = new LocalBinder();

    private Intent mIntent;

    private AccessToken facebookAccessToken;

    private com.parse.twitter.Twitter tw = ParseTwitterUtils.getTwitter();

    //   private Instagram instagram;

    private Long sinceId = 0L;

    private TripLocalOperations tripLocalOperations;

    public class LocalBinder extends Binder {
        public SocialSyncService getServerInstance() {
            return SocialSyncService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mIntent = intent;
        Log.i(TAG, "Social sync Service called");
        new SocialSyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_NOT_STICKY;
    }

    private class SocialSyncTask extends AsyncTask<Void, Void, Void> {

        private boolean stopTrackService;
        private TripSettings tripSettings;

        @Override
        protected Void doInBackground(Void... params) {

            Log.i(TAG, "Social sync task started");

            ServiceUtils.setSyncServiceStatus(true);

            if (ParseUser.getCurrentUser() == null) {
                Log.e(TAG, "No Logged in user found");
                stopTrackService = true;
                return null;
            }

            tripLocalOperations = (TripLocalOperations) TripUtils.getInstance().getCurrentTripOperations();

            Trip trip = tripLocalOperations.getTrip();

            if (trip == null) {
                Log.e(TAG, "No Current trip found");
                stopTrackService = true;
                return null;
            }

            if (trip.isFinished()) {
                Log.e(TAG, "Trip has been completed. Trip: " + trip.getName());
                stopTrackService = true;
                return null;
            }

            tripSettings = trip.getSettings();
            if (tripSettings == null) {
                Log.e(TAG, "No Current trip settings found");
                stopTrackService = true;
                return null;
            }

            if(!ServiceUtils.checkAndSetCameraSyncService()){
                return null;
            }
            Log.i(TAG,"Camera roll sync: " + tripSettings.isCameraRoll());
            if(tripSettings.isCameraRoll()){
                Boolean sdPresent = android.os.Environment.getExternalStorageState()
                        .equals(android.os.Environment.MEDIA_MOUNTED );
                if(sdPresent){
                    Log.i(TAG,"External storage in mounted state");
                    try{
                        Date cameraRollStartTime = tripSettings.getCameraRollSyncTime();
                        if(cameraRollStartTime == null)
                            cameraRollStartTime = trip.getStartTime();
                        Calendar cameraRollSyncTime = Calendar.getInstance();
                        Cursor[] cursors = MediaUtils.getImagesCursor(getContentResolver(), cameraRollStartTime);
                        MergeCursor cursor = new MergeCursor(cursors);
                        int size = cursor.getCount();
                        if(size > 0){
                            Set<String> fileLocalSet = new HashSet<>();
                            for(FileLocal  fileLocal : ParseFileUtils.storedGalleryImages()){
//                                fileLocalSet.add(fileLocal.getLocalUri());
                                fileLocalSet.add(fileLocal.getFileName());
                            }
                            MediaUtils mediaUtils = MediaUtils.newInstance(cursor);
                            List<GalleryImage> galleryImages = new ArrayList<>();
                            for (int i = 0; i < size; i++) {
                                cursor.moveToPosition(i);
                                GalleryImage galleryImage = mediaUtils.getGalleryImage(cursor);
                                if(mediaUtils.isCameraImage(galleryImage) && !fileLocalSet.contains(galleryImage.getName())){
                                    galleryImages.add(galleryImage);
                                }
                            }
                            cursor.close();

                            if(galleryImages.size() > 0)
                                tripLocalOperations.addPhotos(galleryImages);

                            tripSettings.setCameraRollSyncTime(cameraRollSyncTime.getTime());
                            tripLocalOperations.save(tripSettings);
                        }
                    }catch (Exception e){
                        Log.w(TAG,"Error syncing camera roll pictures",e);
                        DebugUtils.logException(e);
                    }
                }
            }

            Log.i(TAG, "Facebook setting: " + tripSettings.isFacebook());

            if (tripSettings.isFacebook()) {
                facebookAccessToken = AccessToken.getCurrentAccessToken();
                if (facebookAccessToken != null && !facebookAccessToken.isExpired()) {
                    try {
                        syncFacebook(tripSettings, trip);
                    }
                    catch(Exception e){
                        DebugUtils.logException(e);
                    }
                } else {
                    Log.w(TAG, "Facebook access token not found");
                }
            }

            Log.i(TAG, "Twitter setting: " + tripSettings.isTwitter());

            if (tripSettings.isTwitter()) {
                try {
                   if(ParseTwitterUtils.isLinked(ParseUser.getCurrentUser()))
                        syncTwitter(tripSettings, trip);
                } catch (Exception e) {
                    Log.e(TAG, "Twitter sync twitter crashing", e);
                    DebugUtils.logException(e);
                }
            } else {
                Log.w(TAG, "Twitter access token not found");
            }

            Log.i(TAG, "Instagram setting: " + tripSettings.isInstagram());

//            if(tripSettings.isInstagram()){
//                if(ParseInstagramUtils.isLinked(ParseUser.getCurrentUser())){
//                    try{
//                        instagram = ParseInstagramUtils.getInstagram();
//                        syncInstagram(tripSettings,trip);
//                    } catch (Exception e){
//                        DebugUtils.logException(e);
//                    }
//                }
//                else{
//                    Log.w(TAG, "Instagram access token not found");
//                }
//            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            if (stopTrackService) {
                Intent intent = new Intent(TripServiceStopManager.TRIP_SERVICE_STOP_BROADCAST_INTENT);
                sendBroadcast(intent);
                Log.i(TAG, "Sending Intent to stop trip services");
            }
            stopSelf();
            Log.i(TAG, "Stopping social sync service");
            SocialSyncServiceReceiver.completeWakefulIntent(mIntent);
            Intent updateIntent = new Intent(TripServiceManager.TRIP_UPDATE_BROADCAST_INTENT);
            sendBroadcast(updateIntent);
            Log.i(TAG, "Sending trip update broadcast");
            if(tripSettings != null && tripSettings.isSync()){
                Intent syncIntent = new Intent(UploadTripReceiver.SYNC_TRIP_INTENT);
                sendBroadcast(syncIntent);
            }
            else{
                ServiceUtils.setSyncServiceStatus(false);
                ServiceUtils.checkAndStartUploadTrip();
            }
            ServiceUtils.setCameraSyncService(false);
        }
    }

    private void syncFacebook(final TripSettings tripSettings, Trip trip) {
        Date date = tripSettings.getFacebookSyncTime();
        if (date == null)
            date = trip.getStartTime();
        Log.i(TAG, "Syncing facebook requests");
        final Calendar requestTime = Calendar.getInstance();
        GraphRequest graphRequest = GraphRequest.newGraphPathRequest(facebookAccessToken
                , FacebookUtils.getPostsUrl(), new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                Log.i(TAG, "Facebook posts Graph request complete");
                if (graphResponse.getError() == null) {
                    Log.i(TAG, "Facebook posts graph request success");
                    JSONObject jsonObject = graphResponse.getJSONObject();
                    savePosts(jsonObject);
                    String nextPage = FacebookUtils.getNextPageUrl(jsonObject);
                    if (nextPage != null) {
                        syncFacebookRecurring(nextPage);
                    }
                    tripSettings.setFacebookSyncTime(requestTime.getTime());
                    try {
                        tripLocalOperations.save(tripSettings);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error while updating facebook sync time", e);
                    }
                } else {
                    Log.e(TAG, "Facebook posts graph request failure: " + graphResponse.getError().getErrorMessage(), graphResponse.getError().getException());
                }

            }
        });

        graphRequest.setParameters(FacebookUtils.getPostsBundle(date));

        graphRequest.executeAndWait();
        return;
    }

    private void syncFacebookRecurring(String nextUrl) {
        GraphRequest graphRequest = GraphRequest.newGraphPathRequest(facebookAccessToken
                , nextUrl, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                if (graphResponse.getError() == null) {
                    JSONObject jsonObject = graphResponse.getJSONObject();
                    String nextPage = FacebookUtils.getNextPageUrl(jsonObject);
                    savePosts(jsonObject);
                    if (nextPage != null) {
                        syncFacebookRecurring(nextPage);
                    }
                }
            }
        });
        graphRequest.executeAndWait();
        return;
    }

    private void savePosts(JSONObject jsonObject) {
        List<FacebookPost> posts = FacebookUtils.getPosts(jsonObject);
        Log.i(TAG, "Facebook posts found: " + posts.size());
        List<FacebookPost> supportedPosts = new ArrayList<>();
        List<FacebookPost> photoPosts = new ArrayList<>();
        for (FacebookPost post : posts) {
            if (post.getType() == FacebookPost.Type.PHOTO) {
                photoPosts.add(post);
                supportedPosts.add(post);
            } else if (post.getType() == FacebookPost.Type.STATUS) {
                supportedPosts.add(post);
            }
        }

        List<GraphRequest> graphRequests = new ArrayList<>();
        for (int i = 0; i < photoPosts.size(); i++) {
            final FacebookPost photoPost = photoPosts.get(i);
            graphRequests.add(GraphRequest.newGraphPathRequest(facebookAccessToken,
                    FacebookUtils.getPostAttachments(photoPost.getId()), new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            if (graphResponse.getError() == null) {
                                photoPost.setMedia(FacebookUtils.getPostAttachments(graphResponse.getJSONObject()));
                            }
                        }
                    }));
        }
        if (graphRequests.size() > 0) {
            GraphRequestBatch requestBatch = new GraphRequestBatch(graphRequests);
            requestBatch.executeAndWait();
        }
        try {
            Log.i(TAG, "supported posts found: " + supportedPosts.size());
            if (supportedPosts.size() > 0)
                tripLocalOperations.addFacebookPosts(supportedPosts);
        } catch (ParseException e) {
            Log.e(TAG, "Error adding facebook posts", e);
        }
    }

    private void syncTwitter(final TripSettings tripSettings, Trip trip) throws twitter4j.TwitterException {

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
           twitter.setOAuthConsumer(tw.getConsumerKey(), tw.getConsumerSecret());
          twitter4j.auth.AccessToken accessToken = new twitter4j.auth.AccessToken(tw.getAuthToken(), tw.getAuthTokenSecret());
          twitter.setOAuthAccessToken(accessToken);
        sinceId = tripSettings.getTwitterSinceId();
        if (sinceId == 0L)
        {
            Paging paging = new Paging(1, 1);
            List<twitter4j.Status> tweet = twitter.getUserTimeline(paging);
            tripSettings.setTwitterSinceId(tweet.get(0).getId());
        }
        else {
            int totalTweets = 20; // no of tweets to be fetched
            Paging paging = new Paging(1, totalTweets);
            List<twitter4j.Status> tweets = twitter.getUserTimeline(paging);
            if(tweets.size() == 0)
                return;     //Zero Tweets in Twitter account.
            if (tweets.get(tweets.size()-1).getId() > sinceId){
                int page = 2, count = tweets.size();
                while (count == totalTweets) {
                    Paging pages = new Paging(page, totalTweets);
                    List<twitter4j.Status> pageTweets = twitter.getUserTimeline(pages);
                    if (pageTweets.get(pageTweets.size()-1).getId() > sinceId)
                        tweets.addAll(pageTweets);
                    else{
                        int limit = 0;
                        for (int i=0;i<pageTweets.size();i++){
                            if (sinceId >= pageTweets.get(i).getId()){
                                limit = i;
                                break;
                            }
                        }
                        if (limit !=0){
                            pageTweets = pageTweets.subList(0, limit);
                            tweets.addAll(pageTweets);
                        }
                        break;
                    }

                    count = pageTweets.size();
                    page++;
                }
            }
            else{
                int limit = 0;
                for (int i=0;i<tweets.size();i++){
                    if (sinceId >= tweets.get(i).getId()){
                        limit = i;
                        break;
                    }
                }
                if (limit !=0)
                    tweets = tweets.subList(0, limit);
                else
                    return;
            }
            Log.i(TAG, "twitter tweets request success");
            tripSettings.setTwitterSinceId(tweets.get(0).getId());
            if (tweets.size() > 0) {
                saveTweets(tweets);
                try {
                    tripLocalOperations.save(tripSettings);
                } catch (ParseException e) {
                    Log.e(TAG, "Error while updating twitter sync time", e);
                }
            }
        }
    }

    private void saveTweets(List<twitter4j.Status> tweets){
        Log.i(TAG, "Twitter tweets found: " + tweets.size());
        try {
            if (tweets.size() > 0)
                tripLocalOperations.addTwitterPosts(tweets);
        } catch (java.text.ParseException e) {
            Log.e(TAG, "Error adding twitter posts", e);
        } catch (ParseException e) {
            Log.e(TAG, "Error adding twitter posts", e);
        }
    }

//    private void syncInstagram(TripSettings tripSettings, Trip trip){
//        Date date = tripSettings.getInstagramSyncTime();
//        if (date == null)
//            date = trip.getStartTime();
//        Log.i(TAG, "Syncing instagram requests");
//        Calendar requestTime = Calendar.getInstance();
//        String instUrl = InstagramUtils.getUserPostsUrl(date,instagram.getAccessToken());
//        while (instUrl != null){
//            InputStream iStream = null;
//            HttpURLConnection urlConnection = null;
//            JSONObject jsonObject = null;
//            String nextUrl = null;
//            try{
//                Log.i(TAG, "Instagram Url: " + instUrl);
//                URL url = new URL(instUrl);
//
//                // Creating an http connection to communicate with url
//                urlConnection = (HttpURLConnection) url.openConnection();
//
//                // Connecting to url
//                urlConnection.connect();
//
//                // Reading data from url
//                iStream = urlConnection.getInputStream();
//
//                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//
//                StringBuffer sb  = new StringBuffer();
//
//                String line = "";
//                while( ( line = br.readLine())  != null){
//                    sb.append(line);
//                }
//
//                jsonObject = new JSONObject(sb.toString());
//                br.close();
//                List<InstagramPost> posts = InstagramUtils.getInstagramPosts(jsonObject);
//                Log.i(TAG,"Instagram posts found:" + posts.size());
//                tripLocalOperations.addInstagramPosts(InstagramUtils.getInstagramPosts(jsonObject));
//                if(jsonObject.has("pagination")){
//                    JSONObject pagination = jsonObject.getJSONObject("pagination");
//                    if(pagination.has("next_url")){
//                        nextUrl = pagination.getString("next_url");
//                    }
//                }
//            }catch(Exception e){
//                Log.e(TAG,"Error reading instagram posts", e);
//            }finally{
//                try{
//                    iStream.close();
//                    urlConnection.disconnect();
//                }
//                catch (Exception e){
//                }
//                instUrl = null;
//                if(nextUrl != null)
//                    instUrl = nextUrl;
//            }
//        }
//
//        tripSettings.setInstagramSyncTime(requestTime.getTime());
//        try {
//            tripLocalOperations.save(tripSettings);
//        } catch (ParseException e) {
//            Log.e(TAG, "Error while updating facebook sync time", e);
//        }
//    }
}