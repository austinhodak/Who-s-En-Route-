package com.fireapps.firedepartmentmanager;

import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class Permissions {

    private boolean mapMarkerEdit;
    private boolean memberAdd, memberDelete, memberEdit;
    private boolean apparatusAdd, apparatusDelete, apparatusEdit;

    public Permissions() {
    }

    public boolean isApparatusAdd() {
        return apparatusAdd;
    }

    public boolean isApparatusDelete() {
        return apparatusDelete;
    }

    public boolean isApparatusEdit() {
        return apparatusEdit;
    }

    public boolean isMapMarkerEdit() {
        return mapMarkerEdit;
    }

    public boolean isMemberAdd() {
        return memberAdd;
    }

    public boolean isMemberDelete() {
        return memberDelete;
    }

    public boolean isMemberEdit() {
        return memberEdit;
    }
}
