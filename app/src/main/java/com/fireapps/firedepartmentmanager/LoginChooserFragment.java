package com.fireapps.firedepartmentmanager;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginChooserFragment extends Fragment {

    @Bind(R.id.loginChooser_LogIn)
    Button logInButton;
    @Bind(R.id.loginChooser_SignUp)
    Button signUpButton;
    private ButtonClickedListener mListener;

    public LoginChooserFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_chooser, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.ButtonClicked(0);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.ButtonClicked(1);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ButtonClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    public interface ButtonClickedListener {
        void ButtonClicked(int logIn);
    }


}
