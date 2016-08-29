package com.fireapps.firedepartmentmanager;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by austinhodak on 7/2/16.
 */

public class SignupThreeFragment extends Fragment implements ColorChooserDialog.ColorCallback, View.OnClickListener {
    Button mColorButton, mProviderButton;
    EditText mNameField, mEmailField, mPositionField, mPositionAbbrvField, mPhoneField;
    Switch mOfficerSwitch;
    FloatingActionButton saveFAB;
    FirebaseUser firebaseUser;
    int selectedProvider = -1;
    String selectedProviderString;
    ProgressBar progressBar;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public SignupThreeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_three, container, false);
        firebaseDatabase = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User signed in
                    firebaseUser = user;

                } else {
                    //Signed out
                }
            }
        };


        mColorButton = (Button) view.findViewById(R.id.signup_userDetails_positionColor);
        mNameField = (EditText) view.findViewById(R.id.signup_userDetails_name);
        mEmailField = (EditText) view.findViewById(R.id.signup_userDetails_email);
        mPositionField = (EditText) view.findViewById(R.id.signup_userDetails_position);
        mPositionAbbrvField = (EditText) view.findViewById(R.id.signup_userDetails_positionAbbrv);
        mPhoneField = (EditText) view.findViewById(R.id.signup_userDetails_phone);
        mProviderButton = (Button) view.findViewById(R.id.signup_userDetails_phoneProv);
        mOfficerSwitch = (Switch) view.findViewById(R.id.signup_userDetails_officer);
        progressBar = (ProgressBar) view.findViewById(R.id.signup_userDetails_PG);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.getString("userEmail") != null) {
            mEmailField.setText(bundle.getString("userEmail"));
        }

        mColorButton.setOnClickListener(this);
        mProviderButton.setOnClickListener(this);

        saveFAB = (FloatingActionButton) view.findViewById(R.id.next_fab2);
        saveFAB.setOnClickListener(this);

        return view;
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signup_userDetails_positionColor:
                new SpectrumDialog.Builder(getActivity())
                        .setColors(R.array.mdcolor_500)
                        .setSelectedColor(mColorButton.getCurrentTextColor())
                        .setDismissOnColorSelected(true)
                        .setOutlineWidth(2)
                        .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(boolean positiveResult, int color) {
                                mColorButton.setTextColor(color);
                                Log.d("COLOR", color + "");
                            }
                        }).build().show(getChildFragmentManager(), "color_dialog");
                break;
            case R.id.next_fab2:
                saveDetails();
                break;
            case R.id.signup_userDetails_phoneProv:
                final String[] providerSMS = new String[]{"@txt.att.net", "@messaging.sprintpcs.com", "@tmomail.net", "@vtext.com"};
                new MaterialDialog.Builder(getActivity())
                        .title("Phone Provider")
                        .items(new String[]{"AT&T", "Sprint", "T-Mobile", "Verizon"})
                        .itemsCallbackSingleChoice(selectedProvider, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                selectedProvider = which;
                                mProviderButton.setText(text);
                                selectedProviderString = providerSMS[which];
                                return false;
                            }
                        }).positiveText("Choose").show();
                break;
        }
    }

    private void saveDetails() {
        if (!validateFields()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/name", mNameField.getText().toString());
        childUpdates.put("/email", mEmailField.getText().toString());
        childUpdates.put("/position", mPositionField.getText().toString());
        childUpdates.put("/positionAbbrv", mPositionAbbrvField.getText().toString());
        //childUpdates.put("/positionColor", String.valueOf(mColorButton.getCurrentTextColor()));
        childUpdates.put("/positionOfficer", mOfficerSwitch.isChecked());
        childUpdates.put("/phoneNum", Long.valueOf(mPhoneField.getText().toString()));
        childUpdates.put("/phoneProvider", selectedProviderString);

        firebaseDatabase.getReference("users/" + firebaseUser.getUid()).updateChildren(childUpdates);
        progressBar.setVisibility(View.GONE);
    }

    private boolean validateFields() {
        //TODO Validate Fields.
        return true;
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
