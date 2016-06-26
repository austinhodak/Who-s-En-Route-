package com.fireapps.firedepartmentmanager;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    @Bind(R.id.signUp_DepartmentID)MaterialEditText departmentIDField;
    @Bind(R.id.signUp_FullName)MaterialEditText fullNameField;
    @Bind(R.id.signUp_Email)MaterialEditText emailField;
    @Bind(R.id.signUp_Password)MaterialEditText passwordField;
    @Bind(R.id.signUp_PasswordRe)MaterialEditText passwordReField;

    @Bind(R.id.signup_fab)FloatingActionButton signUpFAB;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        departmentIDField.setText(bundle.getString("departmentID"));
        departmentIDField.setEnabled(false);

        signUpFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*ParseUser user = new ParseUser();
                user.setUsername(emailField.getText().toString());
                user.setPassword(passwordField.getText().toString());
                user.setEmail(emailField.getText().toString());

                user.put("name", fullNameField.getText().toString());

                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // Hooray! Let them use the app now.
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                        }
                    }
                });*/
            }
        });

        return view;
    }


}
