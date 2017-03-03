package com.fireapps.firedepartmentmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.app.NavigationPolicy;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class FirstSignupActivity extends IntroActivity implements SlideLoginChooser.OnChooserButtonClickedListener {

    private SimpleSlide permissionsSlide;
    private FragmentSlide loginSlide, chooserSlide;
    private SimpleSlide finalSlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new FragmentSlide.Builder()
                .background(R.color.md_blue_500)
                .backgroundDark(R.color.md_blue_700)
                .fragment(new SlideFirst())
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Features!")
                .image(R.drawable.idea)
                .description("From responder tracking to incident reporting. Our system can do it all! Think we should add a feature? Let us know!")
                .background(R.color.md_indigo_500)
                .backgroundDark(R.color.md_indigo_700)
                .build());

        chooserSlide = new FragmentSlide.Builder()
                .background(R.color.md_green_500)
                .backgroundDark(R.color.md_green_700)
                .fragment(new SlideLoginChooser())
                //.canGoForward(false) /*WAITING FOR FIX FROM COMMIT.*/
                .build();

        addSlide(chooserSlide);

        permissionsSlide = new SimpleSlide.Builder()
                .title("Permissions")
                .description("You'll need to grant some permissions so the app can run smoothly without issues.")
                .background(R.color.md_orange_500)
                .backgroundDark(R.color.md_orange_700)
                .image(R.drawable.attention)
                .permissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.VIBRATE, Manifest.permission.CAMERA})
                .build();

        loginSlide = new FragmentSlide.Builder()
                .background(R.color.md_green_500)
                .backgroundDark(R.color.md_green_700)
                .fragment(new SignupOneFragment())
                .build();

        finalSlide = new SimpleSlide.Builder()
                .title("All Set!")
                .description("You are all set! Let us know how you like the app and what can be improved!")
                .background(R.color.md_cyan_500)
                .backgroundDark(R.color.md_cyan_700)
                .image(R.drawable.ok)
                //.canGoBackward(false)
                .build();

        setNavigationPolicy(new NavigationPolicy() {
            @Override
            public boolean canGoForward(int position) {
                return true;
            }

            @Override
            public boolean canGoBackward(int position) {
                return true;
            }
        });
    }

    @Override
    public void onLoginButtonSelected() {
        //addSlide(loginSlide);
    }

    @Override
    public void onLoggedIn() {
        //Check Permissions
        int permissionCheckLOC = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheckVIB = ContextCompat.checkSelfPermission(this,
                Manifest.permission.VIBRATE);
        int permissionCheckCAM = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permissionCheckLOC == PackageManager.PERMISSION_GRANTED && permissionCheckVIB == PackageManager.PERMISSION_GRANTED) {
            addSlide(finalSlide);
            nextSlide();
        } else {
            addSlide(permissionsSlide);
            nextSlide();
            addSlide(finalSlide);
        }
        //removeSlide(chooserSlide);
    }

    void addPermissionsSlide() {
        addSlide(permissionsSlide);
    }

}
