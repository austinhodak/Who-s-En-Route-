package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by austinhodak on 7/14/16.
 */

public class RespondingSystem {

    private final SharedPreferences sharedPreferences;
    private final Intent locationService;
    private final boolean mIsLocationTrackingEnabled;
    private final DatabaseReference userReference;
    public final String currentDepartmentAbbrv;
    String TAG = "RespondingSystem";

    private DepartmentListener departmentListener;

    FirebaseDatabase database = FirebaseDatabase.getInstance();;
    DataSnapshot currentDepartment;
    String currentDepartmentKey;
    List<DataSnapshot> availableDepartments = new ArrayList<>();


    FirebaseUser mUser;
    DataSnapshot currentUser = null;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseAuth mAuth;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageReference = storage.getReferenceFromUrl("gs://fire-department-manager.appspot.com");

    Context context, applicationContext;



    private static RespondingSystem instance;

    public void setDepartmentListener(DepartmentListener departmentListener) {
        this.departmentListener = departmentListener;

    }

    public synchronized static RespondingSystem getInstance(Context context) {
        if (instance == null) {
            instance = new RespondingSystem(context);
        }
        return instance;
    }

    public RespondingSystem(Context context) {

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        locationService = new Intent(applicationContext, WLocationService.class);

        currentDepartmentKey = sharedPreferences.getString("selectedDepartment", "");
        currentDepartmentAbbrv = sharedPreferences.getString("selectedDepartmentAbbrv", "");

        mIsLocationTrackingEnabled = sharedPreferences.getBoolean("pref_map_response_enable", false);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUser = user;
                    getMemberInfo();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        userReference = database.getReference("users/" + mUser.getUid());
        //userReference.keepSynced(true);
    }

    public RespondingSystem getMemberInfo() {
        ValueEventListener memberListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot;
                Member member = dataSnapshot.getValue(Member.class);
                sharedPreferences.edit().putString("userName", member.getName()).apply();
                Log.d(TAG, "getMemberInfo Name Save: " + member.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        database.getReference("users").child(mUser.getUid()).addListenerForSingleValueEvent(memberListener);
        return this;
    }

    public RespondingSystem getMembersDepartments(final DataSnapshot currentUser) {
        final List<String> departmentIds = new ArrayList<>();
        Set<String> strings = sharedPreferences.getStringSet("departmentIds", null);
        List<String> array = new ArrayList<>(strings);
        if (!array.isEmpty()) {
            for (int i = 0; i < array.size(); i++) {
                String string = array.get(i);
                Log.d(TAG, "getMembersDepartments: " + string);
                loadDepartment(string);
            }
        } else {
            //No IDS Found, Load...
            availableDepartments.clear();
            departmentIds.clear();

            database.getReference("users").child(currentUser.getKey()).child("departments").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //Here are the members departments.
                    departmentIds.add(dataSnapshot.getKey());
                    database.getReference("departments").child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            //Add department to available departments.
                            availableDepartments.add(dataSnapshot);
                            Log.d(TAG, "onDataChangeDEPT: " + dataSnapshot.getKey());
                            departmentListener.onDepartmentAdded(dataSnapshot, null);
                            storageReference.child("agencyIcons/" + dataSnapshot.getKey() + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {


                                }
                            });
                            //departmentListener.onDepartmentAdded(dataSnapshot, null);
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
            database.getReference("users").child(currentUser.getKey()).child("departments").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: DATA LOADED");
                    departmentListener.onLoadingFinished();
                    Set<String> set = new HashSet<>();
                    set.addAll(departmentIds);
                    Log.d(TAG, "onDataChange: " + set);
                    sharedPreferences.edit().putStringSet("departmentIds", set).apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        return this;
    }

    public void onStop() {
        mAuth.removeAuthStateListener(mAuthListener);
    }

    public void respondStation() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);

