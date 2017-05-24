package com.weareholidays.bia.activities.journal.photo;

import android.app.Activity;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.AsyncTask;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.models.ImageViewHolder;
import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.GalleryImageAdapter;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MediaUtils;

/**
 * Loads images from SD card using AsyncTask
 * @author kapil
 */

public class PhotosFromGalleryFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    //arraylist storing all the uris passed to the context
    private ArrayList<GalleryImage> mPhotos;
    // Grid view holding the images.
    private GridView sdcardImages;
    //Image adapter for the grid view.
    public GalleryImageAdapter imageAdapter;

    private static String SAVED_GALLERY_IMAGES = "SAVED_GALLERY_IMAGES";

    private TripOperations tripOperations;
    private List<GalleryImage> previous_selections;

    public PhotosFromGalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Async task for loading the images from the SD card.
     */
    public class LoadImagesFromSDCard extends AsyncTask<Void, GalleryImage, Void> {

        private boolean noImagesFound;
        /**
         * Load images from SD Card in the background, and display each image on the screen.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try{
                Set<String> existingUris = new HashSet<>();
                try {
                    for(FileLocal fileLocal : ParseFileUtils.storedGalleryImages()){
                        existingUris.add(fileLocal.getLocalUri());
                    }

                } catch (Exception e) {
                    DebugUtils.logException(e);
                }
                GalleryImage gImage = new GalleryImage(true);
                publishProgress(gImage);
                // Set up an array of the Image ID column we want
                Boolean isSDPresent = android.os.Environment.getExternalStorageState()
                        .equals(android.os.Environment.MEDIA_MOUNTED );
                if (isSDPresent) {
                    Cursor[] cursor_array = MediaUtils.getImagesCursor(getActivity().getContentResolver()
                            , tripOperations.getTrip().getStartTime());
                    MergeCursor cursor = new MergeCursor(cursor_array);
                    MediaUtils mediaUtils = MediaUtils.newInstance(cursor);

                    int size = cursor.getCount();
                    // If size is 0, there are no images on the SD Card/phone.
                    if (size == 0) {
                        //No Images available, post some message to the user
                        noImagesFound = true;
                    } else {
                        for (int i = 0; i < size; i++) {
                            boolean flag = false;
                            cursor.moveToPosition(i);
                            GalleryImage galleryImage = mediaUtils.getGalleryImage(cursor);
                            if(mediaUtils.isCameraImage(galleryImage)) {
                                //disable camera roll images
                                if (existingUris.contains(galleryImage.getUri()))
                                    galleryImage.setDisabled(true);
                                List<FileLocal> fileLocalList = ParseFileUtils.storedGalleryImages();
                                for(FileLocal file : fileLocalList){
                                    if(file.getLocalUri().contains(galleryImage.getName())) {
                                        flag = true;
                                        break;
                                    }
                                }
                                if(!flag)
                                    publishProgress(galleryImage);
                            }
                        }
                        cursor.close();
                    }
                }
            }
            catch (Exception e){
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(GalleryImage... values) {
            addImages(values[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            if(isCancelled())
                return;
            if(noImagesFound){
                String msg = "No images found";
//                Toast.makeText(getActivity(),msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tripOperations = TripUtils.getInstance().getCurrentTripOperations();
        //load images into arraylist
        loadImages(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_photos_from_gallery, container, false);
        if(mPhotos == null){
            mPhotos = new ArrayList<>();
        }
        imageAdapter = new GalleryImageAdapter(getActivity().getApplicationContext(), mPhotos);
        // initialize the GridView
        sdcardImages = (GridView) fragmentView.findViewById(R.id.gridview);
        sdcardImages.setAdapter(imageAdapter);

        //show custom check mark when clicked
        sdcardImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                if(position == 0) {
                    mListener.takePicture();
                } else {
                    ImageViewHolder holder = (ImageViewHolder) v.getTag();
                    ImageView checkmark = holder.checkbox;
                    GalleryImage mPhoto = mPhotos.get(position);
                    if (!mPhoto.isDisabled()) {
                        if (checkmark.getVisibility() == View.GONE) {
                            checkmark.setVisibility(View.VISIBLE);
                            mPhoto.setSelected(true);
                            holder.borderView.setVisibility(View.VISIBLE);
                        } else {
                            holder.borderView.setVisibility(View.GONE);
                            checkmark.setVisibility(View.GONE);
                            mPhoto.setSelected(false);
                        }
                        mListener.onImageSelected(mPhoto);
                    }
                }
            }

        });
        return fragmentView;
    }

    /**
     * Load images.
     */
    private void loadImages(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Object images = savedInstanceState.getSerializable(SAVED_GALLERY_IMAGES);
            if(images != null){
                mPhotos = (ArrayList<GalleryImage>)images;
                if(mPhotos.size() > 0)
                    return;
            }

        }
        new LoadImagesFromSDCard().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    /**
     * Add image(s) to the grid view adapter.
     *
     * @paramvalue Array of string references to path of images
     */
    private void addImages(GalleryImage galleryImage) {
        if ( mPhotos == null ) {
            mPhotos = new ArrayList<>();
        }
        // check if the current photo was selected or not (case when coming back after clicking picture from camera)
        if (tripOperations.getSelectedPhotosList() != null) {
            previous_selections = tripOperations.getSelectedPhotosList();
            for (GalleryImage image: previous_selections) {
                if (image.getUri().equals(galleryImage.getUri()) && image.getType() == GalleryImage.Type.PHONE) {
                    galleryImage.setSelected(image.isSelected());
                }
            }
        }

        mPhotos.add(galleryImage);
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState){
        super.onSaveInstanceState(savedState);
        savedState.putSerializable(SAVED_GALLERY_IMAGES, mPhotos);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onImageSelected(GalleryImage galleryImage);
        void takePicture();
    }

}

