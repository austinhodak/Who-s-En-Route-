package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by austinhodak on 6/24/16.
 */

public class ApparatusListFragment extends Fragment implements View.OnClickListener {

    Member currentMember;
    ApparatusAdapter adapter;
    private String departmentID;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private RelativeLayout emptyLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List<DataSnapshot> apparatusList = new ArrayList<>();
    private FloatingActionButton addAppFab;
    private FloatingActionMenu mainMenuFab;

    public ApparatusListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apparatus_list, container, false);

        departmentID = RespondingSystem.getInstance(getActivity()).getCurrentDepartmentKey();

        //Log.d("Apparatus", departmentID);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        emptyLayout = (RelativeLayout) view.findViewById(R.id.empty_layout);

        adapter = new ApparatusAdapter(apparatusList, getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.apparatus_recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        addAppFab = (FloatingActionButton) view.findViewById(R.id.apparatus_main_fab_add);
        mainMenuFab = (FloatingActionMenu)view.findViewById(R.id.apparatus_main_fab);
        addAppFab.setOnClickListener(this);

        //loadApparatus();

        NavDrawerActivity activity = (NavDrawerActivity) getActivity();
        activity.setMainActivityListener(new NavDrawerActivity.MainActivityListener() {
            @Override
            public void onBottomSheetChanged(int bottomSheet) {
                Log.d("ApparatusList", "onBottomSheetChanged: " + bottomSheet);
                if (bottomSheet == 4) {
                    mainMenuFab.hideMenuButton(false);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        apparatusList.clear();
        adapter.notifyDataSetChanged();
        loadApparatus();
    }

    private void loadApparatus() {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mDatabase.child("apparatus").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //TODO APPLY SAME LOGIC TO RESPONDING.
                        int index = apparatusList.size();
                        try {
                            for (DataSnapshot object : apparatusList) {
                                if (object.getKey().equals(dataSnapshot.getKey())) {
                                    index = apparatusList.indexOf(object);
                                    apparatusList.remove(object);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            apparatusList.add(index, dataSnapshot);
                            adapter.notifyDataSetChanged();
                        }

                        Log.d("Apparatus", "" + dataSnapshot.getValue());

                        if (adapter.getItemCount() == 0) {
                            emptyLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
        mDatabase.child("departments").child(departmentID).child("apparatus").orderByChild("apparatusAbrv").addChildEventListener(childEventListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.apparatus_main_fab_add:
                MaterialDialog alertDialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.dialog_apparatus_new, false)
                        .positiveText("Save")
                        .negativeText("Cancel")
                        .backgroundColorRes(R.color.bottom_sheet_background)
                        .widgetColor(getResources().getColor(R.color.new_accent))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getCustomView();
                                EditText nameField = (EditText) view.findViewById(R.id.apparatus_new_name);
                                EditText abbrvField = (EditText) view.findViewById(R.id.apparatus_new_abbrv);
                                EditText typeField = (EditText) view.findViewById(R.id.apparatus_new_type);
                                Switch inService = (Switch)view.findViewById(R.id.apparatus_new_inservice);

                                if (nameField.getText().toString().length() != 0 && abbrvField.getText().toString().length() != 0 && typeField.getText().toString().length() != 0) {

                                    String key = mDatabase.child("apparatus").push().getKey();

                                    Map<String, Object> alertValues = new HashMap<>();
                                    alertValues.put("apparatusName", nameField.getText().toString());
                                    alertValues.put("apparatusAbrv", abbrvField.getText().toString());
                                    alertValues.put("type", typeField.getText().toString());
                                    alertValues.put("inService", inService.isChecked());
                                    alertValues.put("color", "F44336");
                                    alertValues.put("isAlert", false);

                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/apparatus/" + key, alertValues);
                                    childUpdates.put("/departments/" + departmentID + "/apparatus/" + key, true);

                                    mDatabase.updateChildren(childUpdates);

                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getActivity(), "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .autoDismiss(false)
                        .show();
                FloatingActionMenu floatingActionMenu = (FloatingActionMenu) addAppFab.getParent();
                floatingActionMenu.close(true);
                break;
        }
    }

    public static class ApparatusAdapter extends RecyclerView.Adapter<ApparatusAdapter.ApparatusViewHolder> {

        private final List<DataSnapshot> apparatusList;
        Context context;

        public ApparatusAdapter(List<DataSnapshot> apparatusList, Context context) {
            this.apparatusList = apparatusList;
            this.context = context;
        }

        @Override
        public ApparatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_apparatus, parent, false);
            return new ApparatusViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ApparatusViewHolder holder, int position) {
            final DataSnapshot dataSnapshot = apparatusList.get(position);
            Apparatus apparatus = dataSnapshot.getValue(Apparatus.class);

            ShapeDrawable shapeDrawable = new ShapeDrawable();
            shapeDrawable.setShape(new OvalShape());
            Log.d("COLOR", (String) dataSnapshot.child("color").getValue());
            shapeDrawable.getPaint().setColor(Color.parseColor("#" + dataSnapshot.child("color").getValue()));
            holder.posImage.setBackground(shapeDrawable);

            holder.apparatusName.setText(apparatus.getApparatusName());
            holder.tvAbbrv.setText(apparatus.getApparatusAbrv());

            if (dataSnapshot.child("inService").getValue() != null) {
                boolean inService = (boolean) dataSnapshot.child("inService").getValue();
                if (inService && !(boolean) dataSnapshot.child("isAlert").getValue()) {
                    holder.tvStatus.setText("In Service");
                    holder.tvStatus.setTextColor(context.getResources().getColor(R.color.md_green_500));
                } else if (inService && (boolean) dataSnapshot.child("isAlert").getValue()) {
                    holder.tvStatus.setText("In Service");
                    holder.tvStatus.setTextColor(context.getResources().getColor(R.color.md_orange_500));
                } else if (!inService) {
                    holder.tvStatus.setText("Out Of Service");
                    holder.tvStatus.setTextColor(context.getResources().getColor(R.color.md_red_500));
                }
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ApparatusDetailActivity.class);
                    intent.putExtra("apparatusID", dataSnapshot.getKey());
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return apparatusList.size();
        }

        public static class ApparatusViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvAbbrv, tvStatus;
            private final RelativeLayout posLayout;
            private final ImageView posImage;
            protected TextView apparatusName;
            private View view;

            public ApparatusViewHolder(View itemView) {
                super(itemView);
                view = itemView;

                apparatusName = (TextView) itemView.findViewById(R.id.LI_app_name);
                tvStatus = (TextView) itemView.findViewById(R.id.LI_app_status);
                tvAbbrv = (TextView) itemView.findViewById(R.id.LI_app_abbrv);
                posLayout = (RelativeLayout) itemView.findViewById(R.id.LI_app_layout);
                posImage = (ImageView) itemView.findViewById(R.id.LI_app_image);
            }
        }
    }
}
