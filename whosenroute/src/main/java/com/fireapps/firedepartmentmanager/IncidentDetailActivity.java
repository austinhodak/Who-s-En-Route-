package com.fireapps.firedepartmentmanager;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IncidentDetailActivity extends AppCompatActivity {

    TextView mTitleTv, mDescTv;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setSubtitleTextColor(Color.parseColor("#FFFFFF"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTitleTv = (TextView) findViewById(R.id.incident_info_type);
        mDescTv = (TextView) findViewById(R.id.incident_info_desc);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("incidentID")) {
                getIncidentInfo(getIntent().getExtras().getString("incidentID"));
            }
        }
    }

    private void getIncidentInfo(final String incidentID) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Message incident = dataSnapshot.getValue(Message.class);
                // ...
                if (incident == null) {
                    return;
                }
                String desc = incident.getMessage();
                desc = desc.replace("\r\n", " ").replace("\n", " ");
                mTitleTv.setText(incident.getType());
                mDescTv.setText(desc);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("INCIDENTDETAIL", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.child("messages/" + incidentID).addValueEventListener(postListener);
    }
}
