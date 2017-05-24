package com.weareholidays.bia.activities.journal.trip;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.people.AddPeopleActivity;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.coachmarks.OnShowcaseEventListener;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.models.Coupon;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.local.TripLocal;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import wahCustomViews.view.WahImageView;

import static android.R.attr.data;
import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_CREATE_TRIP_PREF;

public class TripStartActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TRIP_CREATED = "TRIP_BEING_CREATED";
    public static String COUPON_ID = "COUPON_ID";
    private static final int SELECT_PICTURE = 1;
    private static final int SELECT_PEOPLE = 2;
    private Toolbar toolbar;
    private WahImageView img;
    private EditText tripNameText;
    private CenterProgressDialog progressDialog;
    private TextView peopleTextView;
    private ImageView addPeopleImage;
    private FrameLayout peoplePicsView;
    private static String RETAINED_TRIP = "RETAINED_TRIP";
    private View rel;

    private ShowcaseView showcaseView;
    private Button mCouponButton;

    private TripLocal tripLocal = new TripLocal();
    Coupon mAppliedCoupon;

    private static int contactsRequestCode = 114;
    private static boolean contactsAllowedAfterRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || contactsAllowed()) {
            if (savedInstanceState != null) {
                tripLocal = (TripLocal) savedInstanceState.getSerializable(RETAINED_TRIP);
            }
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_trip_start);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            tripNameText = (EditText) findViewById(R.id.trip_name);
            rel = (View) findViewById(R.id.relativeLayout);
            img = (WahImageView) findViewById(R.id.trip_wall);
            addPeopleImage = (ImageView) findViewById(R.id.imageButton2);
            peopleTextView = (TextView) findViewById(R.id.people_selected);
            peoplePicsView = (FrameLayout) findViewById(R.id.selected_people_pics);
            if (tripLocal.getPeople() != null && tripLocal.getPeople().size() > 0) {
                peopleTextView.setText("+ " + tripLocal.getPeople().size() + " people");
                setContactImages();
            }
            if (!TextUtils.isEmpty(tripLocal.getName())) {
                tripNameText.setText(tripLocal.getName());
            }

            mCouponButton = (Button) findViewById(R.id.apply_coupon);
            mCouponButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog();
                }
            });

            if (!TextUtils.isEmpty(tripLocal.getFeatureImage())) {
                /*Glide.with(this)
                        .load(tripLocal.getFeatureImage())
                        .into(img);*/
            }

            drawCoachMarks();
        }
    }

    private boolean contactsAllowed() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, contactsRequestCode);
            return contactsAllowedAfterRequest;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == contactsRequestCode) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contactsAllowedAfterRequest = true;
