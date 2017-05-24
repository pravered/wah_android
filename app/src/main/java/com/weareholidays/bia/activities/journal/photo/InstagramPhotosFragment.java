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

import com.parseInsta.InstagramSession;
import com.parseInsta.ParseInstagramUtil;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.models.ImageViewHolder;
import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.GalleryImageAdapter;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.social.instagram.models.InstagramPost;
import com.weareholidays.bia.social.instagram.utils.InstagramUtils;
//import com.parse.ParseInstagramUtils;
import com.parse.ParseUser;
//import com.parse.instagram.Instagram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InstagramPhotosFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InstagramPhotosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstagramPhotosFragment extends Fragment {

    public static final String DATE_ARG = "DATE_ARG";

    private OnFragmentInteractionListener mListener;

    private static String SAVED_INSTAGRAM_IMAGES = "SAVED_INSTAGRAM_IMAGES";

    private Date instagramSinceTime;

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
     * @return A new instance of fragment InstagramPhotosFragment.
     */
    public static InstagramPhotosFragment newInstance(Date time) {
        InstagramPhotosFragment fragment = new InstagramPhotosFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATE_ARG,time);
        fragment.setArguments(args);
        return fragment;
    }

    public InstagramPhotosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            instagramSinceTime = (Date) getArguments().getSerializable(DATE_ARG);
        }
        tripOperations = TripUtils.getInstance().getCurrentTripOperations();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_instagram_photos, container, false);
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

                if(position == 0) {mListener.takePicture();
                    return;
                }

                ImageViewHolder holder = (ImageViewHolder) v.getTag();
                ImageView checkmark = holder.checkbox;
                GalleryImage mPhoto = mPhotos.get(position);
                if (checkmark.getVisibility() == View.GONE) {
                    checkmark.setVisibility(View.VISIBLE);
                    holder.borderView.setVisibility(View.VISIBLE);
                    mPhoto.setSelected(true);
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
        savedState.putSerializable(SAVED_INSTAGRAM_IMAGES, mPhotos);
    }

    private void loadImages(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Object images = savedInstanceState.getSerializable(SAVED_INSTAGRAM_IMAGES);
            if(images != null){
                mPhotos = (ArrayList<GalleryImage>)images;
                if(mPhotos.size() > 0)
                    return;
            }
        }
        new LoadInstagramImagesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private class LoadInstagramImagesTask extends AsyncTask<Void,GalleryImage,Void>{

//        @Override
//        protected Void doInBackground(Void... params) {
//            GalleryImage gImage = new GalleryImage(true);
//            publishProgress(gImage);
//            if(ParseInstagramUtils.isLinked(ParseUser.getCurrentUser())){
//                Instagram instagram = ParseInstagramUtils.getInstagram();
//                String instUrl = InstagramUtils.getUserPostsUrl(instagramSinceTime, instagram.getAccessToken());
//                while (instUrl != null){
//                    InputStream iStream = null;
//                    HttpURLConnection urlConnection = null;
//                    JSONObject jsonObject = null;
//                    String nextUrl = null;
//                    try{
//                        URL url = new URL(instUrl);
//
//                        // Creating an http connection to communicate with url
//                        urlConnection = (HttpURLConnection) url.openConnection();
//
//                        // Connecting to url
//                        urlConnection.connect();
//
//                        // Reading data from url
//                        iStream = urlConnection.getInputStream();
//
//                        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//
//                        StringBuffer sb  = new StringBuffer();
//
//                        String line = "";
//                        while( ( line = br.readLine())  != null){
//                            sb.append(line);
//                        }
//
//                        jsonObject = new JSONObject(sb.toString());
//                        br.close();
//                        List<InstagramPost> posts = InstagramUtils.getInstagramPosts(jsonObject);
//                        if(posts.size() > 0){
//                            List<GalleryImage> galleryImageList = InstagramUtils.getInstagramGalleryImages(posts);
//                            if(galleryImageList.size() > 0){
//                                GalleryImage[] galleryImages = new GalleryImage[galleryImageList.size()];
//                                publishProgress(galleryImageList.toArray(galleryImages));
//                            }
//                        }
//                        if(jsonObject.has("pagination")){
//                            JSONObject pagination = jsonObject.getJSONObject("pagination");
//                            if(pagination.has("next_url")){
//                                nextUrl = pagination.getString("next_url");
//                            }
//                        }
//                    }catch(Exception e){
//                        Log.e("Instagram Photos","Error reading instagram posts", e);
//                    }finally{
//                        try{
//                            iStream.close();
//                            urlConnection.disconnect();
//                        }
//                        catch (Exception e){
//                        }
//                        instUrl = null;
//                        if(nextUrl != null)
//                            instUrl = nextUrl;
//                    }
//                }
//            }
//            return null;
//        }

        @Override
        protected Void doInBackground(Void... params) {
            GalleryImage galleryImage = new GalleryImage(true);
            publishProgress(galleryImage);
            if (!TextUtils.isEmpty(new InstagramSession(getContext()).getAccessToken())) {
                JSONObject instagramData = ParseInstagramUtil.getUserData(getContext());
                if (instagramData != null) {
                    JSONObject dataAfterTrip = dataAfterTripStart(instagramData);
                    List<InstagramPost> posts = null;
                    try {
                        posts = InstagramUtils.getInstagramPosts(dataAfterTrip);
                        if (posts.size() > 0) {
                            List<GalleryImage> galleryImageList = InstagramUtils.getInstagramGalleryImages(posts);
                            if (galleryImageList.size() > 0) {
                                GalleryImage[] galleryImages = new GalleryImage[galleryImageList.size()];
                                publishProgress(galleryImageList.toArray(galleryImages));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                        if (image.getType() == GalleryImage.Type.INSTAGRAM) {
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

    private JSONObject dataAfterTripStart(JSONObject jsonObject) {
        JSONObject filteredObject = new JSONObject();
        JSONArray filteredImages = new JSONArray();
        try {
            JSONArray allImages = jsonObject.getJSONArray("data");
            for (int i = 0; i < allImages.length(); i++) {
                Long imageCreationTime = Long.parseLong(allImages.getJSONObject(i).getString("created_time"));
                if (imageCreationTime * 1000 >= instagramSinceTime.getTime()) {
                    filteredImages.put(allImages.getJSONObject(i));
                }
            }
            filteredObject.put("pagination", jsonObject.getJSONObject("pagination"));
            filteredObject.put("data", filteredImages);
            filteredObject.put("meta", jsonObject.getJSONObject("meta"));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return filteredObject;
    }

}
