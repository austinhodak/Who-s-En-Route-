package com.fireapps.firedepartmentmanager;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IncidentDetailActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private GoogleMap map;
    private Geocoder geocoder;
    private List<Address> addressList = new ArrayList<>();
    private DataSnapshot message;
    private String departmentID;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.incident_info_type)
    TextView mTitleTv;

    @BindView(R.id.incident_info_desc)
    TextView mDescTv;

    @BindView(R.id.incident_info_date)
    TextView mDateTv;

    @BindView(R.id.incident_detail_activeCard)
    CardView activeCard;

    @OnClick(R.id.incident_detail_activeCard_buttonNO)
    void noButtonClick(View view) {
        activeCard.setVisibility(View.GONE);
        Snackbar.make(findViewById(android.R.id.content), "Message Marked Inactive", Snackbar.LENGTH_SHORT).setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activeCard.setVisibility(View.VISIBLE);
            }
        }).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);

                if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    return;
                }

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/messages/" + message.getKey() + "/activeIncident", false);

                mDatabase.updateChildren(childUpdates);
            }
        }).show();
    }

    @OnClick(R.id.incident_detail_activeCard_buttonYES)
    void yesButtonClick(View view) {
        activeCard.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setSubtitleTextColor(Color.parseColor("#FFFFFF"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("incidentID")) {
                getIncidentInfo(getIntent().getExtras().getString("incidentID"));
                departmentID = getIntent().getExtras().getString("departmentID");
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
                message = dataSnapshot;
                String desc = incident.getMessage();
                desc = desc.replace("\r\n", " ").replace("\n", " ");
                mTitleTv.setText(incident.getType());
                mDescTv.setText(desc);
                mDateTv.setText(incident.getDate());

                if (incident.isActiveIncident()) {
                    activeCard.setVisibility(View.VISIBLE);
                } else {
                    activeCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("INCIDENTDETAIL", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("messages/" + incidentID).addValueEventListener(postListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.incident_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.message_detail_delete:
                firebaseDatabase.getReference("departments").child(departmentID).child("messages").child(message.getKey()).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        setResult(100);
                        finish();
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
