package com.weareholidays.bia.activities.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.HomeActivity;
import com.weareholidays.bia.activities.UserNotificationActivity;
import com.weareholidays.bia.activities.account.InviteFriends;
import com.weareholidays.bia.activities.login.SignUpActivity;
import com.weareholidays.bia.asyncTasks.ShortenURLTask;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.models.Coupon;
import com.weareholidays.bia.parse.models.ParseCustomUser;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.UserCoupon;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.Calendar;
import java.util.List;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_MORE_TAB_PREF;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyAccount.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyAccount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyAccount extends Fragment implements View.OnClickListener {
    CenterProgressDialog progressDialog;

    private OnFragmentInteractionListener mListener;
    private long cLastClickTime = 0;
    private long sLastClickTime = 0;
    private ShortenURLTask mShortenURLTask;
    private ShowcaseView showcaseView;
    private LinearLayout coupon;
    private LinearLayout inviteFriends;
    private LinearLayout notification;
    private LinearLayout playStoreLine;

    public boolean isShowCoachMark() {
        return showCoachMark;
    }

    public void setShowCoachMark(boolean showCoachMark) {
        this.showCoachMark = showCoachMark;
    }

    private boolean showCoachMark;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MyAccount.
     */
    public static MyAccount newInstance(String param1, String param2) {
        MyAccount fragment = new MyAccount();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MyAccount() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
//        drawCoachMarks(getActivity());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_account, container, false);

        coupon = (LinearLayout) rootView.findViewById(R.id.coupon_code);
        coupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1 second
                if (SystemClock.elapsedRealtime() - cLastClickTime < 1000) {
                    return;
                }
                cLastClickTime = SystemClock.elapsedRealtime();
                if (!isConnectingToInternet()) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.no_net)
                            .content(R.string.no_net_msg)
                            .positiveText(R.string.ok)
                            .positiveColor(getResources().getColor(R.color.orange_primary))
                            .show();
                    return;
                }
                showDialog();
            }
        });

        final LinearLayout feedback = (LinearLayout) rootView.findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1 second
                if (SystemClock.elapsedRealtime() - cLastClickTime < 1000) {
                    return;
                }
                cLastClickTime = SystemClock.elapsedRealtime();
                Intent intent = new Intent(getActivity(), FeedbackActivity.class);
                startActivity(intent);
            }
        });

//        LinearLayout beenThere = (LinearLayout)rootView.findViewById(R.id.been_there);
//        beenThere.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isConnectingToInternet()){
//                    Intent intent = new Intent(getActivity(), BeenThereActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);}
//                else {
//                    new MaterialDialog.Builder(getActivity())
//                            .title(R.string.no_net)
//                            .content(R.string.no_net_msg)
//                            .positiveText(R.string.ok)
//                            .positiveColor(getResources().getColor(R.color.orange_primary))
//                            .show();
//                }
//            }
//        });

//        Button logout = (Button) rootView.findViewById(R.id.logout);
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //check if trip is finished or not
//                if (!TripUtils.getInstance().getCurrentTripOperations().isTripAvailable()) {
//                    progressDialog = CenterProgressDialog.show(getActivity(), "Logging Out", null, true);
//                    ParseCustomUser.logOut();
//                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
//                    installation.put("username", "");
//                    installation.saveInBackground();
//                    progressDialog.dismiss();
//                    Toast.makeText(getActivity(), "You have been logged out", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getActivity(), SignUpActivity.class);
//                    startActivity(intent);
//                } else {
//                    //do nothing here
//                    //v.setEnabled(false);
//                    Toast.makeText(getActivity(), "You have not finished your trip!", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
        notification = (LinearLayout) rootView.findViewById(R.id.notification);
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nIntent = new Intent(getActivity(), UserNotificationActivity.class);
                startActivity(nIntent);
            }
        });

        playStoreLine = (LinearLayout) rootView.findViewById(R.id.rate_play_store);
        playStoreLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareUtils.openPlayStore(getActivity());
            }
        });

        /*final LinearLayout shareApp = (LinearLayout)rootView.findViewById(R.id.share_app);

        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Preventing multiple clicks, using threshold of 1 second
                if (SystemClock.elapsedRealtime() - sLastClickTime < 1000) {
                    return;
                }
                sLastClickTime = SystemClock.elapsedRealtime();

                String sendUrl = ShareUtils.getPlayStoreUrl(getActivity());
                mShortenURLTask = new ShortenURLTask(getActivity(), null, null, false, true);
                mShortenURLTask.execute(sendUrl);
            }
        });*/

        inviteFriends = (LinearLayout) rootView.findViewById(R.id.invite_friends);
        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InviteFriends.class);
                startActivity(intent);
            }
        });

        PackageInfo pInfo = null;
        String version = "";
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ((TextView) rootView.findViewById(R.id.version_name)).
                setText("WAH Holidays Pvt. Ltd. | Bia v" + version);

