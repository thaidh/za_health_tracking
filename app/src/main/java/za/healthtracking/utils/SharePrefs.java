package za.healthtracking.utils;

import android.content.Context;
import android.content.SharedPreferences;

import za.healthtracking.app.MyApplication;

/**
 * Created by hiepmt on 12/06/2017.
 */

public class SharePrefs {
    private static SharePrefs _instance;
    private static String FileName = "SharePrefs";
    private Context mContext;

    private SharePrefs(Context context) {
        mContext = context;
    }

    public static SharePrefs getInstance() {
        if (_instance == null) {
            _instance = new SharePrefs(MyApplication.getAppContext());
        }
        return _instance;
    }

    public int GetInt(String key) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getInt(key, 0);
    }

    public int GetInt(String key, int defaultValue) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }

    public void SetInt(String key, int value) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public float GetFloat(String key) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getFloat(key, 0f);
    }

    public float GetFloat(String key, float defaultValue) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getFloat(key, defaultValue);
    }

    public void SetFloat(String key, float value) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }


    public boolean GetBoolean(String key) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, false);
    }

    public boolean GetBoolean(String key, boolean defaultValue) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, defaultValue);
    }

    public void SetBoolean(String key, boolean value) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public String GetString(String key) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");
    }

    public String GetString(String key, String defaultValue) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return sharedPref.getString(key, defaultValue);
    }

    public void SetString(String key, String value) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
