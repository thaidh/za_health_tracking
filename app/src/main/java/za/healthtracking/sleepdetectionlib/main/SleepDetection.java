package za.healthtracking.sleepdetectionlib.main;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import za.healthtracking.sleepdetectionlib.alarm.ScheduleManager;
import za.healthtracking.sleepdetectionlib.database.SharedData;
import za.healthtracking.sleepdetectionlib.engine.SleepTimeModel;
import za.healthtracking.sleepdetectionlib.service.SleepTrackerServiceManager;
import za.healthtracking.sleepdetectionlib.util.Log;

public final class SleepDetection {
    private static SleepDetection mInstance = null;

    public static SleepDetection getInstance() {
        if (mInstance == null) {
            mInstance = new SleepDetection();
        }
        return mInstance;
    }

    public static ArrayList<SleepTimeModel> getSleepTime(long j, long j2) {
        SleepDetectionManager.getInstance();
        return SleepDetectionManager.getSleepTime(j, j2);
    }

    public static SleepDetectionResultEnum startService(Context context) {
        if (context == null) {
            Log.v("SleepDetection", "Error : mContext is null.");
            return SleepDetectionResultEnum.RESULT_ERROR_INITIALIZE;
        }
        if (SleepDetectionManager.mContext == null) {
            SleepDetectionManager.mContext = context;
        }
        context.startService(new Intent(context, SleepTrackerServiceManager.class));
        Log.v("SleepDetection", "Service started.");
        SharedData.getInstance().setSharedPreference(context, SharedData.PreferenceValue.SERVICE_STATUS, Boolean.valueOf(true));
        return SleepDetectionResultEnum.RESULT_OK;
    }

    public static SleepDetectionResultEnum stopService(Context context) {
        if (context == null) {
            return SleepDetectionResultEnum.RESULT_ERROR_INITIALIZE;
        }
        if (SleepDetectionManager.mContext == null) {
            SleepDetectionManager.mContext = context;
        }
        context.stopService(new Intent(context, SleepTrackerServiceManager.class));
        ScheduleManager.getInstance().removeAllSchedule(context);
        SleepDetectionManager.getInstance();
        SleepDetectionManager.stopManager();
        Log.v("SleepDetection", "Service stopped.");
        SharedData.getInstance().setSharedPreference(context, SharedData.PreferenceValue.SERVICE_STATUS, Boolean.valueOf(false));
        return SleepDetectionResultEnum.RESULT_OK;
    }

    public static void updateSleepTime() {
        SleepDetectionManager.getInstance();
        SleepDetectionManager.refreshSleepTime();
    }
}
