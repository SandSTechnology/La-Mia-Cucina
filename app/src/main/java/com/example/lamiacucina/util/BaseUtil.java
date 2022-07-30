package com.example.lamiacucina.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class BaseUtil {
    public static String PREF_NAME="PREFERENCE_MANAGER";
    public static final String LOGIN_ROLE = "login_role";
    public static final String LoggedIn = "LoggedIn";
    public static final String FAMILY_ID = "Family_ID";
    private final Context context;

    public BaseUtil(Context context)
    {
        this.context=context;
    }

    public void ClearPreferences()
    {
        context.getSharedPreferences(PREF_NAME,MODE_PRIVATE).edit().clear().apply();
    }

    public void SetFamilyID(String familyID){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(FAMILY_ID,familyID);
        editor.apply();
    }

    public String getFamilyID(){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        return preferences.getString(FAMILY_ID,"");
    }

    public String getLoginRole(){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        return preferences.getString(LOGIN_ROLE,"");
    }

    public void SetLoginRole(String loginRole){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LOGIN_ROLE,loginRole);
        editor.apply();
    }

    public void SetLoggedIn(boolean status){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(LoggedIn,status);
        editor.apply();
    }

    public Boolean getLoggedIn(){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        return preferences.getBoolean(LoggedIn,false);
    }
}
