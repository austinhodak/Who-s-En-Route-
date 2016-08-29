package com.fireapps.firedepartmentmanager;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;

/**
 * Created by austinhodak on 6/15/16.
 */

public class Application extends MultiDexApplication {

    private static Application instance;

    public static Application getInstance() {
        return instance;
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    private Member currentMember;

    @Override
    public void onCreate() {
        super.onCreate();


        instance = this;

        //getRespondingSystem();

        //LeakCanary.install(this);
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable throwable) {
//                Toast.makeText(Application.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                try {
                    //Glide.with(imageView.getContext()).load(uri).fitCenter().placeholder(placeholder).into(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }
        });

        ConnectionBuddyConfiguration networkInspectorConfig = new ConnectionBuddyConfiguration.Builder(this).notifyOnlyReliableEvents(true).build();
        ConnectionBuddy.getInstance().init(networkInspectorConfig);
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Member currentMember) {
        this.currentMember = currentMember;
    }
}
