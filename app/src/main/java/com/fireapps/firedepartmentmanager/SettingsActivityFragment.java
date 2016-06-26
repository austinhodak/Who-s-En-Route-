package com.fireapps.firedepartmentmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;


/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    public static final String KEY_PREF_PUSH_ENABLE = "whoresp_pushenable";
    public static final String KEY_PREF_PUSH_ENABLE_NR = "whoresp_pushWhenNotResponding";
    public static final String KEY_PREF_SEND_PUSH_ENABLE = "whoresp_sendpush";
    public static final String KEY_PREF_WIFI = "woresp_wifi";
    public static final String KEY_PREF_TEST = "devtesting";
    public static final String KEY_PREF_NOTIFY_SOUND = "pref_resp_notify_tone";
    public static final String KEY_PREF_NOTIFY_VIBRATE = "pref_resp_notify_vibrate";
    public static final String KEY_PREF_RESPONSE_ACCURACY = "pref_map_response_accuracy";
    public static final String KEY_PREF_RESPONSE_FREQUENCY = "pref_map_response_frequency";

    public static final String KEY_PREF_RESP_PROMPT = "pref_resp_prompt";

    boolean devEnabled;

    Preference dispNumber;
    private Preference dispEmail;
    private Preference wifiSSID;
    Preference testing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);

        dispNumber = findPreference("disp_phone");
        dispEmail = findPreference("disp_one");

        wifiSSID = findPreference("whoresp_wifi");

        testing = findPreference(KEY_PREF_TEST);
        testing.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                //restartApp();

                return true;

            }
        });

        dispNumber.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //open browser or intent here
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(i, 0);

                return false;
            }
        });



        /*ParseUser.getCurrentUser().getParseObject("departmentP").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject department, ParseException e) {
                dispNumber.setSummary(department.getString("dispatchPhone"));
                dispEmail.setSummary(department.getString("dispatchEmail"));
                wifiSSID.setSummary(department.getString("departmentWifi"));
            }
        });*/

        //TODO Theme Light
    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), LoginDispatch.class);
        int mPendingIntentId = 100;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        android.os.Process.killProcess(Process.myPid());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == getActivity().RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getActivity().getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            dispNumber.setSummary(cursor.getString(column));

            Log.d("phone number", cursor.getString(column));
        }
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