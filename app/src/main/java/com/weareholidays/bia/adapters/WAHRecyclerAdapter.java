package com.weareholidays.bia.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;

import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 25/6/15.
 */
public class WAHRecyclerAdapter extends RecyclerView.Adapter<WAHRecyclerAdapter.ViewHolder> {
    private List<PeopleContact> mPeople;
    private Context mContext;

    private static int PLACE_HOLDER_VIEW = 1;
    private static int IMAGE_VIEW = 2;

    private OnInteractionListener mListener;

    public abstract static class ViewHolder extends RecyclerView.ViewHolder{

        public View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
        }

        public abstract int getType();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ImageViewHolder extends ViewHolder {
        public TextView mContactName;
        public WahImageView mContactImage;
        public ImageViewHolder(View v) {
            super(v);
            mContactName = (TextView) v.findViewById(R.id.contact_name);
            mContactImage = (WahImageView) v.findViewById(R.id.contact_thumbnail);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecyclerView recyclerView = (RecyclerView) v.getParent();
                    recyclerView.setVisibility(View.GONE);
                    FrameLayout f = (FrameLayout) v.getRootView().findViewById(R.id.selected_people_pics);
                    f.setVisibility(View.VISIBLE);
                }
            });
        }
        @Override
        public int getType() {
            return IMAGE_VIEW;
        }
    }

    public static class DummyViewHolder extends ViewHolder{

        public DummyViewHolder(View v){
            super(v);
        }

        @Override
        public int getType() {
            return PLACE_HOLDER_VIEW;
        }
    }

    @Override
    public int getItemViewType(int position) {
        PeopleContact peopleContact = mPeople.get(position);
        if(peopleContact.isAddPeoplePlaceholder()) {
            return PLACE_HOLDER_VIEW;
        }
        return IMAGE_VIEW;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WAHRecyclerAdapter(Context context, List<PeopleContact> people, OnInteractionListener listener) {
        mContext = context;
        mPeople = people;
        mListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //place add people button at last
        if (viewType == PLACE_HOLDER_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_people_button, parent, false);
            ViewHolder vh = new DummyViewHolder(v);
            return vh;
        }
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_recycler_item, parent, false);

        ViewHolder vh = new ImageViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get element from your dataset at this position
        PeopleContact contact = mPeople.get(position);
        // - replace the contents of the view with that element
        if (holder.getType() == PLACE_HOLDER_VIEW) {
            DummyViewHolder dummy = (DummyViewHolder) holder;
            dummy.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.itemClicked();
                    notifyDataSetChanged();
                }
            });
        } else {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            imageViewHolder.mContactName.setText(contact.getName());
            /*Glide.with(mContext)
                .load(contact.getImageUri())
                .crossFade()
                .placeholder(R.drawable.user_placeholder)
                .centerCrop()
                .into(imageViewHolder.mContactImage);*/
            imageViewHolder.mContactImage.setImageUrl(contact.getImageUri());
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPeople.size();
    }

    public interface OnInteractionListener{
        void itemClicked();
    }

}
