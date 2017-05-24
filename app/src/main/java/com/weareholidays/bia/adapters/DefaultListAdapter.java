package com.weareholidays.bia.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.widgets.CheckBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Sample adapter implementation extending from AsymmetricGridViewAdapter<DemoItem>
 * This is the easiest way to get started.
 */
public class DefaultListAdapter extends ArrayAdapter<DemoItem> implements DemoAdapter {

    private final LayoutInflater layoutInflater;
    public int remainingItems;
    public List<DemoItem> items;
    public Timeline timeline;
    public Context mcontext;
    private TimelineRecyclerAdapter.OnItemInteraction mListener;
    private boolean mDeleteMode;
    private ArrayList<DemoItem> mSelectedItems;

    public ArrayList<DemoItem> getSelectedItems() {
        return mSelectedItems;
    }

    public boolean isDeleteMode() {
        return mDeleteMode;
    }

    public void setDeleteMode(boolean deleteMode) {
        if (mSelectedItems != null) {
            mSelectedItems.clear();
        }

        this.mDeleteMode = deleteMode;
    }

    public DefaultListAdapter(Context context, List<DemoItem> items, int remainingItems, Timeline timeline) {
        super(context, 0, items);
        layoutInflater = LayoutInflater.from(context);
        this.mcontext = context;
        this.remainingItems = remainingItems;
        this.items = items;
        this.timeline = timeline;
        mSelectedItems = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout rl;
        WahImageView iv;
        LinearLayout ll;
        TextView photoNumText;
        CheckBox checkBox;
        DemoItem item = getItem(position);
        if (convertView == null) {
            rl = (RelativeLayout) layoutInflater.inflate(R.layout.adapter_item, parent, false);
        } else {
            rl = (RelativeLayout) convertView;
        }

        iv = (WahImageView) rl.findViewById(R.id.text);
        photoNumText = (TextView) rl.findViewById(R.id.photoNum);
        checkBox = (CheckBox) rl.findViewById(R.id.delete_checkbox);
        checkBox.setTag(item);

        if (isDeleteMode()) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }

        checkBox.setOncheckListener(new CheckBox.OnCheckListener() {
            @Override
            public void onCheck(boolean check, View view) {
                DemoItem checkedItem = (DemoItem) view.getTag();
                if (check) {
                    mSelectedItems.add(checkedItem);

                } else {
                    if (mSelectedItems.contains(checkedItem)) {
                        mSelectedItems.remove(checkedItem);
                    }
                }
            }
        });

        /*Glide.with(parent.getContext()).load(item.getMedia().getMediaSource())
                .placeholder(R.drawable.image_loader).into(iv);*/
        Uri uri = Uri.fromFile(new File(item.getMedia().getMediaSource()));

        String url = item.getMedia().getMediaSource();

       /* if (url.contains("http") || url.contains("https")) {
            iv.setImageUrl(url);  //load from http request
        } else {
            iv.setImageUrl(new File(url));  // load local images

        }*/

        iv.setImageUrl(url);
        checkBox.bringToFront();

        ll = (LinearLayout) rl.findViewById(R.id.myLinear);
        if (remainingItems > 0 && position == getCount() - 1) {
            photoNumText.setText("+" + String.valueOf(remainingItems + 1));
            ll.setVisibility(View.VISIBLE);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditTimeLine(timeline);
                }
            });
        } else
            ll.setVisibility(View.GONE);
        return rl;
    }

    @Override
    public void appendItems(List<DemoItem> newItems) {

    }

    @Override
    public void setItems(List<DemoItem> moreItems) {

    }

    public void setmListener(TimelineRecyclerAdapter.OnItemInteraction mListener) {
        this.mListener = mListener;
    }
}