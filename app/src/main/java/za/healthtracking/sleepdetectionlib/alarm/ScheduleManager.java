package za.healthtracking.sleepdetectionlib.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public final class ScheduleManager {
    private static ScheduleManager mInstance;
    private static HashMap<String, PendingIntent> mIntentList;
    private static HashMap<String, ScheduleTodoWork> mTodoWorkList;

    public static ScheduleManager getInstance() {
        synchronized (ScheduleManager.class) {
            if (mInstance == null) {
                mInstance = new ScheduleManager();
            }
            if (mTodoWorkList == null) {
                mTodoWorkList = new HashMap();
            }
            if (mIntentList == null) {
                mIntentList = new HashMap();
            }
            return mInstance;
        }
    }

    public static void removeSchedule(Context context, String str) {
        if (context != null && str != null && mTodoWorkList != null && mIntentList != null) {
            PendingIntent pendingIntent = (PendingIntent) mIntentList.get(str);
            if (pendingIntent != null) {
                ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
                synchronized (mIntentList) {
                    mIntentList.remove(str);
                }
            }
            synchronized (mTodoWorkList) {
                mTodoWorkList.remove(str);
            }
        }
    }

    public final void addSchedule(Context context, ScheduleTodoWork scheduleTodoWork) {
        int i = 0;
        if (context != null && scheduleTodoWork != null) {
            String actionTag = scheduleTodoWork.getActionTag();
            if (actionTag != null) {
                PendingIntent pendingIntent = (PendingIntent) mIntentList.get(actionTag);
                if (pendingIntent == null) {
                    Intent intent = scheduleTodoWork.getIntent();
                    if (intent == null) {
                        intent = new Intent();
                    }
                    intent.setAction(actionTag);
                    pendingIntent = PendingIntent.getBroadcast(context, 0, intent, scheduleTodoWork.getPendingIntentFlag());
                    synchronized (mIntentList) {
                        mIntentList.put(actionTag, pendingIntent);
                    }
                }
                PendingIntent pendingIntent2 = pendingIntent;
                int alarType = scheduleTodoWork.getAlarType();
                long triggerTime = scheduleTodoWork.getTriggerTime() + scheduleTodoWork.getIntervalTime();
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent2);
                if (VERSION.SDK_INT >= 19) {
                    alarmManager.setExact(alarType, triggerTime, pendingIntent2);
                } else {
                    alarmManager.set(alarType, triggerTime, pendingIntent2);
                }
                synchronized (mTodoWorkList) {
                    mTodoWorkList.put(actionTag, scheduleTodoWork);
                }
            }
        }
    }

    public final void removeAllSchedule(Context context) {
        if (mTodoWorkList != null) {
            synchronized (mTodoWorkList) {
                Set<String> keySet = mTodoWorkList.keySet();
                ArrayList arrayList = new ArrayList();
                for (String add : keySet) {
                    arrayList.add(add);
                }
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    removeSchedule(context, (String) it.next());
                }
            }
        }
    }

    public final void startTodoWork(Context context, Intent intent, String str) {
        if (mTodoWorkList != null && mTodoWorkList.containsKey(str)) {
            ScheduleTodoWork scheduleTodoWork = (ScheduleTodoWork) mTodoWorkList.get(str);
            if (scheduleTodoWork.isRepeat()) {
                addSchedule(context, scheduleTodoWork);
            }
            scheduleTodoWork.startTodoWork(context, intent);
        }
    }
}
