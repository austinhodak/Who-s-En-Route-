package com.fireapps.firedepartmentmanager;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by austinhodak on 8/11/16.
 */

public class MemberAdd extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.memberAdd_FirstName)
    MaterialEditText mFirstNameField;

    @BindView(R.id.memberAdd_MiddleName)
    MaterialEditText mMiddleNameField;

    @BindView(R.id.memberAdd_LastName)
    MaterialEditText mLastNameField;

    @BindView(R.id.member_add_app_email)
    MaterialEditText mAppEmailField;

    @BindView(R.id.member_add_app_password)
    MaterialEditText mAppPassField;

    @BindView(R.id.member_add_basic_email)
    MaterialEditText mBasicEmail;

    @BindView(R.id.member_add_position_ranks)
    Button mPositionRanksButton;

    @BindView(R.id.member_add_position_mainrank)
    Button mPositionMainRankButton;

    @BindView(R.id.member_add_position_primary)
    MaterialEditText mPositionPrimaryField;

    @BindView(R.id.member_add_position_abbrv)
    MaterialEditText mPositionAbbrv;

    @BindView(R.id.member_deptMain_button)
    Button mDepartmentMainButton;

    private int selectedDepartment = -1;
    private List<String> departmentNames = new ArrayList<>();
    private List<String> departmentKeys = new ArrayList<>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @BindView(R.id.member_add_done)
    FloatingActionButton doneFAB;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private String token;

    private CharSequence[] selectedRanks = null;
    private Integer[] selectedRanksInt = null;
    private CharSequence mainRank = null;
    private int mainRankInt = -1;

    @OnClick(R.id.member_dept_overflow)
    public void overflowClick(ImageView imageView) {
        PopupMenu popup = new PopupMenu(this, imageView);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.member_add_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_member_deptOverride:
                        new MaterialDialog.Builder(MemberAdd.this)
                                .title("Department Override")
                                .content("Only use this if you are told to do so by support.")
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .backgroundColorRes(R.color.bottom_sheet_background)
                                .input("Department ID", "", new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        // Do something
                                    }
                                }).show();
                        break;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.member_dept_button)
    public void departmentClicked(final Button button) {
        new MaterialDialog.Builder(MemberAdd.this)
                .title("Select Department")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .backgroundColorRes(R.color.bottom_sheet_background)
                .items(departmentNames)
                .itemsCallbackSingleChoice(selectedDepartment, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        selectedDepartment = which;
                        button.setText(text);
                        return true;
                    }
                }).show();
    }

    @BindView(R.id.member_add_app_enable)
    CheckBox appEnableCheckbox;

    @BindView(R.id.member_add_app_layout)
    LinearLayout appInfoLayout;

    FirebaseApp secondApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_add);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle("Add Member");

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RespondingSystem.getInstance(this).getMembersDepartments(null).setDepartmentListener(new RespondingSystem.DepartmentListener() {
            @Override
            public void onDepartmentAdded(DataSnapshot snapshot, Uri icon) {
                departmentNames.add(snapshot.child("name").getValue().toString());
                departmentKeys.add(snapshot.getKey());
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


        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey("AIzaSyBHhdoez_CTPhb9cEoh_MA62h4gIPmzZ3s")
                    .setApplicationId("1:779586893424:android:3fdc24c3147d9acf")
                    .setDatabaseUrl("https://fire-department-manager.firebaseio.com")
                    .build();

            secondApp = FirebaseApp.initializeApp(getApplicationContext(), options, "Secondary");
        } catch (Exception e) {
            e.printStackTrace();
            secondApp = FirebaseApp.getInstance("Secondary");
        }

        doneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkField()) {
                    //Add Checks to see if user already exists.
                    final MaterialDialog pg = new MaterialDialog.Builder(MemberAdd.this)
                            .title("Creating User")
                            .content("Please Wait")
                            .progress(true, 0)
                            .backgroundColorRes(R.color.bottom_sheet_background)
                            .show();

                    if (appEnableCheckbox.isChecked()) {
                        //First, Setup User Auth
                        FirebaseAuth.getInstance(secondApp).createUserWithEmailAndPassword(mAppEmailField.getText().toString(), mAppPassField.getText().toString())
                                .addOnCompleteListener(MemberAdd.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(MemberAdd.this, "Error, Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                        //pg.dismiss();

                                        String newUserUID = task.getResult().getUser().getUid();

                                        Map<String, Object> childUpdates = new HashMap<>();
                                        //Member Records *ALL SAVES*
                                        childUpdates.put("/members/" + newUserUID + "/name/first", mFirstNameField.getText().toString());
                                        childUpdates.put("/members/" + newUserUID + "/name/middle", mMiddleNameField.getText().toString());
                                        childUpdates.put("/members/" + newUserUID + "/name/last", mLastNameField.getText().toString());

                                        childUpdates.put("/members/" + newUserUID + "/uid/", newUserUID);
                                        childUpdates.put("/members/" + newUserUID + "/email/", mBasicEmail.getText().toString());

                                        childUpdates.put("/members/" + newUserUID + "/position/primaryName", mPositionPrimaryField.getText().toString());
                                        childUpdates.put("/members/" + newUserUID + "/position/primaryAbbreviation", mPositionAbbrv.getText().toString());

                                        if (Arrays.asList(selectedRanks).contains("Chief") || Arrays.asList(selectedRanks).contains("Officer")) {
                                            childUpdates.put("/members/" + newUserUID + "/position/officer", true);
                                        } else {
                                            childUpdates.put("/members/" + newUserUID + "/position/officer", false);
                                        }

                                        //Add Ranks
                                        for (int i=0; i < selectedRanks.length; i++) {
                                            childUpdates.put("/members/" + newUserUID + "/position/ranks/" + Arrays.asList(selectedRanks).get(i).toString().toLowerCase(), true);
                                        }
                                        childUpdates.put("/members/" + newUserUID + "/position/rankMain", mainRank);
                                        childUpdates.put("/departments/" + departmentKeys.get(selectedDepartment) + "/records/members/" + newUserUID, true);


                                        ///////////////////////////////////
                                        //App Info
                                        childUpdates.put("/users/" + newUserUID + "/email/", mAppEmailField.getText().toString());
                                        childUpdates.put("/users/" + newUserUID + "/departments/" + departmentKeys.get(selectedDepartment), true);
                                        childUpdates.put("/users/" + newUserUID + "/department/", departmentKeys.get(selectedDepartment));

                                        childUpdates.put("/users/" + newUserUID + "/location/lat", "");
                                        childUpdates.put("/users/" + newUserUID + "/location/lon", "");

                                        childUpdates.put("/users/" + newUserUID + "/mainPosition", mainRank);
                                        childUpdates.put("/users/" + newUserUID + "/position", mPositionPrimaryField.getText().toString());
                                        childUpdates.put("/users/" + newUserUID + "/positionAbbrv", mPositionAbbrv.getText().toString());
                                        childUpdates.put("/users/" + newUserUID + "/positionColor", "1976D2");

                                        if (Arrays.asList(selectedRanks).contains("Chief") || Arrays.asList(selectedRanks).contains("Officer")) {
                                            childUpdates.put("/users/" + newUserUID + "/positionOfficer", true);
                                        } else {
                                            childUpdates.put("/users/" + newUserUID + "/positionOfficer", false);
                                        }

                                        for (int i=0; i < selectedRanks.length; i++) {
                                            childUpdates.put("/users/" + newUserUID + "/positions/" + Arrays.asList(selectedRanks).get(i).toString().toLowerCase(), true);
                                        }

                                        childUpdates.put("/users/" + newUserUID + "/name", mFirstNameField.getText().toString() + " " + mLastNameField.getText().toString());

                                        //Responding
                                        childUpdates.put("/users/" + newUserUID + "/isResponding", false);
                                        childUpdates.put("/users/" + newUserUID + "/respondingAgency", "");
                                        childUpdates.put("/users/" + newUserUID + "/respondingTime", "");
                                        childUpdates.put("/users/" + newUserUID + "/respondingTo", "");

                                        //Department Info
                                        childUpdates.put("/departments/" + departmentKeys.get(selectedDepartment) + "/members/" + newUserUID, true);
                                        childUpdates.put("/departments/" + departmentKeys.get(selectedDepartment) + "/records/members/" + newUserUID, true);

                                        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(MemberAdd.this, "Error, Please try again.", Toast.LENGTH_SHORT).show();
                                                    pg.dismiss();
                                                    return;
                                                }

                                                pg.dismiss();
                                                setResult(RESULT_OK);
                                                finish();
                                            }
                                        });
                                    }
                                });
                    } else {
                        String newUserUID = mDatabase.child("members").push().getKey();
                        Map<String, Object> childUpdates = new HashMap<>();
                        //childUpdates.put("/posts/" + key, postValues);
                        //Member Records *ALL SAVES*
                        childUpdates.put("/members/" + newUserUID + "/name/first", mFirstNameField.getText().toString());
                        childUpdates.put("/members/" + newUserUID + "/name/middle", mMiddleNameField.getText().toString());
                        childUpdates.put("/members/" + newUserUID + "/name/last", mLastNameField.getText().toString());

                        childUpdates.put("/members/" + newUserUID + "/uid/", newUserUID);
                        childUpdates.put("/members/" + newUserUID + "/email/", mBasicEmail.getText().toString());

                        childUpdates.put("/members/" + newUserUID + "/position/primaryName", mPositionPrimaryField.getText().toString());
                        childUpdates.put("/members/" + newUserUID + "/position/primaryAbbreviation", mPositionAbbrv.getText().toString());

                        if (Arrays.asList(selectedRanks).contains("Chief") || Arrays.asList(selectedRanks).contains("Officer")) {
                            childUpdates.put("/members/" + newUserUID + "/position/officer", true);
                        } else {
                            childUpdates.put("/members/" + newUserUID + "/position/officer", false);
                        }

                        //Add Ranks
                        for (int i=0; i < selectedRanks.length; i++) {
                            childUpdates.put("/members/" + newUserUID + "/position/ranks/" + Arrays.asList(selectedRanks).get(i).toString().toLowerCase(), true);
                        }
                        childUpdates.put("/members/" + newUserUID + "/position/rankMain", mainRank);
                        childUpdates.put("/departments/" + departmentKeys.get(selectedDepartment) + "/records/members/" + newUserUID, true);

                        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(MemberAdd.this, "Error, Please try again.", Toast.LENGTH_SHORT).show();
                                    pg.dismiss();
                                    return;
                                }

                                pg.dismiss();
                                setResult(RESULT_OK);
                                finish();
                            }
                        });

                    }
                }
            }
        });

        appEnableCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    appInfoLayout.setVisibility(View.VISIBLE);
                } else {
                    appInfoLayout.setVisibility(View.GONE);
                }
            }
        });

        Log.d("TOKEN", FirebaseAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }
                // ...
            }
        };

        mPositionRanksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(MemberAdd.this)
                        .title("Select Rank(s)")
                        .items(R.array.memberAddRanks)
                        .backgroundColorRes(R.color.bottom_sheet_background)
                        .itemsCallbackMultiChoice(selectedRanksInt, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                /**
                                 * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected check box to actually be selected.
                                 * See the limited multi choice dialog example in the sample project for details.
                                 **/
                                selectedRanksInt = which;
                                selectedRanks = text;

                                mainRank = null;
                                mainRankInt = -1;
                                mPositionMainRankButton.setText("Main Rank");

                                if (which.length > 0) {
                                    mPositionMainRankButton.setEnabled(true);
                                } else {
                                    mPositionMainRankButton.setEnabled(false);
                                }

                                mPositionRanksButton.setText(selectedRanks.length + " Ranks");
                                return true;
                            }
                        })
                        .positiveText("Done")
                        .show();
            }
        });

        mPositionMainRankButton.setEnabled(false);
        mPositionMainRankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(MemberAdd.this)
                        .title("Select Main Rank")
                        .content("This is used to determine a few things on the website. (Usually highest rank.)")
                        .items(selectedRanks)
                        .backgroundColorRes(R.color.bottom_sheet_background)
                        .itemsCallbackSingleChoice(mainRankInt, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                /**
                                 * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected check box to actually be selected.
                                 * See the limited multi choice dialog example in the sample project for details.
                                 **/
                                mPositionMainRankButton.setText(text);
                                mainRankInt = which;
                                mainRank = text;
                                return true;
                            }
                        })
                        .positiveText("Done")
                        .show();
            }
        });
    }

    private boolean checkField() {
        if (mFirstNameField.getText().toString().isEmpty()) {
            mFirstNameField.setError("First Name Required.");
            return false;
        }
        if (mLastNameField.getText().toString().isEmpty()) {
            mLastNameField.setError("Last Name Required.");
            return false;
        }
        if (selectedDepartment == -1) {
            Toast.makeText(this, "Must Select Department", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (appEnableCheckbox.isChecked()) {
            if (mAppEmailField.getText().toString().isEmpty()) {
                mAppEmailField.setError("Cannot be empty if enabled.");
                return false;
            }
            if (mAppPassField.getText().toString().isEmpty()) {
                mAppPassField.setError("Cannot be empty if enabled.");
                return false;
            }
        }
        if (mPositionPrimaryField.getText().toString().isEmpty()) {
            mPositionPrimaryField.setError("Cannot be empty.");
            return false;
        }
        if (mPositionAbbrv.getText().toString().isEmpty()) {
            mPositionAbbrv.setError("Cannot be empty.");
            return false;
        }
        if (selectedRanks.length == 0) {
            Toast.makeText(this, "Must Select Rank(s)", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mainRankInt == -1) {
            Toast.makeText(this, "Must Select Main Rank", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
        return false;
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

    public void positionInfo(View view) {
        new MaterialDialog.Builder(this)
                .title(R.string.memberAddPositionInfoTitle)
                .content(R.string.memberAddPositionInfoContent)
                .positiveText("Ok")
                .backgroundColorRes(R.color.bottom_sheet_background)
                .neutralText("Request Rank")
                .show();
    }

    public void nfirsInfo(View view) {
        new MaterialDialog.Builder(this)
                .title(R.string.memberAddNFIRSInfoTitle)
                .content(R.string.memberAddNFIRSInfoContent)
                .positiveText("Ok")
                .backgroundColorRes(R.color.bottom_sheet_background)
                .neutralText("MORE INFO")
                .show();
    }
}
