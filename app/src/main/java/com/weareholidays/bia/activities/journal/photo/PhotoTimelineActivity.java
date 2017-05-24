package com.weareholidays.bia.activities.journal.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParseException;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.activities.journal.people.AddPeopleActivity;
import com.weareholidays.bia.activities.journal.trip.ShareTripActivity;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.List;
import java.util.Locale;

public class PhotoTimelineActivity extends TripBaseActivity {

    private Media selectedMedia;
    private Timeline selectedTimeline;
    private TextView caption;
    private TextView locationText;
    private Menu mMenu;
    private List<Media> selectedMediaList;
    private ViewPager mPhotoPager;
    private TimelinePhotosPagerAdapter mPagerAdapter;
    private View bottomBar;
    private View locationPin;

    private static final int SHARE_PHOTO_REQUEST_CODE = 2132;

    @Override
    protected void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        setContentView(R.layout.activity_photo_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#000000"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setUp();
    }

    public void setUp() {
        selectedMedia = tripOperations.getSelectedMedia();
        selectedTimeline = tripOperations.getTimeLine();
        Album album = (Album) selectedTimeline.getContent();
        selectedMediaList = album.getMedia();
        if (selectedMediaList == null) {
            try {
                selectedMediaList = tripOperations.getAlbumMedia(album);
                tripOperations.populateMediaSource(selectedMediaList);
            } catch (ParseException e) {
            }
        }
        caption = (TextView) findViewById(R.id.name);
        locationText = (TextView) findViewById(R.id.locationText);
        bottomBar = findViewById(R.id.bottom_bar);
        locationPin = findViewById(R.id.pin);
        if (mMenu != null) {
            mMenu.clear();
            onCreateOptionsMenu(mMenu);
        }
        mPhotoPager = (ViewPager) findViewById(R.id.full_image_pager);
        mPagerAdapter = new TimelinePhotosPagerAdapter(this, selectedMediaList);
        mPhotoPager.setAdapter(mPagerAdapter);
        mPhotoPager.setCurrentItem(selectedMediaList.indexOf(selectedMedia));

        mPhotoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedMedia = selectedMediaList.get(position);
                tripOperations.setSelectedMedia(selectedMedia);
                onPhotoSelected();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        onPhotoSelected();
    }

