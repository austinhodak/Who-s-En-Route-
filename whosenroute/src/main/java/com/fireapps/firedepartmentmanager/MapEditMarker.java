package com.fireapps.firedepartmentmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapEditMarker extends AppCompatActivity {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Button typeSpinner;
    private TextView coords;
    private MaterialEditText locationField, nameField;
    String TAG = "Marker Add: ";
    String[] types = {"NONE", "HYDRANT", "LANDING ZONE", "HAZARD"};
    String[] icons = {"", "hydrant", "helipad", ""};
    private String selectedType;
    private String selectedIcon;
    private FloatingActionButton doneFab;

    private CardView detailsCard;
    private ImageView detailsImage;
    private TextView detailsTitle;

    private LinearLayout hydrantDetails;

    //Landing Zone
    private MaterialEditText otherField;
    private FirebaseDatabase firebaseDatabase;
    private SharedPreferences sharedPreferences;
    private String departmentID;
    private MapMarker selectedMarker;

    private Button hydrantFlow, hydrantOutlets, hydrantDirection, hydrantType;
    private MaterialEditText hydrantSizes;

    private ImageView markerOverlay;
    private MaterialSimpleListAdapter hydrantFlowAdapter;

    private Markers markers = new Markers();

    private ProgressBar progressBar;

    int selectedHydrantType, selectedHydrantOutlets;
    List<Address> matches = new ArrayList<>();
    private ImageView header;

    private FloatingActionButton markeradd_agency;

    ArrayList<String> departmentNames = new ArrayList<>();
    ArrayList<String> departmentKeys = new ArrayList<>();
    private int selectedDepartment = -1;
    private Bitmap resized;
    private String markerID;
    private double markerLat, markerLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_edit_marker);

        Intent intent = getIntent();
        markerID = intent.getStringExtra("markerID");

        progressBar = (ProgressBar) findViewById(R.id.new_marker_progress);
        detailsCard = (CardView) findViewById(R.id.details_card);
        detailsImage = (ImageView) findViewById(R.id.markeradd_details_icon);
        detailsTitle = (TextView) findViewById(R.id.markeradd_details_title);
        hydrantDetails = (LinearLayout) findViewById(R.id.markeradd_details_hydrantl1);

        coords = (TextView) findViewById(R.id.markeradd_coords);
        locationField = (MaterialEditText) findViewById(R.id.markeradd_location_field);
        nameField = (MaterialEditText) findViewById(R.id.markeradd_name_field);
        doneFab = (FloatingActionButton) findViewById(R.id.markeradd_done);
        //
        otherField = (MaterialEditText) findViewById(R.id.markeradd_other_field);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        departmentID = sharedPreferences.getString("departmentID", null);



        firebaseDatabase = FirebaseDatabase.getInstance();
        markerOverlay = (ImageView) findViewById(R.id.new_marker_overlay);

        loadDepartments();

