package com.weareholidays.bia.activities.journal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RoadJSONParser {

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
        JSONArray jRoutes = null;
        List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

        try {

            jRoutes = jObject.getJSONArray("snappedPoints");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){

                        /** Traversing all points */
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", ((JSONObject)jRoutes.getJSONObject(i).get("location")).getString("latitude"));
                            hm.put("lng", ((JSONObject)jRoutes.getJSONObject(i).get("location")).getString("longitude"));
                            path.add(hm);

            }
            routes.add(path);
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return routes;
    }
    }