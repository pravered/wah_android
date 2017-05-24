package com.weareholidays.bia.adapters;

/**
 * Created by kapil on 8/7/15.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;

import java.util.List;

import wahCustomViews.view.WahImageView;

public class AddPeopleRecyclerAdapter extends RecyclerView.Adapter<AddPeopleRecyclerAdapter.ViewHolder> {
    private List<PeopleContact> mPeople;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mContactName;
        public WahImageView mContactImage;
        public ViewHolder(View v) {
            super(v);
            mContactName = (TextView) v.findViewById(R.id.contact_name);
            mContactImage = (WahImageView) v.findViewById(R.id.contact_thumbnail);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AddPeopleRecyclerAdapter(Context context, List<PeopleContact> people) {
        mContext = context;
        mPeople = people;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AddPeopleRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_recycler_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        PeopleContact contact = mPeople.get(position);
        // - replace the contents of the view with that element
        holder.mContactName.setText(contact.getName());
        holder.mContactImage.setImageUrl(contact.getImageUri());
        /*Glide.with(mContext)
                .load(contact.getImageUri())
                .crossFade()
                .placeholder(R.drawable.user_placeholder)
                .centerCrop()
                .into(holder.mContactImage);*/
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPeople.size();
    }

}
