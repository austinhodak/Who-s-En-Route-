package com.fireapps.firedepartmentmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.porokoro.paperboy.PaperboyFragment;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavDrawerActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "NavDrawerActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Toolbar toolbar;
    AccountHeader accountHeader;
    Drawer drawer;
    private DatabaseReference mDatabase, userRef;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    FloatingActionButton mapFab;
    MenuItem incidentMenuItem;
    Button callInfoDismiss, callInfoComplete;
    private Animation zoomIn;
    private Animation zoomOut;
    List<DataSnapshot> incidentList;
    List<DataSnapshot> departmentMembers;
    private TextView bottomIncidentTitle, bottomIncidentDesc;
    boolean isReady;
    private String departmentID;
    private View bottomSheet;
    FrameLayout frameLayout;
    com.miguelcatalan.materialsearchview.MaterialSearchView searchView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);

        isReady = false;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setSubtitleTextColor(Color.parseColor("#FFFFFF"));
        /*toolbar.setTitle("MVA - ACCIDENT");
        toolbar.setSubtitle("MAIN (PIT) ST/ROUTE 6");
        setTitle("MVA - ACCIDENT");*/

        incidentList = new ArrayList<>();
        departmentMembers = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        createDrawer(savedInstanceState);

        frameLayout = (FrameLayout) findViewById(R.id.frame_layout);

        bottomSheet = findViewById( R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mapFab = (FloatingActionButton) findViewById(R.id.map_fab);
        mapFab.setOnClickListener(this);
        callInfoDismiss = (Button) findViewById(R.id.main_callInfo_dismiss);
        callInfoDismiss.setOnClickListener(this);
        callInfoComplete = (Button) findViewById(R.id.main_callInfo_complete);
        callInfoComplete.setOnClickListener(this);

        zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        zoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);

        bottomIncidentTitle = (TextView) findViewById(R.id.bottom_incident_title);
        bottomIncidentDesc = (TextView) findViewById(R.id.bottom_incident_desc);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    loadUserInformation(user);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    FirebaseMessaging.getInstance().subscribeToTopic("incidents");

                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    Log.d("TAG:)", "Refreshed token: " + refreshedToken);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.start();

        Log.d("WER", getResources().getDisplayMetrics().density + "");
    }

    private void createDrawer(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            accountHeader = new AccountHeaderBuilder()
                    .withActivity(this)
                    .addProfiles(
                            new ProfileDrawerItem().withName("Loading..").withEmail("Loading..").withIcon(getDrawable(R.drawable.departmentlogo))
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();
        } else {
            accountHeader = new AccountHeaderBuilder()
                    .withActivity(this)
                    .addProfiles(
                            new ProfileDrawerItem().withName("Loading..").withEmail("Loading..").withIdentifier(0).withIcon(getResources().getDrawable(R.drawable.departmentlogo))
                    )
                    .build();
        }

        Glide.with(this).load(R.drawable.wall).crossFade().into(accountHeader.getHeaderBackgroundView());

        PrimaryDrawerItem respondingItem = new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(R.drawable.radio).withIconTintingEnabled(false);
        PrimaryDrawerItem incidentItem = new PrimaryDrawerItem().withName(R.string.drawer_item_incident).withIcon(R.drawable.firealarm).withIconTintingEnabled(false);
        PrimaryDrawerItem apparatusItem = new PrimaryDrawerItem().withName(R.string.drawer_item_apparatus).withIcon(R.drawable.firetruck).withIconTintingEnabled(false);
        PrimaryDrawerItem mapItem = new PrimaryDrawerItem().withName(R.string.drawer_item_map).withIcon(R.drawable.globe).withIconTintingEnabled(false);
        PrimaryDrawerItem availablityItem = new PrimaryDrawerItem().withName("Availability").withIcon(R.drawable.clock);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        respondingItem.withIdentifier(0),
                        mapItem.withIdentifier(1),
                        incidentItem.withIdentifier(2),
                        apparatusItem.withIdentifier(3),
                        //availablityItem.withIdentifier(4),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withSelectable(false).withName(R.string.drawer_item_settings).withIdentifier(10)
                        //new SecondaryDrawerItem().withName("Changelog").withIdentifier(11)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        if (drawerItem.getIdentifier() == 10) {
                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(intent);
                        }
                        updateFragment((int) drawerItem.getIdentifier());
                        return false;
                    }
                })
                .build();

        if (savedInstanceState == null) {
            // Do your oncreate stuff because there is no bundle
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            WhosEnRouteFragment fragment = new WhosEnRouteFragment();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();

            Log.d(TAG, "SavedInstanceState: null");
        }
    }

    private void updateFragment(int identifier) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (identifier) {
            case 0:
                WhosEnRouteFragment respondingFrag = new WhosEnRouteFragment();
                fragmentTransaction.replace(R.id.frame_layout, respondingFrag, "responding");
                fragmentTransaction.commit();
                break;
            case 1:
                MapFragment mapFrag = new MapFragment();
                fragmentTransaction.replace(R.id.frame_layout, mapFrag, "map");
                fragmentTransaction.commit();
                break;
            case 2:
                IncidentListFragment incidentListFragment = new IncidentListFragment();
                fragmentTransaction.replace(R.id.frame_layout, incidentListFragment, "incidentList");
                fragmentTransaction.commit();
                break;
            case 3:
                ApparatusListFragment apparatusListFragment = new ApparatusListFragment();
                fragmentTransaction.replace(R.id.frame_layout, apparatusListFragment, "apparatusList");
                fragmentTransaction.commit();
                setTitle("Apparatus");
                break;
            case 4:
                AvailablityFragment availablityFragment = new AvailablityFragment();
                fragmentTransaction.replace(R.id.frame_layout, availablityFragment, "availability");
                fragmentTransaction.commit();
                break;
            case 11:
                //setTitle("Changelog");

                //PaperboyFragment changelogFragment = new PaperboyFragment();
                //fragmentTransaction.replace(R.id.frame_layout, changelogFragment, "changelog");
                //fragmentTransaction.commit();
            default:
                WhosEnRouteFragment fragment = new WhosEnRouteFragment();
                fragmentTransaction.replace(R.id.frame_layout, fragment, "responding");
                fragmentTransaction.commit();
                break;
        }

        if (identifier != 1 && mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            Log.d("FAB", "SHOW");
            mapFab.show();
        } else {
            mapFab.hide();
        }
    }

    private void loadUserInformation(FirebaseUser user) {
        final ProfileDrawerItem profileDrawerItem = (ProfileDrawerItem) accountHeader.getActiveProfile();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Member member = dataSnapshot.getValue(Member.class);
                if (member != null) {
                    profileDrawerItem.withEmail(member.getEmail());
                    profileDrawerItem.withName(member.getName());
                    accountHeader.updateProfile(profileDrawerItem);

                    departmentID = member.getDepartment();
                    sharedPreferences.edit().putString("departmentID", departmentID).apply();
                    sharedPreferences.edit().putString("memberKey", dataSnapshot.getKey()).apply();

                    loadDepartment(member.getDepartment());
                    loadRespondingMembers(member.getDepartment());

                    Application application = ((Application)getApplicationContext());
                    application.setCurrentMember(member);
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
        ValueEventListener postListener = new ValueEventListener() {
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
        mDatabase.child("departments/" + departmentID).addValueEventListener(postListener);
    }

    private void checkForIncidents(Department department, String departmentID) {
        Log.d("Incident", department.isActiveIncident() + "");
        if (department.isActiveIncident()) {
            bottomSheet.setVisibility(View.VISIBLE);

            //Currently Active Incident, Grab Info Show Sheet
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


            Resources r = getResources();
            float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, r.getDisplayMetrics());

            frameLayout.setPadding(0,0,0,Math.round(pxBottomMargin));

            loadActiveIncidents(department, departmentID);

            if (drawer.getCurrentSelection() != 1) {
                mapFab.show();
                mapFab.setVisibility(View.VISIBLE);
            }
        } else {
            mapFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    mBottomSheetBehavior.setHideable(true);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    frameLayout.setPadding(0,0,0,0);
                }
            });
        }
    }

    private void loadActiveIncidents(Department department, String departmentID) {
        DatabaseReference departmentRef = mDatabase.child("departments").child(departmentID).child("messages");
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        incidentMenuItem = menu.findItem(R.id.menu_incident_main);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_incident_main:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mapFab.show();
                mBottomSheetBehavior.setHideable(false);
                mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            incidentMenuItem.setVisible(false);
                            mBottomSheetBehavior.setBottomSheetCallback(null);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_callInfo_dismiss:
                mapFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        mBottomSheetBehavior.setHideable(true);
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                        incidentMenuItem.setVisible(true);
                    }
                });
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
                        .itemsColor(getResources().getColor(R.color.bottom_sheet_title))
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
        Map<String, Object> updates = new HashMap<String, Object>();

        for (DataSnapshot postSnapshot : incidentList) {
            updates.put("messages/" + postSnapshot.getKey() + "/activeIncident", false);
        }

        updates.put("departments/" + departmentID + "/activeIncident", false);

        mDatabase.updateChildren(updates);
    }

    private void resetMembersResponding() {
        Map<String, Object> updates = new HashMap<String, Object>();

        for (DataSnapshot postSnapshot : departmentMembers) {
            updates.put(postSnapshot.getKey() + "/respondingTo", "");
            updates.put(postSnapshot.getKey() + "/isResponding", false);
            updates.put(postSnapshot.getKey() + "/respondingFromLoc", null);
            updates.put(postSnapshot.getKey() + "/respondingTime", "");
            updates.put(postSnapshot.getKey() + "/location/currentLat", "");
            updates.put(postSnapshot.getKey() + "/location/currentLon", "");
        }

        mDatabase.child("users").updateChildren(updates);
    }

    private void loadRespondingMembers(String department) {
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
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}
