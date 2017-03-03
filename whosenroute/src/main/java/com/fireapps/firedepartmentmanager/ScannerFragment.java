package com.fireapps.firedepartmentmanager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by austinhodak on 6/29/16.
 */

public class ScannerFragment extends Fragment {

    private ImageView playButton;
    private BroadcastReceiver receiver;
    private boolean isPlaying = false;
    private TextView mStatus;
    private SharedPreferences sharedPreferences;

    //private String[] items = new String[]{};
    List<String> items = new ArrayList<>();
    List<String> itemIds = new ArrayList<>();
    List<String> itemLocations = new ArrayList<>();
    List<String> itemsTitle = new ArrayList<>();

    String selectedURL, selectedName;
    private String selectedLocation;

    private int selected;

    public ScannerFragment() {

    }

    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        selectedURL = sharedPreferences.getString("scannerSelectedURL", "");
        selectedName = sharedPreferences.getString("scannerSelectedName", "Select a Feed.");
        selectedLocation = sharedPreferences.getString("scannerSelectedLocation", "");

        playButton = (ImageView) view.findViewById(R.id.scanner_play);
        mStatus = (TextView) view.findViewById(R.id.scanner_current_status);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying) {
                    Intent intent = new Intent(getActivity(), AudioFeedService.class);
                    intent.setAction("PLAY");
                    intent.putExtra("url", selectedURL);
                    intent.putExtra("name", selectedName);
                    getActivity().startService(intent);
                } else if (isPlaying) {
                    Intent intent = new Intent(getActivity(), AudioFeedService.class);
                    intent.setAction("PAUSE");
                    intent.putExtra("name", selectedName);
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
                Log.d("STATUS UPDATE", update);
            }
        };

        final TextView scannerName = (TextView)view.findViewById(R.id.scanner_name);
        final TextView scannerDetails = (TextView)view.findViewById(R.id.scanner_details);
        scannerName.setText(selectedName);
        scannerDetails.setText(selectedLocation);

        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AudioFeedService.class.getName().equals(service.service.getClassName())) {
                //Running, ping for update.
                Intent intent2 = new Intent(getActivity(), AudioFeedService.class);
                intent2.setAction("UPDATE");
                getActivity().startService(intent2);
            }
        }

        final Button selectFeed = (Button) view.findViewById(R.id.scanner_select_button);
        selectFeed.setEnabled(false);
        selectFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Select Feed")
                        .items(items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                sharedPreferences.edit().putString("scannerSelectedURL", itemIds.get(which)).apply();
                                sharedPreferences.edit().putString("scannerSelectedName", itemsTitle.get(which)).apply();
                                sharedPreferences.edit().putString("scannerSelectedLocation", itemLocations.get(which)).apply();
                                if (selected != which) {
                                    if (isPlaying) {
                                        //Playing, so stop and restart with new url.
                                        Intent intent = new Intent(getActivity(), AudioFeedService.class);
                                        intent.setAction("PLAY");
                                        intent.putExtra("url", itemIds.get(which));
                                        intent.putExtra("name", itemsTitle.get(which));
                                        getActivity().startService(intent);
                                    } else {
                                        //Not playing, don't start.
                                    }
                                } else {
                                    //Already selected same feed.
                                }

                                //Toast.makeText(getActivity(), "Selected Feed Url: " + itemIds.get(which), Toast.LENGTH_SHORT).show();



                                selectedName = itemsTitle.get(which);
                                selectedURL = itemIds.get(which);
                                selectedLocation = itemLocations.get(which);

                                scannerName.setText(itemsTitle.get(which));
                                scannerDetails.setText(itemLocations.get(which));

                                selected = which;
                            }
                        }).show();
            }
        });

        mDatabase.child("departments/"+RespondingSystem.getInstance(getActivity()).getCurrentDepartmentKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String countyID = dataSnapshot.child("county").getValue().toString();
                mDatabase.child("counties/"+countyID+"/streams").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String location = dataSnapshot.child("location").getValue().toString();
                        String name = dataSnapshot.child("name").getValue().toString();
                        String url = dataSnapshot.child("url").getValue().toString();

                        items.add(name + " - " + location);
                        itemIds.add(url);
                        itemLocations.add(location);
                        itemsTitle.add(name);

                        selectFeed.setEnabled(true);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
