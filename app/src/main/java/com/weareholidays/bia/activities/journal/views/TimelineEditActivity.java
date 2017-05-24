package com.weareholidays.bia.activities.journal.views;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.activities.journal.base.TripBaseActivity;
import com.weareholidays.bia.adapters.DefaultListAdapter;
import com.weareholidays.bia.adapters.DemoItem;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.CheckIn;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Note;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.bumptech.glide.Glide;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridView;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridViewAdapter;
import com.parse.ParseException;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import wahCustomViews.view.WahImageView;

public class TimelineEditActivity extends TripBaseActivity {

    private static final int NOTE_VIEW = 1;
    private static final int IMAGE_VIEW = 2;
    private static final int GRID_VIEW = 3;
    private static final int CHECK_IN_WITH_IMAGE_VIEW = 4;
    private Timeline timeline;
    private TextView headText;
    private ImageView smallMark;
    private TextView locationText;
    private ImageView smallDot;
    private ImageView dispImage;
    private int viewType;
    public TextView time;
    public List<Media> mediaList;
    public ImageView menuButton;
    public WahImageView oneImage;
    public AsymmetricGridView agv;
    private WahImageView checkInImage;
    private AsymmetricGridView listView;
    public Album album;
    public CheckIn checkIn;
    public Note note;
    public String placeText;
    public TextView countryText;
    public DefaultListAdapter adapter;
    public String location = "";
    private boolean isLocation = false;
    private DisplayMetrics displayMetrics;
    private boolean isCheckIn = false;
    private TextView imageLocation;
    AsymmetricGridViewAdapter mAsymmetricAdapter;
    private Button mDeleteImageButton;

