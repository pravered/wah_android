package com.weareholidays.bia.activities.journal.photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseGeoPoint;
import com.weareholidays.bia.R;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;
import com.weareholidays.bia.widgets.ChipsMultiAutoCompleteTextview;

import java.util.ArrayList;
import java.util.Arrays;

import wahCustomViews.view.WahImageView;

public class EditPhotoDetailsActivity extends AppCompatActivity {
    private GalleryImage gImage;
    private CenterProgressDialog progressDialog;
    private TripOperations tripOperations;

    private boolean isEditView = false;
    private boolean disablePrivacy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo_details);
        if(getIntent() != null && getIntent().hasExtra(TripOperations.TRIP_KEY_ARG)){
            tripOperations = TripUtils.getInstance().getTripOperations(getIntent().getStringExtra(TripOperations.TRIP_KEY_ARG));
            isEditView = true;
        }
        setup();
    }

    public void setup() {
        if(isEditView){
            Media media = tripOperations.getSelectedMedia();
            Timeline timeline = tripOperations.getTimeLine();
            if(timeline != null && !Source.WAH.equals(timeline.getSource())){
                disablePrivacy = true;
            }
            GalleryImage galleryImage = new GalleryImage();
            if(media.getLocation() != null){
                galleryImage.setLatitude(media.getLocation().getLatitude());
                galleryImage.setLongitude(media.getLocation().getLongitude());
            }
            galleryImage.setAddress(media.getAddress());
            galleryImage.setCaption(media.getCaption());
            galleryImage.setPrivacy(media.isPrivate());
            galleryImage.setUri(media.getMediaSource());
            galleryImage.setTags(media.getTags());
            gImage = galleryImage;
        }
        else{
            tripOperations = TripUtils.getInstance().getCurrentTripOperations();
            gImage = tripOperations.getSelectedPhoto();
        }

        //set bakcground full image
      /*  Glide.with(this)
                .load(gImage.getUri())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>(300, 500) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ((ImageView) findViewById(R.id.full_image_view)).setImageBitmap(resource);
                    }
                });*/

        ((WahImageView)findViewById(R.id.full_image_view)).setImageUrl(gImage.getUri());

        //setting current values if present
        EditText caption = (EditText) findViewById(R.id.caption);
        String captionLength = "";
        if (getIntent().hasExtra("caption")) {
            captionLength = getIntent().getStringExtra("caption");
        }
        if(TextUtils.isEmpty(captionLength))
        {
            caption.setText(gImage.getCaption());
        }
        else{
            caption.setText(captionLength);
        }
        EditText location = (EditText) findViewById(R.id.location);
        if (!TextUtils.isEmpty(gImage.getAddress()))
            location.setText(gImage.getAddress());
        else if (gImage.getLongitude() != 0.0 || gImage.getLatitude() != 0.0)
            location.setText(String.format("%.4f, %.4f", gImage.getLatitude(), gImage.getLongitude()));

        Switch privacySwitch = (Switch) findViewById(R.id.toggleButton);
        privacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ((ImageView) findViewById(R.id.lock_open)).setColorFilter(0xff666666);
                    ((ImageView) findViewById(R.id.lock_closed)).setColorFilter(0xffffffff);
                }
                else{
                    ((ImageView) findViewById(R.id.lock_open)).setColorFilter(0xffffffff);
                    ((ImageView) findViewById(R.id.lock_closed)).setColorFilter(0xff666666);
                }
            }
        });
        privacySwitch.setChecked(gImage.isPrivacy());
        ChipsMultiAutoCompleteTextview v = (ChipsMultiAutoCompleteTextview) findViewById(R.id.multiAutoCompleteTextView1);
        //changing the line color to white --> apply same background as some other editext
        //v.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        if (Build.VERSION.SDK_INT >= 16) {
            v.setBackground(caption.getBackground());
        }
        else{
            v.setBackgroundDrawable(caption.getBackground());
        }
        //set tags if any present else set empty string
        if (gImage.getTags() != null) {
            String chips = TextUtils.join(" ", gImage.getTags());
            setChipsBack(chips, v);
        } else {
            v.setText("");
        }

        if(disablePrivacy)
            findViewById(R.id.privacy_layout).setVisibility(View.GONE);
        //add listener for adding picture location
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(EditPhotoDetailsActivity.this, SetPhotoLocationActivity.class);
                    if ((gImage.getLatitude() != 0.0) && (gImage.getLongitude() != 0.0) && (gImage.getAddress().length() != 0)) {
                        intent.putExtra("imageLatitude", gImage.getLatitude());
                        intent.putExtra("imageLongitude", gImage.getLongitude());
                        intent.putExtra("imageAddress", gImage.getAddress());
                    }
                    startActivityForResult(intent, 1337);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Couldn't load the location at this moment", Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.button_cancel2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhotoDetails();
            }
        });
    }


    @Override
    public void onBackPressed() {
        if(NavigationUtils.handleBackNavigation(getIntent()) && tripOperations != null)
        {
            startActivity(NavigationUtils.getClosePhotoEditIntent(this,tripOperations.getTripKey(),getIntent()));
            NavigationUtils.closeAnimation(this);
            finish();
            return;
        }
        Intent i = new Intent(EditPhotoDetailsActivity.this, SelectedPhotoActivity.class);
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("FROM_CAMERA", false)) {
                //if the activity was started from camera, send this intent back
                i.putExtra("FROM_CAMERA", true);
            }
        }
        startActivity(i);
        finish();
    }

    //This function has whole logic for chips generation
    public void setChipsBack(String s, ChipsMultiAutoCompleteTextview v) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(s);
        String chips[];
        if (s.contains(" ")) // check space in string
        {
            // split string with space
            chips = s.trim().split(" ");
        } else {
            chips = new String[1];
            chips[0] = s;
        }

        int x = 0;
        // loop will generate ImageSpan for every name separated by space
        for (String c : chips) {
            // inflate chips_edittext layout
            LayoutInflater lf = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            TextView textView = (TextView) lf.inflate(R.layout.chips_edittext, null);
            textView.setText(c); // set text
            // capture bitmap of generated textview
            int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            textView.measure(spec, spec);
            textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
            Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            canvas.translate(-textView.getScrollX(), -textView.getScrollY());
            textView.draw(canvas);
            textView.setDrawingCacheEnabled(true);
            Bitmap cacheBmp = textView.getDrawingCache();
            Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
            textView.destroyDrawingCache();  // destory drawable
            // create bitmap drawable for imagespan
            BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
            bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
            // create and set imagespan
            ssb.setSpan(new ImageSpan(bmpDrawable), x, x + c.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            x = x + c.length() + 1;
        }

        // set chips span
        v.setText(ssb);
        // move cursor to last
        v.setSelection(s.length());
    }

    // Call Back method  to get the Message from other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK) {
            try {
                String photo_location = data.getStringExtra("photo_location");
                if(!isEditView){
                    tripOperations.getSelectedPhoto().setLatitude(data.getDoubleExtra("latitude", 0.0));
                    tripOperations.getSelectedPhoto().setLongitude(data.getDoubleExtra("longitude", 0.0));
                    tripOperations.getSelectedPhoto().setAddress(photo_location);
                }
                else{
                    gImage.setLatitude(data.getDoubleExtra("latitude", 0.0));
                    gImage.setLongitude(data.getDoubleExtra("longitude", 0.0));
                    gImage.setAddress(photo_location);
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
                    parseGeoPoint.setLatitude(gImage.getLatitude());
                    parseGeoPoint.setLongitude(gImage.getLongitude());
                    tripOperations.saveSelectedMediaLocation(gImage.getAddress(),parseGeoPoint);
                }
                //Location coordinates = data.getParcelableExtra("coordinates");
                Log.d("location", String.valueOf(data.getDoubleExtra("latitude", 0)));
                EditText loc = (EditText) findViewById(R.id.location);
                loc.setText(photo_location);
            } catch (Exception e) {
                Toast.makeText(this, "Couldn't load location", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void savePhotoDetails() {
        if(!isEditView){
            progressDialog = CenterProgressDialog.show(this, null, null, true, false);

            String caption = ((EditText) findViewById(R.id.caption)).getText().toString();
            String tags_data = ((ChipsMultiAutoCompleteTextview) findViewById(R.id.multiAutoCompleteTextView1)).getText().toString();
            String[] tags_array = tags_data.split(" ");
            ArrayList<String> tags = new ArrayList<>();
            //case when only 1 tag is added, splitting with " " will give empty array
            if (tags_array.length == 0) {
                tags.add(tags_data);
            } else {
                tags.addAll(Arrays.asList(tags_array));
            }

            String location = ((EditText) findViewById(R.id.location)).getText().toString();
            boolean isPrivate = ((Switch) findViewById(R.id.toggleButton)).isChecked();

            gImage = tripOperations.getSelectedPhoto();
            //save object details
            gImage.setCaption(caption);
            gImage.setTags(tags);
            gImage.setAddress(location);
            gImage.setPrivacy(isPrivate);

            progressDialog.dismiss();

            Intent i = new Intent(this, SelectedPhotoActivity.class);
            if (getIntent() != null) {
                if (getIntent().getBooleanExtra("FROM_CAMERA", false)) {
                    //if the activity was started from camera, send this intent back
                    i.putExtra("FROM_CAMERA", true);
                }
            }
            startActivity(i);
            finish();
        }
        else{
            Media media = tripOperations.getSelectedMedia();
            String caption = ((EditText) findViewById(R.id.caption)).getText().toString();
            String tags_data = ((ChipsMultiAutoCompleteTextview) findViewById(R.id.multiAutoCompleteTextView1)).getText().toString();
            String[] tags_array = tags_data.split(" ");
            ArrayList<String> tags = new ArrayList<>();
            //case when only 1 tag is added, splitting with " " will give empty array
            if (tags_array.length == 0) {
                tags.add(tags_data);
            } else {
                tags.addAll(Arrays.asList(tags_array));
            }

            String location = ((EditText) findViewById(R.id.location)).getText().toString();
            boolean isPrivate = ((Switch) findViewById(R.id.toggleButton)).isChecked();
            if(disablePrivacy)
                isPrivate = false;
            ParseGeoPoint parseGeoPoint = null;
            if(!(gImage.getLatitude() == 0.0 && gImage.getLongitude() == 0.0)){
                parseGeoPoint = new ParseGeoPoint();
                parseGeoPoint.setLatitude(gImage.getLatitude());
                parseGeoPoint.setLongitude(gImage.getLongitude());
            }
            try {
                tripOperations.saveSelectedMediaChanges(isPrivate,tags,caption,location,parseGeoPoint);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_photo_details, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
