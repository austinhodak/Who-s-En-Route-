package com.fireapps.firedepartmentmanager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by austinhodak on 6/19/16.
 */

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

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
    AddressResultReceiver addressResultReceiver;
    Marker currentLocationMarker;
    Marker newPlaceMarker = null;

    Markers markers = new Markers();

    //GoogleMap mMap = null;
    Marker marker;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mRequestinLocationUpdates = true;
    private SharedPreferences sharedPreferences;
    private String departmentID;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userReference;

    private HashMap<String, Marker> memberHashMap = new HashMap<>();
    private ArrayList<DataSnapshot> members = new ArrayList<>();
    String TAG = "MAP";

    private Map<String, DataSnapshot> infoMarkersData = new HashMap<>();
    private Map<String, Marker> infoMarkers;
    private CameraPosition savedCamerPosition;

    private FloatingActionButton addMarkerFab;
    private ImageView markerOverlay;
    private TextView markerOverlayCoords;
    private LinearLayout marker_ll1;
    private FloatingActionButton newMarkerCancel, newMarkerDone;
    private FloatingActionMenu mainFab;

    private ClusterManager<com.fireapps.firedepartmentmanager.ClusterItem> clusterManager;
    private com.androidmapsextensions.GoogleMap mMap;
    private FloatingActionButton editMarkerFab;

    private RelativeLayout mapSearchLayout;

    private int selectedMarkerViews = 1;
    private boolean selectableMarker;
    //0 = None, 1 = Department Only, 2 = ALL;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        getActivity().setTitle("Map");
        setHasOptionsMenu(true);

        final SupportMapFragment supportMapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_fragment, supportMapFragment).commit();

        if (savedInstanceState == null) {
            supportMapFragment.setRetainInstance(true);
        } else {
            mMap = supportMapFragment.getExtendedMap();
        }

        ((AppCompatActivity)getActivity()).getSupportActionBar().setElevation(4);

        setUpGoogleApi();

        if (mMap == null) {
            supportMapFragment.getExtendedMapAsync(new com.androidmapsextensions.OnMapReadyCallback() {
                @Override
                public void onMapReady(com.androidmapsextensions.GoogleMap googleMap) {
                    mMap = supportMapFragment.getExtendedMap();

                    int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        setUpMap();
                    }
                }
            });
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        departmentID = RespondingSystem.getInstance(getActivity()).getCurrentDepartmentKey();

        searchMarkers = new ArrayList<>();
        memberMarkers = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        loadRespondingMembers(departmentID);

        addMarkerFab = (FloatingActionButton) view.findViewById(R.id.map_add_marker_fab);
        editMarkerFab = (FloatingActionButton) view.findViewById(R.id.map_edit_marker_fab);
        markerOverlay = (ImageView)view.findViewById(R.id.new_marker_overlay);
        markerOverlayCoords = (TextView)view.findViewById(R.id.new_marker_coords);
        marker_ll1 = (LinearLayout)view.findViewById(R.id.map_add_marker_ll1);
        newMarkerCancel = (FloatingActionButton) view.findViewById(R.id.map_add_marker_cancel);
        newMarkerDone = (FloatingActionButton)view.findViewById(R.id.map_add_marker_done);
        mainFab = (FloatingActionMenu)view.findViewById(R.id.map_fab);

        mapSearchLayout = (RelativeLayout)view.findViewById(R.id.map_search_layout);

        newMarkerCancel.hide(false);
        newMarkerDone.hide(false);

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
        selectableMarker = false;
        editMarkerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectableMarker = true;
                mainFab.hideMenuButton(true);
                Snackbar snackbar1 = Snackbar.make(view, "Select marker to edit.", Snackbar.LENGTH_INDEFINITE).setAction("CANCEL", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mainFab.showMenuButton(true);
                    }
                }).setActionTextColor(getResources().getColor(R.color.new_accent));

                View view1 = snackbar1.getView();
                view1.setBackgroundColor(getResources().getColor(R.color.snackbar));
                ((TextView)view1.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.text_responding_name));
                snackbar1.show();

                mMap.setOnMarkerClickListener(new com.androidmapsextensions.GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        if(selectableMarker) {
                            Snackbar snackbar = Snackbar.make(getView(), marker.getTitle() + " Marker Selected.", 8000).setAction("CHOOSE", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getActivity(), MapEditMarker.class);
                                    intent.putExtra("markerID", infoMarkersData.get(marker.getId()).getKey());
                                    startActivityForResult(intent, 0);
                                    mainFab.showMenuButton(false);
                                    selectableMarker = false;
                                }
                            }).setCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    super.onDismissed(snackbar, event);
                                    mainFab.showMenuButton(true);
                                    selectableMarker = false;
                                }
                            }).setActionTextColor(getResources().getColor(R.color.new_accent));
                            View view1 = snackbar.getView();
                            view1.setBackgroundColor(getResources().getColor(R.color.snackbar));
                            ((TextView)view1.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.text_responding_name));
                            snackbar.show();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            }
        });

        final EditText search = (EditText)view.findViewById(R.id.map_search_box);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + search.getText().toString());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
                return false;
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //((NavDrawerActivity) getActivity()).getSupportActionBar().setElevation(4);
        /*((NavDrawerActivity) getActivity()).searchView.setOnQueryTextListener(new com.miguelcatalan.materialsearchview.MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getActivity(), GeocodeAddressIntentService.class);
                intent.putExtra(RECEIVER, addressResultReceiver);
                intent.putExtra(FETCH_TYPE_EXTRA, USE_ADDRESS_NAME);
                intent.putExtra(LOCATION_NAME_DATA_EXTRA, query);
                getActivity().startService(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });*/

        addMarkerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerOverlay.setVisibility(View.VISIBLE);
                markerOverlayCoords.setVisibility(View.VISIBLE);
                double lat = mMap.getCameraPosition().target.latitude;
                double lon = mMap.getCameraPosition().target.longitude;
                markerOverlayCoords.setText(String.valueOf(lat).substring(0, 9) + ", " + String.valueOf(lon).substring(0, 10));

                addMarkerFab.hide(true);
                mainFab.hideMenuButton(true);
                newMarkerCancel.show(true);
                newMarkerDone.show(true);

                newMarkerDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        com.androidmapsextensions.GoogleMap.SnapshotReadyCallback snapshotReadyCallback = new com.androidmapsextensions.GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(final Bitmap bitmap) {
                                Log.d(TAG, "onSnapshotReady:");

                                Intent intent = new Intent(getActivity(), MapNewMarker.class);
                                final ByteArrayOutputStream bs = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bs);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                                try {
                                    newPlaceMarker.remove();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                intent.putExtra("map", bs.toByteArray());
                                intent.putExtra("mapPos", mMap.getCameraPosition().target);
                                startActivityForResult(intent, 1);
                                getActivity().overridePendingTransition(R.anim.slide_up, R.anim.stay);

                                markerOverlay.setVisibility(View.GONE);
                                markerOverlayCoords.setVisibility(View.GONE);
                                markerOverlayCoords.setText("");
                                mainFab.showMenuButton(true);
                                newMarkerCancel.hide(true);
                                newMarkerDone.hide(true);

                                Log.d(TAG, "onSnapshotReady: MARKER CENTER");
                            }
                        };
                        mMap.snapshot(snapshotReadyCallback);
                        Log.d(TAG, "onClick: Snapshot Called");
                    }
                });
            }
        });

        newMarkerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerOverlay.setVisibility(View.GONE);
                markerOverlayCoords.setVisibility(View.GONE);
                markerOverlayCoords.setText("");
                mainFab.showMenuButton(true);
                newMarkerCancel.hide(true);
                newMarkerDone.hide(true);

                try {
                    newPlaceMarker.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Permissions.getInstance(getActivity()).canMapEditMarker();

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
        try {
            LatLng latLng = new LatLng(Double.valueOf(sharedPreferences.getString("mapLastLon", null)), Double.valueOf(sharedPreferences.getString("mapLastLon", null)));
            if (latLng != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        sharedPreferences.edit().putString("mapLastLon", String.valueOf(mMap.getCameraPosition().target.longitude)).apply();
        sharedPreferences.edit().putString("mapLastLat", String.valueOf(mMap.getCameraPosition().target.latitude)).apply();
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onResume() {
        selectableMarker = false;
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        mMap.clear();

        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

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
        Log.d(TAG, "setUpMap: ");
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.setOnMapLongClickListener(new com.androidmapsextensions.GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (newPlaceMarker != null) {
                    newPlaceMarker.remove();
                }
                newPlaceMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Marker")
                        .anchor(0.5f, 0.5f)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.location_define, 96, 96))));

                markerOverlay.setVisibility(View.GONE);
                markerOverlayCoords.setVisibility(View.GONE);

                double lat = mMap.getCameraPosition().target.latitude;
                double lon = mMap.getCameraPosition().target.longitude;
                markerOverlayCoords.setText(String.valueOf(lat).substring(0, 9) + ", " + String.valueOf(lon).substring(0, 10));

                addMarkerFab.hide(true);
                mainFab.hideMenuButton(true);
                newMarkerCancel.show(true);
                newMarkerDone.show(true);

                newMarkerDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final com.androidmapsextensions.GoogleMap.SnapshotReadyCallback snapshotReadyCallback = new com.androidmapsextensions.GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap bitmap) {
                                Log.d(TAG, "onSnapshotReady: MARKER DRAG");
                                Intent intent = new Intent(getActivity(), MapNewMarker.class);
                                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);
                                intent.putExtra("map", bs.toByteArray());
                                intent.putExtra("mapPos", mMap.getCameraPosition().target);
                                intent.putExtra("isMarkerVisible", true);
                                startActivityForResult(intent, 1);

                                markerOverlay.setVisibility(View.GONE);
                                markerOverlayCoords.setVisibility(View.GONE);
                                markerOverlayCoords.setText("");
                                mainFab.showMenuButton(true);
                                newMarkerCancel.hide(true);
                                newMarkerDone.hide(true);

                                newPlaceMarker.remove();
                            }
                        };
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(newPlaceMarker.getPosition()));
                        mMap.setOnCameraChangeListener(new com.androidmapsextensions.GoogleMap.OnCameraChangeListener() {
                            @Override
                            public void onCameraChange(CameraPosition cameraPosition) {
                                mMap.snapshot(snapshotReadyCallback);
                                mMap.setOnCameraChangeListener(null);
                            }
                        });
                    }
                });
            }
        });

        mMap.setOnCameraChangeListener(new com.androidmapsextensions.GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                double lat = mMap.getCameraPosition().target.latitude;
                double lon = mMap.getCameraPosition().target.longitude;
                if (markerOverlayCoords.getVisibility() == View.VISIBLE) {
                    markerOverlayCoords.setText(String.valueOf(lat).substring(0, 9) + ", " + String.valueOf(lon).substring(0, 10));
                }
            }
        });

        mMap.setClustering(new ClusteringSettings().clusterOptionsProvider(new MapCluster(getActivity().getResources())).addMarkersDynamically(true).minMarkersCount(10));


        loadInfoMarkers(departmentID, false);

        mMap.setOnMarkerClickListener(new com.androidmapsextensions.GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if(selectableMarker) {
                    Snackbar snackbar = Snackbar.make(getView(), marker.getTitle() + " Marker Selected.", 8000).setAction("CHOOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), MapEditMarker.class);
                            intent.putExtra("markerID", infoMarkersData.get(marker.getId()).getKey());
                            startActivityForResult(intent, 0);
                            mainFab.showMenuButton(false);
                            selectableMarker = false;
                        }
                    }).setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            mainFab.showMenuButton(true);
                            selectableMarker = false;
                        }
                    }).setActionTextColor(getResources().getColor(R.color.new_accent));
                    View view1 = snackbar.getView();
                    view1.setBackgroundColor(getResources().getColor(R.color.snackbar));
                    ((TextView)view1.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.text_responding_name));
                    snackbar.show();
                    return true;
                } else {
                    if (marker.isCluster()) {
                        int zoom = (int) mMap.getCameraPosition().zoom + 2;
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude + (double) 90 / Math.pow(2, zoom), marker.getPosition().longitude), zoom);
                        mMap.animateCamera(cu);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        });

        mMap.setPadding(0, 154, 0, 210);
    }

    private void addIncidentMarker(Address address) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title("Search Result").snippet(address.getAddressLine(0))).showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(address.getLatitude(), address.getLongitude()), 13));
    }

