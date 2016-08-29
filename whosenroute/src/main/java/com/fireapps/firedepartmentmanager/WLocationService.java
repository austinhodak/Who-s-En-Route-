package com.fireapps.firedepartmentmanager;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by austinhodak on 6/27/16.
 */

public class WLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    /* TODO Check to see if responding. */

    private static DatabaseReference userReference;
    private static Member currentMember;
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mInProgress;
    private boolean servicesAvailable = true;
    private SharedPreferences sharedPreferences;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;
    private List<Geofence> mGeoFenceList = new ArrayList<>();
    private PendingIntent mGeoFencePendingIntent;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case 1: {
                    stopSelf();
                }
                break;
            }
        }
    };

    public WLocationService() {
        super();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeoFencePendingIntent != null) {
            return mGeoFencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            firebaseUser = user;
        } else {
            // No user is signed in
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        userReference = firebaseDatabase.getReference("users/" + firebaseUser.getUid());

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Member member = dataSnapshot.getValue(Member.class);
                currentMember = member;
                if (member.getIsResponding() && member.getRespondingTo() != null) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mInProgress = false;
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

        mGeoFenceList.add(new Geofence.Builder()
                .setRequestId("1")
                .setCircularRegion(41.816780, -79.445430, 40)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build()
        );

        setUpLocationClientIfNeeded();
        setTimeout();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFenceList);
        return builder.build();
    }

    private void setTimeout() {
        String mLocationTimeout = sharedPreferences.getString("pref_map_response_timeout", "600000");
        if (mLocationTimeout == "0") {
            Log.d("LocationTimeout", "NO TIMEOUT UNTIL STOPPED");
        } else {
            Log.d("LocationTimeout", "TIMEOUT: " + mLocationTimeout);
            handler.sendEmptyMessageDelayed(1, Integer.valueOf(mLocationTimeout));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (!servicesAvailable || mLocationClient.isConnecting() || mInProgress)
            return START_STICKY;

        setUpLocationClientIfNeeded();

        if (!mLocationClient.isConnected() || !mLocationClient.isConnected() && !mInProgress) {
            mInProgress = true;
            mLocationClient.connect();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mInProgress = false;
        if (servicesAvailable && mLocationClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            mLocationClient = null;
        }
        Log.d("EnRoute", "Location Service: Stopped");
        super.onDestroy();
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mLocationClient != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
            LocationServices.GeofencingApi.addGeofences(mLocationClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mInProgress = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mInProgress = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("WHOER-LOC", "Location Updated" + location);

        Map<String, Object> updates = new HashMap<>();
        updates.put("/location/currentLat", location.getLatitude());
        updates.put("/location/currentLon", location.getLongitude());

        userReference.updateChildren(updates);
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    public static class GeofenceTransitionsIntentService extends IntentService {

        public GeofenceTransitionsIntentService() {
            super("GeofenceTransitionsIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                Log.e("GEOFENCE", "" + geofencingEvent.getErrorCode());
                return;
            }
            int geoFenceTransition = geofencingEvent.getGeofenceTransition();

            if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                List<String> geoFenceIds = new ArrayList<>();

                for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                    geoFenceIds.add(geofence.getRequestId());
                    Log.d("GEOFENCE", "" + geofence.getRequestId());

                    if (geofence.getRequestId().equals("1")) {
                        Log.d("GEOFENCE", "AT STATION");
                        try {
                            if (currentMember.getIsResponding() && currentMember.getRespondingTo() != null && currentMember.getRespondingTo() != "") {
                                Map<String, Object> respondingToStation = new HashMap<String, Object>();
                                respondingToStation.put("respondingTo", "At Station");
                                respondingToStation.put("isResponding", true);
                                userReference.updateChildren(respondingToStation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.getException() != null) {
                                            task.getException().printStackTrace();
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d("GEOFENCE", "ERROR");
                    }
                }
            }
        }
    }
}
