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
 * Created by wah on 12/8/15.
 */
public class SummaryLocationAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<String> dayLocations;
    ArrayList<Integer> locationsCount;

    public SummaryLocationAdapter(Context context, ArrayList<String> dayLocations, ArrayList<Integer> locationsCount) {
        this.mContext = context;
        this.dayLocations = dayLocations;
        this.locationsCount = locationsCount;
    }

    @Override
    public int getCount() {
        if (dayLocations != null)
            return dayLocations.size();
        else
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
            convertView = inflater.inflate(R.layout.summary_location_item, null);
        }

        if (dayLocations != null) {
            ((TextView) convertView.findViewById(R.id.location_summary_city)).setText(dayLocations.get(position));
        }

        if (locationsCount != null) {
            ((TextView) convertView.findViewById(R.id.location_summary_time)).setText(locationsCount.get(position) + "N");
        }

        ((TextView) convertView.findViewById(R.id.location_summary_city)).setText(dayLocations.get(position));

       /* if (dayLocations.size() == 1) {
            if(locationsCount.get(position) == 1) {
                ((TextView) convertView.findViewById(R.id.location_summary_time)).setText(locationsCount.get(position) + "D");
            }else{
                ((TextView) convertView.findViewById(R.id.location_summary_time)).setText(locationsCount.get(position)-1 + "N");
            }
        } else {
            ((TextView) convertView.findViewById(R.id.location_summary_time)).setText(locationsCount.get(position) + "N");
        }*/
        return convertView;
    }
}
