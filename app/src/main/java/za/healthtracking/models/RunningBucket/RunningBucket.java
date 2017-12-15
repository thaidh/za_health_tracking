package za.healthtracking.models.RunningBucket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import za.healthtracking.database.entities.RunningActivityLog;

/**
 * Created by hiepmt on 07/08/2017.
 */

public class RunningBucket {
    public static float MAX_DURATION = 10 * 60 * 1000;
    public List<RunningActivityLog> mLogs = new ArrayList<>();
    public Date mStartDate;
    public Date mEndDate;
    public boolean isEnable = true;

    public RunningBucket(Date startTime, Date endTime, List<RunningActivityLog> logs) {
        mStartDate = startTime;
        mEndDate = endTime;
        this.mLogs = logs;
        this.isEnable = true;
    }

    public RunningBucket(Date startTime, Date endTime, List<RunningActivityLog> logs, boolean isEnable) {
        mStartDate = startTime;
        mEndDate = endTime;
        mLogs = logs;
        this.isEnable = isEnable;
    }

    public long getDuration() {
        if (mLogs == null)
            return 0;

        long duration = 0;
        for (RunningActivityLog log : mLogs) {
            duration += log.durationInMillis;
        }
        return duration;
    }

    public float getDistance() {
        if (mLogs == null)
            return 0;

        float distance = 0;
        for (RunningActivityLog log : mLogs) {
            distance += log.distanceInMeters;
        }
        return distance;
    }

    public String getLabel() {
        return "";
    }

    public String getLiteralDate() {
        return "";
    }
}
