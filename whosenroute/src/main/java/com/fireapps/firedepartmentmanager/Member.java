package com.fireapps.firedepartmentmanager;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by austinhodak on 6/15/16.
 */
@IgnoreExtraProperties
public class Member {
    String department, email, respondingTo, respondingTime, name, phoneProvider, position, positionAbbrv, positionColor;
    boolean canReset, isResponding;
    long phoneNum;

    public Member() {
    }

    public boolean isCanReset() {
        return canReset;
    }

    public void setCanReset(boolean canReset) {
        this.canReset = canReset;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsResponding() {
        return isResponding;
    }

    public void setResponding(boolean responding) {
        isResponding = responding;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(long phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getPhoneProvider() {
        return phoneProvider;
    }

    public void setPhoneProvider(String phoneProvider) {
        this.phoneProvider = phoneProvider;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRespondingTime() {
        return respondingTime;
    }

    public void setRespondingTime(String respondingTime) {
        this.respondingTime = respondingTime;
    }

    public String getRespondingTo() {
        return respondingTo;
    }

    public void setRespondingTo(String respondingTo) {
        this.respondingTo = respondingTo;
    }

    public String getPositionColor() {
        return positionColor;
    }

    public String getPositionAbbrv() {
        return positionAbbrv;
    }
}
