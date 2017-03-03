package com.fireapps.firedepartmentmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by austinhodak on 8/7/16.
 */

public class SlideFirst extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slide_first, container, false);

        ImageView imageView = (ImageView)view.findViewById(R.id.sun);
        Animation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(5000);

        imageView.startAnimation(animation);

        TextView desc = (TextView)view.findViewById(R.id.mi_description);
        String ds = getString(R.string.who_s_en_route) + "\n\nv" + getString(R.string.versionName);
        desc.setText(ds);

        return view;
    }
}
