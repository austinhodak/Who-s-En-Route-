package com.fireapps.firedepartmentmanager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by austinhodak on 6/29/16.
 */

public class ScannerFragment extends Fragment {

    private ImageView playButton;
    private BroadcastReceiver receiver;
    private boolean isPlaying = false;
    private TextView mStatus;

    public ScannerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        playButton = (ImageView) view.findViewById(R.id.scanner_play);
        mStatus = (TextView) view.findViewById(R.id.scanner_current_status);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying) {
                    Intent intent = new Intent(getActivity(), AudioFeedService.class);
                    intent.setAction("PLAY");
                    getActivity().startService(intent);
                } else if (isPlaying) {
                    Intent intent = new Intent(getActivity(), AudioFeedService.class);
                    intent.setAction("PAUSE");
                    getActivity().startService(intent);
                }
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String update = intent.getStringExtra("STREAM_MESSAGE");
                if (update.equalsIgnoreCase("play")) {
                    isPlaying = true;
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_blue24dp));
                    mStatus.setText("Streaming...");
                } if (update.equalsIgnoreCase("pause")) {
                    isPlaying = false;
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_blue24dp));
                    mStatus.setText("Paused...");
                } if (update.equalsIgnoreCase("stop")) {
                    isPlaying = false;
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_blue24dp));
                    mStatus.setText("Stopped...");
                } if (update.equalsIgnoreCase("connecting")) {
                    mStatus.setText("Connecting...");
                } if (update.equalsIgnoreCase("buffering")) {
                    mStatus.setText("Buffering...");
                }
            }
        };

        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AudioFeedService.class.getName().equals(service.service.getClassName())) {
                //Running, ping for update.
                Intent intent2 = new Intent(getActivity(), AudioFeedService.class);
                intent2.setAction("UPDATE");
                getActivity().startService(intent2);
            }
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((receiver),
                new IntentFilter("STREAM_STATUS"));
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }
}
