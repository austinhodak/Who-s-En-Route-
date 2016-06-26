package com.fireapps.firedepartmentmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Austin on 3/31/2015.
 */
public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.CustomViewHolder> {
    private List<MemberObject> feedItemList;
    private Context mContext;

    View view;

    public MemberListAdapter(Context context, List<MemberObject> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_memberslist, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final MemberObject feedItem = feedItemList.get(i);

        //Setting text view title
        customViewHolder.name.setText(feedItem.getName());
        customViewHolder.position.setText(feedItem.getPosition());


        customViewHolder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wrapInScrollView = true;
                MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .title("Edit Member")
                        .customView(R.layout.dialog_member_add, wrapInScrollView)
                        .positiveText("Add")
                        .negativeText("Cancel")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                /*Firebase alanRef = usersRef.child();
                                Map<String, Object> nickname = new HashMap<String, Object>();
                                nickname.put("nickname", "Alan The Machine");
                                alanRef.updateChildren(nickname);*/
                            }
                        })
                        .show();

                View view = dialog.getCustomView();
                MaterialEditText name, email, password, position;
                name = (MaterialEditText) view.findViewById(R.id.member_dialog_name);
                email = (MaterialEditText) view.findViewById(R.id.member_dialog_email);
                password = (MaterialEditText) view.findViewById(R.id.member_dialog_password);
                position = (MaterialEditText) view.findViewById(R.id.member_dialog_position);

                password.setVisibility(View.GONE);

                name.setText(feedItem.getName());
                email.setText(feedItem.getEmail());
                position.setText(feedItem.getPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, respondingTo, position;
        protected Button editButton;

        public CustomViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.memberList_name);
            this.position = (TextView) view.findViewById(R.id.memberList_pos);
            this.editButton = (Button) view.findViewById(R.id.memberList_edit);
        }
    }
}