package com.fireapps.firedepartmentmanager;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class MemberListFragment extends Fragment {

    private MemberListAdapter adapter;
    private List<MemberObject> list;
    @Bind(R.id.list)RecyclerView recyclerView;
    @Bind(R.id.member_list_fab)FloatingActionButton fab;

    List<MemberObject> memberList;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference departmentsRef;
    private DatabaseReference ref;
    private DatabaseReference usersRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    String authToken;
    private FirebaseDatabase database;

    SharedPreferences sharedPreferences;

    public MemberListFragment() {
        // Required empty public constructor
    }

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_member_list, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);

        sharedPreferences = getActivity().getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        memberList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        authToken = user.getToken(true).toString();

        //Toast.makeText(getActivity(), authToken, Toast.LENGTH_SHORT).show();

        mLayoutManager = new LinearLayoutManager(getContext());
        adapter = new MemberListAdapter(getActivity(), memberList);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);


        database = FirebaseDatabase.getInstance();

        ref = database.getReference("users");

        usersRef =
                database.getReference("users");

        //Links
        departmentsRef =
                database.getReference("departments");
        //initResponding();

        getActivity().setTitle("Member List");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wrapInScrollView = true;
                MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .title("Add Member")
                        .customView(R.layout.dialog_member_add, wrapInScrollView)
                        .positiveText("Add")
                        .negativeText("Cancel")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getCustomView();

                                final EditText nameField = (EditText) view.findViewById(R.id.member_dialog_name);
                                final EditText emailField = (EditText) view.findViewById(R.id.member_dialog_email);
                                EditText passwordField = (EditText) view.findViewById(R.id.member_dialog_password);
                                final EditText positionField = (EditText) view.findViewById(R.id.member_dialog_position);
                                final CheckBox isOfficer = (CheckBox) view.findViewById(R.id.member_dialog_isOfficer);
                                final CheckBox canReset = (CheckBox) view.findViewById(R.id.member_dialog_canReset);

                                if (nameField.getText().toString() != "" && emailField.getText().toString() != "" && passwordField.getText().toString() != "" && positionField.getText().toString() != "") {
                                    //Field good. Start sign up process;
                                    mAuth.createUserWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    task.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                        @Override
                                                        public void onSuccess(AuthResult authResult) {
                                                            final FirebaseUser user = authResult.getUser();

                                                            if (!user.getToken(false).toString().equals(authToken)) {
                                                                mAuth.signInWithEmailAndPassword(sharedPreferences.getString("email", ""), sharedPreferences.getString("password", ""))
                                                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                                //Log.d(TAG, "signInWithCustomToken:onComplete:" + task.isSuccessful());
                                                                                if (addNewUserInfo(nameField.getText().toString(), emailField.getText().toString(), positionField.getText().toString(), user, canReset.isChecked(), isOfficer.isChecked())) {
                                                                                    //ALL GOOD!
                                                                                    dialog.dismiss();
                                                                                    updateList();
                                                                                    Toast.makeText(getActivity(), "User Added.", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                                // If sign in fails, display a message to the user. If sign in succeeds
                                                                                // the auth state listener will be notified and logic to handle the
                                                                                // signed in user can be handled in the listener.
                                                                                if (!task.isSuccessful()) {
                                                                                    Log.w("FDM", "signInWithCustomToken", task.getException());
                                                                                    Toast.makeText(getActivity(), "Authentication failed.",
                                                                                            Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                            } else {
                                                                //Original User Signed in!
                                                            }

                                                        }
                                                    });
                                                    if (!task.isSuccessful()) {
                                                        Log.d("FDM1", "", task.getException());
                                                        Toast.makeText(getActivity(), "Authentication failed.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(getActivity(), "Check Fields.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
            }
        });

        updateList();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                } else {
                    // User is signed out
                   // Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        return view;
    }

    private boolean addNewUserInfo(String name, String email, String position, FirebaseUser newUser, boolean canReset , boolean isOfficer) {

        MemberObject member = new MemberObject();
        member.setName(name);
        member.setEmail(email);
        member.setPosition(position);
        member.setCanReset(canReset);
        member.setisOfficer(isOfficer);

        usersRef.child(newUser.getUid()).setValue(member);
        departmentsRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("members/" + newUser.getUid()).setValue(true);

        return true;
    }

    public void updateList() {

        memberList.clear();
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

                //Toast.makeText(getActivity(), "Total:" + snapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                usersRef.child(snapshot.getKey()).orderByChild(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //Retrieve member and do stuff
                        MemberObject post = snapshot.getValue(MemberObject.class);

                        try {
                            for (MemberObject object : memberList) {
                                if (object.getName().equals(post.getName())) {
                                    memberList.remove(object);
                                    adapter.notifyDataSetChanged();

                                    Log.d("List", "REMOVED");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        memberList.add(post);
                        adapter.notifyDataSetChanged();

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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
