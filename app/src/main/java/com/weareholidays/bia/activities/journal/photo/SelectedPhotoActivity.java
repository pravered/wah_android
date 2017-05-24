package com.weareholidays.bia.activities.journal.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.journal.photo.view.PhotosRecyclerAdapter;
import com.weareholidays.bia.activities.journal.trip.TripFragment;
import com.weareholidays.bia.adapters.PhotosPagerAdapter;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SelectedPhotoActivity extends AppCompatActivity implements PhotosRecyclerAdapter.OnInteractionListener {

    List<GalleryImage> selected_pics;
    private CenterProgressDialog progressDialog;
    private EditText caption;
    private TextView pic_location;
    private TextView picDateTaken;
    private int imageWidth;
    private ViewPager mPhotoPager;
    private PhotosPagerAdapter mPagerAdapter;

    private TripOperations tripOperations;
    private GalleryImage currentSelectedPhoto;
    private RecyclerView selectedImageView;
    LinearLayoutManager recyclerManager;
    private PhotosRecyclerAdapter photosAdapter;
    List<GalleryImage> selected_pics_recyler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_photo);

        tripOperations = TripUtils.getInstance().getCurrentTripOperations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //set width as 1/4th of screen width
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        imageWidth = size.x / 4;

        selected_pics = tripOperations.getSelectedPhotosList();
        /*
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("FROM_CAMERA", false)) {
                //if the activity was started from camera, add the clicked picture to selected pics
                GalleryImage galleryImage;
                MergeCursor cursor = null;
                try {
                    Cursor[] cursor_array = MediaUtils.getImagesCursor(getContentResolver(),
                            tripOperations.getTrip().getStartTime());
                    cursor = new MergeCursor(cursor_array);
                    MediaUtils mediaUtils = MediaUtils.newInstance(cursor);
                    // if the cursor is not empty
                    if (cursor.moveToFirst()) {
                        galleryImage = mediaUtils.getGalleryImage(cursor);
                        galleryImage.setSelected(true);
                        selected_pics.add(galleryImage);
                        //add to selected images
                        tripOperations.setSelectedPhotosList(selected_pics);
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
        }
        */
        new GetPhotoLocationTask().execute(selected_pics);

        selected_pics_recyler = new ArrayList<>();

        selected_pics_recyler.add(new GalleryImage(true));

        selected_pics_recyler.addAll(selected_pics);

        selectedImageView = (RecyclerView) findViewById(R.id.my_recycler_view);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selectedImageView.getLayoutParams();
        params.height = imageWidth;
        selectedImageView.setLayoutParams(params);
        recyclerManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        selectedImageView.setLayoutManager(recyclerManager);

        photosAdapter = new PhotosRecyclerAdapter(this, selected_pics_recyler, this);
        selectedImageView.setAdapter(photosAdapter);

        if (tripOperations.getSelectedPhoto() != null) {
            currentSelectedPhoto = tripOperations.getSelectedPhoto();
        } else {
            currentSelectedPhoto = selected_pics.get(0);
        }
        tripOperations.setSelectedPhoto(currentSelectedPhoto);
        currentSelectedPhoto.setCurrentSelection(true);

        mPhotoPager = (ViewPager) findViewById(R.id.full_image_pager);
        mPagerAdapter = new PhotosPagerAdapter(this, selected_pics);
        mPhotoPager.setAdapter(mPagerAdapter);
        //in case of returning from edit photo details, we need to set the page at correct position
        mPhotoPager.setCurrentItem(selected_pics.indexOf(currentSelectedPhoto));
        onPhotoSelected();

        mPhotoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //nothing to do here
            }

            @Override
            public void onPageSelected(int position) {
                itemClicked(selected_pics.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //nothing to do here
            }
        });

        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSelectedPhoto.setCurrentSelection(false);
                pressCancelorBack();
                finish();

            }
        });

        findViewById(R.id.button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhotos();
            }
        });

        //currentSelectedPhoto.setCurrentSelection(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                photosAdapter.notifyDataSetChanged();
            }
        }, 500);
    }

    public void pressCancelorBack() {
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("FROM_CAMERA", false)) {
                //if the activity was started from camera, reload the photo gallery activity on pressing cancel
                Intent mIntent = new Intent(SelectedPhotoActivity.this, PhotoGalleryActivity.class);
                startActivity(mIntent);
            }
        }
    }

    private void onPhotoSelected() {
        //set pic caption if it exists
        caption = (EditText) findViewById(R.id.caption_back);
        caption.setText(currentSelectedPhoto.getCaption());
        //set pic location if it exists
        setPhotoLocation(currentSelectedPhoto);
        //set date taken for the image
        picDateTaken = (TextView) findViewById(R.id.image_clicked_text);
        setPhotoDate(currentSelectedPhoto);
    }

    private void setPhotoLocation(GalleryImage galleryImage) {
        pic_location = (TextView) findViewById(R.id.location_back);
        if (!TextUtils.isEmpty(galleryImage.getAddress())) {
            pic_location.setText(galleryImage.getAddress());
            pic_location.setVisibility(View.VISIBLE);
        } else if (!(galleryImage.getLatitude() == 0.0 && galleryImage.getLongitude() == 0.0)) {
            pic_location.setText(String.format("%.4f, %.4f", galleryImage.getLatitude(), galleryImage.getLongitude()));
            pic_location.setVisibility(View.VISIBLE);
        } else {
            pic_location.setText("");
            pic_location.setVisibility(View.INVISIBLE);
        }
    }

    private void setPhotoDate(GalleryImage galleryImage) {
        if (galleryImage.getDateTaken() > 0) {
            String dateText = (DateUtils.getRelativeTimeSpanString(galleryImage.getDateTaken(),
                                Calendar.getInstance().getTimeInMillis(), DateUtils.SECOND_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL)).toString();
            String prefix = "This photo was clicked on ";
            if (dateText.contains("ago")) {
                prefix = "This photo was clicked ";
            } else if (dateText.toLowerCase().contains("today") || dateText.toLowerCase().contains("yesterday")) {
                prefix = "This photo was clicked ";
                dateText = dateText.toLowerCase();
            }
            picDateTaken.setText(prefix + dateText);
        }
        else{
            picDateTaken.setText("");
        }
    }

    private void savePhotos(){
        progressDialog = CenterProgressDialog.show(this,null,null,true,false);
        //progressDialog.show();
        // caption of current pic may have been edited but not saved
        String caption = ((EditText) findViewById(R.id.caption_back)).getText().toString();
        this.selected_pics.get(this.selected_pics.indexOf(this.currentSelectedPhoto)).setCaption(caption);
        new PhotoSaveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selected_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //listener for edit menu button
        if (id == R.id.action_edit) {
            pressEditButton();
        }

        //listener for delete menu icon
        if (id == R.id.action_delete) {
            //removes checkbox from previous activity
            currentSelectedPhoto.setSelected(false);
            currentSelectedPhoto.setCurrentSelection(false);

            //remove the image from both the arraylist and the scroll view
            selected_pics.remove(currentSelectedPhoto);
            selected_pics_recyler.remove(currentSelectedPhoto);
            //reset the list in tripOperations
            tripOperations.setSelectedPhotosList(selected_pics);

            photosAdapter.notifyDataSetChanged();
            //ViewPager doesn't have a delete method; the closest is to set the adapter
            // again.  When doing so, it deletes all its views.  Then we can delete the view
            // from from the adapter and finally set the adapter to the pager again.  Note
            // that we set the adapter to null before removing the view from "views" - that's
            // because while ViewPager deletes all its views, it will call destroyItem which
            // will in turn cause a null pointer ref.
            mPhotoPager.setAdapter(null);
            mPagerAdapter.notifyDataSetChanged();
            mPhotoPager.setAdapter(mPagerAdapter);
            //then reset currentSelection to 0
            if(selected_pics.size() > 0) {
                currentSelectedPhoto = selected_pics.get(0);
                currentSelectedPhoto.setCurrentSelection(true);
                recyclerManager.scrollToPosition(0);
                mPhotoPager.setCurrentItem(selected_pics.indexOf(currentSelectedPhoto));
                //gets blue border
                onPhotoSelected();
            }
            else{
                Intent i = new Intent(this, PhotoGalleryActivity.class);
                startActivity(i);
                finish();
            }
        }

        if(id == android.R.id.home){
            pressCancelorBack();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void pressEditButton() {
        tripOperations.setSelectedPhoto(currentSelectedPhoto);
        Intent mIntent = new Intent(this, EditPhotoDetailsActivity.class);
        String currentImgCaption = ((EditText) findViewById(R.id.caption_back)).getText().toString();
        mIntent.putExtra("caption", currentImgCaption);
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("FROM_CAMERA", false)) {
                //if the activity was started from camera, tell edit screen about it too
                mIntent.putExtra("FROM_CAMERA", true);
            }
        }
        startActivity(mIntent);
        finish();
    }

    /**
    View insertPhoto(final GalleryImage galleryImage) {
        Log.d("image", String.valueOf(imageWidth));
        final LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));
        layout.setPadding(5, 10, 5, 10);
        layout.setGravity(Gravity.CENTER);

        final ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(this)
                .load(galleryImage.getUri())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageView.setImageBitmap(resource);
                    }
                });
        imageView.setTag(galleryImage);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //capture previous image's caption
                EditText caption = (EditText) findViewById(R.id.caption_back);
                String prev_caption = caption.getText().toString();
                //set it to the previous image
                //note that currentSelectedPhoto will be the prev image since we are resetting it later
                currentSelectedPhoto.setCaption(prev_caption);

                GalleryImage gImage = (GalleryImage) v.getTag();
                //reset current selection
                currentSelectedPhoto = gImage;
                final ImageView full_image = (ImageView) findViewById(R.id.full_image_view);
                Glide.with(SelectedPhotoActivity.this)
                        .load(galleryImage.getUri())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new SimpleTarget<Bitmap>(300, 500) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                full_image.setImageBitmap(resource);
                            }
                        });
                full_image.setTag(gImage);
                //set the new caption as per new image
                caption.setText(currentSelectedPhoto.getCaption());
                //set the new image's location
                TextView pic_location = (TextView) findViewById(R.id.location_back);
                if (currentSelectedPhoto.getAddress().length() > 0) {
                    pic_location.setText(currentSelectedPhoto.getAddress());
                    pic_location.setVisibility(View.VISIBLE);
                } else {
                    pic_location.setVisibility(View.INVISIBLE);
                }
                TextView textView = (TextView) findViewById(R.id.image_clicked_text);
                if (gImage.getDateTaken() > 0) {
                    textView.setText("This photo was clicked on " + DateUtils.getRelativeTimeSpanString(gImage.getDateTaken()
                            , Calendar.getInstance().getTimeInMillis(), DateUtils.SECOND_IN_MILLIS
                            , DateUtils.FORMAT_ABBREV_ALL));
                } else{
                    textView.setText("");
                }
            }
            });
        layout.addView(imageView);
        return layout;
    }
     **/

    @Override
    public void itemClicked(GalleryImage galleryImage) {
        EditText caption = (EditText) findViewById(R.id.caption_back);
        String prev_caption = caption.getText().toString();
        //set it to the previous image
        //note that currentSelectedPhoto will be the prev image since we are resetting it later
        currentSelectedPhoto.setCaption(prev_caption);

        currentSelectedPhoto.setCurrentSelection(false);

        if(galleryImage.isAddPhotoPlaceholder()){
            pressCancelorBack();
            finish();
            return;
        }

        currentSelectedPhoto = galleryImage;
        currentSelectedPhoto.setCurrentSelection(true);
        //this selects the photo inside the recycler view when the photo is changed from view pager
        photosAdapter.notifyDataSetChanged();
        onPhotoSelected();
        recyclerManager.scrollToPosition(selected_pics_recyler.indexOf(galleryImage));
        //this changes the view pager position when the photo is changed from recycler view
        mPhotoPager.setCurrentItem(selected_pics.indexOf(galleryImage));
    }

    private class GetPhotoLocationTask extends AsyncTask<List<GalleryImage>, Void, Void> {
        /***
         * This task gets the address string for all pictures which have latitude and longitude
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(List<GalleryImage>... selectedImages) {
            /*
             * From android docs
             * Please refer https://developer.android.com/training/location/display-address.html
             */
            Geocoder geocoder = new Geocoder(SelectedPhotoActivity.this, Locale.getDefault());
            List<GalleryImage> images = selectedImages[0];
            for (GalleryImage image : images) {
                if ((image.getLatitude() != 0.0 || image.getLongitude() != 0.0) && image.getAddress().length() == 0) {
                    String imageAddress;
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(image.getLatitude(), image.getLongitude(), 1);
                        System.out.println(addresses.size());
                        if (addresses.size() > 0) {
                            String address = addresses.get(0).getAddressLine(0);
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String knownName = addresses.get(0).getFeatureName();
                            imageAddress = knownName + ", " + city + ", " + state;
                            Log.v("address", address + imageAddress);
                            image.setAddress(imageAddress);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            photosAdapter.notifyDataSetChanged();
            onPhotoSelected();
        }
    }

    private class PhotoSaveTask extends AsyncTask<Void,Void,Void>{

        private boolean saved = false;
        private Timeline timeline;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                saved = true;
                timeline = ((TripLocalOperations)tripOperations).addPhotos(selected_pics);
            } catch (ParseException e) {
                Log.e(SelectedPhotoActivity.class.getName(),"Error saving photos",e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
//            if(progressDialog != null){
//                progressDialog.dismiss();
//            }
            if(saved)
                next(timeline);
            else{
                Toast.makeText(SelectedPhotoActivity.this, getString(R.string.toast_add_photo_error), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void next(Timeline timeline){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.SHOW_TAB, HomeActivity.JOURNAL_TAB);
        if(timeline != null){
            intent.putExtra(TripFragment.SHOW_JOURNAL_DAY, timeline.getDayOrder()+1);
            intent.putExtra(TripFragment.TIMELINE_SCROLL_POSITION, timeline.getDisplayOrder());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        tripOperations.setSelectedPhotosList(null);
        finish();
    }

}


