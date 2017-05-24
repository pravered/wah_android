package com.weareholidays.bia.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.Trip;

import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by Teja on 15/07/15.
 */
public class TripRecyclerAdapter extends RecyclerView.Adapter<TripRecyclerAdapter.TripHolder> {

    private List<Trip> tripList;
    private Context mContext;
    private Fragment fragment;
    private OnItemInteraction mListener;

    public TripRecyclerAdapter(Context mContext, List<Trip> tripList, Fragment fragment, OnItemInteraction mListener) {
        this.tripList = tripList;
        this.mContext = mContext;
        this.fragment = fragment;
        this.mListener = mListener;
    }

    public class TripHolder extends RecyclerView.ViewHolder {

        WahImageView featureImage;
        WahImageView tripUserImage;
        TextView tripName;
        TextView tripDays;
        TextView tripDate;
        LinearLayout onGoing;
        View rootView;

        public TripHolder(View itemView) {
            super(itemView);
            int pad_px = mContext.getResources().getDimensionPixelSize(R.dimen.timeline_padding_left_right);//Utils.dpToPx(mContext, 8);
            itemView.setPadding(pad_px, pad_px/2, pad_px, pad_px/2);

            featureImage = (WahImageView) itemView.findViewById(R.id.search_trip_image);
            tripUserImage = (WahImageView) itemView.findViewById(R.id.trip_user_image);
            tripName = (TextView) itemView.findViewById(R.id.search_trip_name);
            tripDays = (TextView) itemView.findViewById(R.id.search_trip_days);
            tripDate = (TextView) itemView.findViewById(R.id.search_trip_date);
            onGoing = (LinearLayout) itemView.findViewById(R.id.on_going);
            rootView = itemView;
        }
    }

    @Override
    public TripRecyclerAdapter.TripHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(mContext)
                .inflate(R.layout.search_trip_row, parent, false);
        return new TripHolder(v);
    }

    @Override
    public void onBindViewHolder(TripRecyclerAdapter.TripHolder holder, int position) {
        final Trip trip = tripList.get(position);

        if(trip.getFeatureImage() != null) {
            holder.featureImage.setImageUrl(trip.getFeatureImage().getUrl());
        }
        else{
            holder.featureImage.setImageResource(R.drawable.trip_placeholder);
        }
        /*if(fragment != null){
            if(trip.getFeatureImage() != null){
                Glide.with(fragment)
                        .load(trip.getFeatureImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(holder.featureImage);
            }
            else{
                Glide.with(fragment)
                        .load(R.drawable.trip_placeholder)
                        .into(holder.featureImage);
            }
        }
        else{
            if(trip.getFeatureImage() != null){
                Glide.with(mContext)
                        .load(trip.getFeatureImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(holder.featureImage);
            }
            else{
                Glide.with(mContext)
                        .load(R.drawable.trip_placeholder)
                        .into(holder.featureImage);
            }
        }*/

        if (trip.getOwner().getProfileImage() != null) {
            holder.tripUserImage.setImageUrl(trip.getOwner().getProfileUrl());
          /*  if(fragment != null){
                Glide.with(fragment)
                        .load(trip.getOwner().getProfileImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(holder.tripUserImage);
            }
            else{
                Glide.with(mContext)
                        .load(trip.getOwner().getProfileImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(holder.tripUserImage);
            }*/
        }
        else{
            holder.tripUserImage.setImageResource(R.drawable.user_placeholder);
        }
        String tripN = trip.getName();
        if(tripN.length()>21)
            tripN = tripN.substring(0,20)+"....";
        holder.tripName.setText(tripN);

        if(!trip.isFinished())
            holder.onGoing.setVisibility(View.VISIBLE);
        else
            holder.onGoing.setVisibility(View.GONE);
        String dayText = " Days";
        if (trip.getDays().size() == 1)
            dayText = " Day";
        holder.tripDays.setText("\u2022 " + trip.getDays().size() + dayText);
        holder.tripDate.setText("\u2022 " + SearchTripResultsAdapter.simpleDateFormat.format(trip.getStartTime()));
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClicked(trip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public interface OnItemInteraction{
        void onItemClicked(Trip selectedTrip);
    }
}
