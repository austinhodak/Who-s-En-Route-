package com.fireapps.firedepartmentmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by austinhodak on 6/15/16.
 */

public class WhosEnRouteFragment extends Fragment implements View.OnClickListener {

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
    boolean mIsLocationTrackingEnabled;
    boolean mIsResponding = false;
    private BottomSheetBehavior mBottomSheetBehavior;
    private SharedPreferences sharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Member mCurrentMember;
    private boolean mRespondingConfirm;
    private LinearLayout mParentLayout;

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
        mRespondingConfirm = sharedPreferences.getBoolean("pref_responding_confirm_status", true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            firebaseUser = user;
        } else {
            // No user is signed in
        }

        getActivity().setTitle("Who's En Route?");
        Log.d("RespondingUser", firebaseUser.getUid());

        firebaseDatabase = FirebaseDatabase.getInstance();
        userReference = firebaseDatabase.getReference("users/" + firebaseUser.getUid());
        userReference.keepSynced(true);

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
        mParentLayout = (LinearLayout) view.findViewById(R.id.linearLayout);

        emptyLayout = (RelativeLayout) view.findViewById(R.id.empty_layout);
        final ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
        final RotateAnimation anim = new RotateAnimation(0.0f, -7.0f, Animation.RELATIVE_TO_SELF, 0.4f, Animation.RELATIVE_TO_SELF, 0.85f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setDuration(30);
        //imageView.startAnimation(anim);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageView.getAnimation() != null) {
                    imageView.clearAnimation();
                } else {
                    imageView.startAnimation(anim);
                }
            }
        });

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
                                    index = listScene.indexOf(object);
                                    listScene.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            for (Member object : listCR) {
                                if (object.getName().equals(member.getName())) {
                                    index = listCR.indexOf(object);
                                    listCR.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            for (Member object : listAtStation) {
                                if (object.getName().equals(member.getName())) {
                                    index = listStation.indexOf(object);
                                    listAtStation.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            for (Member object : listUnknown) {
                                if (object.getName().equals(member.getName())) {
                                    index = listUnknown.indexOf(object);
                                    listUnknown.remove(object);
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            /*CRASH FIX, INDEX*/
                            Log.d("RESPONDING", "onDataChange: " + dataSnapshot.child("respondingAgency").getValue());
                            if (!dataSnapshot.child("respondingAgency").getValue().toString().isEmpty()) {
                                if (member.getIsResponding() && member.getRespondingTo() != null && dataSnapshot.child("respondingAgency").getValue().toString().equals(RespondingSystem.getInstance(getActivity()).getCurrentDepartmentKey())) {
                                    //Log.d("RESPONDING", member.getRespondingTo());
                                    switch (member.getRespondingTo().toUpperCase()) {
                                        case "STATION":
                                            if (sectionAdapter.getSection("Station") == null) {
                                                sectionAdapter.addSection("Station", stationSection);
                                            }
                                            stationSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "SCENE":
                                            if (sectionAdapter.getSection("Scene") == null) {
                                                sectionAdapter.addSection("Scene", sceneSection);
                                            }
                                            sceneSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "NR":
                                            if (sectionAdapter.getSection("CR") == null) {
                                                sectionAdapter.addSection("CR", CRSection);
                                            }
                                            CRSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "CAN'T RESPOND":
                                            if (sectionAdapter.getSection("CR") == null) {
                                                sectionAdapter.addSection("CR", CRSection);
                                            }
                                            CRSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "AT STATION":
                                            if (sectionAdapter.getSection("At Station") == null) {
                                                sectionAdapter.addSection("At Station", atStationSection);
                                            }
                                            atStationSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        default:
                                            if (sectionAdapter.getSection("Unknown") == null) {
                                                sectionAdapter.addSection("Unknown", unknownSection);
                                            }
                                            unknownSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                    }
                                }
                            } else {
                                if (member.getIsResponding() && member.getRespondingTo() != null) {
                                    //Log.d("RESPONDING", member.getRespondingTo());
                                    switch (member.getRespondingTo().toUpperCase()) {
                                        case "STATION":
                                            if (sectionAdapter.getSection("Station") == null) {
                                                sectionAdapter.addSection("Station", stationSection);
                                            }
                                            stationSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "SCENE":
                                            if (sectionAdapter.getSection("Scene") == null) {
                                                sectionAdapter.addSection("Scene", sceneSection);
                                            }
                                            sceneSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "NR":
                                            if (sectionAdapter.getSection("CR") == null) {
                                                sectionAdapter.addSection("CR", CRSection);
                                            }
                                            CRSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "CAN'T RESPOND":
                                            if (sectionAdapter.getSection("CR") == null) {
                                                sectionAdapter.addSection("CR", CRSection);
                                            }
                                            CRSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        case "AT STATION":
                                            if (sectionAdapter.getSection("At Station") == null) {
                                                sectionAdapter.addSection("At Station", atStationSection);
                                            }
                                            atStationSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                        default:
                                            if (sectionAdapter.getSection("Unknown") == null) {
                                                sectionAdapter.addSection("Unknown", unknownSection);
                                            }
                                            unknownSection.addRow(member);
                                            sectionAdapter.notifyDataSetChanged();
                                            break;
                                    }
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

        departmentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Loading Finished.
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
                if (mRespondingConfirm) {
                    Snackbar snackbar = Snackbar.make(mParentLayout, "Respond to Station?", 10000).setAction("RESPOND", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setRespondingSelected(0, false);
                            updateRespondingStatus(0);
                        }
                    }).setActionTextColor(getResources().getColor(R.color.new_accent));
                    View view = snackbar.getView();
                    view.setBackgroundColor(getResources().getColor(R.color.snackbar));
                    ((TextView)view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.text_responding_name));
                    snackbar.show();
                } else {
                    setRespondingSelected(0, false);
                    updateRespondingStatus(0);
                }
                break;
            case R.id.responding_scene_layout:
                if (mRespondingConfirm) {
                    Snackbar snackbar = Snackbar.make(mParentLayout, "Respond to Scene?", 10000).setAction("RESPOND", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setRespondingSelected(1, false);
                            updateRespondingStatus(1);
                        }
                    }).setActionTextColor(getResources().getColor(R.color.new_accent));
                    View view = snackbar.getView();
                    view.setBackgroundColor(getResources().getColor(R.color.snackbar));
                    ((TextView)view.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(getResources().getColor(R.color.text_responding_name));
                    snackbar.show();
                } else {
                    setRespondingSelected(1, false);
                    updateRespondingStatus(1);
                }
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
        Intent locationService = new Intent(getActivity(), WLocationService.class);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);
        mIsResponding = true;
        switch (mRespondingSelected) {
            case 0:
                //Station
                /*Map<String, Object> respondingToStation = new HashMap<String, Object>();
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
                if (mIsLocationTrackingEnabled)
                    getActivity().startService(locationService);*/
                RespondingSystem.getInstance(getActivity()).respondStation();
                break;
            case 1:
                //Scene
                /*Map<String, Object> respondingToScene = new HashMap<String, Object>();
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
                if (mIsLocationTrackingEnabled)
                    getActivity().startService(locationService);*/
                RespondingSystem.getInstance(getActivity()).respondScene();
                break;
            case 2:
                //Can't Respond
                /*Map<String, Object> cantRespond = new HashMap<String, Object>();
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
                });*/
                RespondingSystem.getInstance(getActivity()).respondCant();
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                /*Map<String, Object> respondingReset = new HashMap<String, Object>();
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
                if (mIsLocationTrackingEnabled)
                    getActivity().stopService(locationService);*/
                RespondingSystem.getInstance(getActivity()).clearSelf();
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
                } else {
                    updateButtons(false);
                }
                Log.d("Responding", member.getRespondingTo() + member.getIsResponding());
                loadRespondingMembers(member.getDepartment());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
            if (member.getPositionColor() != null) {
                shapeDrawable.getPaint().setColor(Color.parseColor("#" + member.getPositionColor()));
            } else {
                shapeDrawable.getPaint().setColor(getResources().getColor(R.color.md_blue_500));
            }
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
                                .title(member.getName())
                                .items(menu)
                                .itemsGravity(GravityEnum.CENTER)
                                .titleColorRes(R.color.text_responding_name)
                                .backgroundColorRes(R.color.bottom_sheet_background)
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

        public void addRow(Member item) {
            this.list.add(item);
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