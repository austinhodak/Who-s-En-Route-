package com.fireapps.firedepartmentmanager;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class LoginChooser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_chooser);


        ImageView signupImage = (ImageView) findViewById(R.id.signup_image);
        Glide.with(this).load(R.drawable.heat).centerCrop().crossFade().into(signupImage);
        signupImage.setColorFilter(Color.argb(50, 247, 167, 80));

        Button signupB = (Button)findViewById(R.id.signup_signup);
        Button loginB = (Button)findViewById(R.id.signup_login);

        signupB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginChooser.this, SignupActivity.class);
                startActivity(intent);
            }
        });
        loginB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginChooser.this, LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
