package com.weareholidays.bia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.weareholidays.bia.R;

import java.util.ArrayList;

/**
 * Created by wah on 13/8/15.
 */
public class DiscoverLocationAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<String> dayLocations;

    public DiscoverLocationAdapter(Context context, ArrayList<String> dayLocations) {
        this.mContext = context;
        this.dayLocations = dayLocations;
    }

    @Override
    public int getCount() {
        if (dayLocations != null) {
            if (dayLocations.size() > 3) {
                return 3;
            }
            return dayLocations.size();
        } else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.discover_fragment_location, null);
        }

        if (dayLocations != null) {
            ((TextView) convertView.findViewById(R.id.location_city)).setText(dayLocations.get(position));
        }

        return convertView;
    }
}
