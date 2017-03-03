package com.fireapps.firedepartmentmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {

            Fragment fragment = getSupportFragmentManager().findFragmentByTag(SettingsMain.TAG);
            if (fragment == null) {
                fragment = new SettingsMain();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsMain())
                    .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu_bug:
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","bugs@whosenroute.com", null));
                intent.putExtra(Intent.EXTRA_EMAIL, "bugs@whosenroute.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");

                startActivity(Intent.createChooser(intent, "Send Bug Report"));
                break;
            case R.id.settings_menu_feature:
                Intent intent2 = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","features@whosenroute.com", null));
                intent2.putExtra(Intent.EXTRA_EMAIL, "features@whosenroute.com");
                intent2.putExtra(Intent.EXTRA_SUBJECT, "Feature Suggestion");

                startActivity(Intent.createChooser(intent2, "Send Feature Suggestion"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsMain extends PreferenceFragmentCompat {

        public static final String KEY_INCIDENT_NOTIFY_SCREENON = "pref_incident_notify_screenon";
        public static final String KEY_INCIDENT_NOTIFY_TYPE = "pref_incident_notify_type";

        public static final String TAG = "settings_fragment";

        @Override
        public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);

            /*Preference preference = (Preference) findPreference("notification_screen");
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), SettingsNestedActivity.class);
                    intent.setAction("notifications");
                    getActivity().startActivity(intent);
                    return true;
                }
            });*/

            Preference preference2 = (Preference) findPreference("responding_screen");
            preference2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), SettingsNestedActivity.class);
                    intent.setAction("responding");
                    getActivity().startActivity(intent);
                    return true;
                }
            });

            Preference preference3 = (Preference) findPreference("location_screen");
            preference3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), SettingsNestedActivity.class);
                    intent.setAction("location");
                    getActivity().startActivity(intent);
                    return true;
                }
            });

            ((Preference)findPreference("pref_logout")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sharedPreferences.edit().putStringSet("departmentIds", null).apply();
                    sharedPreferences.edit().putString("selectedDepartment", null).apply();
                    sharedPreferences.edit().putString("selectedDepartmentAbbrv", null).apply();
                    sharedPreferences.edit().putString("userName", null).apply();

                    FirebaseAuth.getInstance().signOut();
                    getActivity().setResult(RESULT_FIRST_USER);
                    getActivity().finish();
                    return true;
                }
            });

            ((Preference)findPreference("departments_manage")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), DepartmentList.class);
                    getActivity().startActivity(intent);
                    return true;
                }
            });
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
