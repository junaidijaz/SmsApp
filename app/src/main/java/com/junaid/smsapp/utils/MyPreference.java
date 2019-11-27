package com.junaid.smsapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPreference {
    private static MyPreference yourPreference;
    private SharedPreferences sharedPreferences;

    public static MyPreference getInstance(Context context) {
        if (yourPreference == null) {
            yourPreference = new MyPreference(context);
        }
        return yourPreference;
    }

    private MyPreference(Context context) {
        sharedPreferences = context.getSharedPreferences("YourCustomNamedPreference",Context.MODE_PRIVATE);
    }

    public void saveData(String key,String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putString(key, value);
        prefsEditor.apply();
    }

    public String getData(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(key, "");
        }
        return "";
    }
}