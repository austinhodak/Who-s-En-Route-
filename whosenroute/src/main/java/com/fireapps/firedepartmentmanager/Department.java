package com.fireapps.firedepartmentmanager;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by austinhodak on 6/16/16.
 */
@IgnoreExtraProperties
public class Department {
    boolean activeIncident;

    public Department() {
    }

    public boolean isActiveIncident() {
        return activeIncident;
    }
}
