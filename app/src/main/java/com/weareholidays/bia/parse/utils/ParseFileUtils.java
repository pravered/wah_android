package com.weareholidays.bia.parse.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.weareholidays.bia.WAHApplication;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.local.PrivateMedia;
import com.weareholidays.bia.utils.DebugUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Teja on 03/06/15.
 */
public class ParseFileUtils {

    public static final String PRIVATE_DIRECTORY_NAME = "BIA_Images";
    public static String LOCAL_FILE_OFFLINE = "local_file_offline";
    private static String TAG = ParseFileUtils.class.getName();

    private static <T extends ParseObject> ParseQuery<FileLocal> fileLocalParseQuery(T parseObject) {
        return ParseQuery.getQuery(FileLocal.class).whereEqualTo(FileLocal.PROXY_CLASS, parseObject).setLimit(1)
                .fromPin(LOCAL_FILE_OFFLINE);
    }

    public static <T extends ParseObject> FileLocal getLocalFileFromPin(@NonNull T parseObject) throws ParseException {
        List<FileLocal> fileLocals = fileLocalParseQuery(parseObject).find();
        if (fileLocals != null && fileLocals.size() > 0) {
            return fileLocals.get(0);
        }
        return null;
    }

    public static <T extends ParseObject> void clearLocalFiles(T parseObject) throws ParseException {
        List<FileLocal> fileLocals = fileLocalParseQuery(parseObject).find();
        if (fileLocals != null && fileLocals.size() > 0) {
            ParseObject.unpinAll(LOCAL_FILE_OFFLINE, fileLocals);
        }
    }


    public static <T extends ParseObject> void getLocalFileFromPinInBackground(@NonNull T parseObject, @NonNull GetCallback<FileLocal> callback) {
        fileLocalParseQuery(parseObject).getFirstInBackground(callback);
    }

    public static <T extends ParseObject> FileLocal pinFileLocally(@NonNull T parseObject
            , @NonNull String objectField, @NonNull Uri localUri, String fileName) throws ParseException {
        List<FileLocal> fileLocals = fileLocalParseQuery(parseObject).find();
        FileLocal fileLocal = null;
        if (fileLocals != null && fileLocals.size() > 0) {
            ParseObject.unpinAll(LOCAL_FILE_OFFLINE, fileLocals);
        }
        if (fileLocal == null) {
            fileLocal = new FileLocal();
        }
        fileLocal.setProxyClass(parseObject);
        fileLocal.setLocalUri(localUri.toString());
        fileLocal.setFileName(fileName);
        fileLocal.setProxyField(objectField);
        fileLocal.setFileUploaded(false);
        fileLocal.setFileLinked(false);
        fileLocal.pin(LOCAL_FILE_OFFLINE);
        return fileLocal;
    }

    public static <T extends ParseObject> FileLocal pinFileLocallyInBackground(@NonNull T parseObject
            , @NonNull String objectField, @NonNull Uri localUri, String fileName) throws ParseException {
        List<FileLocal> fileLocals = fileLocalParseQuery(parseObject).find();
        FileLocal fileLocal = null;
        if (fileLocals != null && fileLocals.size() > 0) {
            ParseObject.unpinAll(LOCAL_FILE_OFFLINE, fileLocals);
        }
        if (fileLocal == null) {
            fileLocal = new FileLocal();
        }
        fileLocal.setProxyClass(parseObject);
        fileLocal.setLocalUri(localUri.toString());
        fileLocal.setFileName(fileName);
        fileLocal.setProxyField(objectField);
        fileLocal.setFileUploaded(false);
        fileLocal.setFileLinked(false);
        fileLocal.pinInBackground(LOCAL_FILE_OFFLINE);
        return fileLocal;
    }

    public static List<FileLocal> storedGalleryImages() throws ParseException {
        return ParseQuery.getQuery(FileLocal.class).whereEqualTo(FileLocal.PROXY_FIELD, Media.CONTENT)
                .fromPin(LOCAL_FILE_OFFLINE).find();
    }

