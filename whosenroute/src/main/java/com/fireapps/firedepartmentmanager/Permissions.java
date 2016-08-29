package com.fireapps.firedepartmentmanager;

import android.content.Context;

/**
 * Created by austinhodak on 8/8/16.
 */

public class Permissions {

    private static Permissions instance;
    private final Context context;

    private boolean mapEditMarker;

    public synchronized static Permissions getInstance(Context context) {
        if (instance == null) {
            instance = new Permissions(context);
        }
        return instance;
    }

    public Permissions(Context context) {
        this.context = context;
    }

    public boolean canMapEditMarker() {
        return mapEditMarker;
    }
}
