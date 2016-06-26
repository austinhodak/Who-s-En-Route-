package com.fireapps.firedepartmentmanager;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by austinhodak on 6/16/16.
 */
@IgnoreExtraProperties
public class Incident {
    boolean isActive;
    String date, type, department;

    public Incident() {
    }

    public boolean getIsActive() {
        return isActive;
    }

    public String getDate() {
        return date;
    }

    public String getDepartment() {
        return department;
    }

    public String getType() {
        return type;
    }
}
