package com.weareholidays.bia.activities.search;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.DiscoverPagerAdapter;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.utils.SharedPrefUtils;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_TRIP_TAB_PREF;

public class DiscoverOuterFragment extends Fragment implements DiscoverPagerAdapter.OnInteractionListener, View.OnClickListener {
    public static final String SHOW_DISCOVER_TAB = "SHOW_DISCOVER_TAB";
    public static final int POPULAR_TAB = 0;
    public static final int PUBLISHED_TAB = 1;
    public static final int TRAVELLED_TAB = 2;

    private static final String TAG = "DISCOVER_OUTER_FRAGMENT";

    private ViewPager viewPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private int selectedTab;
    OnFragmentInteractionListener mListener;

    private ShowcaseView showcaseView;

    public DiscoverOuterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_outer, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = (ViewPager) view.findViewById(R.id.discover_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout = (TabLayout) view.findViewById(R.id.discover_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        handleTabs(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(SHOW_DISCOVER_TAB, selectedTab);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClicked(Trip trip) {
    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();
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

    }

    private void handleTabs(Bundle savedInstanceState) {
        String[] titles = {"Most Popular", "Recently Published", "Recently Travelled"};
        for(int i=0; i < tabLayout.getTabCount(); i++){
            tabLayout.getTabAt(i).setText(titles[i]);
        }
        int selectedTabIndex = POPULAR_TAB;
        if(savedInstanceState != null){
            selectedTabIndex = savedInstanceState.getInt(SHOW_DISCOVER_TAB, POPULAR_TAB);
        }
        TabLayout.Tab selectedTab = tabLayout.getTabAt(selectedTabIndex);
        selectedTab.select();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private int NUM_PAGES = 3;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //TODO: Look into performance vs memory with retaining/destroy fragments
            switch (position) {
                case POPULAR_TAB:
                case PUBLISHED_TAB:
                case TRAVELLED_TAB:
                    Bundle bundle = new Bundle();
                    bundle.putInt("listType", position);
                    return Fragment.instantiate(DiscoverOuterFragment.this.getActivity(),
                            DiscoverFragment.class.getName(), bundle);
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    public void drawCoachMarks() {

        if (!SharedPrefUtils.getBooleanPreference(getActivity(), COACH_TRIP_TAB_PREF)) {
            Log.d("Test1234", "COACH_MORE_TAB_PREF");

            View view1 = getActivity().getWindow().getDecorView();;

            Target markLocationByView[] = new Target[1];
            markLocationByView[0] = new ViewTarget(view1);

            Point markLocationByOffset[] = new Point[1];
            markLocationByOffset[0] = new Point(-110, 30);

            Point markTextPoint[] = new Point[1];
            markTextPoint[0] = new Point(85, 30);

            float circleRadius[] = new float[1];
            circleRadius[0] = 70f;

            String coachMarkTextArray[];

            coachMarkTextArray = getResources().getStringArray(R.array.coachmark_trip);

            showcaseView = new ShowcaseView.Builder(getActivity(), true, true)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray,circleRadius)
                    .setOnClickListener(DiscoverOuterFragment.this)
                    .build();

            showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

            SharedPrefUtils.setBooleanPreference(getActivity(), COACH_TRIP_TAB_PREF, true);
        }

    }
}
