package com.calvinlsliang.foodontheway;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seven on 12/7/15.
 */
public class Place {
    String placeId;
    String name;
    LatLng latLng;
    String extraTime;
    Boolean openNow;

    public String openStatus(){
        if(openNow){
            return "OPEN NOW";
        }else{
            return "CLOSED";
        }
    }

    public void setExtraTime(String extraTime) {
        this.extraTime = extraTime;
    }

    public String getExtraTime() {

        return extraTime;
    }

    public String getName() {
        return name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public static Place fromJSON(JSONObject jsonObject){
        Place place = new Place();
        //extract the values from the json, store them
        try {
            place.placeId = jsonObject.getString("place_id");
            place.name = jsonObject.getString("name");
            JSONObject location = jsonObject.getJSONObject("geometry").getJSONObject("location");
            place.latLng = new LatLng(Double.valueOf(location.getString("lat")), Double.valueOf(location.getString("lng")));
            JSONObject hours = jsonObject.getJSONObject("opening_hours");
            if(hours != null){
                place.openNow = hours.getBoolean("open_now");
            }else{
                place.openNow = false;
            }

            Log.d("in-- place", place.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //return the tweet obj
        return place;
    }

    // tweet.fromJSONArray() => List<Tweet>
    public static ArrayList<Place> fromJSONObj(JSONObject jsonObject) {
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Place> places = new ArrayList<>();
        //iterate the json array and create tweets
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
                Place  tweet = Place.fromJSON(tweetJson);
                if(tweet != null){
                    places.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

        }
        // return finished list
        return places;
    }
}