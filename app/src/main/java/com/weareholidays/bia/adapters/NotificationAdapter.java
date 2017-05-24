package com.weareholidays.bia.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.Notification;
import com.weareholidays.bia.parse.utils.ShareUtils;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 21/5/15.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationItemViewHolder> {

    private List<Notification> notifications;
    Context mContext;

    public NotificationAdapter(Context context){
        this.mContext = context;
    }

    public NotificationAdapter(Activity activity, List<Notification> items) {
        super();
        notifications = items;
    }

    public void setNotificationList(Activity activity, List<Notification> items){
        this.mContext = activity;
        this.notifications = items;
    }

    public List<Notification> getNotificationList(){
        return notifications;
    }

    @Override
    public NotificationItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_row, parent, false);
        return new NotificationItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotificationItemViewHolder holder, final int position) {

        final Notification notification = notifications.get(position);

        if(notification.getNotifier() != null && notification.getNotifier().getProfileImage() != null){
            /*Glide.with(getContext())
                    .load(notification.getNotifier().getProfileImage().getUrl())
                    .into(imageView);*/
            holder.imageView.setImageUrl(notification.getNotifier().getProfileImage().getUrl());
        }
        else{
            holder.imageView.setImageResource(R.drawable.user_placeholder);
        }

        holder.notificationText.setText(Html.fromHtml(notification.getContent()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(notification.getContentTime());
        String[] months = new DateFormatSymbols().getMonths();
        int month = calendar.get(Calendar.MONTH);
        String monthName = months[month];
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);

        holder.notificationTime.setText(monthName + " "+ date + ", "+ year);

        holder.imageView.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(position);
            }
        });

    }


    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(int position);
    }


    public void setOnItemClickListener(final OnItemClickListener mOnItemClickListener) {
        this.onItemClickListener = mOnItemClickListener;
    }

    @Override
    public int getItemCount() {
        if(notifications != null){
            return notifications.size();
        } else {
            return 0;
        }
    }

    public class NotificationItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView notificationText;
        protected TextView notificationTime;
        protected WahImageView imageView;

        public NotificationItemViewHolder(View itemView) {
            super(itemView);
            notificationText = (TextView)itemView.findViewById(R.id.notification_text);
            notificationTime = (TextView)itemView.findViewById(R.id.notification_time);
            imageView = (WahImageView)itemView.findViewById(R.id.notification_user_image);
        }
    }
}
