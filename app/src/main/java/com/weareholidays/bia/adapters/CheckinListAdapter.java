package com.weareholidays.bia.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.weareholidays.bia.R;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.parse.models.CheckIn;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MapUtils;

import java.net.URLEncoder;
import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by challa on 15/7/15.
 */
public class CheckinListAdapter extends ArrayAdapter<DemoItem> {
    private Timeline timeline;
    private List<DemoItem> items;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private DisplayMetrics displayMetrics;

    public CheckinListAdapter(Context context, List<DemoItem> items, DisplayMetrics displayMetrics, Timeline timeline) {
        super(context, 0, items);
        layoutInflater = LayoutInflater.from(context);
        this.displayMetrics = displayMetrics;
        this.mContext = context;
        this.items = items;
        this.timeline = timeline;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout rl;
        WahImageView iv;
        CheckIn checkIn;
        checkIn = (CheckIn) timeline.getContent();
        if (convertView == null) {
            rl = (RelativeLayout) layoutInflater.inflate(R.layout.google_logo_layout, parent, false);
        } else {
            rl = (RelativeLayout) convertView;
        }
        iv = (WahImageView)rl.findViewById(R.id.text);
        if(items.size() == 2 && position == 0){
            String photoUrl = null;
            try {
                photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth="
                        + URLEncoder.encode(String.valueOf(displayMetrics.widthPixels), "UTF-8")
                        + "&photoreference="
                        + URLEncoder.encode(checkIn.getPhotoReference(), "UTF-8")
                        + "&key="
                        + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8");

               /* Glide.with(mContext).load(photoUrl).into(iv);*/
                iv.setImageUrl(photoUrl);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }

        }
        else {
            /*Glide.with(mContext).load(MapUtils.getCheckInMapImageUrl(checkIn.getLocation(), displayMetrics)).into(iv);*/
            iv.setImageUrl(MapUtils.getCheckInMapImageUrl(checkIn.getLocation(),displayMetrics));
        }
        return rl;
    }
}