//        if (isShowCoachMark()) {
//            drawCoachMarks(getActivity());
//        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public void showDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title("Enter Coupon Code")
                .customView(R.layout.fragment_coupon, true)
                .positiveText(R.string.apply)
                .negativeText(R.string.cancel)
                .positiveColor(getResources().getColor(R.color.orange_primary))
                .negativeColor(getResources().getColor(R.color.orange_primary))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        hideKeyboard(dialog);
                        Calendar c = Calendar.getInstance();
                        String codeEntered = ((EditText) dialog.findViewById(R.id.coupon)).getText().toString().toUpperCase();
                        progressDialog = CenterProgressDialog.show(getActivity(), "Logging Out", null, true);
                        try {
                            ParseQuery.getQuery(Coupon.class).whereEqualTo(Coupon.CODE, codeEntered).whereLessThanOrEqualTo(Coupon.VALID_FROM, c.getTime())
                                    .whereGreaterThanOrEqualTo(Coupon.VALID_TILL, c.getTime()).whereEqualTo(Coupon.ISACTIVE, true)
                                    .findInBackground(new FindCallback<Coupon>() {
                                        @Override
                                        public void done(List<Coupon> coupons, ParseException e) {
                                            if (coupons.size() != 0) {
                                                progressDialog.dismiss();

                                                if (TripUtils.getInstance().getCurrentTripOperations().isTripAvailable()) {
                                                    Trip trip = TripUtils.getInstance().getCurrentTripOperations().getTrip();
                                                    //if no coupon has been applied , then show success msg
                                                    if (trip.getCoupon() == null) {
                                                        TripUtils.getInstance().getCurrentTripOperations().getTrip().setCoupon(coupons.get(0));
                                                        new MaterialDialog.Builder(getActivity())
                                                                .title(R.string.applied)
                                                                .content(coupons.get(0).getMessage())
                                                                .positiveText(R.string.ok)
                                                                .positiveColor(getResources().getColor(R.color.orange_primary))
                                                                .show();
                                                    }
                                                    //if you have already used one coupon
                                                    else {
                                                        new MaterialDialog.Builder(getActivity())
                                                                .title(R.string.applied)
                                                                .content(R.string.coupon_already_applied)
                                                                .positiveText(R.string.ok)
                                                                .positiveColor(getResources().getColor(R.color.orange_primary))
                                                                .show();
                                                    }

                                                } else {
                                                    //if there is no ongoing trip, dont apply coupon
                                                    new MaterialDialog.Builder(getActivity())
                                                            .title(R.string.applied)
                                                            .content(R.string.no_ongoing_trip)
                                                            .positiveText(R.string.ok)
                                                            .positiveColor(getResources().getColor(R.color.orange_primary))
                                                            .show();
                                                }
                                            } else {
                                                //no matching coupon code found
                                                progressDialog.dismiss();
                                                new MaterialDialog.Builder(getActivity())
                                                        .title(R.string.invalid_coupon)
                                                        .content(R.string.enter_valid)
                                                        .positiveText(R.string.ok)
                                                        .positiveColor(getResources().getColor(R.color.orange_primary))
                                                        .show();
                                            }
                                        }
                                    });
                        } catch (Exception e) {
                            DebugUtils.logException(e);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        hideKeyboard(dialog);
                    }
                })
                .show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void hideKeyboard(MaterialDialog dialog) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((EditText) dialog.findViewById(R.id.coupon)).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public boolean isConnectingToInternet() {
        Context context = getActivity().getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();
    }

    public interface OnFragmentInteractionListener {

    }


    public void drawCoachMarks(Context mContext) {

        if (!SharedPrefUtils.getBooleanPreference(getActivity(), COACH_MORE_TAB_PREF)) {

            View view1 = null;
            View view2 = null;

            if (coupon != null) {
                view1 = notification.getChildAt(0);
            }

            if (inviteFriends != null) {
                view2 = inviteFriends.getChildAt(0);
            }


            Target markLocationByView[] = new Target[2];
            markLocationByView[0] = new ViewTarget(view1);
            markLocationByView[1] = new ViewTarget(view2);

            Point markLocationByOffset[] = new Point[2];
            markLocationByOffset[0] = new Point(0, 0);
            markLocationByOffset[1] = new Point(0, 0);

            Point markTextPoint[] = new Point[2];
            markTextPoint[0] = new Point(60, 0);
            markTextPoint[1] = new Point(60, 0);

            float circleRadius[] = new float[2];
            circleRadius[0] = 30f;
            circleRadius[1] = 30f;

            String coachMarkTextArray[];

            coachMarkTextArray = getResources().getStringArray(R.array.coachmark_moretab);

            showcaseView = new ShowcaseView.Builder(getActivity(), true, true)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                    .setOnClickListener(MyAccount.this)
                    .build();

            showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

            SharedPrefUtils.setBooleanPreference(getActivity(), COACH_MORE_TAB_PREF, true);
        }

    }
}
