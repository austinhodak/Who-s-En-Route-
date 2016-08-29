package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by austinhodak on 6/16/16.
 */

public class PopupActivity extends Activity implements View.OnClickListener {
    Button mSceneButton, mStationButton, mCRButton, mDismissButton, mOpenButton;
    TextView mTitleTv, mDescTv;
    Context context;
    ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private DatabaseReference userReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;
    private String department;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (extras != null) {
            if (extras.containsKey("department")) {
                department = extras.getString("department");
            }
        }
        boolean incidentTurnOnScreen = true;
        if (department != null) {
            incidentTurnOnScreen = sharedPreferences.getBoolean(department + "_incidentScreenOn", true);
        }

        if (incidentTurnOnScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }


        context = this;
        setContentView(R.layout.popup_new_incident);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            firebaseUser = user;
        } else {
            // No user is signed in
        }
        firebaseDatabase = FirebaseDatabase.getInstance();
        userReference = firebaseDatabase.getReference("users/" + firebaseUser.getUid());

        mSceneButton = (Button) findViewById(R.id.popup_scene);
        mStationButton = (Button) findViewById(R.id.popup_station);
        mCRButton = (Button) findViewById(R.id.popup_cr);
        mDismissButton = (Button) findViewById(R.id.popup_dismiss);
        mOpenButton = (Button) findViewById(R.id.popup_open);
        mSceneButton.setOnClickListener(this);
        mStationButton.setOnClickListener(this);
        mCRButton.setOnClickListener(this);
        mDismissButton.setOnClickListener(this);
        mOpenButton.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.popup_progress);

        mTitleTv = (TextView) findViewById(R.id.popup_title);
        mDescTv = (TextView) findViewById(R.id.popup_desc);


        if (extras != null) {
            if (extras.containsKey("incidentTitle")) {
                String title = extras.getString("incidentTitle");
                mTitleTv.setText(title);
            }
            if (extras.containsKey("incidentDesc")) {
                String desc = extras.getString("incidentDesc");
                mDescTv.setText(desc);
            }
            if (extras.containsKey("department")) {
                department = extras.getString("department");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popup_dismiss:
                finish();
                break;
            case R.id.popup_open:
                Intent i = new Intent(this, NavDrawerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
                break;
            case R.id.popup_station:
                updateRespondingStatus(0);
                break;
            case R.id.popup_scene:
                updateRespondingStatus(1);
                break;
            case R.id.popup_cr:
                updateRespondingStatus(2);
                break;
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }


    private void updateRespondingStatus(int mRespondingSelected) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        String mLastUpdateTime = dateFormat.format(date);

        switch (mRespondingSelected) {
            case 0:
                progressBar.setVisibility(View.VISIBLE);
                //Station
                Map<String, Object> respondingToStation = new HashMap<String, Object>();
                respondingToStation.put("respondingTo", "Station");
                respondingToStation.put("isResponding", true);
                respondingToStation.put("respondingTime", mLastUpdateTime);
                userReference.updateChildren(respondingToStation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(PopupActivity.this, NavDrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                break;
            case 1:
                //Scene
                progressBar.setVisibility(View.VISIBLE);
                Map<String, Object> respondingToScene = new HashMap<String, Object>();
                respondingToScene.put("respondingTo", "Scene");
                respondingToScene.put("isResponding", true);
                respondingToScene.put("respondingTime", mLastUpdateTime);
                userReference.updateChildren(respondingToScene).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(PopupActivity.this, NavDrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
                break;
            case 2:
                //Can't Respond
                progressBar.setVisibility(View.VISIBLE);
                Map<String, Object> cantRespond = new HashMap<String, Object>();
                cantRespond.put("respondingTo", "Can't Respond");
                cantRespond.put("isResponding", true);
                cantRespond.put("respondingTime", mLastUpdateTime);
                userReference.updateChildren(cantRespond).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                        finish();
                    }
                });
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                Map<String, Object> respondingReset = new HashMap<String, Object>();
                respondingReset.put("respondingTo", "");
                respondingReset.put("isResponding", false);
                respondingReset.put("respondingTime", "");
                userReference.updateChildren(respondingReset).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                    }
                });
                break;
        }
    }
}
