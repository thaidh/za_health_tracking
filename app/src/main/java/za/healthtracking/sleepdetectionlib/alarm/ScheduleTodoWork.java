package za.healthtracking.sleepdetectionlib.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import za.healthtracking.sleepdetectionlib.util.Time;


public final class ScheduleTodoWork {
    private String actionTag = null;
    private int alarmType = 0;
    private Intent intent;
    private long intervalTime;
    private boolean isRepeat = false;
    private int pendingIntentFlag = 0/*SecSQLiteDatabase.CREATE_IF_NECESSARY*/;
    private TodoWorkHandler todoWork;
    private TriggerTime triggerTime = TriggerTime.CURRENT_TIME;

    public interface TodoWorkHandler {
        void execute$3b2d1350();
    }

    public enum TriggerTime {
        CURRENT_TIME,
        ELAPSED_REALTIME,
        MIDNIGHT_ONE_TIME,
        MIDNIGHT_HALF_TIME,
        MIDNIGHT_QUARTER_TIME,
        ONE_HOUR,
        ONE_MINUTE
    }

    public ScheduleTodoWork(int alarmType, TriggerTime triggerTime, long intervalTime, boolean isRepeat, String actionTag, TodoWorkHandler todoWrok) {
        this.triggerTime = triggerTime;
        this.intervalTime = 43200000;
        this.isRepeat = true;
        this.actionTag = actionTag;
        this.todoWork = todoWrok;
    }

    public final int getAlarType() {
        return this.alarmType;
    }

    public final long getTriggerTime() {
        if (this.triggerTime != TriggerTime.CURRENT_TIME) {
            if (this.triggerTime == TriggerTime.ELAPSED_REALTIME) {
                return SystemClock.elapsedRealtime();
            }
            if (this.triggerTime == TriggerTime.MIDNIGHT_ONE_TIME) {
                return Time.timeZeroSet(System.currentTimeMillis());
            }
            if (this.triggerTime == TriggerTime.MIDNIGHT_HALF_TIME) {
                return getNearbyTime();
            }
            if (this.triggerTime == TriggerTime.MIDNIGHT_QUARTER_TIME) {
                return getNearbyTime();
            }
            if (this.triggerTime == TriggerTime.ONE_HOUR) {
                return getNearbyTime();
            }
            if (this.triggerTime == TriggerTime.ONE_MINUTE) {
                return Time.timeZeroSetMinute(System.currentTimeMillis());
            }
        }
        return System.currentTimeMillis();
    }

    private long getNearbyTime() {
        long nowTime = System.currentTimeMillis();
        long returnTime = Time.timeZeroSet(nowTime);
        long addTime = 0;
        if (this.triggerTime == TriggerTime.MIDNIGHT_HALF_TIME) {
            addTime = 43200000;
        } else if (this.triggerTime == TriggerTime.MIDNIGHT_QUARTER_TIME) {
            addTime = 21600000;
        } else if (this.triggerTime == TriggerTime.ONE_HOUR) {
            addTime = 3600000;
        }
        while (returnTime + addTime < nowTime) {
            returnTime += addTime;
        }
        return returnTime;
    }

    public final long getIntervalTime() {
        return this.intervalTime;
    }

    public final boolean isRepeat() {
        return this.isRepeat;
    }

    public final String getActionTag() {
        return this.actionTag;
    }

    public final Intent getIntent() {
        return this.intent;
    }

    public final int getPendingIntentFlag() {
        return this.pendingIntentFlag;
    }

    public final void startTodoWork(Context context, Intent intent) {
        if (this.todoWork != null) {
            this.todoWork.execute$3b2d1350();
        }
    }
}
