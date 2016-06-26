package com.fireapps.firedepartmentmanager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Implementation of App Widget functionality.
 */
public class RespondingWidget extends AppWidgetProvider {

    public static String STATION_BUTTON = "STATION_BUTTON";
    public static String NR_BUTTON = "NR_BUTTON";
    public static String SCENE_BUTTON = "SCENE_BUTTON";
    private static Query userFirebase;
    private static DatabaseReference firebase;
    private static String respondingStatus;

    public static String WIDGET_BUTTON = "com.fireapps.firedepartmentmanager.WIDGET_BUTTON";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.responding_widget_new);

//            Intent intent = new Intent(WIDGET_BUTTON);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            views.setOnClickPendingIntent(R.id.widget_refresh, pendingIntent );

            appWidgetManager.updateAppWidget(appWidgetId, views);

            //updateAppWidget(context, appWidgetManager, appWidgetId);

            /*// Create an Intent to launch ExampleActivity
            Intent station = new Intent(context, MainActivity.class);
            station.putExtra("RT", "station");

            Intent scene = new Intent(context, MainActivity.class);
            scene.putExtra("RT", "scene");

            Intent nr = new Intent(context, MainActivity.class);
            nr.putExtra("RT", "nr");

            PendingIntent stationpendingIntent = PendingIntent.getActivity(context, 0, station, PendingIntent.FLAG_CANCEL_CURRENT);
            PendingIntent scenependingIntent2 = PendingIntent.getActivity(context, 1, scene, PendingIntent.FLAG_CANCEL_CURRENT);
            PendingIntent nrpendingIntent2 = PendingIntent.getActivity(context, 2, nr, PendingIntent.FLAG_CANCEL_CURRENT);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button

            //views.setOnClickPendingIntent(R.id.responding_widget_station, stationpendingIntent);
            //views.setOnClickPendingIntent(R.id.responding_widget_scene, scenependingIntent2);
            //views.setOnClickPendingIntent(R.id.responding_widget_nr, nrpendingIntent2);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);*/
        }
    }

    public static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                       final int appWidgetId){

       /* final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.responding_widget_new);
        //updateViews.setTextViewText(R.id.widgettext, "[" + String.valueOf(appWidgetId) + "]" + strWidgetText);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        firebase = database.getReference("users");
        userFirebase = firebase.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // do some stuff once
                MemberObjectFire memberObjectFire = snapshot.getValue(MemberObjectFire.class);

                respondingStatus = memberObjectFire.getRespondingTo();

                if (respondingStatus == null) {
                    //nav.setSelectedIndex(0, true);

                    views.setInt(R.id.widget_station, "setBackgroundDrawable", 0);
                    views.setInt(R.id.widget_cr, "setBackgroundDrawable", 0);
                    views.setInt(R.id.widget_scene, "setBackgroundDrawable", 0);

                    //editor.putString("respondingTo", "none");
                    //editor.apply();
                } else {

                    switch (respondingStatus) {
                        case "Station":
                            //nav.setSelectedIndex(1, true);

                            //editor.putString("respondingTo", "station");
                            //editor.apply();

                            views.setInt(R.id.widget_station, "setBackgroundDrawable", R.drawable.chip_loc);
                            views.setInt(R.id.widget_cr, "setBackgroundDrawable", 0);
                            views.setInt(R.id.widget_scene, "setBackgroundDrawable", 0);
                            break;
                        case "At Station":
                            //nav.setSelectedIndex(1, true);

                            //editor.putString("respondingTo", "station");
                            //editor.apply();

                            views.setInt(R.id.widget_station, "setBackgroundDrawable", R.drawable.chip_loc);
                            views.setInt(R.id.widget_cr, "setBackgroundDrawable", 0);
                            views.setInt(R.id.widget_scene, "setBackgroundDrawable", 0);
                            break;
                        case "Scene":
                            //nav.setSelectedIndex(3, true);

                            //editor.putString("respondingTo", "scene");
                            //editor.apply();

                            views.setInt(R.id.widget_station, "setBackgroundDrawable", 0);
                            views.setInt(R.id.widget_cr, "setBackgroundDrawable", 0);
                            views.setInt(R.id.widget_scene, "setBackgroundDrawable", R.drawable.chip_loc);
                            break;
                        case "NR":
                            //nav.setSelectedIndex(2, true);

                            //editor.putString("respondingTo", "nr");
                            //editor.apply();

                            views.setInt(R.id.widget_station, "setBackgroundDrawable", 0);
                            views.setInt(R.id.widget_cr, "setBackgroundDrawable", R.drawable.chip_loc);
                            views.setInt(R.id.widget_scene, "setBackgroundDrawable", 0);
                            break;
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });*/

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (WIDGET_BUTTON.equals(intent.getAction())) {

            Bundle extras = intent.getExtras();
            if(extras!=null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), RespondingWidget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

                onUpdate(context, appWidgetManager, appWidgetIds);
            }

            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.responding_widget_new);


        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

