package za.healthtracking.sleepdetectionlib.engine;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class SleepTimeModel {
    long checkTime;
    long endTime;
    int ignoreSleep;
    long startTime;

    public SleepTimeModel(long checkTime, int ignoreSleep, long startTime, long endTime) {
        this.checkTime = checkTime;
        this.ignoreSleep = ignoreSleep;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public final long getCheckTime() {
        return this.checkTime;
    }

    public final String getCheckTimeText() {
        return new SimpleDateFormat("MM-dd HH:mm").format(new Date(this.checkTime));
    }

    public final int getIgnoreSleep() {
        return this.ignoreSleep;
    }

    public final long getStartTime() {
        return this.startTime;
    }

    public final String getStartTimeText() {
        return new SimpleDateFormat("MM-dd HH:mm").format(new Date(this.startTime));
    }

    public final long getEndTime() {
        return this.endTime;
    }

    public final String getEndTimeText() {
        return new SimpleDateFormat("MM-dd HH:mm").format(new Date(this.endTime));
    }

    public final long getDuration() {
        return this.endTime - this.startTime;
    }
}
