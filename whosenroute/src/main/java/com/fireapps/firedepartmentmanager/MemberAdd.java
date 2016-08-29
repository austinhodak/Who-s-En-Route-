package com.fireapps.firedepartmentmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by austinhodak on 8/11/16.
 */

public class MemberAdd extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.memberAdd_FirstName)
    MaterialEditText mFirstNameField;

    @BindView(R.id.memberAdd_MiddleName)
    MaterialEditText mMiddleNameField;

    @BindView(R.id.memberAdd_LastName)
    MaterialEditText mLastNameField;

    @OnClick(R.id.member_dept_overflow)
    public void overflowClick(ImageView imageView) {
        PopupMenu popup = new PopupMenu(this, imageView);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.member_add_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_add_member_deptOverride:
                        new MaterialDialog.Builder(MemberAdd.this)
                                .title("Department Override")
                                .content("Only use this if you are told to do so by support.")
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .backgroundColorRes(R.color.bottom_sheet_background)
                                .input("Department ID", "", new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        // Do something
                                    }
                                }).show();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_add);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle("Add Member");

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
        return false;
    }
}
