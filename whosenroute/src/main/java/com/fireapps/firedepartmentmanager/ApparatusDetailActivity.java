package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ApparatusDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mApparatusNameTv, mInServiceTv;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;

    private List<DataSnapshot> alertsList = new ArrayList<>();
    private FloatingActionButton addAlertFAB;
    String apparatusID;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private AlertAdapter adapter;

    @BindView(R.id.apparatus_main_fab_status)
    FloatingActionButton statusFab;

    @BindView(R.id.apparatus_info_status)
    TextView statusDetailText;

    private boolean inService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apparatus_detail);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        toolbar.setSubtitleTextColor(Color.parseColor("#FFFFFF"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mApparatusNameTv = (TextView) findViewById(R.id.apparatus_info_name);
        mInServiceTv = (TextView) findViewById(R.id.text_apparatus_status);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("apparatusID")) {
                getApparatusInfo(getIntent().getExtras().getString("apparatusID"));
                apparatusID = getIntent().getExtras().getString("apparatusID");
            }
        }

        addAlertFAB = (FloatingActionButton) findViewById(R.id.apparatus_main_fab_alert);
        addAlertFAB.setOnClickListener(this);

        adapter = new AlertAdapter(alertsList, this, apparatusID);
        recyclerView = (RecyclerView) findViewById(R.id.apparatus_detail_alerts);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        statusFab.setOnClickListener(this);
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

                if ((boolean) dataSnapshot.child("isAlert").getValue()) {
                    Log.d("APPARATUS", "ALERTS FOUND");
                    //GET ALERT(s).
                    getAlerts(dataSnapshot);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
                if ((boolean) dataSnapshot.child("inService").getValue()) {
                    mInServiceTv.setText("IN SERVICE");
                    inService = true;
                } else {
                    mInServiceTv.setText("OUT OF SERVICE");
                    inService = false;
                }

                if (dataSnapshot.child("serviceStatus").exists()) {
                    statusDetailText.setVisibility(View.VISIBLE);
                    statusDetailText.setText(dataSnapshot.child("serviceStatus").getValue().toString() + " | " + dataSnapshot.child("serviceStatusTime").getValue().toString());
                } else {
                    statusDetailText.setVisibility(View.GONE);
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
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {

                        } finally {
                            alertsList.add(dataSnapshot);
                            adapter.notifyDataSetChanged();
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
                try {
                    for (DataSnapshot snapshot : alertsList) {
                        if (snapshot.getKey() == dataSnapshot.getKey()) {
                            alertsList.remove(snapshot);
                            adapter.notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {

                }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.apparatus_main_fab_alert:
                MaterialDialog alertDialog = new MaterialDialog.Builder(this)
                        .customView(R.layout.dialog_apparatus_new_alert, false)
                        .positiveText("Set Alert")
                        .negativeText("Cancel")
                        .backgroundColorRes(R.color.bottom_sheet_background)
                        .widgetColor(getResources().getColor(R.color.new_accent))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getCustomView();
                                EditText alertTitle = (EditText) view.findViewById(R.id.apparatus_alertnew_title);
                                EditText alertDesc = (EditText) view.findViewById(R.id.apparatus_alertnew_desc);
                                EditText alertPer = (EditText) view.findViewById(R.id.apparatus_alertnew_per);

                                if (alertTitle.getText().toString().length() != 0 && alertDesc.getText().toString().length() != 0 && alertPer.getText().toString().length() != 0) {
                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getDefault());
                                    Date date = new Date();
                                    String mLastUpdateTime = dateFormat.format(date);

                                    String key = mDatabase.child("apparatusAlerts").push().getKey();

                                    Map<String, Object> alertValues = new HashMap<>();
                                    alertValues.put("title", alertTitle.getText().toString());
                                    alertValues.put("desc", alertDesc.getText().toString());
                                    alertValues.put("per", alertPer.getText().toString());
                                    alertValues.put("date", mLastUpdateTime);
                                    alertValues.put("color", "F44336");

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/apparatus/" + apparatusID + "/alerts/" + key, true);
                                    childUpdates.put("/apparatus/" + apparatusID + "/isAlert", true);
                                    childUpdates.put("/apparatusAlerts/" + key, alertValues);

                                    mDatabase.updateChildren(childUpdates);

                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(ApparatusDetailActivity.this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
                FloatingActionMenu floatingActionMenu = (FloatingActionMenu) addAlertFAB.getParent();
                floatingActionMenu.close(true);

                break;
            case R.id.apparatus_main_fab_status:
                MaterialDialog statusDialog = new MaterialDialog.Builder(ApparatusDetailActivity.this)
                        .customView(R.layout.dialog_apparatus_status, false)
                        .positiveText("Save")
                        .negativeText("Cancel")
                        .backgroundColorRes(R.color.bottom_sheet_background)
                        .widgetColor(getResources().getColor(R.color.new_accent))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getCustomView();
                                Switch inServiceSwitch = (Switch) view.findViewById(R.id.apparatus_status_inservice);

                                MaterialEditText materialEditText = (MaterialEditText)view.findViewById(R.id.apparatus_status_details);

                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                dateFormat.setTimeZone(TimeZone.getDefault());
                                Date date = new Date();
                                String mLastUpdateTime = dateFormat.format(date);

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/apparatus/" + apparatusID + "/inService", inServiceSwitch.isChecked());
                                if(materialEditText.getText().toString().isEmpty()) {
                                    childUpdates.put("/apparatus/" + apparatusID + "/serviceStatus", null);
                                } else {
                                    childUpdates.put("/apparatus/" + apparatusID + "/serviceStatus", materialEditText.getText().toString());
                                }
                                childUpdates.put("/apparatus/" + apparatusID + "/serviceStatusTime", mLastUpdateTime);

                                mDatabase.updateChildren(childUpdates);

                                dialog.dismiss();
                            }
                        })
                        .autoDismiss(true)
                        .show();
                View statusDialogCustomView = statusDialog.getCustomView();

                final Switch inServiceSwitch = (Switch) statusDialogCustomView.findViewById(R.id.apparatus_status_inservice);
                inServiceSwitch.setChecked(inService);

                if (inService) {
                    inServiceSwitch.setText("In Service");
                } else {
                    inServiceSwitch.setText("Out Of Service");
                }

                inServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            inServiceSwitch.setText("In Service");
                        } else {
                            inServiceSwitch.setText("Out Of Service");
                        }
                    }
                });
                break;
        }
    }

    public static class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

        private final List<DataSnapshot> apparatusList;
        Context context;
        String apparatusID;

        public AlertAdapter(List<DataSnapshot> apparatusList, Context context, String ID) {
            this.apparatusList = apparatusList;
            this.context = context;
            this.apparatusID = ID;
        }

        @Override
        public AlertViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.apparatus_alert_template, parent, false);
            return new AlertViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(AlertViewHolder holder, int position) {
            final DataSnapshot dataSnapshot = apparatusList.get(position);

            holder.tvTitle.setText((String) dataSnapshot.child("title").getValue());
            holder.tvDesc.setText((String) dataSnapshot.child("desc").getValue());
            holder.tvPer.setText((String) dataSnapshot.child("per").getValue());
            holder.tvDate.setText((String) dataSnapshot.child("date").getValue());

            CardView cardView = (CardView) holder.itemView;
            try {
                cardView.setCardBackgroundColor(Color.parseColor("#" + dataSnapshot.child("color").getValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/apparatus/" + apparatusID + "/alerts/" + dataSnapshot.getKey(), null);
                    if (apparatusList.size() == 1) {
                        childUpdates.put("/apparatus/" + apparatusID + "/isAlert", false);
                    }
                    FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);

                    apparatusList.remove(dataSnapshot);
                    notifyDataSetChanged();

                    dataSnapshot.getRef().removeValue();

                    return false;
                }
            });

            /*@SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            try {
                Date date;

                date = dateFormat.parse((String)dataSnapshot.child("date").getValue());

                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                calendar.setTime(date);

                holder.tvDate.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
            } catch (ParseException e) {
                e.printStackTrace();
                holder.tvDate.setVisibility(View.INVISIBLE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                holder.tvDate.setVisibility(View.INVISIBLE);
            }*/
        }

        @Override
        public int getItemCount() {
            return apparatusList.size();
        }

        public static class AlertViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTitle, tvDesc, tvPer, tvDate;
            private View view;

            public AlertViewHolder(View itemView) {
                super(itemView);
                view = itemView;

                tvTitle = (TextView) view.findViewById(R.id.apparatus_alert_title);
                tvDesc = (TextView) view.findViewById(R.id.apparatus_alert_desc);
                tvPer = (TextView) view.findViewById(R.id.alert_by);
                tvDate = (TextView) view.findViewById(R.id.alert_time);

            }
        }
    }
}
