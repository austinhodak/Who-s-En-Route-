package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by austinhodak on 8/24/16.
 */

public class DepartmentList extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.department_list_rv)
    RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    @BindView(R.id.department_list_progress)
    ProgressBar progressBar;

    @BindView(R.id.header_image)
    ImageView headerImage;

    @BindView(R.id.departmentList_done)
    FloatingActionButton doneFab;

    @OnClick(R.id.department_list_add)
    void departmentAdd(View view) {
        Intent intent = new Intent(this, DepartmentAdd.class);
        startActivityForResult(intent, 1);
    }

    DepartmentAdapter departmentAdapter;
    List<DataSnapshot> dataSnapshots = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departments);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle("Manage Departments");

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        departmentAdapter = new DepartmentAdapter(dataSnapshots, this);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(departmentAdapter);

        Glide.with(this).load(R.drawable.wallnight).crossFade().into(headerImage);
        //headerImage.setColorFilter(Color.BLACK, PorterDuff.Mode.DARKEN);

        loadDepartments();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadDepartments() {
        RespondingSystem.getInstance(getApplicationContext()).loadInitialData().setDepartmentListener(new RespondingSystem.DepartmentListener() {
            @Override
            public void onDepartmentAdded(DataSnapshot snapshot, Uri icon) {
                dataSnapshots.add(snapshot);
                departmentAdapter.notifyDataSetChanged();

                Log.d("DepartmentList", departmentAdapter.getItemCount() + "");

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onDepartmentUpdated(DataSnapshot snapshot) {

            }

            @Override
            public void onLoadingFinished() {

            }

            @Override
            public void onDepartmentAddedFirstTime(DataSnapshot snapshot) {

            }
        });
    }

    private String selectedDept;
    public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentViewHolder> {

        private final List<DataSnapshot> apparatusList;
        Context context;
        SharedPreferences sharedPreferences;

        public DepartmentAdapter(List<DataSnapshot> apparatusList, Context context) {
            this.apparatusList = apparatusList;
            this.context = context;
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        @Override
        public DepartmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_department, parent, false);
            return new DepartmentViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DepartmentViewHolder holder, int position) {
            final DataSnapshot dataSnapshot = apparatusList.get(position);
            final Department member = dataSnapshot.getValue(Department.class);


            holder.name.setText(dataSnapshot.child("name").getValue().toString());
            holder.abbrv.setText(dataSnapshot.child("abbrv").getValue().toString());
            holder.notificationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedDept = dataSnapshot.getKey();
                    MaterialDialog alertDialog = new MaterialDialog.Builder(DepartmentList.this)
                            .customView(R.layout.dialog_department_notifications, false)
                            .positiveText("Save")
                            .negativeText("Cancel")
                            .backgroundColorRes(R.color.bottom_sheet_background)
                            .widgetColor(getResources().getColor(R.color.new_accent))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    View view = dialog.getCustomView();

                                }
                            })
                            .autoDismiss(true)
                            .show();
                    View view2 = alertDialog.getCustomView();
                    TextView title = (TextView) view2.findViewById(R.id.dialog_alert_title);
                    title.setText(member.getAbbrv() + " Notifications");

                    final TextView sound = (TextView) view2.findViewById(R.id.deptNotify_Sound);
                    sound.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sharedPreferences.getString(selectedDept + "_incidentSound", "content://settings/system/notification_sound")));
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALL);
                            DepartmentList.this.startActivityForResult(intent, 999);
                        }
                    });



                    /* Incident Notification Screen On Switch */
                    final Switch deptNotify_Screen = (Switch) view2.findViewById(R.id.deptNotify_Screen);
                    deptNotify_Screen.setChecked(sharedPreferences.getBoolean(selectedDept + "_incidentScreenOn", true));
                    deptNotify_Screen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            sharedPreferences.edit().putBoolean(selectedDept + "_incidentScreenOn", b).apply();
                        }
                    });

                    /* Incident Notification Vibrate Switch */
                    final Switch deptNotify_Vibrate = (Switch) view2.findViewById(R.id.deptNotify_Vibrate);
                    deptNotify_Vibrate.setChecked(sharedPreferences.getBoolean(selectedDept + "_incidentVibrate", true));
                    deptNotify_Vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            sharedPreferences.edit().putBoolean(selectedDept + "_incidentVibrate", b).apply();
                        }
                    });

                    /* Tones Notification Master Switch */
                    Switch deptNotify_Tones_Master = (Switch) view2.findViewById(R.id.deptNotify_Tones_Enable);
                    deptNotify_Tones_Master.setChecked(sharedPreferences.getBoolean(selectedDept + "_tonesMaster", true));
                    deptNotify_Tones_Master.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            sharedPreferences.edit().putBoolean(selectedDept + "_tonesMaster", b).apply();
                        }
                    });

                    /* Manpower Notification Master Switch */
                    Switch deptNotify_Manpower_Master = (Switch) view2.findViewById(R.id.deptNotify_Manpower_Enable);
                    deptNotify_Manpower_Master.setChecked(sharedPreferences.getBoolean(selectedDept + "_manpowerMaster", true));
                    deptNotify_Manpower_Master.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            sharedPreferences.edit().putBoolean(selectedDept + "_manpowerMaster", b).apply();
                        }
                    });

                    /* Incident Notifications Master Switch */
                    Switch deptNotify_Master = (Switch) view2.findViewById(R.id.deptNotify_Master);
                    deptNotify_Master.setChecked(sharedPreferences.getBoolean(selectedDept + "_incidentMaster", true));
                    deptNotify_Screen.setEnabled(sharedPreferences.getBoolean(selectedDept + "_incidentMaster", true));
                    deptNotify_Vibrate.setEnabled(sharedPreferences.getBoolean(selectedDept + "_incidentMaster", true));
                    sound.setEnabled(sharedPreferences.getBoolean(selectedDept + "_incidentMaster", true));
                    deptNotify_Master.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            sharedPreferences.edit().putBoolean(selectedDept + "_incidentMaster", b).apply();
                            if (b) {
                                deptNotify_Screen.setEnabled(true);
                                deptNotify_Vibrate.setEnabled(true);
                                sound.setEnabled(true);
                            } else {
                                deptNotify_Screen.setEnabled(false);
                                deptNotify_Vibrate.setEnabled(false);
                                sound.setEnabled(false);
                            }
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return apparatusList.size();
        }




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                sharedPreferences.edit().putString(selectedDept + "_incidentSound", uri.toString()).apply();
            } else {
                sharedPreferences.edit().putString(selectedDept + "_incidentSound", "none").apply();
            }
        }
    }

    public static class DepartmentViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final TextView name, abbrv;
        Button notificationButton, leaveButton;

        public DepartmentViewHolder(View view) {
            super(view);

            rootView = view;
            name = (TextView) view.findViewById(R.id.listitemDepartment_name);
            abbrv = (TextView) view.findViewById(R.id.listitemDepartment_abbrv);

            notificationButton = (Button)view.findViewById(R.id.listitemDept_notificationButton);
            leaveButton = (Button)view.findViewById(R.id.listitemDept_leaveButton);
        }
    }
}
