package com.weareholidays.bia.activities.journal.people;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.PlaceholderFragment;
import com.weareholidays.bia.activities.journal.trip.TripStartActivity;
import com.weareholidays.bia.adapters.AddPeopleRecyclerAdapter;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;
import com.weareholidays.bia.widgets.SlidingTabLayout;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class AddPeopleActivity extends AppCompatActivity implements PhoneBookFragment.OnFragmentInteractionListener
        , FacebookPeopleFragment.OnFragmentInteractionListener{

    public static final String SHARE_PEOPLE_VIEW = "SHARE_PEOPLE_VIEW";
    public static final String SHARE_IMAGE_VIEW = "SHARE_IMAGE_VIEW";
    public static final String SHARE_APP_VIEW = "SHARE_APP_VIEW";

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private SlidingTabLayout tabLayout;
    private int NUM_PAGES = 3;
    private int FACEBOOK_TAB = 1000;
    private int TWITTER_TAB = 2000;
    private int PHONE_TAB = 3000;
    private RecyclerView selectedPeopleView;
    private AddPeopleRecyclerAdapter peopleAdapter;
    private List<PeopleContact> contactList = new ArrayList<>();
    private boolean shareView;
    private boolean sharePhoto;

    private Button addPeople;

    private HashSet<PeopleContact> contactSet = new HashSet<>();

    private TripOperations tripOperations;

    private List<String> identifierList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_people);
        selectedPeopleView = (RecyclerView) findViewById(R.id.my_recycler_view);
        selectedPeopleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        String title = "Add People";
        if (getIntent() != null) {
            // Get the extras (if there are any)
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                //activity called from Trip Start
                if (extras.containsKey("addedPeople")) {
                    contactSet.addAll((ArrayList<PeopleContact>) extras.getSerializable("addedPeople"));
                    contactList.addAll(contactSet);
                    peopleAdapter = new AddPeopleRecyclerAdapter(this, contactList);
                    selectedPeopleView.setAdapter(peopleAdapter);
                //activity called from Trip Fragment
                } else if (extras.containsKey("addedPeopleTrip")) {
                    contactSet.addAll((ArrayList<PeopleContact>) extras.getSerializable("addedPeopleTrip"));
                    contactList.addAll(contactSet);
                    peopleAdapter = new AddPeopleRecyclerAdapter(this, contactList);
                    selectedPeopleView.setAdapter(peopleAdapter);
                }

                for(PeopleContact peopleContact : contactList){
                    identifierList.add(peopleContact.getIdentifier());
                }

                if(extras.containsKey(SHARE_PEOPLE_VIEW)){
                    title = "Share with Friends";
                    shareView = true;
                }

                if(extras.containsKey(SHARE_IMAGE_VIEW)){
                    title = "Share with Friends";
                    shareView = true;
                    sharePhoto = true;
                }

                if(extras.containsKey(TripOperations.TRIP_KEY_ARG)){
                    tripOperations = TripUtils.getInstance().getTripOperations(extras.getString(TripOperations.TRIP_KEY_ARG));
                }

            }
        }

        handlePagerItems();
        mPager = (ViewPager) findViewById(R.id.contact_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(mPagerAdapter);

        //setting title and subtitle for top panel
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        //setting up swipe tabs
        tabLayout = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        tabLayout.setDistributeEvenly(true);
        tabLayout.setCustomTabView(R.layout.tab_layout, R.id.tab_text);

        tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.app_orange);
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
        addPeople = (Button)findViewById(R.id.add_people);
        if(shareView){
            addPeople.setText(R.string.share_people);
        }
        addPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                addPeople();
            }
        });

        if(contactSet.size()>0)
        {
            selectedPeopleView.setVisibility(View.VISIBLE);
            addPeople.setBackgroundColor(getResources().getColor(R.color.orange_primary));
        }
        else{
            selectedPeopleView.setVisibility(View.GONE);
            addPeople.setBackgroundColor(getResources().getColor(R.color.add_people));
        }

    }

    public void hideKeyboard(){
        ViewUtils.hideKeyboard(this);
    }

    private void addPeople(){
        if(contactSet.size() > 0){
            if(tripOperations == null){
                Intent i = new Intent(AddPeopleActivity.this, TripStartActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("people", contactSet);
                i.putExtras(mBundle);
                setResult(RESULT_OK,i);
                finish();
            }
            else{
                new SavePeopleTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        else{
            Toast.makeText(this,"No People have been selected",Toast.LENGTH_LONG).show();
        }
    }

    private class SavePeopleTask extends AsyncTask<Void,Void,Void>{

        private boolean failed;
        private CenterProgressDialog progressDialog;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try{
                progressDialog.dismiss();
                if(failed){
                    Toast.makeText(AddPeopleActivity.this,"Error while saving people",Toast.LENGTH_LONG).show();
                }
                else{
//                    if(TripOperations.CURRENT_TRIP_ID.equals(tripOperations.getTripKey())){
//                        Intent intent = new Intent(AddPeopleActivity.this,HomeActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra(HomeActivity.SHOW_TAB,HomeActivity.JOURNAL_TAB);
//                        startActivity(intent);
//                    }
//                    else{
//                        Intent intent = new Intent(AddPeopleActivity.this,TripActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra(TripOperations.TRIP_KEY_ARG,tripOperations.getTripKey());
//                        startActivity(intent);
//                    }
                    Intent intent = new Intent();
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable("addedPeopleTrip", (Serializable) contactList);
                    intent.putExtras(mBundle);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
            catch (Exception e){

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = CenterProgressDialog.show(AddPeopleActivity.this,null,null,true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(shareView){
                    for(PeopleContact peopleContact : contactList){
                        if(peopleContact.getType() == PeopleContact.Type.PHONE && !ShareUtils.isValidPhone(peopleContact.getIdentifier()))
                            continue;
                        List<ParseCustomUser> selectedUsers = ShareUtils.getParseShareUser(peopleContact.getIdentifier(), peopleContact.getType()).setLimit(1).find();
                        if(selectedUsers != null && selectedUsers.size() == 1){
                            if(sharePhoto){
                                ShareUtils.sendTripPhotoShareNotification(selectedUsers.get(0), tripOperations.getTrip()
                                        ,tripOperations.getTimeLine(),tripOperations.getSelectedMedia());
                            }
                            else{
                                ShareUtils.sendTripShareNotification(selectedUsers.get(0), tripOperations.getTrip());
                            }
                        }
                        else if(peopleContact.getType() == PeopleContact.Type.PHONE){
                            //TODO: check if email present and notify via email.
                        }
                    }
                }
                else {
                    tripOperations.saveTripPeople(contactList);
                }
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            return null;
        }
    }

    private void handlePagerItems() {
        int page_index = 0;
        if(ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())){
            FACEBOOK_TAB = page_index;
            page_index++;
        }
//        if(ParseTwitterUtils.isLinked(ParseUser.getCurrentUser())){
//            TWITTER_TAB = page_index;
//            page_index++;
//        }
        PHONE_TAB = page_index;
        NUM_PAGES = page_index + 1;
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
    public void onContactChanged(PeopleContact peopleContact) {
        if(peopleContact.isSelected()) {
            contactSet.add(peopleContact);
            //never do this because the items will get checked everytime the view is refreshed creating duplicate
            // entries since we are maintaining the state of checkbox while scrolling
            //---->contactList.add(peopleContact);  <!--- (Dont do this)
            //always do contactList.addAll(contactSet) because copying from the set will make sure there are no
            //duplicate entries(check the comment above)
            contactList.clear();
            contactList.addAll(contactSet);
            if (peopleAdapter != null) {
                peopleAdapter.notifyDataSetChanged();
                selectedPeopleView.setAdapter(peopleAdapter);
            } else {
                peopleAdapter = new AddPeopleRecyclerAdapter(this, contactList);
                selectedPeopleView.setAdapter(peopleAdapter);
            }
        } else {
            contactSet.remove(peopleContact);
            contactList.clear();
            contactList.addAll(contactSet);
            peopleAdapter.notifyDataSetChanged();
            selectedPeopleView.setAdapter(peopleAdapter);
        }

        //TODO: control show/hide based on contact set and change button color
        if(contactSet.size()>0)
        {
            selectedPeopleView.setVisibility(View.VISIBLE);
            addPeople.setBackgroundColor(getResources().getColor(R.color.orange_primary));
        }
        else{
            selectedPeopleView.setVisibility(View.GONE);
            addPeople.setBackgroundColor(getResources().getColor(R.color.add_people));
        }
    }

    @Override
    public List<String> getContactIdentifierList() {
        return identifierList;
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == FACEBOOK_TAB){
                return Fragment.instantiate(AddPeopleActivity.this,
                        FacebookPeopleFragment.class.getName());
            }
            if(position == TWITTER_TAB){
                return Fragment.instantiate(AddPeopleActivity.this,
                        PlaceholderFragment.class.getName());
            }
            if(position == PHONE_TAB){
                return Fragment.instantiate(AddPeopleActivity.this,
                        PhoneBookFragment.class.getName());
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == FACEBOOK_TAB){
                return Html.fromHtml("<font color='#A5A5A5'>FACEBOOK</font>");
            }
            if(position == TWITTER_TAB){
                return Html.fromHtml("<font color='#A5A5A5'>TWITTER</font>");
            }
            if(position == PHONE_TAB){
                return Html.fromHtml("<font color='#A5A5A5'>PHONEBOOK</font>");
            }
            return null;
        }

        public CharSequence getSelectedPageTitle(int position){
            if(position == FACEBOOK_TAB){
                return Html.fromHtml("<font color='#FE734C'><b>FACEBOOK</b></font>");
            }
            if(position == TWITTER_TAB){
                return Html.fromHtml("<font color='#FE734C'><b>TWITTER</b></font>");
            }
            if(position == PHONE_TAB){
                return Html.fromHtml("<font color='#FE734C'><b>PHONEBOOK</b></font>");
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_people, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
