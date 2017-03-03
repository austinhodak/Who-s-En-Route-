package com.fireapps.firedepartmentmanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.javiersantos.appupdater.AppUpdater;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mukesh.permissions.AppPermissions;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavDrawerActivity extends AppCompatActivity implements View.OnClickListener, ConnectivityChangeListener {

    AccountHeader accountHeader;
    Drawer drawer;
    MenuItem incidentMenuItem;
    List<DataSnapshot> incidentList = new ArrayList<>();
    List<DataSnapshot> departmentMembers = new ArrayList<>();
    boolean isReady;
    private String TAG = "NavDrawerActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase, userRef;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    private Animation zoomIn;
    private Animation zoomOut;
    private String departmentID;
    private SharedPreferences sharedPreferences;
    private float elevation;
    private MediaPlayer mediaPlayer;
    private MenuItem playStreamMenu;
    private MenuItem pauseStreamMenu;
    private boolean mIsResponding;
    private DatabaseReference userReference;
    private FirebaseUser firebaseUser;
    private boolean mIsLocationTrackingEnabled;
    private Drawer secondDrawer;
    private MaterialDialog dialog;
    private Handler handler;
    private DataSnapshot currentMember;
    private HashMap<ProfileDrawerItem, DataSnapshot> departments = new HashMap<>(); //Garland, -------.
    private List<DataSnapshot> departmentsList = new ArrayList<>();
    private Fragment currentFragment;

    private ValueEventListener departmentListener;

    private MainActivityListener listener;

    float pxBottomMargin;
    private boolean mQuickRespondShortcutEnabled;

    public void setMainActivityListener(MainActivityListener mainListener) {
        this.listener = mainListener;
    }

    @BindView(R.id.home_connection)
    LinearLayout connectionLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;

    @BindView(R.id.bottom_sheet)
    View bottomSheet;

    @BindView(R.id.main_callInfo_complete)
    Button callInfoComplete;

    @BindView(R.id.main_callInfo_dismiss)
    Button callInfoDismiss;

    @BindView(R.id.bottom_incident_title)
    TextView bottomIncidentTitle;

    @BindView(R.id.bottom_incident_desc)
    TextView bottomIncidentDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        ButterKnife.bind(this);

        Log.d(TAG, "onCreate: START");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsLocationTrackingEnabled = sharedPreferences.getBoolean("pref_map_response_enable", false);
        mQuickRespondShortcutEnabled = sharedPreferences.getBoolean("pref_responding_shortcut_menu", false);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setSubtitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setSubtitle("Loading..");
        toolbar.setSubtitle(sharedPreferences.getString("selectedDepartmentAbbrv", null));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(0);
            appBarLayout.setElevation(0);
        }

        pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, this.getResources().getDisplayMetrics());

        setupFirebase();

        isReady = false;
        
        Set<String> strings = sharedPreferences.getStringSet("departmentIds", null);

        elevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        //appBarLayout.setElevation(Math.round(elevation));
        //toolbar.setElevation(Math.round(elevation));

        loadDepartment(RespondingSystem.getInstance(getApplicationContext()).getCurrentDepartmentKey());
        loadRespondingMembers(RespondingSystem.getInstance(getApplicationContext()).getCurrentDepartmentKey());


        createDrawer(savedInstanceState);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        //mapFab.setOnClickListener(this);
        callInfoDismiss.setOnClickListener(this);
        callInfoComplete.setOnClickListener(this);
        //mapFab.hide();

        zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.start();

        AppPermissions appPermissions = new AppPermissions(this);
        if (!appPermissions.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            appPermissions.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, 0);
        }

        handleIntent(getIntent());

        Log.d(TAG, "onCreate: FINISH");
    }

    private void setupFirebase() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            firebaseUser = user;
        } else {
            // No user is signed in
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    loadUserInformation(user);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    Log.d("TAG:)", "Refreshed token: " + refreshedToken);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        userReference = mDatabase.child("users/" + firebaseUser.getUid());
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equals(Constants.ACTION_RESPONDING_STATION)) {
            //RESPONDING TO STATION
            updateRespondingStatus(0);
        } else if (action.equals(Constants.ACTION_RESPONDING_SCENE)) {
            //RESPONDING TO SCENE
            updateRespondingStatus(1);
        } else if (action.equals(Constants.ACTION_RESPONDING_CR)) {
            //RESPONDING TO CR
            updateRespondingStatus(2);
        }
    }

    private void updateRespondingStatus(int mRespondingSelected) {
        Intent locationService = new Intent(this, WLocationService.class);
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
                    }
                });
                if (mIsLocationTrackingEnabled)
                    startService(locationService);
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
                    }
                });
                if (mIsLocationTrackingEnabled)
                    startService(locationService);
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
                if (mIsLocationTrackingEnabled)
                    stopService(locationService);
                break;
        }
    }

    private void createDrawer(Bundle savedInstanceState) {
            accountHeader = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withSavedInstance(savedInstanceState)
                    .addProfiles(
                            new ProfileDrawerItem().withName("Loading..").withEmail("Loading..").withIdentifier(0).withIcon(getResources().getDrawable(R.drawable.departmentlogo))
                            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                @Override
                                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                    FirebaseAuth.getInstance().signOut();
                                    finish();
                                    Intent intent = new Intent(NavDrawerActivity.this, LoginDispatch.class);
                                    startActivity(intent);
                                    return false;
                                }
                            })
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                            Log.d(TAG, "onProfileChanged: " + profile);
                            for (int i=0; i < departmentsList.size(); i++) {
                                if (departmentsList.get(i).child("name").getValue().toString() == profile.getEmail().toString()) {
                                    RespondingSystem.getInstance(NavDrawerActivity.this).setSelectedDepartment(departmentsList.get(i).getKey());

                                    mDatabase.child("departments/" + departmentID).removeEventListener(departmentListener);
                                    loadDepartment(departmentsList.get(i).getKey());
                                    loadRespondingMembers(departmentsList.get(i).getKey());

                                    try {
                                        if (currentFragment != null){
                                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                            ft.detach(currentFragment).attach(currentFragment).commit();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    toolbar.setSubtitle(departmentsList.get(i).child("abbrv").getValue().toString());
                                    sharedPreferences.edit().putString("selectedDepartmentAbbrv", departmentsList.get(i).child("abbrv").getValue().toString()).apply();
                                }
                            }
                            return false;
                        }
                    })
                    .build();

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            Glide.with(this).load(R.drawable.wallnight).centerCrop().crossFade().into(accountHeader.getHeaderBackgroundView());
        } else {
            Glide.with(this).load(R.drawable.wall).crossFade().into(accountHeader.getHeaderBackgroundView());
        }

        PrimaryDrawerItem respondingItem = new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(R.drawable.radio).withIconTintingEnabled(false);
        PrimaryDrawerItem incidentItem = new PrimaryDrawerItem().withName(R.string.drawer_item_incident).withIcon(R.drawable.firealarm).withIconTintingEnabled(false);
        PrimaryDrawerItem apparatusItem = new PrimaryDrawerItem().withName(R.string.drawer_item_apparatus).withIcon(R.drawable.firetruck).withIconTintingEnabled(false);
        PrimaryDrawerItem mapItem = new PrimaryDrawerItem().withName(R.string.drawer_item_map).withIcon(R.drawable.globe).withIconTintingEnabled(false);
        PrimaryDrawerItem availablityItem = new PrimaryDrawerItem().withName("Availability").withIcon(R.drawable.clock);
        PrimaryDrawerItem scannerItem = new PrimaryDrawerItem().withName("Scanner").withIcon(R.drawable.walkietalkie).withIconTintingEnabled(false);

        ExpandableDrawerItem message_incidentItem = new ExpandableDrawerItem().withIdentifier(1001).withName("Messages • Incidents").withSelectable(false).withIcon(R.drawable.firealarm).withIconTintingEnabled(false).withSubItems(
                new SecondaryDrawerItem().withName("Incidents").withIcon(R.drawable.documnet).withIconTintingEnabled(false),
                new SecondaryDrawerItem().withName("Messages").withIcon(R.drawable.message).withIdentifier(2).withIconTintingEnabled(false)
        );

        PrimaryDrawerItem membersList = new PrimaryDrawerItem().withName("Members").withIcon(R.drawable.people).withIconTintingEnabled(false);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withSavedInstance(savedInstanceState)
                .addDrawerItems(
                        respondingItem.withIdentifier(0),
                        mapItem.withIdentifier(1),
                        message_incidentItem,
                        apparatusItem.withIdentifier(3),
                        scannerItem.withIdentifier(5),
                        //availablityItem.withIdentifier(4),
                        new SectionDrawerItem().withName("Department Functions"),
                        membersList.withIdentifier(6),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withSelectable(false).withName(R.string.drawer_item_settings).withIcon(R.drawable.settings).withIdentifier(10)
                        //new SecondaryDrawerItem().withSelectable(false).withName("Signup Test").withIdentifier(11)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        if (drawerItem.getIdentifier() == 10) {
                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivityForResult(intent, 1001);
                        } else {
                            updateFragment((int) drawerItem.getIdentifier());
                        }
                        return false;
                    }
                })
                .build();

        getDepartments();

        if (savedInstanceState == null) {
            if(getIntent().getAction() == "stream") {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ScannerFragment fragment = new ScannerFragment();
                fragmentTransaction.replace(R.id.frame_layout, fragment);
                fragmentTransaction.commit();
                currentFragment = fragment;
                drawer.setSelection(5, false);
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            WhosEnRouteFragment fragment = new WhosEnRouteFragment();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();
            currentFragment = fragment;

            Log.d(TAG, "SavedInstanceState: null");
        }

        //final List<String> stream = new ArrayList<>();
        final List<String> streamURLS = new ArrayList<>();
        final List<String> streamLocations = new ArrayList<>();
        final List<String> streamTitles = new ArrayList<>();
        final List<IDrawerItem> streamDrawerItems = new ArrayList<>();

        final ExpandableDrawerItem expandableDrawerItem = new ExpandableDrawerItem().withName("Radio Streams").withSelectable(false).withIcon(R.drawable.walkietalkie).withIdentifier(1999).withSubItems(streamDrawerItems);

        secondDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Call Dispatch").withIcon(R.drawable.headset).withIconTintingEnabled(false).withIdentifier(102).withSelectable(false),
                        expandableDrawerItem)
                .addStickyDrawerItems(
                        new PrimaryDrawerItem().withName("Courtesy Message").withIcon(R.drawable.message).withIconTintingEnabled(false).withIdentifier(101).withSelectable(false)
                        //new SectionDrawerItem().withName("Radio Streams"),
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch ((int)drawerItem.getIdentifier()) {
                            case 102:
                                break;
                        }
                        return false;
                    }
                })
                .withDrawerGravity(Gravity.END)
                .withSelectedItem(-1)
                .append(drawer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        handler = new Handler();

        mDatabase.child("departments/"+RespondingSystem.getInstance(this).getCurrentDepartmentKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String countyID = dataSnapshot.child("county").getValue().toString();
                    mDatabase.child("counties/"+countyID+"/streams").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, final String s) {
                            String location = dataSnapshot.child("location").getValue().toString();
                            String name = dataSnapshot.child("name").getValue().toString();
                            String url = dataSnapshot.child("url").getValue().toString();

                            //stream.add(name + " - " + location);
                            streamURLS.add(url);
                            streamLocations.add(location);
                            streamTitles.add(name);

                            final boolean[] isPlaying = {false};

                            streamDrawerItems.add(new SwitchDrawerItem().withName(name).withSelectable(false).withDescription(location).withIdentifier(2000 + streamURLS.size()).withOnCheckedChangeListener(new OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(final IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        drawerItem.withEnabled(false);
                                        secondDrawer.updateItem(drawerItem);
                                        AudioStreamService.getInstance(NavDrawerActivity.this).setStreamListener(new AudioStreamService.StreamListener() {
                                            @Override
                                            public void onStreamLoaded(String streamURL, String streamName, MediaPlayer stream) {
                                                drawerItem.withEnabled(true);
                                                secondDrawer.updateItem(drawerItem);
                                                secondDrawer.updateName(drawerItem.getIdentifier(), new StringHolder(streamName + " (✓)"));
                                                isPlaying[0] = true;
                                            }

                                            @Override
                                            public void onStreamStopped(String streamURL) {
                                                drawerItem.withEnabled(true);
                                                secondDrawer.updateItem(drawerItem);
                                                secondDrawer.updateName(drawerItem.getIdentifier(), new StringHolder(streamTitles.get(streamURLS.indexOf(streamURL)) + " - Stopped"));
                                                isPlaying[0] = false;
                                            }

                                            @Override
                                            public void onStreamPaused(String streamURL) {
                                                drawerItem.withEnabled(true);
                                                secondDrawer.updateItem(drawerItem);
                                                secondDrawer.updateName(drawerItem.getIdentifier(), new StringHolder(streamTitles.get(streamURLS.indexOf(streamURL)) + " - Paused"));
                                                isPlaying[0] = false;
                                            }

                                            @Override
                                            public void onStreamResumed(String streamURL) {
                                                drawerItem.withEnabled(true);
                                                secondDrawer.updateItem(drawerItem);
                                                secondDrawer.updateName(drawerItem.getIdentifier(), new StringHolder(streamTitles.get(streamURLS.indexOf(streamURL)) + " (✓)"));
                                                isPlaying[0] = true;
                                            }
                                        });
                                        try {
                                            AudioStreamService.getInstance(NavDrawerActivity.this).startStream(streamURLS.get(streamDrawerItems.indexOf(drawerItem)), streamTitles.get(streamDrawerItems.indexOf(drawerItem)), streamLocations.get(streamDrawerItems.indexOf(drawerItem)));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        if (!isPlaying[0]) {

                                        } else {
                                            AudioStreamService.getInstance(NavDrawerActivity.this).stopStream(streamURLS.get(streamDrawerItems.indexOf(drawerItem)));
                                            secondDrawer.updateName(drawerItem.getIdentifier(), new StringHolder(streamTitles.get(streamDrawerItems.indexOf(drawerItem))));
                                        }
                                    }
                                }
                            }));

                            secondDrawer.updateItem(expandableDrawerItem);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            startActivity(new Intent(this, LoginDispatch.class));
            finish();
        }
    }

    private void updateFragment(int identifier) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ColorDrawable redColor = new ColorDrawable(getResources().getColor(R.color.newRed));
        ColorDrawable greyBlueColor = new ColorDrawable(getResources().getColor(R.color.accent));
        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{redColor, greyBlueColor});

        TransitionDrawable normal = new TransitionDrawable(new Drawable[]{greyBlueColor, redColor});

        switch (identifier) {
            case 0:
                WhosEnRouteFragment respondingFrag = new WhosEnRouteFragment();
                fragmentTransaction.replace(R.id.frame_layout, respondingFrag, "responding");
                fragmentTransaction.commit();
                currentFragment = respondingFrag;
                break;
            case 1:
                MapFragment mapFrag = new MapFragment();
                fragmentTransaction.replace(R.id.frame_layout, mapFrag, "map");
                fragmentTransaction.commit();
                currentFragment = mapFrag;
                break;
            case 2:
                IncidentListFragment incidentListFragment = new IncidentListFragment();
                fragmentTransaction.replace(R.id.frame_layout, incidentListFragment, "incidentList");
                fragmentTransaction.commit();
                currentFragment = incidentListFragment;
                break;
            case 3:
                ApparatusListFragment apparatusListFragment = new ApparatusListFragment();
                fragmentTransaction.replace(R.id.frame_layout, apparatusListFragment, "apparatusList");
                fragmentTransaction.commit();
                setTitle("Apparatus");
                currentFragment = apparatusListFragment;
                break;
            case 4:
                AvailablityFragment availablityFragment = new AvailablityFragment();
                fragmentTransaction.replace(R.id.frame_layout, availablityFragment, "availability");
                fragmentTransaction.commit();
                currentFragment = availablityFragment;
                break;
            case 5:
                ScannerFragment scannerFragment = new ScannerFragment();
                fragmentTransaction.replace(R.id.frame_layout, scannerFragment, "scanner");
                fragmentTransaction.commit();
                setTitle("Scanner");
                currentFragment = scannerFragment;
                break;
            case 6:
                MemberFragment membersListFragment = new MemberFragment();
                fragmentTransaction.replace(R.id.frame_layout, membersListFragment, "memberFragment");
                fragmentTransaction.commit();
                setTitle("Members");
                currentFragment = membersListFragment;
                break;
            case 11:
                Intent signup = new Intent(this, FirstSignupActivity.class);
                startActivity(signup);
                break;
            case 101:
                //Courtesy Message
                showCourtesyPopup();
                break;
            case 1001:
                break;
            default:
                WhosEnRouteFragment fragment = new WhosEnRouteFragment();
                fragmentTransaction.replace(R.id.frame_layout, fragment, "responding");
                fragmentTransaction.commit();
                currentFragment = fragment;
                break;
        }

