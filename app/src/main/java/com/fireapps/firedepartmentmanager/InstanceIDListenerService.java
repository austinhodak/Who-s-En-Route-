package com.fireapps.firedepartmentmanager;

import android.content.Intent;

/**
 * Created by austinhodak on 4/21/16.
 */
public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {



    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
