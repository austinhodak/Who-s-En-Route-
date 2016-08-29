package com.fireapps.firedepartmentmanager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class DepartmentAdd extends AppCompatActivity {

    @BindView(R.id.department_add_bg)
    ImageView background;

    @BindView(R.id.department_add_icon)
    ImageView icon;

    @BindView(R.id.department_add_header)
    RelativeLayout headerLayout;

    @BindView(R.id.department_add_buttons)
    LinearLayout buttons;

    @BindView(R.id.department_add_qr)
    SurfaceView surfaceView;

    @BindView(R.id.department_button_qr)
    Button qrButton;

    @BindView(R.id.department_button_pin)
    Button pinButton;

    @BindView(R.id.department_add_header_small)
    LinearLayout smallHeader;

    QREader qrEader;
    private FirebaseDatabase firebaseDatabase;
    private DataSnapshot foundDepartment;
    private CardView deptInfo;
    private TextView deptName;
    private TextView deptAddress;
    private TextView deptPhone;
    private Button mDeptInfoYes;
    private Button mDeptInfoNo;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_add);
        ButterKnife.bind(this);

        Glide.with(this).load(R.drawable.heat).centerCrop().into(background);
        background.setColorFilter(Color.argb(125, 66, 66, 66));
        Glide.with(this).load(R.drawable.icon).into(icon);

        deptInfo = (CardView) findViewById(R.id.signuptwo_deptinfo);
        deptName = (TextView) findViewById(R.id.signuptwo_dept_name);
        deptAddress = (TextView) findViewById(R.id.signuptwo_dept_address);
        deptPhone = (TextView) findViewById(R.id.signuptwo_dept_phone);

        mDeptInfoYes = (Button) findViewById(R.id.signuptwo_deptinfo_yes);
        mDeptInfoNo = (Button) findViewById(R.id.signuptwo_deptinfo_no);

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headerLayout.setVisibility(View.GONE);
                buttons.setVisibility(View.GONE);
                surfaceView.setVisibility(View.VISIBLE);
                smallHeader.setVisibility(View.VISIBLE);
                qrEader = new QREader.Builder(DepartmentAdd.this, surfaceView, new QRDataListener() {
                    @Override
                    public void onDetected(final String data) {
                        Log.d("QR", data);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findDepartment(data);
                                qrEader.stop();
                            }
                        });

                    }
                }).height(300).width(300).build();
                qrEader.init();
            }
        });

        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headerLayout.setVisibility(View.GONE);
                buttons.setVisibility(View.GONE);
                smallHeader.setVisibility(View.VISIBLE);
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();

        qrEader = new QREader.Builder(this, surfaceView, new QRDataListener() {
            @Override
            public void onDetected(final String data) {
                Log.d("QR", data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findDepartment(data);
                        qrEader.releaseAndCleanup();
                    }
                });

            }
        }).height(300).width(300).build();
        qrEader.init();

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
            }
        });
        mDeptInfoNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttons.setVisibility(View.VISIBLE);
                surfaceView.setVisibility(View.GONE);
                deptInfo.setVisibility(View.GONE);
                headerLayout.setVisibility(View.VISIBLE);
                smallHeader.setVisibility(View.GONE);
            }
        });
    }

    public void findDepartment(String id) {
        surfaceView.setVisibility(View.GONE);
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
}
