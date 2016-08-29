package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupOneFragment extends Fragment {

    OnSignedUpListener mCallback;

    TextView mNameField, mEmailField, mPasswordField;
    FloatingActionButton mSignupButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_one, container, false);
        firebaseDatabase = FirebaseDatabase.getInstance();

        mNameField = (TextView) view.findViewById(R.id.signup_name);
        mEmailField = (TextView) view.findViewById(R.id.signup_email);
        mPasswordField = (TextView) view.findViewById(R.id.signup_password);
        mSignupButton = (FloatingActionButton) view.findViewById(R.id.next_fab);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFields()) {
                    final String mEmail = mEmailField.getText().toString();
                    final String mName = mNameField.getText().toString();
                    String mPassword = mPasswordField.getText().toString();

                    mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            Map<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put("/users/" + user.getUid() + "/email", mEmail);
                            childUpdates.put("/users/" + user.getUid() + "/name", mName);
                            firebaseDatabase.getReference().updateChildren(childUpdates);

                            mCallback.onSignedUp(user, mEmail);

                            if (!task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User signed in

                } else {
                    //Signed out
                }
            }
        };

        return view;
    }

    private boolean checkFields() {
        if (mNameField.getText().toString().length() >= 2 && mEmailField.getText().toString().length() >= 3 && mPasswordField.getText().toString().length() >= 3) {
            return true;
        }
        if (mNameField.getText().toString().length() < 2) {
            mNameField.setError("Required");
            return false;
        }
        if (mEmailField.getText().toString().length() < 3) {
            mEmailField.setError("Required");
            return false;
        }
        if (mPasswordField.getText().toString().length() < 3) {
            mPasswordField.setError("Required");
            return false;
        }
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnSignedUpListener) activity;
        } catch (ClassCastException e) {

        }
    }

    public interface OnSignedUpListener {
        public void onSignedUp(FirebaseUser user, String email);
    }
}