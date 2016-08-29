package com.fireapps.firedepartmentmanager;

import java.util.ArrayList;

/**
 * Created by austinhodak on 7/12/16.
 */

public class Markers {

    public static final MapMarker road_closure = new MapMarker(null, "Road Closure", R.drawable.map_closure);
    public static final MapMarker landing_zone = new MapMarker(null, "Landing Zone", R.drawable.map_landingzone);
    public static final MapMarker aed = new MapMarker(null, "AED", R.drawable.map_aed);
    public static final MapMarker hospital = new MapMarker(null, "Hospital", R.drawable.map_hospital);
    public static final MapMarker fire_station = new MapMarker(null, "Fire Station", R.drawable.map_firestation);
    public static final MapMarker police_station = new MapMarker(null, "Police Station", R.drawable.map_policestation);
    public static final MapMarker dam = new MapMarker(null, "Dam", R.drawable.map_dam);
    public static final MapMarker radio_tower = new MapMarker(null, "Radio Tower", R.drawable.map_radiotower);
    public static final MapMarker solar_panel = new MapMarker(null, "Solar Panel", R.drawable.map_solarpanel);
    public static final MapMarker railroad_crossing = new MapMarker(null, "Railroad Crossing", R.drawable.map_railroad_crossing);
    public static final MapMarker water_supply = new MapMarker(null, "Water Supply", R.drawable.map_water_supply);
    public static final MapMarker school = new MapMarker(null, "School", R.drawable.map_school);
    public static final MapMarker post_office = new MapMarker(null, "Post Office", R.drawable.post_office);

    ArrayList<MapMarker> mapMarkers = new ArrayList<>();
    ArrayList<MapMarker> hydrantMarkers = new ArrayList<>();

    public static MapMarker hydrant = new MapMarker(null, "Hydrant", R.drawable.map_hydrant);
    public static MapMarker construction = new MapMarker(null, "Construction", R.drawable.map_construction);
    public static MapMarker airport = new MapMarker(null, "Airport", R.drawable.map_airport);


    public static MapMarker hydrant500 = new MapMarker(null, "< 500 GPM", R.drawable.hydrant_red);
    public static MapMarker hydrant999 = new MapMarker(null, "500-999 GPM", R.drawable.hydrant_yellow);
    public static MapMarker hydrant1499 = new MapMarker(null, "1000-1499 GPM", R.drawable.hydrant_green);
    public static MapMarker hydrant1500 = new MapMarker(null, "1500+ GPM", R.drawable.hydrant_blue);
    public static MapMarker hydrantunknown = new MapMarker(null, "Unknown GPM", R.drawable.hydrant_teal);

    public Markers() {
        mapMarkers.add(hydrant);
        mapMarkers.add(construction);
        mapMarkers.add(road_closure);
        mapMarkers.add(landing_zone);
        mapMarkers.add(aed);
        mapMarkers.add(hospital);
        mapMarkers.add(fire_station);
        mapMarkers.add(police_station);
        mapMarkers.add(dam);
        mapMarkers.add(radio_tower);
        mapMarkers.add(solar_panel);
        mapMarkers.add(airport);
        mapMarkers.add(railroad_crossing);
        mapMarkers.add(water_supply);
        mapMarkers.add(school);
        mapMarkers.add(post_office);

        hydrantMarkers.add(hydrant500);
        hydrantMarkers.add(hydrant999);
        hydrantMarkers.add(hydrant1499);
        hydrantMarkers.add(hydrant1500);
        hydrantMarkers.add(hydrantunknown);
    }

    public ArrayList<MapMarker> getMarkers() {
        /*mapMarkers.add(hydrant);
        mapMarkers.add(construction);
        mapMarkers.add(road_closure);
        mapMarkers.add(landing_zone);
        mapMarkers.add(aed);
        mapMarkers.add(hospital);
        mapMarkers.add(fire_station);
        mapMarkers.add(police_station);
        mapMarkers.add(dam);
        mapMarkers.add(radio_tower);
        mapMarkers.add(solar_panel);
        mapMarkers.add(airport);*/

//        Collections.sort(mapMarkers, new Comparator<MapMarker>() {
//            @Override
//            public int compare(MapMarker s1, MapMarker s2) {
//                return s1.getName().compareToIgnoreCase(s2.getName());
//            }
//        });
        return mapMarkers;
    }

    public MapMarker getMarkerForValue(String value) {
        for (int i = 0; i < mapMarkers.size(); i++) {
            MapMarker m = mapMarkers.get(i);
            if (value.equalsIgnoreCase(m.getName())) {
                //Matches return;
                return m;
            }
        }
        return null;
    }



    public MapMarker getHydrantMarkerForValue(String value) {
        for (int i = 0; i < hydrantMarkers.size(); i++) {
            MapMarker m = hydrantMarkers.get(i);
            if (value.equalsIgnoreCase(m.getName())) {
                //Matches return;
                return m;
            }
        }
        return null;
    }


}