//        try {
//            if (identifier != 1 && mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
//                Log.d("FAB", "SHOW");
//                mapFab.show();
//            } else {
//                mapFab.hide();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        /*if (identifier == 1) {
            mapFab.hide();
        } else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            mapFab.hide();
        } else {
            mapFab.show();
        }*/

        if (listener != null)
        listener.onBottomSheetChanged(mBottomSheetBehavior.getState());
    }

    String[] array = new String[]{"Mom", "Wife", "Kid"};

    int courtesyCountdown = 10;
    private void showCourtesyPopup() {

        dialog = new MaterialDialog.Builder(this)
                .title("Courtesy Message")
                .items(array)
                .widgetColorRes(R.color.primary)
                .neutralColorRes(R.color.primary)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        return true;
                    }
                })
                .positiveText("SEND")
                .negativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                    }
                })
                .show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                handler.removeCallbacks(runnable);
            }
        });
        courtesyCountdown = 10;
        runnable.run();
        //secondDrawer.closeDrawer();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            courtesyCountdown--;
            if (courtesyCountdown > 0) {
                dialog.setActionButton(DialogAction.POSITIVE, "Sending In " + courtesyCountdown + " Seconds");
                handler.postDelayed(runnable, 1000);
            } else if (courtesyCountdown == 0) {
                dialog.setActionButton(DialogAction.POSITIVE, "Sending...");
                dialog.dismiss();
                handler.removeCallbacks(runnable);
            }
        }
    };

    private void loadUserInformation(FirebaseUser user) {
        final ProfileDrawerItem profileDrawerItem = (ProfileDrawerItem) accountHeader.getActiveProfile();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                final Member member = dataSnapshot.getValue(Member.class);
                if (member != null) {

                    if (member.getDepartment() != null) {
                        departmentID = member.getDepartment();
                        sharedPreferences.edit().putString("departmentID", departmentID).apply();
                        sharedPreferences.edit().putString("memberKey", dataSnapshot.getKey()).apply();
                    }

                    Log.d(TAG, "onDataChange: USER LOADED");

                    try {
                        List<IProfile> profiles = accountHeader.getProfiles();
                        for (int i = 0; i < profiles.size(); i++) {
                            IProfile profile = profiles.get(i);
                            profile.withName(dataSnapshot.child("name").getValue().toString());
                            accountHeader.updateProfile(profile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    currentMember = dataSnapshot;

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.child("users/" + user.getUid()).addValueEventListener(postListener);
    }

    private void loadDepartment(final String departmentID) {
        departmentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Department department = dataSnapshot.getValue(Department.class);
                if (department != null) {
                    checkForIncidents(department, departmentID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.child("departments/" + departmentID).addValueEventListener(departmentListener);
    }

    private void checkForIncidents(Department department, String departmentID) {
        Log.d("Incident", department.isActiveIncident() + "");
        if (department.isActiveIncident() && RespondingSystem.getInstance(this).getCurrentDepartmentKey() == departmentID) {
            //Load then Show.
            bottomSheet.setVisibility(View.VISIBLE);

            //Currently Active Incident, Grab Info Show Sheet
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mBottomSheetBehavior.setHideable(false);


            frameLayout.setPadding(0, 0, 0, Math.round(pxBottomMargin));

            loadActiveIncidents(department, departmentID);

            if (drawer.getCurrentSelection() != 1) {
                //mapFab.show();
                //mapFab.setVisibility(View.VISIBLE);
            }
            if (listener != null)
            listener.onBottomSheetChanged(mBottomSheetBehavior.getState());
        } else {
/*            mapFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);

                }
            });*/
            mBottomSheetBehavior.setHideable(true);
            try {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } catch (Exception e) {
                e.printStackTrace();
            }
            frameLayout.setPadding(0, 0, 0, 0);
            if (listener != null)
                listener.onBottomSheetChanged(mBottomSheetBehavior.getState());
        }
    }

    public int getBottomSheet() {
        return mBottomSheetBehavior.getState();
    }

    private void loadActiveIncidents(Department department, String departmentID) {
        Query departmentRef = mDatabase.child("departments").child(departmentID).child("messages").limitToLast(1);
        departmentRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Query query = mDatabase.child("messages").child(dataSnapshot.getKey());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot messageSnapshot) {
                        final Message incident = messageSnapshot.getValue(Message.class);

                        try {
                            for (DataSnapshot object : incidentList) {
                                if (object.getKey().equals(messageSnapshot.getKey())) {
                                    incidentList.remove(object);
                                }
                            }
                        } catch (Exception e) {

                        } finally {
                            if (incident.isActiveIncident()) {
                                updateIncidentInfo(incident, messageSnapshot);
                                incidentList.add(messageSnapshot);
                            } else {
                                //INCIDENT NOT ACTIVE/NO RUSH
                            }
                            Log.d("Message: ", incident.getType() + " " + incident.isActiveIncident());
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

    private void updateIncidentInfo(Message incident, DataSnapshot dataSnapshot) {
        String desc = incident.getMessage();
        desc = desc.replace("\r\n", " ").replace("\n", " ");
        bottomIncidentTitle.setText(incident.getType());
        bottomIncidentDesc.setText(desc);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_incident_main:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //mapFab.show();
                mBottomSheetBehavior.setHideable(false);
                mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            incidentMenuItem.setVisible(false);
                            mBottomSheetBehavior.setBottomSheetCallback(null);
                        }
                        if (listener != null)
                        listener.onBottomSheetChanged(mBottomSheetBehavior.getState());
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
                frameLayout.setPadding(0, 0, 0, Math.round(pxBottomMargin));
                if (listener != null)
                listener.onBottomSheetChanged(mBottomSheetBehavior.getState());
                break;
            case R.id.menu_location_get:
                Intent intent = new Intent(this, WLocationService.class);
                startService(intent);
                break;
            case R.id.menu_location_stop:
                stopService(new Intent(this, WLocationService.class));
                break;
            case R.id.menu_responding_clear:
                resetMembersResponding();
                break;
            case R.id.menu_stream_play:
                Intent intent1 = new Intent(this, AudioFeedService.class);
                startService(intent1);
                break;
            case R.id.menu_stream_pause:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_callInfo_dismiss:
                /*mapFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);

                    }
                });*/
                mBottomSheetBehavior.setHideable(true);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                incidentMenuItem.setVisible(true);
                frameLayout.setPadding(0, 0, 0, 0);

                if (listener != null)
                    listener.onBottomSheetChanged(mBottomSheetBehavior.getState());
                break;
            case R.id.map_fab:
                if (drawer.getCurrentSelection() != 1)
                    drawer.setSelection(1);
                break;
            case R.id.main_callInfo_complete:
                if (!isReady) {
                    Toast.makeText(this, "Please try again. Still loading.", Toast.LENGTH_SHORT).show();
                    return;
                }
                CharSequence[] items = {"Clear & Complete", "Clear & Fill Details", "Only Clear Responding", "Only Clear Incident"};
                new MaterialDialog.Builder(this)
                        .title("Complete Incident")
                        .titleGravity(GravityEnum.CENTER)
                        .items(items)
                        .itemsGravity(GravityEnum.CENTER)
                        .itemsColorRes(R.color.text_responding_name)
                        .backgroundColorRes(R.color.bottom_sheet_background)
                        .widgetColor(getResources().getColor(R.color.new_accent))
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        resetMembersResponding();
                                        completeIncident();
                                        break;
                                    case 1:
                                        break;
                                    case 2:
                                        resetMembersResponding();
                                        break;
                                    case 3:
                                        completeIncident();
                                        break;
                                }
                            }
                        })
                        .negativeText("Cancel")
                        .show();
                break;
        }
    }

    private void completeIncident() {
        Map<String, Object> updates = new HashMap<>();

        for (DataSnapshot postSnapshot : incidentList) {
            updates.put("messages/" + postSnapshot.getKey() + "/activeIncident", false);
        }

        updates.put("departments/" + RespondingSystem.getInstance(getApplicationContext()).getCurrentDepartmentKey() + "/activeIncident", false);

        mDatabase.updateChildren(updates);
    }

    private void resetMembersResponding() {
        Map<String, Object> updates = new HashMap<>();

        for (DataSnapshot postSnapshot : departmentMembers) {
            updates.put(postSnapshot.getKey() + "/respondingTo", "");
            updates.put(postSnapshot.getKey() + "/isResponding", false);
            updates.put(postSnapshot.getKey() + "/respondingFromLoc", null);
            updates.put(postSnapshot.getKey() + "/respondingTime", "");
            updates.put(postSnapshot.getKey() + "/location/currentLat", "");
            updates.put(postSnapshot.getKey() + "/location/currentLon", "");
            updates.put(postSnapshot.getKey() + "/respondingAgency", "");
            updates.put(postSnapshot.getKey() + "/on_scene", null);
        }

        mDatabase.child("users").updateChildren(updates);
    }

    private void loadRespondingMembers(String department) {
        departmentMembers.clear();
        DatabaseReference departmentRef = mDatabase.child("departments").child(department).child("members");
        departmentRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mDatabase.child("users").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Member member = dataSnapshot.getValue(Member.class);
                        isReady = true;
                        try {
                            for (DataSnapshot object : departmentMembers) {
                                if (object.getKey().equals(dataSnapshot.getKey())) {
                                    departmentMembers.remove(object);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            departmentMembers.add(dataSnapshot);
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
    protected void onResume() {
        super.onResume();
//        if (drawer.getCurrentSelection() != 0) {
//            appBarLayout.setElevation(Math.round(elevation));
//            toolbar.setElevation(Math.round(elevation));
//        } else {
//            appBarLayout.setElevation(0);
//            toolbar.setElevation(0);
//        }
//        Log.d("DRAWER", "" + drawer.getCurrentSelection());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState = drawer.saveInstanceState(outState);
//        outState = accountHeader.saveInstanceState(outState);
//        outState.putString("title", (String) toolbar.getTitle());
//        outState.putString("titleColor", sharedPreferences.getString("hometoolbarcolor", "#E51C23"));
//        outState.putInt("statusColor", getWindow().getStatusBarColor());
        super.onSaveInstanceState(outState);
    }

    public void getDepartments() {
        Log.d(TAG, "getDepartments: ");
        if (currentMember == null) {
            RespondingSystem.getInstance(getApplicationContext()).loadInitialData().setDepartmentListener(new RespondingSystem.DepartmentListener() {
                @Override
                public void onDepartmentAdded(DataSnapshot snapshot, Uri icon) {

                    try {
                        Member member = null;
                        try {
                            member = currentMember.getValue(Member.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        accountHeader.removeProfileByIdentifier(0);

                        final ProfileDrawerItem profileDrawerItem1 = new ProfileDrawerItem().withIcon(R.drawable.default_fdicon).withName(RespondingSystem.getInstance(getApplicationContext()).getUserName()).withEmail(snapshot.child("name").getValue().toString());
                        accountHeader.addProfile(profileDrawerItem1, accountHeader.getProfiles().size());

                        departmentsList.add(snapshot);

//                    Glide.with(getApplicationContext()).load(icon).asBitmap().into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                            profileDrawerItem1.withIcon(resource);
//                            accountHeader.updateProfile(profileDrawerItem1);
//                        }
//                    });

                        if (RespondingSystem.getInstance(getApplicationContext()).getCurrentDepartmentKey().equals(snapshot.getKey())) {
                            accountHeader.setActiveProfile(profileDrawerItem1, true);
                            Log.d(TAG, "onDepartmentAdded: Selected Department" + profileDrawerItem1.getEmail());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDepartmentUpdated(DataSnapshot snapshot) {

                }

                @Override
                public void onLoadingFinished() {
                    Log.d(TAG, "onLoadingFinished: ");
                }

                @Override
                public void onDepartmentAddedFirstTime(DataSnapshot snapshot) {
                    try {
                        Member member = null;
                        try {
                            member = currentMember.getValue(Member.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        accountHeader.removeProfileByIdentifier(0);

                        final ProfileDrawerItem profileDrawerItem1 = new ProfileDrawerItem().withIcon(R.drawable.default_fdicon).withName(RespondingSystem.getInstance(getApplicationContext()).getUserName()).withEmail(snapshot.child("name").getValue().toString());
                        accountHeader.addProfile(profileDrawerItem1, accountHeader.getProfiles().size());

                        departmentsList.add(snapshot);
                        accountHeader.setActiveProfile(profileDrawerItem1, true);

//                    Glide.with(getApplicationContext()).load(icon).asBitmap().into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                            profileDrawerItem1.withIcon(resource);
//                            accountHeader.updateProfile(profileDrawerItem1);
//                        }
//                    });

//                        if (RespondingSystem.getInstance(getApplicationContext()).getCurrentDepartmentKey().equals(snapshot.getKey())) {
//                            accountHeader.setActiveProfile(profileDrawerItem1, true);
//                            Log.d(TAG, "onDepartmentAdded: Selected Department" + profileDrawerItem1.getEmail());
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        if (event.getState() == ConnectivityState.CONNECTED) {
            //Connection Present
            connectionLayout.setVisibility(View.GONE);
        } else {
            //No Connection
            connectionLayout.setVisibility(View.VISIBLE);
        }
    }

    public interface MainActivityListener {
        public void onBottomSheetChanged(int bottomSheet);
    }


}
