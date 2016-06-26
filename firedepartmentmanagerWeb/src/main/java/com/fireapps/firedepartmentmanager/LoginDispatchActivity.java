package com.fireapps.firedepartmentmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by austinhodak on 3/28/15.
 */
public abstract class LoginDispatchActivity extends Activity {

    private static final int LOGIN_REQUEST = 0;
    private static final int TARGET_REQUEST = 1;

    private static final String LOG_TAG = "LoginDispatch";
    private DatabaseReference myFirebaseRef;

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        runDispatch();
    }

    @Override
    final protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            runDispatch();
        } else {
            finish();
        }
    }

    private void runDispatch() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            startActivityForResult(new Intent(this, MainActivity.class), TARGET_REQUEST);
            Log.d("FDM", "LoggedIn");
        } else {
            // No user is signed in
            Intent i = new Intent(this, LoginChooserActivity.class);
            startActivityForResult(i, LOGIN_REQUEST);
            Log.d("FDM", "NotLoggedIn");
        }
    }
}
