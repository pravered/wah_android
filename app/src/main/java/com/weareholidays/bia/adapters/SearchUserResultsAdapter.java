package com.weareholidays.bia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.ParseCustomUser;

import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 17/6/15.
 */
public class SearchUserResultsAdapter extends ArrayAdapter<ParseCustomUser> {
    private List<ParseCustomUser> users;

    public SearchUserResultsAdapter(Context context, List<ParseCustomUser> items) {
        super(context, R.layout.search_user_row, items);
        users = items;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.search_user_row, parent, false);
            int pad_px = getContext().getResources().getDimensionPixelSize(R.dimen.timeline_padding_left_right);//Utils.dpToPx(getContext(), 8);
            convertView.setPadding(pad_px, 0, pad_px, 0);
        }

        WahImageView userImage = (WahImageView) convertView.findViewById(R.id.user_image);
        if (users.get(position).getProfileImage() != null) {
           /* Glide.with(getContext())
                    .load(users.get(position).getProfileImage().getUrl())
                    .centerCrop()
                    //.placeholder(R.drawable.default_user)
                    .crossFade()
                    .into(userImage);*/
            userImage.setImageUrl(users.get(position).getProfileImage().getUrl());
        }
        else{
            userImage.setImageResource(R.drawable.user_placeholder);
        }
        TextView userName = (TextView) convertView.findViewById(R.id.user_name);
        userName.setText(users.get(position).getName());

        TextView userLocation = (TextView) convertView.findViewById(R.id.user_location);
        if (users.get(position).getPlace() != null) {
            if (users.get(position).getPlace().length() > 0)
                userLocation.setText(users.get(position).getPlace());
        } else {
            userLocation.setVisibility(View.GONE);
        }

        TextView userTrips = (TextView) convertView.findViewById(R.id.user_trips);
        userTrips.setText(users.get(position).getTotalPublishedTrips() + " Trip Journals");

        return convertView;
    }
}
