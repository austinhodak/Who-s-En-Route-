package com.fireapps.firedepartmentmanager;


import android.app.Activity;
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
public class SignUpDepartmentID extends Fragment {

    @Bind(R.id.signUpDP_DepartmentID) MaterialEditText departmentIDField;

    @Bind(R.id.signupDP_fab) FloatingActionButton fab;

    public SignUpDepartmentID() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up_department_id, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*ParseQuery<ParseObject> query;
                query = ParseQuery.getQuery("Departments");
                query.getInBackground(departmentIDField.getText().toString(), new GetCallback<ParseObject>() {
                    public void done(final ParseObject result, ParseException e) {
                        if (e == null) {
                            // object will be your game score
                            boolean wrapInScrollView = true;
                            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                    .title("Is This Your Department?")
                                    .customView(R.layout.signup_department_dialog, wrapInScrollView)
                                    .positiveText("Yes")
                                    .negativeText("No")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onNegative(MaterialDialog dialog) {
                                            super.onNegative(dialog);
                                        }

                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                            //Verified department, continue sign up.
                                            mListener.DepartmentVerified(3, result);
                                        }
                                    })
                                    .show();
                            View view = dialog.getCustomView();
                            assert view != null;
                            TextView departmentName = (TextView) view.findViewById(R.id.signup_dpt_dialog_name);
                            TextView departmentAddress = (TextView) view.findViewById(R.id.signup_dpt_dialog_address);
                            TextView departmentPhone = (TextView) view.findViewById(R.id.signup_dpt_dialog_phone);
                            TextView departmentID = (TextView) view.findViewById(R.id.signup_dpt_dialog_id);

                            departmentName.setText(result.get("departmentName").toString());
                            departmentAddress.setText(result.get("departmentAddress").toString());
                            departmentPhone.setText(result.get("departmentPhone").toString());
                            departmentID.setText(departmentIDField.getText().toString());
                        } else {
                            // something went wrong
                        }
                    }
                });*/
            }
        });

        return view;
    }

    /*public interface DepartmentVerifiedListener {
        void DepartmentVerified(int logIn, ParseObject departmentEntity);
    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (DepartmentVerifiedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

}
