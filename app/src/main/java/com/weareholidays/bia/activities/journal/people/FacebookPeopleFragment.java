package com.weareholidays.bia.activities.journal.people;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.adapters.PhoneBookAdapter;
import com.weareholidays.bia.social.facebook.models.FacebookContact;
import com.weareholidays.bia.social.facebook.utils.FacebookUtils;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.weareholidays.bia.utils.DebugUtils;

import java.util.List;

//import twitter4j.auth.AccessToken;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FacebookPeopleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FacebookPeopleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FacebookPeopleFragment extends Fragment implements PhoneBookAdapter.OnContactChangedListener {

    private OnFragmentInteractionListener mListener;

    private List<FacebookContact> mContacts;
    private EditText search_bar;
    private TextView statusText;
    ListView mContactsListView;
    // An adapter that binds the result Cursor to the ListView
    private PhoneBookAdapter mCursorAdapter;
    private String mSearchString;
    private View statusPlaceHolder;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FacebookPeopleFragment.
     */
    public static FacebookPeopleFragment newInstance() {
        FacebookPeopleFragment fragment = new FacebookPeopleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public FacebookPeopleFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_facebook_people, container, false);
        search_bar = (EditText) getActivity().findViewById(R.id.search);
        statusPlaceHolder = rootView.findViewById(R.id.status_placeholder);
        statusText = (TextView) rootView.findViewById(R.id.status_text);
        statusText.setText(R.string.loading_facebook);
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchString = s.toString();
                //A unique id has to be given every time to initLoader or else it will check if
                //a cursor with same id exists and load it back again instead of preforming the search again
                if(mCursorAdapter != null)
                    mCursorAdapter.getFilter().filter(mSearchString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //nothing to do here
            }
        });
        GraphRequest request = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), FacebookUtils.getFriendsUrl(), new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                try{
                    if(graphResponse.getError() == null){
                        List<FacebookContact> contacts = FacebookContact.parseContacts(graphResponse.getJSONObject());
                        if(contacts.size() != 0){
                            mContacts = contacts;
                            for(PeopleContact ct : mContacts){
                                if(mListener.getContactIdentifierList().contains(ct.getIdentifier()))
                                    ct.setSelected(true);
                            }
                            mCursorAdapter = new PhoneBookAdapter(getActivity(), mContacts,FacebookPeopleFragment.this);
                            // Find list and bind to adapter
                            mContactsListView = (ListView) getActivity().findViewById(R.id.facebook_list);
                            mContactsListView.setAdapter(mCursorAdapter);
                            mContactsListView.setVisibility(View.VISIBLE);
                            statusPlaceHolder.setVisibility(View.GONE);
                            statusText.setVisibility(View.GONE);
                        }
                        else{
                            statusText.setText(R.string.no_facebook_friends);
                        }
                    }
                    else{
                        statusText.setText(R.string.error_loading_facebook);
                    }
                } catch (Exception e){
                    DebugUtils.logException(e);
                }
            }
        });
        request.setParameters(FacebookUtils.getUserFriendsBundle());
        request.executeAsync();
        return rootView;
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

    @Override
    public void onChanged(PeopleContact contact) {
        mListener.onContactChanged(contact);
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
        void onContactChanged(PeopleContact phoneBookContact);
        List<String> getContactIdentifierList();
    }

}
