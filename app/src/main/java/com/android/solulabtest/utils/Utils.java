package com.android.solulabtest.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DecimalFormat;

public class Utils {
    public static String PREF_DISTANCE_DATA = "PREF_DISTANCE_DATA";
    public static String PREF_KEY_TOTAL_DISTANCE = "PREF_KEY_TOTAL_DISTANCE";

    public static void saveTotalTravelledMeters(Context context, double meter){
        SharedPreferences sharedPref = context.getSharedPreferences(
                PREF_DISTANCE_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_KEY_TOTAL_DISTANCE, (Utils.getFormatedDecimal(meter)));
        editor.apply();
    }

    public static String getTotalTravelledMeters(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                PREF_DISTANCE_DATA, Context.MODE_PRIVATE);
        return sharedPref.getString(PREF_KEY_TOTAL_DISTANCE, "");
    }

    public static String getFormatedDecimal(double d){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(d);
    }

}
