package com.fireapps.firedepartmentmanager;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by austinhodak on 6/16/16.
 */
@IgnoreExtraProperties
public class Message {
    boolean activeIncident, completed;
    String date, type, department, message, sender;

    public Message() {
    }

    public boolean isActiveIncident() {
        return activeIncident;
    }

    public String getDate() {
        return date;
    }

    public String getDepartment() {
        return department;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }
}
