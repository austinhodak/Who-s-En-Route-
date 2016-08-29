package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.glomadrian.codeinputlib.CodeInput;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

/**
 * Created by austinhodak on 7/2/16.
 */

public class SignupTwoFragment extends Fragment {

    SurfaceView surfaceView;
    Toast toast;
    TextView title, content;
    Button qrCodeButton, pinButton, mDeptInfoYes, mDeptInfoNo;
    LinearLayout linearLayout;
    CodeInput pinLayout;
    QREader qrEader;
    ProgressBar progressBar;
    CardView deptInfo;
    TextView deptName, deptAddress, deptPhone;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser newUser;
    private DataSnapshot foundDepartment;
    private OnDepartmentJoined mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_two, container, false);

        surfaceView = (SurfaceView) view.findViewById(R.id.signup_qr);
        qrCodeButton = (Button) view.findViewById(R.id.signuptwo_qrButton);
        pinButton = (Button) view.findViewById(R.id.signuptwo_codeButton);
        title = (TextView) view.findViewById(R.id.signuptwo_title);
        content = (TextView) view.findViewById(R.id.signuptwo_desc);
        linearLayout = (LinearLayout) view.findViewById(R.id.signuptwo_buttonLayout);
        progressBar = (ProgressBar) view.findViewById(R.id.signuptwo_pg);

        pinLayout = (CodeInput) view.findViewById(R.id.pin_layout);

        deptInfo = (CardView) view.findViewById(R.id.signuptwo_deptinfo);
        deptName = (TextView) view.findViewById(R.id.signuptwo_dept_name);
        deptAddress = (TextView) view.findViewById(R.id.signuptwo_dept_address);
        deptPhone = (TextView) view.findViewById(R.id.signuptwo_dept_phone);

        mDeptInfoYes = (Button) view.findViewById(R.id.signuptwo_deptinfo_yes);
        mDeptInfoNo = (Button)view.findViewById(R.id.signuptwo_deptinfo_no);

        surfaceView.setVisibility(View.GONE);

        firebaseDatabase = FirebaseDatabase.getInstance();

        qrEader = new QREader.Builder(getActivity(), surfaceView, new QRDataListener() {
            @Override
            public void onDetected(final String data) {
                Log.d("QR", data);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findDepartment(data);
                        qrEader.releaseAndCleanup();
                    }
                });

            }
        }).height(300).width(300).build();
        qrEader.init();

        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.GONE);
                surfaceView.setVisibility(View.VISIBLE);
                title.setText("QR Scan");
                content.setText("Scan the QR Code on your department's dashboard.");
            }
        });
        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinLayout.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
                title.setText("Pin Code");
                content.setText("Enter the pin code generated on your department's dashboard.");
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User signed in
                    newUser = user;
                } else {
                    //Signed out
                }
            }
        };

        mDeptInfoYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> childUpdates = new HashMap<>();

                childUpdates.put("/users/" + newUser.getUid() + "/department", foundDepartment.getKey());
                childUpdates.put("/users/" + newUser.getUid() + "/departments/" + foundDepartment.getKey(), true);
                childUpdates.put("/departments/" + foundDepartment.getKey() + "/members/" + newUser.getUid(), true);
                firebaseDatabase.getReference().updateChildren(childUpdates);
                mCallback.onJoined();
            }
        });
        mDeptInfoNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.VISIBLE);
                pinLayout.setVisibility(View.GONE);
                surfaceView.setVisibility(View.GONE);
                deptInfo.setVisibility(View.GONE);
                qrEader.init();
            }
        });

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    if (linearLayout.getVisibility() != View.VISIBLE) {
                        surfaceView.setVisibility(View.GONE);
                        pinLayout.setVisibility(View.GONE);
                    }
                } else {
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    public void findDepartment(String id) {
        surfaceView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        try {
            firebaseDatabase.getReference("departments/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Department department = dataSnapshot.getValue(Department.class);
                    if (department == null) {
                        return;
                    }
                    foundDepartment = dataSnapshot;
                    Log.d("QR", (String) dataSnapshot.child("name").getValue());
                    //Toast.makeText(getActivity(), (String) dataSnapshot.child("name").getValue(), Toast.LENGTH_SHORT).show();
                    title.setText("Agency Found");
                    content.setText("Is this your department?");
                    progressBar.setVisibility(View.GONE);

                    deptName.setText((String) dataSnapshot.child("name").getValue());
                    deptAddress.setText((String) dataSnapshot.child("address").child("street").getValue());
                    deptPhone.setText(String.valueOf((long) dataSnapshot.child("departmentPhone").getValue()));
                    deptInfo.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        qrEader.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        qrEader.stop();
        qrEader.releaseAndCleanup();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnDepartmentJoined) activity;
        } catch (ClassCastException e) {

        }
    }

    public interface OnDepartmentJoined {
        public void onJoined();
    }


}