package com.weareholidays.bia.adapters;

/**
 * Created by challa on 22/5/15.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.CustomLocation;

import java.util.List;

public class LazyAdapter extends ArrayAdapter {

    private Activity activity;
    private List<CustomLocation> data;
    private static LayoutInflater inflater=null;
    private String[] iconsNames = {"mosque", "museum"};

    public LazyAdapter(Activity a, List<CustomLocation> d) {
        super(a, R.layout.list_row, d.toArray());
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);

        TextView text_name=(TextView)vi.findViewById(R.id.placeName);
        TextView text_distance=(TextView)vi.findViewById(R.id.placeDistance);
        ImageView image=(ImageView)vi.findViewById(R.id.placeImage);

        CustomLocation record = (CustomLocation)getItem(position);
        text_name.setText(record.getName());
        if (record.getDistance() < 0) {
            text_distance.setVisibility(View.GONE);
        }
        else
            text_distance.setText(String.valueOf(record.getDistance()) + " miles away");

        List<String> categories = record.getCategory();
        String place_type = checkCategory(categories);

        if ("mosque".equals(place_type)) {
            image.setImageResource(R.drawable.timeline_landmark);

//        } else if ("museum".equals(place_type)) {
//            image.setImageResource(R.drawable.monument);
//
        } else if("restaurant".equals(place_type)){
            image.setImageResource(R.drawable.timeline_restaurant);
        }
        else {
            image.setImageResource(R.drawable.timeline_checkin);
        }

        return vi;
    }

    public String checkCategory(List<String> categories){
        for(String category : categories){
            for(String iconName: iconsNames){
                if (category.equals(iconName))
                    return category;
            }
        }
        return "None";
    }
}