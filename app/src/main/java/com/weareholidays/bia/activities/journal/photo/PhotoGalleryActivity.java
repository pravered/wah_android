package com.weareholidays.bia.activities.journal.photo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.Manifest;
import com.weareholidays.bia.R;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.TripSettings;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.GPSTracker;
import com.weareholidays.bia.utils.MediaUtils;
import com.weareholidays.bia.widgets.SlidingTabLayout;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class PhotoGalleryActivity extends AppCompatActivity implements
        PhotosFromGalleryFragment.OnFragmentInteractionListener, FacebookPhotosFragment.OnFragmentInteractionListener,
        InstagramPhotosFragment.OnFragmentInteractionListener, GPSTracker.GPSListener{

    Button continueBtn;
    //swipe tabs
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private SlidingTabLayout tabLayout;
    private int NUM_PAGES = 3;
    private int dayOrder;

    public int GALLERY_TAB = 0;
    public int FACEBOOK_TAB = 1000;
    public int INSTAGRAM_TAB = 2000;
    private static final int CAMERA_REQUEST = 1010;
    private Uri fileUri;

    private TripOperations tripOperations;

    private Set<GalleryImage> selectedImages = new LinkedHashSet<>();

    private static final String FILE_URI_KEY = "FILE_URI_KEY";

    private static final String FILE_KEY = "FILE_KEY";
    private GPSTracker mGpsTracker;
    private Location mLocation;

    private static int cameraRequestCode = 100;
    private static boolean cameraPermittedAfterRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

                super.onCreate(savedInstanceState);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkCameraPermissions();
                }

                setContentView(R.layout.activity_photo_gallery);
                tripOperations = TripUtils.getInstance().getCurrentTripOperations();
                if (getIntent() != null) {
                    dayOrder = getIntent().getIntExtra(TripUtils.DAY_ORDER_FOR_INTENT, 0);
                }
                if (tripOperations.getSelectedPhotosList() != null) {
                    selectedImages.addAll(tripOperations.getSelectedPhotosList());
                }

                handlePagerItems();

                mPager = (ViewPager) findViewById(R.id.photo_pager);
                mPager.setOffscreenPageLimit(3);
                mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                mPager.setAdapter(mPagerAdapter);

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                //setting title and subtitle for top panel
                String tripName = tripOperations.getTrip().getName();
                if (tripName.length() > 21)
                    tripName = tripName.substring(0, 20) + "....";
                getSupportActionBar().setTitle(tripName);
                getSupportActionBar().setSubtitle("ADDING PHOTOS");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                //setting up swipe tabs
                mGpsTracker = new GPSTracker(this, this);

                tabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
                tabLayout.setDistributeEvenly(true);
                tabLayout.setCustomTabView(R.layout.tab_layout, R.id.tab_text);

                tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                    @Override
                    public int getIndicatorColor(int position) {
                        return getResources().getColor(R.color.white);
                    }
                });
                tabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

                    @Override
                    public void onPageSelected(int position) {
                        if (tabLayout != null) {
                            resetTabs();
                            View view = tabLayout.getTabView(position);
                            if (view != null)
                                ((TextView) view.findViewById(R.id.tab_text)).setText(mPagerAdapter.getSelectedPageTitle(position));
                        }
                    }
                });
                tabLayout.setViewPager(mPager);
                handleTabs();

                continueBtn = (Button) findViewById(R.id.continue_btn);
                continueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent;
                        boolean flag = false;
                        if (selectedImages.size() > 0) {
                            flag = true;
                            intent = new Intent(PhotoGalleryActivity.this, SelectedPhotoActivity.class);
                            intent.putExtra(TripUtils.DAY_ORDER_FOR_INTENT, dayOrder);
                            tripOperations.setSelectedPhotosList(new ArrayList<>(selectedImages));
                            tripOperations.setSelectedPhoto(tripOperations.getSelectedPhotosList().get(0));
                            startActivity(intent);
                        }
                        if (!flag) {
//                            Toast.makeText(getApplicationContext(), "You have not made any selections", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, cameraRequestCode);
            }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == cameraRequestCode) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermittedAfterRequest = true;
