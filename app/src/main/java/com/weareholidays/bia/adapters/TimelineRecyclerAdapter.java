package com.weareholidays.bia.adapters;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.felipecsl.asymmetricgridview.library.Utils;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridView;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridViewAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.parse.ParseGeoPoint;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.MyMapFragment;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.CheckIn;
import com.weareholidays.bia.parse.models.Media;
import com.weareholidays.bia.parse.models.Note;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.local.DayLocationPin;
import com.weareholidays.bia.parse.models.local.DaySummaryDummy;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.MapUtils;
import com.weareholidays.bia.utils.reorderUtils.DrawableUtils;
import com.weareholidays.bia.utils.reorderUtils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import wahCustomViews.view.WahImageView;

/**
 * Created by challa on 2/6/15.
 */
public class TimelineRecyclerAdapter extends RecyclerView.Adapter<TimelineRecyclerAdapter.TimeLineViewHolder>
        implements DraggableItemAdapter<TimelineRecyclerAdapter.TimeLineViewHolder> {

    private static final int NOTE_VIEW_TYPE = 1;
    private static final int CHECK_IN_VIEW_TYPE = 2;
    private static final int ALBUM_GRID_VIEW_TYPE = 3;
    private static final int ALBUM_SINGLE_PHOTO_VIEW_TYPE = 4;
    private static final int LOCATION_PIN_VIEW_TYPE = 5;
    private static final int DAY_SUMMARY_VIEW_TYPE = 6;
    private static final int LOADER_VIEW_TYPE = 7;
    private static String LOCATION_TIME_SEPERATOR = "   • ";

    private static final int DISTANCE_TAB = 0;
    private static final int CHECKIN_TAB = 1;
    private static final int PHOTO_TAB = 2;

    private List<Timeline> timelines;
    private Context mContext;
    public boolean reorder;
    public boolean delete;
    public boolean isPressed;
    private boolean canWrite = false;
    private DisplayMetrics displayMetrics;
    private String tripKey;
    private OnItemInteraction mListener;

    private boolean loading;

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public abstract class TimeLineViewHolder extends AbstractDraggableItemViewHolder {

        FrameLayout mContainer;

        public TimeLineViewHolder(View itemView) {
            super(itemView);
            mContainer = (FrameLayout) itemView.findViewById(R.id.container);
        }

        public abstract int getViewType();
    }

    public class NoteViewHolder extends TimeLineViewHolder {

        TextView headText;
        ImageView smallMark;
        TextView locationText;
        ImageView dispImage;
        ImageView menuButton;
        ImageView reorderButton;
        ImageView deleteButton;
        ImageView editButton;

        public NoteViewHolder(View itemView) {
            super(itemView);
            headText = (TextView) itemView.findViewById(R.id.placeText);
            smallMark = (ImageView) itemView.findViewById(R.id.small_mark);
            locationText = (TextView) itemView.findViewById(R.id.location_text);
            dispImage = (ImageView) itemView.findViewById(R.id.disp_image);
            menuButton = (ImageView) itemView.findViewById(R.id.menuButton);
            deleteButton = (ImageView) itemView.findViewById(R.id.deleteButton);
            editButton = (ImageView) itemView.findViewById(R.id.editButton);
            reorderButton = (ImageView) itemView.findViewById(R.id.reorderButton);
            reorderButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            isPressed = true;
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            isPressed = false;
                            break;
                        }
                    }
                    return true;
                }
            });
            if (reorder || delete) {
                mContainer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
        }

        @Override
        public int getViewType() {
            return NOTE_VIEW_TYPE;
        }
    }

    public class CheckInViewHolder extends TimeLineViewHolder {

        TextView headText;
        TextView locationText;
        TextView timeText;
        ImageView dispImage;
        ImageView menuButton;
        ImageView reorderButton;
        ImageView editButton;
        ImageView deleteButton;

        WahImageView checkInPhoto;
        WahImageView checkInMap;

        public CheckInViewHolder(View itemView) {
            super(itemView);
            headText = (TextView) itemView.findViewById(R.id.placeText);
            locationText = (TextView) itemView.findViewById(R.id.location_text);
            //asymmetricGridView = (AsymmetricGridView)itemView.findViewById(R.id.asymmetric_grid_view);
            timeText = (TextView) itemView.findViewById(R.id.time_text);
            dispImage = (ImageView) itemView.findViewById(R.id.disp_image);
            menuButton = (ImageView) itemView.findViewById(R.id.menuButton);
            deleteButton = (ImageView) itemView.findViewById(R.id.deleteButton);
            editButton = (ImageView) itemView.findViewById(R.id.editButton);
            reorderButton = (ImageView) itemView.findViewById(R.id.reorderButton);
            checkInMap = (WahImageView) itemView.findViewById(R.id.checkin_map);
            checkInPhoto = (WahImageView) itemView.findViewById(R.id.checkin_photo);
            reorderButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            isPressed = true;
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            isPressed = false;
                            break;
                        }
                    }
                    return true;
                }
            });
            if (reorder || delete) {
                mContainer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
        }

        @Override
        public int getViewType() {
            return CHECK_IN_VIEW_TYPE;
        }
    }

    public abstract class AlbumViewHolder extends TimeLineViewHolder {
        TextView headText;
        ImageView smallMark;
        TextView locationText;
        ImageView dispImage;


        public AlbumViewHolder(View itemView) {
            super(itemView);
            headText = (TextView) itemView.findViewById(R.id.placeText);
            smallMark = (ImageView) itemView.findViewById(R.id.small_mark);
            locationText = (TextView) itemView.findViewById(R.id.location_text);
            dispImage = (ImageView) itemView.findViewById(R.id.disp_image);
        }
    }

    public class AlbumGridViewHolder extends AlbumViewHolder {

        ImageView menuButton;
        ImageView reorderButton;
        ImageView editButton;
        ImageView deleteButton;
        AsymmetricGridView asymmetricGridView;

        public AlbumGridViewHolder(View itemView) {
            super(itemView);
            menuButton = (ImageView) itemView.findViewById(R.id.menuButton);
            editButton = (ImageView) itemView.findViewById(R.id.editButton);
            deleteButton = (ImageView) itemView.findViewById(R.id.deleteButton);
            reorderButton = (ImageView) itemView.findViewById(R.id.reorderButton);
            reorderButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            isPressed = true;
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            isPressed = false;
                            break;
                        }
                    }
                    return true;
                }
            });
            if (reorder || delete) {
                mContainer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
            this.asymmetricGridView = (AsymmetricGridView) itemView.findViewById(R.id.asymmetric_grid_view);
        }

        @Override
        public int getViewType() {
            return ALBUM_GRID_VIEW_TYPE;
        }
    }

    public class AlbumSinglePhotoViewHolder extends AlbumViewHolder {

        ImageView menuButton;
        ImageView reorderButton;
        WahImageView photoImage;
        ImageView deleteButton;
        ImageView editButton;

        public AlbumSinglePhotoViewHolder(View itemView) {
            super(itemView);
            mContainer = (FrameLayout) itemView.findViewById(R.id.container);
            menuButton = (ImageView) itemView.findViewById(R.id.menuButton);
            editButton = (ImageView) itemView.findViewById(R.id.editButton);
            deleteButton = (ImageView) itemView.findViewById(R.id.deleteButton);
            reorderButton = (ImageView) itemView.findViewById(R.id.reorderButton);
            reorderButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            isPressed = true;
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            isPressed = false;
                            break;
                        }
                    }
                    return true;
                }
            });
            if (reorder || delete) {
                mContainer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
            this.photoImage = (WahImageView) itemView.findViewById(R.id.photo_image);
        }

        @Override
        public int getViewType() {
            return ALBUM_SINGLE_PHOTO_VIEW_TYPE;
        }
    }

    public class LocationPinViewHolder extends TimeLineViewHolder {

        ImageView plusTimeline;
        TextView cityText;
        TextView countryText;
        WahImageView mapFragment;


        public LocationPinViewHolder(View itemView) {
            super(itemView);
            this.cityText = (TextView) itemView.findViewById(R.id.placeText);
            this.countryText = (TextView) itemView.findViewById(R.id.country_text);
            this.plusTimeline = (ImageView) itemView.findViewById(R.id.plus_timeline);
            this.mapFragment = (WahImageView) itemView.findViewById(R.id.map_fragment);
        }

        @Override
        public int getViewType() {
            return LOCATION_PIN_VIEW_TYPE;
        }
    }

    public class DaySummaryViewHolder extends TimeLineViewHolder {

        TextView distCount;
        TextView checkCount;
        TextView photoCount;
        ImageView summary_map_layer;
        LinearLayout day_summary_layout;
        LinearLayout day_summary_distance;
        LinearLayout day_summary_checkin;
        LinearLayout day_summary_photo;

        public DaySummaryViewHolder(View itemView) {
            super(itemView);
            this.distCount = (TextView) itemView.findViewById(R.id.km_number);
            this.checkCount = (TextView) itemView.findViewById(R.id.checkins_number);
            this.photoCount = (TextView) itemView.findViewById(R.id.photo_number);
            this.summary_map_layer = (ImageView) itemView.findViewById(R.id.summary_map_layer);
            this.day_summary_layout = (LinearLayout) itemView.findViewById(R.id.day_summary_layout);
            this.day_summary_distance = (LinearLayout) itemView.findViewById(R.id.day_summary_distance);
            this.day_summary_checkin = (LinearLayout) itemView.findViewById(R.id.day_summary_checkin);
            this.day_summary_photo = (LinearLayout) itemView.findViewById(R.id.day_summary_photo);
        }

        @Override
        public int getViewType() {
            return DAY_SUMMARY_VIEW_TYPE;
        }
    }

    public class LoaderViewHolder extends TimeLineViewHolder {

        ProgressBar progressBar;

        public LoaderViewHolder(View itemView) {
            super(itemView);
            this.progressBar = (ProgressBar) itemView.findViewById(R.id.timeline_progress);
        }

        @Override
        public int getViewType() {
            return LOADER_VIEW_TYPE;
        }
    }

    public TimelineRecyclerAdapter(Context context, OnItemInteraction listener, List<Timeline> timelines, boolean reorder, boolean delete,
                                   boolean canWrite) {
        this.timelines = timelines;
        this.mListener = listener;
        this.mContext = context;
        this.reorder = reorder;
        this.delete = delete;
        setHasStableIds(true);
        this.canWrite = canWrite;
        displayMetrics = mContext.getResources().getDisplayMetrics();
    }

    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case ALBUM_GRID_VIEW_TYPE:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.photo_with_grid, parent, false);
                return new AlbumGridViewHolder(v);
            case ALBUM_SINGLE_PHOTO_VIEW_TYPE:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.photo_with_image, parent, false);
                return new AlbumSinglePhotoViewHolder(v);
            case CHECK_IN_VIEW_TYPE:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.checkin_with_grid, parent, false);
                return new CheckInViewHolder(v);
            case DAY_SUMMARY_VIEW_TYPE:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.timeline_day_summary, parent, false);
                return new DaySummaryViewHolder(v);
            case LOCATION_PIN_VIEW_TYPE:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.timeline_pin, parent, false);
                return new LocationPinViewHolder(v);
            case LOADER_VIEW_TYPE:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.timeline_loader, parent, false);
                return new LoaderViewHolder(v);
            default:
                v = LayoutInflater.from(mContext)
                        .inflate(R.layout.note_timeline, parent, false);
                return new NoteViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous

        if (position + 1 == getItemCount()) {
            return LOADER_VIEW_TYPE;
        }

        Timeline timeline = timelines.get(position);
        String contentType = timeline.getContentType();
        if (Timeline.ALBUM_CONTENT.equals(contentType)) {
            Album album = (Album) timeline.getContent();
            List<Media> mediaList = album.getMedia();
            if(mediaList!=null){
                if (mediaList.size() > 1) {
                    return ALBUM_GRID_VIEW_TYPE;
                } else {
                    return ALBUM_SINGLE_PHOTO_VIEW_TYPE;
                }
            }
        }
        if (Timeline.CHECK_IN_CONTENT.equals(contentType)) {
            return CHECK_IN_VIEW_TYPE;
        }
        if (Timeline.DAY_SUMMARY_DUMMY_CONTENT.equals(contentType)) {
            return DAY_SUMMARY_VIEW_TYPE;
        }
        if (Timeline.DAY_LOCATION_PIN.equals(contentType))
            return LOCATION_PIN_VIEW_TYPE;
        return NOTE_VIEW_TYPE;
    }

    @Override
    public void onBindViewHolder(TimeLineViewHolder holder, int position) {

        if (position + 1 == getItemCount()) {
            bindLoaderViewHolder(holder);
        } else {
            Timeline timeline = timelines.get(position);
            switch (holder.getViewType()) {
                case CHECK_IN_VIEW_TYPE:
                    bindCheckInView(timeline, (CheckInViewHolder) holder);
                    break;
                case ALBUM_GRID_VIEW_TYPE:
                    bindAlbumGridView(timeline, (AlbumGridViewHolder) holder);
                    break;
                case ALBUM_SINGLE_PHOTO_VIEW_TYPE:
                    bindAlbumSinglePhotoView(timeline, (AlbumSinglePhotoViewHolder) holder);
                    break;
                case LOCATION_PIN_VIEW_TYPE:
                    bindLocationPinView(timeline, (LocationPinViewHolder) holder);
                    break;
                case DAY_SUMMARY_VIEW_TYPE:
                    bindDaySummaryView(timeline, (DaySummaryViewHolder) holder);
                    break;
                default:
                    bindNoteView(timeline, (NoteViewHolder) holder);
            }
        }
        final int dragState = holder.getDragStateFlags();

        if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0)) {
            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {

                // need to clear drawable state here to get correct appearance of the dragging item.
                DrawableUtils.clearState(holder.mContainer.getForeground());
            }

        }
    }

    private void bindLoaderViewHolder(TimeLineViewHolder holder) {
        LoaderViewHolder loaderViewHolder = (LoaderViewHolder) holder;
        if (loading) {
            loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
        } else {
            loaderViewHolder.progressBar.setVisibility(View.GONE);
        }
    }

    private void bindNoteView(final Timeline timeline, NoteViewHolder holder) {
        Note note = (Note) timeline.getContent();
        String noteText = note.getContent();
        String locationText = note.getLocationText();
        String timeText = getFormattedTime(timeline.getContentTime());

        holder.headText.setText(noteText);

        if (TextUtils.isEmpty(locationText)) {
            holder.smallMark.setVisibility(View.GONE);
            holder.locationText.setText(timeText);
        } else {
            if (locationText.matches("^[0-9.\\-\\s]*$") && com.weareholidays.bia.utils.ViewUtils.isNetworkAvailable(mContext)) {
                new GetLocationSynchTask(timeline, NOTE_VIEW_TYPE, note.getLocation().getLatitude(), note.getLocation().getLongitude(), holder, timeText).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            } else {
                holder.locationText.setText(locationText + LOCATION_TIME_SEPERATOR + timeText);
            }
            holder.smallMark.setVisibility(View.VISIBLE);
        }

        if (Source.WAH.equals(timeline.getSource())) {
            holder.dispImage.setImageResource(R.drawable.timeline_notes);
        } else if (Source.FB.equals(timeline.getSource())) {
            holder.dispImage.setImageResource(R.drawable.timeline_facebook);
        } else if (Source.TWITTER.equals(timeline.getSource())) {
            holder.dispImage.setImageResource(R.drawable.timeline_twitter);
        } else if (Source.INSTAGRAM.equals(timeline.getSource())) {
            holder.dispImage.setImageResource(R.drawable.timeline_instagram);
        }

        //Handle Reorder and Menu
        holder.menuButton.setVisibility(View.GONE);
        holder.reorderButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.editButton.setVisibility(View.GONE);
        holder.dispImage.setBackgroundColor(Color.parseColor("#f2f2f2"));
        holder.menuButton.setOnClickListener(new OverflowMenuClickListener(timeline));
        if (reorder) {
            holder.menuButton.setVisibility(View.GONE);
            holder.reorderButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        if (delete) {
            holder.menuButton.setVisibility(View.GONE);
            holder.reorderButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.editButton.setVisibility(View.VISIBLE);
            holder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteTimelineBulk(timeline);
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditTimeLineBulk(timeline);
                }
            });
        }
    }

    private void bindDaySummaryView(Timeline timeline, DaySummaryViewHolder holder) {
        DaySummaryDummy day = (DaySummaryDummy) timeline.getContent();
        holder.checkCount.setText("" + day.getCheckins());
        holder.distCount.setText("" + day.getDistance());
        holder.photoCount.setText("" + day.getPhotos());
        /*holder.day_summary_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showDaySummary();
            }
        });*/
        holder.day_summary_checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showDaySummary(CHECKIN_TAB);
            }
        });
        holder.day_summary_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showDaySummary(DISTANCE_TAB);
            }
        });
        holder.day_summary_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showDaySummary(PHOTO_TAB);
            }
        });
        mListener.getSummaryFragmentManager().beginTransaction()
                .replace(R.id.summary_map_layout, mListener.getMyMapFragment()).commitAllowingStateLoss();
        holder.summary_map_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showDaySummary();
            }
        });
    }

    private void bindLocationPinView(Timeline timeline, LocationPinViewHolder holder) {
        DayLocationPin dayLocationPin = (DayLocationPin) timeline.getContent();
        holder.cityText.setText(dayLocationPin.getCityName());
        ParseGeoPoint point = dayLocationPin.getParseGeoPoint();

        if (TextUtils.isEmpty(dayLocationPin.getCityName()) && com.weareholidays.bia.utils.ViewUtils.isNetworkAvailable(mContext)) {
            new GetLocationSynchTask(timeline, LOCATION_PIN_VIEW_TYPE, point.getLatitude(), point.getLongitude(), holder, "").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        }

/*
        Glide.with(mContext).load(MapUtils.getTimelineMapImageUrl(mContext, dayLocationPin.getParseGeoPoint(), displayMetrics))
                .placeholder(R.drawable.image_loader).into(holder.mapFragment);
*/
        holder.mapFragment.setImageUrl(MapUtils.getTimelineMapImageUrl(mContext, dayLocationPin.getParseGeoPoint(), displayMetrics));

        holder.countryText.setText(dayLocationPin.getCountryName());

        if (dayLocationPin.isEnded()) {
            holder.plusTimeline.setVisibility(View.GONE);
        } else {
            holder.plusTimeline.setVisibility(View.VISIBLE);
        }
    }

    private void bindAlbumSinglePhotoView(final Timeline timeline, AlbumSinglePhotoViewHolder holder) {
        Album album = (Album) timeline.getContent();

        bindAlbumTexts(timeline, holder);

        //Load Image
        List<Media> mediaList = album.getMedia();
        if (mediaList.size() > 0) {
            Media media = mediaList.get(0);
            double aspect = ((double) media.getMediaWidth()) / media.getMediaHeight();
            ViewGroup.LayoutParams layoutParams = holder.photoImage.getLayoutParams();
            int width = displayMetrics.widthPixels;
            layoutParams.height = (int) (width / aspect);

            holder.photoImage.setLayoutParams(layoutParams);
            //Glide.with(mContext).load(media.getMediaSource()).placeholder(R.drawable.image_loader).into(holder.photoImage);
            holder.photoImage.setImageUrl(media.getMediaSource());
        }

        //Handle Reorder and Menu
        holder.menuButton.setVisibility(View.GONE);
        holder.reorderButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.dispImage.setBackgroundColor(Color.parseColor("#f2f2f2"));
        holder.menuButton.setOnClickListener(new OverflowMenuClickListener(timeline));
        if (reorder) {
            holder.menuButton.setVisibility(View.GONE);
            holder.reorderButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        if (delete) {
            holder.menuButton.setVisibility(View.GONE);
            holder.reorderButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteTimelineBulk(timeline);
                }
            });
            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditTimeLineBulk(timeline);
                }
            });
        }

        if (!reorder && !delete) {
            holder.photoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditTimeLine(timeline);
                }
            });
        }
    }

    private void bindAlbumGridView(final Timeline timeline, AlbumGridViewHolder holder) {
        Album album = (Album) timeline.getContent();
        bindAlbumTexts(timeline, holder);

        List<Media> mediaList = album.getMedia();

        AsymmetricGridView listView = (AsymmetricGridView) holder.asymmetricGridView.findViewById(R.id.asymmetric_grid_view);
        listView.setRequestedHorizontalSpacing(0);
        listView.setVerticalScrollBarEnabled(false);

        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        int heightGrid;
        int colCount = 3;
        // Choose your own preferred column width

        final List<DemoItem> items = new ArrayList<>();
        switch (mediaList.size()) {
            case 2:
                colCount = 2;
                break;
        }
        boolean imagesExceeded = false;
        listView.setRequestedColumnCount(colCount);
        listView.determineColumns();
        heightGrid = (int) (displayMetrics.heightPixels * 704 / 2000);
        switch (mediaList.size()) {
            case 0:
                heightGrid = 0;
                break;
            case 2:
                heightGrid = (displayMetrics.heightPixels / 6);
                break;
            case 3:
                heightGrid = (displayMetrics.heightPixels / 6);
                break;
        }

        layoutParams.height = heightGrid; //this is in pixels
        listView.setLayoutParams(layoutParams);
        DemoItem item;
        int colSpanCount = 0;
        for (int i = 0; i < mediaList.size(); i++) {
            if (colSpanCount >= 6) {
                break;
            }
            int colSpan = 1;
            Media media = mediaList.get(i);
            double aspect = ((double) media.getMediaWidth()) / media.getMediaHeight();
            if (mediaList.size() > 3) {
                if (aspect > 1 && aspect <= 2)
                    colSpan = 2;
                else if (aspect > 2) {
                    colSpan = 3;
                }
                colSpanCount += colSpan;
                if (colSpanCount > 6) {
                    colSpan -= colSpanCount - 6; // remove excess colspans
                    colSpanCount = 6;
                }

                if (colSpanCount - colSpan < 3 && colSpanCount > 3) { // don't cross over colspancount = 3
                    //if (colSpan >= 2) {
                    colSpan = 3 - colSpanCount + colSpan;
                    colSpanCount = 3;
                    //}
                }
                if (items.size() == mediaList.size() - 1) { // last item, fill 6 places
                    colSpan += 6 - colSpanCount;
                }
            }
            item = new DemoItem(colSpan, 1, i);
            item.setMedia(media);
            items.add(item);
        }
        if (items.size() < mediaList.size()) {
            imagesExceeded = true;
        }

        // initialize your items array
        DefaultListAdapter adapter = new DefaultListAdapter(mContext, items, mediaList.size() - items.size(), timeline);
        adapter.setmListener(mListener);
        AsymmetricGridViewAdapter asymmetricAdapter =
                new AsymmetricGridViewAdapter<>(mContext, listView, adapter);
        listView.setAdapter(asymmetricAdapter);
        final boolean finalImagesExceeded = imagesExceeded;
        if (!reorder && !delete) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!finalImagesExceeded || position != items.size() - 1)
                        mListener.onEditTimeline(timeline, position);
                }
            });
        }

        //Handle Reorder and Menu
        holder.menuButton.setVisibility(View.GONE);
        holder.reorderButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.dispImage.setBackgroundColor(Color.parseColor("#f2f2f2"));
        holder.menuButton.setOnClickListener(new OverflowMenuClickListener(timeline));
        if (reorder) {
            holder.menuButton.setVisibility(View.GONE);
            holder.reorderButton.setVisibility(View.VISIBLE);
            holder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        if (delete) {
            holder.menuButton.setVisibility(View.GONE);
            holder.reorderButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteTimelineBulk(timeline);
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onEditTimeLineBulk(timeline);
                }
            });
        }
    }

    private void bindAlbumTexts(Timeline timeline, AlbumViewHolder holder) {
        Album album = (Album) timeline.getContent();

        int mediaCount = album.getMedia().size();
        String extraText = "";
        if (mediaCount == 1)
            extraText = " Photo clicked at ";
        else
            extraText = " Photos clicked at ";

        String headerText = album.getContent();
        String locationText = album.getLocationText();
        String timeText = getFormattedTime(timeline.getContentTime());

        if (Source.WAH.equals(timeline.getSource())) {
            if (TextUtils.isEmpty(locationText)) {
                holder.headText.setText(mediaCount + extraText + timeText);
                holder.smallMark.setVisibility(View.GONE);
                holder.locationText.setVisibility(View.GONE);
                holder.dispImage.setImageResource(R.drawable.timeline_photo);
            } else {
                holder.headText.setText(locationText);
                holder.smallMark.setVisibility(View.GONE);
                holder.locationText.setVisibility(View.VISIBLE);
                holder.locationText.setText(timeText);
                holder.dispImage.setImageResource(R.drawable.timeline_location);
            }
        } else {
            if (TextUtils.isEmpty(headerText) && TextUtils.isEmpty(locationText)) {
                holder.headText.setText(mediaCount + extraText + timeText);
                holder.smallMark.setVisibility(View.GONE);
                holder.locationText.setVisibility(View.GONE);
            } else {
                String firstLineText = "";
                String secondLineText = "";

                //Header Text is first line, location and time in second line
                if (!TextUtils.isEmpty(headerText) && !TextUtils.isEmpty(locationText)) {
                    firstLineText = headerText;
                    secondLineText = locationText + LOCATION_TIME_SEPERATOR + timeText;
                    holder.smallMark.setVisibility(View.VISIBLE);
                } else if (TextUtils.isEmpty(headerText)) {//location in first line and time in second line
                    firstLineText = locationText;
                    secondLineText = timeText;
                    holder.smallMark.setVisibility(View.GONE);
                } else if (TextUtils.isEmpty(locationText)) {//header in first line and time in second line
                    firstLineText = headerText;
                    secondLineText = timeText;
                    holder.smallMark.setVisibility(View.GONE);
                }

                holder.headText.setText(firstLineText);
                holder.locationText.setText(secondLineText);
                holder.locationText.setVisibility(View.VISIBLE);
            }

            if (Source.FB.equals(timeline.getSource())) {
                holder.dispImage.setImageResource(R.drawable.timeline_facebook);
            } else if (Source.INSTAGRAM.equals(timeline.getSource())) {
                holder.dispImage.setImageResource(R.drawable.timeline_instagram);
            } else if (Source.TWITTER.equals(timeline.getSource())) {
                holder.dispImage.setImageResource(R.drawable.timeline_twitter);
            }
        }
    }

    public void bindCheckInView(final Timeline timeline, CheckInViewHolder checkInViewHolder) {
        CheckIn checkIn = (CheckIn) timeline.getContent();
        String timeText = getFormattedTime(timeline.getContentTime());
        String locationText = checkIn.getLocationText();

        if (Source.WAH.equals(timeline.getSource())) {
            locationText = checkIn.getName();
        }

        if (locationText.matches("^[0-9.\\-\\s]*$") && com.weareholidays.bia.utils.ViewUtils.isNetworkAvailable(mContext)) {
            new GetLocationSynchTask(timeline, CHECK_IN_VIEW_TYPE, checkIn.getLocation().getLatitude(), checkIn.getLocation().getLongitude(), checkInViewHolder, "").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
            checkInViewHolder.headText.setText(locationText);
        }

        checkInViewHolder.locationText.setText(timeText);

        String photoReferenceUrl = "";

        if (!TextUtils.isEmpty(checkIn.getPhotoReference()))
            photoReferenceUrl = MapUtils.getPhotoReferenceUrl(checkIn.getPhotoReference(), displayMetrics);

        if (TextUtils.isEmpty(photoReferenceUrl)) {
            checkInViewHolder.checkInPhoto.setVisibility(View.GONE);
            int width = displayMetrics.widthPixels - Utils.dpToPx(mContext, 16);
            String url = MapUtils.getCheckInMapImageUrl(checkIn.getLocation(), width, displayMetrics.heightPixels / 6);

            ViewGroup.LayoutParams layoutParamsMap = checkInViewHolder.checkInMap.getLayoutParams();
            layoutParamsMap.width = width;
            layoutParamsMap.height = displayMetrics.heightPixels / 6;
            checkInViewHolder.checkInMap.setLayoutParams(layoutParamsMap);
            checkInViewHolder.checkInMap.setImageUrl(url);
          //  Glide.with(mContext).load(url).placeholder(R.drawable.image_loader).into(checkInViewHolder.checkInMap);
        } else {

            int colWidth = (displayMetrics.widthPixels - Utils.dpToPx(mContext, 20)) / 3;

            ViewGroup.LayoutParams layoutParams = checkInViewHolder.checkInPhoto.getLayoutParams();
            layoutParams.height = displayMetrics.heightPixels / 6;
            layoutParams.width = colWidth;
            checkInViewHolder.checkInPhoto.setLayoutParams(layoutParams);
            ViewGroup.LayoutParams layoutParamsMap = checkInViewHolder.checkInMap.getLayoutParams();
            layoutParamsMap.width = colWidth * 2;
            layoutParamsMap.height = displayMetrics.heightPixels / 6;
            checkInViewHolder.checkInMap.setLayoutParams(layoutParamsMap);

            String url = MapUtils.getCheckInMapImageUrl(checkIn.getLocation(), colWidth * 2, displayMetrics.heightPixels / 6);
            //Glide.with(mContext).load(url).placeholder(R.drawable.image_loader).into(checkInViewHolder.checkInMap);
            checkInViewHolder.checkInMap.setImageUrl(url);
            checkInViewHolder.checkInPhoto.setVisibility(View.VISIBLE);
           // Glide.with(mContext).load(photoReferenceUrl).centerCrop().placeholder(R.drawable.image_loader).into(checkInViewHolder.checkInPhoto);
            checkInViewHolder.checkInPhoto.setImageUrl(photoReferenceUrl);
        }
//        AsymmetricGridView listView = (AsymmetricGridView) checkInViewHolder.asymmetricGridView.findViewById(R.id.asymmetric_grid_view);
//        listView.setRequestedHorizontalSpacing(0);
//        listView.setVerticalScrollBarEnabled(false);
//        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
//        int colCount = 3;
//        final List<DemoItem> items = new ArrayList<>();
//        layoutParams.height = (displayMetrics.heightPixels/6);
//        listView.setLayoutParams(layoutParams);
//        DemoItem item;
//        listView.setRequestedColumnCount(colCount);
//        listView.determineColumns();
//        int testCount = 3;
//        if(!TextUtils.isEmpty(checkIn.getPhotoReference())){
//            item = new DemoItem(1, 1, 0);
//            items.add(item);
//            testCount -= 1;
//        }
//        item = new DemoItem(testCount, 1, 0);
//        items.add(item);
//        CheckinListAdapter adapter = new CheckinListAdapter(mContext, items, displayMetrics
//                , timeline);
//        AsymmetricGridViewAdapter asymmetricAdapter =
//                new AsymmetricGridViewAdapter<>(mContext, listView, adapter);
//        listView.setAdapter(asymmetricAdapter);

        //As of now check in is only supported for WAH and Facebook (facebook also is not fully supported)
        if (Source.WAH.equals(timeline.getSource())) {
            checkInViewHolder.dispImage.setImageResource(R.drawable.timeline_checkin);
        } else if (Source.FB.equals(timeline.getSource())) {
            checkInViewHolder.dispImage.setImageResource(R.drawable.checkin_fb);
        }

        //Handle Reorder and Menu
        checkInViewHolder.menuButton.setVisibility(View.GONE);
        checkInViewHolder.reorderButton.setVisibility(View.GONE);
        checkInViewHolder.deleteButton.setVisibility(View.GONE);
        checkInViewHolder.dispImage.setBackgroundColor(Color.parseColor("#f2f2f2"));
        checkInViewHolder.menuButton.setOnClickListener(new OverflowMenuClickListener(timeline));
        if (reorder) {
            checkInViewHolder.menuButton.setVisibility(View.GONE);
            checkInViewHolder.reorderButton.setVisibility(View.VISIBLE);
            checkInViewHolder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        if (delete) {
            checkInViewHolder.menuButton.setVisibility(View.GONE);
            checkInViewHolder.reorderButton.setVisibility(View.GONE);
            checkInViewHolder.deleteButton.setVisibility(View.VISIBLE);
            checkInViewHolder.dispImage.setBackgroundColor(mContext.getResources().getColor(R.color.white));

            checkInViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDeleteTimelineBulk(timeline);
                }
            });
        }
    }

    private String getFormattedTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY) % 12;
        int minutes = calendar.get(Calendar.MINUTE);
        int amPm = calendar.get(Calendar.AM_PM);
        String minuteText = "";
        String dayText;
        String hourText = "";
        if (amPm == 0)
            dayText = "am";
        else
            dayText = "pm";

        if (minutes < 10)
            minuteText = "0";
        if (hours < 10)
            hourText = "0";

        if (amPm != 0 && hours == 0) {
            hourText = "";
            hours = 12;
        }

        return hourText + hours + ":" + minuteText + minutes + " " + dayText;
    }

    public class OverflowMenuClickListener implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        public Timeline timeline;

        public OverflowMenuClickListener(Timeline timeline) {
            this.timeline = timeline;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(mContext, v);
            popup.setOnMenuItemClickListener(this);
            MenuInflater inflater = popup.getMenuInflater();
            if (canWrite)
                inflater.inflate(R.menu.menu_timeline, popup.getMenu());
            else
                inflater.inflate(R.menu.menu_timeline_view, popup.getMenu());
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            int itemId = arg0.getItemId();
            if (itemId == R.id.view_post) {
                mListener.onEditTimeLine(timeline);
                return true;
            }
            if (itemId == R.id.delete_post) {
                mListener.onDeleteTimeline(timeline);
                return true;
            }
            return false;
        }

    }

    @Override
    public int getItemCount() {
        return (null != timelines ? timelines.size() + 1 : 1);
    }

    @Override
    public long getItemId(int position) {
        if (position + 1 == getItemCount()) {
            return 1234;
        }
        return timelines.get(position).getDateInMilli();
    }

    public int getPositionForId(long id) {
        for (int i = 0; i < timelines.size(); i++) {
            if (timelines.get(i).getDateInMilli() == id) {
                return i;
            }
        }
        return -1;
    }

    public Timeline getItemForId(long id) {
        for (int i = 0; i < timelines.size(); i++) {
            if (timelines.get(i).getDateInMilli() == id) {
                return timelines.get(i);
            }
        }
        return null;
    }

    public void moveItem(int start, int end) {
        int max = Math.max(start, end);
        int min = Math.min(start, end);
        if (min >= 0 && max < timelines.size()) {
            Timeline item = timelines.remove(min);
            timelines.add(max, item);
            notifyItemMoved(min, max);
            mListener.onReordered();
        }
    }


    public List<Timeline> getTimelines() {
        return timelines;
    }

    @Override
    public boolean onCheckCanStartDrag(TimeLineViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mContainer;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return isPressed && ViewUtils.hitTest(containerView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(TimeLineViewHolder myViewHolder, int i) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        moveItem(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        // TODO: Need to check this implementation, new method added to interface after updating dependency
        return true;
    }

    public interface OnItemInteraction {

        void onEditTimeLine(Timeline timeline);

        void onDeleteTimeline(Timeline timeline);

        void onEditTimeline(Timeline timeline, int position);

        void onReordered();

        void showDaySummary();

        void showDaySummary(int itemSelected);

        FragmentManager getSummaryFragmentManager();

        MyMapFragment getMyMapFragment();

        void onDeleteTimelineBulk(Timeline timeline);

        void onEditTimeLineBulk(Timeline timeline);

    }


    private class GetLocationSynchTask extends AsyncTask<String, Void, Void> {

        private Timeline timeline;
        private int timeLineType;
        double latitude, longitude;
        TimeLineViewHolder holder;
        List<Address> addresses;
        private String timeText;

        public GetLocationSynchTask(Timeline timeline, int timelineType, double latitude, double longitude, TimeLineViewHolder holder, String timeText) {
            this.timeline = timeline;
            this.latitude = latitude;
            this.longitude = longitude;
            this.holder = holder;
            this.timeLineType = timelineType;
            this.timeText = timeText;
        }

        /**
         * This task gets the address string for all pictures which have latitude and longitud
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(String... params) {
            /**
             * From android docs
             * Please refer https://developer.android.com/training/location/display-address.html
             */
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (addresses != null && addresses.size() > 0) {

                String imageAddress;
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String knownName = addresses.get(0).getFeatureName();
                addresses.get(0).getSubLocality();
                String country = addresses.get(0).getCountryName();
                imageAddress = address + ", " + city + ", " + state;
                Log.v("address", address + imageAddress);

                if (timeLineType == CHECK_IN_VIEW_TYPE) {
                    ((CheckInViewHolder) holder).headText.setText(imageAddress);
                    CheckIn checkIn = (CheckIn) timeline.getContent();
                    if (Source.WAH.equals(timeline.getSource())) {
                        checkIn.setName(imageAddress);
                        checkIn.saveInBackground();
                    }
                } else if (timeLineType == LOCATION_PIN_VIEW_TYPE) {
                    ((LocationPinViewHolder) holder).cityText.setText(city);
                    ((LocationPinViewHolder) holder).countryText.setText(country);
                    DayLocationPin dayLocationPin = (DayLocationPin) timeline.getContent();
                    dayLocationPin.setCityName(city);
                    dayLocationPin.setCountryName(country);
                    dayLocationPin.saveInBackground();
                } else if (timeLineType == NOTE_VIEW_TYPE) {
                    ((NoteViewHolder) holder).locationText.setText(imageAddress + LOCATION_TIME_SEPERATOR + timeText);
                    Note note = (Note) timeline.getContent();
                    note.setLocationText(imageAddress);
                    note.saveInBackground();
                }
            }
        }
    }
}