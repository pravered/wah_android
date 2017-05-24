package com.weareholidays.bia.models;

import android.graphics.Bitmap;

/**
 * Created by kapil on 27/5/15.
 *
 * A LoadedImage contains the Bitmap loaded for the image.
 */
public class LoadedImage {
    Bitmap mBitmap;

    public LoadedImage(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
