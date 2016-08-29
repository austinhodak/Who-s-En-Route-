package com.fireapps.firedepartmentmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by austinhodak on 6/28/16.
 */

public class WConstantLocationService extends Service {

    public WConstantLocationService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
