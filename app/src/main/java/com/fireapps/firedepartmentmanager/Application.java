package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.firebase.database.FirebaseDatabase;
import com.pubnub.api.Pubnub;

import io.fabric.sdk.android.Fabric;

/**
 * Created by austinhodak on 7/28/15.
 */
public class Application extends android.app.Application {

    private GoogleCloudMessaging gcm;
    private String regId;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();
        //Debug.startMethodTracing("startup");
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectAll()
        .penaltyLog()
        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .penaltyDeath()
        .build());

        context = getApplicationContext();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        register();

        Fabric.with(this, new Crashlytics());

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //Fabric.with(context, new Crashlytics());
    }
    private void register() {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            try {
                regId = getRegistrationId(context);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (regId.isEmpty()) {
                registerInBackground();
            } else {

            }
        } else {

        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private String getRegistrationId(Context context) throws Exception {
        final SharedPreferences prefs = getSharedPreferences(Application.class.getSimpleName(), MODE_PRIVATE);
        String registrationId = prefs.getString("registration_id", "");
        if (registrationId.isEmpty()) {
            return "";
        }

        return registrationId;
    }

    private void registerInBackground() {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register("44746123279");
                    msg = "Device registered, registration ID: " + regId;

                    sendRegistrationId(regId);

                    storeRegistrationId(context, regId);
                } catch (Exception e) {
                    msg = "Error : "+ e.getMessage();
                    Log.e("FDM", msg);
                }
    }

    private final Pubnub pubnub = new Pubnub("pub-c-31e7d284-39d2-4aff-b682-d709c4aa105e", "sub-c-8c10dcfe-3732-11e5-87df-02ee2ddab7fe");

    private void sendRegistrationId(String regId) {

        if (!sharedPref.getBoolean(SettingsActivityFragment.KEY_PREF_TEST, false)) {
            pubnub.enablePushNotificationsOnChannel("gvfd1", regId);
        } else {
            //TESTING
            pubnub.enablePushNotificationsOnChannel("testing1", regId);
        }

    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences(Application.class.getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("registration_id", regId);
        editor.apply();
    }



    public String method(String str) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == '+') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public void updateCurrentCallInfo(final String address, final String callType, final boolean priorityCall, final String message) {
        /*ParseQuery<IncidentObject> query = ParseQuery.getQuery(IncidentObject.class);
        query.whereEqualTo("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
        query.getFirstInBackground(new GetCallback<IncidentObject>() {
            @Override
            public void done(IncidentObject object, ParseException e) {
                if (object.getBoolean("isActive")) {
                    //Incident Already Active, Check Address and Call Type to see if text is another incident.
                    if (!object.getString("location").contains(address)) {
                        //Different Address = Separate Incident. Make New.
                        IncidentObject incidentObject = new IncidentObject();
                        incidentObject.add("location", address);
                        incidentObject.add("callType", callType);
                        incidentObject.add("isPriority", priorityCall);
                        incidentObject.add("initialText", message);
                        incidentObject.add("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
                        incidentObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                            }
                        });
                    } else if (!object.getString("callType").contains(callType)){
                        //Address Same, Call Type Different. Make New
                        IncidentObject incidentObject = new IncidentObject();
                        incidentObject.add("location", address);
                        incidentObject.add("callType", callType);
                        incidentObject.add("isPriority", priorityCall);
                        incidentObject.add("initialText", message);
                        incidentObject.add("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
                        incidentObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                            }
                        });
                    }
                } else if (object.getString("initialText").equals(message)){
                    //Incident NOT Active. Text Received After Call Possibly. Do NOTHING...
                } else {
                    //NEW INCIDENT. CREATE OBJECT AND NOTIFY OTHERS WHO HAVEN'T RECEIVED TEXT.
                    IncidentObject incidentObject = new IncidentObject();
                    incidentObject.add("location", address);
                    incidentObject.add("callType", callType);
                    incidentObject.add("isPriority", priorityCall);
                    incidentObject.add("initialText", message);
                    incidentObject.add("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
                    incidentObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                }
            }
        });*/
    }

    public void addMessage(final String message) {
        /*ParseQuery<IncidentObject> query = ParseQuery.getQuery(IncidentObject.class);
        query.whereEqualTo("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
        query.getFirstInBackground(new GetCallback<IncidentObject>() {
            @Override
            public void done(IncidentObject object, ParseException e) {
                if (object.getBoolean("isActive")) {
                    JSONArray jsonArray = object.getMessages();
                    jsonArray.put(message);
                    object.setMessages(jsonArray);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                }
            }
        });
    }*/
    }
}
