package com.fireapps.firedepartmentmanager;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class RespondingFragment extends Fragment {

    private RespondingListAdapter adapter;
    private List<MemberObject> list;
    private RecyclerView recyclerView;

    @Bind(R.id.repsonding_total)
    TextView totalResponding;
    private DatabaseReference userFirebase;
    private DatabaseReference firebase;
    private DatabaseReference usersRef;
    private DatabaseReference departmentsRef;
    private int respondingToStation;
    private int respondingToScene;

    public RespondingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_respondingnew, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_main);

        list = new ArrayList<>();

        adapter = new RespondingListAdapter(getActivity(), list);

        firebase = database.getReference("users");

        //Users
        usersRef =
                database.getReference("users");

        //Departments
        departmentsRef =
                database.getReference("departments");

        userFirebase = firebase.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        initResponding();

        getActivity().setTitle("Who's En Route?");

        FloatingActionButton officerReset = (FloatingActionButton) view.findViewById(R.id.respOff_reset);

        final FloatingActionMenu officerFAB = (FloatingActionMenu) view.findViewById(R.id.respOff_fab);

        officerReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Reset Call?")
                        .content("This will reset the call? Are you sure?")
                        .positiveText("RESET")
                        .negativeText("CANCEL")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                officerFAB.close(true);

                                Query query = firebase.orderByChild("name");

                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                            MemberObject post = postSnapshot.getValue(MemberObject.class);
                                            Map<String, Object> nickname = new HashMap<String, Object>();
                                            nickname.put(postSnapshot.getKey() + "/respondingTo", null);
                                            nickname.put(postSnapshot.getKey() + "/isResponding", false);
                                            nickname.put(postSnapshot.getKey() + "/respondingFromLoc", null);
                                            nickname.put(postSnapshot.getKey() + "/respondingTime", null);
                                            firebase.updateChildren(nickname);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError firebaseError) {
                                        System.out.println("The read failed: " + firebaseError.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
            }
        });

        return view;
    }

    private void initResponding() {


        /*Query query = ref.orderByChild("name");

        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    MemberObject post = postSnapshot.getValue(MemberObject.class);
                    if (post.isResponding) {
                        list.add(post);
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });*/
    }

    public void updateList() {

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        Query query = firebase.orderByChild("name");

        list.clear();
        adapter.notifyDataSetChanged();

        DatabaseReference linkCommentsRef = departmentsRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("members");

        /*linkCommentsRef.on("child_added", function(snap) {
            commentsRef.child(snap.key()).once("value", function() {
                // Render the comment on the link page.
                ));
            });*/

        linkCommentsRef.addChildEventListener(new ChildEventListener() {
            // Retrieve members
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                usersRef.child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //Retrieve member and do stuff
                        MemberObject post = snapshot.getValue(MemberObject.class);

                        try {
                            for (MemberObject object : list) {
                                if (object.getName().equals(post.getName())) {
                                    list.remove(object);
                                    adapter.notifyDataSetChanged();
                                    Log.d("List", "REMOVED");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (post.getisResponding()) {
                            list.add(post);
                            adapter.notifyDataSetChanged();

                            if (post.getRespondingTo().equals("Station")) {
                                respondingToStation++;
                            } else if (post.getRespondingTo().equals("Scene")) {
                                respondingToScene++;
                            }
                        } else {

                        }

                        totalResponding.setText(String.valueOf(respondingToStation+respondingToScene) + " Responding");
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
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
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

}
