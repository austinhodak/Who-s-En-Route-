package com.fireapps.firedepartmentmanager;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by austinhodak on 6/16/16.
 */
@IgnoreExtraProperties
public class Apparatus {
    boolean inService, isAlert;
    String apparatusName, apparatusAbrv, type;

    public Apparatus() {
    }

    public String getApparatusAbrv() {
        return apparatusAbrv;
    }

    public String getApparatusName() {
        return apparatusName;
    }

    public boolean isInService() {
        return inService;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public String getType() {
        return type;
    }
}
