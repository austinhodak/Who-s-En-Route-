package com.fireapps.firedepartmentmanager;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import eu.fiskur.chipcloud.ChipCloud;
import eu.fiskur.chipcloud.ChipListener;

import static com.fireapps.firedepartmentmanager.MapsActivity2.sharedPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
public class ApparatusListFragment extends Fragment {

    @Bind(R.id.apparatus_list)RecyclerView recyclerView;
    @Bind(R.id.apparatus_main_fab_add)FloatingActionButton fabAdd;
    @Bind(R.id.apparatus_main_fab_alert)FloatingActionButton fabAlert;
    private ArrayList<ApparatusObject> memberList;
    private ApparatusListAdapter adapter;
    @Bind(R.id.progressBar1)ProgressBar progressBar;
    private DatabaseReference departmentsRef;
    private DatabaseReference apparatusRef;
    private ArrayList<String> apparatusIdList = new ArrayList<>();

    //private List<ApparatusObject> apparatus;

//    RVAdapter adapter;

    public ApparatusListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apparatus_list, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        apparatusRef = database.getReference("apparatus");
        departmentsRef =
                database.getReference("departments");

        getActivity().setTitle("Apparatus");

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);

        memberList = new ArrayList<>();
        adapter = new ApparatusListAdapter(getActivity(), memberList);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wrapInScrollView = true;
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("Add Apparatus")
                        .customView(R.layout.dialog_apparatus_add, wrapInScrollView)
                        .positiveText("Add")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getView();
                                EditText apparatusName = (EditText) view.findViewById(R.id.apparatus_add_name);
                                EditText apparatusAbbr = (EditText) view.findViewById(R.id.apparatus_add_abbrv);
                                Switch apparatusInService = (Switch) view.findViewById(R.id.apparatus_add_inservice);

                                ApparatusObject newApparatus = new ApparatusObject(apparatusName.getText().toString(), apparatusAbbr.getText().toString(), apparatusInService.isChecked(), false);
                                String key = apparatusRef.push().getKey();
                                apparatusRef.child(key).setValue(newApparatus);

                                DatabaseReference departmentRef = database.getReference("departments/"+ sharedPreferences.getString("department", "") +"/apparatus");
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put(key, true);
                                departmentRef.updateChildren(childUpdates);

                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        fabAlert.setOnClickListener(new View.OnClickListener() {
            int selected;
            String color;
            @Override
            public void onClick(View v) {
                final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.dialog_apparatus_new_alert, true)
                        .title("New Alert")
                        .positiveText("Add")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getView();
                                ChipCloud chipCloud = (ChipCloud) view.findViewById(R.id.chip_cloud);
                                EditText title = (EditText) view.findViewById(R.id.apparatus_alertnew_title);
                                EditText desc = (EditText) view.findViewById(R.id.apparatus_alertnew_desc);
                                EditText per = (EditText) view.findViewById(R.id.apparatus_alertnew_per);

                                ApparatusObject object = memberList.get(selected);
                                object.setAlert(true);
                                object.setAlertTitle(title.getText().toString());
                                object.setAlertDesc(desc.getText().toString());
                                object.setAlertBy(per.getText().toString());
                                object.setAlertColor(color);

                                Map<String, Object> postValues = object.toMap();

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put(apparatusIdList.get(selected), postValues);
                                apparatusRef.updateChildren(childUpdates);

                                dialog.dismiss();
                            }
                        })
                        .show();

                View view = dialog.getView();
                ChipCloud chipCloud = (ChipCloud) view.findViewById(R.id.chip_cloud);

                new ChipCloud.ChipCloudBuilder()
                        .chipCloud(chipCloud)
                        .selectedColor(getResources().getColor(R.color.primary))
                        .selectedFontColor(Color.parseColor("#ffffff"))
                        .deselectedColor(Color.parseColor("#e1e1e1"))
                        .deselectedFontColor(Color.parseColor("#333333"))
                        .selectTransitionMS(500)
                        .deselectTransitionMS(250)
                        .chipListener(new ChipListener() {
                            @Override
                            public void chipSelected(int index) {
                                //...
                                selected = index;
                            }
                        })
                        .build();
                for (ApparatusObject apparatusObject : memberList) {
                    chipCloud.addChip(apparatusObject.getApparatusAbrv());
                }

                chipCloud.setSelectedChip(0);

                Spinner spinner = (Spinner) view.findViewById(R.id.apparatus_alertnew_color);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.alert_spinner_colors, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_red_500));
                                color = "#F44336";
                                break;
                            case 1:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_pink_500));
                                color = "#E91E63";
                                break;
                            case 2:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_purple_500));
                                color = "#9C27B0";
                                break;
                            case 3:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_deep_purple_500));
                                color = "#673AB7";
                                break;
                            case 4:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_indigo_500));
                                color = "#3F51B5";
                                break;
                            case 5:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_blue_500));
                                color = "#2196F3";
                                break;
                            case 6:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_light_blue_500));
                                color = "#03A9F4";
                                break;
                            case 7:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_cyan_500));
                                color = "#00BCD4";
                                break;
                            case 8:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_teal_500));
                                color = "#009688";
                                break;
                            case 9:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_green_500));
                                color = "4CAF50";
                                break;
                            case 10:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_light_green_500));
                                color = "#8BC34A";
                                break;
                            case 11:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_lime_500));
                                color = "#CDDC39";
                                break;
                            case 12:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_yellow_500));
                                color = "#FFEB3B";
                                break;
                            case 13:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_amber_500));
                                color = "#FFC107";
                                break;
                            case 14:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_orange_500));
                                color = "#FF9800";
                                break;
                            case 15:
                                dialog.getTitleView().setTextColor(getResources().getColor(R.color.md_deep_orange_500));
                                color = "#FF5722";
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        memberList.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference linkCommentsRef = departmentsRef.child(sharedPreferences.getString("department", "none")).child("apparatus");
        linkCommentsRef.keepSynced(true);
        apparatusRef.keepSynced(true);
        linkCommentsRef.addChildEventListener(new ChildEventListener() {
            // Retrieve members
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                progressBar.setVisibility(View.GONE);
                apparatusRef.child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //Retrieve member and do stuff
                        ApparatusObject post = snapshot.getValue(ApparatusObject.class);
                        int location = 0;

                        try {
                            for (ApparatusObject object : memberList) {
                                if (object.getApparatusName().equals(post.getApparatusName())) {
                                    location = memberList.indexOf(object);
                                    memberList.remove(object);
                                    apparatusIdList.remove(snapshot.getKey());
                                    adapter.notifyDataSetChanged();

                                    Log.d("List", "REMOVED");
                                } else {
                                    location = memberList.size();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        memberList.add(location, post);
                        apparatusIdList.add(location, snapshot.getKey());
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        Toast.makeText(getActivity(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("FDM", firebaseError.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Toast.makeText(getActivity(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("FDM", firebaseError.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });

        return view;
    }

    public class ApparatusListAdapter extends RecyclerView.Adapter<ApparatusListAdapter.CustomViewHolder> {
        private List<ApparatusObject> feedItemList;
        private Context mContext;

        View view;

        public ApparatusListAdapter(Context context, List<ApparatusObject> feedItemList) {
            this.feedItemList = feedItemList;
            this.mContext = context;
        }

        @Override
        public com.fireapps.firedepartmentmanager.ApparatusListFragment.ApparatusListAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_apparatus, null);

            com.fireapps.firedepartmentmanager.ApparatusListFragment.ApparatusListAdapter.CustomViewHolder viewHolder = new com.fireapps.firedepartmentmanager.ApparatusListFragment.ApparatusListAdapter.CustomViewHolder(view);
            return viewHolder;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(CustomViewHolder customViewHolder, final int i) {
            final ApparatusObject feedItem = feedItemList.get(i);

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            customViewHolder.apparatusAlert.setVisibility(View.GONE);
            customViewHolder.apparatusName.setText(feedItem.getApparatusName());

            try {
                Log.d("FDM-Appartus", Boolean.toString(feedItem.getIsAlert()));
                if (Boolean.toString(feedItem.getIsAlert()).equalsIgnoreCase("true")) {
                    customViewHolder.apparatusAlert.setVisibility(View.VISIBLE);
                    customViewHolder.apparatusAlertTitle.setText(feedItem.getAlertTitle());
                    customViewHolder.apparatusAlert.setBackgroundColor(Color.parseColor(feedItem.alertColor));
                    customViewHolder.apparatusAlert.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean wrapInScrollView = false;
                            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                    .customView(R.layout.dialog_alert_detail, wrapInScrollView)
                                    .positiveText("Ok")
                                    .neutralText("Clear Alert")
                                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            feedItem.setAlert(false);
                                            feedItem.setAlertBy(null);
                                            feedItem.setAlertColor(null);
                                            feedItem.setAlertDesc(null);
                                            feedItem.setAlertTitle(null);

                                            Map<String, Object> postValues = feedItem.toMap();

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put(apparatusIdList.get(i), postValues);
                                            apparatusRef.updateChildren(childUpdates);
                                        }
                                    })
                                    .show();
                            View view = dialog.getView();

                            RelativeLayout alertColor = (RelativeLayout) view.findViewById(R.id.dialog_alert_color);
                            TextView alertTitle = (TextView) view.findViewById(R.id.dialog_alert_title);
                            TextView alertIssuer = (TextView) view.findViewById(R.id.dialog_alert_per);
                            TextView alertDesc = (TextView) view.findViewById(R.id.dialog_alert_details);

                            alertColor.setBackgroundColor(Color.parseColor(feedItem.alertColor));
                            alertTitle.setText(feedItem.alertTitle);
                            alertIssuer.setText(feedItem.alertBy);
                            alertDesc.setText(feedItem.alertDesc);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            protected TextView apparatusName, apparatusAbbr, apparatusAlertTitle;
            RelativeLayout apparatusAlert;

            public CustomViewHolder(View view) {
                super(view);
                this.apparatusName = (TextView) view.findViewById(R.id.list_apparatus_title);
                this.apparatusAlert = (RelativeLayout)view.findViewById(R.id.apparatus_alert_layout);
                this.apparatusAlertTitle = (TextView) view.findViewById(R.id.list_apparatus_alert);
            }
        }
    }


}