//                Toast.makeText(this, "Camera allowed", Toast.LENGTH_SHORT).show();
            }
            else {
//                Toast.makeText(this, "Camera denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        mGpsTracker.stopUsingGPS();
        mGpsTracker = null;
        super.onDestroy();
    }

    private void handlePagerItems() {
        TripSettings tripSettings = tripOperations.getTrip().getSettings();
        int num_pages = 1;
        if(tripSettings.isFacebook()){
            FACEBOOK_TAB = num_pages;
            num_pages++;
        }
        if(tripSettings.isInstagram()){
            INSTAGRAM_TAB = num_pages;
            num_pages++;
        }
        NUM_PAGES = num_pages;
    }

    private void handleTabs() {
        resetTabs();
        View view = tabLayout.getTabView(0);
        if(view != null){
            ((TextView) view.findViewById(R.id.tab_text)).setText(mPagerAdapter.getSelectedPageTitle(0));
        }
    }

    private void resetTabs() {
        if(tabLayout != null){
            for(int i=0; i < mPagerAdapter.getCount(); i++){
                View view = tabLayout.getTabView(i);
                if(view != null){
                    ((TextView) view.findViewById(R.id.tab_text)).setText(mPagerAdapter.getPageTitle(i));
                }
            }
        }
    }

    @Override
    public void onImageSelected(GalleryImage galleryImage) {
        if(galleryImage.isSelected()){
            selectedImages.add(galleryImage);
        }
        else{
            selectedImages.remove(galleryImage);
        }
        String subtitle = String.format("ADDING %d PHOTOS", selectedImages.size());
        getSupportActionBar().setSubtitle(subtitle);
    }

    public File mFile;

    @Override
    public void takePicture() {
        try {
            // create Intent to take a picture and return control to the calling application
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            mFile = MediaUtils.getMediaImageStoreLocation();
            fileUri = Uri.fromFile(mFile); // create a file to save the image
            //fileUri = MediaUtils.getOutputMediaFileUri(MediaUtils.MEDIA_TYPE_IMAGE); // create a file to save the image
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            // start the image capture Intent
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't load camera at this moment", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(fileUri != null)
            outState.putParcelable(FILE_URI_KEY,fileUri);
        if(mFile != null)
            outState.putSerializable(FILE_KEY,mFile);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey(FILE_URI_KEY))
            fileUri = savedInstanceState.getParcelable(FILE_URI_KEY);
        if(savedInstanceState.containsKey(FILE_KEY))
            mFile = (File) savedInstanceState.getSerializable(FILE_KEY);
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && fileUri != null) {
            if (requestCode == CAMERA_REQUEST) {
                GalleryImage galleryImage;
                //add image to gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(fileUri);
                this.sendBroadcast(mediaScanIntent);

                BitmapFactory.Options bitmapOptions = ParseFileUtils.decodeBitMapOptions(fileUri.toString());

                //set all the details for that image
                galleryImage = new GalleryImage(mFile.getAbsolutePath());
                galleryImage.setType(GalleryImage.Type.PHONE);
                galleryImage.setDateTaken(System.currentTimeMillis());
                LatLng latLng = MediaUtils.getLatLangFromFile(mFile.getAbsolutePath());
                if(latLng != null){
                    galleryImage.setLatitude(latLng.latitude);
                    galleryImage.setLongitude(latLng.longitude);
                } else {
                    if(mLocation != null){
                        galleryImage.setLatitude(mLocation.getLatitude());
                        galleryImage.setLongitude(mLocation.getLongitude());
                    }
                }
                try {
                    int height = bitmapOptions.outHeight;
                    int width = bitmapOptions.outWidth;
                    galleryImage.setMediaHeight(height);
                    galleryImage.setMediaWidth(width);
                } catch (NullPointerException e) {
                    DebugUtils.logException(e);
                }
                galleryImage.setSelected(true);
                selectedImages.add(galleryImage);
                //add to selected images
                tripOperations.setSelectedPhotosList(new ArrayList<>(selectedImages));
                tripOperations.setSelectedPhoto(galleryImage);
                Intent i = new Intent(this, SelectedPhotoActivity.class);
                //this is passed to identify whether selected photo activity was reached after opening camera or not
                i.putExtra("FROM_CAMERA", true );
                startActivity(i);
                finish();

            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
//            Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGPSLocationChanged(Location location) {
            mLocation = location;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if(position == GALLERY_TAB){
                return Fragment.instantiate(PhotoGalleryActivity.this,
                        PhotosFromGalleryFragment.class.getName());
            }

            if(position == FACEBOOK_TAB){
                Bundle bundle = new Bundle();
                bundle.putSerializable(FacebookPhotosFragment.DATE_ARG,tripOperations.getTrip().getStartTime());
                return Fragment.instantiate(PhotoGalleryActivity.this,
                        FacebookPhotosFragment.class.getName(),bundle);
            }

            if(position == INSTAGRAM_TAB){
                Bundle bundle = new Bundle();
                bundle.putSerializable(FacebookPhotosFragment.DATE_ARG,tripOperations.getTrip().getStartTime());
                return Fragment.instantiate(PhotoGalleryActivity.this,
                        InstagramPhotosFragment.class.getName(),bundle);
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == GALLERY_TAB){
                return getString(R.string.gallery);
            }

            if(position == FACEBOOK_TAB){
                return getString(R.string.facebook);
            }

            if(position == INSTAGRAM_TAB){
                return getString(R.string.instagram);
            }
            return null;
        }

        public CharSequence getSelectedPageTitle(int position){
            if(position == GALLERY_TAB){
                return Html.fromHtml("<b>Gallery</b>");
            }

            if(position == FACEBOOK_TAB){
                return Html.fromHtml("<b>Facebook</b>");
            }

            if(position == INSTAGRAM_TAB){
                return Html.fromHtml("<b>Instagram</b>");
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        tripOperations.setSelectedPhotosList(null);
        finish();
    }
}
