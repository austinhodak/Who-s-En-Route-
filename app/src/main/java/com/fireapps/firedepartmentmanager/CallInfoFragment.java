package com.fireapps.firedepartmentmanager;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class CallInfoFragment extends Fragment {


    public CallInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call_info, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        return view;
    }

    private void updateCallInfo() {

    }
}
