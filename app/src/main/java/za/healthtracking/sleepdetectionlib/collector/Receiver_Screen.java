package za.healthtracking.sleepdetectionlib.collector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.util.Random;

import za.healthtracking.sleepdetectionlib.database.DatabaseManager;

public class Receiver_Screen extends BroadcastReceiver {
    private static boolean lastScreenOn = false;
    private String TAG = getClass().getSimpleName();
    private Random random = new Random();

    private void insertData(final ScreenModel screenModel) {
        synchronized (this) {
            new Thread(new Runnable() {
                public final void run() {
                    DatabaseManager.getInstance().insertData(screenModel);
                }
            }).start();
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                Log.d(this.TAG, "ACTION_SCREEN_OFF");
                insertData(new ScreenModel(System.currentTimeMillis(), 0, 0, 0));
                lastScreenOn = false;
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                int increaseRandomTime = 3600000 * (random.nextInt(10) + 1);
                Log.d(this.TAG, "ACTION_SCREEN_ON: "  + increaseRandomTime);
                insertData(new ScreenModel(System.currentTimeMillis() + increaseRandomTime, 1, 0, 0));

                lastScreenOn = true;
            } else if (action.equals("android.intent.action.USER_PRESENT")) {
                Log.d(this.TAG, "ACTION_USER_PRESENT");
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (powerManager != null) {
                    boolean isScreenOn = powerManager.isScreenOn();
                    if (isScreenOn || lastScreenOn) {
                        insertData(new ScreenModel(System.currentTimeMillis(), 1, 1, 0));
                    }
                    lastScreenOn = isScreenOn;
                    return;
                }
                insertData(new ScreenModel(System.currentTimeMillis(), 1, 1, 0));
                lastScreenOn = true;
            }
        }
    }
}
