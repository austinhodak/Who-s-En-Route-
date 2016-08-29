package com.fireapps.firedepartmentmanager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by austinhodak on 7/13/16.
 */

public class ClusterItem implements com.google.maps.android.clustering.ClusterItem {

    private final LatLng mPosition;

    public ClusterItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
