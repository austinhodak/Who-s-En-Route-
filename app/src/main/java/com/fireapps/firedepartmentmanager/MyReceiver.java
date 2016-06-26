package com.fireapps.firedepartmentmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Austin on 1/17/2015.
 */
public class MyReceiver extends GcmListenerService {

    public static final String ACTION="com.fireapps.whoisresponding.MainNavigation";
    public static final String PARSE_EXTRA_DATA_KEY="com.parse.Data";
    public static final String PARSE_JSON_ALERT_KEY="alert";
    public static final String PARSE_JSON_CHANNELS_KEY="com.parse.Channel";

    private static final String TAG = "BroadcastReceiver";
    private IDateCallback callerActivity;
    private String objectId;

    Context context;

    SharedPreferences sharedPreferences;

    public interface IDateCallback{
        void call();
    }

    /*@Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            String action = intent.getAction();

            sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

            //"com.parse.Channel"
            String channel =
                    intent.getExtras()
                            .getString(PARSE_JSON_CHANNELS_KEY);

            JSONObject json =
                    new JSONObject(
                            intent.getExtras()
                                    .getString(PARSE_EXTRA_DATA_KEY));



            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            Iterator itr = json.keys();
            while (itr.hasNext())
            {
                String key = (String) itr.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));
            }
            try {
                callerActivity.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            boolean pushEnabled = sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_PUSH_ENABLE, true);
            boolean pushWhenNotRespondingEnabled = sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_PUSH_ENABLE_NR, true);

            objectId = json.getString("object");

            //Notification not sent from current user.
            if(!ParseUser.getCurrentUser().getObjectId().equals(objectId)){

                context.sendBroadcast(new Intent("UPDATE_LIST"));

                //Check if push setting is enabled.
                if(pushEnabled){
                    //If current user is not responding, check if push when not responding is checked, and if so, notify.
                    if(ParseUser.getCurrentUser().getString("respondingTo") == null && pushWhenNotRespondingEnabled){
                        //If not, don't notify.
                        notify(context, intent, json);
                    } else if(ParseUser.getCurrentUser().getString("respondingTo") != null) {
                        notify(context, intent, json);
                    }
                }
            }
        }
        catch (JSONException e)
        {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }*/


    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

    }

    private void notify(Context ctx, Intent i, JSONObject dataObject)
            throws JSONException
    {
        context = ctx;

        try {
            callerActivity.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int icon = R.mipmap.ic_launcher;
        String contextText =
                dataObject.getString("customdata");
        long when = System.currentTimeMillis();


        long[] v = {500,1000};
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.fs)
                .setContentTitle("FDM Responding")
                .setContentText(contextText)
                .setWhen(when)
                .setVibrate(v)
                .setSound(uri)
                .addAction(R.drawable.ic_domain_24dp, "Station", notifyButtonStation())
                .addAction(R.drawable.ic_directions_car_24dp, "Scene", notifyButtonScene());


        mBuilder.setContentIntent(notifyButtonDefault());

        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private PendingIntent notifyButtonStation(){

        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntentStation = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            // Perform the operation associated with our pendingIntent
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("respondingTo", "station");
            editor.apply();

            Log.d("Notification", "PendingIntentActivated");

            pendingIntentStation.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        return pendingIntentStation;

    }

    private PendingIntent notifyButtonScene(){

        Intent intent2 = new Intent(context, MainActivity.class);

        PendingIntent pendingIntentScene = PendingIntent.getActivity(context, 1, intent2, PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntentScene;

    }

    private PendingIntent notifyButtonDefault(){

        Intent intent3 = new Intent(context, MainActivity.class);

        PendingIntent pendingIntentNone = PendingIntent.getActivity(context, 1, intent3, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pendingIntentNone.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        return pendingIntentNone;

    }

}
