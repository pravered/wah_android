package com.weareholidays.bia.activities.journal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseGeoPoint;
import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.timeline.TimeLineFragment;
import com.weareholidays.bia.parse.models.Album;
import com.weareholidays.bia.parse.models.CheckIn;
import com.weareholidays.bia.parse.models.Day;
import com.weareholidays.bia.parse.models.RoutePoint;
import com.weareholidays.bia.parse.models.Source;
import com.weareholidays.bia.parse.models.Timeline;
import com.weareholidays.bia.parse.models.Trip;
import com.weareholidays.bia.parse.utils.TripOperations;
import com.weareholidays.bia.utils.DebugUtils;
import com.weareholidays.bia.utils.NavigationUtils;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by hament on 1/6/15.
 */

public class MyMapFragment extends Fragment {
    GoogleMap map;
    DownloadTask downloadTask;

    ArrayList<LatLng> markerPoints;
    List<RoutePoint> test = new ArrayList<>();
    ArrayList<LatLng> givenPoints = new ArrayList<LatLng>();
    ArrayList<LatLng> latlng = new ArrayList<>();
    ArrayList<FlightRoute> flights = new ArrayList<FlightRoute>(); // used for drawing flight routes
    ArrayList<PolylineOptions> mlineOptionList = new ArrayList<>(); // used for drawing polylines
    PolylineOptions lineOptions = new PolylineOptions(); // used for drawing markers
    ArrayList<Timeline> photoTimelines = null;
    TextView total_count, map_title, count_type;
    double day_distance = 0.0, distance_interval = 10.0;
    MarkerOptions markers = new MarkerOptions();

    Boolean roadapi = false;                                // true for using Road Api and false for Direction Api
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    int color, count = 0, skip = 0;
    int dayOrder = 0;                                              // 0-number fo days for distance, check in and photos
    String type = "type";                                          // have 3 options (DISTANCE, CHECKIN, PHOTO, DURATION,TIMELINE,DURATIONMAP)
    int tab = 0;
    float scale = 1;

    private TripOperations tripOperations;
    private Trip trip;
    private Day day;
    private View mapsLoaderLayout;
    private int totalItemCount = 0;

