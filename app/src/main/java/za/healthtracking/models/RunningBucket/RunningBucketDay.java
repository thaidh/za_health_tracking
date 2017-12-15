package za.healthtracking.models.RunningBucket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import za.healthtracking.database.entities.RunningActivityLog;

/**
 * Created by hiepmt on 07/08/2017.
 */

public class RunningBucketDay extends RunningBucket {
    public RunningBucketDay(Date startTime, Date endTime, List<RunningActivityLog> logs) {
        super(startTime, endTime, logs);
    }

    public RunningBucketDay(Date startTime, Date endTime, List<RunningActivityLog> logs, boolean isEnable) {
        super(startTime, endTime, logs, isEnable);
    }

    @Override
    public String getLabel() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mStartDate);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (day == 1) {
            return "1/" + (calendar.get(Calendar.MONTH)+1);
        }

        return day + "";
    }

    @Override
    public String getLiteralDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM dd");
        return formatter.format(mStartDate);
    }
}
