package com.fireapps.firedepartmentmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by austinhodak on 6/21/16.
 */

public class AvailablityFragment extends Fragment {

    Switch mainSwitch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_availability, container, false);
        getActivity().setTitle("Availability");

        setHasOptionsMenu(true);

        mainSwitch = (Switch) view.findViewById(R.id.main_switch);
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mainSwitch.setText("Available");
                } else {
                    mainSwitch.setText("Not Available");
                }
            }
        });
        return view;
    }
}
