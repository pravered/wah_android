package com.weareholidays.bia.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.Trip;

import java.text.SimpleDateFormat;
import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 8/7/15.
 */
public class DiscoverPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<Trip> mTrips;
    private OnInteractionListener mListener;


    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy");

    public DiscoverPagerAdapter(Context context, List<Trip> trips, OnInteractionListener mListener) {
        mContext = context;
        mTrips = trips;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListener = mListener;
    }

    @Override
    public int getCount() {
        return mTrips.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (FrameLayout) object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.discover_pager, container, false);
        final Trip currentTrip = mTrips.get(position);
        //set the background feature image if present
        WahImageView tripBg = (WahImageView) itemView.findViewById(R.id.trip_image);
        if (currentTrip.getFeatureImage() != null) {
            /*Glide.with(mContext)
                    .load(currentTrip.getFeatureImage().getUrl())
                    .centerCrop()
                    .crossFade()
                    .into(tripBg);*/

            tripBg.setImageUrl(currentTrip.getFeatureImage().getUrl());
        }
        else{
            tripBg.setImageResource(R.drawable.trip_placeholder);
        }
        //set trip user's image if present
        WahImageView userImage = (WahImageView) itemView.findViewById(R.id.trip_user_image);
        if (currentTrip.getOwner().getProfileImage() != null) {
            userImage.setImageUrl(currentTrip.getOwner().getProfileImage().getUrl());
            /*Glide.with(mContext)
                    .load(currentTrip.getOwner().getProfileImage().getUrl())
                    .centerCrop()
                    .crossFade()
                    .into(userImage);*/
        }
        else{
            userImage.setImageResource(R.drawable.user_placeholder);
        }
        //populate rest of the values
        TextView tripName = (TextView) itemView.findViewById(R.id.trip_name);

        String tripN = currentTrip.getName();
        if (tripN.length() > 21)
            tripN = tripN.substring(0, 20) + "....";
        tripName.setText(tripN);

        TextView tripDays = (TextView) itemView.findViewById(R.id.trip_days);
        String dayText = " Days";
        if (currentTrip.getDays().size() == 1)
            dayText = " Day";
        tripDays.setText("\u2022 " + currentTrip.getDays().size() + dayText);

        TextView tripDate = (TextView) itemView.findViewById(R.id.trip_date);
        tripDate.setText("\u2022 " + simpleDateFormat.format(currentTrip.getStartTime()));

        container.addView(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClicked(currentTrip);
            }
        });

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }

    public interface OnInteractionListener {
        void onClicked(Trip trip);
    }
}
