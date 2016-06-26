package com.fireapps.firedepartmentmanager;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by austinhodak on 6/18/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    private static final String TAG = "MyFirebaseMsgService";

    SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean incidentNotificationEnabled = sharedPreferences.getBoolean("pref_incident_notify_enable", true);

        switch (remoteMessage.getData().get("notification-type")) {
            case "incident":
                if (incidentNotificationEnabled) {
                    if (sharedPreferences.getString(SettingsActivity.SettingsActivityFragment.KEY_INCIDENT_NOTIFY_TYPE, "Notification").equalsIgnoreCase("popup dialog")) {
                        Intent intent = new Intent(this, PopupActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("incidentTitle", remoteMessage.getData().get("incidentTitle"));
                        intent.putExtra("incidentDesc", remoteMessage.getData().get("incidentDesc"));
                        startActivity(intent);

                        notifyPopup(remoteMessage);
                    } else if (sharedPreferences.getString(SettingsActivity.SettingsActivityFragment.KEY_INCIDENT_NOTIFY_TYPE, "Notification").equalsIgnoreCase("notification")){
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
            default:
                break;
        }
    }

    private void notifyPopup(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, NavDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] vibrate = new long[0];
        boolean vibrateEnabled = sharedPreferences.getBoolean("pref_incident_notify_vibrate", true);

        if (vibrateEnabled) {
            vibrate = new long[]{1000, 1000, 1000, 1000, 1000};
        }

        Uri ringtoneUri = Uri.parse(sharedPreferences.getString("pref_incident_notify_ringtone", "DEFAULT_RINGTONE_URI"));

        Uri customSound = Uri.parse("android.resource://"
                + getApplicationContext().getPackageName() + "/" + R.raw.minitorfive_standardtone);

        Notification notification = new NotificationCompat.Builder(this)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.fire2)
                .setContentText(remoteMessage.getData().get("incidentDesc"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("incidentDesc")))
                .setContentTitle("NEW INCIDENT | " + remoteMessage.getData().get("incidentTitle"))
                .setSound(ringtoneUri)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(vibrate)
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