package com.fireapps.firedepartmentmanager;

import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by austinhodak on 6/15/16.
 */

public class Application extends android.app.Application {

    private Member currentMember;

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override public void onCreate() {
        super.onCreate();
        //LeakCanary.install(this);
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Member currentMember) {
        this.currentMember = currentMember;
    }
}
