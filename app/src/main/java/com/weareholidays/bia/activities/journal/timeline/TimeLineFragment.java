package com.weareholidays.bia.activities.journal.timeline;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.felipecsl.asymmetricgridview.library.Utils;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.MyMapFragment;
import com.weareholidays.bia.activities.journal.actions.NoteActivity;
import com.weareholidays.bia.activities.journal.base.TripOperationsLoader;
import com.weareholidays.bia.activities.journal.views.DayDurationActivity;
import com.weareholidays.bia.activities.journal.views.FbActivityFragment;
import com.weareholidays.bia.activities.journal.views.NotesActivityFragment;
import com.weareholidays.bia.adapters.TimelineRecyclerAdapter;
import com.weareholidays.bia.background.services.ServiceUtils;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.listeners.EndlessRecyclerOnScrollListener;
import com.weareholidays.bia.models.GalleryImage;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.DaySummary;
import com.weareholidays.bia.parse.models.FileLocal;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.models.TripSettings;
import com.weareholidays.bia.parse.models.local.DayLocationPin;
import com.weareholidays.bia.parse.models.local.DaySummaryDummy;
import com.weareholidays.bia.parse.utils.ParseFileUtils;
import com.weareholidays.bia.parse.utils.TripLocalOperations;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MediaUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.utils.reorderUtils.DayTimeLine;
import com.weareholidays.bia.widgets.CenterProgressDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimeLineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimeLineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeLineFragment extends Fragment implements TimelineRecyclerAdapter.OnItemInteraction {

    public static final String TIMELINE_TYPE = "TIMELINE_TYPE";
    public static final String DAY_ORDER = "DAY_ORDER";
    public static final String TIMELINE_SCROLL_POSITION = "TIMELINE_SCROLL_POSITION";

    public static final String DAY_TIMELINE = "DAY_TIMELINE";
    public static final String FB_TIMELINE = "FB_TIMELINE";
    public static final String TWITTER_TIMELINE = "TWITTER_TIMELINE";
    public static final String INSTAGRAM_TIMELINE = "INSTAGRAM_TIMELINE";
    public static final String CHECK_IN_TIMELINE = "CHECK_IN_TIMELINE";
    public static final String PHOTO_TIMELINE = "PHOTO_TIMELINE";
    public static final String NOTE_TIMELINE = "NOTE_TIMELINE";

    public static int UPDATED_NOTE_CONTENT = 12312;

    private int dayOrder;
    private List<Timeline> timeLines;
    private int visibleThreshold = 15;
    private int skip = 0;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private TimelineRecyclerAdapter adapter;
    private LinearLayoutManager mLayoutManager;
    private String socialType;
    private String contentType;
    private boolean fromReOrder;
    private boolean fromDelete;
    private GetTimeLines timelineAsync;
    public LinearLayout line;
    private boolean isEnded = false;
    private int scrollPosition = -1;
    private boolean pinExists = false;

    private List<Timeline> retrievedTimeLines;

    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    public static String TYPE = "TYPE";

    private TripOperations tripOperations;

    private OnFragmentInteractionListener mListener;

    private DayTimeLine dayTimeLine;
    private static String TAG = "TimeLineFragment";

    private ShowcaseView showcaseView;
    private boolean loading = false;
    private boolean loading_done = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param timeLineType Type of Type line you want to show
     * @param dayOrder     Order of the day in trip.
     * @return A new instance of fragment TimeLineFragment.
     */
    public static TimeLineFragment newInstance(String timeLineType, int dayOrder) {
        TimeLineFragment fragment = new TimeLineFragment();
        Bundle args = new Bundle();
        args.putString(TIMELINE_TYPE, timeLineType);
        args.putInt(DAY_ORDER, dayOrder);
        fragment.setArguments(args);
        return fragment;
    }

    public TimeLineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String trip_key = TripOperations.CURRENT_TRIP_ID;
        if (getArguments() != null) {
            String timeLineType = getArguments().getString(TIMELINE_TYPE);
            if (DAY_TIMELINE.equals(timeLineType)) {
                dayOrder = getArguments().getInt(DAY_ORDER);
            }
            fromDelete = getArguments().getBoolean(DeleteActivity.FROM_DELETE);
            fromReOrder = getArguments().getBoolean(ReorderActivity.FROM_REORDER);
            socialType = getArguments().getString(FbActivityFragment.SOCIAL_TYPE);
            contentType = getArguments().getString(NotesActivityFragment.CONTENT_TYPE);
            if (mListener.loadFromActivity()) {
                dayTimeLine = mListener.getDayTimeLines(dayOrder);
                skip = dayTimeLine.getSkip();
                visibleThreshold = dayTimeLine.getLimit();
                timeLines = dayTimeLine.getTimelines();
            }
            scrollPosition = getArguments().getInt(TIMELINE_SCROLL_POSITION, -1);
            if (scrollPosition >= 0)
                visibleThreshold = scrollPosition + 1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_time_line, container, false);
        setUp(rootView);
        return rootView;
    }

    private void setUp(View rootView) {
        if (timeLines == null)
            timeLines = new ArrayList<>();

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);
        line = (LinearLayout) rootView.findViewById(R.id.line);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
