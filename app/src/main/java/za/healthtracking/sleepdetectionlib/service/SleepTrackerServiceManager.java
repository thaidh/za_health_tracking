package za.healthtracking.sleepdetectionlib.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import za.healthtracking.sleepdetectionlib.main.SleepDetectionManager;
import za.healthtracking.sleepdetectionlib.util.Log;

public class SleepTrackerServiceManager extends Service {
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.v("SleepTrackerServiceManager", "Starting Service.");
        Context context = getApplicationContext();
        SleepDetectionManager.getInstance();
        SleepDetectionManager.startManager(context);
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v("SleepTrackerServiceManager", "Sleep Detection Service Destroy.");
        SleepDetectionManager.mContext = null;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
