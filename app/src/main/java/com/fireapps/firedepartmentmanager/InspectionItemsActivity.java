package com.fireapps.firedepartmentmanager;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import butterknife.Bind;
import butterknife.ButterKnife;

public class InspectionItemsActivity extends AppCompatActivity {

    @Bind(R.id.toolbar_main) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        Bundle bundle = new Bundle();
        bundle.putString("categoryID", getIntent().getStringExtra("categoryID"));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
       // InspectionItemsFragment fragment = new InspectionItemsFragment();
        //fragment.setArguments(bundle);

        //fragmentTransaction.add(R.id.container, fragment);
        //fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inspection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
