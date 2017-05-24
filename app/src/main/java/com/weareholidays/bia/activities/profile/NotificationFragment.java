package com.weareholidays.bia.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.weareholidays.bia.R;
import com.weareholidays.bia.adapters.NotificationAdapter;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.listeners.SwipeableRecyclerViewTouchListener;
import com.weareholidays.bia.parse.models.Notification;
import com.weareholidays.bia.parse.utils.ShareUtils;
import com.weareholidays.bia.parse.utils.TripUtils;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_NOTIFICATION;


public class NotificationFragment extends Fragment implements View.OnClickListener {

    private List<Notification> notifications;
    public Context mcontext;
    private TextView noNotification;
    private ImageView noNotificationImage;
    private LinearLayout noInternet;
    private RelativeLayout actualLayout;
    boolean isRefresh = false;
    //    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mNotificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private ProgressBar mProgressBar;

    private ShowcaseView showcaseView;

    public NotificationFragment() {
        mcontext = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notification, container, false);
        noInternet = (LinearLayout) v.findViewById(R.id.no_internet);
        actualLayout = (RelativeLayout) v.findViewById(R.id.with_internet);
        noNotification = (TextView) v.findViewById(R.id.no_notification);
        noNotificationImage = (ImageView) v.findViewById(R.id.no_notification_image);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        mNotificationsRecyclerView = (RecyclerView) v.findViewById(R.id.notification_recyclerview);

        mNotificationsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mNotificationsRecyclerView.setLayoutManager(linearLayoutManager);

        notificationAdapter = new NotificationAdapter(getActivity());
        mNotificationsRecyclerView.setAdapter(notificationAdapter);

        notificationAdapter.setOnItemClickListener(new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Notification notification = notificationAdapter.getNotificationList().get(position);

//                String action_param = notification.getActionParams();
//                String action_type = notification.getActionType();

//                Toast.makeText(getActivity(), "action_type = " + action_type + " Action = " + action_param, Toast.LENGTH_SHORT).show();

                Intent intent = ShareUtils.getNotificationRedirectIntent(notification, getActivity());
                if (intent != null)
                    getActivity().startActivity(intent);
            }
        });

        notifications = new ArrayList<>();

        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(mNotificationsRecyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    DebugUtils.LogD("onDismissedBySwipeLeft Position = " + position);

                                    Notification notification = notificationAdapter.getNotificationList().get(position);
                                    notification.setIsDeleted(true);
                                    try {
                                        notification.save();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    notificationAdapter.getNotificationList().remove(position);
                                    notificationAdapter.notifyDataSetChanged();
                                }

                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    DebugUtils.LogD("onDismissedBySwipeRight Position = " + position);

                                    Notification notification = notificationAdapter.getNotificationList().get(position);
                                    notification.setIsDeleted(true);
                                    try {
                                        notification.save();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    notificationAdapter.getNotificationList().remove(position);
                                    notificationAdapter.notifyDataSetChanged();
                                }
//                                notificationAdapter.notifyDataSetChanged();
                            }
                        });

        mNotificationsRecyclerView.addOnItemTouchListener(swipeTouchListener);


        if (ViewUtils.isNetworkAvailable(getActivity())) {
            initiateTask();
        } else {
            actualLayout.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
            //set retry button functionality
            LinearLayout retryButton = (LinearLayout) v.findViewById(R.id.retry_button);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ViewUtils.isNetworkAvailable(getActivity())) {
                        actualLayout.setVisibility(View.VISIBLE);
                        noInternet.setVisibility(View.GONE);
                        initiateTask();
                    }
                }
            });
        }

//        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);

        // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
//        mSwipeRefreshLayout.setColorSchemeResources(
//                R.color.swipe_color_1, R.color.swipe_color_2,
//                R.color.swipe_color_3, R.color.swipe_color_4);
//
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                isRefresh = true;
//                initiateTask();
//            }
//        });
        return v;
    }

    private void initiateTask() {
        new NotificationsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();
    }

    private class NotificationsTask extends AsyncTask<Void, Void, Void> {

        List<Notification> notificationsList;
        boolean noError = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isRefresh)
                mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                notificationsList = TripUtils.getNotification().find();
                noError = true;
            } catch (ParseException e) {
                Log.e("Notification", "Error searching notifications", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isCancelled()) {
//                if (isRefresh && mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
//                    isRefresh = false;
//                    mSwipeRefreshLayout.setRefreshing(false);
//                }
                return;
            }

            try {

//                if (isRefresh && mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
//                    isRefresh = false;
//                    mSwipeRefreshLayout.setRefreshing(false);
//                }

                if (noError) {
                    if (notificationsList.size() > 0) {
                        notifications.clear();
                        notifications.addAll(notificationsList);
                        notificationAdapter.setNotificationList(getActivity(), notifications);
                        notificationAdapter.notifyDataSetChanged();
                        drawCoachMarks();
                    } else {
                        noNotification.setVisibility(View.VISIBLE);
                        noNotificationImage.setVisibility(View.VISIBLE);
                    }
                }

                mProgressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }
    }

    public interface OnFragmentInteractionListener {
    }

    private void drawCoachMarks() {

        if (!SharedPrefUtils.getBooleanPreference(getActivity(), COACH_NOTIFICATION)) {

            View view1 = mNotificationsRecyclerView.getRootView();

            Target markLocationByView[] = new Target[1];
            markLocationByView[0] = new ViewTarget(view1);

            Point markLocationByOffset[] = new Point[1];
            markLocationByOffset[0] = new Point(0, 0);

            Point markTextPoint[] = new Point[1];
            markTextPoint[0] = new Point(-20, -130);

            float circleRadius[] = new float[1];
            circleRadius[0] = 70f;

            String coachMarkTextArray[];

            coachMarkTextArray = getResources().getStringArray(R.array.coachmark_notification);

            showcaseView = new ShowcaseView.Builder(getActivity(), true, true)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                    .setOnClickListener(NotificationFragment.this)
                    .build();

            showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

            SharedPrefUtils.setBooleanPreference(getActivity(), COACH_NOTIFICATION, true);

        }
    }

}
