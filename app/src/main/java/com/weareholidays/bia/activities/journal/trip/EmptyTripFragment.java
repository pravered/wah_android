package com.weareholidays.bia.activities.journal.trip;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.profile.BeenThereActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EmptyTripFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EmptyTripFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmptyTripFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static int readContactsRequestCode = 119;
    private static boolean contactsAllowedAfterRequest = false;
    private Boolean contactsAllowed = false;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EmptyTripFragment.
     */
    public static EmptyTripFragment newInstance() {
        EmptyTripFragment fragment = new EmptyTripFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public EmptyTripFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.empty_trip_fragment, container, false);
        setUp(rootView);
        return rootView;
    }

    private void setUp(View rootView) {
        rootView.findViewById(R.id.btnStartTripNow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    startTrip();
                }
                else {
                    checkContactsPermission();
                    if(contactsAllowed) {
                        startTrip();
                    }
                }
            }
        });

//        rootView.findViewById(R.id.textView3).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openPreviousTrips();
//            }
//        });
    }

    private void checkContactsPermission() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactsAllowed = true;
            return;
        } else {
            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, readContactsRequestCode);
        }
    }



    private boolean readContactsAllowed() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.READ_CONTACTS}, readContactsRequestCode);
        return contactsAllowedAfterRequest;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == readContactsRequestCode) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contactsAllowed = true;
//                this.contactsAllowedAfterRequest = true;
//                Toast.makeText(getActivity(), "Read Contacts Allowed", Toast.LENGTH_SHORT).show();
//                Toast.makeText(getActivity(), "contacts allowed", Toast.LENGTH_LONG).show();
            }
            else {
//                Toast.makeText(getActivity(), "Please allow contacts to start a trip", Toast.LENGTH_LONG).show();
            }
        }
    }

        private void openPreviousTrips(){
        if (isConnectingToInternet()){
            Intent intent = new Intent(getActivity(), BeenThereActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);}
        else {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.no_net)
                    .content(R.string.no_net_msg)
                    .positiveText(R.string.ok)
                    .positiveColor(getResources().getColor(R.color.orange_primary))
                    .show();
        }
    }


    public void startTrip() {
        Intent intent = new Intent(getActivity(), TripStartActivity.class);
        startActivity(intent);
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

    public boolean isConnectingToInternet(){
        Context context = getActivity().getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

}
