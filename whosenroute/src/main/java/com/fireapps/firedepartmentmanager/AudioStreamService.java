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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by austinhodak on 6/29/16.
 */

public class AudioStreamService extends Service {

    private static AudioStreamService instance;
    private MediaPlayer mediaPlayer;
    private LocalBroadcastManager broadcaster;
    private boolean isStarted = false;

    private String feedURL, feedName;
    private StreamListener streamListener;

    private Context context;

    private List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private ArrayMap<String, MediaPlayer> mediaPlayerArray = new ArrayMap<>();

    public AudioStreamService(Context context1) {
        context = context1;
    }

    public AudioStreamService() {
    }

    public void setStreamListener(AudioStreamService.StreamListener streamListener) {
        this.streamListener = streamListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public synchronized static AudioStreamService getInstance(Context context1) {
        if (instance == null) {
            instance = new AudioStreamService(context1);

        }
        return instance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            Log.d("STREAMUPDATE", intent.getAction() + " - NOTIFICATION ACTION");
            handleIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface StreamListener {
        public void onStreamLoaded(String streamURL, String streamName, MediaPlayer stream);
        public void onStreamStopped(String streamURL);
        public void onStreamPaused(String streamURL);
        public void onStreamResumed(String streamURL);
    }


    private void handleIntent(Intent intent) {
        if (intent.getAction() == null) {
            return;
        }
        switch (intent.getAction().toLowerCase()) {
            case "stopall":
                stopAllStreams();
                break;
            case "pauseall":
                pauseAllStreams();
                break;
            case "play":
                resumeAllStreams();
                break;
        }
    }

    private void stopAllStreams() {
        for (int i = 0; i < mediaPlayerArray.size(); i++) {
            mediaPlayerArray.valueAt(i).stop();
            mediaPlayerArray.valueAt(i).release();
            streamListener.onStreamStopped(mediaPlayerArray.keyAt(i));
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
    }

    private void pauseAllStreams() {
        for (int i = 0; i < mediaPlayerArray.size(); i++) {
            mediaPlayerArray.valueAt(i).pause();
            streamListener.onStreamPaused(mediaPlayerArray.keyAt(i));
        }
        buildNotification(generateAction(R.drawable.ic_play_arrow_24dp, "Play", "play", ""), false, mediaPlayerArray.size() + " Scanners Playing.", mediaPlayerArray.size(), "");
    }

    private void resumeAllStreams() {
        for (int i = 0; i < mediaPlayerArray.size(); i++) {
            mediaPlayerArray.valueAt(i).start();
            streamListener.onStreamResumed(mediaPlayerArray.keyAt(i));
        }
        buildNotification(generateAction(R.drawable.ic_pause_24dp, "Pause", "pauseall", ""), false, mediaPlayerArray.size() + " Scanners Playing.", mediaPlayerArray.size(), "");
    }

    public void startStream(final String streamURL, final String streamName, final String streamLocation) throws IOException {
        if (mediaPlayerArray.keySet().contains(streamURL)) {
            //Already streaming, send update just in-case.
            streamListener.onStreamResumed(streamURL);
            return;
        }
        Log.d("STREAMAUDIO", "STARTING");
        final MediaPlayer stream = new MediaPlayer();
        stream.setAudioStreamType(AudioManager.STREAM_MUSIC);
        stream.setDataSource(streamURL);
        stream.prepareAsync();
        stream.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                stream.start();
                streamListener.onStreamLoaded(streamURL, streamName, stream);
                Log.d("STREAMAUDIO", "STARTED");
                buildNotification(generateAction(R.drawable.ic_pause_24dp, "Pause", "pauseall", streamURL), false, streamName, mediaPlayerArray.size(), streamLocation);
            }
        });
        stream.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.d("STREAMUPDATE", percent + "");
            }
        });
        mediaPlayerArray.put(streamURL, stream);
    }

    public void pauseStream(String streamURL) {
        mediaPlayerArray.get(streamURL).pause();
        streamListener.onStreamPaused(streamURL);

        buildNotification(generateAction(R.drawable.ic_play_arrow_24dp, "Play", "play", streamURL), false, mediaPlayerArray.size() + " Scanners Playing.", mediaPlayerArray.size(), "");
    }

    public void stopStream(String streamURL) {
        mediaPlayerArray.get(streamURL).stop();
        mediaPlayerArray.get(streamURL).release();
        mediaPlayerArray.remove(streamURL);
        streamListener.onStreamStopped(streamURL);

        Log.d("STREAMAUDIO", "STOPPING");

        if (mediaPlayerArray.size() == 0) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(2);
        } else {
            buildNotification(generateAction(R.drawable.ic_pause_24dp, "Pause", "pauseall", streamURL), false, mediaPlayerArray.size() + " Scanners Playing.", mediaPlayerArray.size(), "");
        }
    }

    public ArrayMap<String, MediaPlayer> getCurrentStreams() {
        return mediaPlayerArray;
    }

    private void buildNotification(Notification.Action action, boolean dismissible, String title, int totalScanners, String location) {
        Notification.MediaStyle style = new Notification.MediaStyle();
        style.setShowActionsInCompactView(0);

        Intent intent2 = new Intent(context, NavDrawerActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent2.setAction("stream");
        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap notificationLarge = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);

        Intent intent = new Intent(context, AudioStreamService.class);
        intent.setAction("stopall");
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, 0);

        int icon;

        switch (mediaPlayerArray.size()) {
            case 1:
                icon = R.drawable.stream1;
                break;
            case 2:
                icon = R.drawable.stream2;
                break;
            case 3:
                icon = R.drawable.stream3;
                break;
            default:
                icon = R.drawable.streamplus;
                break;
        }

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(icon)
                //.setLargeIcon(notificationLarge)
                .setContentTitle(title)
                .setColor(context.getResources().getColor(R.color.toolbar_background))
                .addAction(action)
                .setDeleteIntent(pendingIntent)
                .setContentIntent(pendingIntent2)
                .setStyle(style);

        if (totalScanners > 1) {
            builder.setContentText(totalScanners + " Scanners Playing.");
        } else {
            builder.setContentText(location);
        }

        if (dismissible) {
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }

    private Notification.Action generateAction(int icon, String title, String intentAction, String URL) {
        Intent intent = new Intent(context, AudioStreamService.class);
        intent.setAction(intentAction);
        intent.putExtra("url", URL);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }
}
