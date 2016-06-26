package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by austinhodak on 6/14/16.
 */

public class LoginDispatch extends Activity {

    private static final int LOGIN_REQUEST = 0;
    private static final int TARGET_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            runDispatch();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        runDispatch();
    }

    private void runDispatch() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent i = new Intent(this, NavDrawerActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(i, TARGET_REQUEST);
        } else {
            // No user is signed in
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(i, LOGIN_REQUEST);
        }
    }
}
