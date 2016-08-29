package com.fireapps.firedepartmentmanager;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class FirstSignupActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new SimpleSlide.Builder()
                .title("Welcome!")
                .description("Saving time and saving lives! \nThe BEST First Responder system available.")
                .background(R.color.md_teal_500)
                .backgroundDark(R.color.md_teal_700)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Features!")
                .description("From responder tracking to incident reporting. Our system can do it all! Think we should add a feature? Let us know!")
                .background(R.color.md_cyan_500)
                .backgroundDark(R.color.md_cyan_700)
                .build());



//        addSlide(new SimpleSlide.Builder()
//                .background(R.color.md_green_500)
//                .backgroundDark(R.color.md_green_700)
//                .title("Create an Account or Login!")
//                .buttonCtaLabel("Get Started!")
//                .buttonCtaClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        addSlide();
//                    }
//                })
//                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Create an Account or Login!")
                .description("TEST")
                .background(R.color.newRed)
                .backgroundDark(R.color.newRedDark)
                .buttonCtaLabel("Get Started!")
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addSlide();
                    }
                })
                .build());

        //setButtonCtaVisible(true);
    }

    public void addSlide() {
        addSlide(new SimpleSlide.Builder()
                .title("Permissions")
                .description("You'll need to grant some permissions so the app can run smoothly without issues.")
                .background(R.color.md_orange_500)
                .backgroundDark(R.color.md_orange_700)
                .permissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.VIBRATE, Manifest.permission.CAMERA})
                .build());
    }
}
