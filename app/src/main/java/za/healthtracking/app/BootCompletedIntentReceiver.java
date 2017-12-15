package za.healthtracking.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import za.healthtracking.service.PedometerService;

/**
 * Created by hiepmt on 04/08/2017.
 */

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d("ZHealth", "ZaloHealth Service is restarted after rebooting");

            Intent pushIntent = new Intent(context, PedometerService.class);
            context.startService(pushIntent);
        }
    }
}