    @Override
    public void onTripLoaded(Bundle savedInstanceState) {
        int resourceId = 0;
        String titleString;

        super.onTripLoaded(savedInstanceState);
        timeline = tripOperations.getTimeLine();

        try {
            if (Timeline.CHECK_IN_CONTENT.equals(timeline.getContentType())) {
                resourceId = R.layout.checkin_with_image;
                viewType = CHECK_IN_WITH_IMAGE_VIEW;
            } else if (!Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
                resourceId = R.layout.note_timeline;
                viewType = NOTE_VIEW;
            } else if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
                album = (Album) timeline.getContent();
                mediaList = new ArrayList<>();
                try {
                    mediaList = tripOperations.getAlbumMedia(album);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (mediaList.size() == 1) {
                    resourceId = R.layout.photo_with_image;
                    viewType = IMAGE_VIEW;
                } else {
                    resourceId = R.layout.photo_with_grid_edit;
                    viewType = GRID_VIEW;
                }
            }

            String source = timeline.getSource();
            String contentType = timeline.getContentType();
            if (Source.FB.equals(source)) {
                titleString = "Facebook Post";
                this.setTheme(R.style.WAH_Theme_Facebook);
            } else if (Source.INSTAGRAM.equals(source)) {
                titleString = "Instagram Post";
                this.setTheme(R.style.WAH_Theme_Instagram);
            } else if (Source.TWITTER.equals(source)) {
                titleString = "Twitter Post";
                this.setTheme(R.style.WAH_Theme_Twitter);
            } else {
                if (Timeline.ALBUM_CONTENT.equals(contentType)) {
                    titleString = "Album";
                    try {
                        mediaList = tripOperations.getAlbumMedia(album);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (mediaList.size() > 1) {
                        this.setTheme(R.style.WAH_Theme_Photo);
                    }
                } else if (Timeline.CHECK_IN_CONTENT.equals(contentType)) {
                    titleString = "Check In";
                    this.setTheme(R.style.WAH_Theme);
                } else {
                    titleString = "Note";
                    this.setTheme(R.style.WAH_Theme);
                }
            }
            setContentView(R.layout.activity_timeline_edit);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            LinearLayout item = (LinearLayout) findViewById(R.id.item);
            View child = getLayoutInflater().inflate(resourceId, null);
            item.addView(child);
            getSupportActionBar().setTitle(titleString);
            String tripName = tripOperations.getTrip().getName();
            if (tripName.length() > 21)
                tripName = tripName.substring(0, 20) + "....";
            getSupportActionBar().setSubtitle(tripName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setUp();
        } catch (Exception e) {
            DebugUtils.logException(e);
        }
    }

    public void setUp() {
        headText = (TextView) findViewById(R.id.placeText);
        smallMark = (ImageView) findViewById(R.id.small_mark);
        locationText = (TextView) findViewById(R.id.location_text);
        dispImage = (ImageView) findViewById(R.id.disp_image);
        countryText = (TextView) findViewById(R.id.country_text);
        menuButton = (ImageView) findViewById(R.id.menuButton);
        menuButton.setVisibility(View.GONE);
        if (viewType == IMAGE_VIEW) {
            oneImage = (WahImageView) findViewById(R.id.photo_image);
            holderViewTextHelper(timeline);
            viewHolderSingleImageHelper();
        }
        if (viewType == GRID_VIEW) {
            agv = (AsymmetricGridView) findViewById(R.id.asymmetric_grid_view);
            listView = (AsymmetricGridView) agv.findViewById(R.id.asymmetric_grid_view);
            mDeleteImageButton = (Button) findViewById(R.id.delete_button);
            holderViewTextHelper(timeline);
            viewHolderGridImageHelper();
        }
        if (viewType == CHECK_IN_WITH_IMAGE_VIEW) {
            isCheckIn = true;
            checkInImage = (WahImageView) findViewById(R.id.checkin_image);
            imageLocation = (TextView) findViewById(R.id.location_text);
            holderViewTextHelper(timeline);
            viewHolderCheckinImageHelper();
        } else {
            holderViewTextHelper(timeline);
        }
        try {
            Timeline timeline = tripOperations.getTimeLine();
            if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
                Album album = (Album) timeline.getContent();
                int albumCount = album.getPublicMediaCount();
                if (tripOperations.canWrite())
                    albumCount = album.getMediaCount();
                if (albumCount == 1) {
                    List<Media> mediaList = tripOperations.getAlbumMedia(album);
                    Media media = mediaList.get(0);
                    tripOperations.setSelectedMedia(media);
//                    Intent myIntent = new Intent(getBaseContext(), PhotoTimelineActivity.class);
//                    myIntent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
                    startActivity(NavigationUtils.getPhotoViewIntent(this, tripOperations.getTripKey(), getIntent()));
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void holderViewTextHelper(Timeline timeline) {
        isLocation = false;
        isCheckIn = false;
        boolean fbCheckin = false;
        if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
            location = album.getLocationText();
            album = (Album) timeline.getContent();
            placeText = album.getContent();
            if (Source.WAH.equals(timeline.getSource())) {
                dispImage.setImageResource(R.drawable.timeline_photo);
                if ("".equals(location)) {
                    isLocation = true;
                }
                if (!"".equals(location)) {
                    placeText = location;
                    isLocation = true;
                } else if (album.getCategory() == null) {
                    dispImage.setImageResource(R.drawable.timeline_location);
                } else if (album.getCategory().contains("restaurant"))
                    dispImage.setImageResource(R.drawable.timeline_restaurant);
                else if (album.getCategory().contains("museum"))
                    dispImage.setImageResource(R.drawable.museum);
                else if (album.getCategory().contains("mosque"))
                    dispImage.setImageResource(R.drawable.timeline_landmark);
                else
                    dispImage.setImageResource(R.drawable.timeline_location);

            }

        } else if (Timeline.CHECK_IN_CONTENT.equals(timeline.getContentType())) {
            dispImage.setImageResource(R.drawable.timeline_checkin);
            checkIn = (CheckIn) timeline.getContent();
            location = checkIn.getName();
            placeText = "Check In";
            isCheckIn = true;
            if (Source.TWITTER.equals(timeline.getSource()))
                location = checkIn.getLocationText();
            if (Source.FB.equals(timeline.getSource())) {
                fbCheckin = true;
                dispImage.setImageResource(R.drawable.checkin_fb);
            }
        } else if (Timeline.NOTE_CONTENT.equals(timeline.getContentType())) {
            dispImage.setImageResource(R.drawable.timeline_notes);
            note = (Note) timeline.getContent();
            location = note.getLocationText();
            placeText = note.getContent();
        }
        if (!fbCheckin && Source.FB.equals(timeline.getSource())) {
            dispImage.setImageResource(R.drawable.timeline_facebook);
        } else if (Source.TWITTER.equals(timeline.getSource()))
            dispImage.setImageResource(R.drawable.timeline_twitter);
        else if (Source.INSTAGRAM.equals(timeline.getSource()))
            dispImage.setImageResource(R.drawable.timeline_instagram);

        if (location == null || "".equals(location) || isLocation) {
            smallMark.setVisibility(View.GONE);
        } else {
            if (smallMark != null)
                smallMark.setVisibility(View.VISIBLE);
        }


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeline.getContentTime());
        int hours = calendar.get(Calendar.HOUR_OF_DAY) % 12;
        int minutes = calendar.get(Calendar.MINUTE);
        int amPm = calendar.get(Calendar.AM_PM);
        String minuteText = "";
        String dayText;
        String hourText = "";
        if (amPm == 0)
            dayText = "am";
        else
            dayText = "pm";

        if (minutes < 10)
            minuteText = "0";
        if (hours < 10)
            hourText = "0";
        String setTime = hourText + hours + ":" + minuteText + minutes + " " + dayText;

        if (isLocation && "".equals(location)) {
            placeText = setTime;
        } else if (isLocation) {
            location = setTime;
            locationText.setText(location);
        } else if (!"".equals(location) && location != null && !isCheckIn) {
            location += "   • " + setTime;
            locationText.setText(location);
        } else if (location == null || "".equals(location)) {
            location = setTime;
            locationText.setText(location);
        }
        if (isCheckIn) {
            smallMark.setVisibility(View.GONE);
            locationText.setText(setTime);
            imageLocation.setText(location);

        }
        headText.setText(placeText);
    }

    public void viewHolderSingleImageHelper() {
        final Media media = mediaList.get(0);
        double aspect = ((double) media.getMediaWidth()) / media.getMediaHeight();
        ViewGroup.LayoutParams layoutParams = oneImage.getLayoutParams();
        displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        layoutParams.height = (int) (width / aspect);

        oneImage.setLayoutParams(layoutParams);
        //Glide.with(this).load(media.getMediaSource()).into(oneImage);
        oneImage.setImageUrl(media.getMediaSource());

    }

    ArrayList<DemoItem> items = new ArrayList<>();

    public void viewHolderGridImageHelper() {
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        int heightGrid;
        int colCount = 3;
        // Choose your own preferred column width


        this.findViewById(R.id.parent_box).setVisibility(View.GONE);

        Album album = (Album) timeline.getContent();
        List<Media> albumMedia = album.getMedia();
        if (albumMedia == null) {
            try {
                albumMedia = tripOperations.getAlbumMedia(album);
                tripOperations.populateMediaSource(albumMedia);
            } catch (ParseException e) {
            }
        }
        int mediaCount = albumMedia.size();

        String extraText = " Photos clicked at ";

        String headerText = album.getContent();
        String locationText = album.getLocationText();
        String timeText = getFormattedTime(timeline.getContentTime());

        String titleText = "";

        if (Source.WAH.equals(timeline.getSource())) {
            if (TextUtils.isEmpty(locationText)) {
                titleText = mediaCount + extraText + timeText;
            } else {
                titleText = locationText;
            }
        } else {
            if (TextUtils.isEmpty(headerText) && TextUtils.isEmpty(locationText)) {
                titleText = mediaCount + extraText + timeText;
            } else {
                if (!TextUtils.isEmpty(headerText)) {
                    titleText = headerText;
                } else {
                    titleText = locationText;
                }
            }
        }

        if (!TextUtils.isEmpty(titleText)) {
            if (titleText.length() > 21) {
                titleText = titleText.substring(0, 20) + "....";
            }

            getSupportActionBar().setTitle(titleText);
        }

        listView.setRequestedColumnCount(colCount);
        listView.determineColumns();
        listView.setRequestedHorizontalSpacing(0);

        displayMetrics = getResources().getDisplayMetrics();
        listView.setLayoutParams(layoutParams);
        DemoItem item;
        if (mediaList != null) {
            for (int i = 0; i < mediaList.size(); i++) {
                Media media = mediaList.get(i);
                item = new DemoItem(1, 1, i);
                item.setMedia(media);
                items.add(item);
            }

            // initialize your items array
            adapter = new DefaultListAdapter(this, items, mediaList.size() - items.size(), timeline);
            mAsymmetricAdapter =
                    new AsymmetricGridViewAdapter<>(this, listView, adapter);
            listView.setAdapter(mAsymmetricAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter.isDeleteMode())
                        return;
                    tripOperations.setSelectedMedia(mediaList.get(position));
                    startActivity(NavigationUtils.getPhotoViewFromAlbumIntent(TimelineEditActivity.this, tripOperations.getTripKey(), getIntent()));
                    NavigationUtils.openAnimation(TimelineEditActivity.this);
                }
            });

            if (adapter.isDeleteMode()) {
                mDeleteImageButton.setVisibility(View.VISIBLE);
            } else {
                mDeleteImageButton.setVisibility(View.GONE);
            }

            mDeleteImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DeleteMediaTask(timeline, adapter.getSelectedItems(), tripOperations).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }
    }

