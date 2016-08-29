package com.fireapps.firedepartmentmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

/**
 * Created by austinhodak on 7/21/16.
 */

public abstract class AppPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.md_blue_grey_900));
    }
}