        Map<String, Object> respondingToStation = new HashMap<String, Object>();
        respondingToStation.put("respondingTo", "Station");
        respondingToStation.put("isResponding", true);
        respondingToStation.put("respondingTime", mLastUpdateTime);
        respondingToStation.put("respondingAgency", getCurrentDepartmentKey());
        userReference.updateChildren(respondingToStation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.getException() != null) {
                    task.getException().printStackTrace();
                }
            }
        });
        if (mIsLocationTrackingEnabled)
            applicationContext.startService(locationService);
    }

    public void respondScene() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);

        Map<String, Object> respondingToScene = new HashMap<String, Object>();
        respondingToScene.put("respondingTo", "Scene");
        respondingToScene.put("isResponding", true);
        respondingToScene.put("respondingTime", mLastUpdateTime);
        respondingToScene.put("respondingAgency", getCurrentDepartmentKey());
        userReference.updateChildren(respondingToScene).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.getException() != null) {
                    task.getException().printStackTrace();
                }
            }
        });
        if (mIsLocationTrackingEnabled)
            applicationContext.startService(locationService);
    }

    public void respondCant() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);

        Map<String, Object> cantRespond = new HashMap<String, Object>();
        cantRespond.put("respondingTo", "Can't Respond");
        cantRespond.put("isResponding", true);
        cantRespond.put("respondingTime", mLastUpdateTime);
        cantRespond.put("respondingAgency", getCurrentDepartmentKey());
        userReference.updateChildren(cantRespond).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.getException() != null) {
                    task.getException().printStackTrace();
                }
            }
        });
        applicationContext.stopService(locationService);
    }

    public void clearSelf() {
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
            }
        });
        applicationContext.stopService(locationService);
    }

    public void setSelectedDepartment(String key) {
        this.currentDepartmentKey = key;
        sharedPreferences.edit().putString("selectedDepartment", key).commit();
        Log.d(TAG, "setSelectedDepartment: " + key);
    }

    public String getCurrentDepartmentKey() {
        if (currentDepartmentKey == null) {
            currentDepartmentKey = sharedPreferences.getString("selectedDepartment", "");
        }
        return currentDepartmentKey;
    }

    String abbrv;
    public String getCurrentDepartmentAbbrv() {
        abbrv = null;
        if (currentDepartmentKey == null) {
            currentDepartmentKey = sharedPreferences.getString("selectedDepartment", "");
        }

        database.getReference("departments").child(currentDepartmentKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    abbrv = dataSnapshot.child("abbrv").getValue().toString();
                    sharedPreferences.edit().putString("selectedDepartmentAbbrv", abbrv).apply();
                    Log.d(TAG, "onDataChange: " + abbrv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return abbrv;
    }

    public interface DepartmentListener {
        public void onDepartmentAdded(DataSnapshot snapshot, Uri icon);
        public void onDepartmentUpdated(DataSnapshot snapshot);
        public void onLoadingFinished();
        public void onDepartmentAddedFirstTime(DataSnapshot snapshot);
    }

    public void loadDepartment(String id) {

        final DataSnapshot datasnapshot = null;
        database.getReference("departments").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange2: " + dataSnapshot);
                departmentListener.onDepartmentAdded(dataSnapshot, null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /////////////////
    public RespondingSystem loadInitialData() {
        Set<String> departmentIds = sharedPreferences.getStringSet("departmentIds", null);
        if (departmentIds == null) {
            //Load User, Depts, Save, Load Profiles.
            loadDepartments();
        } else {
            List<String> ids = new ArrayList<>(departmentIds);
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                Log.d(TAG, "loadInitialData: " + id);
                database.getReference("departments").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot);
                        if (dataSnapshot != null)
                            try {
                                departmentListener.onDepartmentAdded(dataSnapshot, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            watchForDepartmentChanges();
        }
        return this;
    }

    private void watchForDepartmentChanges() {
        final List<String> departmentIds = new ArrayList<>();
        database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("departments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Here are the members departments.
                departmentIds.add(dataSnapshot.getKey());
                database.getReference("departments").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        departmentListener.onDepartmentUpdated(dataSnapshot);
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
        database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("departments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                set.addAll(departmentIds);
                sharedPreferences.edit().putStringSet("departmentIds", set).apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadDepartments() {
        ValueEventListener memberListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot;
                Member member = dataSnapshot.getValue(Member.class);
                sharedPreferences.edit().putString("userName", member.getName()).apply();

                final List<String> departmentIds = new ArrayList<>();
                database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("departments").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //Here are the members departments.
                        departmentIds.add(dataSnapshot.getKey());
                        //TODO
                        sharedPreferences.edit().putString("selectedDepartment", dataSnapshot.getKey()).apply();
                        database.getReference("departments").child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                //departmentListener.onDepartmentAdded(dataSnapshot, null);
                                departmentListener.onDepartmentAddedFirstTime(dataSnapshot);
                                FirebaseMessaging.getInstance().subscribeToTopic("incidents-"+dataSnapshot.getKey());
                                FirebaseMessaging.getInstance().subscribeToTopic("manpower-"+dataSnapshot.getKey());
                                FirebaseMessaging.getInstance().subscribeToTopic("tones-"+dataSnapshot.getKey());
                                Log.d(TAG, "onDataChange: Subscribed to topics!");
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
                database.getReference("users").child(currentUser.getKey()).child("departments").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            departmentListener.onLoadingFinished();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Set<String> set = new HashSet<>();
                        set.addAll(departmentIds);
                        sharedPreferences.edit().putStringSet("departmentIds", set).apply();

                        watchForDepartmentChanges();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        database.getReference("users").child(mUser.getUid()).addListenerForSingleValueEvent(memberListener);
    }

    String name;
    public String getUserName() {
        name = null;
        name = sharedPreferences.getString("userName", null);
        if (name == null) {
            database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue().toString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //Toast.makeText(context, name[0], Toast.LENGTH_SHORT).show();
        Log.d(TAG, "getUserName: " + name);
        return name;
    }
}
