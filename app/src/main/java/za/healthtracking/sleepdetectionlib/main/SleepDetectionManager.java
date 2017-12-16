package za.healthtracking.sleepdetectionlib.main;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;

import za.healthtracking.sleepdetectionlib.alarm.ScheduleManager;
import za.healthtracking.sleepdetectionlib.database.DatabaseManager;
import za.healthtracking.sleepdetectionlib.engine.SleepDetectionEngine;
import za.healthtracking.sleepdetectionlib.engine.SleepTimeModel;
import za.healthtracking.sleepdetectionlib.util.Log;

public final class SleepDetectionManager {
    public static Context mContext;
    private static SleepDetectionManager mInstance = null;
    final Handler handler = new Handler(Looper.getMainLooper());

    public static synchronized SleepDetectionManager getInstance() {
        SleepDetectionManager sleepDetectionManager;
        synchronized (SleepDetectionManager.class) {
            if (mInstance == null) {
                mInstance = new SleepDetectionManager();
            }
            sleepDetectionManager = mInstance;
        }
        return sleepDetectionManager;
    }

    public static void startManager(Context context) {
        mContext = context;
        Log.v("SleepDetectionManager", "start service");
        SleepDetectionEngine.getInstance().startSleepDetection(mContext);
    }

    public static void stopManager() {
        SleepDetectionEngine.getInstance().stopSleepDetection(mContext);
        ScheduleManager.getInstance().removeAllSchedule(mContext);
        DatabaseManager.getInstance();
        DatabaseManager.onDestroy();
        mContext = null;
    }

    public static ArrayList<SleepTimeModel> getSleepTime(long start, long end) {
        ArrayList arrayList = new ArrayList();
        SleepDetectionEngine.getInstance();
        return SleepDetectionEngine.getSleepTime(start, end);
    }

    public static void refreshSleepTime() {
        SleepDetectionEngine.getInstance().procSleepDetection();
    }
}