//                Toast.makeText(this, "Contacts allowed", Toast.LENGTH_SHORT).show();
            }
            else {
//                Toast.makeText(this, "Contacts denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

        @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            rel.setBackgroundResource(R.color.white);
            loadImage(data.getData());
        }
        if (requestCode == SELECT_PEOPLE && resultCode == RESULT_OK) {
            try {
                HashSet<PeopleContact> pContacts = (HashSet<PeopleContact>) data.getSerializableExtra("people");
                tripLocal.setPeople(new ArrayList<>(pContacts));
                peopleTextView.setText("+ " + tripLocal.getPeople().size() + " people");
                setContactImages();
            } catch (Exception e) {
//                Toast.makeText(this, "Couldn't get people", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setContactImages() {
        WahImageView first_contact_image = (WahImageView) peoplePicsView.findViewById(R.id.first_contact_image);
        WahImageView second_contact_image = (WahImageView) peoplePicsView.findViewById(R.id.second_contact_image);
        WahImageView third_contact_image = (WahImageView) peoplePicsView.findViewById(R.id.third_contact_image);
        if (tripLocal.getPeople().size() > 0) {
            peoplePicsView.setVisibility(View.VISIBLE);
            //getting people with images
            int count = 0;
            List<PeopleContact> peopleWithImage = new ArrayList<>();
            for (int i = 0; i < tripLocal.getPeople().size(); i++) {
                if (tripLocal.getPeople().get(i).getImageUri() != null) {
                    peopleWithImage.add(tripLocal.getPeople().get(i));
                    count++;
                    //stop when we have 3  contacts that have profile images
                    if (count == 3)
                        break;
                }
            }
            //show contact icons based on size of list
            if (tripLocal.getPeople().size() == 1) {
                first_contact_image.setVisibility(View.VISIBLE);
            } else if (tripLocal.getPeople().size() == 2) {
                first_contact_image.setVisibility(View.VISIBLE);
                second_contact_image.setVisibility(View.VISIBLE);
            } else {
                first_contact_image.setVisibility(View.VISIBLE);
                second_contact_image.setVisibility(View.VISIBLE);
                third_contact_image.setVisibility(View.VISIBLE);
            }

            if (peopleWithImage.size() > 0) {
                /*Glide.with(this)
                        .load(peopleWithImage.get(0).getImageUri())
                        .centerCrop()
                        .crossFade()
                        .into(first_contact_image);*/
                first_contact_image.setImageUrl(peopleWithImage.get(0).getImageUri());
            }

            if (peopleWithImage.size() > 1) {
                /*Glide.with(this)
                        .load(peopleWithImage.get(1).getImageUri())
                        .centerCrop()
                        .crossFade()
                        .into(second_contact_image);*/
                second_contact_image.setImageUrl(peopleWithImage.get(1).getImageUri());

            }
            if (peopleWithImage.size() > 2) {
               /* Glide.with(this)
                        .load(peopleWithImage.get(2).getImageUri())
                        .centerCrop()
                        .crossFade()
                        .into(third_contact_image);*/
                third_contact_image.setImageUrl(peopleWithImage.get(2).getImageUri());

            }
        }
        /**
         if (tripLocal.getPeople().get(0).getImageUri() != null) {
         Glide.with(this)
         .load(tripLocal.getPeople().get(0).getImageUri())
         .crossFade()
         .placeholder(R.drawable.user_placeholder)
         .centerCrop()
         .into((CircleImageView) peoplePicsView.findViewById(R.id.first_contact_image));
         }
         if (tripLocal.getPeople().size() > 1) {
         if (tripLocal.getPeople().get(1).getImageUri() != null) {
         Glide.with(this)
         .load(tripLocal.getPeople().get(1).getImageUri())
         .crossFade()
         .placeholder(R.drawable.user_placeholder)
         .centerCrop()
         .into((CircleImageView) peoplePicsView.findViewById(R.id.second_contact_image));
         }
         }
         peoplePicsView.setVisibility(View.VISIBLE);**/
    }

    private void loadImage(Uri uri) {
        progressDialog = CenterProgressDialog.show(this, null, null, true, false);
        new LoadImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putSerializable(RETAINED_TRIP, tripLocal);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_journal03, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.upload_image) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        }
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void startTrip(View view) {
        String tripName = tripNameText.getText().toString();
        if (TextUtils.isEmpty(tripName)) {
            tripNameText.setError(getString(R.string.required));
            return;
        }
        hideKeyboard();
        tripLocal.setName(tripName);

        Intent intent = new Intent(this, TripSettingsActivity.class);
        intent.putExtra(TRIP_CREATED, tripLocal);
        if (mAppliedCoupon != null) {
            intent.putExtra(COUPON_ID, mAppliedCoupon.getObjectId());
        }
        startActivity(intent);
    }

    public void hideKeyboard() {
        ViewUtils.hideKeyboard(this);
    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();
    }

    private class LoadImageTask extends AsyncTask<Uri, Void, Void> {

        private Uri selectedImageUri;

        @Override
        protected Void doInBackground(Uri... params) {
            if (params != null && params[0] != null)
                selectedImageUri = ParseFileUtils.saveToPrivateLocation(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (tripLocal != null) {
                tripLocal.setFeatureImage(selectedImageUri.toString());
               /* Glide.with(TripStartActivity.this)
                        .load(tripLocal.getFeatureImage())
                        .into(img);*/
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
                img.setLayoutParams(param);
                img.setImageUrl(selectedImageUri);
            }
        }
    }

    public void addPeople(View view) {
        Intent aIntent = new Intent(this, AddPeopleActivity.class);
        Bundle b = new Bundle();
        b.putSerializable("addedPeople", tripLocal.getPeople());
        aIntent.putExtras(b);
        startActivityForResult(aIntent, SELECT_PEOPLE);
    }

    public void drawCoachMarks() {

        if (!SharedPrefUtils.getBooleanPreference(TripStartActivity.this, COACH_CREATE_TRIP_PREF)) {
            Log.d("Test12345", "COACH_CREATE_TRIP_PREF");

            View view1 = img;
            View view2 = tripNameText;
            View view3 = addPeopleImage;

            Target markLocationByView[] = new Target[3];
            markLocationByView[0] = new ViewTarget(view1);
            markLocationByView[1] = new ViewTarget(view2);
            markLocationByView[2] = new ViewTarget(view3);

            Point markLocationByOffset[] = new Point[3];
            markLocationByOffset[0] = new Point(0, 0);
            markLocationByOffset[1] = new Point(-105, 0);
            markLocationByOffset[2] = new Point(0, 0);

            Point markTextPoint[] = new Point[3];
            markTextPoint[0] = new Point(60, -10);
            markTextPoint[1] = new Point(45, 0);
            markTextPoint[2] = new Point(30, 35);

            float circleRadius[] = new float[3];
            circleRadius[0] = 50f;
            circleRadius[1] = 30f;
            circleRadius[2] = 30f;

            String coachMarkTextArray[];

            coachMarkTextArray = getResources().getStringArray(R.array.coachmark_create_trip);

            showcaseView = new ShowcaseView.Builder(TripStartActivity.this, true, true)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                    .setOnClickListener(TripStartActivity.this)
                    .build();

            showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

            SharedPrefUtils.setBooleanPreference(TripStartActivity.this, COACH_CREATE_TRIP_PREF, true);
        }
    }

    public void showDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(TripStartActivity.this)
                .title("Enter Coupon Code")
                .customView(R.layout.fragment_coupon, true)
                .positiveText(R.string.apply)
                .negativeText(R.string.cancel)
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .negativeColor(getResources().getColor(R.color.orange_primary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        hideKeyboard(dialog);
                        Calendar c = Calendar.getInstance();
                        String codeEntered = ((EditText) dialog.findViewById(R.id.coupon)).getText().toString().toUpperCase();
                        progressDialog = CenterProgressDialog.show(TripStartActivity.this, "Applying coupon", null, true);
                        try {
                            ParseQuery.getQuery(Coupon.class).whereEqualTo(Coupon.CODE, codeEntered).whereLessThanOrEqualTo(Coupon.VALID_FROM, c.getTime())
                                    .whereGreaterThanOrEqualTo(Coupon.VALID_TILL, c.getTime()).whereEqualTo(Coupon.ISACTIVE, true)
                                    .findInBackground(new FindCallback<Coupon>() {
                                        @Override
                                        public void done(List<Coupon> coupons, ParseException e) {
                                            if (coupons.size() != 0) {
                                                progressDialog.dismiss();
                                                mAppliedCoupon = coupons.get(0);
                                                mCouponButton.setText(String.format(getResources().getString(R.string.trip_start_coupon_applied), coupons.get(0).getCode()));

                                                //if no coupon has been applied , then show success msg
                                                new MaterialDialog.Builder(TripStartActivity.this)
                                                        .title(R.string.applied)
                                                        .content(coupons.get(0).getMessage())
                                                        .positiveText(R.string.ok)
                                                        .positiveColor(getResources().getColor(R.color.orange_primary))
                                                        .show();
                                            } else {
                                                //no matching coupon code found
                                                progressDialog.dismiss();
                                                new MaterialDialog.Builder(TripStartActivity.this)
                                                        .title(R.string.invalid_coupon)
                                                        .content(R.string.enter_valid)
                                                        .positiveText(R.string.ok)
                                                        .positiveColor(getResources().getColor(R.color.orange_primary))
                                                        .show();
                                            }
                                        }
                                    });
                        } catch (Exception e) {
                            DebugUtils.logException(e);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        hideKeyboard(dialog);
                    }
                })
                .show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void hideKeyboard(MaterialDialog dialog) {
        InputMethodManager imm = (InputMethodManager) TripStartActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((EditText) dialog.findViewById(R.id.coupon)).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
