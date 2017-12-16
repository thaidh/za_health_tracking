package za.healthtracking.sleepdetectionlib.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import za.healthtracking.sleepdetectionlib.main.SleepDetectionManager;

public class IntentStartService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public IntentStartService(String name) {
        super(name);
    }

    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        if (SleepDetectionManager.mContext == null) {
            SleepDetectionManager.mContext = context;
        }
        context.startService(new Intent(this, SleepTrackerServiceManager.class));
    }

    protected void onHandleIntent(Intent intent) {
    }
}
