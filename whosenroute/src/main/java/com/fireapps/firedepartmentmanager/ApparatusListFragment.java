package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by austinhodak on 6/24/16.
 */

public class ApparatusListFragment extends Fragment {

    Member currentMember;
    private String departmentID;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private RelativeLayout emptyLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    ApparatusAdapter adapter;
    private List<DataSnapshot> apparatusList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apparatus_list, container, false);

        Application application = ((Application) getActivity().getApplicationContext());
        currentMember = application.getCurrentMember();
        departmentID = currentMember.getDepartment();

        Log.d("Apparatus", departmentID);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        emptyLayout = (RelativeLayout) view.findViewById(R.id.empty_layout);

        adapter = new ApparatusAdapter(apparatusList, getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.apparatus_recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        loadApparatus();

        return view;
    }

    private void loadApparatus() {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mDatabase.child("apparatus").child(dataSnapshot.getKey()).orderByChild("apparatusAbrv").addValueEventListener(new ValueEventListener() {
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
        mDatabase.child("departments").child(departmentID).child("apparatus").addChildEventListener(childEventListener);
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
            Log.d("COLOR", (String)dataSnapshot.child("color").getValue());
            shapeDrawable.getPaint().setColor(Color.parseColor("#" + dataSnapshot.child("color").getValue()));
            holder.posImage.setBackground(shapeDrawable);

            holder.apparatusName.setText(apparatus.getApparatusName());
            holder.tvAbbrv.setText(apparatus.getApparatusAbrv());

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
            private final TextView tvAbbrv;
            private final RelativeLayout posLayout;
            private final ImageView posImage;
            protected TextView apparatusName;
            private View view;

            public ApparatusViewHolder(View itemView) {
                super(itemView);
                view = itemView;

                apparatusName = (TextView)itemView.findViewById(R.id.LI_app_name);
                tvAbbrv = (TextView) itemView.findViewById(R.id.LI_app_abbrv);
                posLayout = (RelativeLayout) itemView.findViewById(R.id.LI_app_layout);
                posImage = (ImageView) itemView.findViewById(R.id.LI_app_image);
            }
        }
    }
}
