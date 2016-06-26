package com.fireapps.firedepartmentmanager;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;

public class LoginChooserActivity extends AppCompatActivity implements LoginChooserFragment.ButtonClickedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_chooser);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LogInFragment fragment = new LogInFragment();
        fragmentTransaction.add(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void ButtonClicked(int logIn) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (logIn) {
            case 0:
                LogInFragment logInFragment = new LogInFragment();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    logInFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
                }
                fragmentTransaction.replace(R.id.container, logInFragment);
                fragmentTransaction.addToBackStack("transaction");
                fragmentTransaction.commit();
                break;
            case 1:
                SignUpDepartmentID signUpFragment = new SignUpDepartmentID();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    signUpFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
                }
                fragmentTransaction.replace(R.id.container, signUpFragment);
                fragmentTransaction.addToBackStack("transaction");
                fragmentTransaction.commit();
                break;
        }
    }

    /*@Override
    public void DepartmentVerified(int logIn, ParseObject departmentEntity) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (logIn == 3) {
            Bundle bundle = new Bundle();
            bundle.putString("departmentID", departmentEntity.get("_id").toString());

            SignUpFragment signUpFragment = new SignUpFragment();
            signUpFragment.setArguments(bundle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                signUpFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            }
            fragmentTransaction.replace(R.id.container, signUpFragment);
            fragmentTransaction.addToBackStack("transaction");
            fragmentTransaction.commit();
        }
    }*/
}