    private void onPhotoSelected() {
        String captionText = selectedMedia.getCaption();
        String address = selectedMedia.getAddress();
        if (TextUtils.isEmpty(captionText) && selectedMedia.getLocation() == null) {
            bottomBar.setVisibility(View.INVISIBLE);
        } else {
            bottomBar.setVisibility(View.VISIBLE);
            caption.setText(captionText);
            locationText.setText(address);
            locationPin.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(address)) {
                if (selectedMedia.getLocation() != null) {
                    locationPin.setVisibility(View.VISIBLE);
                    locationText.setText(String.format("%.4f, %.4f", selectedMedia.getLocation().getLatitude(), selectedMedia.getLocation().getLongitude()));
                    if (ViewUtils.isNetworkAvailable(this) && !selectedMedia.isFetchingAddress())
                        new GetPhotoLocationTask(selectedTimeline, selectedMedia, tripOperations).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, selectedMedia);
                }
            }
            if (selectedMedia.getLocation() == null)
                locationPin.setVisibility(View.GONE);
        }
    }

    private class GetPhotoLocationTask extends AsyncTask<Media, Void, Void> {

        private Timeline timeline;
        private Media image;
        private TripOperations tripOperations;

        public GetPhotoLocationTask(Timeline timeline, Media image, TripOperations tripOperations) {
            this.timeline = timeline;
            this.image = image;
            this.tripOperations = tripOperations;
            image.setFetchingAddress(true);
        }

        /**
         * This task gets the address string for all pictures which have latitude and longitud
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Media... selectedImages) {
            /**
             * From android docs
             * Please refer https://developer.android.com/training/location/display-address.html
             */
            Geocoder geocoder = new Geocoder(PhotoTimelineActivity.this, Locale.getDefault());

            String imageAddress;
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(image.getLocation().getLatitude(), image.getLocation().getLongitude(), 1);
                System.out.println(addresses.size());
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String knownName = addresses.get(0).getFeatureName();
                    imageAddress = knownName + ", " + city + ", " + state;
                    Log.v("address", address + imageAddress);
                    tripOperations.saveSelectedMediaAddress(imageAddress, image, timeline);
                }

            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                image.setFetchingAddress(false);
                onPhotoSelected();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        if (tripOperations != null) {
            if (tripOperations.canWrite()) {
                if (tripOperations.getTrip().isFinished())
                    getMenuInflater().inflate(R.menu.menu_photo_timeline, menu);
                else
                    getMenuInflater().inflate(R.menu.menu_photo_timeline_unpublished, menu);
                return true;
            } else {
                getMenuInflater().inflate(R.menu.menu_photo_timeline_public, menu);
                return true;
            }
        }
        return false;
    }

    private void onPhotoDeleted(Media media) {
        if (tripOperations != null) {
            int removedIndex = selectedMediaList.indexOf(media);
            selectedMediaList.remove(media);
            int moveToIndex = removedIndex - 1;
            if (moveToIndex < 0)
                moveToIndex = 0;
            mPagerAdapter.notifyDataSetChanged();
            tripOperations.setSelectedMedia(selectedMediaList.get(moveToIndex));//set selected media to currently selected item on pager adapter
            mPhotoPager.setCurrentItem(moveToIndex, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.edit) {
            startActivity(NavigationUtils.getOpenPhotoEditIntent(this, tripOperations.getTripKey(), getIntent()));
            NavigationUtils.openAnimation(this);
            return true;
        }

        if (id == R.id.share) {
            if (selectedMedia.isPrivate()) {
                Toast.makeText(this, "Private Photos can't be shared", Toast.LENGTH_LONG);
                return true;
            }
            Intent myIntent = new Intent(getBaseContext(), ShareTripActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
            bundle.putString(AddPeopleActivity.SHARE_IMAGE_VIEW, "yes");
            myIntent.putExtras(bundle);
            startActivityForResult(myIntent, SHARE_PHOTO_REQUEST_CODE);
            return true;
        }

        if (id == R.id.details) {
            startActivity(NavigationUtils.getOpenPhotoInfoIntent(this, tripOperations.getTripKey(), getIntent()));
            NavigationUtils.openAnimation(this);
            return true;
        }

        if (id == R.id.delete) {
            new MaterialDialog.Builder(this)
                    .title(R.string.action_delete_photo)
                    .content("Are you sure you want to delete this image?")
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .positiveColor(getResources().getColor(R.color.orange_primary))
                    .negativeColor(getResources().getColor(R.color.orange_primary))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            new DeleteMediaTask(selectedTimeline, selectedMedia, tripOperations).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }).show();
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DeleteMediaTask extends AsyncTask<Void, Void, Void> {

        private CenterProgressDialog progressDialog;
        private boolean deleted;
        private boolean singlePhotoAlbum;
        private Timeline timeline;
        private Media media;
        private TripOperations tripOperations;


        public DeleteMediaTask(Timeline timeline, Media media, TripOperations tripOperations) {
            this.timeline = timeline;
            this.media = media;
            this.tripOperations = tripOperations;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressDialog = CenterProgressDialog.show(PhotoTimelineActivity.this, null, null, true);
            } catch (Exception e) {

            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (deleted) {
                    if (singlePhotoAlbum) {
                        onBackPressed();
                    } else {
                        onPhotoDeleted(media);
                    }
                } else {
                    Toast.makeText(PhotoTimelineActivity.this, "Unable to delete the selected photo", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            } catch (Exception e) {

            } finally {
                timeline = null;
                tripOperations = null;
                media = null;
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Album album = (Album) selectedTimeline.getContent();
                int albumCount = album.getPublicMediaCount();
                if (tripOperations.canWrite())
                    albumCount = album.getMediaCount();
                if (albumCount == 1) {
                    tripOperations.deleteTimeLine(timeline);
                    singlePhotoAlbum = true;
                } else {
                    tripOperations.deleteMedia(timeline, media);
                }
                deleted = true;
            } catch (Exception e) {
                DebugUtils.logException(e);
                deleted = false;
            }
            return null;
        }
    }

    public class TimelinePhotosPagerAdapter extends PagerAdapter {

        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private List<Media> mImages;

        public TimelinePhotosPagerAdapter(Context context, List<Media> images) {
            mContext = context;
            mImages = images;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.timeline_photos_pager, container, false);
            Media currentMedia = mImages.get(position);
            final ImageView fullImage = (ImageView) itemView.findViewById(R.id.full_image_view);
            double aspect = 0;
            if (currentMedia.getMediaHeight() != 0) {
                aspect = ((double) currentMedia.getMediaWidth()) / currentMedia.getMediaHeight();
            }
            ViewGroup.LayoutParams layoutParams = fullImage.getLayoutParams();
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            if (aspect != 0){
                height = (int)(width/aspect);
            }
            layoutParams.height = height;
            fullImage.setLayoutParams(layoutParams);

          /*  Picasso.with(mContext)
                    .load(currentMedia.getMediaSource())
                    .into(fullImage, new Callback() {
                        CenterProgressDialog pDialog = CenterProgressDialog.show(mContext, "Loading!!", null, false, true);

                        @Override
                        public void onSuccess() {
                            if(pDialog != null) {
                                pDialog.dismiss();
                                pDialog = null;
                            }
                        }

                        @Override
                        public void onError() {
                            if(pDialog != null) {
                                pDialog.dismiss();
                                pDialog = null;
                            }
                        }
                    });*/
            Glide.with(mContext)
                    .load(currentMedia.getMediaSource())
                    .asBitmap()
                    //.placeholder(R.drawable.image_loader)
                    .into(new SimpleTarget<Bitmap>(width, height) {
                        CenterProgressDialog pDialog = CenterProgressDialog.show(mContext, "Loading!!", null, false, true);

                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            //dismiss when image is loaded
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                                pDialog = null;
                            }
                            fullImage.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            super.onLoadCleared(placeholder);
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                                pDialog = null;
                            }
                        }

                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            if (pDialog != null) {
                                pDialog.setTitle("Loading!!");
                                pDialog.show(); //showing dialog since image loading is started
                            }
                        }
                    });

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }

    @Override
    public void onBackPressed() {
        if (NavigationUtils.handleBackNavigation(getIntent())) {
            startActivity(NavigationUtils.photoViewBackIntent(this, getIntent(), tripOperations.getTripKey(), selectedTimeline));
            NavigationUtils.closeAnimation(this);
            finish();
            return;
        }
        super.onBackPressed();
    }
}
