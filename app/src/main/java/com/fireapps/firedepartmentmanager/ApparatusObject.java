package com.fireapps.firedepartmentmanager;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by austinhodak on 6/8/16.
 */
@IgnoreExtraProperties
public class ApparatusObject {

    String apparatusName, apparatusAbrv, alertTitle, alertColor, alertDesc, alertBy, id;
    boolean inService;
    boolean isAlert;

    public ApparatusObject(){}

    public ApparatusObject(String apparatusName, String apparatusAbrv, boolean inService, Boolean isAlert) {
        this.apparatusName = apparatusName;
        this.apparatusAbrv = apparatusAbrv;
        this.inService = inService;
        this.isAlert = isAlert;
    }

    public String getApparatusName() {
        return apparatusName;
    }
    public String getApparatusAbrv() {
        return apparatusAbrv;
    }
    public boolean isInService() {
        return inService;
    }
    public boolean getIsAlert() { return isAlert; }
    public String getAlertTitle() { return alertTitle; }
    public String getAlertColor() { return alertColor; }
    public String getAlertDesc() { return  alertDesc; }
    public String getAlertBy() { return alertBy; }

    public String getId() {
        return id;
    }

//Setters

    public void setAlertBy(String alertBy) {
        this.alertBy = alertBy;
    }

    public void setAlertTitle(String alertTitle) {
        this.alertTitle = alertTitle;
    }

    public void setAlertDesc(String alertDesc) {
        this.alertDesc = alertDesc;
    }

    public void setAlertColor(String alertColor) {
        this.alertColor = alertColor;
    }

    public void setAlert(boolean alert) {
        isAlert = alert;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("isAlert", isAlert);
        result.put("alertBy", alertBy);
        result.put("alertTitle", alertTitle);
        result.put("alertDesc", alertDesc);
        result.put("alertColor", alertColor);
        result.put("apparatusName", apparatusName);
        result.put("apparatusAbrv", apparatusAbrv);
        result.put("inService", inService);

        return result;
    }
}
