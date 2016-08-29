package com.fireapps.firedepartmentmanager;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.List;

public class IncidentListFragment extends Fragment {

    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    RelativeLayout emptyLayout;
    RecyclerView recyclerView;
    List<DataSnapshot> listIncident;
    private IncidentAdapter incidentAdapter;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "IncidentList: ";
    private String departmentID;
    private DatabaseReference mDatabase;
    private static SharedPreferences sharedPreferences;
    private Parcelable mListState;
    private LinearLayoutManager layoutManager;
    private String LIST_STATE_KEY = "INCIDENT_LIST_STATE";
    private DataSnapshot selectedMessage;

    public IncidentListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incident_list, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        listIncident = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            firebaseUser = user;
        } else {
            // No user is signed in
        }

        getActivity().setTitle("Messages");

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        emptyLayout = (RelativeLayout) view.findViewById(R.id.empty_layout);

        incidentAdapter = new IncidentAdapter(listIncident, getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.incident_recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(incidentAdapter);

        departmentID = RespondingSystem.getInstance(getActivity().getApplicationContext()).getCurrentDepartmentKey();

        loadRespondingMembers(departmentID);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        loadUserInformation(user);

        Log.d("SIS", savedInstanceState + "");

        NavDrawerActivity activity = (NavDrawerActivity) getActivity();
        if (activity.getBottomSheet() == BottomSheetBehavior.STATE_COLLAPSED) {

        }

        return view;
    }

    private void loadUserInformation(FirebaseUser user) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Member member = dataSnapshot.getValue(Member.class);
                if (member != null) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.child("users/" + user.getUid()).addValueEventListener(postListener);
    }

    private void loadRespondingMembers(String department) {
        DatabaseReference departmentRef = firebaseDatabase.getReference("departments").child(department).child("messages");
        departmentRef.limitToLast(30).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                firebaseDatabase.getReference("messages").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int index = 0;
                        try {
                            for (DataSnapshot object : listIncident) {
                                if (object.getKey().equals(dataSnapshot.getKey())) {
                                    index = listIncident.indexOf(object);
                                    listIncident.remove(object);
                                    incidentAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            listIncident.add(index, dataSnapshot);
                            incidentAdapter.notifyDataSetChanged();
                        }

                        Log.d("Apparatus", "" + dataSnapshot.getValue());

                        if (incidentAdapter.getItemCount() == 0) {
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
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            loadRespondingMembers(departmentID);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Incidents", "onPause");
        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Incidents", "onResume");
        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Incidents", "onStart");
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Incidents", "onStop");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        //sharedPreferences.edit().remove("incidentListPos");
    }

    /*class MySection extends StatelessSection {

        String title;
        List<DataSnapshot> list;
        Context context;

        public MySection(String title, List<DataSnapshot> list, Context context) {
            // call constructor with layout resources for this Section header, footer and items
            super(R.layout.listitem_section, R.layout.listitem_incident);

            this.title = title;
            this.list = list;
            this.context = context;
        }

        @Override
        public int getContentItemsTotal() {
            return list.size(); // number of items of this section
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            // return a custom instance of ViewHolder for the items of this section
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;
            final DataSnapshot dataSnapshot = list.get(position);
            final Message member = dataSnapshot.getValue(Message.class);

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            Calendar calendar = GregorianCalendar.getInstance();

            try {
                date = dateFormat.parse(member.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
                try {
                    date = dateFormat2.parse(member.getDate());
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

            calendar.setTime(date);

            itemHolder.tvTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

            itemHolder.tvAbbrv.setText((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH));

            ShapeDrawable shapeDrawable = new ShapeDrawable();
            shapeDrawable.setShape(new OvalShape());
            shapeDrawable.getPaint().setColor(Color.parseColor("#009688"));
            itemHolder.posImage.setBackground(shapeDrawable);

            // bind your view here
            itemHolder.tvName.setText(member.getType());
            itemHolder.tvDesc.setText(member.getMessage());
            itemHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferences.edit().putInt("incidentListPos", recyclerView.computeVerticalScrollOffset()).commit();
                    Intent intent = new Intent(context, IncidentDetailActivity.class);
                    intent.putExtra("incidentID", dataSnapshot.getKey());
                    intent.putExtra("departmentID", departmentID);
                    selectedMessage = dataSnapshot;
                    startActivityForResult(intent, 99);
                }
            });


        }



        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            // bind your header view here
            headerHolder.tvTitle.setText(title);
        }

        public void addRow(DataSnapshot item) {
            this.list.add(0, item);
        }

        public List<DataSnapshot> getList() {
            return this.list;
        }


    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        public HeaderViewHolder(View view) {
            super(view);

            tvTitle = (TextView) view.findViewById(R.id.section);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView tvName, tvAbbrv, tvTime;
        private final RelativeLayout posLayout;
        private final ImageView posImage;
        private final TextView tvDesc;

        public ItemViewHolder(View view) {
            super(view);

            rootView = view;
            tvName = (TextView) view.findViewById(R.id.LI_responding_name);
            tvAbbrv = (TextView) view.findViewById(R.id.LI_responding_abbrv);
            tvDesc = (TextView) view.findViewById(R.id.LI_responding_desc);
            tvTime = (TextView) view.findViewById(R.id.LI_responding_time);
            posLayout = (RelativeLayout) view.findViewById(R.id.LI_pos_layout);
            posImage = (ImageView) view.findViewById(R.id.LI_pos_image);

            //rootView.setSelected(true);
            //rootView.requestFocus();
            //view.setFocusableInTouchMode(true);
            //view.setFocusable(true);
            //view.requestFocus();
            //view.setSelected(true);
        }
    }*/
////////
    public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.ApparatusViewHolder> {

        private final List<DataSnapshot> apparatusList;
        Context context;

        public IncidentAdapter(List<DataSnapshot> apparatusList, Context context) {
            this.apparatusList = apparatusList;
            this.context = context;
        }

        @Override
        public IncidentAdapter.ApparatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_incident, parent, false);
            return new IncidentAdapter.ApparatusViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(IncidentAdapter.ApparatusViewHolder holder, int position) {
            final DataSnapshot dataSnapshot = apparatusList.get(position);
            final Message member = dataSnapshot.getValue(Message.class);

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            Calendar calendar = GregorianCalendar.getInstance();

            try {
                date = dateFormat.parse(member.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
                try {
                    date = dateFormat2.parse(member.getDate());
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }

            calendar.setTime(date);

            holder.tvTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

            holder.tvAbbrv.setText((calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH));

            ShapeDrawable shapeDrawable = new ShapeDrawable();
            shapeDrawable.setShape(new OvalShape());
            shapeDrawable.getPaint().setColor(Color.parseColor("#009688"));
            holder.posImage.setBackground(shapeDrawable);

            // bind your view here
            holder.tvName.setText(member.getType());
            holder.tvDesc.setText(member.getMessage());
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferences.edit().putInt("incidentListPos", recyclerView.computeVerticalScrollOffset()).apply();
                    Intent intent = new Intent(context, IncidentDetailActivity.class);
                    intent.putExtra("incidentID", dataSnapshot.getKey());
                    intent.putExtra("departmentID", departmentID);
                    selectedMessage = dataSnapshot;
                    startActivityForResult(intent, 99);
                }
            });

            if (member.isActiveIncident()) {
                //Active Message, Highlight for easy finding.
                shapeDrawable.getPaint().setColor(Color.parseColor("#FF9800"));
                holder.posImage.setBackground(shapeDrawable);
            }
        }

        @Override
        public int getItemCount() {
            return apparatusList.size();
        }

        public class ApparatusViewHolder extends RecyclerView.ViewHolder {
            private final View rootView;
            private final TextView tvName, tvAbbrv, tvTime;
            private final RelativeLayout posLayout;
            private final ImageView posImage;
            private final TextView tvDesc;

            public ApparatusViewHolder(View view) {
                super(view);

                rootView = view;
                tvName = (TextView) view.findViewById(R.id.LI_responding_name);
                tvAbbrv = (TextView) view.findViewById(R.id.LI_responding_abbrv);
                tvDesc = (TextView) view.findViewById(R.id.LI_responding_desc);
                tvTime = (TextView) view.findViewById(R.id.LI_responding_time);
                posLayout = (RelativeLayout) view.findViewById(R.id.LI_pos_layout);
                posImage = (ImageView) view.findViewById(R.id.LI_pos_image);

                //rootView.setSelected(true);
                //rootView.requestFocus();
                //view.setFocusableInTouchMode(true);
                //view.setFocusable(true);
                //view.requestFocus();
                //view.setSelected(true);
            }
        }
    }
}
