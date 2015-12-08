package com.calvinlsliang.foodontheway;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;


import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import com.google.maps.android.ui.IconGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private LatLng myLatLng;
    private String wayPoints;
    ArrayList<Place> places;
    private int durationSec;
    HashMap<String, Marker> markerMap;
    HashMap<String, Place> placeMap;

//    String origin = "Sunnyvale,CA";
//    String destination = "Palo Alto,CA";
//    String radius = "500";
//    String foodType = "burger";
    private String origin = null;
    private String destination = null;
    private String foodType = null;
    private String radius = null;



    AsyncHttpClient client;

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
        client = new AsyncHttpClient();
        places = new ArrayList<>();

        markerMap = new HashMap<String, Marker>();
        placeMap = new HashMap<String, Place>();

        origin = getIntent().getStringExtra("origin");
        destination = getIntent().getStringExtra("destination");
        foodType = getIntent().getStringExtra("food");
        radius = getIntent().getStringExtra("radius");

    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setMyLocationEnabled(true);

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

                @Override
                public boolean onMarkerClick(Marker marker) {
                    Toast.makeText(MapActivity.this, getLatLngString(marker.getPosition()), Toast.LENGTH_SHORT).show();
                    getPlaceDetail(origin, destination, marker.getPosition());
                    return true;
                }
            });

            // Now that map has loaded, let's get our location!
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            connectClient();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Called when the Activity becomes visible.
    */
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
	 * Called when the Activity is no longer visible.
	 */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                }

        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            Log.d("debug location=", location.toString());
            Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            myLatLng = latLng;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
            if(origin == null || origin.length() == 0){
                origin = getLatLngString(latLng);
            }

            getDirection(origin, destination);



        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
    }

    public String getLatLngString(LatLng latLng){
        return Double.toString(latLng.latitude)+","+Double.toString(latLng.longitude);
    }

    public void getRestaurants(LatLng latLng) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        // specify the params
        RequestParams params = new RequestParams();
        params.put("location", getLatLngString(latLng));
        params.put("radius", radius);
        params.put("types","food");
        params.put("name",foodType);
        params.put("key","AIzaSyDtkF1VK5-Aj08-VcBb99b7DcH-jCJfnGE");
        // execute the request

        client.addHeader("Accept-Encoding", "identity");
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<Place> newPlaces = Place.fromJSONObj(response);
                places.addAll(newPlaces);
                addRest(newPlaces);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });
    }

    public void addRest(ArrayList<Place> places){
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        for(int i=0;i<places.size();i++) {
            LatLng latLng = places.get(i).getLatLng();
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(defaultMarker));
            placeMap.put(getLatLngString(latLng),places.get(i));

            IconGenerator iconFactory = new IconGenerator(MapActivity.this);
            iconFactory.setStyle(IconGenerator.STYLE_BLUE);
            addIcon(iconFactory, places.get(i).getName(), latLng);
        }
    }

    public void getPlaceDetail(String origin, String destination, final LatLng position) {
        String url = "http://maps.googleapis.com/maps/api/directions/json";
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED);

        // specify the params
        RequestParams params = new RequestParams();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("sensor", false);
        params.put("waypoints", getLatLngString(position));

        // execute the request

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Map mapData = Map.fromJSON(response);
                int delayTime = (mapData.getTotalDurationnSec() - durationSec)/60; //in min

                //get place marker

                Marker maker = markerMap.get(getLatLngString(position));
                Place place = placeMap.get(getLatLngString(position));
                maker.setTitle("+" + Integer.toString(delayTime) + " min");
                maker.setSnippet(place.openStatus());
                maker.showInfoWindow();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });
    }

    public void getDirection(String origin, String destination) {
        String url = "http://maps.googleapis.com/maps/api/directions/json";
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED);

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
                Map mapData = Map.fromJSON(response);
                String encodedPoints = mapData.getPolylinePoints();
                String duration = mapData.getDuration();
                durationSec = mapData.getDurationSec();
                List<LatLng> latLngs = PolyUtil.decode(encodedPoints);
                Log.d("in-- latLngs =", latLngs.toString());

                //add dest marker
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(latLngs.get(latLngs.size() - 1))
//                        .title(title)
//                        .snippet(snippet)
                        .icon(defaultMarker));
                IconGenerator iconFactory = new IconGenerator(MapActivity.this);
                iconFactory.setStyle(IconGenerator.STYLE_GREEN);
                addIcon(iconFactory, duration, latLngs.get(latLngs.size() / 2));

                //adding polyline
                addPolylineToMap(latLngs);
                fixZoomForLatLngs(map, latLngs);

                //TODO add wayPoints on map and label with time of delay
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }
        });
    }

    private void addIcon(IconGenerator iconFactory, String text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        Marker marker = map.addMarker(markerOptions);
        markerMap.put(getLatLngString(position), marker);
    }
    public  void fixZoomForLatLngs(GoogleMap googleMap, List<LatLng> latLngs) {
        if (latLngs!=null && latLngs.size() > 0) {
            LatLngBounds.Builder bc = new LatLngBounds.Builder();

            for (LatLng latLng: latLngs) {
                bc.include(latLng);
            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50),4000, null);
        }
    }
    public void addPolylineToMap(List<LatLng> latLngs) {
        PolylineOptions options = new PolylineOptions();
        for (LatLng latLng : latLngs) {
            options.add(latLng);
        }
        int latLngSize = latLngs.size();
        Log.d("DEBUG latLngSize=", Integer.toString(latLngSize));
        int step = latLngSize/20;
        for(int i=0;i<latLngs.size();i+=step){
            getRestaurants(latLngs.get(i));
        }

        map.addPolyline(options);
    }

//    protected void startLocationUpdates() {
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
//        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
//                mLocationRequest, this);
//    }

    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

}