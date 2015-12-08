package com.calvinlsliang.foodontheway;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
    private List<String> wayPoints;

    private String origin = null;
    private String destination = null;
    private String radius = null;

//    private MapClient client;

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
//        client = new MapClient();
        client = new AsyncHttpClient();

        origin = getIntent().getStringExtra("origin");
        destination = getIntent().getStringExtra("destination");
        radius = getIntent().getStringExtra("radius");

    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setMyLocationEnabled(true);

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
//            startLocationUpdates();
            //TODO get origin, dest from selection intent

            Log.d("in-- origin/dest", origin);
            Log.d("in-- origin/dest", destination);

            // TODO remove overrides
            String origin = "Sunnyvale,CA";
            String destination = "Palo Alto,CA";
            getDirection(origin, destination);
            getRestaurants();
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
    }

    public void getRestaurants() {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

//        url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=cruise&key=AIzaSyDtkF1VK5-Aj08-VcBb99b7DcH-jCJfnGE";
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        // specify the params
        RequestParams params = new RequestParams();
        params.put("location", "-33.8670522,151.1957362");
        params.put("radius", radius);
        params.put("types","food");
        params.put("name","cruise");
        params.put("key","AIzaSyDC8ncI8wg-sQ--1cBeIzSFOQ6j1LlQOZU");
        // execute the request

//        AsyncHttpClient client = new AsyncHttpClient();


        Log.d("in-- MyApp params", params.toString());

        client.addHeader("Accept-Encoding", "identity");

        client.get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                try {
//                    Log.d("in-- getRestaurants", new String(responseBody, "UTF-8"));
//                } catch (UnsupportedEncodingException e) {
//                    Log.d("in-- rest e", e.toString());
//
//                    e.printStackTrace();
//                }
                //TODO assign wayPoints from response

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.e("in-- MyApp", "Caught error", error);
                Log.e("in-- MyApp failure", headers.toString());
                try {
                    Log.e("in-- MyApp failure", new String(responseBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getDirection(String origin, String destination) {
        String url = "http://maps.googleapis.com/maps/api/directions/json";
        final BitmapDescriptor defaultMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED);

//        AsyncHttpClient client = new AsyncHttpClient();

        // specify the params
        RequestParams params = new RequestParams();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("sensor",false);
        // execute the request

        //TODO add wayPoints to request
        // https://developers.google.com/maps/documentation/directions/intro#Waypoints

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("in-- DEBUG", response.toString());
                // Root JSON in response is an dictionary i.e { "data : [ ... ] }
                // Handle resulting parsed JSON response here
                Map mapData = Map.fromJSON(response);
                String encodedPoints = mapData.getPolylinePoints();
                String duration = mapData.getDuration();
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
                Log.d("in-- DEBUG = statusCode", Integer.toString(statusCode));
            }
        });
    }

    private void addIcon(IconGenerator iconFactory, String text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        map.addMarker(markerOptions);
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
