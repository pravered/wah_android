package com.weareholidays.bia.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.felipecsl.asymmetricgridview.library.Utils;
import com.weareholidays.bia.R;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.models.ImageViewHolder;

import java.util.ArrayList;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 28/5/15.
 */
public class GalleryImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<GalleryImage> mItems;
    //private static LruCache<String, Bitmap> mMemoryCache;
    private LayoutInflater mInflater;

    //constructor
    public GalleryImageAdapter(Context context, ArrayList<GalleryImage> items) {
        mContext = context;
        mItems = items;
/*
        //http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
*/
    }

    public void clear() {
        mItems.clear();
    }

    public void remove(int position){
        mItems.remove(position);
    }

    //function that adds images to our arraylist
    public void addImage(GalleryImage image) {
        if (mItems == null) {
            mItems = new ArrayList<GalleryImage>();
        }
        mItems.add(image);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public GalleryImage getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        //returns the id as one plus position of image in the array
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageViewHolder holder = null;

        GalleryImage galleryImage = mItems.get(position);

        if ( convertView == null ) {
            holder = new ImageViewHolder();
            //inflate custom layout for your gridview item
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(
                    R.layout.gallery_image, null);
            // set holder
            holder.imageview = (ImageView) convertView.findViewById(R.id.grid_image);
            holder.disabledMask = (ImageView) convertView.findViewById(R.id.disabled);
            //set dimensions of grid as per the screen size
            WindowManager windowManager = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            float height = (metrics.widthPixels - Utils.dpToPx(mContext,16))/3;
            holder.imageview.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int)height));
            holder.disabledMask.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int)height));
            holder.checkbox = (ImageView) convertView.findViewById(R.id.check_image);
            holder.borderView =  convertView.findViewById(R.id.selected_border);
            holder.borderView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int)height));


            convertView.setTag(holder);

        } else {
            holder = (ImageViewHolder) convertView.getTag();
        }

        if(galleryImage.isAddPhotoPlaceholder()){
           /* Glide.with(mContext)
                    .load(R.drawable.add_photo_camera_button)
                    .into(holder.imageview);*/
            holder.imageview.setImageResource(R.drawable.add_photo_camera_button);
            holder.checkbox.setVisibility(View.GONE);
            holder.borderView.setVisibility(View.INVISIBLE);
            holder.disabledMask.setVisibility(View.GONE);
            return convertView;
        }

        //load images via Glide....takes care of caching
        String url = galleryImage.getUri();
        /*DiskCacheStrategy strategy = DiskCacheStrategy.RESULT;
        if(url.startsWith("http"))
            strategy = DiskCacheStrategy.SOURCE;
        Glide.with(mContext)
                .load(url)
                .diskCacheStrategy(strategy)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .crossFade()
                .into(holder.imageview);*/

        ((WahImageView)holder.imageview).setImageUrl(url);

        //necessary to retain state of checkbox
        if (galleryImage.isSelected()) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.borderView.setVisibility(View.VISIBLE);
        } else  {
            holder.checkbox.setVisibility(View.GONE);
            holder.borderView.setVisibility(View.INVISIBLE);
        }

        if (galleryImage.isDisabled()) {
            holder.disabledMask.setVisibility(View.VISIBLE);
        } else {
            holder.disabledMask.setVisibility(View.GONE);
        }

        return convertView;
    }
}

