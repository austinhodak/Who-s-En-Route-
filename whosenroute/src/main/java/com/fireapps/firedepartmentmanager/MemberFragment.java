package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    @BindView(R.id.member_main_fab_preapprove)
    FloatingActionButton preApproveFAB;

    public MemberFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_members_list, container, false);
        ButterKnife.bind(this, view);

        departmentID = RespondingSystem.getInstance(getActivity()).getCurrentDepartmentKey();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emptyLayout = (RelativeLayout) view.findViewById(R.id.empty_layout);

        adapter = new MemberAdapter(membersList, getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.apparatus_recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        membersList.clear();
        adapter.notifyDataSetChanged();
        loadMembers();

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

        if (RespondingSystem.getInstance(getActivity()).getPermissions() != null)
        addMemberFAB.setEnabled(RespondingSystem.getInstance(getActivity()).getPermissions().isMemberAdd());

        preApproveFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .backgroundColor(getResources().getColor(R.color.md_white_1000))
                        .title("Pre-Approve Member")
                        .positiveText("Approve")
                        .neutralText("Help")
                        .positiveColorRes(R.color.md_blue_500)
                        .neutralColorRes(R.color.md_blue_500)
                        .titleColorRes(R.color.md_blue_500)
                        .theme(Theme.LIGHT)
                        .widgetColorRes(R.color.md_blue_500)
                        .contentColorRes(R.color.md_blue_grey_900)
                        .content("This lets a member sign themselves up. Just enter their email and you're done! At this time you cannot revoke pre-approvals.")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input("Email", null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(final MaterialDialog dialog, CharSequence input) {

                                if (!input.toString().isEmpty()) {
                                    String key = FirebaseDatabase.getInstance().getReference("memberPreApprovals").child(departmentID).child("/").push().getKey();
                                    // Do something
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put("/memberPreApprovals/" + departmentID + "/" + key, input.toString());
                                    childUpdates.put("/departments/" + departmentID + "/preapprovals/" + key, input.toString());

                                    mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dialog.dismiss();
                                                return;
                                            }
                                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                                } else {
                                    dialog.getInputEditText().setError("Cannot be empty.");
                                }
                            }
                        })
                        .autoDismiss(false)
                        .show();
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
                        membersList.add(dataSnapshot);
                        adapter.notifyDataSetChanged();

                        if (adapter.getItemCount() == 0) {
                            emptyLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        sortList();
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            membersList.clear();
            adapter.notifyDataSetChanged();
            loadMembers();
        }
    }

    public void sortList() {
        Collections.sort(membersList, new Comparator<DataSnapshot>() {
            @Override
            public int compare(DataSnapshot s1, DataSnapshot s2) {
                return s1.child("name/last").getValue().toString().compareToIgnoreCase(s2.child("name/last").getValue().toString());
            }
        });
        adapter.notifyDataSetChanged();
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

            try {
                holder.tvPos.setText(dataSnapshot.child("position/primaryName").getValue().toString());
            } catch (Exception e) {
                FirebaseDatabase.getInstance().getReference("users").child(dataSnapshot.child("uid").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot2) {

                        try {
                            holder.tvPos.setText(dataSnapshot2.child("position").getValue().toString());
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            FirebaseDatabase.getInstance().getReference("users").child(dataSnapshot.child("uid").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot2) {
                    if (dataSnapshot2.getValue() != null) {
                        holder.phoneImage.setImageDrawable(context.getDrawable(R.drawable.phone96));
                    }
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
            private ImageView phoneImage;

            public MemberViewHolder(View itemView) {
                super(itemView);
                view = itemView;

                tvName = (TextView) itemView.findViewById(R.id.LI_member_name);
                tvPos = (TextView) itemView.findViewById(R.id.LI_member_position);
                phoneImage = (ImageView) itemView.findViewById(R.id.LI_member_phone);
            }
        }
    }
}
