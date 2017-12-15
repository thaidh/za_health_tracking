package za.healthtracking.models.RunningBucket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import za.healthtracking.database.entities.RunningActivityLog;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 07/08/2017.
 */

public class RunningBucketMonth extends RunningBucket {
    public RunningBucketMonth(Date startTime, Date endTime, List<RunningActivityLog> logs) {
        super(startTime, endTime, logs);
    }

    public RunningBucketMonth(Date startTime, Date endTime, List<RunningActivityLog> logs, boolean isEnable) {
        super(startTime, endTime, logs, isEnable);
    }

    @Override
    public String getLabel() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM");
        return formatter.format(mStartDate);
    }

    @Override
    public String getLiteralDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
        String ans = formatter.format(mStartDate);
        Date stopDate = mEndDate;
        if (TimeHelper.getMonth(stopDate) != TimeHelper.getMonth(mStartDate)) {
            ans += " - " + formatter.format(stopDate);
        } else {
            ans += " - " + TimeHelper.getDay(stopDate);
        }

        return ans;
    }
}