//        markeradd_agency = (FloatingActionButton) findViewById(R.id.markeradd_agency);
//        Bitmap bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.map_firestation)).getBitmap();
//        resized = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
//        markeradd_agency.setImageBitmap(resized);
//        markeradd_agency.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new MaterialDialog.Builder(MapEditMarker.this)
//                        .title("Select Agency")
//                        .items(departmentNames)
//                        .itemsCallbackSingleChoice(selectedDepartment, new MaterialDialog.ListCallbackSingleChoice() {
//                            @Override
//                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                                selectedDepartment = which;
//
//                                return true;
//                            }
//                        }).show();
//            }
//        });

        hydrantFlow = (Button) findViewById(R.id.markeradd_hydrant_flow);
        hydrantOutlets = (Button) findViewById(R.id.markeradd_hydrant_outlets);
        hydrantType = (Button) findViewById(R.id.markeradd_hydrant_type);
        hydrantSizes = (MaterialEditText) findViewById(R.id.markeradd_hydrant_size);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Edit Marker");

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(this);
        hydrantFlowAdapter = new MaterialSimpleListAdapter(this);
        loadHydrantOptions();

        //final List<MaterialSimpleListItem> mItems = new ArrayList<>();

        for (final MapMarker mapMarker : markers.getMarkers()) {
            adapter.add(new MaterialSimpleListItem.Builder(MapEditMarker.this)
                    .content(mapMarker.getName())
                    .icon(mapMarker.getMainIcon())
                    .backgroundColor(Color.WHITE)
                    .build());
        }

        adapter.sort(new Comparator<MaterialSimpleListItem>() {
            @Override
            public int compare(MaterialSimpleListItem t1, MaterialSimpleListItem t2) {
                return t1.getContent().toString().compareTo(t2.getContent().toString());
            }
        });

        typeSpinner = (Button) findViewById(R.id.spinner);
        typeSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MapEditMarker.this)
                        .title("Marker Type")
                        .adapter(adapter, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                MaterialSimpleListItem item = adapter.getItem(which);
                                selectedMarker = markers.getMarkerForValue(item.getContent().toString());
                                selectedType = selectedMarker.getName();
                                typeSpinner.setText(selectedMarker.getName());
                                showDetailsCard();
                                dialog.dismiss();

                                resetFields();
                            }
                        })
                        .theme(Theme.LIGHT).show();
            }
        });

        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {

                    Map<String, Object> alertValues = new HashMap<>();

                    alertValues.put("location", locationField.getText().toString());
                    alertValues.put("name", nameField.getText().toString());
                    alertValues.put("type", selectedType);
                    alertValues.put("other", otherField.getText().toString());
                    alertValues.put("updateBy", "Admin");

                    firebaseDatabase.getReference("markers").child(markerID).updateChildren(alertValues);

                    if (selectedMarker.equals(Markers.hydrant)) {
                        saveHydrantValues(markerID);
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            }
        });

        doneFab.hide(false);

        loadMarkerInfo();
    }

    public void loadMarkerInfo() {

        firebaseDatabase.getReference("markers").child(markerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                markerLat = (double) dataSnapshot.child("lat").getValue();
                markerLon = (double) dataSnapshot.child("lon").getValue();
                coords.setText(String.valueOf(markerLat).substring(0, 9) + ", " + String.valueOf(markerLon).substring(0, 10));

                nameField.setText(dataSnapshot.child("name").getValue().toString());
                locationField.setText(dataSnapshot.child("location").getValue().toString());


                selectedType = dataSnapshot.child("type").getValue().toString();
                selectedMarker = markers.getMarkerForValue(selectedType);
                typeSpinner.setText(dataSnapshot.child("type").getValue().toString());

                if (dataSnapshot.child("type").getValue().toString().equals("Hydrant")) {
                    hydrantType.setText(dataSnapshot.child("hydrant").child("type").getValue().toString());
                    hydrantFlow.setText(dataSnapshot.child("hydrant").child("flow").getValue().toString());
                    hydrantOutlets.setText(dataSnapshot.child("hydrant").child("outlets").getValue().toString());
                    hydrantSizes.setText(dataSnapshot.child("hydrant").child("sizes").getValue().toString());
                    showDetailsCard();
                } else {
                    showDetailsCard();
                }

                otherField.setText(dataSnapshot.child("other").getValue().toString());

                doneFab.show(true);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private void saveHydrantValues(String key) {
        Map<String, Object> hydrantValues = new HashMap<>();

        hydrantValues.put("type", hydrantType.getText().toString());
        hydrantValues.put("flow", hydrantFlow.getText().toString());
        hydrantValues.put("outlets", hydrantOutlets.getText().toString());
        hydrantValues.put("sizes", hydrantSizes.getText().toString());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/markers/" + key + "/hydrant/", hydrantValues);
        firebaseDatabase.getReference().updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void loadHydrantOptions() {
        hydrantFlowAdapter.add(new MaterialSimpleListItem.Builder(this)
                .content(Markers.hydrant500.getName())
                .icon(Markers.hydrant500.getMainIcon())
                .backgroundColor(Color.WHITE)
                .build());

        hydrantFlowAdapter.add(new MaterialSimpleListItem.Builder(this)
                .content(Markers.hydrant999.getName())
                .icon(Markers.hydrant999.getMainIcon())
                .backgroundColor(Color.WHITE)
                .build());

        hydrantFlowAdapter.add(new MaterialSimpleListItem.Builder(this)
                .content(Markers.hydrant1499.getName())
                .icon(Markers.hydrant1499.getMainIcon())
                .backgroundColor(Color.WHITE)
                .build());

        hydrantFlowAdapter.add(new MaterialSimpleListItem.Builder(this)
                .content(Markers.hydrant1500.getName())
                .icon(Markers.hydrant1500.getMainIcon())
                .backgroundColor(Color.WHITE)
                .build());

        hydrantFlowAdapter.add(new MaterialSimpleListItem.Builder(this)
                .content(Markers.hydrantunknown.getName())
                .icon(Markers.hydrantunknown.getMainIcon())
                .backgroundColor(Color.WHITE)
                .build());

        hydrantFlowAdapter.notifyDataSetChanged();
        hydrantFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MapEditMarker.this)
                        .title("Flow Rate")
                        .adapter(hydrantFlowAdapter, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                MaterialSimpleListItem item = hydrantFlowAdapter.getItem(which);
                                MapMarker marker = markers.getHydrantMarkerForValue(item.getContent().toString());
                                hydrantFlow.setText(item.getContent());
                                detailsImage.setImageResource(marker.getMainIcon());
                                dialog.dismiss();
                            }
                        })
                        .theme(Theme.LIGHT).show();
            }
        });

        final String[] hydrant_outlets = {"1", "2", "3", "4", "4+"};
        hydrantOutlets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MapEditMarker.this).title("Outlets")
                        .items(hydrant_outlets)
                        .itemsCallbackSingleChoice(selectedHydrantOutlets, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                selectedHydrantOutlets = which;
                                hydrantOutlets.setText(text.toString() + " Outlets");
                                return false;
                            }
                        }).show();
            }
        });

        final int[] selectedType = {-1};
        final String[] hydrant_types = {"Hydrant", "Dry Hydrant", "Lake/Pond", "Seasonal Lake/Pond", "Pool", "Other"};
        hydrantType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MapEditMarker.this).title("Hydrant Type")
                        .items(hydrant_types)
                        .itemsCallbackSingleChoice(selectedHydrantType, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                selectedHydrantType = which;
                                hydrantType.setText(text.toString());
                                return false;
                            }
                        }).show();
            }
        });
    }

    private void showDetailsCard() {
        detailsCard.setVisibility(View.VISIBLE);
        detailsTitle.setText(selectedMarker.getName());
        detailsImage.setImageResource(selectedMarker.getMainIcon());

        if (selectedMarker.equals(Markers.hydrant)) {
            hydrantDetails.setVisibility(View.VISIBLE);
        } else {
            hydrantDetails.setVisibility(View.GONE);
        }
    }

    private boolean validateFields() {
        if (nameField.getText().toString().isEmpty()) {
            nameField.setError("Name cannot be empty.");
            return false;
        }
        if (locationField.getText().toString().isEmpty()) {
            locationField.setError("Location cannot be empty.");
            return false;
        }

        if (selectedMarker != null)
        if (selectedMarker.equals(Markers.hydrant)) {
            if (hydrantType.getText().toString().equals("Hydrant Type")) {
                return false;
            }
            if (hydrantFlow.getText().toString().equals("Flow Rate")) {
                return false;
            }
            if (hydrantOutlets.getText().toString().equals("Outlets")) {
                return false;
            }
            if (hydrantSizes.getText().toString().isEmpty()) {
                return false;
            }
        }
        return selectedType != "Select Marker Type";
    }

    private void resetFields() {
        selectedHydrantOutlets = -1;
        selectedHydrantType = -1;

        hydrantType.setText("Hydrant Type");
        hydrantFlow.setText("Flow Rate");
        hydrantOutlets.setText("Outlets");
        hydrantSizes.setText("");
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

    private void loadDepartments() {
        firebaseDatabase.getReference().child("departments").child(departmentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebaseDatabase.getReference().child("counties").child((String)dataSnapshot.child("county").getValue()).child("departments").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        firebaseDatabase.getReference().child("departments").child(dataSnapshot.getKey()).orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Add to dialog.
                                departmentNames.add(dataSnapshot.child("name").getValue().toString());
                                departmentKeys.add(dataSnapshot.getKey());
                                Log.d(TAG, "onDataChange: " + dataSnapshot.child("name").getValue());

                                try {
                                    selectedDepartment = departmentKeys.indexOf(departmentID);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
