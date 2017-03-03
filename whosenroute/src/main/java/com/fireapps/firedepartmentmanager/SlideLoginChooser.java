package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by austinhodak on 8/7/16.
 */

public class SlideLoginChooser extends Fragment {

    private OnChooserButtonClickedListener mCallback;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    private String TAG = "LoginSlide";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide_chooser, container, false);

        Button mSignInButton = (Button) view.findViewById(R.id.slide_signin_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        //.title(R.string.title)
                        .customView(R.layout.dialog_login, true)
                        .backgroundColor(getResources().getColor(R.color.md_white_1000))
                        .title("Sign In")
                        .positiveText("Sign In")
                        .neutralText("Forgot Password")
                        .positiveColorRes(R.color.md_blue_500)
                        .neutralColorRes(R.color.md_blue_500)
                        .titleColorRes(R.color.md_blue_500)
                        .theme(Theme.LIGHT)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getView();
                                final MaterialEditText emailField = (MaterialEditText) view.findViewById(R.id.login_dialog_emailField);
                                final MaterialEditText passwordField = (MaterialEditText) view.findViewById(R.id.login_dialog_passwordField);

                                if (emailField.getText().toString().isEmpty()) {
                                    emailField.setError("Email cannot be empty.");
                                    return;
                                }
                                if (passwordField.getText().toString().isEmpty()) {
                                    passwordField.setError("Password cannot be empty.");
                                    return;
                                }
                                final MaterialDialog pg = new MaterialDialog.Builder(getActivity())
                                        .title("Signing In")
                                        .content("Please Wait.")
                                        .progress(true, 0)
                                        .backgroundColor(getResources().getColor(R.color.md_white_1000))
                                        .theme(Theme.LIGHT)
                                        .widgetColorRes(R.color.md_blue_500)
                                        .show();

                                auth.signInWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                                // If sign in fails, display a message to the user. If sign in succeeds
                                                // the auth state listener will be notified and logic to handle the
                                                // signed in user can be handled in the listener.
                                                if (!task.isSuccessful()) {
                                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                                    Toast.makeText(getActivity(), "Sign In Failed. Please try again.",
                                                            Toast.LENGTH_SHORT).show();
                                                    pg.dismiss();
                                                    return;
                                                }
                                                mCallback.onLoggedIn();
                                                dialog.dismiss();
                                                pg.dismiss();
                                            }
                                        });
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                View view = dialog.getView();
                                final MaterialEditText emailField = (MaterialEditText) view.findViewById(R.id.login_dialog_emailField);

                                if (!emailField.getText().toString().isEmpty()) {
                                    new MaterialDialog.Builder(getActivity())
                                            .title("Reset Password")
                                            .content("An email will be sent so you can reset your password.")
                                            .positiveText("SEND")
                                            .negativeText("CANCEL")
                                            .positiveColorRes(R.color.md_blue_500)
                                            .neutralColorRes(R.color.md_blue_500)
                                            .titleColorRes(R.color.md_blue_500)
                                            .backgroundColor(getResources().getColor(R.color.md_white_1000))
                                            .theme(Theme.LIGHT)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                                    auth.sendPasswordResetEmail(emailField.getText().toString())
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Log.d(TAG, "Email sent.");
                                                                        Toast.makeText(getActivity(), "Reset Email Sent.", Toast.LENGTH_SHORT).show();
                                                                        dialog.dismiss();
                                                                    }
                                                                }
                                                            });
                                                }
                                            })
                                            .show();
                                } else {
                                    emailField.setError("Please input email to reset.");
                                }
                            }
                        })
                        .autoDismiss(false)
                        .show();
            }
        });



        Button createButton = (Button) view.findViewById(R.id.slide_create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ask if pre-approved.
                new MaterialDialog.Builder(getActivity())
                        .title("Pre-Approved")
                        .content("Have you been pre-approved by an admin of your agency?")
                        .positiveText("YES")
                        .negativeText("NO")
                        .neutralText("I Don't Know")
                        .positiveColorRes(R.color.md_blue_500)
                        .neutralColorRes(R.color.md_blue_500)
                        .negativeColorRes(R.color.md_blue_grey_900)
                        .titleColorRes(R.color.md_blue_500)
                        .backgroundColor(getResources().getColor(R.color.md_white_1000))
                        .theme(Theme.LIGHT)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                showSignUpDialog(true);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                showSignUpDialog(false);
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new MaterialDialog.Builder(getActivity())
                                        .backgroundColor(getResources().getColor(R.color.md_white_1000))
                                        .title("Pre-Approve Check")
                                        .positiveText("Check")
                                        .neutralText("Help")
                                        .positiveColorRes(R.color.md_blue_500)
                                        .neutralColorRes(R.color.md_blue_500)
                                        .titleColorRes(R.color.md_blue_500)
                                        .theme(Theme.LIGHT)
                                        .widgetColorRes(R.color.md_blue_500)
                                        .contentColorRes(R.color.md_blue_grey_900)
                                        .content("Enter your email to see if you've been pre-approved.")
                                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                                        .input("Email", null, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(final MaterialDialog dialog, final CharSequence input) {
                                                if (!input.toString().isEmpty()) {
                                                    final MaterialDialog pg = new MaterialDialog.Builder(getActivity())
                                                            .title("Checking...")
                                                            .content("Please Wait.")
                                                            .progress(true, 0)
                                                            .backgroundColor(getResources().getColor(R.color.md_white_1000))
                                                            .theme(Theme.LIGHT)
                                                            .widgetColorRes(R.color.md_blue_500)
                                                            .show();

                                                    FirebaseDatabase.getInstance().getReference("/memberPreApprovals/").addChildEventListener(new ChildEventListener() {
                                                        @Override
                                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/memberPreApprovals/").child(dataSnapshot.getKey());
                                                            ref.addChildEventListener(new ChildEventListener() {
                                                                @Override
                                                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                                    String email = dataSnapshot.getValue().toString();
                                                                    if (email.equals(input.toString())) {
                                                                        pg.dismiss();
                                                                        dialog.dismiss();
                                                                        Snackbar.make(getView(), "You have been pre-approved!", 10000).setAction("Create Account", new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                showSignUpDialog(true);
                                                                            }
                                                                        }).show();
                                                                    }
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
                                                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {

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
                                                } else {
                                                    dialog.getInputEditText().setError("Cannot be empty.");
                                                }
                                            }
                                        })
                                        .autoDismiss(false)
                                        .show();
                            }
                        })
                        .show();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnChooserButtonClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public interface OnChooserButtonClickedListener {
        public void onLoginButtonSelected();
        public void onLoggedIn();
    }

    void showSignUpDialog(boolean preApproved) {
        final MaterialDialog signUpDialog = new MaterialDialog.Builder(getActivity())
                //.title(R.string.title)
                .customView(R.layout.dialog_signup, true)
                .backgroundColor(getResources().getColor(R.color.md_white_1000))
                .title("Create Account")
                .positiveText("Next")
                .neutralText("Help")
                .positiveColorRes(R.color.md_blue_500)
                .neutralColorRes(R.color.md_blue_500)
                .titleColorRes(R.color.md_blue_500)
                .theme(Theme.LIGHT)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                                /*SIGNUP PROCESS, BASIC ACCOUNT/EMAIL*/
                        View view = dialog.getView();
                        final MaterialEditText firstnameField = (MaterialEditText) view.findViewById(R.id.one_signup_dialog_firstname);
                        final MaterialEditText lastnameField = (MaterialEditText) view.findViewById(R.id.one_signup_dialog_lastname);
                        final MaterialEditText emailField = (MaterialEditText) view.findViewById(R.id.one_signup_dialog_email);
                        final MaterialEditText passwordField = (MaterialEditText) view.findViewById(R.id.one_signup_dialog_password);
                        MaterialEditText passwordREField = (MaterialEditText) view.findViewById(R.id.one_signup_dialog_passwordRE);

                        if (firstnameField.getText().toString().isEmpty()) {
                            firstnameField.setError("Name cannot be empty.");
                            return;
                        }
                        if (lastnameField.getText().toString().isEmpty()) {
                            lastnameField.setError("Name cannot be empty.");
                            return;
                        }
                        if (emailField.getText().toString().isEmpty()) {
                            emailField.setError("Email cannot be empty.");
                            return;
                        }
                        if (passwordField.getText().toString().isEmpty()) {
                            passwordField.setError("Password cannot be empty.");
                            return;
                        }
                        if (passwordREField.getText().toString().isEmpty()) {
                            passwordREField.setError("Password cannot be empty.");
                            return;
                        }
                        if (!passwordField.getText().toString().equals(passwordREField.getText().toString())) {
                            passwordREField.setError("Passwords don't match.");
                            return;
                        }

                        final MaterialDialog pg1 = new MaterialDialog.Builder(getActivity())
                                .title("Checking...")
                                .content("Please Wait.")
                                .progress(true, 0)
                                .backgroundColor(getResources().getColor(R.color.md_white_1000))
                                .theme(Theme.LIGHT)
                                .widgetColorRes(R.color.md_blue_500)
                                .show();

                        FirebaseDatabase.getInstance().getReference("/memberPreApprovals/").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/memberPreApprovals/").child(dataSnapshot.getKey());
                                ref.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot2, String s) {
                                        String emailD = dataSnapshot2.getValue().toString();
                                        if (emailD.equals(emailField.getText().toString())) {
                                            pg1.dismiss();
                                            signUpUser(dataSnapshot2, emailField.getText().toString(), passwordField.getText().toString(), firstnameField.getText().toString(), lastnameField.getText().toString(), dialog, dataSnapshot.getKey());
                                        }
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
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

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

                        /*auth.createUserWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getActivity(), task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            pg.dismiss();
                                            return;
                                        }

                                        String newUserUID = task.getResult().getUser().getUid();

                                        Map<String, Object> childUpdates = new HashMap<>();
                                        childUpdates.put("/users/" + newUserUID + "/email", emailField.getText().toString());
                                        childUpdates.put("/users/" + newUserUID + "/isResponding", false);
                                        childUpdates.put("/users/" + newUserUID + "/location/lat", "");
                                        childUpdates.put("/users/" + newUserUID + "/location/lon", "");
                                        childUpdates.put("/users/" + newUserUID + "/memberUID", newUserUID);

                                        childUpdates.put("/users/" + newUserUID + "/name", firstnameField.getText().toString() + " " + lastnameField.getText().toString());

                                        //Responding
                                        childUpdates.put("/users/" + newUserUID + "/isResponding", false);
                                        childUpdates.put("/users/" + newUserUID + "/respondingAgency", "");
                                        childUpdates.put("/users/" + newUserUID + "/respondingTime", "");
                                        childUpdates.put("/users/" + newUserUID + "/respondingTo", "");

                                        *//*Member Details*//*
                                        childUpdates.put("/members/" + newUserUID + "/name/first", firstnameField.getText().toString());
                                        childUpdates.put("/members/" + newUserUID + "/name/last", lastnameField.getText().toString());
                                        childUpdates.put("/members/" + newUserUID + "/uid/", newUserUID);
                                        childUpdates.put("/members/" + newUserUID + "/email/", emailField.getText().toString());

                                        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                pg.dismiss();
                                                if (task.isSuccessful()) {
                                                    dialog.dismiss();
                                                    //Join Department
                                                    return;
                                                }
                                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });*/
                    }
                })
                .autoDismiss(false).show();
    }

    public String checkIfPreApproved(final String email) {
        final String[] department = {null};
        FirebaseDatabase.getInstance().getReference("/memberPreApprovals/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/memberPreApprovals/").child(dataSnapshot.getKey());
                ref.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot2, String s) {
                        String emailD = dataSnapshot2.getValue().toString();
                        if (emailD.equals(email)) {
                            department[0] = dataSnapshot2.getKey();
                        }
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
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

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
        return department[0];
    }

    void signUpUser(final DataSnapshot dataSnapshot2, final String email, String password, final String firstName, final String lastName, final MaterialDialog dialog, final String department) {
        final MaterialDialog pg = new MaterialDialog.Builder(getActivity())
                .title("Creating Account")
                .content("Please Wait.")
                .progress(true, 0)
                .backgroundColor(getResources().getColor(R.color.md_white_1000))
                .theme(Theme.LIGHT)
                .widgetColorRes(R.color.md_blue_500)
                .show();

        final CharSequence[][] selectedRanks = {null};
        final Integer[][] selectedRanksInt = {null};
        final CharSequence[] mainRank = {null};
        final int[] mainRankInt = {-1};

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            pg.dismiss();
                            return;
                        }
                        dialog.dismiss();
                        pg.dismiss();

                        final String newUserUID = task.getResult().getUser().getUid();

                        final Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/users/" + newUserUID + "/email", email);
                        childUpdates.put("/users/" + newUserUID + "/isResponding", false);
                        childUpdates.put("/users/" + newUserUID + "/location/lat", "");
                        childUpdates.put("/users/" + newUserUID + "/location/lon", "");
                        childUpdates.put("/users/" + newUserUID + "/memberUID", newUserUID);

                        childUpdates.put("/users/" + newUserUID + "/name", firstName + " " + lastName);

                        //Responding
                        childUpdates.put("/users/" + newUserUID + "/isResponding", false);
                        childUpdates.put("/users/" + newUserUID + "/respondingAgency", "");
                        childUpdates.put("/users/" + newUserUID + "/respondingTime", "");
                        childUpdates.put("/users/" + newUserUID + "/respondingTo", "");

                        //*Member Details*//
                        childUpdates.put("/members/" + newUserUID + "/name/first", firstName);
                        childUpdates.put("/members/" + newUserUID + "/name/last", lastName);
                        childUpdates.put("/members/" + newUserUID + "/uid/", newUserUID);
                        childUpdates.put("/members/" + newUserUID + "/email/", email);

                        childUpdates.put("/users/" + newUserUID + "/departments/" + department, true);
                        childUpdates.put("/users/" + newUserUID + "/department/", department);


                        childUpdates.put("/departments/" + department + "/members/" + newUserUID, true);
                        childUpdates.put("/departments/" + department + "/records/members/" + newUserUID, true);

                        final MaterialDialog rankDialog = new MaterialDialog.Builder(getActivity())
                                //.title(R.string.title)
                                .customView(R.layout.dialog_rank, true)
                                .backgroundColor(getResources().getColor(R.color.md_white_1000))
                                .title("Position/Rank")
                                .positiveText("Next")
                                .neutralText("Help")
                                .positiveColorRes(R.color.md_blue_500)
                                .neutralColorRes(R.color.md_blue_500)
                                .titleColorRes(R.color.md_blue_500)
                                .theme(Theme.LIGHT)
                                .autoDismiss(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                        View positionDialog = dialog.getCustomView();
                                        final Button rankButton = (Button) positionDialog.findViewById(R.id.signup_position_ranks);
                                        final Button mainRankButton = (Button) positionDialog.findViewById(R.id.signup_position_mainrank);
                                        final MaterialEditText positionField = (MaterialEditText) positionDialog.findViewById(R.id.signup_position_name);
                                        final MaterialEditText positionAbbrvField = (MaterialEditText) positionDialog.findViewById(R.id.signup_position_abbrv);

                                        if (positionField.getText().toString().isEmpty()) {
                                            positionField.setError("Cannot be empty.");
                                            return;
                                        }
                                        if (positionAbbrvField.getText().toString().isEmpty()) {
                                            positionAbbrvField.setError("Cannot be empty.");
                                            return;
                                        }
                                        if (selectedRanks[0].length == 0) {
                                            Toast.makeText(getActivity(), "Must Select Rank(s)", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (mainRankInt[0] == -1) {
                                            Toast.makeText(getActivity(), "Must Select Main Rank", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        childUpdates.put("/members/" + newUserUID + "/position/primaryName", positionField.getText().toString());
                                        childUpdates.put("/members/" + newUserUID + "/position/primaryAbbreviation", positionAbbrvField.getText().toString());

                                        if (Arrays.asList(selectedRanks[0]).contains("Chief") || Arrays.asList(selectedRanks[0]).contains("Officer")) {
                                            childUpdates.put("/members/" + newUserUID + "/position/officer", true);
                                        } else {
                                            childUpdates.put("/members/" + newUserUID + "/position/officer", false);
                                        }

                                        //Add Ranks
                                        for (int i=0; i < selectedRanks[0].length; i++) {
                                            childUpdates.put("/members/" + newUserUID + "/position/ranks/" + Arrays.asList(selectedRanks[0]).get(i).toString().toLowerCase(), true);
                                        }
                                        childUpdates.put("/members/" + newUserUID + "/position/rankMain", mainRank[0]);
                                        childUpdates.put("/departments/" + department + "/records/members/" + newUserUID, true);

                                        childUpdates.put("/users/" + newUserUID + "/mainPosition", mainRank[0]);
                                        childUpdates.put("/users/" + newUserUID + "/position", positionField.getText().toString());
                                        childUpdates.put("/users/" + newUserUID + "/positionAbbrv", positionAbbrvField.getText().toString());
                                        childUpdates.put("/users/" + newUserUID + "/positionColor", "1976D2");

                                        if (Arrays.asList(selectedRanks[0]).contains("Chief") || Arrays.asList(selectedRanks[0]).contains("Officer")) {
                                            childUpdates.put("/users/" + newUserUID + "/positionOfficer", true);
                                        } else {
                                            childUpdates.put("/users/" + newUserUID + "/positionOfficer", false);
                                        }

                                        for (int i=0; i < selectedRanks[0].length; i++) {
                                            childUpdates.put("/users/" + newUserUID + "/positions/" + Arrays.asList(selectedRanks[0]).get(i).toString().toLowerCase(), true);
                                        }

                                        pg.show();

                                        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                pg.dismiss();
                                                if (task.isSuccessful()) {
                                                    dialog.dismiss();
                                                    Toast.makeText(getActivity(), "User Created!", Toast.LENGTH_SHORT).show();
                                                    pg.setTitle("Loading...");
                                                    pg.show();
                                                    dataSnapshot2.getRef().removeValue();
                                                    FirebaseDatabase.getInstance().getReference("departments/").child(department).child("preapprovals").child(dataSnapshot2.getKey()).removeValue();
                                                    RespondingSystem.getInstance(getActivity()).getMemberInfo(newUserUID);
                                                    RespondingSystem.getInstance(getActivity()).setDepartmentListener(new RespondingSystem.DepartmentListener() {
                                                        @Override
                                                        public void onDepartmentAdded(DataSnapshot snapshot, Uri icon) {

                                                        }

                                                        @Override
                                                        public void onDepartmentUpdated(DataSnapshot snapshot) {

                                                        }

                                                        @Override
                                                        public void onLoadingFinished() {
                                                            pg.dismiss();
                                                            Intent intent = new Intent(getActivity(), NavDrawerActivity.class);
                                                            startActivity(intent);
                                                        }

                                                        @Override
                                                        public void onDepartmentAddedFirstTime(DataSnapshot snapshot) {

                                                        }
                                                    });
                                                    return;
                                                }
                                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .show();
                        View positionDialog = rankDialog.getCustomView();
                        final Button rankButton = (Button) positionDialog.findViewById(R.id.signup_position_ranks);
                        final Button mainRankButton = (Button) positionDialog.findViewById(R.id.signup_position_mainrank);
                        final MaterialEditText positionField = (MaterialEditText) positionDialog.findViewById(R.id.signup_position_name);
                        final MaterialEditText positionAbbrvField = (MaterialEditText) positionDialog.findViewById(R.id.signup_position_abbrv);

                        rankButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                        .title("Select Rank(s)")
                                        .items(R.array.memberAddRanks)
                                        .backgroundColorRes(R.color.bottom_sheet_background)
                                        .itemsCallbackMultiChoice(selectedRanksInt[0], new MaterialDialog.ListCallbackMultiChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                                /**
                                                 * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                                 * returning false here won't allow the newly selected check box to actually be selected.
                                                 * See the limited multi choice dialog example in the sample project for details.
                                                 **/
                                                selectedRanksInt[0] = which;
                                                selectedRanks[0] = text;

                                                mainRank[0] = null;
                                                mainRankInt[0] = -1;
                                                rankButton.setText("Main Rank");

                                                if (which.length > 0) {
                                                    mainRankButton.setEnabled(true);
                                                } else {
                                                    mainRankButton.setEnabled(false);
                                                }

                                                rankButton.setText(selectedRanks[0].length + " Ranks");
                                                return true;
                                            }
                                        })
                                        .positiveText("Done")
                                        .show();
                            }
                        });
                        mainRankButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                        .title("Select Main Rank")
                                        .content("This is used to determine a few things on the website. (Usually highest rank.)")
                                        .items(selectedRanks[0])
                                        .backgroundColorRes(R.color.bottom_sheet_background)
                                        .itemsCallbackSingleChoice(mainRankInt[0], new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                /**
                                                 * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                                 * returning false here won't allow the newly selected check box to actually be selected.
                                                 * See the limited multi choice dialog example in the sample project for details.
                                                 **/
                                                mainRankButton.setText(text);
                                                mainRankInt[0] = which;
                                                mainRank[0] = text;
                                                return true;
                                            }
                                        })
                                        .positiveText("Done")
                                        .show();
                            }
                        });
                    }
                });
    }
}
