package com.calvinlsliang.foodontheway;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 * Created by seven on 12/5/15.
 */
public class MapClient {

    public void getSome(){
        Log.d("in-- getsome","some");
        String origin="Chicago,IL";
        String destination="Seattle,WA";

        String url = "http://maps.googleapis.com/maps/api/directions/json";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("sensor",false);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("in-- DEBUG", response.toString());
                // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                // Handle resulting parsed JSON response here
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("in-- DEBUG = statusCode", Integer.toString(statusCode) );
            }
        });
    }
/*
    public void getHomeTimeLine(AsyncHttpResponseHandler handler, long page) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        // specify the params
        RequestParams params = new RequestParams();
        params.put("count", PAGE_SIZE);
//		params.put("since_id", 1);
//		Log.d("in-- page", Long.toString(page));
        if (page != Long.MAX_VALUE) {
            params.put("max_id", page-1);
        }

//		params.put("max_id", page * PAGE_SIZE + 1);
        // execute the request
        getClient().get(apiUrl, params, handler);

    }
  */
    public void getDirection(AsyncHttpResponseHandler handler) {
        String origin="Chicago,IL";
        String destination="Seattle,WA";
        String url = "http://maps.googleapis.com/maps/api/directions/json";
        AsyncHttpClient client = new AsyncHttpClient();

        // specify the params
        RequestParams params = new RequestParams();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("sensor",false);
        // execute the request

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("in-- DEBUG", response.toString());
                // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                // Handle resulting parsed JSON response here
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("in-- DEBUG = statusCode", Integer.toString(statusCode));
            }
        });
    }
}
