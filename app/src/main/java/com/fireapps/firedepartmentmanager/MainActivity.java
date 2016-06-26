package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gigamole.library.navigationtabstrip.NavigationTabStrip;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.pubnub.api.Pubnub;

import br.com.mauker.materialsearchview.MaterialSearchView;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener, RespondingFragment.OnMemberChanged, RespondingFragment.OnResponded {

    @Bind(R.id.toolbar_main)
    Toolbar toolbar;
    String respondingTo = "";
    Drawer drawer;
    private GoogleApiClient mGoogleApiClient;
    private Pubnub mPubnub;
    private RespondingFragment respondingFragment;
    private CallInfoFragment callInfoFragment;
    private SharedPreferences sharedPreferences;
    private MainTabs mainTabs;

    public static MaterialSearchView searchView = null;

    @Override
    public void onBackPressed() {
        if (searchView.isOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DatabaseReference usersRef = database.getReference("users");

        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (getIntent() != null) {
            try {
                assert getIntent() != null;
                respondingTo = getIntent().getExtras().getString("RT");
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

        attachFragment(savedInstanceState);

        BadgeStyle badgeStyle = new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary);

        //All Drawer Items for All Modules
        final PrimaryDrawerItem apparatus = new PrimaryDrawerItem().withName("Apparatus").withIconTintingEnabled(true);
        final PrimaryDrawerItem apparatusInspection = new PrimaryDrawerItem().withName("Apparatus Inspections").withIconTintingEnabled(true).withBadge("2 Due").withBadgeStyle(badgeStyle);
        final PrimaryDrawerItem equipmentInventory = new PrimaryDrawerItem().withName("Equipment Inventory").withIconTintingEnabled(true);

        final PrimaryDrawerItem incidentList = new PrimaryDrawerItem().withName("Incidents").withIconTintingEnabled(true).withBadge("1").withBadgeStyle(badgeStyle).withIdentifier(8);

        final IProfile profile = new ProfileDrawerItem().withName("Loading...").withEmail("Loading...").withIcon(R.drawable.departmentlogo);

        final AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(false)
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName("Log Out").withIdentifier(1)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        if (profile.getIdentifier() == 1) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MainActivity.this, LoginDispatch.class);
                            startActivity(intent);
                            finish();
                        }
                        return false;
                    }
                })
                .build();

        Log.d("FDM", "TESTING: " + sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_TEST, false));

        /*sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equalsIgnoreCase(SettingsActivityFragment.KEY_PREF_TEST)) {

                    if (!sharedPreferences.getBoolean(SettingsActivityFragment.KEY_PREF_TEST, false)) {
                        drawer = new DrawerBuilder()
                                .withActivity(MainActivity.this)
                                .withToolbar(toolbar)
                                .withAccountHeader(headerResult)
                                .addDrawerItems(
                                        //new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.ic_home_white_24dp).withIconTintingEnabled(true),
                                        new PrimaryDrawerItem().withName("Who's En Route?").withIcon(R.drawable.ic_directions_car_white_24dp).withIconTintingEnabled(true).withIdentifier(0),
                                        //new PrimaryDrawerItem().withName("Apparatus").withIcon(R.drawable.ic_build_white_24dp).withIconTintingEnabled(true).withIdentifier(11),

                                        //new DividerDrawerItem(),
                                        //new PrimaryDrawerItem().withName("Events").withIcon(R.drawable.ic_event_white_24dp).withIconTintingEnabled(true),

                                        //new SectionDrawerItem().withName("Incidents"),
                                        //incidentList,

                                        //new SectionDrawerItem().withName("Equipment"),
                                        //apparatus,
                                        //apparatusInspection,
                                        //equipmentInventory,

                                        new DividerDrawerItem(),
                                        new SecondaryDrawerItem().withName("Settings").withIdentifier(9).withSelectable(false).withIcon(R.drawable.ic_settings_black_24dp)
                                )
                                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                    @Override
                                    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                                        // do something with the clicked item :D
                                        if (iDrawerItem.getIdentifier() == 11) {
                                            FragmentManager fragmentManager = getSupportFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            ApparatusListFragment fragment = new ApparatusListFragment();
                                            fragmentTransaction.replace(R.id.container, fragment);
                                            fragmentTransaction.commit();
                                        }
                                        if (iDrawerItem.getIdentifier() == 0) {
                                            attachFragment(savedInstanceState);
                                        }
                                        if (iDrawerItem.getIdentifier() == 8) {
                                            if (savedInstanceState == null && getIntent() != null) {
                                                CallInfoFragment callInfoFragment = new CallInfoFragment();
                                                getSupportFragmentManager().beginTransaction()
                                                        .replace(R.id.container, callInfoFragment, "callInfoFragment").commit();
                                            } else {
                                                callInfoFragment = (CallInfoFragment) getSupportFragmentManager().findFragmentByTag("callInfoFragment");
                                            }
                                        }
                                        if (iDrawerItem.getIdentifier() == 9) {
                                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                            startActivity(intent);
                                        }
                                        return false;
                                    }
                                })
                                .build();
                    } else {
                        //TESTING
                        drawer = new DrawerBuilder()
                                .withActivity(MainActivity.this)
                                .withToolbar(toolbar)
                                .withAccountHeader(headerResult)
                                .addDrawerItems(
                                        //new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.ic_home_white_24dp).withIconTintingEnabled(true),
                                        new PrimaryDrawerItem().withName("Who's En Route?").withIcon(R.drawable.ic_directions_car_white_24dp).withIconTintingEnabled(true).withIdentifier(0),
                                        new PrimaryDrawerItem().withName("Apparatus").withIcon(R.drawable.ic_build_white_24dp).withIconTintingEnabled(true).withIdentifier(11),

                                        new DividerDrawerItem(),
                                        new PrimaryDrawerItem().withName("Events").withIcon(R.drawable.ic_event_white_24dp).withIconTintingEnabled(true),

                                        new SectionDrawerItem().withName("Incidents"),
                                        incidentList,

                                        new SectionDrawerItem().withName("Equipment"),
                                        apparatus,
                                        apparatusInspection,
                                        equipmentInventory,

                                        new DividerDrawerItem(),
                                        new SecondaryDrawerItem().withName("Settings").withIdentifier(9).withSelectable(false).withIcon(R.drawable.ic_settings_black_24dp)
                                )
                                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                    @Override
                                    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                                        // do something with the clicked item :D
                                        if (iDrawerItem.getIdentifier() == 11) {
                                            FragmentManager fragmentManager = getSupportFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            ApparatusListFragment fragment = new ApparatusListFragment();
                                            fragmentTransaction.replace(R.id.container, fragment);
                                            fragmentTransaction.commit();
                                        }
                                        if (iDrawerItem.getIdentifier() == 0) {
                                            attachFragment(savedInstanceState);
                                        }
                                        if (iDrawerItem.getIdentifier() == 8) {
                                            if (savedInstanceState == null && getIntent() != null) {
                                                CallInfoFragment callInfoFragment = new CallInfoFragment();
                                                getSupportFragmentManager().beginTransaction()
                                                        .replace(R.id.container, callInfoFragment, "callInfoFragment").commit();
                                            } else {
                                                callInfoFragment = (CallInfoFragment) getSupportFragmentManager().findFragmentByTag("callInfoFragment");
                                            }
                                        }
                                        if (iDrawerItem.getIdentifier() == 9) {
                                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                            startActivity(intent);
                                        }
                                        return false;
                                    }
                                })
                                .build();
                    }


                }
            }
        });
*/
        Glide.with(this).load(R.drawable.wall).into(headerResult.getHeaderBackgroundView());

        if (!sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_TEST, false)) {
            drawer = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withAccountHeader(headerResult)
                    .addDrawerItems(
                            //new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.ic_home_white_24dp).withIconTintingEnabled(true),
                            new PrimaryDrawerItem().withName("Who's En Route?").withIcon(R.drawable.ic_directions_car_white_24dp).withIconTintingEnabled(true).withIdentifier(0),
                            //new PrimaryDrawerItem().withName("Apparatus").withIcon(R.drawable.ic_build_white_24dp).withIconTintingEnabled(true).withIdentifier(11),

                            //new DividerDrawerItem(),
                            //new PrimaryDrawerItem().withName("Events").withIcon(R.drawable.ic_event_white_24dp).withIconTintingEnabled(true),

                            //new SectionDrawerItem().withName("Incidents"),
                            //incidentList,

                            new SectionDrawerItem().withName("Equipment"),
                            new PrimaryDrawerItem().withName("Apparatus").withIdentifier(11),
                            //apparatus,
                            //apparatusInspection,
                            //equipmentInventory,

                            new DividerDrawerItem(),
                            new SecondaryDrawerItem().withName("Settings").withIdentifier(9).withSelectable(false).withIcon(R.drawable.ic_settings_black_24dp)
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                            // do something with the clicked item :D
                            if (iDrawerItem.getIdentifier() == 11) {
                                ApparatusListFragment fragment = new ApparatusListFragment();
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, fragment, "apparatusFrag").commit();
                            }
                            if (iDrawerItem.getIdentifier() == 0) {
                                attachFragment(savedInstanceState);
                            }
                            if (iDrawerItem.getIdentifier() == 8) {
                                if (savedInstanceState == null && getIntent() != null) {
                                    CallInfoFragment callInfoFragment = new CallInfoFragment();
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.container, callInfoFragment, "callInfoFragment").commit();
                                } else {
                                    callInfoFragment = (CallInfoFragment) getSupportFragmentManager().findFragmentByTag("callInfoFragment");
                                }
                            }
                            if (iDrawerItem.getIdentifier() == 9) {
                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(intent);
                            }
                            return false;
                        }
                    })
                    .build();
        } else {
            //TESTING
            drawer = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withAccountHeader(headerResult)
                    .addDrawerItems(
                            //new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.ic_home_white_24dp).withIconTintingEnabled(true),
                            new PrimaryDrawerItem().withName("Who's En Route?").withIcon(R.drawable.ic_directions_car_white_24dp).withIconTintingEnabled(true).withIdentifier(0),
                            new PrimaryDrawerItem().withName("Apparatus").withIcon(R.drawable.ic_build_white_24dp).withIconTintingEnabled(true).withIdentifier(11),

                            new DividerDrawerItem(),
                            new PrimaryDrawerItem().withName("Events").withIcon(R.drawable.ic_event_white_24dp).withIconTintingEnabled(true),

                            new SectionDrawerItem().withName("Incidents"),
                            incidentList,

                            new SectionDrawerItem().withName("Equipment"),
                            apparatus,
                            apparatusInspection,
                            equipmentInventory,

                            new DividerDrawerItem(),
                            new SecondaryDrawerItem().withName("Settings").withIdentifier(9).withSelectable(false).withIcon(R.drawable.ic_settings_black_24dp)
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                            // do something with the clicked item :D
                            if (iDrawerItem.getIdentifier() == 11) {
                                ApparatusListFragment fragment = new ApparatusListFragment();
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, fragment, "mainTabs").commit();
                            }
                            if (iDrawerItem.getIdentifier() == 0) {
                                attachFragment(savedInstanceState);
                            }
                            if (iDrawerItem.getIdentifier() == 8) {
                                if (savedInstanceState == null && getIntent() != null) {
                                    CallInfoFragment callInfoFragment = new CallInfoFragment();
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.container, callInfoFragment, "callInfoFragment").commit();
                                } else {
                                    callInfoFragment = (CallInfoFragment) getSupportFragmentManager().findFragmentByTag("callInfoFragment");
                                }
                            }
                            if (iDrawerItem.getIdentifier() == 9) {
                                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(intent);
                            }
                            return false;
                        }
                    })
                    .build();
        }

        //checkForCall();

        DatabaseReference userREf = usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userREf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                MemberObjectFire departmentObject = snapshot.getValue(MemberObjectFire.class);
                profile.withEmail(departmentObject.getEmail());
                profile.withName(departmentObject.getName());
                snapshot.getKey();
                headerResult.updateProfile(profile);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", departmentObject.getName());
                editor.putBoolean("canReset", departmentObject.getCanReset());
                editor.putBoolean("isOfficer", departmentObject.getIsOfficer());
                editor.putString("department", departmentObject.getDepartment());
                editor.commit();

                DatabaseReference departmentRef = database.getReference("departments/"+ sharedPreferences.getString("department", "") +"/departmentPhone");
                departmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("departmentPhone", snapshot.getValue().toString());
                        editor.apply();
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Toast.makeText(MainActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void checkForCall() {
        /*ParseUser.getCurrentUser().getParseObject("departmentP").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject.getString("callType") != null) {
                    if (!parseObject.getString("callType").isEmpty()) {
                        //Call Present
                        PrimaryDrawerItem currentCall = new PrimaryDrawerItem().withName(parseObject.getString("callType")).withIcon(getResources().getDrawable(R.drawable.ic_whatshot_24dp)).withIconTintingEnabled(true).withIdentifier(8);
                        drawer.addItemAtPosition(new SectionDrawerItem().withName("Current Call").withIdentifier(100), 2);
                        drawer.addItemAtPosition(currentCall, 3);
                    } else {
                        drawer.removeItem(8);
                        drawer.removeItem(100);
                    }
                }
            }
        });*/
    }

    /*private void attachFragment(Bundle savedInstanceState) {
        Bundle bundle = new Bundle();
        bundle.putString("respondingTo", respondingTo);
        if (savedInstanceState == null && getIntent() != null) {
            respondingFragment = RespondingFragment.newInstance(respondingTo);
            respondingFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, respondingFragment, "respFragment").commit();
        } else {
            respondingFragment = (RespondingFragment) getSupportFragmentManager().findFragmentByTag("respFragment");
        }
    }*/

    private void attachFragment(Bundle savedInstanceState) {
        Bundle bundle = new Bundle();
        bundle.putString("respondingTo", respondingTo);
        if (savedInstanceState == null && getIntent() != null) {
            mainTabs = MainTabs.newInstance(respondingTo);
            mainTabs.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mainTabs, "mainTabs").commit();
        } else {
            mainTabs = (MainTabs) getSupportFragmentManager().findFragmentByTag("mainTabs");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("Fire", "Connection to Google API suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        //broadcastLocation(location);
    }

    @Override
    public void onMemberChange(MemberObjectFire memberObjectFire) {
        MainTabs tabs = (MainTabs) getSupportFragmentManager().findFragmentByTag("mainTabs");

        MapsActivity2 map = (MapsActivity2) tabs.getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.vp + 1);
        //MapsActivity2.updateMemberLocations(memberObjectFire);

        Log.d("MemberChange", "Update Map" + memberObjectFire.getName());
    }

    @Override
    public void onRespond() {
//        MainTabs tabs = (MainTabs) getSupportFragmentManager().findFragmentByTag("mainTabs");
//
//        MapsActivity2 map = (MapsActivity2) tabs.getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.vp + 1);
//        //MapsActivity2.userResponded();
//        MapsActivity2 mapsActivity2 = new MapsActivity2();
//        mapsActivity2.userResponded();
    }

    public static class MainTabs extends Fragment {

        String respondingTo = "";
        private NavigationTabStrip mTopNavigationTabStrip;
        private ViewPager mViewPager;
        private MapsActivity2 mapsActivity2;

        public static MainTabs newInstance(String respondingTo) {
            MainTabs dialogFragment = new MainTabs();
            Bundle bundle = new Bundle();
            bundle.putString("respondingTo", respondingTo);
            dialogFragment.setArguments(bundle);
            return dialogFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_main_tabs, container, false);
            ButterKnife.bind(this, view);

            mTopNavigationTabStrip = (NavigationTabStrip) view.findViewById(R.id.nts_top);
            mViewPager = (ViewPager) view.findViewById(R.id.vp);


            Bundle bundle = getArguments();
            respondingTo = bundle.getString("RT");

            mViewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
            mTopNavigationTabStrip.setViewPager(mViewPager);
            mTopNavigationTabStrip.setStripType(NavigationTabStrip.StripType.LINE);
            mTopNavigationTabStrip.setStripColor(Color.parseColor("#FFFFFF"));

            return view;
        }

        class PagerAdapter extends FragmentPagerAdapter {

            public PagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                mapsActivity2 = MapsActivity2.newInstance();

                switch (position) {
                    case 0:
                        return RespondingFragment.newInstance(respondingTo);
                    case 1:
                        return mapsActivity2;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

        }
    }
}
