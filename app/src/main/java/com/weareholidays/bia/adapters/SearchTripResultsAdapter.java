package com.weareholidays.bia.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.Trip;

import java.text.SimpleDateFormat;
import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 17/6/15.
 */
public class SearchTripResultsAdapter extends ArrayAdapter<Trip> {
    private List<Trip> trips;

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy");

    private Fragment fragment;

    public SearchTripResultsAdapter(Context context, List<Trip> items) {
        super(context, R.layout.search_trip_row, items);
        trips = items;
    }

    public SearchTripResultsAdapter(Context context, List<Trip> items, Fragment fragment) {
        super(context, R.layout.search_trip_row, items);
        trips = items;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return trips.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.search_trip_row, parent, false);
            int pad_px = getContext().getResources().getDimensionPixelSize(R.dimen.timeline_padding_left_right);//Utils.dpToPx(getContext(), 8);
            convertView.setPadding(pad_px,0,pad_px,0);
        }

        Trip trip = trips.get(position);

        WahImageView featureImage = (WahImageView) convertView.findViewById(R.id.search_trip_image);

        if(trip.getFeatureImage() != null){
            featureImage.setImageUrl(trip.getFeatureImage().getUrl());
        }
        else{
            featureImage.setImageResource(R.drawable.trip_placeholder);
        }
      /*  if(fragment != null){
            if(trip.getFeatureImage() != null){
               *//* Glide.with(fragment)
                        .load(trip.getFeatureImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(featureImage);*//*
                featureImage.setImageUrl(trip.getFeatureImage().getUrl());
            }
           *//* else{
                Glide.with(fragment)
                        .load(R.drawable.trip_placeholder)
                        .into(featureImage);
            }*//*
        }
        else{
            if(trip.getFeatureImage() != null){
                Glide.with(getContext())
                        .load(trip.getFeatureImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(featureImage);
            }
            else{
                Glide.with(getContext())
                        .load(R.drawable.trip_placeholder)
                        .into(featureImage);
            }
        }*/

        WahImageView tripUserImage = (WahImageView) convertView.findViewById(R.id.trip_user_image);

        if (trip.getOwner() != null && trip.getOwner().getProfileImage() != null) {
        tripUserImage.setImageUrl(trip.getOwner().getProfileImage().getUrl());
        /*
            if(fragment != null){
                Glide.with(fragment)
                        .load(trip.getOwner().getProfileImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(tripUserImage);
            }
            else{
                Glide.with(getContext())
                        .load(trip.getOwner().getProfileImage().getUrl())
                        .centerCrop()
                                //.placeholder(R.drawable.placeholder)
                        .crossFade()
                        .into(tripUserImage);
            }*/
        }
        else{
            tripUserImage.setImageResource(R.drawable.user_placeholder);
        }

        TextView tripName = (TextView) convertView.findViewById(R.id.search_trip_name);
        String tripN = trip.getName();
        //if(tripN.length()>21)
        //    tripN = tripN.substring(0,20)+"....";
        tripName.setText(tripN);

        TextView tripDays = (TextView) convertView.findViewById(R.id.search_trip_days);
        String dayText = " Days";
        if (trip.getDays().size() == 1)
            dayText = " Day";
        tripDays.setText("\u2022 " + trip.getDays().size() + dayText);

        TextView tripDate = (TextView) convertView.findViewById(R.id.search_trip_date);
        tripDate.setText("\u2022 " + simpleDateFormat.format(trip.getStartTime()));

        return convertView;
    }
}
