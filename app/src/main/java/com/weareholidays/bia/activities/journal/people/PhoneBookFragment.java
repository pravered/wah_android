package com.weareholidays.bia.activities.journal.people;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.weareholidays.bia.activities.journal.people.models.PeopleContact;
import com.weareholidays.bia.activities.journal.people.models.PhoneBookContact;
import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.PhoneBookAdapter;
import com.weareholidays.bia.utils.DebugUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PhoneBookFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PhoneBookFragment extends Fragment implements LoaderCallbacks<Cursor>, PhoneBookAdapter.OnContactChangedListener {
    // Define a ListView object
    ListView mContactsListView;
    // An adapter that binds the result Cursor to the ListView
    private PhoneBookAdapter mCursorAdapter;
    //private ArrayList<PhoneBookContact> mContacts;
    // Defines the id of the loader for later reference
    public static final int CONTACT_LOADER_ID = 1778; // From docs: A unique identifier for this loader. Can be whatever you want.
    private OnFragmentInteractionListener mListener;
    private EditText search_bar;
    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ? AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + " LIKE 1";
    //initial selection criteria (people with one phone no atleast)
    private static final String INITIAL_SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + " != 0" ;
    private static final String orderBy = ContactsContract.Contacts.DISPLAY_NAME + " ASC";
    // Defines a variable for the search string
    private String mSearchString;
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };
    private static String SAVED_PHONE_CONTACTS = "SAVED_PHONE_CONTACTS";

    private Cursor mCursor;

    private List<PhoneBookContact> mContacts;

    private TextView statusText;

    private ContactsTask contactsTask;

    private View statusPlaceHolder;


    public PhoneBookFragment() {
        // Required empty public constructor
    }

    // Create and return the actual cursor loader for the contacts data
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // Define the columns to retrieve
        String[] projectionFields =  new String[] { ContactsContract.Contacts._ID,
                                                    ContactsContract.Contacts.DISPLAY_NAME,
                                                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI };
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        mSelectionArgs[0] = "%" + mSearchString + "%";

        CursorLoader cursorLoader;
        //for initial loading of all contacts or when count is 0
        if (loaderId==CONTACT_LOADER_ID || loaderId==0) {
            // Construct the loader
            cursorLoader = new CursorLoader(getActivity(),
                                            ContactsContract.Contacts.CONTENT_URI, // URI
                                            projectionFields,  // projection fields
                                            INITIAL_SELECTION, // get only people with a phone no
                                            null, // the selection args
                                            orderBy // the sort order
            );
        } else {
            // Construct the loader
            cursorLoader = new CursorLoader(getActivity(),
                                            ContactsContract.Contacts.CONTENT_URI, // URI
                                            projectionFields,  // projection fields
                                            SELECTION, // the selection criteria
                                            mSelectionArgs, // the selection args
                                            orderBy // the sort order
            );
        }
        // Return the loader for use
        return cursorLoader;
    }

    // When the system finishes retrieving the Cursor through the CursorLoader,
    // a call to the onLoadFinished() method takes place.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor  = cursor;
        contactsTask = new ContactsTask();
        contactsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(contactsTask != null && !contactsTask.isCancelled() && contactsTask.getStatus() == AsyncTask.Status.RUNNING){
            contactsTask.cancel(true);
        }
    }

    // This method is triggered when the loader is being reset
    // and the loader data is no longer available. Called if the data
    // in the provider changes and the Cursor becomes stale.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear the Cursor we were using with another call to the swapCursor()
        //mCursorAdapter.swapCursor(null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_phone_book, container, false);
        search_bar = (EditText) getActivity().findViewById(R.id.search);
        statusText = (TextView) rootView.findViewById(R.id.status_text);
        statusPlaceHolder = rootView.findViewById(R.id.status_placeholder);
        statusText.setText(R.string.loading_contact);
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchString = s.toString();
                //A unique id has to be given every time to initLoader or else it will check if
                //a cursor with same id exists and load it back again instead of performing the search again
                if(mCursorAdapter != null)
                    mCursorAdapter.getFilter().filter(mSearchString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //nothing to do here
            }
        });
        getLoaderManager().initLoader(CONTACT_LOADER_ID, null, this);
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


    private class ContactsTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            mContacts = new ArrayList<>();
            //set the contents of cursor in arraylist for further reuse
            try {
                for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    // The Cursor is now set to the right position
                    PhoneBookContact contact = new PhoneBookContact();
                    contact.setContactName(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)));
                    contact.setContactImagePath(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
                    String contactId =
                            mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = null;
                    try{
                        // Query phone here. Covered next
                        phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
                        String selectedPhoneNumber = null;
                        int phoneOrder = 0;
                        while (phones.moveToNext()) {
                            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                            if(!TextUtils.isEmpty(phoneNumber)){
                                switch (type) {
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                        if(selectedPhoneNumber == null || phoneOrder > 2){
                                            selectedPhoneNumber = phoneNumber;
                                            phoneOrder = 2;
                                        }
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                        selectedPhoneNumber = phoneNumber;
                                        phoneOrder = 1;
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                        if(selectedPhoneNumber == null || phoneOrder > 3){
                                            phoneOrder = 3;
                                            selectedPhoneNumber = phoneNumber;
                                        }
                                        break;
                                    default:
                                        if(selectedPhoneNumber == null){
                                            selectedPhoneNumber = phoneNumber;
                                            phoneOrder = 4;
                                        }
                                        break;
                                }
                            }
                        }
                        contact.setNumber(selectedPhoneNumber);

                    } catch (Exception e){

                    }
                    finally {
                        try{
                            if(phones != null)
                                phones.close();
                        } catch (Exception e){

                        }
                    }
                    if(mListener.getContactIdentifierList().contains(contact.getIdentifier()))
                        contact.setSelected(true);
                    if(!TextUtils.isEmpty(contact.getIdentifier())){
                        mContacts.add(contact);

                        //Get Emails
                        Cursor emailCur = null;
                        try {
                            emailCur = getActivity().getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                    new String[]{contactId}, null);
                            String selectedEmail = null;
                            int emailOrder = 0;
                            while (emailCur.moveToNext()) {
                                // This would allow you get several email addresses
                                // if the email addresses were stored in an array
                                String email = emailCur.getString(
                                        emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                int emailType = emailCur.getInt(
                                        emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                                if(!TextUtils.isEmpty(email)) {
                                    switch (emailType) {
                                        case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                                            if (selectedEmail == null || emailOrder > 2) {
                                                selectedEmail = email;
                                                emailOrder = 2;
                                            }
                                            break;
                                        case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                                            selectedEmail = email;
                                            emailOrder = 1;
                                            break;
                                        case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                                            if (selectedEmail == null || emailOrder > 3) {
                                                emailOrder = 3;
                                                selectedEmail = email;
                                            }
                                            break;
                                        default:
                                            if (selectedEmail == null) {
                                                selectedEmail = email;
                                                emailOrder = 4;
                                            }
                                            break;
                                    }
                                }
                            }
                            if(!TextUtils.isEmpty(selectedEmail)){
                                contact.setEmail(selectedEmail);
                            }
                        } catch (Exception e){

                        } finally {
                            try {
                                if(emailCur != null)
                                    emailCur.close();
                            } catch (Exception e){

                            }
                        }
                    }
                }
            }
            catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            if(isCancelled())
                return;
            try{
                if(mContacts != null && mContacts.size() > 0){
                    mCursorAdapter = new PhoneBookAdapter(getActivity(), mContacts,PhoneBookFragment.this);
                    // Find list and bind to adapter
                    mContactsListView = (ListView) getActivity().findViewById(R.id.phonebook);
                    mContactsListView.setAdapter(mCursorAdapter);
                    mContactsListView.setVisibility(View.VISIBLE);
                    statusPlaceHolder.setVisibility(View.GONE);
                    statusText.setVisibility(View.GONE);
                }
                else{
                    statusText.setText(R.string.contacts_not_found);
                }
            } catch (Exception e){
                DebugUtils.logException(e);
            }
        }
    }

}
