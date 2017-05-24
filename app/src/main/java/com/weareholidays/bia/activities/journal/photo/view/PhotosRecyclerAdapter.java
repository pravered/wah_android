package com.weareholidays.bia.activities.journal.photo.view;

/**
 * Created by Teja on 08-07-2015.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.felipecsl.asymmetricgridview.library.Utils;
import com.weareholidays.bia.R;
import com.weareholidays.bia.models.GalleryImage;

import java.util.List;

import wahCustomViews.view.WahImageView;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<GalleryImage> galleryImages;

    private static int PLACE_HOLDER_VIEW = 1;
    private static int IMAGE_VIEW = 2;

    private OnInteractionListener mListener;

    public PhotosRecyclerAdapter(Context context, List<GalleryImage> images, OnInteractionListener mListener) {
        this.mContext = context;
        this.galleryImages = images;
        this.mListener = mListener;
    }

    ;

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
        }

        public abstract int getType();
    }

    public static class ImageViewHolder extends ViewHolder {
        public WahImageView imageView;
        public View borderView;

        public ImageViewHolder(View v, Context context) {
            super(v);
            imageView = (WahImageView) v.findViewById(R.id.grid_image);
            borderView = v.findViewById(R.id.selected_border);

            WindowManager windowManager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);

            int width = (int) (metrics.widthPixels / 4);
            int height = width - Utils.dpToPx(context, 10);
            width = width - Utils.dpToPx(context, 12);

            imageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
            borderView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        }

        @Override
        public int getType() {
            return IMAGE_VIEW;
        }
    }

    public static class DummyViewHolder extends ViewHolder {

        public DummyViewHolder(View v, Context context) {
            super(v);
            WindowManager windowManager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int width = (int) (metrics.widthPixels / 4);
            v.findViewById(R.id.add_more).setLayoutParams(new RelativeLayout.LayoutParams(width, width));
        }

        @Override
        public int getType() {
            return PLACE_HOLDER_VIEW;
        }
    }


    @Override
    public int getItemViewType(int position) {
        GalleryImage galleryImage = galleryImages.get(position);
        if (galleryImage.isAddPhotoPlaceholder()) {
            return PLACE_HOLDER_VIEW;
        }
        return IMAGE_VIEW;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (PLACE_HOLDER_VIEW == viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_more_button, parent, false);
            ViewHolder vh = new DummyViewHolder(v, mContext);
            return vh;
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_selected_layout, parent, false);

        ViewHolder vh = new ImageViewHolder(v, mContext);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final GalleryImage galleryImage = galleryImages.get(position);
        if (holder.getType() == IMAGE_VIEW) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            /*DiskCacheStrategy strategy = DiskCacheStrategy.RESULT;
            if(galleryImage.getUri().startsWith("http"))
                strategy = DiskCacheStrategy.SOURCE;*/
            /*Glide.with(mContext)
                    .load(galleryImage.getUri())
                    .diskCacheStrategy(strategy)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .crossFade()
                    .into(imageViewHolder.imageView);*/
            imageViewHolder.imageView.setImageUrl(galleryImage.getUri());

            if (galleryImage.isCurrentSelection()) {
                imageViewHolder.borderView.setVisibility(View.VISIBLE);
            } else {
                imageViewHolder.borderView.setVisibility(View.INVISIBLE);
            }
        }

        holder.rootView.setTag(galleryImage);
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryImage image = (GalleryImage) v.getTag();
                mListener.itemClicked(image);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return galleryImages.size();
    }

    public interface OnInteractionListener {
        void itemClicked(GalleryImage galleryImage);
    }

}
