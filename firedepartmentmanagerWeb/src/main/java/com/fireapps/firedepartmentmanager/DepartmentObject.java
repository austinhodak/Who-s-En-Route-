package com.fireapps.firedepartmentmanager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by Austin on 3/31/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentObject {

    String name;
    String email;

    public DepartmentObject(){}

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

}
