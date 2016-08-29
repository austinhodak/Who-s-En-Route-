package com.fireapps.firedepartmentmanager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;


public class SettingsNestedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {

            Fragment fragment = getSupportFragmentManager().findFragmentByTag(SettingsMain.TAG);
            if (fragment == null) {
                fragment = new SettingsMain();
            }
            Bundle bundle = new Bundle();
            bundle.putString("action", getIntent().getAction());

            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public static class SettingsMain extends PreferenceFragmentCompat {

        public static final String KEY_INCIDENT_NOTIFY_SCREENON = "pref_incident_notify_screenon";
        public static final String KEY_INCIDENT_NOTIFY_TYPE = "pref_incident_notify_type";

        public static final String TAG = "settings_fragment";

        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {

            switch (getArguments().getString("action")) {
                case "notifications":
                    addPreferencesFromResource(R.xml.pref_notifications);
                    getActivity().setTitle("Notification Settings");
                    break;
                case "responding":
                    addPreferencesFromResource(R.xml.pref_responding);
                    getActivity().setTitle("Response Settings");
                    break;
                case "location":
                    addPreferencesFromResource(R.xml.pref_location);
                    getActivity().setTitle("Location Settings");
                    break;
            }
        }

        @Override
        public void setDivider(Drawable divider) {
            super.setDivider(new ColorDrawable(Color.TRANSPARENT));
        }

        @Override
        public void setDividerHeight(int height) {
            super.setDividerHeight(0);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    getActivity().finish();
                    return true;
            }

            return super.onOptionsItemSelected(item);
        }
    }
}
