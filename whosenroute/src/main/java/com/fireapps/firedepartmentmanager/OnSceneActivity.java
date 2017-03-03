package com.fireapps.firedepartmentmanager;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OnSceneActivity extends AppCompatActivity {

    boolean emsEnabled = false, MVAEnabled = false, SFEnabled = false, otherEnabled = false;
    private ColorStateList unSelectedTint;

    @BindView(R.id.onScene_EMS_Card)CardView emsCard;
    @BindView(R.id.onScene_MVA_Card)CardView MVACard;
    @BindView(R.id.onScene_SF_Card)CardView SFCard;
    @BindView(R.id.onScene_OTHER_Card)CardView OTHERCard;

    @BindView(R.id.onScene_MVA_Icon)ImageView onScene_MVA_Icon;
    @BindView(R.id.onScene_SF_Icon)ImageView onScene_SF_Icon;
    @BindView(R.id.onScene_OTHER_Icon)ImageView onScene_OTHER_Icon;
    @BindView(R.id.onScene_EMS_Icon)ImageView onScene_EMS_Icon;

    @BindView(R.id.toolbar)Toolbar toolbar;

    //Checkboxes
    @BindView(R.id.onScene_EMS_ALS)CheckBox EMSCheckALS;
    @BindView(R.id.onScene_EMS_SignOff)CheckBox EMSCheckSignOff;
    @BindView(R.id.onScene_EMS_NoTransport)CheckBox EMSCheckNoTransport;

    @BindView(R.id.onScene_MVA_Entrapment)CheckBox MVACheckEntrapment;
    @BindView(R.id.onScene_MVA_Injuries)CheckBox MVACheckInjuries;
    @BindView(R.id.onScene_MVA_Fire)CheckBox MVACheckFire;
    @BindView(R.id.onScene_MVA_NoTrap)CheckBox MVACheckNoEntrapment;
    @BindView(R.id.onScene_MVA_NoInj)CheckBox MVACheckNoInjuries;
    @BindView(R.id.onScene_MVA_Traffic)CheckBox MVACheckTrafficControl;

    @BindView(R.id.onScene_SF_FireShowing)CheckBox SFCheckFire;
    @BindView(R.id.onScene_SF_Smoke)CheckBox SFCheckSmoke;
    @BindView(R.id.onScene_SF_Trap)CheckBox SFCheckEntrapment;
    @BindView(R.id.onScene_SF_NoShow)CheckBox SFCheckNothingShowing;
    @BindView(R.id.onScene_SF_Out)CheckBox SFCheckAllOut;

    @BindView(R.id.onScene_OTHER_Tree)CheckBox OTHERCheckTree;
    @BindView(R.id.onScene_OTHER_Wires)CheckBox OTHERCheckWires;
    @BindView(R.id.onScene_OTHER_LiveWire)CheckBox OTHERCheckLiveWires;
    @BindView(R.id.onScene_OTHER_Traffic)CheckBox OTHERCheckTraffic;

    @BindView(R.id.onScene_SAFETY_Caution)CheckBox SAFETYCheckCaution;
    @BindView(R.id.onScene_SAFETY_Manpower)CheckBox SAFETYCheckManpower;
    @BindView(R.id.onScene_SAFETY_TapAgain)CheckBox SAFETYCheckTapRequest;
    @BindView(R.id.onScene_SAFETY_KeepAway)CheckBox SAFETYCheckKeepAway;
    @BindView(R.id.onScene_SAFETY_Station)CheckBox SAFETYCheckStation;
    @BindView(R.id.onScene_SAFETY_Other)EditText SAFETYCheckOtherInfo;

    @BindView(R.id.onScene_NotifyDispatch)CheckBox notifyDispatchCheck;
    @BindView(R.id.onScene_DoneButton)Button buttonDone;

    private DatabaseReference mDatabase;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_scene);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        unSelectedTint = onScene_EMS_Icon.getImageTintList();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOnSceneUpdate(notifyDispatchCheck.isChecked());
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.onscene_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.onscene_menu_notifyHelp:
                new MaterialDialog.Builder(this)
                        .title("Notify Dispatch Immediately")
                        .content("This will immediately notify your dispatch center with your location and any options selected below. \n" +
                                "\n" +
                                "This should only be used in an emergency.")
                        .positiveText("NOTIFY")
                        .negativeText("CANCEL")
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSceneEMSButton (View view) {
        if (emsEnabled) {
            onScene_EMS_Icon.setImageTintList(unSelectedTint);
            emsEnabled = false;
            emsCard.setVisibility(View.GONE);
        } else {
            emsEnabled = true;
            onScene_EMS_Icon.setImageTintList(AppCompatResources.getColorStateList(this, R.color.md_blue_500));
            emsCard.setVisibility(View.VISIBLE);
        }
    }

    public void onSceneMVAButton (View view) {
        if (MVAEnabled) {
            onScene_MVA_Icon.setImageTintList(unSelectedTint);
            MVAEnabled = false;
            MVACard.setVisibility(View.GONE);
        } else {
            MVAEnabled = true;
            onScene_MVA_Icon.setImageTintList(AppCompatResources.getColorStateList(this, R.color.md_orange_500));
            MVACard.setVisibility(View.VISIBLE);
        }
    }

    public void onSceneSTButton (View view) {
        if (SFEnabled) {
            onScene_SF_Icon.setImageTintList(unSelectedTint);
            SFEnabled = false;
            SFCard.setVisibility(View.GONE);
        } else {
            SFEnabled = true;
            onScene_SF_Icon.setImageTintList(AppCompatResources.getColorStateList(this, R.color.md_red_500));
            SFCard.setVisibility(View.VISIBLE);
        }
    }

    public void onSceneOTHERButton (View view) {
        if (otherEnabled) {
            onScene_OTHER_Icon.setImageTintList(unSelectedTint);
            otherEnabled = false;
            OTHERCard.setVisibility(View.GONE);
        } else {
            otherEnabled = true;
            onScene_OTHER_Icon.setImageTintList(AppCompatResources.getColorStateList(this, R.color.md_teal_500));
            OTHERCard.setVisibility(View.VISIBLE);
        }
    }

    public void saveOnSceneUpdate(boolean notifyDispatch) {
        ArrayList<String> EMSValues = new ArrayList<String>();
        ArrayList<String> MVAValues = new ArrayList<String>();
        ArrayList<String> SFValues = new ArrayList<String>();
        ArrayList<String> OTHERValues = new ArrayList<String>();
        ArrayList<String> SAFETYValues = new ArrayList<String>();

        if (EMSCheckALS.isChecked()) {
            EMSValues.add("ALS NEEDED");
        }

        if (EMSCheckSignOff.isChecked()) {
            EMSValues.add("POSSIBLE SIGN OFF");
        }

        if (EMSCheckNoTransport.isChecked()) {
            EMSValues.add("NO TRANSPORT NEEDED");
        }
        //
        if (MVACheckEntrapment.isChecked()) {
            MVAValues.add("ENTRAPMENT");
        }

        if (MVACheckInjuries.isChecked()) {
            MVAValues.add("INJURIES");
        }

        if (MVACheckFire.isChecked()) {
            MVAValues.add("FIRE");
        }
        if (MVACheckNoEntrapment.isChecked()) {
            MVAValues.add("NO ENTRAPMENT");
        }

        if (MVACheckNoInjuries.isChecked()) {
            MVAValues.add("NO INJURIES");
        }

        if (MVACheckTrafficControl.isChecked()) {
            MVAValues.add("TRAFFIC CONTROL");
        }
        //
        if (SFCheckFire.isChecked()) {
            SFValues.add("FIRE SHOWING");
        }

        if (SFCheckSmoke.isChecked()) {
            SFValues.add("SMOKE SHOWING");
        }

        if (SFCheckEntrapment.isChecked()) {
            SFValues.add("ENTRAPMENT");
        }
        if (SFCheckNothingShowing.isChecked()) {
            SFValues.add("NOTHING SHOWING");
        }

        if (SFCheckAllOut.isChecked()) {
            SFValues.add("EVERYONE OUT");
        }
        //
        if (OTHERCheckTree.isChecked()) {
            OTHERValues.add("TREE DOWN");
        }

        if (OTHERCheckWires.isChecked()) {
            OTHERValues.add("WIRES DOWN");
        }

        if (OTHERCheckLiveWires.isChecked()) {
            OTHERValues.add("LIVE WIRES DOWN");
        }
        if (OTHERCheckTraffic.isChecked()) {
            OTHERValues.add("TRAFFIC CONTROL");
        }
        //
        if (SAFETYCheckCaution.isChecked()) {
            SAFETYValues.add("USE CAUTION");
        }
        if (SAFETYCheckManpower.isChecked()) {
            SAFETYValues.add("MORE MANPOWER");
        }
        if (SAFETYCheckTapRequest.isChecked()) {
            SAFETYValues.add("TAP REQUEST");
        }
        if (SAFETYCheckKeepAway.isChecked()) {
            SAFETYValues.add("KEEP AWAY");
        }
        if (SAFETYCheckStation.isChecked()) {
            SAFETYValues.add("ALL TO STATION");
        }

        String EMSCSV = null, MVACSV = null, SFCSV = null, OTHERCSV = null, SAFETYCSV = null;

        if (EMSValues.size() != 0) {
            EMSCSV = TextUtils.join(", ", EMSValues);
        }
        if (MVAValues.size() != 0) {
            MVACSV = TextUtils.join(", ", MVAValues);
        }
        if (SFValues.size() != 0) {
            SFCSV = TextUtils.join(", ", SFValues);
        }
        if (OTHERValues.size() != 0) {
            OTHERCSV = TextUtils.join(", ", OTHERValues);
        }
        if (SAFETYValues.size() != 0) {
            SAFETYCSV = TextUtils.join(", ", SAFETYValues);
        }

        String key = mDatabase.child("logs").child(currentUser.getUid()).child("on_scene").push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/logs/" + currentUser.getUid() + "/on_scene/" + key + "/timestamp", ServerValue.TIMESTAMP);
        childUpdates.put("/logs/" + currentUser.getUid() + "/on_scene/" + key + "/notify_dispatch", notifyDispatch);
        childUpdates.put("/logs/" + currentUser.getUid() + "/on_scene/" + key + "/updates/EMS", EMSCSV);
        childUpdates.put("/logs/" + currentUser.getUid() + "/on_scene/" + key + "/updates/MVA", MVACSV);
        childUpdates.put("/logs/" + currentUser.getUid() + "/on_scene/" + key + "/updates/STRUCTF", SFCSV);
        childUpdates.put("/logs/" + currentUser.getUid() + "/on_scene/" + key + "/updates/OTHER", OTHERCSV);
        childUpdates.put("/logs/" + currentUser.getUid() + "/on_scene/" + key + "/updates/SAFETY", SAFETYCSV);

        childUpdates.put("/users/" + currentUser.getUid() + "/on_scene/timestamp", ServerValue.TIMESTAMP);
        childUpdates.put("/users/" + currentUser.getUid() + "/on_scene/EMS", EMSCSV);
        childUpdates.put("/users/" + currentUser.getUid() + "/on_scene/MVA", MVACSV);
        childUpdates.put("/users/" + currentUser.getUid() + "/on_scene/STRUCTF", SFCSV);
        childUpdates.put("/users/" + currentUser.getUid() + "/on_scene/OTHER", OTHERCSV);
        childUpdates.put("/users/" + currentUser.getUid() + "/on_scene/SAFETY", SAFETYCSV);
        childUpdates.put("/users/" + currentUser.getUid() + "/on_scene/notify_dispatch", notifyDispatch);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(OnSceneActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
