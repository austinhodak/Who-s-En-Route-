package com.fireapps.firedepartmentmanager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by Austin on 3/31/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@IgnoreExtraProperties
public class MemberObject {

    String name, position, department, respondingTo, phoneNumber, respondingFrom, email;
    boolean isResponding, canReset, isOfficer;
    List<Boolean> departments;
    private String respondingTime, respondingFromLoc;
    String id;

    public MemberObject(){}

    public MemberObject(String username){
        this.id = username;
    }

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

    public String getLocation(){
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
    public void setCanReset(boolean value) {
        this.canReset = value;
    }
    public void setisOfficer(boolean value) {
        this.isOfficer = value;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public List<Boolean> getDepartments() {
        return departments;
    }

    public String getRespondingTime() {
        return respondingTime;
    }

    public String getRespondingFromLoc() {
        return respondingFromLoc;
    }


}
