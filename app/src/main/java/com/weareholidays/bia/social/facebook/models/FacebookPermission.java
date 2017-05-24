package com.weareholidays.bia.social.facebook.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Teja on 06/06/15.
 */
public class FacebookPermission {

    public static String PERMISSION_GRANTED = "granted";

    private String permission;
    private String status;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static FacebookPermission parsePermission(JSONObject jsonObject){
        FacebookPermission permission = new FacebookPermission();
        try {
            permission.setPermission(jsonObject.getString("permission"));
            permission.setStatus(jsonObject.getString("status"));
        } catch (Exception e) {

        }
        return permission;
    }

    public static List<FacebookPermission> parsePermissions(JSONArray permissionsArray){
        List<FacebookPermission> permissions = new ArrayList<>();
        try {
            for(int j = 0; j < permissionsArray.length(); j++){
                JSONObject pj = permissionsArray.getJSONObject(j);
                permissions.add(parsePermission(pj));
            }
        }
        catch (Exception e){

        }
        return permissions;
    }

    public static List<FacebookPermission> parsePermissions(JSONObject permissionsData){
        List<FacebookPermission> permissions = new ArrayList<>();
        try {
            return parsePermissions(permissionsData.getJSONArray("data"));
        }
        catch (Exception e){

        }
        return permissions;
    }
}
