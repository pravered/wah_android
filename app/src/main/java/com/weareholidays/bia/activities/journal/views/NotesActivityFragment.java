package com.weareholidays.bia.activities.journal.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.timeline.TimeLineFragment;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.widgets.TabLayoutCustom;

/**
 * A placeholder fragment containing a simple view.
 */
public class NotesActivityFragment extends Fragment {

    public static String SHOW_JOURNAL_DAY = "SHOW_JOURNAL_DAY";
    public static String SOCIAL_TYPE = "SOCIAL_TYPE";
    public static String CONTENT_TYPE = "CONTENT_TYPE";

    public static int JOURNAL_SUMMARY_VIEW = -1;

    private OnFragmentInteractionListener mListener;

    private ViewPager tripViewPager;

    private TabLayout tripTabs;

    private int selectedDayOrder;

    private int defaultTab = JOURNAL_SUMMARY_VIEW;

    private TripOperations tripOperations;

    private Trip trip;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TripFragment.
     */
    public static NotesActivityFragment   newInstance() {
        NotesActivityFragment   fragment = new NotesActivityFragment  ();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NotesActivityFragment  () {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trip = tripOperations.getTrip();
        if(getArguments() != null){
            defaultTab = getArguments().getInt(SHOW_JOURNAL_DAY,JOURNAL_SUMMARY_VIEW);
        }
        defaultTab++;//increase the index
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_notes, container, false);
        setup(v);
        return v;
    }

    private void setup(View v) {

        tripTabs = (TabLayout) v.findViewById(R.id.trip_tabs);

        tripViewPager = (ViewPager) v.findViewById(R.id.trip_pager);

        tripViewPager.setAdapter(new TripSlidePagerAdapter(getChildFragmentManager()));

        tripTabs.setupWithViewPager(tripViewPager);

        tripViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedDayOrder = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tripTabs.getTabAt(defaultTab).select();
        if(trip.getDays().size() > 3)
            tripTabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        if(trip.getDays().size() == 1){
            ((TabLayoutCustom)tripTabs).setOverrideMaxWidth(true);
        }
    }

    private class TripSlidePagerAdapter extends FragmentStatePagerAdapter {

        public TripSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putString(TimeLineFragment.TIMELINE_TYPE, TimeLineFragment.DAY_TIMELINE);
            args.putInt(TimeLineFragment.DAY_ORDER, position);
            args.putString(SOCIAL_TYPE, Source.WAH);
            args.putString(TripOperations.TRIP_KEY_ARG,tripOperations.getTripKey());
            args.putString(CONTENT_TYPE, Timeline.NOTE_CONTENT);
            return Fragment.instantiate(getActivity(),TimeLineFragment.class.getName(),args);

        }

        @Override
        public int getCount() {
            return  trip.getDays().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return trip.getDays().get(position).getName();

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            tripOperations = mListener.getTripOperations();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        tripOperations = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        TripOperations getTripOperations();
    }

}
