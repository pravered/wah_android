package com.weareholidays.bia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.Day;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by hament on 24/6/15.
 */
public class DurationAdapter extends ArrayAdapter<Day> {
    private List<Day> days;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy");

    public DurationAdapter(Context context, List<Day> items) {
        super(context, R.layout.search_day_row, items);
        days = items;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.search_day_row, parent, false);
        }

        Day day = days.get(position);

        ImageView featureImage = (ImageView) convertView.findViewById(R.id.search_trip_image);
        int photos = day.getDaySummary().getPhotos();
        double distance = day.getDaySummary().getDistance();
        int checks = day.getDaySummary().getCheckIns();

        TextView tripDay = (TextView) convertView.findViewById(R.id.trip_day);
        tripDay.setText("Day " + (position+1));

        TextView Distance = (TextView) convertView.findViewById(R.id.distance);
//        DecimalFormat df = new DecimalFormat("#.00");
        Distance.setText("\u2022 " + Math.round(distance) + " KM");

        TextView checkIns = (TextView) convertView.findViewById(R.id.check_ins);
        checkIns.setText("\u2022 " + checks +" Check-Ins");

        TextView Photos = (TextView) convertView.findViewById(R.id.photos);
        Photos.setText("\u2022 " + photos + " Photos");
        return convertView;
    }
}