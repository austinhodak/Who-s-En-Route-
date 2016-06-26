package com.fireapps.firedepartmentmanager;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogInFragment extends Fragment {

    @Bind(R.id.login_fab_progress)FABProgressCircle fabProgressCircle;
    @Bind(R.id.login_fab)FloatingActionButton floatingActionButton;

    @Bind(R.id.login_departmentID)MaterialEditText departmentIDfield;
    @Bind(R.id.login_email)MaterialEditText emailField;
    @Bind(R.id.login_password)MaterialEditText passwordField;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    String email, password;

    public LogInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailField.getText().toString();
                password = passwordField.getText().toString();

                setSpinner(true);
                if (email.length() != 0 && password.length() != 0) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d("FDM", "signInWithEmail:onComplete:" + task.isSuccessful());

                                    if (!task.isSuccessful()) {
                                        Log.w("FDM", "signInWithEmail", task.getException());
                                        Toast.makeText(getActivity(), "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();

                                        setSpinner(false);

                                        emailField.setError("Wrong email or password.");
                                    }

                                }
                            });

                } else if (email.length() == 0) {
                    emailField.setError("Email cannot be empty.");
                    setSpinner(false);
                } else {
                    passwordField.setError("Password cannot be empty.");
                    setSpinner(false);
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    sharedPreferences.edit().putString("email", email).putString("password", password).apply();
                    fabProgressCircle.beginFinalAnimation();
                    fabProgressCircle.attachListener(new FABProgressListener() {
                        @Override
                        public void onFABProgressAnimationEnd() {
                            //Toast.makeText(getActivity(), user.getUsername(), Toast.LENGTH_SHORT).show();
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    });
                    Log.d("FDM", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("FDM", "onAuthStateChanged:signed_out");
                }
            }
        };

        return view;
    }

    private void setSpinner(boolean active){
        if(active){
            fabProgressCircle.show();
        } else {
            fabProgressCircle.hide();
        }
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
