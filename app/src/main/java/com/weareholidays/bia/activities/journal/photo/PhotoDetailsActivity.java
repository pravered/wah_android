package com.weareholidays.bia.activities.journal.photo;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MapUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import wahCustomViews.view.WahImageView;


public class PhotoDetailsActivity extends TripBaseActivity {

    private Media selectedMedia;
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy hh:mm a");
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private TextView picAddress;

    @Override
    protected void onTripLoaded(Bundle savedInstanceState) {
        super.onTripLoaded(savedInstanceState);
        setContentView(R.layout.activity_photo_details);
        setup();
    }
   /* private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            final ImageView background = (ImageView) findViewById(R.id.background);
            Drawable drawable = new BitmapDrawable(getResources(), ViewUtils.blurfast(bitmap, 8));
            background.setImageDrawable(drawable);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };*/

    public void setup() {
        selectedMedia = tripOperations.getSelectedMedia();
        final ImageView background = (ImageView) findViewById(R.id.background);
        Glide.with(this)
                .load(selectedMedia.getMediaSource())
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(background){
                    protected void setResource(Bitmap resource) {
                        Drawable drawable = new BitmapDrawable(getResources(), ViewUtils.blurfast(resource, 8));
                        background.setImageDrawable(drawable);
                    }
                });

     /*   Picasso.with(this)
                .load(selectedMedia.getMediaSource())
                .centerCrop()
                .into(target);*/


        ParseCustomUser tripUser = tripOperations.getTrip().getOwner();
        if(tripOperations.canWrite())
            tripUser = ParseCustomUser.getCurrentUser();
        //set user image
        WahImageView userImage = (WahImageView) findViewById(R.id.user_image);
        String profileUrl = tripUser.getProfileUrl();
        if(!TextUtils.isEmpty(profileUrl)) {
            userImage.setImageUrl(profileUrl);
/*        Glide.with(this)
                .load(profileUrl)
                .crossFade()
                .centerCrop()
                .into(userImage);*/
        }
        // set user's name
        TextView userName = (TextView) findViewById(R.id.user_name);
        userName.setText(tripUser.getName());
        //set pic date
        TextView picDate = (TextView) findViewById(R.id.pic_date);
        if (selectedMedia.getContentCreatedDate() != null) {
            picDate.setText(simpleDateFormat.format(selectedMedia.getContentCreatedDate()));
        }
        //set pic address
        picAddress = (TextView) findViewById(R.id.location);
        if (selectedMedia.getAddress() != null && selectedMedia.getAddress().length() > 0) {
            picAddress.setText(selectedMedia.getAddress());
        }
        //set tags for the image
        TextView firstTag = (TextView) findViewById(R.id.first_tag);
        TextView secondTag = (TextView) findViewById(R.id.second_tag);
        TextView thirdTag = (TextView) findViewById(R.id.third_tag);
        TextView moreTags = (TextView) findViewById(R.id.more_tags);
        List<String> tags = selectedMedia.getTags();
        if (tags != null && tags.size() > 0) {
            if (tags.get(0).trim().length() > 0) {
                firstTag.setText(tags.get(0));
                firstTag.setVisibility(View.VISIBLE);
            }

            if (tags.size() > 1) {
                secondTag.setText(tags.get(1));
                secondTag.setVisibility(View.VISIBLE);

                if (tags.size() > 2) {
                    thirdTag.setText(tags.get(2));
                    thirdTag.setVisibility(View.VISIBLE);

                    if (tags.size() > 3) {
                        moreTags.setText("+" + String.valueOf(tags.size() - 3) + " more");
                        moreTags.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            ((TextView)findViewById(R.id.no_tags)).setVisibility(View.VISIBLE);
        }
        //add marker on map
        WahImageView map = (WahImageView) findViewById(R.id.location_map);
        if (selectedMedia.getLocation() != null) {
            double latitude = selectedMedia.getLocation().getLatitude();
            double longitude = selectedMedia.getLocation().getLongitude();
            Log.d("location", String.valueOf(latitude)+" "+String.valueOf(longitude));

            map.setImageUrl(MapUtils.getPhotoMapImageUrl(latitude,longitude));
            /*Glide.with(this)
                    .load(MapUtils.getPhotoMapImageUrl(latitude, longitude))
                            //.centerCrop()
                    .crossFade()
                    .into(map);*/

            //if no address string present, find address if internet is there else set latlng as address
            if (selectedMedia.getAddress() == null || selectedMedia.getAddress().length() == 0) {
                picAddress.setText(String.format("%.4f, %.4f", selectedMedia.getLocation().getLatitude(), selectedMedia.getLocation().getLongitude()));
                if (ViewUtils.isNetworkAvailable(this) && !selectedMedia.isFetchingAddress())
                    new GetPhotoLocationTask(tripOperations.getTimeLine(),selectedMedia,tripOperations).execute(selectedMedia);
            }
        } else {
            map.setVisibility(View.GONE);
        }
        /*
        mMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map));
        mMap = mMapFragment.getMap();
        if (selectedMedia.getLocation() != null) {
            LatLng position = new LatLng(selectedMedia.getLocation().getLatitude(), selectedMedia.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(position));
            //Build camera position
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)
                    .zoom(10).build();
            //Zoom in and animate the camera.
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //disable map dragging
            mMap.getUiSettings().setScrollGesturesEnabled(false);
            mMap.getUiSettings().setZoomGesturesEnabled(false);
        } else {
            mMapFragment.getView().setVisibility(View.GONE);
        }*/

        ImageView cross = (ImageView) findViewById(R.id.cancel);
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        /***
         * This task gets the address string for all pictures which have latitude and longitude
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Media... selectedImages) {
            /**
             * From android docs
             * Please refer https://developer.android.com/training/location/display-address.html
             */
            Geocoder geocoder = new Geocoder(PhotoDetailsActivity.this, Locale.getDefault());

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
                    tripOperations.saveSelectedMediaAddress(imageAddress,image,timeline);
                    image.setAddress(imageAddress);
                }

            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try{
                image.setFetchingAddress(false);
                if(!TextUtils.isEmpty(selectedMedia.getAddress()))
                    picAddress.setText(selectedMedia.getAddress());
            } catch (Exception e){

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(NavigationUtils.handleBackNavigation(getIntent()))
        {
            startActivity(NavigationUtils.getClosePhotoInfoIntent(this,tripOperations.getTripKey(),getIntent()));
            NavigationUtils.closeAnimation(this);
            finish();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
 //       Picasso.with(this).cancelRequest(target);
        super.onDestroy();
    }
}
