package com.weareholidays.bia.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Teja on 23-06-2015.
 */
public class ViewUtils {

    public static final String PARENT_ACTIVITY = "PARENT_ACTIVITY";
    public static final String PLACES_TYPES = "airport|amusement_park|aquarium|art_gallery|bar|bowling_alley|bus_station|cafe|church|cemetery|casino|car_rental|city_hall|courthouse|clothing_store|embassy|establishment|mosque|museum|food|grocery_or_supermarket|gym|health|hindu_temple|hospital|library|liquor_store|gas_station|restaurant|local_government_office|lodging|movie_theater|night_club|park|place_of_worship|police|post_office|school|shopping_mall|spa|stadium|subway_station|synagogue|train_station|university|zoo|administrative_area_level_2|administrative_area_level_3|colloquial_area|country|intersection|locality|natural_feature|neighborhood|politicalpoint_of_interest|postal_code|postal_town|premise|route|sublocality|transit_station";
    public static final String REMAINING_PLACES_TYPES = "airport|amusement_park|aquarium|art_gallery|bus_station|bowling_alley|church|casino|museum|local_government_office|park|place_of_worship|shopping_mall|stadium|train_station|zoo|administrative_area_level_2|administrative_area_level_3|natural_feature|point_of_interest|sublocality";

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    //check if working internet is available or not
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Bitmap blurfast(Bitmap bmp, int radius) {
        Bitmap bmpT = bmp.copy(bmp.getConfig(), true);
        int w = bmpT.getWidth();
        int h = bmpT.getHeight();
        int[] pix = new int[w * h];
        bmpT.getPixels(pix, 0, w, 0, 0, w, h);

        for(int r = radius; r >= 1; r /= 2) {
            for(int i = r; i < h - r; i++) {
                for(int j = r; j < w - r; j++) {
                    int tl = pix[(i - r) * w + j - r];
                    int tr = pix[(i - r) * w + j + r];
                    int tc = pix[(i - r) * w + j];
                    int bl = pix[(i + r) * w + j - r];
                    int br = pix[(i + r) * w + j + r];
                    int bc = pix[(i + r) * w + j];
                    int cl = pix[i * w + j - r];
                    int cr = pix[i * w + j + r];

                    pix[(i * w) + j] = 0xFF000000 |
                            (((tl & 0xFF) + (tr & 0xFF) + (tc & 0xFF) + (bl & 0xFF) + (br & 0xFF) + (bc & 0xFF) + (cl & 0xFF) + (cr & 0xFF)) >> 3) & 0xFF |
                            (((tl & 0xFF00) + (tr & 0xFF00) + (tc & 0xFF00) + (bl & 0xFF00) + (br & 0xFF00) + (bc & 0xFF00) + (cl & 0xFF00) + (cr & 0xFF00)) >> 3) & 0xFF00 |
                            (((tl & 0xFF0000) + (tr & 0xFF0000) + (tc & 0xFF0000) + (bl & 0xFF0000) + (br & 0xFF0000) + (bc & 0xFF0000) + (cl & 0xFF0000) + (cr & 0xFF0000)) >> 3) & 0xFF0000;
                }
            }
        }
        bmpT.setPixels(pix, 0, w, 0, 0, w, h);
        return bmpT;
    }
}
