package com.fireapps.firedepartmentmanager;

import android.support.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by austinhodak on 7/12/16.
 */

public class MapMarker {
    public HashMap<String, String> icons = new HashMap<>();
    public int mainIcon;
    public String name;

    public MapMarker(@Nullable HashMap<String, String> icons, String name, int mainIcon) {
        this.icons = icons;
        this.name = name;
        this.mainIcon = mainIcon;
    }

    public int getMainIcon() {
        return mainIcon;
    }

    public String getName() {
        return name;
    }
}
