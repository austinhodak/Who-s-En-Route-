package com.fireapps.firedepartmentmanager;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentListFragment extends Fragment {

    @Bind(R.id.listView)ListView list;

    String apparatusID;

   // IncidentObject incidentEntity;

    //private Adapter adapter;

    public IncidentListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incidentlist, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        loadIncidents();

        //adapter = new Adapter(getActivity(), new ArrayList<IncidentObject>());
        //list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*CategoryObject object = adapter.getItem(position);
                Intent intent = new Intent(getActivity(), InspectionItemsActivity.class);
                intent.putExtra("categoryID", object.getObjectId());
                startActivity(intent);*/
            }
        });

        return view;
    }

    private void loadIncidents() {
        /*ParseQuery<IncidentObject> query = ParseQuery.getQuery(IncidentObject.class);
        query.whereEqualTo("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
        query.setLimit(10);
        query.findInBackground(new FindCallback<IncidentObject>() {
            @Override
            public void done(List<IncidentObject> objects, ParseException e) {
                if (e==null) {
                    adapter.addAll(objects);
                }
            }
        });*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_categories, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_category:
                //New Category
                /*CategoryObject category = new CategoryObject();
                category.setDisplayName("Lights");
                category.put("apparatus", apparatusEntity);
                category.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        loadIncidents();
                    }
                });*/

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


/*
    public static class Adapter extends ArrayAdapter<IncidentObject> {
        private Context mContext;
        private List<IncidentObject> mList;
        TextView name, address;

        public Adapter(Context context, List<IncidentObject> objects) {
            super(context, R.layout.listitem_incident, objects);
            this.mContext = context;
            this.mList = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                convertView = layoutInflater.inflate(R.layout.listitem_incident, null);
            }

            IncidentObject category = mList.get(position);

            name = (TextView)convertView.findViewById(R.id.listItem_Incident_Type);
            address = (TextView)convertView.findViewById(R.id.listItem_Incident_Address);

            //name.setText(category.get("typeOfAlarm").toString());
            //address.setText(category.get("address").toString());

            return convertView;
        }
    }
*/

}