    private final double markerDistance = 0.5d;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scale = (getActivity().getApplicationContext().getResources().getDisplayMetrics().density) / 3;
        if (getArguments() != null) {
            dayOrder = getArguments().getInt(TimeLineFragment.DAY_ORDER, dayOrder);
            type = getArguments().getString("TYPE");
            tab = getArguments().getInt("TAB", 0);
            if ("DISTANCE".equals(type))
                color = Color.rgb(48, 132, 223);
            else if ("PHOTO".equals(type))
                color = Color.rgb(101, 105, 177);
            else if (type.equals("DURATIONMAP"))
                color = Color.rgb(245, 166, 35);
            else             //(type.equals("CHECKIN"))   (type.equals("DURATION"))
                color = Color.rgb(254, 115, 76);
        }
        trip = tripOperations.getTrip();
        if (dayOrder >= 0)
            day = trip.getDay(dayOrder);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_map, null, false);
        total_count = (TextView) v.findViewById(R.id.fobject_count);
        map_title = (TextView) v.findViewById(R.id.fmap_title);
        count_type = (TextView) v.findViewById(R.id.fobject_type);
        View view = v.findViewById(R.id.shadow);
        // Getting reference to SupportMapFragment of the fragment_my_map
        SupportMapFragment fm = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fmap);
        map = fm.getMap();
        mapsLoaderLayout = v.findViewById(R.id.maps_loader_layout);

        if (type.equals("TIMELINE")) {
            view.setVisibility(View.GONE);
            total_count.setVisibility(View.GONE);
            count_type.setVisibility(View.GONE);
            map_title.setVisibility(View.GONE);
            map.getUiSettings().setZoomGesturesEnabled(false);
            map.getUiSettings().setScrollGesturesEnabled(false);
        }
        total_count.setTextColor(color);
        count_type.setTextColor(color);
        total_count.setText("0");

        if (type.equals("DISTANCE") || (type.equals("DURATION") && tab == 0)) {
            count_type.setText("KM");
            map_title.setText("DISTANCE TRAVELED");
            if (type.equals("DURATION") && tab == 0)
                map_title.setText("TOTAL DISTANCE");
        } else if (type.equals("CHECKIN") || (type.equals("DURATION") && tab == 1)) {
            map_title.setText("TOTAL CHECK-INS");
        } else if (type.equals("PHOTO") || (type.equals("DURATION") && tab == 2)) {
            map_title.setText("TOTAL PHOTOS");
        } else if (type.equals("DURATIONMAP")) {
            map_title.setText("TOTAL DURATION");
        } else {
            total_count.setText("default");
            map_title.setText("default");
        }

        Log.i("Internet", "Checking internet availablity");
        if (!isConnectingToInternet()) {
            if (!type.equals("TIMELINE")) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.no_net)
                        .content(R.string.no_net_msg)
                        .positiveText(R.string.ok)
                        .positiveColor(getResources().getColor(R.color.orange_primary))
                        .show();
            }
            return v;
        }

        if (type.equals(("DURATIONMAP"))) {
            durationMap();
            return v;
        }

        downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute();

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (type.equals("CHECKIN") || type.equals("DURATION") && tab == 1 || type.equals("DURATIONMAP"))
                    marker.showInfoWindow();
                if (type.equals("PHOTO") || type.equals("DURATION") && tab == 2) {
                    if (photoTimelines != null) {
                        String id = marker.getSnippet();
                        if (id != null) {
                            int index = Integer.parseInt(id);
                            Timeline photoTimeline = photoTimelines.get(index);
                            if (photoTimeline != null) {
                                tripOperations.setTimeLine(photoTimeline);
                                startActivity(NavigationUtils.getTimelineEditIntent(getActivity(), tripOperations.getTripKey(), getActivity().getIntent()));
                                NavigationUtils.openAnimation(getActivity());
                            }
                        }
                    }
                }
                return true;
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (downloadTask != null && downloadTask.getStatus().equals(AsyncTask.Status.RUNNING))
            downloadTask.cancel(true);
    }


    private class DownloadTask extends AsyncTask<Void, MarkerOptions, Void> {

        @Override
        protected void onPreExecute() {
            try {
                super.onPreExecute();
                if (!type.equals("TIMELINE"))
                    mapsLoaderLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }

        @Override
        protected Void doInBackground(Void... input) {
            try {

                if (type.equals("CHECKIN") || (type.equals("DURATION") && tab == 1)) {
                    try {
                        List<MarkerOptions> markerOptionsList = addCheckInMarker();
                        if (markerOptionsList.size() > 0) {
                            MarkerOptions[] markerOptionses = new MarkerOptions[markerOptionsList.size()];
                            publishProgress(markerOptionsList.toArray(markerOptionses));
                        } else {
                            publishProgress();
                        }
                    } catch (Exception e) {
                        DebugUtils.logException(e);
                    }
                } else if (type.equals("PHOTO") || (type.equals("DURATION") && tab == 2)) {
                    try {
                        List<MarkerOptions> markerOptionsList = addPhotoMarker();
                        if (markerOptionsList.size() > 0) {
                            MarkerOptions[] markerOptionses = new MarkerOptions[markerOptionsList.size()];
                            publishProgress(markerOptionsList.toArray(markerOptionses));
                        } else {
                            publishProgress();
                        }
                    } catch (Exception e) {
                        DebugUtils.logException(e);
                    }
                }

                Log.i("RouteMap", "Loading route points");
                skip = 0;
                boolean loop = true;
                if ((type.equals("DISTANCE") || type.equals("CHECKIN") || type.equals("PHOTO")) && dayOrder == -1) {
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
                } else {
                    do {
                        try {
                            List<RoutePoint> tmp = tripOperations.getDayRoutePoints(dayOrder, skip);
                            test.addAll(tmp);
                            skip = test.size();
                            if (tmp.size() < 1000)
                                loop = false;
                        } catch (com.parse.ParseException e) {
                            Log.i("RouteMap", "Error Loading route points", e);
                        }
                    } while (loop);
                }

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
                PolylineOptions mLineOptions = new PolylineOptions();
                if (flights.size() == 0 || flights.get(0).getStart() != 0) { //add first routepoint if it is not part of flight
                    mLineOptions.add(previousMarker);
                    // lineOptions.add(givenPoints.get(0));  //dont add 0th point is already added for markers
                }

                for (int i = 0, j = 0; i < givenPoints.size() - 1; ) {//first and last points are always added to list of markers
                    int flightIndex = flights.size() > 0 ? flights.get(j).getStart() : Integer.MAX_VALUE;
                    if (flightIndex == i) {
                        if (i != 0) { //dont add 0th point as it is already added
                            mLineOptions.add(givenPoints.get(i)); //start of flight is the last point in the current mLineOptions
                            lineOptions.add(givenPoints.get(i));//add market for the start of flight
                        }
                        previousMarker = givenPoints.get(flights.get(j).getEnd()); //set previousMarker to the end point of flight
                        lineOptions.add(previousMarker); //add marker for end of flight
                        i = flights.get(j).getEnd() + 1;
                        if (j < flights.size() - 1) { // to avoid out of bound exception for flights
                            j++;
                        }
                        mlineOptionList.add(mLineOptions);
                        mLineOptions = new PolylineOptions();
                        mLineOptions.add(previousMarker); //Last point of flight is first point of next mLineOptions
                    } else if (CalculationByDistance(previousMarker, givenPoints.get(i)) > markerDistance) {
                        previousMarker = givenPoints.get(i);
                        mLineOptions.add(previousMarker);
                        lineOptions.add(previousMarker);
                        i++;
                    } else {
                        i++;
                    }
                }
                if (flights.size() ==0 || flights.get(flights.size() - 1).getEnd() != givenPoints.size() - 1) {
                    lineOptions.add(givenPoints.get(givenPoints.size() - 1));
                    mLineOptions.add(givenPoints.get(givenPoints.size()-1));
                }
                mlineOptionList.add(mLineOptions);
                day_distance = getTotalDistance(lineOptions);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(MarkerOptions... values) {
            try {
                boolean addMarkers = false;
                if (type.equals("CHECKIN") || (type.equals("DURATION") && tab == 1)) {
                    addMarkers = true;
                } else if (type.equals("PHOTO") || (type.equals("DURATION") && tab == 2)) {
                    addMarkers = true;
                }

                if (addMarkers) {
                    if (values != null && values.length > 0) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        for (MarkerOptions m : values) {
                            map.addMarker(m);
                            builder.include(m.getPosition());
                        }
                        LatLngBounds bounds = builder.build();
                        int padding = 10;
                        int height = (int) (600 * scale);
                        int width = (int) (400 * scale);
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, height, width, padding);
                        map.animateCamera(cu);
                        mapsLoaderLayout.setVisibility(View.GONE);
                    }
                    total_count.setText("" + totalItemCount);
                }
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (isCancelled()) {
                    mapsLoaderLayout.setVisibility(View.GONE);
                    return;
                }
                super.onPostExecute(result);

                if (latlng.size() != 0) {
                    for (LatLng l : latlng) {
                        builder.include(l);
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 10; // offset from edges of the map in pixels
                    int height = (int) (600 * scale);
                    int width = (int) (400 * scale);
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, height, width, padding);
                    map.animateCamera(cu);
                }

                try {
                    // Drawing polyline in the Google Map for the i-th route
                    if (type.equals("DISTANCE") || (type.equals("DURATION") && tab == 0)) {
                        total_count.setText("" + Math.round(day_distance));
                    } else if (type.equals("CHECKIN") || (type.equals("DURATION") && tab == 1)) {

                    } else if (type.equals("PHOTO") || (type.equals("DURATION") && tab == 2)) {

                    } else {
                        total_count.setText("default");
                    }

                    if (lineOptions.getPoints().size() > 1)
                        getIntervalDistance(lineOptions);
                    if(type.equals("DISTANCE") || (type.equals("DURATION") && tab == 0) || type.equals("TIMELINE")) {
                        for (PolylineOptions mlineOption : mlineOptionList) {
                            mlineOption.width(8 * scale);
                            mlineOption.color(color);
                            map.addPolyline(mlineOption);
                        }
                    }
                    lineOptions = new PolylineOptions();
                } catch (Exception e) {
                    DebugUtils.logException(e);
                }
                if(type.equals("DISTANCE") || (type.equals("DURATION") && tab == 0) || type.equals("TIMELINE"))
                    drawFlightPath();
                mapsLoaderLayout.setVisibility(View.GONE);
            } catch (Exception e) {
                DebugUtils.logException(e);
            }
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

    // make latlng array list and populate flight route list
    private ArrayList<LatLng> getLatlngPoints(List<RoutePoint> x) throws ParseException, FileNotFoundException {
        latlng.add(0, new LatLng(x.get(0).getLocation().getLatitude(), x.get(0).getLocation().getLongitude()));
        int start = 0, end = 0;
        double tmp = 0, speed = 0;
        for (int i = 1; i < x.size(); i++) {
            latlng.add(i, new LatLng(x.get(i).getLocation().getLatitude(), x.get(i).getLocation().getLongitude()));
            //  Getting index of latlng for flight's start and end Geo Points.
            speed = getSpeed(latlng.get(i - 1), latlng.get(i), test.get(i-1).getRecordedTime(), test.get(i).getRecordedTime());
            if (speed > 400.0 && tmp < 400) {
                start = i - 1;
                //end = i;
            }
            if (speed < 400 && tmp > 400) {
                end = i - 1;
                flights.add(new FlightRoute(start, end));
            }
            tmp = speed;
        }
        return latlng;
    }

    /*
    private String getDirectionsUrl(LatLng origin, LatLng dest) throws ParseException {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "waypoints=";
        if (markerPoints.size() > 2) {
            for (int i = 1; i < markerPoints.size() - 1; i++) {
                LatLng point = (LatLng) markerPoints.get(i);
                waypoints += point.latitude + "," + point.longitude + "|";
            }
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service  +"&key="+API_KEY
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String getRoadUrl() throws ParseException {
        String parameters = "path=" + markerPoints.get(0).latitude + "," + markerPoints.get(0).longitude;
        for (int i = 1; i < markerPoints.size() - 1; i++) {
            parameters += "|" + markerPoints.get(i).latitude + "," + markerPoints.get(i).longitude;
        }

        String url = "https://roads.googleapis.com/v1/snapToRoads?" + parameters + "&interpolate=true&key=" + WAHApplication.GOOGLE_KEY;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    } */

    public double getTotalDistance(PolylineOptions x) {
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        points.addAll(x.getPoints());
        double distance = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            double dis = CalculationByDistance(points.get(i), points.get(i + 1));
            distance += dis;
        }
        day_distance = distance;
        if (dayOrder >= 0) {
            //TODO: save this in trip summary properly
            day.getDaySummary().setDistance(Math.round(distance));
        }
        return distance;
    }

    //  Getting Fixed distance intervaled Geo Points and placing markers.
    public void getIntervalDistance(PolylineOptions x) {
        ArrayList<LatLng> points = new ArrayList<LatLng>();
        points.addAll(x.getPoints());
        day_distance = 0;
        PolylineOptions mLineOptions = new PolylineOptions();
        if (type.equals("TIMELINE"))
            addTimelineMarker(points.get(0));
        else if (type.equals("DISTANCE") || (type.equals("DURATION") && tab == 0))
            addDistanceMarker(points.get(0), day_distance);
        double tmp = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            double dis = CalculationByDistance(points.get(i), points.get(i + 1));

            day_distance += dis;
            if (day_distance - tmp > 15 || i == points.size() - 2) {
                if (type.equals("TIMELINE"))
                    addTimelineMarker(points.get(i + 1));
                else if (type.equals("DISTANCE") || (type.equals("DURATION") && tab == 0))
                    addDistanceMarker(points.get(i + 1), day_distance);
                tmp = day_distance;
            }
        }
    }

    private void addTimelineMarker(LatLng point) {
        Bitmap bmp;
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.timeline_map_dot_5);
        markers.position(point);
        markers.icon(BitmapDescriptorFactory.fromBitmap(bmp));
        map.addMarker(markers);
    }

    //  put custom marker at given LatLng point
    private void addDistanceMarker(LatLng point, double distance) {
        Bitmap tmp;
        if (type.equals("DURATION") && tab == 0)
            tmp = BitmapFactory.decodeResource(getResources(), R.drawable.checkins_summary);
        else
            tmp = BitmapFactory.decodeResource(getResources(), R.drawable.distance_dayview);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(tmp.getWidth(), tmp.getHeight(), conf);
        Canvas canvas1 = new Canvas(bmp);

// stroke width, size
        Paint color = new Paint();
        color.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        int x, y;
        if (distance < 10) {
            color.setTextSize(25 * scale);
            x = 20;
            y = 45;
        } else if (distance < 100) {
            color.setTextSize(25 * scale);
            x = 13;
            y = 45;
        } else if (distance < 1000) {
            color.setTextSize(20 * scale);
            x = 12;
            y = 45;
        } else {
            color.setTextSize(15 * scale);
            x = 12;
            y = 45;
        }
        color.setColor(Color.WHITE);

//modify canvas
        if (type.equals("DURATION") && tab == 0) {
            canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.checkins_summary), 0, 0, color);
        } else {
            canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.distance_dayview), 0, 0, color);
        }

        canvas1.drawText("" + Math.round(distance) + "K", x * scale, y * scale, color);

        markers.position(point);
        markers.icon(BitmapDescriptorFactory.fromBitmap(bmp));
        markers.snippet("snippet");
        markers.title("title");
        map.addMarker(markers);
    }

    private List<MarkerOptions> addPhotoMarker() throws com.parse.ParseException, ExecutionException, InterruptedException {
        int count = 0;
        List<MarkerOptions> markerOptionsList = new ArrayList<>();
        List<Timeline> dayTimeLines = new ArrayList<>();
        skip = 0;
        boolean loop = true;
        do {
            if ((type.equals("DISTANCE") || type.equals("CHECKIN") || type.equals("PHOTO")) && dayOrder == -1) {
                dayTimeLines.addAll(tripOperations.getTripTimeLines(100, skip, Source.WAH, Timeline.ALBUM_CONTENT));
                if (dayTimeLines.size() - skip < 100)
                    loop = false;
                skip = dayTimeLines.size();
            } else {
                dayTimeLines.addAll(tripOperations.getDayTimeLines(day, 100, skip, Source.WAH, Timeline.ALBUM_CONTENT));
                if (dayTimeLines.size() - skip < 100)
                    loop = false;
                skip = dayTimeLines.size();
            }
        } while (loop);

        if (photoTimelines == null) {
            photoTimelines = new ArrayList<>();
        }
        photoTimelines.addAll(dayTimeLines);

        for (Timeline dayTimeLine : dayTimeLines) {
            Album album = (Album) dayTimeLine.getContent();
            ParseGeoPoint loc = album.getLocation();
            if (loc == null)
                continue;

            int albumPublicMediaCount = album.getPublicMediaCount();
            count += albumPublicMediaCount;
            /*List<Media> medias =  tripOperations.getAlbumMedia(album);
            tripOperations.populateMediaSource(medias);
            count += medias.size();
            Media media = medias.get(0);
            Bitmap bitmap;
            try{
                String uri = media.getMediaSource();
                bitmap = BitmapFactory.decodeFile(uri);
                if (bitmap == null) {
                    URL url = new URL(uri);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                }
            }catch (Exception e){continue; }*/

            // set for small marker
            int radius = (int) (100 * scale);
            int stroke = (int) (6 * scale);
            float verticalAnchor = 0.944f;

            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap((int) radius, (int) radius + (int) (25 * scale), conf);
            Canvas canvas = new Canvas(bmp);

            // creates a centered bitmap of the desired size
            /*bitmap = ThumbnailUtils.extractThumbnail(bitmap, (int) radius - stroke, (int) radius - stroke, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);*/

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);

            // the triangle laid under the circle
            int pointedness = (int) (20 * scale);
            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(radius / 2, radius + (int) (15 * scale));
            path.lineTo(radius / 2 + pointedness, radius - (int) (10 * scale));
            path.lineTo(radius / 2 - pointedness, radius - (int) (10 * scale));
            canvas.drawPath(path, paint);

            // gray circle background
            RectF rect = new RectF(0, 0, radius, radius);
            canvas.drawRoundRect(rect, radius / 2, radius / 2, paint);

            // circle photo
            /*paint.setShader(shader);*/
            rect = new RectF(stroke, stroke, radius - stroke, radius - stroke);
            canvas.drawRoundRect(rect, (radius - stroke) / 2, (radius - stroke) / 2, paint);

            //  Writing no of photos on the marker.
            if (albumPublicMediaCount > 0) {
                Paint text = new Paint();
                text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                text.setColor(Color.WHITE);

                int x, y;
                if (albumPublicMediaCount < 10) {
                    text.setTextSize(45 * scale);
                    x = 35;
                    y = 60;
                } else if (albumPublicMediaCount < 100) {
                    text.setTextSize(40 * scale);
                    x = 25;
                    y = 65;
                } else {
                    text.setTextSize(30 * scale);
                    x = 20;
                    y = 65;
                }

                canvas.drawText("" + albumPublicMediaCount, x * scale, y * scale, text);
            }

            // add the marker
            markerOptionsList.add(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude())).icon(BitmapDescriptorFactory.fromBitmap(bmp)).anchor(0.5f, verticalAnchor).snippet(String.valueOf(dayTimeLines.indexOf(dayTimeLine))));

        }
        totalItemCount = count;
        //total_count.setText(""+count);
        return markerOptionsList;
    }

    public List<MarkerOptions> addCheckInMarker() throws com.parse.ParseException {
        int count = 0;
        Bitmap bmp;
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.checkins_dayview);
        List<MarkerOptions> markerOptionsList = new ArrayList<>();
        List<Timeline> dayTimeLines = new ArrayList<>();
        skip = 0;
        boolean loop = true;
        do {
            if ((type.equals("DISTANCE") || type.equals("CHECKIN") || type.equals("PHOTO")) && dayOrder == -1) {
                dayTimeLines.addAll(tripOperations.getTripTimeLines(100, skip, Source.WAH, Timeline.CHECK_IN_CONTENT));
                if (dayTimeLines.size() - skip < 100)
                    loop = false;
                skip = dayTimeLines.size();
            } else {
                dayTimeLines.addAll(tripOperations.getDayTimeLines(day, 100, skip, Source.WAH, Timeline.CHECK_IN_CONTENT));
                if (dayTimeLines.size() - skip < 100)
                    loop = false;
                skip = dayTimeLines.size();
            }
        } while (loop);

        for (Timeline dayTimeLine : dayTimeLines) {
            CheckIn checkin = (CheckIn) dayTimeLine.getContent();
            ParseGeoPoint loc = checkin.getLocation();
            MarkerOptions markers = new MarkerOptions();
            if (loc != null) {
                markers.position(new LatLng(loc.getLatitude(), loc.getLongitude()));
                markers.icon(BitmapDescriptorFactory.fromBitmap(bmp));
                if (dayTimeLine.getContent().get("name") != null)
                    markers.title((String) dayTimeLine.getContent().get("name"));
                if (dayTimeLine.getContentTime() != null) {
                    Date date = dayTimeLine.getContentTime();
                    String string;
                    if (date.getMinutes() < 10)
                        string = "" + date.getHours() + ":0" + date.getMinutes();
                    else
                        string = "" + date.getHours() + ":" + date.getMinutes();
                    markers.snippet(string);
                }
                //map.addMarker(markers);
                markerOptionsList.add(markers);
                count += 1;
            }
        }
        totalItemCount = count;
        return markerOptionsList;
    }

    private void addDurationMarker(LatLng p, String city, int count) {
        Bitmap tmp;
        tmp = BitmapFactory.decodeResource(getResources(), R.drawable.duration_pin);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(tmp.getWidth(), tmp.getHeight(), conf);
        Canvas canvas1 = new Canvas(bmp);

//  stroke width, size
        Paint color = new Paint();
        color.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        int x, y;
        color.setTextSize(25 * scale);
        x = 20;
        y = 45;
        color.setColor(Color.WHITE);
        //TODO change checkins_summary to duration_summary icon (Yellow marker)
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.duration_pin), 0, 0, color);

        canvas1.drawText("" + Math.round(count) + "D", x * scale, y * scale, color);

        markers.position(p);
        markers.icon(BitmapDescriptorFactory.fromBitmap(bmp));
        markers.title(city);
        map.addMarker(markers);
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
        double c = 2 * Math.asin(Math.sqrt(a));
