package com.fireapps.firedepartmentmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import br.com.mauker.materialsearchview.MaterialSearchView;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

import static android.content.Context.MODE_PRIVATE;

public class MapsActivity2 extends SupportMapFragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {


    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static ArrayList<Marker> markerArray;
    ArrayList<Marker> searchMarkers = new ArrayList<>();
    private Pubnub mPubnub;
    private static GoogleApiClient mGoogleApiClient;

    static SharedPreferences sharedPreferences;
    private static Location mLocation;
    private FirebaseDatabase database;
    private static SharedPreferences sharedPref;
    public static Context context;

    public static MapsActivity2 newInstance() {
        MapsActivity2 dialogFragment = new MapsActivity2();
        return dialogFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();

        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mPubnub = new Pubnub("pub-c-31e7d284-39d2-4aff-b682-d709c4aa105e", "sub-c-8c10dcfe-3732-11e5-87df-02ee2ddab7fe");
        /*try {
            mPubnub.subscribe("test", subscribeCallback);
        } catch (PubnubException e) {
            Log.e("Fire", e.toString());
        }*/

        context = getContext();

        markerArray = new ArrayList<>();

        setHasOptionsMenu(true);

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

               /* buildGoogleApiClient();
                mGoogleApiClient.connect();

                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setCompassEnabled(true);*/

                mMap.setMyLocationEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                if (mLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                }
                //updateCallLocation();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.map_layers) {
            new MaterialDialog.Builder(getActivity())
                    .title("Set Map Layer")
                    .items(R.array.map_layers)
                    .itemsCallbackSingleChoice(mMap.getMapType(), new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            /**
                             * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                             * returning false here won't allow the newly selected radio button to actually be selected.
                             **/
                            switch (which) {
                                case 1:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    break;
                                case 4:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                    break;
                                case 2:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                    break;
                                case 3:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                    break;
                                case 0:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                                    break;
                                default:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                    break;
                            }
                            return true;
                        }
                    })
                    .show();
        }
        if (id == R.id.map_search) {


            new MaterialDialog.Builder(getActivity())
                    .title("Search Map")
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS)
                    .input("Address", "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            // Do something
                            LatLng latLng = getLocationFromAddress(getContext(), input.toString());
                            if (latLng != null) {
                                try {
                                    for (Marker marker : searchMarkers) {
                                        marker.remove();
                                    }
                                } catch (Exception e) {

                                } finally {
                                    Marker resultMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(input.toString()));
                                    searchMarkers.add(resultMarker);
                                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                    resultMarker.showInfoWindow();
                                }
                            } else {
                                Snackbar.make(getView(), "Location not found.", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onPause() {
        super.onPause();
        //mGoogleApiClient.disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        //mGoogleApiClient.disconnect();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("FDM-Maps-onConnected", "CONNECTED");
        LocationRequest mLocationRequest = createLocationRequest();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        /*if (sharedPreferences.getString("respondingTo", "none").equalsIgnoreCase("none") || sharedPreferences.getString("respondingTo", "none").equalsIgnoreCase("nr")) {
            Log.d("FDM-Maps-onConnected", "Not Responding. Removing Updates.");
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d("FDM-Maps-onConnected", "Responding. Requesting Updates and Pushing.");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }*/

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("FDM-Maps-Suspended", "SUSPENDED");
    }

    private static LocationRequest createLocationRequest() {
        Log.d("FDM-Maps-createRequest", "CREATED");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        switch (sharedPref.getString(SettingsActivityFragment.KEY_PREF_RESPONSE_ACCURACY, "PRIORITY_BALANCED_POWER_ACCURACY")) {
            case "PRIORITY_NO_POWER":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
                break;
            case "PRIORITY_LOW_POWER":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            case "PRIORITY_BALANCED_POWER_ACCURACY":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case "PRIORITY_HIGH_ACCURACY":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
            default:
                break;
        }
        return mLocationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {
        //broadcastLocation(location);

        Log.d("FDM Location Map", "Location Updated");

        mLocation = location;

        DatabaseReference userREf = database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/lat", location.getLatitude());
        childUpdates.put("/lon", location.getLongitude());
        childUpdates.put("cityName", getCityName(location));

        userREf.updateChildren(childUpdates);

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
        mPubnub.publish("test", message, publishCallback);
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
        //initializeMap();
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //updatePolyline();
                    //updateCamera();
                    //updateMarker();
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
    public void onResume() {
        super.onResume();
    }

    private void updateCallLocation() {
        /*Marker locationMarker = mMap.addMarker(new MarkerOptions().position(getLocationFromAddress(getContext(), "31 Bailey Lane, Warren County, PA")).title("STRUCTURE FIRE").snippet("31 Bailey Lane").icon(BitmapDescriptorFactory.fromResource(R.drawable.fire_icon)));
        Marker locationMarker2 = mMap.addMarker(new MarkerOptions().position(getLocationFromAddress(getContext(), "311 Main St 16340")).title("EMS CHEST PAIN").snippet("311 Main St").icon(BitmapDescriptorFactory.fromResource(R.drawable.ems_icon)));
        if (!locationMarker.isInfoWindowShown()) {
            locationMarker.showInfoWindow();
            locationMarker2.showInfoWindow();
        }*/
    }

    public static void updateMemberLocations(MemberObjectFire memberObjectFire) {
        Log.d("UpdateMarker", Double.toString(memberObjectFire.getLat()));

        if (markerArray.isEmpty()) {
            if (memberObjectFire.getisResponding()) {
                Marker member = mMap.addMarker(new MarkerOptions().position(new LatLng(memberObjectFire.getLat(), memberObjectFire.getLon())).title(memberObjectFire.getName()).snippet(memberObjectFire.getRespondingTo() + " | " + getDate(memberObjectFire.getRespondingTime())));
                if (!member.isInfoWindowShown()) {
                    member.showInfoWindow();
                }
                markerArray.add(member);
            }
        }

        try {
            for (Marker marker : markerArray) {
                if (marker.getTitle().equalsIgnoreCase(memberObjectFire.getName())) {
                    if (memberObjectFire.getisResponding()) {
                        marker.setPosition(new LatLng(memberObjectFire.getLat(), memberObjectFire.getLon()));
                        marker.setTitle(memberObjectFire.getName());
                        marker.setSnippet(memberObjectFire.getRespondingTo() + " | " + getDate(memberObjectFire.getRespondingTime()));
                    } else {
                        marker.remove();
                    }

                    /*Marker member = mMap.addMarker(new MarkerOptions().position(new LatLng(memberObjectFire.getLat(), memberObjectFire.getLon())).title(memberObjectFire.getName()).snippet(memberObjectFire.getRespondingTo() + " | " + getDate(memberObjectFire.getRespondingTime())));
                    if (!member.isInfoWindowShown()) {
                        member.showInfoWindow();
                    }
                    markerArray.add(member);*/
                } else {
                    if (memberObjectFire.getisResponding()) {
                        Marker member = mMap.addMarker(new MarkerOptions().position(new LatLng(memberObjectFire.getLat(), memberObjectFire.getLon())).title(memberObjectFire.getName()).snippet(memberObjectFire.getRespondingTo() + " | " + getDate(memberObjectFire.getRespondingTime())));
                        if (!member.isInfoWindowShown()) {
                            member.showInfoWindow();
                        }
                        markerArray.add(member);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDate(String string) {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            Date date;

            date = dateFormat.parse(string);

            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);

            //customViewHolder.timeER.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

            return (String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LatLng getLocationFromAddress(Context context, String address) {

        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        LatLng p1 = null;

        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses == null) {
                return null;
            }
            Address address1 = addresses.get(0);
            address1.getLatitude();
            address1.getLongitude();

            p1 = new LatLng(address1.getLatitude(), address1.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p1;
    }

    public interface RunnableListener {
        void onResult(String result);
    }

    private RunnableListener runnableListener;

    public String getCityName(final Location mLocation) {

        final String[] cityname = new String[1];

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                //latTextView.setText(String.valueOf(location.getLatitude()));
                //longTextView.setText(String.valueOf(location.getLongitude()));
                Log.d("FDM Location", "Location Found");
                List<Address> addresses;

                try {
                    addresses = gcd.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                    if (addresses.size() > 0)

                    {
                        //while(locTextView.getText().toString()=="Location") {
                        cityname[0] = addresses.get(0).getLocality().toString();

                        Log.d("FDM Location", cityname[0]);



                        // }
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        });

        return cityname[0];
    }



    public void responding() {
        Log.d("FDM-Maps-userResponded", "Update");



        /*if (mGoogleApiClient.isConnected()) {
            LocationRequest mLocationRequest = createLocationRequest();
            if (sharedPreferences.getString("respondingTo", "none").equalsIgnoreCase("none") || sharedPreferences.getString("respondingTo", "none").equalsIgnoreCase("nr")) {
                try {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        } else {
            mGoogleApiClient.connect();

            mGoogleApiClient.isConnectionCallbacksRegistered(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    LocationRequest mLocationRequest = createLocationRequest();
                    if (sharedPreferences.getString("respondingTo", "none").equalsIgnoreCase("none") || sharedPreferences.getString("respondingTo", "none").equalsIgnoreCase("nr")) {
                        try {
                            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity2.this);
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        }*/
    }

    public void userResponded() {
        responding();
    }
}
