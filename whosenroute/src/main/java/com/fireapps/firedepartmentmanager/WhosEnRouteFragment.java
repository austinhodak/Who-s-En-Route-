package com.fireapps.firedepartmentmanager;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by austinhodak on 6/15/16.
 */

public class WhosEnRouteFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    ImageView moreButton;
    TextView mStation, mScene, mCantRespond;
    Drawable mRespondingSelected;
    RelativeLayout mStationLayout, mCantRespondLayout, mSceneLayout;
    ProgressBar mStationPB, mCantRespondPB, mScenePB;
    FirebaseUser firebaseUser;
    DatabaseReference userReference;
    FirebaseDatabase firebaseDatabase;
    List<Member> listStation, listScene, listCR, listAtStation, listUnknown;
    SectionedRecyclerViewAdapter sectionAdapter;
    MySection stationSection, sceneSection, CRSection, atStationSection, unknownSection;
    RelativeLayout emptyLayout;
    RecyclerView recyclerView;
    private BottomSheetBehavior mBottomSheetBehavior;

    boolean mIsLocationTrackingEnabled;
    private SharedPreferences sharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    boolean mIsResponding = false;
    private LocationRequest mLocationRequest;
    private Member mCurrentMember;

    public WhosEnRouteFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_whosenroute, container, false);

        listStation = new ArrayList<>();
        listScene = new ArrayList<>();
        listCR = new ArrayList<>();
        listAtStation = new ArrayList<>();
        listUnknown = new ArrayList<>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mIsLocationTrackingEnabled = sharedPreferences.getBoolean("pref_map_response_enable", false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            firebaseUser = user;
        } else {
            // No user is signed in
        }


        setUpGoogleApi();

        getActivity().setTitle("Who's En Route?");
        Log.d("RespondingUser", firebaseUser.getUid());

        firebaseDatabase = FirebaseDatabase.getInstance();
        userReference = firebaseDatabase.getReference("users/" + firebaseUser.getUid());

        moreButton = (ImageView) view.findViewById(R.id.responding_more);
        moreButton.setOnClickListener(this);

        mStation = (TextView) view.findViewById(R.id.responding_station);
        mScene = (TextView) view.findViewById(R.id.responding_scene);
        mCantRespond = (TextView) view.findViewById(R.id.responding_cantrespond);

        mStationLayout = (RelativeLayout) view.findViewById(R.id.responding_station_layout);
        mCantRespondLayout = (RelativeLayout) view.findViewById(R.id.responding_cr_layout);
        mSceneLayout = (RelativeLayout) view.findViewById(R.id.responding_scene_layout);
        mStationLayout.setOnClickListener(this);
        mCantRespondLayout.setOnClickListener(this);
        mSceneLayout.setOnClickListener(this);

        mStationPB = (ProgressBar) view.findViewById(R.id.responding_station_pb);
        mCantRespondPB = (ProgressBar) view.findViewById(R.id.responding_cr_pb);
        mScenePB = (ProgressBar) view.findViewById(R.id.responding_scene_pb);

        emptyLayout = (RelativeLayout) view.findViewById(R.id.empty_layout);

        sectionAdapter = new SectionedRecyclerViewAdapter();

        stationSection = new MySection("Station", listStation, getActivity());
        sceneSection = new MySection("Scene", listScene, getActivity());
        CRSection = new MySection("Can't Respond", listCR, getActivity());
        atStationSection = new MySection("At Station", listAtStation, getActivity());
        unknownSection = new MySection("Unknown", listUnknown, getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.responding_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(sectionAdapter);
        getRespondingStatus();
        return view;
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
                        int index = 0;
                        try {
                            for (Member object : listStation) {
                                if (object.getName().equals(member.getName())) {
                                    index = listStation.indexOf(object);
                                    listStation.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            for (Member object : listScene) {
                                if (object.getName().equals(member.getName())) {
                                    index= listScene.indexOf(object);
                                    listScene.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            for (Member object : listCR) {
                                if (object.getName().equals(member.getName())) {
                                    index= listCR.indexOf(object);
                                    listCR.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            for (Member object : listAtStation) {
                                if (object.getName().equals(member.getName())) {
                                    index= listStation.indexOf(object);
                                    listAtStation.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            for (Member object : listUnknown) {
                                if (object.getName().equals(member.getName())) {
                                    index= listUnknown.indexOf(object);
                                    listUnknown.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {

                            if (member.getIsResponding()) {
                                //Log.d("RESPONDING", member.getRespondingTo());
                                switch (member.getRespondingTo().toUpperCase()) {
                                    case "STATION":
                                        if (sectionAdapter.getSection("Station") == null) {
                                            sectionAdapter.addSection("Station", stationSection);
                                        }
                                        stationSection.addRow(member, index);
                                        sectionAdapter.notifyDataSetChanged();
                                        break;
                                    case "SCENE":
                                        if (sectionAdapter.getSection("Scene") == null) {
                                            sectionAdapter.addSection("Scene", sceneSection);
                                        }
                                        sceneSection.addRow(member, index);
                                        sectionAdapter.notifyDataSetChanged();
                                        break;
                                    case "NR":
                                        if (sectionAdapter.getSection("CR") == null) {
                                            sectionAdapter.addSection("CR", CRSection);
                                        }
                                        CRSection.addRow(member, index);
                                        sectionAdapter.notifyDataSetChanged();
                                        break;
                                    case "CAN'T RESPOND":
                                        if (sectionAdapter.getSection("CR") == null) {
                                            sectionAdapter.addSection("CR", CRSection);
                                        }
                                        CRSection.addRow(member, index);
                                        sectionAdapter.notifyDataSetChanged();
                                        break;
                                    case "AT STATION":
                                        if (sectionAdapter.getSection("At Station") == null) {
                                            sectionAdapter.addSection("At Station", atStationSection);
                                        }
                                        atStationSection.addRow(member, index);
                                        sectionAdapter.notifyDataSetChanged();
                                        break;
                                    default:
                                        if (sectionAdapter.getSection("Unknown") == null) {
                                            sectionAdapter.addSection("Unknown", unknownSection);
                                        }
                                        unknownSection.addRow(member, index);
                                        sectionAdapter.notifyDataSetChanged();
                                        break;
                                }
                            }
                        }

                        if (listStation.size() == 0) {
                            sectionAdapter.removeSection("Station");
                        }
                        if (listScene.size() == 0) {
                            sectionAdapter.removeSection("Scene");
                        }
                        if (listCR.size() == 0) {
                            sectionAdapter.removeSection("CR");
                        }
                        if (listAtStation.size() == 0) {
                            sectionAdapter.removeSection("At Station");
                        }
                        if (listUnknown.size() == 0) {
                            sectionAdapter.removeSection("Unknown");
                        }

                        if (sectionAdapter.getItemCount() == 0) {
                            emptyLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.responding_more:
                PopupMenu popup = new PopupMenu(getActivity(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.responding_more_menu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.resp_more_remove:
                                setRespondingSelected(5, false);
                                updateRespondingStatus(5);
                                break;
                        }
                        return false;
                    }
                });
                break;
            case R.id.responding_station_layout:
                setRespondingSelected(0, false);
                updateRespondingStatus(0);
                break;
            case R.id.responding_scene_layout:
                setRespondingSelected(1, false);
                updateRespondingStatus(1);
                break;
            case R.id.responding_cr_layout:
                setRespondingSelected(2, false);
                updateRespondingStatus(2);
                break;
        }
    }

    private void setRespondingSelected(int mRespondingSelected, boolean isUpdated) {
        if (!isAdded()) {
            return;
        }
        if (isUpdated) {
            switch (mRespondingSelected) {
                case 0:
                    mStation.setTextColor(Color.parseColor("#000000"));
                    mStationLayout.setBackground(getResources().getDrawable(R.drawable.chip_loc));
                    mStationPB.setVisibility(View.INVISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#FFFFFF"));
                    mCantRespondLayout.setBackground(null);
                    mCantRespondPB.setVisibility(View.INVISIBLE);

                    mScene.setTextColor(Color.parseColor("#FFFFFF"));
                    mSceneLayout.setBackground(null);
                    mScenePB.setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    mStation.setTextColor(Color.parseColor("#FFFFFF"));
                    mStationLayout.setBackground(null);
                    mStationPB.setVisibility(View.INVISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#FFFFFF"));
                    mCantRespondLayout.setBackground(null);
                    mCantRespondPB.setVisibility(View.INVISIBLE);

                    mScene.setTextColor(Color.parseColor("#000000"));
                    mSceneLayout.setBackground(getResources().getDrawable(R.drawable.chip_loc));
                    mScenePB.setVisibility(View.INVISIBLE);

                    break;
                case 2:
                    mStation.setTextColor(Color.parseColor("#FFFFFF"));
                    mStationLayout.setBackground(null);
                    mStationPB.setVisibility(View.INVISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#000000"));
                    mCantRespondLayout.setBackground(getResources().getDrawable(R.drawable.chip_loc));
                    mCantRespondPB.setVisibility(View.INVISIBLE);

                    mScene.setTextColor(Color.parseColor("#FFFFFF"));
                    mSceneLayout.setBackground(null);
                    mScenePB.setVisibility(View.INVISIBLE);

                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    //Removed
                    mStation.setTextColor(Color.parseColor("#FFFFFF"));
                    mStationLayout.setBackground(null);
                    mStationPB.setVisibility(View.INVISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#FFFFFF"));
                    mCantRespondLayout.setBackground(null);
                    mCantRespondPB.setVisibility(View.INVISIBLE);

                    mScene.setTextColor(Color.parseColor("#FFFFFF"));
                    mSceneLayout.setBackground(null);
                    mScenePB.setVisibility(View.INVISIBLE);
                    break;
            }
        } else {
            switch (mRespondingSelected) {
                case 0:
                    mStation.setTextColor(Color.parseColor("#000000"));
                    mStationLayout.setBackground(getResources().getDrawable(R.drawable.chip_loc));
                    mStationPB.setVisibility(View.VISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#FFFFFF"));
                    mCantRespondLayout.setBackground(null);
                    mCantRespondPB.setVisibility(View.INVISIBLE);

                    mScene.setTextColor(Color.parseColor("#FFFFFF"));
                    mSceneLayout.setBackground(null);
                    mScenePB.setVisibility(View.INVISIBLE);

                    break;
                case 1:
                    mStation.setTextColor(Color.parseColor("#FFFFFF"));
                    mStationLayout.setBackground(null);
                    mStationPB.setVisibility(View.INVISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#FFFFFF"));
                    mCantRespondLayout.setBackground(null);
                    mCantRespondPB.setVisibility(View.INVISIBLE);

                    mScene.setTextColor(Color.parseColor("#000000"));
                    mSceneLayout.setBackground(getResources().getDrawable(R.drawable.chip_loc));
                    mScenePB.setVisibility(View.VISIBLE);

                    break;
                case 2:
                    mStation.setTextColor(Color.parseColor("#FFFFFF"));
                    mStationLayout.setBackground(null);
                    mStationPB.setVisibility(View.INVISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#000000"));
                    mCantRespondLayout.setBackground(getResources().getDrawable(R.drawable.chip_loc));
                    mCantRespondPB.setVisibility(View.VISIBLE);

                    mScene.setTextColor(Color.parseColor("#FFFFFF"));
                    mSceneLayout.setBackground(null);
                    mScenePB.setVisibility(View.INVISIBLE);

                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    //Removed
                    mStation.setTextColor(Color.parseColor("#FFFFFF"));
                    mStationLayout.setBackground(null);
                    mStationPB.setVisibility(View.INVISIBLE);

                    mCantRespond.setTextColor(Color.parseColor("#FFFFFF"));
                    mCantRespondLayout.setBackground(null);
                    mCantRespondPB.setVisibility(View.INVISIBLE);

                    mScene.setTextColor(Color.parseColor("#FFFFFF"));
                    mSceneLayout.setBackground(null);
                    mScenePB.setVisibility(View.INVISIBLE);
                    break;
            }
        }

    }

    private void updateRespondingStatus(int mRespondingSelected) {

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
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);
        mIsResponding = true;
        switch (mRespondingSelected) {
            case 0:
                //Station
                Map<String, Object> respondingToStation = new HashMap<String, Object>();
                respondingToStation.put("respondingTo", "Station");
                respondingToStation.put("isResponding", true);
                respondingToStation.put("respondingTime", mLastUpdateTime);
                userReference.updateChildren(respondingToStation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        mStationPB.setVisibility(View.INVISIBLE);
                    }
                });
                break;
            case 1:
                //Scene
                Map<String, Object> respondingToScene = new HashMap<String, Object>();
                respondingToScene.put("respondingTo", "Scene");
                respondingToScene.put("isResponding", true);
                respondingToScene.put("respondingTime", mLastUpdateTime);
                userReference.updateChildren(respondingToScene).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        mScenePB.setVisibility(View.INVISIBLE);
                    }
                });
                break;
            case 2:
                //Can't Respond
                Map<String, Object> cantRespond = new HashMap<String, Object>();
                cantRespond.put("respondingTo", "Can't Respond");
                cantRespond.put("isResponding", true);
                cantRespond.put("respondingTime", mLastUpdateTime);
                userReference.updateChildren(cantRespond).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        mCantRespondPB.setVisibility(View.INVISIBLE);
                    }
                });
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                Map<String, Object> respondingReset = new HashMap<String, Object>();
                respondingReset.put("respondingTo", "");
                respondingReset.put("isResponding", false);
                respondingReset.put("respondingTime", "");
                respondingReset.put("location/currentLat", "");
                respondingReset.put("location/currentLon", "");
                userReference.updateChildren(respondingReset).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        mIsResponding = false;
                    }
                });
                break;
        }
    }

    private void getRespondingStatus() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Member member = dataSnapshot.getValue(Member.class);
                mCurrentMember = member;
                // ...
                if (member.getIsResponding() && member.getRespondingTo() != null) {
                    updateButtons(true, member.getRespondingTo());
                    Log.d("Responding", member.getRespondingTo());
                    if (member.getRespondingTo() != "Can't Respond" || member.getRespondingTo() != "NR" && mIsLocationTrackingEnabled) {
                        mIsResponding = true;
                        if (mGoogleApiClient.isConnected()) {
                            startLocationUpdates();
                        }
                    }
                } else {
                    updateButtons(false);
                }
                Log.d("Responding", member.getRespondingTo() + member.getIsResponding());
                loadRespondingMembers(member.getDepartment());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        };
        userReference.addValueEventListener(postListener);
    }

    private void updateButtons(boolean isResponding) {
        setRespondingSelected(5, true);
        mIsResponding = false;
    }

    private void updateButtons(boolean isResponding, String respondingTo) {
        switch (respondingTo.toLowerCase()) {
            case "":
                setRespondingSelected(5, true);
                break;
            case "station":
                setRespondingSelected(0, true);
                break;
            case "can't respond":
                setRespondingSelected(2, true);
                break;
            case "nr":
                setRespondingSelected(2, true);
                break;
            case "scene":
                setRespondingSelected(1, true);
                break;
            default:
                setRespondingSelected(5, true);
                break;
        }
    }


    /* LOCATION STUFF */

    protected void startLocationUpdates() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mLocationRequest != null) {
            Log.d("WHOER-LOC", "Location already being tracked, no need to start more.");
            return;
            //Already requesting location, don't start again.
        }

        String mLocationPriority = sharedPreferences.getString("pref_map_response_accuracy", "PRIORITY_HIGH_ACCURACY");
        String mLocationInterval = sharedPreferences.getString("pref_map_response_frequency", "10000");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Integer.parseInt(mLocationInterval));
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(10f);
        switch (mLocationPriority) {
            case "PRIORITY_HIGH_ACCURACY":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
            case "PRIORITY_NO_POWER":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
                break;
            case "PRIORITY_LOW_POWER":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            case "PRIORITY_BALANCED_POWER_ACCURACY":
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            default:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        Log.d("WHOER-LOC", "LOCATION TRACKING STARTED");
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
    public void onResume() {
        if (mIsResponding && mIsLocationTrackingEnabled) {
            //startLocationUpdates();
        }
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("WHOER-LOC", "CONNECTED");
        if (mIsLocationTrackingEnabled && mIsResponding) {
            //startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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

    @Override
    public void onLocationChanged(Location location) {
        if (!mIsLocationTrackingEnabled) {
            return;
        }
        //Location Enabled, send to Firebase!!
        Log.d("WHOER-LOC", "Location Updated" + location);
        Log.d("WHOER-LOC", "" + mCurrentMember);

        Map<String, Object> updates = new HashMap<>();
        updates.put("/location/currentLat", location.getLatitude());
        updates.put("/location/currentLon", location.getLongitude());

        userReference.updateChildren(updates);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("WHOER-LOC", connectionResult.getErrorMessage());
    }

    /* END LOCATION STUFF */

    class MySection extends StatelessSection {

        String title;
        List<Member> list;
        Context context;

        public MySection(String title, List<Member> list, Context context) {
            // call constructor with layout resources for this Section header, footer and items
            super(R.layout.listitem_section, R.layout.listitem_responding);

            this.title = title;
            this.list = list;
            this.context = context;
        }

        @Override
        public int getContentItemsTotal() {
            return list.size(); // number of items of this section
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            // return a custom instance of ViewHolder for the items of this section
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            final Member member = list.get(position);

            ShapeDrawable shapeDrawable = new ShapeDrawable();
            shapeDrawable.setShape(new OvalShape());
            Log.d("COLOR", member.getPositionColor());
            shapeDrawable.getPaint().setColor(Color.parseColor("#" + member.getPositionColor()));
            itemHolder.posImage.setBackground(shapeDrawable);

            itemHolder.tvAbbrv.setText(member.getPositionAbbrv());

            // bind your view here
            itemHolder.tvName.setText(member.getName());

            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Long.valueOf(member.getPhoneNum()) != null) {
                        String menu[] = {"Call", "Text"};
                        new MaterialDialog.Builder(context)
                                .title("Complete Incident")
                                .items(menu)
                                .itemsGravity(GravityEnum.CENTER)
                                .itemsColor(getResources().getColor(R.color.bottom_sheet_title))
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        switch (which) {
                                            case 0:
                                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", String.valueOf(member.getPhoneNum()), null));
                                                startActivity(intent);
                                                break;
                                            case 1:
                                                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                                                sendIntent.setData(Uri.parse("sms:" + member.getPhoneNum()));
                                                startActivity(sendIntent);
                                                break;
                                        }
                                    }
                                })
                                .negativeText("Cancel")
                                .show();
                    }
                }
            });

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            try {
                Date date;

                date = dateFormat.parse(member.getRespondingTime());

                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                calendar.setTime(date);

                itemHolder.tvTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            } catch (ParseException e) {
                e.printStackTrace();
                itemHolder.tvTime.setVisibility(View.INVISIBLE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                itemHolder.tvTime.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            // bind your header view here
            headerHolder.tvTitle.setText(title);
        }

        public void addRow (Member item, Integer index) {
            this.list.add(index, item);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        public HeaderViewHolder(View view) {
            super(view);

            tvTitle = (TextView) view.findViewById(R.id.section);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView tvName, tvAbbrv, tvTime;
        private final RelativeLayout posLayout;
        private final ImageView posImage;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            tvName = (TextView) view.findViewById(R.id.LI_responding_name);
            tvAbbrv = (TextView) view.findViewById(R.id.LI_responding_abbrv);
            tvTime = (TextView) view.findViewById(R.id.LI_responding_time);
            posLayout = (RelativeLayout) view.findViewById(R.id.LI_pos_layout);
            posImage = (ImageView) view.findViewById(R.id.LI_pos_image);
        }
    }
}