package za.healthtracking.sleepdetectionlib.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            long currentTimeMillis = System.currentTimeMillis();
            Log.d("ScheduleReceiver***", "onReceive : " + intent.getAction() + " / Started at " + currentTimeMillis);
            ScheduleManager.getInstance().startTodoWork(context, intent, intent.getAction());
            long currentTimeMillis2 = System.currentTimeMillis();
            Log.d("ScheduleReceiver***", "onReceive : Finished at " + currentTimeMillis2 + " / Duration : " + (currentTimeMillis2 - currentTimeMillis));
        }
    }
}
