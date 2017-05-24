package com.weareholidays.bia.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;
import com.weareholidays.bia.models.GalleryImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Teja on 29/06/15.
 */
public class MediaUtils {

    public static Cursor[] getImagesCursor(ContentResolver contentResolver, Date time){
        String[] columns = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DESCRIPTION, MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE};
        String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        Cursor cursor_ext = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, // Which columns to return
                MediaStore.Images.Media.DATE_TAKEN + ">?",
                new String[]{"" + time.getTime()},
                orderBy);
        // Create the cursor pointing to internal storage
        // even though the latest android phones have EXTERNAL STORAGE pointing to the internal content,
        // it is safe to use this for covering all kinds of phones
        Cursor cursor_int = contentResolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                columns, // Which columns to return
                MediaStore.Images.Media.DATE_TAKEN + ">?",
                new String[]{"" + time.getTime()},
                orderBy);
        Cursor[] cursor_array = {cursor_ext, cursor_int};
        return cursor_array;
    }

    public static File getMediaImageStoreLocation(){
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File thumbnails = new File(dcim, "/.thumbnails");
        File[] listOfImg = dcim.listFiles();
        File imgDir = null;
        if (dcim.isDirectory()){
            //for each child in DCIM directory
            for (int i = 0; i < listOfImg.length; ++i){
                //no thumbnails
                if( !listOfImg[i].getAbsolutePath().equals(thumbnails.getAbsolutePath()) ){
                    //only get the directory (100MEDIA, Camera, 100ANDRO, and others)
                    if(listOfImg[i].isDirectory()) {
                        String[] test = listOfImg[i].list();
                        if(imgDir == null){
                            imgDir = listOfImg[i];
                        }
                        if(test != null && test.length > 0){
                            imgDir = listOfImg[i];
                            break;
                        }
                    }
                }
            }
        }

        if(imgDir == null)
            imgDir = new File(dcim,"/Camera");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File image = new File(imgDir,imageFileName);

        return image;
    }

    /** Create a file Uri for saving an image or video */
    public static Uri getMediaImageStoreLocationUri() {
        return Uri.fromFile(getMediaImageStoreLocation());
    }



    private int columnIndex;
    private int dataColumnIndex;
    private int imageSizeColumnIndex;
    private int dateTakenColumnIndex;
    private int latitudeColumnIndex;
    private int longitudeColumnIndex;
    private int bucketIdColumnIndex;
    private int heightColumnIndex;
    private int widthColumnIndex;
    private List<String> bucketIds = new ArrayList<>();


    private MediaUtils(Cursor cursor){
        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        imageSizeColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
        dateTakenColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
        latitudeColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
        longitudeColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
        bucketIdColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
        widthColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
        heightColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
        processCameraFolders();
    }

    private void processCameraFolders() {
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
      //  File ext = Environment.getExternalStorageDirectory(Environment.DIRECTORY_DCIM);
        File thumbnails = new File(dcim, "/.thumbnails");
        File[] listOfImg = dcim.listFiles();
        if (dcim.isDirectory()){
            //for each child in DCIM directory
            for (int i = 0; i < listOfImg.length; ++i){
                //no thumbnails
                if( !listOfImg[i].getAbsolutePath().equals(thumbnails.getAbsolutePath()) ){
                    //only get the directory (100MEDIA, Camera, 100ANDRO, and others)
                    if(listOfImg[i].isDirectory()) {
                        bucketIds.add(getBucketId(listOfImg[i].getAbsolutePath()));
                    }
                }
            }
        }
    }

    private static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public static MediaUtils newInstance(Cursor cursor) {
        return new MediaUtils(cursor);
    }

    public GalleryImage getGalleryImage(Cursor cursor){
        String uriPath = cursor.getString(dataColumnIndex);
        long dateTaken = cursor.getLong(dateTakenColumnIndex);
        long imageSize = 0;
        if (imageSizeColumnIndex != -1)
            imageSize = cursor.getLong(imageSizeColumnIndex);
        //pass full path, using just getPath() wouldn't work
        GalleryImage galleryImage = new GalleryImage();
        galleryImage.setUri(uriPath);
        galleryImage.setSize(imageSize);
        galleryImage.setDateTaken(dateTaken);
        galleryImage.setBucketId(cursor.getString(bucketIdColumnIndex));
        galleryImage.setMediaWidth(cursor.getInt(widthColumnIndex));
        galleryImage.setMediaHeight(cursor.getInt(heightColumnIndex));
        if (latitudeColumnIndex != -1 && longitudeColumnIndex != -1) {
            double latitude = cursor.getDouble(latitudeColumnIndex);
            double longitude = cursor.getDouble(longitudeColumnIndex);
            galleryImage.setLatitude(latitude);
            galleryImage.setLongitude(longitude);
        }
        galleryImage.setType(GalleryImage.Type.PHONE);
        return galleryImage;
    }

    public boolean isCameraImage(GalleryImage galleryImage){
        if(bucketIds.contains(galleryImage.getBucketId()))
            return true;
        return false;
    }

    public static LatLng getLatLangFromFile(String file){

        try {
            ExifInterface exif = new ExifInterface(file);
            String attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            if((attrLATITUDE !=null)
                    && (attrLATITUDE_REF !=null)
                    && (attrLONGITUDE != null)
                    && (attrLONGITUDE_REF !=null))
            {
                float latitude;
                float longitude;

                if(attrLATITUDE_REF.equals("N")){
                    latitude = convertToDegree(attrLATITUDE);
                }
                else{
                    latitude = 0 - convertToDegree(attrLATITUDE);
                }

                if(attrLONGITUDE_REF.equals("E")){
                    longitude = convertToDegree(attrLONGITUDE);
                }
                else{
                    longitude = 0 - convertToDegree(attrLONGITUDE);
                }

                return new LatLng(latitude,longitude);
            }
        } catch (Exception e) {

        }
        return null;
    }

    private static Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;


    };
}
