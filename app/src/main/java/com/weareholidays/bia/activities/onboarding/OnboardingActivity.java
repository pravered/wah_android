package com.weareholidays.bia.activities.onboarding;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.OnboardPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private OnboardPagerAdapter mPagerAdapter;
    CirclePageIndicator circleIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        mViewPager = (ViewPager) findViewById(R.id.demopager);
        circleIndicator = (CirclePageIndicator) findViewById(R.id.circles);
        //mPagerAdapter = new OnboardPagerAdapter(this);
        mPagerAdapter = new OnboardPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        //Bind the title indicator to the adapter
        circleIndicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //nothing to do here
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 5) {
                    circleIndicator.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //nothing to do here
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_onboarding, menu);
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