//        double valueResult= Radius*c;
//        double km=valueResult/1;
//        DecimalFormat newFormat = new DecimalFormat("####");
//        int kmInDec =  Integer.valueOf(newFormat.format(km));
//        double meter=valueResult%1000;
//        int  meterInDec= Integer.valueOf(newFormat.format(meter));
//        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec + " Meter   " + meterInDec);

        return Radius * c;
    }

    public double getSpeed(LatLng start, LatLng end, Date startTime, Date endTime) throws ParseException {
        /**
         * vijay: this will break (return NaN) if startTime = endTime
         */
        //int i = latlng.indexOf(start);
        //int j = latlng.indexOf(end);
        double speed = 0;
        //Date datei = test.get(i).getRecordedTime();
        //Date datej = test.get(j).getRecordedTime();
        double distance = CalculationByDistance(start, end);
        double duration = (double) (endTime.getTime() - startTime.getTime()) / 1000 / 60 / 60;
        speed = distance / duration;
        return speed;
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

        @Override
        public String toString() {
            return "start: " + this.start + " end: " + this.end;
        }
    }

    public void drawFlightPath() {
        for (int i = 0; i < flights.size(); i++) {
            LatLng origin = givenPoints.get(flights.get(i).getStart());
            LatLng dest = givenPoints.get(flights.get(i).getEnd());
            if (CalculationByDistance(origin, dest) < 200.0d) {
                PolylineOptions options = new PolylineOptions().width(6 * scale).color(color).geodesic(true);
                options.add(origin);
                options.add(dest);
                map.addPolyline(options);
            } else {
                createDashedLine(map, origin, dest, color);
                MarkerOptions marker = new MarkerOptions();
                Bitmap bmp;
                float angle = getPolylineOptionAngle(origin, dest);
                LatLng mid = new LatLng(((origin.latitude) * 11 + (dest.latitude) * 9) / 20, ((origin.longitude) * 11 + (dest.longitude) * 9) / 20);
                if (type.equals("DISTANCE"))
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.intercitytravel_distance);
                else if (type.equals("PHOTO"))
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.intercitytravel_photosvideos);
                else
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.intercitytravel_checkins);
                marker.icon(BitmapDescriptorFactory.fromBitmap(bmp));
                marker.position(mid);
                marker.rotation(angle);
                marker.anchor(0.5f, 0.5f);
                marker.flat(true);
                map.addMarker(marker);
            }
        }
    }

    public float getPolylineOptionAngle(LatLng start, LatLng end) {
        float angle;
        double x1, x2, y1, y2;
        x1 = start.latitude;
        x2 = end.latitude;
        y1 = start.longitude;
        y2 = end.longitude;

        angle = (float) Math.toDegrees(Math.atan2((y2 - y1), (x2 - x1)));
        return angle;
    }

    public boolean isConnectingToInternet() {
        Context context = getActivity().getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
            }
        }
        return false;
    }

    public void createDashedLine(GoogleMap map, LatLng latLngOrig, LatLng latLngDest, int color) {
        double difLat = latLngDest.latitude - latLngOrig.latitude;
        double difLng = latLngDest.longitude - latLngOrig.longitude;

        double zoom = map.getCameraPosition().zoom;

        double divLat = difLat / (zoom * 20);
        double divLng = difLng / (zoom * 20);

        LatLng tmpLatOri = latLngOrig;

        for (int i = 0; i < (int)(zoom * 20); i++) {
            LatLng loopLatLng = tmpLatOri;
            LatLng to = new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng);

            if (i > 0) {
                loopLatLng = new LatLng(tmpLatOri.latitude + (divLat * 0.25f), tmpLatOri.longitude + (divLng * 0.25f));
            }
            if(i==(int)(zoom * 20)-1) {
                to = new LatLng(latLngDest.latitude, latLngDest.longitude);
            }
            Polyline polyline = map.addPolyline(new PolylineOptions()
                    .add(loopLatLng)
                    .add(to)
                    .color(color)
                    .width(6 * scale));

            tmpLatOri = new LatLng(tmpLatOri.latitude + divLat, tmpLatOri.longitude + divLng);
        }

    }

    public void durationMap() {
        List<Day> days = trip.getDays();
        PolylineOptions options = new PolylineOptions().width(6 * scale).color(color).geodesic(true);
        ArrayList<LatLng> route = new ArrayList<>();
        ArrayList<Integer> count = new ArrayList<>();
        ArrayList<String> cities = new ArrayList<>();
        int index = 0;
        for (int d = 0; d < days.size(); d++) {
            if (days.get(d).getLocation() == null)
                continue;
            Boolean entry = true;
            String city = "";
            LatLng p = new LatLng(days.get(d).getLocation().getLatitude(), days.get(d).getLocation().getLongitude());
            if (days.get(d).getCity() != null)
                city = days.get(d).getCity();
            for (LatLng l : route) {
                if (CalculationByDistance(p, l) < 50 && route.size() > 0) {
                    count.add(route.indexOf(l), (count.get(route.indexOf(l)) + 1));
                    entry = false;
                }
            }
            if (entry) {
                route.add(index, p);
                count.add(index, 1);
                cities.add(index, city);
                index++;
            }
        }
        if (route.size() == 0)
            return;

        total_count.setText("" + days.size());
        for (int d = 0; d < index; d++) {
            addDurationMarker(route.get(d), cities.get(d), count.get(d));
        }

        options.addAll(route);
        map.addPolyline(options);

        if (route.size() != 0) {
            for (LatLng l : route) {
                builder.include(l);
            }

            if (index == 1) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(route.get(0))
                        .zoom(10)
                        .bearing(0)
                        .tilt(0)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else {
                LatLngBounds bounds = builder.build();
                int padding = 10; // offset from edges of the map in pixels
                int height = (int) (600 * scale);
                int width = (int) (400 * scale);
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, height, width, padding);
                map.animateCamera(cu);
            }
        }
    }

    private OnFragmentInteractionListener mListener;

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

    public interface OnFragmentInteractionListener {
        TripOperations getTripOperations();
    }
}
