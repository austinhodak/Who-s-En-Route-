package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Austin on 3/31/2015.
 */
public class RespondingListAdapter extends ArrayAdapter<MemberObjectFire> {
    TextView name, date, resTo;
    private Context mContext;
    private List<MemberObjectFire> mList;

    public RespondingListAdapter(Context context, List<MemberObjectFire> objects) {
        super(context, R.layout.listitem_respondingmember_small, objects);
        this.mContext = context;
        this.mList = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.listitem_respondingmember_small, null);
        }

        MemberObjectFire memberObject = mList.get(position);

        name = (TextView) convertView.findViewById(R.id.member_list_name);
        date = (TextView) convertView.findViewById(R.id.member_list_title);
        resTo = (TextView) convertView.findViewById(R.id.res_list_to);

        name.setText(memberObject.getName());
        resTo.setText(memberObject.getRespondingTo());
        date.setText(memberObject.getPosition());

        //TODO UPDATE
        /*Date respondingTime = memberObject.getDate("respondingTime");

        long diff = 0;
        if (respondingTime != null) {
            diff = new Date().getTime() - respondingTime.getTime();
            long time = diff / (1000*60);

            date.setText(String.valueOf(time) + " Minutes Ago");
        } else {
            date.setVisibility(View.GONE);
        }*/

        if (memberObject.getRespondingTo() != null) {
            if (memberObject.getRespondingTo().equals("Scene")) {
                resTo.setBackground(mContext.getResources().getDrawable(R.drawable.chip_scene));
            }
            if (memberObject.getRespondingTo().equals("NR")) {
                resTo.setBackground(mContext.getResources().getDrawable(R.drawable.chip_nr));
            }
            if (memberObject.getRespondingTo().equals("Station")) {
                resTo.setBackground(mContext.getResources().getDrawable(R.drawable.chip_station));
            }
        }

        return convertView;
    }
}

