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

import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by austinhodak on 8/10/16.
 */

public class MemberFragment extends Fragment {

    List<DataSnapshot> membersList = new ArrayList<>();
    List<DataSnapshot> membersListSorted = new ArrayList<>();
    private String departmentID;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mDatabase;
    private RelativeLayout emptyLayout;
    private MemberAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    private FloatingActionButton addMemberFAB;
    private FloatingActionMenu mainFAB;

    public MemberFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_members_list, container, false);

        departmentID = RespondingSystem.getInstance(getActivity()).getCurrentDepartmentKey();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emptyLayout = (RelativeLayout) view.findViewById(R.id.empty_layout);

        adapter = new MemberAdapter(membersList, getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.apparatus_recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        addMemberFAB = (FloatingActionButton)view.findViewById(R.id.member_main_fab_add);
        mainFAB = (FloatingActionMenu) view.findViewById(R.id.member_main_fab);
        addMemberFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MemberAdd.class);
                startActivityForResult(intent, 1);
                getActivity().overridePendingTransition(R.anim.slide_up, R.anim.stay);
            }
        });

        return view;
    }

    private void loadMembers() {
        mDatabase.child("departments").child(departmentID).child("records").child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mDatabase.child("members").child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        membersList.add(0, dataSnapshot);
                        adapter.notifyDataSetChanged();

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
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        membersList.clear();
        adapter.notifyDataSetChanged();
        loadMembers();
    }

    public static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

        private final List<DataSnapshot> apparatusList;
        Context context;

        public MemberAdapter(List<DataSnapshot> apparatusList, Context context) {
            this.apparatusList = apparatusList;
            this.context = context;
        }

        @Override
        public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_member, parent, false);
            return new MemberViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MemberViewHolder holder, int position) {
            final DataSnapshot dataSnapshot = apparatusList.get(position);

            holder.tvName.setText(dataSnapshot.child("name/last").getValue().toString() + ", " + dataSnapshot.child("name/first").getValue().toString());

            FirebaseDatabase.getInstance().getReference("users").child(dataSnapshot.child("uid").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.tvPos.setText(dataSnapshot.child("position").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return apparatusList.size();
        }

        public static class MemberViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvName, tvPos;
            private View view;

            public MemberViewHolder(View itemView) {
                super(itemView);
                view = itemView;

                tvName = (TextView) itemView.findViewById(R.id.LI_member_name);
                tvPos = (TextView) itemView.findViewById(R.id.LI_member_position);
            }
        }
    }
}
