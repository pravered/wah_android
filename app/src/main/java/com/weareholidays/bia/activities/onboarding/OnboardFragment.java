package com.weareholidays.bia.activities.onboarding;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.SplashActivity;

public class OnboardFragment extends Fragment {

    private static final String POSITION_KEY = "POSITION_KEY";

    private int position;

    public OnboardFragment() {
        this.position = 0;
    }

    public static OnboardFragment newInstance(int position){
        OnboardFragment onboardFragment = new OnboardFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION_KEY, position);
        onboardFragment.setArguments(bundle);
        return onboardFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            position = getArguments().getInt(POSITION_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.onboarding_pager, container, false);
        LinearLayout topLeft = (LinearLayout) itemView.findViewById(R.id.top_left);
        LinearLayout topRight = (LinearLayout) itemView.findViewById(R.id.top_right);
        ImageView image = (ImageView) itemView.findViewById(R.id.image);
        LinearLayout bottom = (LinearLayout) itemView.findViewById(R.id.bottom);
        TextView heading = (TextView) itemView.findViewById(R.id.heading);
        TextView content = (TextView) itemView.findViewById(R.id.content);
        Button startButton = (Button) itemView.findViewById(R.id.next);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SplashActivity.class);
                startActivity(intent);
            }
        });
        switch (position) {
            case 0:
                topLeft.setBackgroundColor(Color.parseColor("#FE815E"));
                topRight.setBackgroundColor(Color.parseColor("#FE815E"));
                bottom.setBackgroundColor(Color.parseColor("#FE734C"));
                image.setImageResource(R.drawable.onboard_one);
                heading.setText(R.string.heading1);
                content.setText(R.string.content1);
                break;

            case 1:
                topLeft.setBackgroundColor(Color.parseColor("#2DD9DD"));
                topRight.setBackgroundColor(Color.parseColor("#2DD9DD"));
                bottom.setBackgroundColor(Color.parseColor("#15D5D9"));
                image.setImageResource(R.drawable.onboard_two);
                heading.setText(R.string.heading2);
                content.setText(R.string.content2);
                break;

            case 2:
                topLeft.setBackgroundColor(Color.parseColor("#F2515C"));
                topRight.setBackgroundColor(Color.parseColor("#DE4953"));
                bottom.setBackgroundColor(Color.parseColor("#F03D49"));
                image.setImageResource(R.drawable.onboard_three);
                heading.setText(R.string.heading3);
                content.setText(R.string.content3);
                break;

            case 3:
                topLeft.setBackgroundColor(Color.parseColor("#7578B9"));
                topRight.setBackgroundColor(Color.parseColor("#7578B9"));
                bottom.setBackgroundColor(Color.parseColor("#6569B1"));
                image.setImageResource(R.drawable.onboard_four);
                ((FrameLayout.LayoutParams)image.getLayoutParams()).gravity = Gravity.CENTER;
                heading.setText(R.string.heading4);
                content.setText(R.string.content4);
                break;

            case 4:
                topLeft.setBackgroundColor(Color.parseColor("#56B3F2"));
                topRight.setBackgroundColor(Color.parseColor("#56B3F2"));
                bottom.setBackgroundColor(Color.parseColor("#43AAF0"));
                image.setImageResource(R.drawable.onboard_five);
                ((FrameLayout.LayoutParams)image.getLayoutParams()).gravity = Gravity.CENTER;
                heading.setText(R.string.heading5);
                content.setText(R.string.content5);
                break;

            case 5:
                topLeft.setBackgroundColor(Color.parseColor("#5EB787"));
                topRight.setBackgroundColor(Color.parseColor("#5EB787"));
                bottom.setBackgroundColor(Color.parseColor("#4CAF79"));
                image.setImageResource(R.drawable.onboard_six);
                ((FrameLayout.LayoutParams)image.getLayoutParams()).gravity = Gravity.CENTER;
                heading.setText(R.string.heading6);
                content.setText(R.string.content6);
                startButton.setVisibility(View.VISIBLE);
                break;
        }
        return itemView;
    }
}
