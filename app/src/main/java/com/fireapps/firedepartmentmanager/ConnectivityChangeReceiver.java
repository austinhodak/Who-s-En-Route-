package com.fireapps.firedepartmentmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by austinhodak on 8/24/15.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {


    private SharedPreferences sharedPreferences;
    private DatabaseReference firebase;
    private DatabaseReference userFirebase;

    @Override
    public void onReceive(Context context, Intent intent) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        firebase = database.getReference("users");

        userFirebase = firebase.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String deptWifi = sharedPref.getString(SettingsActivityFragment.KEY_PREF_WIFI, "GarlandVFD");

        try {
            if (getCurrentSSID(context).contains(deptWifi)) {
                Log.i("FDM", "AT STATION");

                if (sharedPreferences.getString("respondingTo", "").equalsIgnoreCase("station")) {
                    Map<String, Object> respondingTo = new HashMap<String, Object>();
                    respondingTo.put("respondingTo", "At Station");
                    userFirebase.updateChildren(respondingTo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentSSID(Context context) {
        String ssid = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
                ssid = wifiInfo.getSSID();
            }
        }
        return ssid;
    }
}
