package com.weareholidays.bia.activities.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.weareholidays.bia.R;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;

public class SearchFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private EditText searchText;
    private ProgressBar spinner;
    private View noResults;
    private TextView noResultsText;

    private static final String TAG = "SEARCH_FRAGMENT";

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View retView  = inflater.inflate(R.layout.fragment_search, container, false);
        setup(retView);
        return retView;
    }

    public void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromInputMethod(searchText.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onDestroyView() {
        hideKeyboard();
        super.onDestroyView();
    }

    private void setup(View retView) {

        searchText = (EditText)retView.findViewById(R.id.search_tab);
        spinner = (ProgressBar) retView.findViewById(R.id.progressBar1);
        noResults = retView.findViewById(R.id.no_results);
        noResultsText = (TextView) retView.findViewById(R.id.noresults);

        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    new SearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{searchText.getText().toString()});
                    return true;
                }
                return false;
            }
        });
    }

    private class SearchTask extends AsyncTask<String,Void,Void>{

        private int tripsCount = 0;
        private int usersCount = 0;
        private String searchString;

        @Override
        protected void onPreExecute(){
            spinner.setVisibility(View.VISIBLE);
            noResults.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(String... params) {
            if(params != null && params.length > 0){
                searchString = params[0];
                try {
                    tripsCount = TripUtils.searchTrips(searchString).count();
                } catch (ParseException e) {
                    Log.e(TAG,"Error searching trips",e);
                }

                try {
                    usersCount = TripUtils.searchUsers(searchString).count();
                } catch (ParseException e) {
                    Log.e(TAG,"Error searching users",e);
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {

            try {
                if (tripsCount + usersCount > 0) {
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    intent.putExtra(SearchActivity.TRIP_RESULT_COUNT, tripsCount);
                    intent.putExtra(SearchActivity.USER_RESULT_COUNT, usersCount);
                    intent.putExtra(SearchActivity.SEARCH_STRING, searchString);
                    startActivity(intent);
                }
                else{
                    noResults.setVisibility(View.VISIBLE);
                    noResultsText.setVisibility(View.VISIBLE);
                }
                spinner.setVisibility(View.GONE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
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

    }

}
