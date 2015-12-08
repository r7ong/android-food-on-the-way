package com.calvinlsliang.foodontheway;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
    {
        geocoded_waypoints:[]
        routes:[
        {
            overview_polyline:{
                points: "eir***"
            }
        },
        {}
        ]
    }
 */

/**
 * Created by seven on 12/5/15.
 */
public class Map {
    public String polylinePoints;
    private String duration;
    private int durationSec;
    private int totalDurationnSec;

    public int getDurationSec() {
        return durationSec;
    }

    public int getTotalDurationnSec() {
        return totalDurationnSec;
    }

    public String getPolylinePoints() {
        return polylinePoints;
    }

    public static Map fromJSON(JSONObject jsonObject){
        Map map = new Map();
        //extract the values from the json, store them
        try {
            JSONArray routesArray = jsonObject.getJSONArray("routes");
            map.polylinePoints = routesArray.getJSONObject(0).getJSONObject("overview_polyline").getString("points");
            JSONArray legs = routesArray.getJSONObject(0).getJSONArray("legs");
            if(legs.length() == 1){
                map.duration = legs.getJSONObject(0).getJSONObject("duration").getString("text");
                map.durationSec = legs.getJSONObject(0).getJSONObject("duration").getInt("value");
            } else if(legs.length() == 2){
                map.totalDurationnSec = legs.getJSONObject(0).getJSONObject("duration").getInt("value") +
                        legs.getJSONObject(1).getJSONObject("duration").getInt("value");

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        //return the tweet obj
        return map;
    }

    public String getDuration() {
        return duration;
    }
}