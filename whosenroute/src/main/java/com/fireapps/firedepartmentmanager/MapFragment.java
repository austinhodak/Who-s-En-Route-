package com.fireapps.firedepartmentmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by austinhodak on 6/19/16.
 */

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    AddressResultReceiver addressResultReceiver;

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;

    public static final int USE_ADDRESS_NAME = 1;
    public static final int USE_ADDRESS_LOCATION = 2;

    public static final String PACKAGE_NAME =
            "com.fireapps.firedepartmentmanager";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String RESULT_ADDRESS = PACKAGE_NAME + ".RESULT_ADDRESS";
    public static final String LOCATION_LATITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LATITUDE_DATA_EXTRA";
    public static final String LOCATION_LONGITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LONGITUDE_DATA_EXTRA";
    public static final String LOCATION_NAME_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_NAME_DATA_EXTRA";
    public static final String FETCH_TYPE_EXTRA = PACKAGE_NAME + ".FETCH_TYPE_EXTRA";

    public List<Marker> searchMarkers;
    public List<Marker> memberMarkers;

    Marker currentLocationMarker;

    GoogleMap mMap = null;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mRequestinLocationUpdates = true;
    private SharedPreferences sharedPreferences;
    private String departmentID;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userReference;

    private HashMap<String, Marker> memberHashMap = new HashMap<>();
    private ArrayList<DataSnapshot> members = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        getActivity().setTitle("Map");
        setHasOptionsMenu(true);
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_fragment, supportMapFragment).commit();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        departmentID = sharedPreferences.getString("departmentID", null);

        searchMarkers = new ArrayList<>();
        memberMarkers = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        loadRespondingMembers(departmentID);



        addressResultReceiver = new AddressResultReceiver(null);
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }



        setUpGoogleApi();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    setUpMap();
                }
            }
        });
        ((NavDrawerActivity) getActivity()).getSupportActionBar().setElevation(4);
        ((NavDrawerActivity) getActivity()).searchView.setOnQueryTextListener(new com.miguelcatalan.materialsearchview.MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getActivity(), GeocodeAddressIntentService.class);
                intent.putExtra(RECEIVER, addressResultReceiver);
                intent.putExtra(FETCH_TYPE_EXTRA, USE_ADDRESS_NAME);
                intent.putExtra(LOCATION_NAME_DATA_EXTRA, query);
                getActivity().startService(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        return view;
    }

    private void setUpGoogleApi() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    setUpMap();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setUpMap() {
        if (mMap == null) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    private void addIncidentMarker(Address address) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title("Search Result").snippet(address.getAddressLine(0))).showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(address.getLatitude(), address.getLongitude()), 13));
    }

    private void addSearchMarker(Address address) {
        try {
            for (Marker marker : searchMarkers) {
                marker.remove();
            }
        } catch (Exception e) {

        }

        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title("Search Result").snippet(address.getAddressLine(0)));
        marker.showInfoWindow();
        searchMarkers.add(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(address.getLatitude(), address.getLongitude()), 13));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastLocation();
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mRequestinLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(10f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void getLastLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        /*if (currentLocationMarker != null) {
            currentLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), mMap.getCameraPosition().zoom));
            updateMemberMarkers();
        }*/
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(RESULT_ADDRESS);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addSearchMarker(address);
                    }
                });

            }
            else {
                Toast.makeText(getActivity(), "Address Not Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_map_search);
        ((NavDrawerActivity) getActivity()).searchView.setMenuItem(item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_map_layers) {
            new MaterialDialog.Builder(getActivity())
                    .title("Set Map Layer")
                    .items(R.array.map_layers)
                    .titleColorRes(R.color.text_responding_name)
                    .contentColorRes(R.color.text_responding_name)
                    .backgroundColorRes(R.color.material_drawer_background)
                    .itemsCallbackSingleChoice(mMap.getMapType(), new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
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
        return super.onOptionsItemSelected(item);
    }

    public void updateMemberMarkers() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Map.Entry<String, Marker> member : memberHashMap.entrySet()) {
            Marker marker = member.getValue();
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        try {
            int padding = 250;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        } catch (Exception e) {
            e.printStackTrace();
            int padding = 50;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
        if (mMap.getCameraPosition().zoom > 18) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    public Marker addMemberMarker(DataSnapshot member) {
        Member member1 = member.getValue(Member.class);
        IconGenerator iconGen = new IconGenerator(getActivity());
        iconGen.setColor(Color.parseColor("#" + member1.getPositionColor()));
        //iconGen.setRotation(90);
        //iconGen.setContentRotation(-90);
        iconGen.setTextAppearance(R.style.MapMarkerDark);
        Bitmap icon = iconGen.makeIcon(member1.getName());
        Marker memberMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(member.child("location/currentLat").getValue().toString()), Double.valueOf(member.child("location/currentLon").getValue().toString())))
                .icon(BitmapDescriptorFactory.fromBitmap(icon)));

        return memberMarker;
    }

    private void loadRespondingMembers(String department) {
        DatabaseReference departmentRef = firebaseDatabase.getReference("departments").child(department).child("members");
        departmentRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                firebaseDatabase.getReference("users").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Member member = dataSnapshot.getValue(Member.class);
                        if (!sharedPreferences.getString("memberKey", "").equals(dataSnapshot.getKey())) {
                            try {
                                Marker marker = memberHashMap.get(dataSnapshot.getKey());
                                if (marker != null) {
                                    marker.setPosition(new LatLng(Double.valueOf(dataSnapshot.child("location/currentLat").getValue().toString()), Double.valueOf(dataSnapshot.child("location/currentLon").getValue().toString())));
                                    marker.setSnippet(member.getRespondingTo());
                                    updateMemberMarkers();

                                    if (!member.getIsResponding()) {
                                        marker.remove();
                                        memberHashMap.remove(dataSnapshot.getKey());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                Marker marker = memberHashMap.get(dataSnapshot.getKey());
                                if (marker == null) {
                                    Log.d("MemberLoc", "" + dataSnapshot.child("location/currentLat").getValue());
                                    if (member.getIsResponding() && dataSnapshot.child("location/currentLat").getValue() != null) {
                                        memberHashMap.put(dataSnapshot.getKey(), addMemberMarker(dataSnapshot));
                                        updateMemberMarkers();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
