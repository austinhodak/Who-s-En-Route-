package com.fireapps.firedepartmentmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by austinhodak on 6/29/16.
 */

public class AudioFeedService extends Service {

    private MediaPlayer mediaPlayer;
    private LocalBroadcastManager broadcaster;
    private boolean isStarted = false;

    private String feedURL, feedName;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
        //startFeed();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String url = intent.getStringExtra("url");
        if (url != null) {
            if (feedURL == null) {
                feedURL = url;
                startFeed();
            } else if (feedURL == url) {

            }
        } else {
            feedURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("scannerSelectedURL", "");
            feedName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("scannerSelectedName", "");
        }
        feedName = intent.getStringExtra("name");
        handleIntent(intent);
        return START_STICKY;
    }

    public void startFeed() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(feedURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();

        sendStatusUpdates("connecting");
        Log.d("Stream", "Connecting : " + feedURL);

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                Log.d("STREAM", "onBufferingUpdate: " + i);
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("Stream", "Prepared : " + feedURL);
                mediaPlayer.start();
                sendStatusUpdates("play");
                buildNotification(generateAction(R.drawable.ic_pause_24dp, "Pause", "PAUSE", feedURL), false);
                isStarted = true;
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                if (i == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    startFeed();
                    sendStatusUpdates("connecting");
                    Log.d("Stream", "MEDIA SERVER DIED, RESTARTING");
                } else if (i == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                    mediaPlayer.prepareAsync();
                    sendStatusUpdates("connecting");
                    Log.d("Stream", "Timed Out, Restarting.");
                }
                return false;
            }
        });
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
    }

    private void buildNotification(Notification.Action action, boolean dismissible) {
        Notification.MediaStyle style = new Notification.MediaStyle();
        style.setShowActionsInCompactView(0);

        Intent intent2 = new Intent(this, NavDrawerActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent2.setAction("stream");
        PendingIntent pendingIntent2 = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap notificationLarge = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon);

        Intent intent = new Intent(getApplicationContext(), AudioFeedService.class);
        intent.setAction("STOP");
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon_nottify)
                //.setLargeIcon(notificationLarge)
                .setContentTitle("Who's En Route Scanner")
                .setContentText(feedName)
                .setColor(getResources().getColor(R.color.toolbar_background))
                .addAction(action)
                .setDeleteIntent(pendingIntent)
                .setContentIntent(pendingIntent2)
                .setStyle(style);

        if (dismissible) {
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase("STOP")) {
            stopStream();
        } else if (action.equalsIgnoreCase("PAUSE")) {
            //Log.d("Stream", "Paused");
            buildNotification(generateAction(R.drawable.ic_play_arrow_24dp, "Play", "PLAY", feedURL), true);
            mediaPlayer.pause();
            sendStatusUpdates("PAUSE");
        } else if (action.equalsIgnoreCase("PLAY")) {
            if (!isStarted){
                return;
            }
            //Log.d("Stream", "Play");
            if (intent.getStringExtra("url") != feedURL) {
                Log.d("Stream", "Disconnect from current feed: " + feedURL);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(2);
                try {
                    feedURL = intent.getStringExtra("url");
                } catch (Exception e) {
                    e.printStackTrace();
                    feedURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("scannerSelectedURL", "");
                    feedName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("scannerSelectedName", "");
                }
                mediaPlayer.stop();
                mediaPlayer.release();
                startFeed();
            } else {
                feedURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("scannerSelectedURL", "");
                feedName = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("scannerSelectedName", "");
                buildNotification(generateAction(R.drawable.ic_pause_24dp, "Pause", "PAUSE", feedURL), false);
                mediaPlayer.start();
                sendStatusUpdates("PLAY");
            }
        } else if (action.equals("UPDATE")) {
            if (mediaPlayer.isPlaying()) {
                sendStatusUpdates("PLAY");
            } else {
                sendStatusUpdates("STOP");
            }
        }
    }

    private void stopStream() {
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Stream", "Stop");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
        sendStatusUpdates("STOP");
        stopSelf();
    }

    private Notification.Action generateAction(int icon, String title, String intentAction, String URL) {
        Intent intent = new Intent(getApplicationContext(), AudioFeedService.class);
        intent.setAction(intentAction);
        intent.putExtra("url", URL);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    public void sendStatusUpdates(String update) {
        Intent intent = new Intent("STREAM_STATUS");
        if (update != null) {
            intent.putExtra("STREAM_MESSAGE", update);
        }
        broadcaster.sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
