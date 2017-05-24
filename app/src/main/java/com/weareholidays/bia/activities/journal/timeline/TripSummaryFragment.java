package com.weareholidays.bia.activities.journal.timeline;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.base.TripOperationsLoader;
import com.weareholidays.bia.activities.journal.trip.TripActivity;
import com.weareholidays.bia.adapters.SummaryLocationAdapter;
import com.weareholidays.bia.coachmarks.ShowcaseView;
import com.weareholidays.bia.coachmarks.targets.Target;
import com.weareholidays.bia.coachmarks.targets.ViewTarget;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.DaySummary;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;
import com.weareholidays.bia.utils.SharedPrefUtils;
import com.weareholidays.bia.utils.ViewUtils;
import com.weareholidays.bia.widgets.ExpandableHeightGridView;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.weareholidays.bia.utils.SharedPrefUtils.Keys.COACH_PUBLISHED_TRIP_PREF;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TripSummaryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripSummaryFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private TripOperations tripOperations;
    private TextView durationView;
    private TextView distanceView;
    private TextView kmText, tripDuration;
    private ExpandableHeightGridView locationGrid;
    private CardView tripSummaryLocationCard;
    private double distance;
    int duration;
    int checkIns = 0;
    int photos = 0;
    int fb = 0;
    int twitter = 0;
    int instagram = 0;
    int notes = 0;
    LoadTotalDistance mLoadDistanceTask;
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy");

    View rootView;
    TextView checkInView;
    TextView photoView;
    TextView fbView;
    TextView twitterView;
    TextView instagramView;

    private ShowcaseView showcaseView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TripSummaryFragment.
     */
    public static TripSummaryFragment newInstance() {
        TripSummaryFragment fragment = new TripSummaryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TripSummaryFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_trip_summary, container, false);
        TextView text = (TextView) rootView.findViewById(R.id.summary_duration_unit);
        kmText = (TextView) rootView.findViewById(R.id.summary_distance_unit);
        locationGrid = (ExpandableHeightGridView) rootView.findViewById(R.id.location_gridview);
        locationGrid.setExpanded(true);
        tripDuration = (TextView) rootView.findViewById(R.id.tv_trip_duration);
        tripSummaryLocationCard = (CardView) rootView.findViewById(R.id.trip_summary_location_card);

        setup(rootView);
        mLoadDistanceTask = new LoadTotalDistance();
        mLoadDistanceTask.execute();
        int days = tripOperations.getTrip().getDays().size();
        if (days > 1)
            text.setText("DAYS");
        if (days > 99) {
            durationView.setTextSize(25);
            text.setTextSize(15);
        } else if (days > 9) {
            durationView.setTextSize(35);
            text.setTextSize(15);
        }

        if (tripOperations.getTrip().isFinished()) {
            Date date = tripOperations.getTrip().getStartTime();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            String startDate = sdf.format(c.getTime());
            c.add(Calendar.DATE, days - 1); //Add the number of days
            String endDate = sdf.format(c.getTime());

            if (days > 1) {
                tripDuration.setText(startDate + " - " + endDate);
            } else {
                tripDuration.setText(startDate);
            }

            ArrayList<Integer> counts = new ArrayList<>();
            ArrayList<String> dayLocations = new ArrayList<>();
            for (Day day : tripOperations.getTrip().getDays()) {
                String city = day.getCity();
                if (city == null) {
                    if (day.getLocation() != null && ViewUtils.isNetworkAvailable(getActivity())) {
                        new GetLocationSynchTask(getActivity(), tripOperations, day.getLocation().getLatitude(), day.getLocation().getLongitude(), day.getDisplayOrder()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                    }
                    continue;
                }

                if (city != null) {
                    int index = dayLocations.indexOf(city);
                    if (index != -1) {
                        counts.set(index, counts.get(index) + 1);
                    } else {
                        dayLocations.add(city);
                        counts.add(1);
                    }
                }
            }

            SummaryLocationAdapter adapter = new SummaryLocationAdapter(getActivity(), dayLocations, counts);
            locationGrid.setAdapter(adapter);
        } else {
            tripSummaryLocationCard.setVisibility(View.GONE);
        }

        drawCoachMarks();
        return rootView;
    }

    public void startDistanceIntent() {
        startActivity(NavigationUtils.getDistanceIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    public void startDurationIntent() {
        startActivity(NavigationUtils.getDurationIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    public void startCheckInIntent() {
        if (checkIns == 0) {
            showDialog("checjachck-in");
            return;
        }
        startActivity(NavigationUtils.getCheckInIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    public void startPhotoIntent() {
        if (photos == 0) {
            showDialog("photo");
            return;
        }
        startActivity(NavigationUtils.getPhotoIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    public void startFbIntent() {
        if (fb == 0) {
            showDialog("facebook");
            return;
        }
        startActivity(NavigationUtils.getFbIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    public void startTwitterIntent() {
        if (twitter == 0) {
            showDialog("twitter");
            return;
        }
        startActivity(NavigationUtils.getTwitterIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    public void startInstagramIntent() {
        if (instagram == 0) {
            showDialog("instagram");
            return;
        }
        startActivity(NavigationUtils.getInstIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    public void startNotesIntent() {
        if (notes == 0) {
            showDialog("notes");
            return;
        }
        startActivity(NavigationUtils.getNotesIntent(getActivity(), tripOperations.getTripKey()));
        NavigationUtils.openAnimation(getActivity());
    }

    private void setup(View rootView) {

        distanceView = (TextView) rootView.findViewById(R.id.summary_distance);
        durationView = (TextView) rootView.findViewById(R.id.summary_duration);
        checkInView = (TextView) rootView.findViewById(R.id.summary_check_ins);
        photoView = (TextView) rootView.findViewById(R.id.summary_photos);

        fbView = (TextView) rootView.findViewById(R.id.summary_fb);
        twitterView = (TextView) rootView.findViewById(R.id.summary_twitter);
        instagramView = (TextView) rootView.findViewById(R.id.summary_instagram);
        TextView notesView = (TextView) rootView.findViewById(R.id.summary_notes);

        CardView distanceCard = (CardView) rootView.findViewById(R.id.distance_block);
        CardView durationCard = (CardView) rootView.findViewById(R.id.duration_block);
        CardView checkInCard = (CardView) rootView.findViewById(R.id.check_in_block);
        CardView photoCard = (CardView) rootView.findViewById(R.id.photo_block);

        CardView fbCard = (CardView) rootView.findViewById(R.id.fb_block);
        CardView twitterCard = (CardView) rootView.findViewById(R.id.twitter_block);
        CardView instagramCard = (CardView) rootView.findViewById(R.id.inst_block);
        CardView notesCard = (CardView) rootView.findViewById(R.id.notes_block);

        distanceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDistanceIntent();
            }
        });
        durationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDurationIntent();
            }
        });
        checkInCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheckInIntent();
            }
        });
        photoCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhotoIntent();
            }
        });

        fbCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFbIntent();
            }
        });
        twitterCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTwitterIntent();
            }
        });
        instagramCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInstagramIntent();
            }
        });
        notesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNotesIntent();
            }
        });


        List<Day> days = tripOperations.getTrip().getDays();

        distance = 0;
        duration = days.size();

        for (Day day : days) {
            DaySummary daySummary = day.getDaySummary();

            checkIns += daySummary.getCheckIns();
            if (tripOperations.canWrite())
                photos += daySummary.getPhotos();
            else
                photos += daySummary.getPublicPhotos();
            fb += daySummary.getFacebook();
            instagram += daySummary.getInstagram();
            twitter += daySummary.getTwitter();
            notes += daySummary.getNotes();
        }

        distance = Math.round(distance);

        distanceView.setText(String.valueOf((int) distance));
        durationView.setText(String.valueOf(duration));
        checkInView.setText(String.valueOf(checkIns));
        photoView.setText(String.valueOf(photos));
        fbView.setText(String.valueOf(fb));
        twitterView.setText(String.valueOf(twitter));
        notesView.setText(String.valueOf(notes));
        instagramView.setText(String.valueOf(instagram));

        //TODO: handle clicks of the cards


    }

    private void showDialog(String msg) {
        new MaterialDialog.Builder(getActivity())
                .title("No posts to show!")
                .backgroundColor(Color.WHITE)
                .titleColor(Color.GRAY)
                .contentGravity(GravityEnum.CENTER)
                .show();
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
    public void onDestroyView() {
        super.onDestroyView();
        if (mLoadDistanceTask != null && !mLoadDistanceTask.isCancelled())
            mLoadDistanceTask.cancel(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        tripOperations = null;
    }

    @Override
    public void onClick(View v) {
        showcaseView.hide();
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
    public interface OnFragmentInteractionListener extends TripOperationsLoader {

    }

    /**
     * Class for calculating total trip distance, use same logic as MyMapFragment else total distance may differ
     */
    private class LoadTotalDistance extends AsyncTask<Void, Void, Void> {
        List<RoutePoint> test = new ArrayList<>();
        ArrayList<LatLng> givenPoints = new ArrayList<LatLng>();
        ArrayList<LatLng> latlng = new ArrayList<>();
        ArrayList<FlightRoute> flights = new ArrayList<FlightRoute>();
        double day_distance = 0.0;
        int skip = 0;
        PolylineOptions lineOptions = new PolylineOptions();

        public double getTotalDistance(PolylineOptions x) {
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            points.addAll(x.getPoints());
            double calcdistance = 0;
            for (int i = 0; i < points.size() - 1; i++) {
                double dis = CalculationByDistance(points.get(i), points.get(i + 1));
                calcdistance += dis;
            }
            day_distance = calcdistance;
            return calcdistance;
        }

        //  Calculate distance between two LatLng points
        public double CalculationByDistance(LatLng StartP, LatLng EndP) {
            int Radius = 6371;//radius of earth in Km
            double lat1 = StartP.latitude;
            double lat2 = EndP.latitude;
            double lon1 = StartP.longitude;
            double lon2 = EndP.longitude;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return Radius * c;
        }

        public double getSpeed(LatLng start, LatLng end) throws ParseException {
            int i = latlng.indexOf(start);
            int j = latlng.indexOf(end);
            double speed = 0;
            Date datei = test.get(i).getRecordedTime();
            Date datej = test.get(j).getRecordedTime();
            double distance = CalculationByDistance(start, end);
            double duration = (double) (datej.getTime() - datei.getTime()) / 1000 / 60 / 60;
            speed = distance / duration;
            return speed;
        }

        private ArrayList<LatLng> getLatlngPoints(List<RoutePoint> x) throws ParseException, FileNotFoundException {
            latlng.add(0, new LatLng(x.get(0).getLocation().getLatitude(), x.get(0).getLocation().getLongitude()));
            int start = 0, end = 0;
            double tmp = 0, speed = 0;
            for (int i = 1; i < test.size(); i++) {
                latlng.add(i, new LatLng(x.get(i).getLocation().getLatitude(), x.get(i).getLocation().getLongitude()));
                //  Getting index of latlng for flight's start and end Geo Points.
                speed = getSpeed(latlng.get(i - 1), latlng.get(i));
                if (speed > 400.0 && tmp < 400)
                    start = i - 1;
                end = i;
                if (speed < 400 && tmp > 400) {
                    end = i - 1;
                    flights.add(new FlightRoute(start, end));
                }
                tmp = speed;
            }
            return latlng;
        }

        private Void caluclateTotalDistance() {
            boolean loop = true;
            do {
                try {
                    List<RoutePoint> tmp = tripOperations.getTripRoutePoints(skip, 1000);
                    test.addAll(tmp);
                    skip = test.size();
                    if (tmp.size() < 1000)
                        loop = false;
                } catch (com.parse.ParseException e) {
                    Log.i("RouteMap", "Error Loading route points", e);
                }
            } while (loop);
            Collections.sort(test, new RoutePointComp());
            if (test.size() == 0) {
                Log.i("RouteMap", "Zero route points");
                return null;
            }

            try {
                givenPoints = getLatlngPoints(test);
            } catch (ParseException | FileNotFoundException e) {
                e.printStackTrace();
            }
            lineOptions.add(givenPoints.get(0));
            LatLng previousMarker = givenPoints.get(0);
            for (int i = 0, j = 0; i < givenPoints.size() - 1; ) {//first and last points are always added to list of markers
                int flightIndex = flights.size() > 0 ? flights.get(j).getStart() : Integer.MAX_VALUE;
                if (flightIndex == i) {
                    if (i != 0) { //dont add 0th point as it is already added
                        lineOptions.add(givenPoints.get(i));//add market for the start of flight
                    }
                    previousMarker = givenPoints.get(flights.get(j).getEnd()); //set previousMarker to the end point of flight
                    lineOptions.add(previousMarker); //add marker for end of flight
                    i = flights.get(j).getEnd() + 1;
                    if (j < flights.size() - 1) { // to avoid out of bound exception for flights
                        j++;
                    }
                } else if (CalculationByDistance(previousMarker, givenPoints.get(i)) > 0.5d) {
                    previousMarker = givenPoints.get(i);
                    lineOptions.add(previousMarker);
                    i++;
                } else {
                    i++;
                }
            }
            if (flights.size() == 0 || flights.get(flights.size() - 1).getEnd() != givenPoints.size() - 1) {
                lineOptions.add(givenPoints.get(givenPoints.size() - 1));
            }
            day_distance = getTotalDistance(lineOptions);
            distance = day_distance;
            return null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return caluclateTotalDistance();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isCancelled())
                return;
            super.onPostExecute(aVoid);
            if ((int) day_distance > 9999) {
                distanceView.setTextSize(22);
                kmText.setTextSize(15);
            } else if ((int) day_distance > 999) {
                distanceView.setTextSize(28);
                kmText.setTextSize(15);
            } else if ((int) day_distance > 99) {
                distanceView.setTextSize(35);
                kmText.setTextSize(15);
            }
            distanceView.setText("" + Math.round(day_distance));
        }
    }

    public class FlightRoute {
        int start = 0, end = 0;

        public FlightRoute(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }
    }

    public class RoutePointComp implements Comparator<RoutePoint> {
        @Override
        public int compare(RoutePoint e1, RoutePoint e2) {
            if (e1.getRecordedTime().after(e2.getRecordedTime())) {
                return 1;
            } else if (e1.getRecordedTime().before(e2.getRecordedTime())) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private class GetLocationSynchTask extends AsyncTask<String, Void, Void> {

        private TripOperations tripOperations;
        double latitude, longitude;
        Context mContext;
        List<Address> addresses;
        int dayOrder;

        public GetLocationSynchTask(Context context, TripOperations tripOperations, double latitude, double longitude, int dayOrder) {
            this.tripOperations = tripOperations;
            this.latitude = latitude;
            this.longitude = longitude;
            this.mContext = context;
            this.dayOrder = dayOrder;
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
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                Day day = tripOperations.getTrip().getDay(dayOrder);
                day.setCountry(country);
                day.setCity(city);
                day.saveInBackground();
            }
        }
    }

    private void drawCoachMarks() {


        if (!SharedPrefUtils.getBooleanPreference(getActivity(), COACH_PUBLISHED_TRIP_PREF)) {

            if (tripOperations.getTrip().isPublished()) {

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                DisplayMetrics outMetrics = new DisplayMetrics();
                display.getMetrics(outMetrics);

                float density = getResources().getDisplayMetrics().density;
                float dpHeight = outMetrics.heightPixels / density;
                float dpWidth = outMetrics.widthPixels / density;


                if (getActivity() instanceof TripActivity) {

                }
                View view1 = null;
                View view2 = distanceView;
                View view3 = checkInView;
                View view4 = photoView;

                Target markLocationByView[] = new Target[4];
                markLocationByView[0] = new ViewTarget(view1);
                markLocationByView[1] = new ViewTarget(view2);
                markLocationByView[2] = new ViewTarget(view3);
                markLocationByView[3] = new ViewTarget(view4);

                Point markLocationByOffset[] = new Point[4];
                markLocationByOffset[0] = new Point(-100, 0);
                markLocationByOffset[1] = new Point(0, 0);
                markLocationByOffset[2] = new Point(0, 0);
                markLocationByOffset[3] = new Point(0, 0);

                Point markTextPoint[] = new Point[4];
                markTextPoint[0] = new Point(-150, 50);
                markTextPoint[1] = new Point(50, -20);
                markTextPoint[2] = new Point(50, -40);
                markTextPoint[3] = new Point(50, -40);

                float circleRadius[] = new float[4];
                circleRadius[0] = 35f;
                circleRadius[1] = 35f;
                circleRadius[2] = 35f;
                circleRadius[3] = 35f;

                String coachMarkTextArray[];

                coachMarkTextArray = getResources().getStringArray(R.array.coachmark_published_trip);

                showcaseView = new ShowcaseView.Builder(getActivity(), true, false)
                        .setStyle(R.style.CustomShowcaseTheme2)
                        .setTarget(markLocationByView, markLocationByOffset, markTextPoint, coachMarkTextArray, circleRadius)
                        .setOnClickListener(TripSummaryFragment.this)
                        .build();

                showcaseView.setButtonText(getResources().getString(R.string.coachmark_button_gotit));

                SharedPrefUtils.setBooleanPreference(getActivity(), COACH_PUBLISHED_TRIP_PREF, true);
            }
        }
    }
}