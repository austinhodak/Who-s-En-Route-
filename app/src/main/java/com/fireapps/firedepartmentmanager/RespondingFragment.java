package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pubnub.api.Callback;
import com.pubnub.api.PnGcmMessage;
import com.pubnub.api.PnMessage;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import permission.auron.com.marshmallowpermissionhelper.FragmentManagePermission;
import permission.auron.com.marshmallowpermissionhelper.PermissionResult;
import permission.auron.com.marshmallowpermissionhelper.PermissionUtils;

public class RespondingFragment extends FragmentManagePermission implements AdapterView.OnItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static String respondingTo;
    private final Pubnub pubnub = new Pubnub("pub-c-31e7d284-39d2-4aff-b682-d709c4aa105e", "sub-c-8c10dcfe-3732-11e5-87df-02ee2ddab7fe");
    @Bind(R.id.responding_ListView)
    RecyclerView recyclerView;
    @Bind(R.id.respOff_fab)
    FloatingActionMenu officerFAB;
    @Bind(R.id.respOff_reset)
    FloatingActionButton officerReset;
    @Bind(R.id.empty_layout)
    TextView emptyLayout;
    @Bind(R.id.progressBar1)
    ProgressBar progressBar;
    @Bind(R.id.respOff_viewmap)
    FloatingActionButton mapButton;
    @Bind(R.id.bottomNavigation)
    BottomNavigation nav;
    @Bind(R.id.CoordinatorLayout01)
    CoordinatorLayout coordinatorLayout;
    DatabaseReference firebase, userFirebase, usersRef, departmentsRef;
    private RespondingListAdapterNew adapter;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String respondingStatus;
    private SharedPreferences sharedPreferences;

    List<MemberObjectFire> memberList;

    private LinearLayoutManager mLayoutManager;
    private String cityname;
    private int respondingToStation;
    private int respondingToScene;
    private SharedPreferences sharedPref;

    OnMemberChanged onMemberChangedCallback;
    OnResponded onResponded;

    public RespondingFragment() {
    }

    public static RespondingFragment newInstance(String respondingTo) {
        RespondingFragment dialogFragment = new RespondingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("respondingTo", respondingTo);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_responding_main, container, false);
        ButterKnife.bind(this, view);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        firebase = database.getReference("users");

        //Users
        usersRef =
                database.getReference("users");

        //Departments
        departmentsRef =
                database.getReference("departments");

        userFirebase = firebase.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Bundle bundle = getArguments();

        assert bundle != null;
        try {
            if (!bundle.getString("respondingTo").isEmpty()) {
                //Widget response
                switch (bundle.getString("respondingTo")) {
                    case "station":
                        respondingClicked(1);
                        respondingTo = "Station";
                        sendPush();
                        break;
                    case "scene":
                        respondingClicked(2);
                        respondingTo = "Scene";
                        sendPush();
                        break;
                    case "nr":
                        respondingClicked(0);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        memberList = new ArrayList<>();
        mLayoutManager = new LinearLayoutManager(getContext());
        adapter = new RespondingListAdapterNew(getActivity(), memberList);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

                initResponding();
                initListeners();
                updateButtons();


        getActivity().setTitle("Who's En Route?");

        setHasOptionsMenu(true);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        final Snackbar notConnected = Snackbar.make(coordinatorLayout, "No Internet Connection.", Snackbar.LENGTH_INDEFINITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notConnected.setActionTextColor(getActivity().getColor(R.color.primary));
        } else {
            notConnected.setActionTextColor(getActivity().getResources().getColor(R.color.primary));
        }

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    System.out.println("connected");
                    notConnected.dismiss();
                } else {
                    System.out.println("not connected");
                    notConnected.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

        return view;
    }

    private void initListeners() {

        nav.setOnMenuItemClickListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(@IdRes int i, int i1) {
                if (i1 == 0) {
                    respondingTo = "";
                    Map<String, Object> respondingTo = new HashMap<String, Object>();
                    respondingTo.put("respondingTo", null);
                    respondingTo.put("isResponding", false);
                    respondingTo.put("respondingTo", null);
                    respondingTo.put("isResponding", false);
                    respondingTo.put("respondingFromLoc", null);
                    respondingTo.put("respondingTime", null);
                    userFirebase.updateChildren(respondingTo);
                }
                if (i1 == 1) {
                    if (sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_RESP_PROMPT, true)) {
                        new MaterialDialog.Builder(getActivity())
                                .title("Respond to Station?")
                                .positiveText("RESPOND")
                                .negativeText("CANCEL")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        respondingClicked(1);
                                        respondingTo = "Station";
                                        sendPush();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        nav.setSelectedIndex(0, true);
                                    }
                                })
                                .show();
                    } else {
                        respondingClicked(1);
                        respondingTo = "Station";
                        sendPush();
                    }
                }
                if (i1 == 2) {
                    respondingClicked(0);
                    respondingTo = "NR";
                }
                if (i1 == 3) {
                    if (sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_RESP_PROMPT, true)) {
                        new MaterialDialog.Builder(getActivity())
                                .title("Respond to Scene?")
                                .positiveText("RESPOND")
                                .negativeText("CANCEL")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        respondingClicked(2);
                                        respondingTo = "Scene";
                                        sendPush();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        nav.setSelectedIndex(0, true);
                                    }
                                })
                                .show();
                    } else {
                        respondingClicked(2);
                        respondingTo = "Scene";
                        sendPush();
                    }
                }
            }

            @Override
            public void onMenuItemReselect(@IdRes int i, int i1) {

            }
        });

        officerReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Reset Call?")
                        .content("This will reset the call? Are you sure?")
                        .positiveText("RESET")
                        .negativeText("CANCEL")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                progressBar.setVisibility(View.VISIBLE);
                                officerFAB.close(true);

                                Query query = firebase.orderByChild("name");

                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                            MemberObjectFire post = postSnapshot.getValue(MemberObjectFire.class);
                                            Map<String, Object> nickname = new HashMap<String, Object>();
                                            nickname.put(postSnapshot.getKey() + "/respondingTo", null);
                                            nickname.put(postSnapshot.getKey() + "/isResponding", false);
                                            nickname.put(postSnapshot.getKey() + "/respondingFromLoc", null);
                                            nickname.put(postSnapshot.getKey() + "/respondingTime", null);
                                            nickname.put(postSnapshot.getKey() + "/lat", null);
                                            nickname.put(postSnapshot.getKey() + "/lon", null);
                                            firebase.updateChildren(nickname);

                                            updateButtons();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError firebaseError) {
                                        System.out.println("The read failed: " + firebaseError.getMessage());
                                        progressBar.setVisibility(View.GONE);
                                        //swipeRefreshLayout.setRefreshing(false);
                                    }
                                });
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity2.class);
                startActivity(intent);
            }
        });
    }

    private void initResponding() {
        if (!isAdded()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        memberList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference linkCommentsRef = departmentsRef.child(sharedPreferences.getString("department", "none")).child("members");

        linkCommentsRef.addChildEventListener(new ChildEventListener() {
            // Retrieve members
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                usersRef.child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //Retrieve member and do stuff
                        MemberObjectFire post = snapshot.getValue(MemberObjectFire.class);

                        try {
                            for (MemberObjectFire object : memberList) {
                                if (object.getName().equals(post.getName())) {
                                    memberList.remove(object);
                                    adapter.notifyDataSetChanged();

                                    Log.d("List", "REMOVED");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!snapshot.getKey().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        onMemberChangedCallback.onMemberChange(post);

                        if (post.getisResponding()) {
                            memberList.add(post);
                            adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            //swipeRefreshLayout.setRefreshing(false);

                            if (post.getRespondingTo().equals("Station")) {
                                respondingToStation++;
                            } else if (post.getRespondingTo().equals("Scene")) {
                                respondingToScene++;
                            }
                            updateButtons();

                        } else {
                            progressBar.setVisibility(View.GONE);
                            //swipeRefreshLayout.setRefreshing(false);
                            updateButtons();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        Toast.makeText(getActivity(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("FDM", firebaseError.getMessage());
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
            public void onCancelled(DatabaseError firebaseError) {
                Toast.makeText(getActivity(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("FDM", firebaseError.getMessage());
            }
        });

        boolean canReset = sharedPreferences.getBoolean("canReset", false);
        if (canReset) {
            officerReset.setVisibility(View.VISIBLE);
            Log.d("CanReset", "True");
        } else {
            officerReset.setVisibility(View.GONE);
            Log.d("CanReset", "False");
        }

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        userFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                MemberObjectFire memberObjectFire = snapshot.getValue(MemberObjectFire.class);

                respondingStatus = memberObjectFire.getRespondingTo();

                if (respondingStatus == null) {
                    nav.setSelectedIndex(0, true);

                    editor.putString("respondingTo", "none");
                    editor.apply();
                } else {

                    switch (respondingStatus) {
                        case "Station":
                            nav.setSelectedIndex(1, true);

                            editor.putString("respondingTo", "station");
                            editor.apply();
                            break;
                        case "At Station":
                            nav.setSelectedIndex(1, true);

                            editor.putString("respondingTo", "station");
                            editor.apply();
                            break;
                        case "Scene":
                            nav.setSelectedIndex(3, true);

                            editor.putString("respondingTo", "scene");
                            editor.apply();
                            break;
                        case "NR":
                            nav.setSelectedIndex(2, true);

                            editor.putString("respondingTo", "nr");
                            editor.apply();
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    private void updateButtons(){
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            userFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    // do some stuff once
                    MemberObjectFire memberObjectFire = snapshot.getValue(MemberObjectFire.class);

                    try {
                        respondingStatus = memberObjectFire.getRespondingTo();

                        if (respondingStatus == null) {
                            nav.setSelectedIndex(0, true);

                            editor.putString("respondingTo", "none");
                            editor.apply();
                            onResponded.onRespond();
                        } else {

                            switch (respondingStatus) {
                                case "Station":
                                    nav.setSelectedIndex(1, true);

                                    editor.putString("respondingTo", "station");
                                    editor.apply();
                                    break;
                                case "At Station":
                                    nav.setSelectedIndex(1, true);

                                    editor.putString("respondingTo", "station");
                                    editor.apply();
                                    break;
                                case "Scene":
                                    nav.setSelectedIndex(3, true);

                                    editor.putString("respondingTo", "scene");
                                    editor.apply();
                                    break;
                                case "NR":
                                    nav.setSelectedIndex(2, true);

                                    editor.putString("respondingTo", "nr");
                                    editor.apply();
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Callback callback = new Callback() {
        @Override
        public void successCallback(String channel, Object message) {
            super.successCallback(channel, message);
            Log.i("FDM", "Success on channel " + channel + " : " + message);
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            super.errorCallback(channel, error);
            Log.i("FDM", "Error on channel " + channel + " : " + error);
        }
    };

    private void updateCallLayout() {
        /*callLayout.setVisibility(View.GONE);
        ParseUser.getCurrentUser().getParseObject("departmentP").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (!parseObject.getString("callType").isEmpty()) {
                    callLayout.setVisibility(View.VISIBLE);
                    callType.setText(parseObject.getString("callType"));
                    callAddress.setText(parseObject.getString("callLocation"));
                    if (parseObject.getBoolean("priorityCall")) {
                        callLayout.setBackgroundColor(getResources().getColor(R.color.primary));
                    } else {
                        callLayout.setBackgroundColor(getResources().getColor(R.color.md_orange_500));
                    }
                    if (parseObject.getBoolean("callLocationFound")) {
                        mapButton.setVisibility(View.VISIBLE);
                    } else {
                        mapButton.setVisibility(View.GONE);
                    }
                }
            }
        });*/
    }

    public void sendPush() {

        boolean sendPushes = sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_SEND_PUSH_ENABLE, true);
        PnMessage message = null;

        if (sendPushes) {
            PnGcmMessage gcmMessage = new PnGcmMessage();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("respondingTo", sharedPreferences.getString("name", "Unknown") + " is responding to the " + respondingTo + ".");
                jsonObject.put("sendersID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            } catch (JSONException e) {
            }
            gcmMessage.setData(jsonObject);

            if (!sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_TEST, false)) {
                 message = new PnMessage(
                        pubnub,
                        sharedPreferences.getString("department", "none"),
                        callback,
                        gcmMessage);
            } else {
                //TESTING
                message = new PnMessage(
                        pubnub,
                        "testing1",
                        callback,
                        gcmMessage);
            }

            try {
                message.publish();
            } catch (PubnubException e) {
                e.printStackTrace();
            }
        }
    }

    public void respondingClicked(int to) {
        String city = cityname;

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date).toString();

        switch (to) {
            case 0:
                Map<String, Object> respondingTo = new HashMap<String, Object>();
                respondingTo.put("respondingTo", "NR");
                respondingTo.put("isResponding", true);
                userFirebase.updateChildren(respondingTo);
                break;
            case 1:
                Map<String, Object> respondingTo2 = new HashMap<String, Object>();
                respondingTo2.put("respondingTo", "Station");
                respondingTo2.put("isResponding", true);
                respondingTo2.put("respondingTime", mLastUpdateTime);
                respondingTo2.put("respondingFromLoc", city);
                userFirebase.updateChildren(respondingTo2);
                break;
            case 2:
                Map<String, Object> respondingTo3 = new HashMap<String, Object>();
                respondingTo3.put("respondingTo", "Scene");
                respondingTo3.put("isResponding", true);
                respondingTo3.put("respondingTime", mLastUpdateTime);
                respondingTo3.put("respondingFromLoc", city);
                userFirebase.updateChildren(respondingTo3);
                break;
        }

        onResponded.onRespond();
    }

    public void onStart() {
        checkPlayServices();
        //mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public Object getSharedElementEnterTransition() {
        return super.getSharedElementEnterTransition();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_responding, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.call_department) {
            askCompactPermissions(new String[]{PermissionUtils.Manifest_CALL_PHONE}, new PermissionResult() {
                @Override
                public void permissionGranted() {
                    //permission granted
                    //replace with your action
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + sharedPreferences.getString("departmentPhone", "")));
                    startActivity(callIntent);
                }

                @Override
                public void permissionDenied() {
                    //permission denied
                    //replace with your action
                }
            });


            return true;
        }
        if (id == R.id.refresh_list) {
            initResponding();
            Log.d("FDM", "Refresh");
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                //getActivity().finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        cityname = "";

        Log.d("Location", "Connected");


        AsyncTask.execute(new Runnable() {
            public void run() {
                askCompactPermissions(new String[]{PermissionUtils.Manifest_ACCESS_COARSE_LOCATION, PermissionUtils.Manifest_ACCESS_FINE_LOCATION}, new PermissionResult() {
                    @Override
                    public void permissionGranted() {
                        //permission granted
                        //replace with your action
                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                mGoogleApiClient);
                        if (mLastLocation != null) {
                            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));

                            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                            //latTextView.setText(String.valueOf(location.getLatitude()));
                            //longTextView.setText(String.valueOf(location.getLongitude()));
                            Log.d("FDM Location", "Location Found");
                            List<Address> addresses;

                            try {
                                addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                                if (addresses.size() > 0)

                                {
                                    //while(locTextView.getText().toString()=="Location") {
                                    cityname = addresses.get(0).getLocality().toString();
                                    Log.d("FDM Location", cityname);
                                    // }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();

                            }

                            //Toast.makeText(getActivity(), cityname, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("FDM Location", "null");
                        }
                    }

                    @Override
                    public void permissionDenied() {
                        //permission denied
                        //replace with your action
                    }
                });
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d("Location", connectionResult.getErrorMessage());

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public interface OnMemberChanged {
        void onMemberChange(MemberObjectFire memberObjectFire);
    }

    public interface OnResponded {
        void onRespond();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onMemberChangedCallback = (OnMemberChanged) activity;
            onResponded = (OnResponded) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }
}