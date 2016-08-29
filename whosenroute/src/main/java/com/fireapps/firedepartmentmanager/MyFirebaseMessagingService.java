package com.fireapps.firedepartmentmanager;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by austinhodak on 6/18/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        String department = remoteMessage.getData().get("department");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean incidentNotificationEnabled = true;
        boolean mManpowerNotificationEnable = true;
        boolean tonesNotifyEnable = true;
        if (department != null) {
            incidentNotificationEnabled = sharedPreferences.getBoolean(department + "_incidentNotify", true);
            mManpowerNotificationEnable = sharedPreferences.getBoolean(department + "_manpowerMaster", true);
            tonesNotifyEnable = sharedPreferences.getBoolean(department + "_tonesMaster", true);
        } else {
            incidentNotificationEnabled = sharedPreferences.getBoolean("pref_incident_notify_enable", true);
            mManpowerNotificationEnable = sharedPreferences.getBoolean("pref_incident_notify_manpower", true);
            tonesNotifyEnable = sharedPreferences.getBoolean("pref_tones_notify_enable", true);
        }

        switch (remoteMessage.getData().get("notification-type")) {
            case "incident":
                if (incidentNotificationEnabled) {
                    if (sharedPreferences.getString(SettingsActivity.SettingsMain.KEY_INCIDENT_NOTIFY_TYPE, "Notification").equalsIgnoreCase("popup dialog")) {

                        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                /*Bug Fix! Was only showing IF responding, fixed. !((boolean) dataSnapshot.child("isResponding").getValue())*/
                                if ((boolean)dataSnapshot.child("isResponding").getValue()) {
                                    //Responding, Don't Show Popup, Still Notify
                                } else {
                                    Intent intent = new Intent(MyFirebaseMessagingService.this, PopupActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("incidentTitle", remoteMessage.getData().get("incidentTitle"));
                                    intent.putExtra("incidentDesc", remoteMessage.getData().get("incidentDesc"));
                                    intent.putExtra("department", remoteMessage.getData().get("department"));
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        notifyPopup(remoteMessage);
                    } else if (sharedPreferences.getString(SettingsActivity.SettingsMain.KEY_INCIDENT_NOTIFY_TYPE, "Notification").equalsIgnoreCase("notification")) {
                        //No popup, just notification
                        notifyPopup(remoteMessage);
                    }
                } else {
                    //Notifications disabled for incidents, do nothing.
                    break;
                }
                break;
            case "responding":
                break;
            case "apparatus":
                break;
            case "manpower":
                if (mManpowerNotificationEnable)
                    notifyManpower(remoteMessage);
                break;
            case "tones":
                if (tonesNotifyEnable)
                    notifyPopup(remoteMessage);
                break;
            default:
                break;
        }
    }

    private void notifyPopup(RemoteMessage remoteMessage) {
        String department = remoteMessage.getData().get("department");

        Intent intent = new Intent(this, NavDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stationIntent = new Intent(this, NavDrawerActivity.class);
        stationIntent.setAction(Constants.ACTION_RESPONDING_STATION);
        PendingIntent pendingIntentStation = PendingIntent.getActivity(this, 1, stationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent sceneIntent = new Intent(this, NavDrawerActivity.class);
        stationIntent.setAction(Constants.ACTION_RESPONDING_SCENE);
        PendingIntent pendingIntentScene = PendingIntent.getActivity(this, 2, sceneIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent crIntent = new Intent(this, NavDrawerActivity.class);
        stationIntent.setAction(Constants.ACTION_RESPONDING_CR);
        PendingIntent crPendingIntent = PendingIntent.getActivity(this, 3, crIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] vibrate = new long[0];

        Uri ringtoneUri;
        boolean vibrateEnabled;
        if (department != null) {
            vibrateEnabled = sharedPreferences.getBoolean(department + "_incidentVibrate", true);
            ringtoneUri = Uri.parse(sharedPreferences.getString(department + "_incidentSound", "content://settings/system/notification_sound"));
        } else {
            vibrateEnabled = sharedPreferences.getBoolean("pref_incident_notify_vibrate", true);
            ringtoneUri = Uri.parse(sharedPreferences.getString("pref_incident_notify_ringtone", "content://settings/system/notification_sound"));
        }

        if (vibrateEnabled) {
            vibrate = new long[]{1000, 1000, 1000, 1000, 1000};
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.icon_nottify)
                .setContentText(remoteMessage.getData().get("incidentDesc"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("incidentDesc")))
                .setContentTitle("NEW INCIDENT | " + remoteMessage.getData().get("incidentTitle"))
                .setSound(ringtoneUri)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(vibrate)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.fire_station_24, "Station", pendingIntentStation).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.responding_cancel_24, "Can't Respond", crPendingIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.fire_24, "Scene", pendingIntentScene).build())
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    private void notifyManpower(RemoteMessage remoteMessage) {
        String department = remoteMessage.getData().get("department");

        Intent intent = new Intent(this, NavDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stationIntent = new Intent(this, NavDrawerActivity.class);
        stationIntent.setAction(Constants.ACTION_RESPONDING_STATION);
        PendingIntent pendingIntentStation = PendingIntent.getActivity(this, 1, stationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent sceneIntent = new Intent(this, NavDrawerActivity.class);
        stationIntent.setAction(Constants.ACTION_RESPONDING_SCENE);
        PendingIntent pendingIntentScene = PendingIntent.getActivity(this, 2, sceneIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent crIntent = new Intent(this, NavDrawerActivity.class);
        stationIntent.setAction(Constants.ACTION_RESPONDING_CR);
        PendingIntent crPendingIntent = PendingIntent.getActivity(this, 3, crIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] vibrate = new long[0];

        Uri ringtoneUri;
        boolean vibrateEnabled;
        if (department != null) {
            vibrateEnabled = sharedPreferences.getBoolean(department + "_incidentVibrate", true);
            ringtoneUri = Uri.parse(sharedPreferences.getString(department + "_incidentSound", "content://settings/system/notification_sound"));
        } else {
            vibrateEnabled = sharedPreferences.getBoolean("pref_incident_notify_vibrate", true);
            ringtoneUri = Uri.parse(sharedPreferences.getString("pref_incident_notify_ringtone", "content://settings/system/notification_sound"));
        }

        if (vibrateEnabled) {
            vibrate = new long[]{1000, 1000, 1000, 1000, 1000};
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.icon_nottify)
                //.setContentText(remoteMessage.getData().get("incidentDesc"))
                //.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("incidentDesc")))
                .setContentTitle("More Manpower Needed.")
                .setSound(ringtoneUri)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(vibrate)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.fire_station_24, "Station", pendingIntentStation).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.responding_cancel_24, "Can't Respond", crPendingIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.fire_24, "Scene", pendingIntentScene).build())
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        /*Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());*/
    }
}