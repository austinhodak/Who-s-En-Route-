package com.fireapps.firedepartmentmanager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Pubnub mPubnub;
    private GoogleApiClient mGoogleApiClient;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        mPubnub = new Pubnub("pub-c-31e7d284-39d2-4aff-b682-d709c4aa105e", "sub-c-8c10dcfe-3732-11e5-87df-02ee2ddab7fe");
        try {
            mPubnub.subscribe("gvfd65", subscribeCallback);
        } catch (PubnubException e) {
            Log.e("Fire", e.toString());
        }

        this.buildGoogleApiClient();
        mGoogleApiClient.connect();



    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {
        broadcastLocation(location);
    }

    private void broadcastLocation(Location location) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", location.getLatitude());
            message.put("lng", location.getLongitude());
            message.put("alt", location.getAltitude());
            message.put("name", FirebaseAuth.getInstance().getCurrentUser().getUid());
            message.put("respondingTo",sharedPreferences.getString("responding", ""));
        } catch (JSONException e) {
            Log.e("", e.toString());
        }
        mPubnub.publish("gvfd65", message, publishCallback);
    }

    Callback publishCallback = new Callback() {
        @Override
        public void successCallback(String channel, Object response) {
            Log.d("PUBNUB", response.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.e("PUBNUB", error.toString());
        }
    };

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        initializeMap();
    }

    private PolylineOptions mPolylineOptions;

    private void initializeMap() {
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.RED).width(10);
    }

    private LatLng mLatLng;
    private String name;

    private String respondingTo;
    private String callAddress;
    private String callType;
    Callback subscribeCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object message) {
            JSONObject jsonMessage = (JSONObject) message;
            try {
                double mLat = jsonMessage.getDouble("lat");
                double mLng = jsonMessage.getDouble("lng");
                mLatLng = new LatLng(mLat, mLng);
                name = jsonMessage.getString("name");
                respondingTo = jsonMessage.getString("respondingTo");
                callAddress = jsonMessage.getString("callLocation");
                callType = jsonMessage.getString("callType");
            } catch (JSONException e) {
                Log.e("TAG", e.toString());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePolyline();
                    updateCamera();
                    updateMarker();
                }
            });
        }
    };

    private void updatePolyline() {
        mMap.clear();
        mMap.addPolyline(mPolylineOptions.add(mLatLng));
    }

    private void updateMarker() {
        Marker locationMarker = mMap.addMarker(new MarkerOptions().position(mLatLng).title(name + " - " + respondingTo));
        if (!locationMarker.isInfoWindowShown()) {
            locationMarker.showInfoWindow();
        }
    }

    private void updateCamera() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 14));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateCallLocation() {
        Marker locationMarker = mMap.addMarker(new MarkerOptions().position(mLatLng).title(name + " - " + respondingTo));
        if (!locationMarker.isInfoWindowShown()) {
            locationMarker.showInfoWindow();
        }
    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
}
