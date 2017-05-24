package com.weareholidays.bia.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.weareholidays.bia.activities.onboarding.OnboardFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kapil on 9/7/15.
 */
public class OnboardPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public OnboardPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<Fragment>();
        fragments.add(OnboardFragment.newInstance(0));
        fragments.add(OnboardFragment.newInstance(1));
        fragments.add(OnboardFragment.newInstance(2));
        fragments.add(OnboardFragment.newInstance(3));
        fragments.add(OnboardFragment.newInstance(4));
        fragments.add(OnboardFragment.newInstance(5));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
