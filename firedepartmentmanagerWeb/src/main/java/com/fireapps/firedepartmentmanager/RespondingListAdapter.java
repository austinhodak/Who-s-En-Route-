package com.fireapps.firedepartmentmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Austin on 3/31/2015.
 */
public class RespondingListAdapter extends RecyclerView.Adapter<RespondingListAdapter.CustomViewHolder> {
    private List<MemberObject> feedItemList;
    private Context mContext;

    View view;

    public RespondingListAdapter(Context context, List<MemberObject> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_respondingmember_small, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        MemberObject feedItem = feedItemList.get(i);

        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            Date date;

            date = dateFormat.parse(feedItem.getRespondingTime());

            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);

            customViewHolder.timeER.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();

            customViewHolder.timeER.setVisibility(View.GONE);
            customViewHolder.timeERtitle.setVisibility(View.GONE);
        }

        //Setting text view title
        customViewHolder.name.setText(feedItem.getName());
        customViewHolder.respondingTo.setText(feedItem.getRespondingTo());
        customViewHolder.position.setText(feedItem.getPosition());

        if (feedItem.getRespondingFromLoc() != null) {
            customViewHolder.respFrom.setText(feedItem.getRespondingFromLoc());
        } else {
            customViewHolder.respFrom.setVisibility(View.GONE);
        }

        if (feedItem.getRespondingTo() != null) {
            if (feedItem.getRespondingTo().equals("Scene")) {
                customViewHolder.respondingTo.setBackground(mContext.getResources().getDrawable(R.drawable.chip_scene));
            }
            if (feedItem.getRespondingTo().equals("NR")) {
                customViewHolder.respondingTo.setBackground(mContext.getResources().getDrawable(R.drawable.chip_nr));
                customViewHolder.respondingTo.setText("Can't Respond");
            }
            if (feedItem.getRespondingTo().equals("Station")) {
                customViewHolder.respondingTo.setBackground(mContext.getResources().getDrawable(R.drawable.chip_station));
            }
        }

        //pulse();
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, respondingTo, position, timeER, timeERtitle, respFrom;

        public CustomViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.member_list_name);
            this.respondingTo = (TextView) view.findViewById(R.id.res_list_to);
            this.position = (TextView) view.findViewById(R.id.member_list_title);
            this.timeER = (TextView) view.findViewById(R.id.res_list_timeER);
            this.timeERtitle = (TextView) view.findViewById(R.id.res_list_er);
            this.respFrom = (TextView) view.findViewById(R.id.res_list_loc);
        }
    }

    private void pulse(){
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(4);
        view.startAnimation(anim);
    }
}