package com.fireapps.firedepartmentmanager;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by austinhodak on 4/21/16.
 */
public class RegistrationIntentService extends IntentService {
    JSONObject jsonObject;
    private DatabaseReference firebase;
    private SharedPreferences sharedPref;

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        firebase = database.getReference("users/");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean pushEnabled = sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_PUSH_ENABLE, true);
        boolean pushWhenNotRespondingEnabled = sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_PUSH_ENABLE_NR, true);

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            if (extras.getString("sendersID").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                //Don't notify, same user.
            } else {
                //Notify, not same user.

                if (pushEnabled) {
                    //If current user is not responding, check if push when not responding is checked, and if so, notify.
                    if(sharedPref.getString("respondingTo", "none") == null && pushWhenNotRespondingEnabled){
                    //If not, don't notify.
                        sendNotification(extras.getString("respondingTo"));
                    } else if(sharedPref.getString("respondingTo", "none") != null) {
                        sendNotification(extras.getString("respondingTo"));
                    }
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String s) {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntentStation = PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class).putExtra("RT", "Station"), FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentScene = PendingIntent.getActivity(this, 2, new Intent(this, MainActivity.class).putExtra("RT", "Scene"), FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentNR = PendingIntent.getActivity(this, 3, new Intent(this, MainActivity.class).putExtra("RT", "NR"), FLAG_UPDATE_CURRENT);

        String contentText = null;
        int icon = R.drawable.fire;

        long when = System.currentTimeMillis();

        long[] v;

        if (sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_NOTIFY_VIBRATE, true)) {
            v = new long[]{500, 1000};
        } else {
            v = new long[]{0, 0};
        }

        Uri uri = Uri.parse(sharedPref.getString(SettingsActivityFragment.KEY_PREF_NOTIFY_SOUND, "default ringtone"));


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle("Who's En Route?")
                .setContentText(s)
                .setWhen(when)
                .setVibrate(v)
                .setSound(uri)
                .setPriority(2)
                .setColor(getResources().getColor(R.color.primary))
                .addAction(R.drawable.ic_location_city_black_24dp, "Station", pendingIntentStation)
                .addAction(R.drawable.ic_clear_24dp, "Can't Respond", pendingIntentNR)
                .addAction(R.drawable.ic_whatshot_24dp, "Scene", pendingIntentScene);
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(pendingIntent);
        notificationManager.notify(1, mBuilder.build());
    }

    private PendingIntent notifyButtonStation() {
        return null;
    }

    private PendingIntent notifyButtonScene() {
        return null;
    }

}
