package com.fireapps.firedepartmentmanager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by austinhodak on 6/28/16.
 */

public class WidgetProvider extends AppWidgetProvider {

    public static String WIDGET_REFRESH = "com.fireapps.firedepartmentmanager.WIDGET_UPDATE";
    public static String respondingTo = null;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userReference;
    private boolean mIsLocationTrackingEnabled;
    private SharedPreferences sharedPreferences;

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;
        Log.d("WIDGET", "ONUPDATE");
        for (int i = 0; i < count; i++) {
            final int widgetId = appWidgetIds[i];

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            mIsLocationTrackingEnabled = sharedPreferences.getBoolean("pref_map_response_enable", false);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // User is signed in
                firebaseUser = user;
            } else {
                // No user is signed in
            }
            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.responding_widget_new);

            firebaseDatabase = FirebaseDatabase.getInstance();
            if (firebaseUser == null) {
                return;
            }
            userReference = firebaseDatabase.getReference("users/" + firebaseUser.getUid());
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Member member = dataSnapshot.getValue(Member.class);
                    Log.d("UPDATE", "MEMBER RECEIVED");
                    if (respondingTo == null) {
                        if (member.getIsResponding() && member.getRespondingTo() != "" && member.getRespondingTo() != null)
                            switch (member.getRespondingTo().toLowerCase()) {
                                case "":
                                    break;
                                case "station":
                                    respondingTo = "0";
                                    break;
                                case "can't respond":
                                    respondingTo = "2";
                                    break;
                                case "nr":
                                    respondingTo = "2";
                                    break;
                                case "scene":
                                    respondingTo = "1";
                                    break;
                                default:
                                    break;
                            }
                    }
                    Log.d("RES", "" + respondingTo);
                    updateViews(remoteViews, context);

                    Intent intent = new Intent(WIDGET_REFRESH);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_refresh, pendingIntent);

                    Intent stationIntent = new Intent(WIDGET_REFRESH);
                    stationIntent.putExtra("respondingto", "0");
                    PendingIntent stationPIntent = PendingIntent.getBroadcast(context, 1, stationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_station, stationPIntent);

                    Intent sceneIntent = new Intent(WIDGET_REFRESH);
                    sceneIntent.putExtra("respondingto", "1");
                    PendingIntent scenePIntent = PendingIntent.getBroadcast(context, 2, sceneIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_scene, scenePIntent);

                    Intent crIntent = new Intent(WIDGET_REFRESH);
                    crIntent.putExtra("respondingto", "2");
                    PendingIntent crPIntent = PendingIntent.getBroadcast(context, 3, crIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.widget_cancel, crPIntent);

                    Intent intent1 = new Intent(context, NavDrawerActivity.class);
                    PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 4, intent1, 0);
                    remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent1);
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (bundle.getString("respondingto") != null) {
                String respondingTo = bundle.getString("respondingto");
                this.respondingTo = respondingTo;

                Log.d("WIDGET", respondingTo);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                onUpdate(context, appWidgetManager, appWidgetIds);
            } else {
                respondingTo = null;
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        } else {
            respondingTo = null;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    private void updateRespondingStatus(int mRespondingSelected, final Context context) {
        final Intent locationService = new Intent(context, WLocationService.class);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);
        switch (mRespondingSelected) {
            case 0:
                //Station
                firebaseDatabase.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!((boolean) dataSnapshot.child("isResponding").getValue())) {
                            RespondingSystem.getInstance(context).respondStation();
                            if (mIsLocationTrackingEnabled)
                                context.startService(locationService);
                        } else {
                            //Already Responding, Clear
                            RespondingSystem.getInstance(context.getApplicationContext()).clearSelf();
                            context.stopService(locationService);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case 1:
                //Scene
                firebaseDatabase.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!((boolean) dataSnapshot.child("isResponding").getValue()) || dataSnapshot.child("respondingTo").getValue().toString() != "Station") {
                            RespondingSystem.getInstance(context).respondScene();
                            if (mIsLocationTrackingEnabled)
                                context.startService(locationService);
                        } else {
                            //Already Responding, Clear
                            RespondingSystem.getInstance(context.getApplicationContext()).clearSelf();
                            context.stopService(locationService);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case 2:
                //Can't Respond
                firebaseDatabase.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!((boolean) dataSnapshot.child("isResponding").getValue())) {
                            RespondingSystem.getInstance(context.getApplicationContext()).respondCant();
                        } else {
                            //Already Responding, Clear
                            RespondingSystem.getInstance(context.getApplicationContext()).clearSelf();
                            context.stopService(locationService);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                break;
        }
    }

    private void updateViews(RemoteViews remoteViews, Context context) {

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);

        remoteViews.setTextViewText(R.id.widget_time, mLastUpdateTime);

        if (respondingTo != null) {
            Intent locationService = new Intent(context, WLocationService.class);
            switch (respondingTo) {
                case "0":
                    //Station
                    remoteViews.setInt(R.id.widget_station, "setBackgroundResource", R.drawable.chip_loc);
                    remoteViews.setInt(R.id.widget_cr, "setBackgroundResource", 0);
                    remoteViews.setInt(R.id.widget_scene, "setBackgroundResource", 0);

                    remoteViews.setTextColor(R.id.widget_station, Color.parseColor("#000000"));
                    remoteViews.setTextColor(R.id.widget_cr, Color.parseColor("#FFFFFF"));
                    remoteViews.setTextColor(R.id.widget_scene, Color.parseColor("#FFFFFF"));

                    updateRespondingStatus(0, context);
                    break;
                case "1":
                    //Scene
                    remoteViews.setInt(R.id.widget_station, "setBackgroundResource", 0);
                    remoteViews.setInt(R.id.widget_cr, "setBackgroundResource", 0);
                    remoteViews.setInt(R.id.widget_scene, "setBackgroundResource", R.drawable.chip_loc);

                    remoteViews.setTextColor(R.id.widget_station, Color.parseColor("#FFFFFF"));
                    remoteViews.setTextColor(R.id.widget_cr, Color.parseColor("#FFFFFF"));
                    remoteViews.setTextColor(R.id.widget_scene, Color.parseColor("#000000"));

                    updateRespondingStatus(1, context);
                    break;
                case "2":
                    //Can't Respond
                    remoteViews.setInt(R.id.widget_station, "setBackgroundResource", 0);
                    remoteViews.setImageViewResource(R.id.widget_cancel, R.drawable.respond_cancel_white);
                    remoteViews.setInt(R.id.widget_scene, "setBackgroundResource", 0);

                    remoteViews.setTextColor(R.id.widget_station, Color.parseColor("#FFFFFF"));
                    remoteViews.setTextColor(R.id.widget_scene, Color.parseColor("#FFFFFF"));

                    updateRespondingStatus(2, context);
                    break;
            }
        } else {
            remoteViews.setInt(R.id.widget_station, "setBackgroundResource", 0);
            remoteViews.setImageViewResource(R.id.widget_cancel, R.drawable.responding_cancel);
            remoteViews.setInt(R.id.widget_scene, "setBackgroundResource", 0);

            remoteViews.setTextColor(R.id.widget_station, Color.parseColor("#FFFFFF"));
            remoteViews.setTextColor(R.id.widget_scene, Color.parseColor("#FFFFFF"));
        }
    }
}