//    private void addMapMarkers() {
//
//    }

    public Bitmap resizeMapIcons(int iconName, final int width, final int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconName);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        imageBitmap.recycle();
        return resizedBitmap;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_map_search);
        //((NavDrawerActivity) getActivity()).searchView.setMenuItem(item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_map_search) {

            if (mapSearchLayout.getVisibility() == View.INVISIBLE) {
                int cx = mapSearchLayout.getWidth() / 2;
                int cy = mapSearchLayout.getHeight() / 2;

                float finalRadius = (float) Math.hypot(cx, cy);

                Animator anim =
                        ViewAnimationUtils.createCircularReveal(mapSearchLayout, cx, cy, 0, finalRadius);

                mapSearchLayout.setVisibility(View.VISIBLE);
                anim.start();
            } else {
                int cx = mapSearchLayout.getWidth() / 2;
                int cy = mapSearchLayout.getHeight() / 2;

// get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);

// create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(mapSearchLayout, cx, cy, initialRadius, 0);

// make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mapSearchLayout.setVisibility(View.INVISIBLE);
                    }
                });

// start the animation
                anim.start();

            }
        }



        if (id == R.id.menu_map_layers) {
//            new MaterialDialog.Builder(getActivity())
//                    .title("Set Map Layer")
//                    .items(R.array.map_layers)
//                    .itemsCallbackSingleChoice(mMap.getMapType(), new MaterialDialog.ListCallbackSingleChoice() {
//                        @Override
//                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//
//                            return true;
//                        }
//                    })
//                    .show();

            final MaterialDialog alertDialog = new MaterialDialog.Builder(getActivity())
                    .customView(R.layout.dialog_map_layers, false)
                    .positiveText("Ok")
                    .negativeText("Cancel")
                    .backgroundColorRes(R.color.bottom_sheet_background)
                    .widgetColor(getResources().getColor(R.color.new_accent))
                    .theme(Theme.LIGHT)
                    .widgetColor(getResources().getColor(R.color.md_red_700))
                    .show();

            View view = alertDialog.getCustomView();
            MaterialSpinner layerSpinner = (MaterialSpinner)view.findViewById(R.id.dialog_maplayers_style);
            MaterialSpinner markerSpinner = (MaterialSpinner)view.findViewById(R.id.dialog_maplayers_markers);

            layerSpinner.setItems("None", "Normal", "Satellite", "Terrain", "Hybrid");
            layerSpinner.setSelectedIndex(mMap.getMapType());
            layerSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    switch (position) {
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
                }
            });

            markerSpinner.setItems("None", "Your Department", "All Departments");
            markerSpinner.setSelectedIndex(selectedMarkerViews);
            markerSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    selectedMarkerViews = position;
                    if (position == 0) {
                        try {
                            for (String key: infoMarkers.keySet()) {
                                Marker marker = infoMarkers.get(key);
                                marker.remove();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            infoMarkers = null;
                            infoMarkersData = null;
                        }
                    }
                    if (position == 1) {
                        loadInfoMarkers(departmentID, false);
                    } else if (position == 2) {
                        loadInfoMarkers(departmentID, true);
                    }
                }
            });
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
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
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

                                    try {
                                        if (member.getIsResponding() && dataSnapshot.child("location/currentLat").getValue() != "" && dataSnapshot.child("location/currentLat").getValue() != "") {
                                            memberHashMap.put(dataSnapshot.getKey(), addMemberMarker(dataSnapshot));
                                            updateMemberMarkers();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
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

    private void loadInfoMarkers(String department, boolean loadAll) {
        mMap.clear();

        infoMarkers = new HashMap<>();
        infoMarkersData = new HashMap<>();
        if (loadAll) {
            firebaseDatabase.getReference().child("markers").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (!isAdded()) {
                        return;
                    }
                    int icon;
                    MapMarker mapMarker = markers.getMarkerForValue(dataSnapshot.child("type").getValue().toString());
                    icon = mapMarker.getMainIcon();
                    if (mapMarker.equals(Markers.hydrant)) {
                        try {
                            MapMarker hydrant = markers.getHydrantMarkerForValue(dataSnapshot.child("hydrant").child("flow").getValue().toString());
                            icon = hydrant.getMainIcon();
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                    LatLng latLng = new LatLng((double) dataSnapshot.child("lat").getValue(), (double) dataSnapshot.child("lon").getValue());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(mapMarker.getName())
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(icon, 60, 60)))
                            .flat(false).clusterGroup(ClusterGroup.FIRST_USER));
                    infoMarkersData.put(marker.getId(), dataSnapshot);
                    infoMarkers.put(marker.getId(), marker);

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
        } else {
            firebaseDatabase.getReference("departments").child(department).child("markers").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot2, String s) {

                    firebaseDatabase.getReference("markers").child(dataSnapshot2.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!isAdded()) {
                                return;
                            }
                            int icon;
                            Log.d(TAG, "onChildAdded: MAP MARKER " + dataSnapshot.getKey());
                            MapMarker mapMarker = markers.getMarkerForValue(dataSnapshot.child("type").getValue().toString());
                            icon = mapMarker.getMainIcon();
                            if (mapMarker.equals(Markers.hydrant)) {
                                try {
                                    MapMarker hydrant = markers.getHydrantMarkerForValue(dataSnapshot.child("hydrant").child("flow").getValue().toString());
                                    icon = hydrant.getMainIcon();
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            }
                            LatLng latLng = new LatLng((double) dataSnapshot.child("lat").getValue(), (double) dataSnapshot.child("lon").getValue());
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(mapMarker.getName())
                                    .anchor(0.5f, 0.5f)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(icon, 60, 60)))
                                    .flat(false).clusterGroup(ClusterGroup.FIRST_USER));
                            infoMarkersData.put(marker.getId(), dataSnapshot);
                            infoMarkers.put(marker.getId(), marker);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
//            updateMarkers();
//        }
//        if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
//            updateMarkers();
//        }
        updateMarkers();
    }

    private void updateMarkers() {
        try {
            for (String key: infoMarkers.keySet()) {
                Marker marker = infoMarkers.get(key);
                marker.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            infoMarkers = null;
            infoMarkersData = null;
            loadInfoMarkers(departmentID, false);
        }
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

            } else {
                Toast.makeText(getActivity(), "Address Not Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CustomInfoWindowAdapter implements com.androidmapsextensions.GoogleMap.InfoWindowAdapter {

        private View view;

        public CustomInfoWindowAdapter() {

        }

        @Override
        public View getInfoWindow(Marker marker) {
//            MapFragment.this.marker = marker;
//            if (marker != null && marker.isInfoWindowShown()) {
//                marker.hideInfoWindow();
//                marker.showInfoWindow();
//            }
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            view = getLayoutInflater(getArguments()).inflate(R.layout.map_info_marker, null);

            MapFragment.this.marker = marker;

            String type = null, location = null;
            String other = null;

            final TextView title = (TextView) view.findViewById(R.id.map_marker_title);
            final TextView subTitle = (TextView) view.findViewById(R.id.map_marker_subtitle);
            final TextView locationTv = (TextView) view.findViewById(R.id.map_marker_loc);
            final TextView name = (TextView) view.findViewById(R.id.map_marker_name);
            final TextView otherTv  = (TextView) view.findViewById(R.id.map_marker_other);
            final TextView otherSection  = (TextView) view.findViewById(R.id.section2);

            final TextView inService = (TextView) view.findViewById(R.id.marker_service);

            final LinearLayout hydrantLayout = (LinearLayout) view.findViewById(R.id.hydrant_ll1);
            final TextView hydrantFlow = (TextView) view.findViewById(R.id.map_hydrant_fr);
            final TextView hydrantType = (TextView) view.findViewById(R.id.map_hydrant_type);
            final TextView hydrantSize = (TextView) view.findViewById(R.id.map_hydrant_size);
            final TextView hydrantOutlets = (TextView) view.findViewById(R.id.map_hydrant_outlets);

            try {
                if (marker.getId() != null && infoMarkersData != null && infoMarkersData.size() > 0) {
                    if (infoMarkersData.get(marker.getId()) != null) {
                        DataSnapshot markerInfo = infoMarkersData.get(marker.getId());
                        type = markerInfo.child("type").getValue().toString();
                        location = markerInfo.child("location").getValue().toString();
                        name.setText(markerInfo.child("name").getValue().toString());
                        Log.d("MARKER", markerInfo + "2");

                        other = markerInfo.child("other").getValue().toString();

                        if (!other.isEmpty()) {
                            otherTv.setText(other);
                            otherTv.setVisibility(View.VISIBLE);
                        } else {
                            otherSection.setVisibility(View.GONE);
                            otherTv.setVisibility(View.GONE);
                        }

                        Log.d(TAG, "getInfoContents: " + otherTv.getText());

                        if (markerInfo.child("inService").getValue() != null) {
                            if ((boolean)markerInfo.child("inService").getValue()) {
                                inService.setText("IN SERVICE");
                            } else {
                                inService.setText("OUT OF SERVICE");
                                inService.setTextColor(getResources().getColor(R.color.primary));
                            }
                        } else {
                            inService.setVisibility(View.GONE);
                        }

                        if (markerInfo.child("hydrant").getValue() != null) {
                            //Marker is a hydrant.
                            if (markerInfo.child("hydrant").child("flow").getValue() != null) {
                                hydrantFlow.setText(markerInfo.child("hydrant").child("flow").getValue().toString());
                            }
                            if (markerInfo.child("hydrant").child("outlets").getValue() != null) {
                                hydrantOutlets.setText(markerInfo.child("hydrant").child("outlets").getValue().toString());
                            }
                            if (markerInfo.child("hydrant").child("type").getValue() != null) {
                                hydrantType.setText(markerInfo.child("hydrant").child("type").getValue().toString());
                            }
                            if (markerInfo.child("hydrant").child("sizes").getValue() != null) {
                                hydrantSize.setText(markerInfo.child("hydrant").child("sizes").getValue().toString());
                            }
                        } else {
                            hydrantLayout.setVisibility(View.GONE);
                        }
                    }
                }
                Log.d("MARKER", type);
                title.setText(type);
                LatLng latLng = marker.getPosition();
                subTitle.setText(String.valueOf(latLng.latitude).substring(0, 9) + ", " + String.valueOf(latLng.longitude).substring(0, 10));
                locationTv.setText(location);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    marker.hideInfoWindow();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            return view;
        }
    }
}
