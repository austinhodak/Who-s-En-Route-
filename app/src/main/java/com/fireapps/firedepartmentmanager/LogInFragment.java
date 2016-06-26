package com.fireapps.firedepartmentmanager;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogInFragment extends Fragment {

    @Bind(R.id.login_field_email)
    EditText emailField;

    @Bind(R.id.login_field_password)
    EditText passwordField;

    /*@Bind(R.id.login_pg)
    ProgressBar progressBar;*/

    @Bind(R.id.imageView3)
    ImageView imageView;

    @Bind(R.id.loginbutton)
    Button login;

    @Bind(R.id.logo)
    ImageView logo;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public LogInFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in_new, container, false);
        ButterKnife.bind(this, view);

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.login_pg);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressBar.setVisibility(View.INVISIBLE);
        login.setVisibility(View.VISIBLE);
        mAuth = FirebaseAuth.getInstance();

        imageView.setColorFilter(Color.argb(210, 244, 67, 54));
        Glide.with(this).load(R.drawable.social).centerCrop().into(imageView);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                login.setVisibility(View.INVISIBLE);

                if (email.length() != 0 && password.length() != 0) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d("FDM", "signInWithEmail:onComplete:" + task.isSuccessful());
                                    if (!task.isSuccessful()) {
                                        Log.w("FDM", "signInWithEmail" + task.getException().getMessage(), task.getException());
                                        Toast.makeText(getActivity(), task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();

                                        emailField.setError("Wrong email or password.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                        login.setVisibility(View.VISIBLE);
                                    }

                                }
                            });

                } else if (email.length() == 0) {
                    emailField.setError("Email cannot be empty.");
                    progressBar.setVisibility(View.INVISIBLE);
                    login.setVisibility(View.VISIBLE);
                } else {
                    passwordField.setError("Password cannot be empty.");
                    progressBar.setVisibility(View.INVISIBLE);
                    login.setVisibility(View.VISIBLE);
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //progressBar.setVisibility(View.INVISIBLE);
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                    Log.d("FDM", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("FDM", "onAuthStateChanged:signed_out");
                }
            }
        };
        return view;
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