    public static <T extends ParseObject> FileLocal pinFileBitmapLocally(@NonNull T parseObject
            , @NonNull String objectField, @NonNull Bitmap bitmap, String fileName) throws ParseException {
        List<FileLocal> fileLocals = fileLocalParseQuery(parseObject).find();
        FileLocal fileLocal = null;
        if (fileLocals != null && fileLocals.size() > 0) {
            ParseObject.unpinAll(LOCAL_FILE_OFFLINE, fileLocals);
        }
        if (fileLocal == null) {
            fileLocal = new FileLocal();
        }
        fileLocal.setProxyClass(parseObject);
        fileLocal.setLocalUri(saveBitmap(bitmap).toString());
        fileLocal.setFileName(fileName);
        fileLocal.setProxyField(objectField);
        fileLocal.setFileUploaded(false);
        fileLocal.setFileLinked(false);
        fileLocal.pin(LOCAL_FILE_OFFLINE);
        return fileLocal;
    }

    public static <T extends ParseObject> FileLocal pinFileLocallyPrivate(@NonNull T parseObject
            , @NonNull String objectField, @NonNull Uri localUri, String fileName) throws ParseException {
        List<FileLocal> fileLocals = fileLocalParseQuery(parseObject).find();
        FileLocal fileLocal = null;
        if (fileLocals != null && fileLocals.size() > 0) {
            ParseObject.unpinAll(LOCAL_FILE_OFFLINE, fileLocals);
        }
        if (fileLocal == null) {
            fileLocal = new FileLocal();
        }
        fileLocal.setProxyClass(parseObject);
        fileLocal.setLocalUri(saveToPrivateLocation(localUri).toString());
        fileLocal.setFileName(fileName);
        fileLocal.setProxyField(objectField);
        fileLocal.setFileUploaded(false);
        fileLocal.setFileLinked(false);
        fileLocal.pin(LOCAL_FILE_OFFLINE);
        return fileLocal;
    }

    public static ParseFile convertToParseFile(@NonNull FileLocal fileLocal, @NonNull Context context) throws FileNotFoundException{
        Log.i(TAG, "Local file url: " + fileLocal.getLocalUri());
        byte[] data = convertImageToBytes(fileLocal.getLocalUri(), context);
        String filename = fileLocal.getFileName();
        if (TextUtils.isEmpty(filename))
            filename = "no_name.jpeg";
        if (data != null)
            return new ParseFile(filename, data , "image/jpeg");
        return null;
    }

    private static ParseObject updateFileLink(ParseFile parseFile, FileLocal fileLocal) {
        ParseObject parseObject = fileLocal.getProxyClass();
        parseObject.put(fileLocal.getProxyField(), parseFile);
        return parseObject;
    }

    public static <T extends ParseObject> void uploadParseFile(T parseObject) throws ParseException {
        FileLocal fileLocal = getLocalFileFromPin(parseObject);
        if (fileLocal != null) {
            if (fileLocal.isFileLinked()) {
                return;
            }
            ParseFile file = null;
            try {
                file = convertToParseFile(fileLocal, WAHApplication.getWAHContext());
            }
            catch(FileNotFoundException e){
                DebugUtils.logException(e);
            }
            if (file != null) {
                file.save();
                updateFileLink(file, fileLocal);
                parseObject.save();
                fileLocal.setFileLinked(true);
                fileLocal.pinInBackground(LOCAL_FILE_OFFLINE);
            }
        }
    }

    public static Uri saveToPrivateLocation(@NonNull Uri uri) {
        File myRepo = WAHApplication.getWAHContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (uri.getPath().contains(myRepo.getPath())) {
            return uri;
        }

        uri = runUriCheck(uri);

        if (uri.getScheme() != null && (uri.getScheme().startsWith("http")))
            return uri;
        File file = new File(myRepo.getPath() + File.separatorChar + "BIA_" + System.nanoTime() + Math.random());
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        URLConnection ucon;
        try {
            ContentResolver cr = WAHApplication.getWAHContext().getContentResolver();
            bis = new BufferedInputStream(cr.openInputStream(uri));

            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
            return Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null)
                    bis.close();
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uri;
    }

