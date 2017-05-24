package com.weareholidays.bia.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.models.GalleryImage;

import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 12/7/15.
 */
public class PhotosPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<GalleryImage> mImages;

    public PhotosPagerAdapter(Context context, List<GalleryImage> images) {
        mContext = context;
        mImages = images;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.photos_pager, container, false);
        final WahImageView fullImage = (WahImageView) itemView.findViewById(R.id.full_image_view);
     /*   Glide.with(mContext)
                .load(mImages.get(position).getUri())
                .crossFade()
                .centerCrop()
                .into(fullImage);*/
        fullImage.setImageUrl(mImages.get(position).getUri());

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
