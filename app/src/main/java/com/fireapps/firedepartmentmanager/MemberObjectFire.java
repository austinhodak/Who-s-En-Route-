package com.fireapps.firedepartmentmanager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Austin on 3/31/2015.
 */
@IgnoreExtraProperties
public class MemberObjectFire {

    String name, position, department, respondingTo, phoneNumber, respondingFrom, email, respondingTime, respondingFromLoc;
    boolean isResponding, canReset, isOfficer;
    //Location location;

    public MemberObjectFire(){}

    public MemberObjectFire(String fullName ){}

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String value) {
        this.position = value;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String value) {
        this.department = value;
    }


    public String getRespondingTo() {
        return respondingTo;
    }

    public void setRespondingTo(String value) {
        this.respondingTo = value;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setPhoneNumber(String value){
        this.phoneNumber = value;
    }

    public String getCurrentLoc(){
        return respondingFrom;
    }

    public void setLocation(String value){
        this.respondingFrom = value;
    }

    public boolean getisResponding() {
        return isResponding;
    }

    public boolean getCanReset() {
        return canReset;
    }
    public boolean getIsOfficer() {
        return isOfficer;
    }

    public String getEmail() {
        return email;
    }

    public String getRespondingTime() {
        return respondingTime;
    }

    public String getRespondingFromLoc() {
        return respondingFromLoc;
    }



    String cityName;
    double lat, lon;
    public String getCityName() {
        return cityName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }


}