    private static Uri saveBitmap(Bitmap bitmap) {
        File myRepo = WAHApplication.getWAHContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(myRepo.getPath() + File.separatorChar + "file-" + System.nanoTime() + Math.random());
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }
        return Uri.fromFile(file);

    }

    public static BitmapFactory.Options decodeBitMapOptions(String uri) {
        BufferedInputStream inputStream = null;
        try {
            ContentResolver cr = WAHApplication.getWAHContext().getContentResolver();
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            inputStream = new BufferedInputStream(cr.openInputStream(Uri.parse(uri)));
            BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
            return bitmapOptions;
        } catch (Exception e) {

        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {

            }
        }
        return null;
    }

    public static byte[] convertImageToBytes(@NonNull Uri uri, @NonNull Context context) {
        byte[] data = null;
        uri = runUriCheck(uri);

        try {
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            Log.w("File not found for uri", uri.toString());
        }
        return data;
    }

    public static byte[] convertFileToByteFormat(String uri, Context context) {
        File file = new File(uri);
        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try {
            if(!uri.contains("http")) {
                ios = new FileInputStream(file);
                ios.read(buffer);
            } else {
//                ios = new URL(uri).openStream();
                ios =  new URL(uri).openStream();
                buffer = getBytes(ios);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG,"FileNotFoundException : " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException : " + e.toString());
        } finally {
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException : " + e.toString());
            }
        }
        return buffer;
    }


    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static byte[] convertImageToBytes(@NonNull String uri, @NonNull Context context) throws FileNotFoundException{
        //return convertImageToBytesWithCompression(runUriCheck(Uri.parse(uri)),context);
        return convertFileToByteFormat(uri, context);
    }

    private static Uri runUriCheck(Uri uri) {
        if (uri.getScheme() == null && uri.getPath().startsWith("/")) {
            uri = Uri.fromFile(new File(uri.getPath()));
        }
        return uri;
    }

    public static String getFilename(String path) { //NO
        File[] files = ContextCompat.getExternalFilesDirs(WAHApplication.getWAHContext(), PRIVATE_DIRECTORY_NAME);
        File file = files[0];  //new File(Environment.getExternalStorageDirectory().getPath(), "BiaApp/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        File oldFile = new File(path);
        String uriSting = (file.getAbsolutePath() + File.separatorChar + oldFile.getName());
        return uriSting;
    }

    /**
     * deletes local image directory
     */
    public static void deleteImageDirectory(){
        File[] files = ContextCompat.getExternalFilesDirs(WAHApplication.getWAHContext(), PRIVATE_DIRECTORY_NAME);
        File file = files[0];
        deletePrivateDirectory(file);
    }

    /**
     * deletes local image directory recursively
     */
    public static void deletePrivateDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deletePrivateDirectory(child);

        fileOrDirectory.delete();
    }

    /**
     * makes copy of image on private location
     */
    public static PrivateMedia copyImageToPrivateLocation(String imageUrl, Context context) {
        Uri imageUri = Uri.parse(imageUrl);
        imageUri = runUriCheck(imageUri);
        String filePath = getRealPathFromURI(imageUri.toString(), context);
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 960x960
        float maxHeight = 960.0f;
        float maxWidth = 960.0f;
        float imgRatio = (float) actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
            // hence abort, when bmp is null
            if(bmp==null)
                return null;
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream out = null;
        String filename = getFilename(imageUrl);
        try {
            out = new FileOutputStream(filename);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File file = new File(filename);
        return new PrivateMedia(filename,scaledBitmap.getHeight(),scaledBitmap.getWidth(),file.length());
    }

    public static byte[] convertImageToBytesWithCompression(Uri imageUri,Context context) throws FileNotFoundException{
        byte[] data = null;
        imageUri = runUriCheck(imageUri);
        String filePath = getRealPathFromURI(imageUri.toString(),context);
        Log.d(TAG,"filepath: "+filePath);

        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

//        if(bmp == null){
//            throw new FileNotFoundException(filePath);
//        }

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 1920x1920
        float maxHeight = 960.0f;
        float maxWidth = 960.0f;
        float imgRatio = (float) actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        data = baos.toByteArray();
        return data;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private static String getRealPathFromURI(String contentURI,Context context) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            /* vijay: upload trip was failing if the trip had 'people' with 'avatars' as
             * there is no column named MediaStore.Images.ImageColumns.DATA in
             * contacts cursor. here, we've handled that so that upload trip does not
             * fail but contacts' avatars are still not uploaded as the path returned
             * from here is like contacts://... and our code handles only filesystem paths
             * TODO: fix it.
             */
            if(contentURI.startsWith("content://com.android.contacts/")) {
                int index = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
                return cursor.getString(index);
            }
            else{ //if(contentURI.startsWith("content://com.android.contacts/")) {
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                return cursor.getString(index);
            }
        }
        return contentUri.getPath();
    }
}