//        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
//                (NinePatchDrawable) ContextCompat.getDrawable(this.getActivity(), R.drawable.material_shadow_z3_9));

        if ( tripOperations != null)
            adapter = new TimelineRecyclerAdapter(this.getActivity(), this, timeLines, fromReOrder, fromDelete, tripOperations.canWrite());

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(adapter);
        mRecyclerView.setAdapter(mWrappedAdapter);
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager, visibleThreshold) {
            @Override
            public void onLoadMore(int current_page) {
                if (!loading && !loading_done) {
                    if (dayTimeLine != null)
                        dayTimeLine.setSkip(skip);
                    skip = skip + visibleThreshold;
                    timelineAsync = new GetTimeLines();
                    timelineAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        if (fromReOrder)
            mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
        if (!loading && !loading_done) {
            timelineAsync = new GetTimeLines();
            timelineAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLayoutManager != null && mListener != null) {
                    mListener.onTimelineScrollChanged(mLayoutManager.findFirstVisibleItemPosition(), dayOrder);
                }
            }
        });

    }

    @Override
    public void onEditTimeLine(Timeline timeline) {
        if (timeline == null || fromReOrder || fromDelete)
            return;
        tripOperations.setTimeLine(timeline);
        startActivity(NavigationUtils.getTimelineEditIntent(getActivity(), tripOperations.getTripKey(), getActivity().getIntent()));
        NavigationUtils.openAnimation(getActivity());
    }

    @Override
    public void onDeleteTimeline(Timeline timeline) {
        if (timeline == null || fromReOrder || fromDelete)
            return;
        new DeleteTimeLineTask(timeline).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDeleteTimelineBulk(Timeline timeline) {
        if (dayTimeLine != null) {
            dayTimeLine.setDeleted(true);
            dayTimeLine.getDeletedTimeLines().add(timeline);
            int deletedIndex = dayTimeLine.getTimelines().indexOf(timeline);
            dayTimeLine.getTimelines().remove(timeline);
            adapter.notifyItemRemoved(deletedIndex);
        }
    }

    @Override
    public void onEditTimeline(Timeline timeline, int position) {
        if (timeline == null || fromReOrder || fromDelete)
            return;
        Album album = (Album) timeline.getContent();
        Media mMedia = album.getMedia().get(position);
        tripOperations.setSelectedMedia(mMedia);
        tripOperations.setTimeLine(timeline);
        startActivity(NavigationUtils.getPhotoViewIntent(getActivity(), tripOperations.getTripKey(), getActivity().getIntent()));
        NavigationUtils.openAnimation(getActivity());
    }

    @Override
    public void onEditTimeLineBulk(final Timeline timeline) {
        if (timeline == null || fromReOrder)
            return;

        if (mListener.hasChanges()) {
            new MaterialDialog.Builder(getActivity())
                    .widgetColor(getResources().getColor(R.color.orange_primary))
                    .positiveText("EDIT POST")
                    .negativeText("KEEP CHANGES")
                    .positiveColor(getResources().getColor(R.color.orange_primary))
                    .negativeColor(getResources().getColor(R.color.orange_primary))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            dialog.dismiss();
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            dialog.dismiss();
                            launchEditFromEditView(timeline);
                        }
                    }).title("You have unsaved deletes! Your changes will be discarded").show();
        } else {
            launchEditFromEditView(timeline);
        }
    }

    private void launchEditFromEditView(Timeline timeline) {
        if (Timeline.NOTE_CONTENT.equals(timeline.getContentType())) {
            tripOperations.setSelectedNote(timeline);
            Intent intent = new Intent(getActivity(), NoteActivity.class);
            intent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
            intent.putExtra(NoteActivity.EDIT_NOTE_VIEW, true);
            startActivityForResult(intent, UPDATED_NOTE_CONTENT);
        }

        if (Timeline.ALBUM_CONTENT.equals(timeline.getContentType())) {
            tripOperations.setTimeLine(timeline);
            startActivity(NavigationUtils.getTimelineEditIntent(getActivity(), tripOperations.getTripKey(), getActivity().getIntent()));
            NavigationUtils.openAnimation(getActivity());
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATED_NOTE_CONTENT && resultCode == Activity.RESULT_OK) {
            getActivity().setResult(Activity.RESULT_OK);
            try {
                if (dayTimeLine != null) {
                    dayTimeLine.getTimelines().clear();
                    dayTimeLine.getDeletedTimeLines().clear();
                    dayTimeLine.setSkip(0);
                    skip = 0;
//                    visibleThreshold = 5;
                    loading = false;
                    loading_done = false;
                    dayTimeLine.setDeleted(false);
                    adapter.notifyDataSetChanged();
                    isEnded = false;
                    timelineAsync = new GetTimeLines();
                    timelineAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } catch (Exception e) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onReordered() {
        if (dayTimeLine != null)
            dayTimeLine.setReordered(true);
    }

    @Override
    public void showDaySummary() {
        Intent myIntent = new Intent(getActivity(), DayDurationActivity.class);
        myIntent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
        myIntent.putExtra("dayOrder", dayOrder);
        startActivity(myIntent);
    }

    @Override
    public void showDaySummary(int itemSelected) {
        Intent myIntent = new Intent(getActivity(), DayDurationActivity.class);
        myIntent.putExtra(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
        myIntent.putExtra("dayOrder", dayOrder);
        myIntent.putExtra("itemSelected", itemSelected);
        startActivity(myIntent);
    }

    @Override
    public MyMapFragment getMyMapFragment() {
        MyMapFragment fragment = new MyMapFragment();
        Bundle args = new Bundle();
        args.putString(TripOperations.TRIP_KEY_ARG, tripOperations.getTripKey());
        args.putString(TYPE, "TIMELINE");
        args.putInt(TimeLineFragment.DAY_ORDER, dayOrder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public FragmentManager getSummaryFragmentManager() {
        return getChildFragmentManager();
    }

    public class GetTimeLines extends AsyncTask<Void, Void, Void> {


        private boolean stopTrackService;
        private TripSettings tripSettings;

        @Override
        protected void onPreExecute() {
            loading = true;
            try {
                if (!isEnded) {
                    adapter.setLoading(true);
//                    adapter.notifyItemChanged(adapter.getItemCount() - 1);
//                    mRecyclerView.post(new Runnable() {
//                        public void run() {
//                            // There is no need to use notifyDataSetChanged()
//                            adapter.notifyItemInserted(adapter.getItemCount() - 1);
//                        }
//                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if( tripOperations == null )
                return null;
            try {
                Trip trip = tripOperations.getTrip();
                if (tripOperations.canWrite() && trip != null && !trip.isFinished()) {
                    tripSettings = trip.getSettings();
                    if (tripSettings != null && !trip.getDay(dayOrder).hasEnded() && !fromDelete && !fromReOrder && socialType == null && ServiceUtils.checkAndSetCameraSyncService()) {

                        Log.i(TAG, "Camera roll sync: " + tripSettings.isCameraRoll());
                        if (tripSettings.isCameraRoll()) {
                            Boolean sdPresent = android.os.Environment.getExternalStorageState()
                                    .equals(android.os.Environment.MEDIA_MOUNTED);
                            if (sdPresent) {
                                Log.i(TAG, "External storage in mounted state");
                                try {
                                    Date cameraRollStartTime = tripSettings.getCameraRollSyncTime();
                                    if (cameraRollStartTime == null)
                                        cameraRollStartTime = trip.getStartTime();
                                    Calendar cameraRollSyncTime = Calendar.getInstance();
                                    Cursor[] cursors = MediaUtils.getImagesCursor(getActivity().getContentResolver(), cameraRollStartTime);
                                    MergeCursor cursor = new MergeCursor(cursors);
                                    int size = cursor.getCount();
                                    if (size > 0) {
                                        Set<String> fileLocalSet = new HashSet<>();
                                        for (FileLocal fileLocal : ParseFileUtils.storedGalleryImages()) {
                                            fileLocalSet.add(fileLocal.getLocalUri());
                                        }
                                        MediaUtils mediaUtils = MediaUtils.newInstance(cursor);
                                        List<GalleryImage> galleryImages = new ArrayList<>();
                                        for (int i = 0; i < size; i++) {
                                            cursor.moveToPosition(i);
                                            GalleryImage galleryImage = mediaUtils.getGalleryImage(cursor);
                                            if (mediaUtils.isCameraImage(galleryImage) && !fileLocalSet.contains(galleryImage.getUri())) {
                                                galleryImages.add(galleryImage);
                                            }
                                        }
                                        cursor.close();

                                        if (galleryImages.size() > 0)
                                            ((TripLocalOperations)tripOperations).addPhotos(galleryImages);

                                        tripSettings.setCameraRollSyncTime(cameraRollSyncTime.getTime());
                                        tripOperations.save(tripSettings);
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Error syncing camera roll pictures", e);
                                    DebugUtils.logException(e);
                                }
                            }
                        }
                    }
                }


                Log.d("skip",String.valueOf(skip));
                Log.d("visible thresold", String.valueOf(visibleThreshold));
                retrievedTimeLines = tripOperations.getDayTimeLines(dayOrder, visibleThreshold, skip, socialType, contentType);

                Timeline daySummaryTimeline = null;
                Timeline dayPinTimeline = null;

                //Handle Day Pins
                if (skip == 0 && !fromReOrder && !fromDelete && socialType == null) {
                    Day day = tripOperations.getTrip().getDay(dayOrder);

                    if (day.getLocation() != null) {
                        DayLocationPin dayLocationPin = new DayLocationPin();
                        dayLocationPin.setCityName(day.getCity());
                        dayLocationPin.setCountryName(day.getCountry());
                        dayLocationPin.setParseGeoPoint(day.getLocation());
                        if (day.hasEnded()) {
                            dayLocationPin.setCurrentLocation(false);
                            dayLocationPin.setEnded(true);
                        } else {
                            if (retrievedTimeLines.size() > 0)
                                dayLocationPin.setEnded(true);
                            //TODO: check for intercity travel
                            dayLocationPin.setCurrentLocation(true);
                        }
                        dayPinTimeline = new Timeline();
                        dayPinTimeline.setContent(dayLocationPin);
                        dayPinTimeline.setContentTime(day.getStartTime());
                    }
                }

                //Handle Day summary
                Day day = tripOperations.getTrip().getDay(dayOrder);
                if (day.hasEnded() && !fromDelete && !fromReOrder && socialType == null && retrievedTimeLines.size() <visibleThreshold && !isEnded) {
                    boolean hasTimelines = false;
                    if (skip != 0)
                        hasTimelines = true;
                    else
                        hasTimelines = false;

                    if (hasTimelines || day.hasEnded()) {
                        DaySummary daySummary = tripOperations.getTrip().getDay(dayOrder).getDaySummary();
                        DaySummaryDummy myDummy = new DaySummaryDummy(daySummary.getDistance(), daySummary.getPhotos(), daySummary.getCheckIns());
                        myDummy.setDayOrder(dayOrder);
                        daySummaryTimeline = new Timeline();
                        daySummaryTimeline.setContent(myDummy);
                        if (day.hasEnded())
                            daySummaryTimeline.setContentTime(day.getEndTime());
                        else
                            daySummaryTimeline.setContentTime(Calendar.getInstance().getTime());
                        isEnded = true;
                    }
                }


                if (dayPinTimeline != null) {
                    pinExists = true;
                    retrievedTimeLines.add(0, dayPinTimeline);
                    if (scrollPosition >= 0)
                        scrollPosition++;
                }

                if (daySummaryTimeline != null)
                    retrievedTimeLines.add(daySummaryTimeline);

                List<Timeline> privateAlbums = new ArrayList<>();

                //Retrieve Images for Albums and add Ids for reorder
                for (Timeline timeLine : retrievedTimeLines) {
                    try {
                        timeLine.setDateInMilli(timeLine.getContentTime().getTime());
                    } catch (Exception e) {
                        Log.e("ERROR", e.toString());
                    }
                    if (Timeline.ALBUM_CONTENT.equals(timeLine.getContentType())) {
                        Album album = (Album) timeLine.getContent();
                        List<Media> mediaList = tripOperations.getAlbumMedia(album);
                        if (mediaList == null || mediaList.size() == 0) {
                            privateAlbums.add(timeLine);
                        } else {
                            tripOperations.populateMediaSource(mediaList);
                            album.setMedia(mediaList);
                        }
                    }
                }

                if (privateAlbums.size() > 0) {
                    for (Timeline timeline : privateAlbums) {
                        retrievedTimeLines.remove(timeline);
                    }
                }
            } catch (Exception e) {
                Log.e("TimeLine", "error retrieving timelines", e);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            loading = false;
            if (isCancelled())
                return;
            try {
                if (adapter != null) {
                    adapter.setLoading(false);
                    int size = adapter.getItemCount() - 1;
                    if(retrievedTimeLines.size() < visibleThreshold){
                            loading_done = true;
                    }
                    if (retrievedTimeLines != null && retrievedTimeLines.size() > 0) {
                        //int size = adapter.getItemCount() - 1;
                        timeLines.addAll(retrievedTimeLines);
                        adapter.notifyItemInserted(size);
                        Log.d("Size",String.valueOf(size));
                        Log.d("retrievedTimeLines", String.valueOf(retrievedTimeLines));
                        Log.d("timelines", String.valueOf(timeLines));

                        adapter.notifyItemRangeChanged(size, retrievedTimeLines.size());
                        adapter.notifyDataSetChanged();
                        if (skip == 0 && scrollPosition >= 0 && timeLines.size() > scrollPosition) {
                            mLayoutManager.scrollToPositionWithOffset(scrollPosition, Utils.dpToPx(getActivity(), 5));
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mLayoutManager.scrollToPositionWithOffset(scrollPosition, Utils.dpToPx(getActivity(), 5));
                                    } catch (Exception e) {
                                            e.printStackTrace();
                                    }
                                }
                            }, 1000);
                        }
                    }
                    if ((pinExists && timeLines.size() == 0) || (!pinExists && timeLines.size() == 0)) {
                        line.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    } else {
                        line.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {

            }
            //Race condition to check
            ServiceUtils.setCameraSyncService(false);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            tripOperations = mListener.getTripOperations();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        tripOperations = null;
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
    public interface OnFragmentInteractionListener extends TripOperationsLoader {
        boolean loadFromActivity();

        DayTimeLine getDayTimeLines(int dayOrder);

        boolean hasChanges();

        void onTimelineScrollChanged(int scrollIndex, int dayOrder);
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (timelineAsync != null && !timelineAsync.isCancelled() && timelineAsync.getStatus() == AsyncTask.Status.RUNNING) {
            timelineAsync.cancel(true);
        }

        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        adapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }

    private class DeleteTimeLineTask extends AsyncTask<Void, Void, Void> {

        private Timeline timeline;

        private CenterProgressDialog progressDialog;

        private boolean deleteFailed;

        public DeleteTimeLineTask(Timeline timeline) {
            this.timeline = timeline;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressDialog = CenterProgressDialog.show(getActivity(), null, null, true);
            } catch (Exception e) {

            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (!deleteFailed) {
                    timeLines.remove(timeline);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "An error occured while deleting the selected post", Toast.LENGTH_LONG);
                }

                if (progressDialog != null)
                    progressDialog.dismiss();
            } catch (Exception e) {

            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                tripOperations.deleteTimeLine(timeline);
            } catch (Exception e) {
                DebugUtils.logException(e);
                deleteFailed = true;
            }
            return null;
        }
    }
}
