package com.weareholidays.bia.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.felipecsl.asymmetricgridview.library.Utils;
import com.parse.ParseGeoPoint;
import com.weareholidays.bia.WAHApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Teja on 14/07/15.
 */
public class MapUtils {

    public static String getCheckInMapImageUrl(ParseGeoPoint parseGeoPoint, DisplayMetrics displayMetrics) {
        //TODO: USE API KEY AND ADJUST MAP IMAGE SIZE
        return "https://maps.googleapis.com/maps/api/staticmap?size=640x100&scale=2&zoom=14&maptype=roadmap&markers=color:red%7Clabel:C%7C" + parseGeoPoint.getLatitude() + "," + parseGeoPoint.getLongitude();
    }

    public static String getCheckInMapImageUrl(ParseGeoPoint parseGeoPoint, int width, int height) {
        return "https://maps.googleapis.com/maps/api/staticmap?size=" + (width / 2) + "x" + (height / 2) + "&scale=2&zoom=14&maptype=roadmap&markers=color:red%7Clabel:C%7C" + parseGeoPoint.getLatitude() + "," + parseGeoPoint.getLongitude();
    }

    public static String getPhotoMapImageUrl(double latitude, double longitude) {
        return "https://maps.googleapis.com/maps/api/staticmap?size=640x100&scale=2&zoom=14&maptype=roadmap&markers=color:red%7Clabel:C%7C" + latitude + "," + longitude;
    }

    public static String getTimelineMapImageUrl(Context context, ParseGeoPoint parseGeoPoint, DisplayMetrics displayMetrics) {
        int height = Utils.dpToPx(context, 140) / 2;
        int width = displayMetrics.widthPixels / 2;

        return "https://maps.googleapis.com/maps/api/staticmap?size=" + width + "x" + height + "&scale=2&zoom=10&maptype=roadmap&center=" + parseGeoPoint.getLatitude() + "," + parseGeoPoint.getLongitude()
                + "&style=feature:all%7Celement:labels%7Clightness:50";
    }

    public static String getPhotoReferenceUrl(String photReference, DisplayMetrics displayMetrics) {
        try {
            return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + displayMetrics.widthPixels
                    + "&photoreference="
                    + URLEncoder.encode(photReference, "UTF-8")
                    + "&key="
                    + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8");
        } catch (Exception e) {

        }
        return "";
    }

    public static boolean isEqual(ParseGeoPoint p1, ParseGeoPoint p2) {
        if (p1 == null || p2 == null)
            return false;
        if (p1.getLatitude() == p2.getLatitude() && p1.getLongitude() == p2.getLongitude())
            return true;

        return false;
    }

    public static String getGooglePlaceSearch(String latitude, String longitude, HashMap<String, String> params) throws UnsupportedEncodingException {
        String request;
        request = "https://maps.googleapis.com/maps/api/place/search/json?location="
                + URLEncoder.encode(latitude, "UTF-8")
                + ","
                + URLEncoder.encode(longitude, "UTF-8");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            request = request + "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
        }

        request = request
                + "&key="
                + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8");

        return makeCall(request);
    }

    public static String getGooglePlaceDetails(String reference) throws UnsupportedEncodingException {
        return makeCall("https://maps.googleapis.com/maps/api/place/details/json?location="
                + "&sensor="
                + URLEncoder.encode("true", "UTF-8")
                + "&placeid="
                + URLEncoder.encode(reference, "UTF-8")
                + "&key="
                + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8"));
    }

    public static String getGooglePlaceTextSearch(String searchText) throws UnsupportedEncodingException{
        return makeCall("https://maps.googleapis.com/maps/api/place/textsearch/json?location="
                + "&query="
                + URLEncoder.encode(searchText, "UTF-8")
                + "&key="
                + URLEncoder.encode(WAHApplication.GOOGLE_KEY, "UTF-8"));
    }

    public static String makeCall(String urlStr) {

        // string buffers the url
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlStr);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            //Log.d("Exception while downloading url", e.toString());
        } finally {
            try {
                iStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }

        // trim the whitespaces
        return data.trim();
    }

}
