package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApparatusDetailActivity extends AppCompatActivity {

    private TextView mApparatusNameTv;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;

    private List<DataSnapshot> alertsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apparatus_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setSubtitleTextColor(Color.parseColor("#FFFFFF"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mApparatusNameTv = (TextView) findViewById(R.id.apparatus_info_name);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("apparatusID")) {
                getApparatusInfo(getIntent().getExtras().getString("apparatusID"));
            }
        }
    }

    private void getApparatusInfo(final String apparatusID) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Apparatus apparatus = dataSnapshot.getValue(Apparatus.class);
                // ...
                if (apparatus == null) {
                    return;
                }
                mApparatusNameTv.setText(apparatus.getApparatusName());

                if ((boolean)dataSnapshot.child("isAlert").getValue()) {
                    Log.d("APPARATUS", "ALERTS FOUND");
                    //GET ALERT(s).
                    getAlerts(dataSnapshot);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("APPARATUSDETAIL", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.child("apparatus").child(apparatusID).addValueEventListener(postListener);
    }

    private void getAlerts(DataSnapshot dataSnapshot) {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            for (DataSnapshot snapshot : alertsList) {
                                if (snapshot.getKey() == dataSnapshot.getKey()) {
                                    alertsList.remove(snapshot);
                                }
                            }
                        } catch (Exception e) {

                        } finally {
                            alertsList.add(dataSnapshot);
                            updateAlertsList();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mDatabase.child("apparatusAlerts").child(dataSnapshot.getKey()).addValueEventListener(valueEventListener);

                Log.d("APPARATUS", dataSnapshot.getKey());
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
        };
        mDatabase.child("apparatus/" + dataSnapshot.getKey()).child("alerts").addChildEventListener(childEventListener);
    }

    private void updateAlertsList() {
        for (DataSnapshot dataSnapshot : alertsList) {
            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.apparatus_alert_template, null);

            CardView cardView = (CardView) v.findViewById(R.id.apparatus_info_alert_card);
            cardView.setCardBackgroundColor(Color.parseColor("#" + dataSnapshot.child("color").getValue()));

            TextView alertTitle = (TextView) v.findViewById(R.id.apparatus_alert_title);
            alertTitle.setText((String)dataSnapshot.child("title").getValue());

            TextView alertDetail = (TextView) v.findViewById(R.id.apparatus_alert_desc);
            alertDetail.setText((String)dataSnapshot.child("desc").getValue());

            ViewGroup insertPoint = (ViewGroup) findViewById(R.id.apparatus_detail_cards);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            Resources r = getResources();
            float pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
            layoutParams.setMargins(Math.round(pxBottomMargin), Math.round(pxBottomMargin), Math.round(pxBottomMargin), Math.round(pxBottomMargin));

            //insertPoint.addView(v, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            insertPoint.addView(v, 1, layoutParams);
        }
    }
}
