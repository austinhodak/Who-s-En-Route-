package com.fireapps.firedepartmentmanager;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity implements SignupOneFragment.OnSignedUpListener, SignupTwoFragment.OnDepartmentJoined {

    public String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ImageView signupImage = (ImageView) findViewById(R.id.signup_image);
        Glide.with(this).load(R.drawable.heat).centerCrop().crossFade().into(signupImage);
        signupImage.setColorFilter(Color.argb(50, 247, 167, 80));

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SignupTwoFragment fragment = new SignupTwoFragment();
            fragmentTransaction.replace(R.id.signup_frame, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onSignedUp(FirebaseUser user, String email) {
        mEmail = email;
        //Next step
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignupTwoFragment fragment = new SignupTwoFragment();
        fragmentTransaction.replace(R.id.signup_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onJoined() {
        //Department Joined, Next Step, Details.
        Bundle bundle = new Bundle();
        bundle.putString("userEmail", mEmail);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignupThreeFragment fragment = new SignupThreeFragment();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.signup_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}