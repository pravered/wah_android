package com.weareholidays.bia.activities.journal.photo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.models.ImageViewHolder;
import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.GalleryImageAdapter;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//import twitter4j.auth.AccessToken;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FacebookPhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FacebookPhotosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FacebookPhotosFragment extends Fragment {

    public static final String DATE_ARG = "DATE_ARG";

    private OnFragmentInteractionListener mListener;

    private static String SAVED_FACEBOOK_IMAGES = "SAVED_FACEBOOK_IMAGES";

    private Date facebookSinceTime;

    private ArrayList<GalleryImage> mPhotos;
    // Grid view holding the images.
    private GridView sdcardImages;
    //Image adapter for the grid view.
    public GalleryImageAdapter imageAdapter;
    private TripOperations tripOperations;
    private List<GalleryImage> previous_selections;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FacebookPhotosFragment.
     */
    public static FacebookPhotosFragment newInstance(Date time) {
        FacebookPhotosFragment fragment = new FacebookPhotosFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATE_ARG,time);
        fragment.setArguments(args);
        return fragment;
    }

    public FacebookPhotosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            facebookSinceTime = (Date) getArguments().getSerializable(DATE_ARG);
        }
        tripOperations = TripUtils.getInstance().getCurrentTripOperations();
    }

    private void loadImages(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Object images = savedInstanceState.getSerializable(SAVED_FACEBOOK_IMAGES);
            if(images != null){
                mPhotos = (ArrayList<GalleryImage>)images;
                if(mPhotos.size() > 0)
                    return;
            }
        }
        new LoadFacebookImagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_facebook_photos, container, false);
        if(mPhotos == null){
            mPhotos = new ArrayList<>();
        }
        imageAdapter = new GalleryImageAdapter(getActivity().getApplicationContext(), mPhotos);
        // initialize the GridView
        sdcardImages = (GridView) fragmentView.findViewById(R.id.gridview);
        sdcardImages.setAdapter(imageAdapter);

        sdcardImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                if(position == 0) {
                    mListener.takePicture();
                    return;
                }

                ImageViewHolder holder = (ImageViewHolder) v.getTag();
                ImageView checkmark = holder.checkbox;
                ImageView image = holder.imageview;
                GalleryImage mPhoto = mPhotos.get(position);
                if (checkmark.getVisibility() == View.GONE) {
                    checkmark.setVisibility(View.VISIBLE);
                    mPhoto.setSelected(true);
                    holder.borderView.setVisibility(View.VISIBLE);
                } else {
                    checkmark.setVisibility(View.GONE);
                    holder.borderView.setVisibility(View.GONE);
                    mPhoto.setSelected(false);
                }
                mListener.onImageSelected(mPhoto);
            }
        });

        loadImages(savedInstanceState);

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedState){
        super.onSaveInstanceState(savedState);
        savedState.putSerializable(SAVED_FACEBOOK_IMAGES, mPhotos);
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

    private class LoadFacebookImagesTask extends AsyncTask<Void,GalleryImage,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            GalleryImage gImage = new GalleryImage(true);
            publishProgress(gImage);
            AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
            if (facebookAccessToken != null && !facebookAccessToken.isExpired()) {
                boolean pollFacebook = true;
                String nextPage = "";
                while(pollFacebook){
                    pollFacebook = false;
                    GraphRequest graphRequest = null;
                    if(TextUtils.isEmpty(nextPage)){
                        graphRequest = GraphRequest.newGraphPathRequest(facebookAccessToken, FacebookUtils.getUserPhotosUrl(), new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse graphResponse) {

                            }
                        });

                        graphRequest.setParameters(FacebookUtils.getUserPhotosBundle(facebookSinceTime));
                    }
                    else{
                        graphRequest = GraphRequest.newGraphPathRequest(facebookAccessToken, nextPage, new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse graphResponse) {

                            }
                        });
                    }
                    GraphResponse graphResponse = graphRequest.executeAndWait();
                    if (graphResponse.getError() == null) {
                        JSONObject jsonObject = graphResponse.getJSONObject();
                        List<GalleryImage> galleryImageList = FacebookUtils.getUserImages(jsonObject);
                        if(galleryImageList.size() > 0){
                            GalleryImage[] galleryImages = new GalleryImage[galleryImageList.size()];
                            publishProgress(galleryImageList.toArray(galleryImages));
                        }
                        nextPage = FacebookUtils.getNextPageUrl(jsonObject);
                        if (!TextUtils.isEmpty(nextPage)) {
                            pollFacebook = true;
                        }
                    }
                    else {
                        Log.e("FacebookPhotos","Error getting facebook photos",graphResponse.getError().getException());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(GalleryImage... values) {
            if(values.length > 0){
                if ( mPhotos == null ) {
                    mPhotos = new ArrayList<>();
                }
                // check if the current photo was selected or not (case when coming back after clicking picture from camera)
                if (tripOperations.getSelectedPhotosList() != null) {
                    previous_selections = tripOperations.getSelectedPhotosList();
                    for (GalleryImage image: previous_selections) {
                        if (image.getType() == GalleryImage.Type.FB) {
                            for (GalleryImage gImage: values) {
                                if (image.getSourceId().equals(gImage.getSourceId())) {
                                    gImage.setSelected(image.isSelected());
                                }
                            }
                        }
                    }
                }
                mPhotos.addAll(Arrays.asList(values));
                imageAdapter.notifyDataSetChanged();
            }
        }
    }

}
