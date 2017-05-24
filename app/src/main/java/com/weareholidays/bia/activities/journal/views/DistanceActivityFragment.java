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
import com.weareholidays.bia.activities.journal.MyMapFragment;
import com.weareholidays.bia.activities.journal.timeline.TimeLineFragment;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripOperations;

/**
 * A placeholder fragment containing a simple view.
 */
public class DistanceActivityFragment extends Fragment {

    public static String SHOW_JOURNAL_DAY = "SHOW_JOURNAL_DAY";
    public static String TYPE = "TYPE";

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
    public static DistanceActivityFragment newInstance() {
        DistanceActivityFragment fragment = new DistanceActivityFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DistanceActivityFragment() {
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

        View v = inflater.inflate(R.layout.fragment_distance, container, false);
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
        if(trip.getDays().size() > 2)
            tripTabs.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private class TripSlidePagerAdapter extends FragmentStatePagerAdapter {

        public TripSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putString(TimeLineFragment.TIMELINE_TYPE, TimeLineFragment.DAY_TIMELINE);
            args.putString(TripOperations.TRIP_KEY_ARG,tripOperations.getTripKey());
            args.putInt(TimeLineFragment.DAY_ORDER, position-1);
            args.putString(TYPE, "DISTANCE");
            return Fragment.instantiate(getActivity(),MyMapFragment.class.getName(),args);

        }

        @Override
        public int getCount() {
            return trip.getDays().size()+1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return "SUMMARY";
            else
                return trip.getDays().get(position-1).getName();

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