    private String getFormattedTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY) % 12;
        int minutes = calendar.get(Calendar.MINUTE);
        int amPm = calendar.get(Calendar.AM_PM);
        String minuteText = "";
        String dayText;
        String hourText = "";
        if (amPm == 0)
            dayText = "am";
        else
            dayText = "pm";

        if (minutes < 10)
            minuteText = "0";
        if (hours < 10)
            hourText = "0";

        if (amPm != 0 && hours == 0) {
            hourText = "";
            hours = 12;
        }

        return hourText + hours + ":" + minuteText + minutes + " " + dayText;
    }

    public void viewHolderCheckinImageHelper() {
        CheckIn myCheckin = (CheckIn) timeline.getContent();
        if (myCheckin.getPhotoReference() != null) {
            try {
                String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth="
                        + URLEncoder.encode(String.valueOf(480), "UTF-8")
                        + "&photoreference="
                        + URLEncoder.encode(myCheckin.getPhotoReference(), "UTF-8")
                        + "&key="
                        + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8");

                // Glide.with(this).load(photoUrl).into(checkInImage);
                checkInImage.setImageUrl(photoUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileLocal file = ParseFileUtils.getLocalFileFromPin(checkIn);
                if (file != null) {
                    //  Glide.with(this).load(file.getLocalUri().toString()).into(checkInImage);
                    checkInImage.setImageUrl(file.getLocalUri().toString());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (viewType == GRID_VIEW && tripOperations.canWrite()) {
            getMenuInflater().inflate(R.menu.menu_delete_grid, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_timeline_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.delete_image) {
            if (!adapter.isDeleteMode()) {
                mDeleteImageButton.setVisibility(View.VISIBLE);
                adapter.setDeleteMode(true);
                adapter.notifyDataSetChanged();
            } else {
                new DeleteMediaTask(timeline, adapter.getSelectedItems(), tripOperations).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (NavigationUtils.handleBackNavigation(getIntent())) {
            startActivity(NavigationUtils.albumViewBackIntent(this, getIntent(), tripOperations.getTripKey(), timeline));
            NavigationUtils.closeAnimation(this);
            finish();
            return;
        }
        super.onBackPressed();
    }

    private class DeleteMediaTask extends AsyncTask<Void, Void, Void> {

        private CenterProgressDialog progressDialog;
        private boolean deleted;
        private boolean singlePhotoAlbum;
        private Timeline timeline;
        private ArrayList<DemoItem> media;
        private TripOperations tripOperations;


        public DeleteMediaTask(Timeline timeline, ArrayList<DemoItem> media, TripOperations tripOperations) {
            this.timeline = timeline;
            this.media = media;
            this.tripOperations = tripOperations;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressDialog = CenterProgressDialog.show(TimelineEditActivity.this, null, null, true);
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
                        for (DemoItem item : media) {
                            items.remove(item);
                        }
                        adapter.setDeleteMode(false);
                        adapter.notifyDataSetChanged();
                        // onPhotoDeleted(media);
                    }
                } else {
                    Toast.makeText(TimelineEditActivity.this, "Unable to delete the selected photo", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            } catch (Exception e) {

            } finally {
                timeline = null;
                tripOperations = null;
                media = null;
                mDeleteImageButton.setVisibility(View.GONE);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Album album = (Album) timeline.getContent();
                int albumCount = album.getPublicMediaCount();
                if (tripOperations.canWrite())
                    albumCount = album.getMediaCount();
                if (albumCount == 1 || albumCount == media.size()) {
                    tripOperations.deleteTimeLine(timeline);
                    singlePhotoAlbum = true;
                } else {
                    for (DemoItem data : media) {
                        tripOperations.deleteMedia(timeline, data.getMedia());
                    }
                }
                deleted = true;
            } catch (Exception e) {
                DebugUtils.logException(e);
                deleted = false;
            }
            return null;
        }
    }
}
