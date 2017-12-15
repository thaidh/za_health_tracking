package za.healthtracking.models.RunningBucket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import za.healthtracking.database.entities.RunningActivityLog;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 07/08/2017.
 */

public class RunningBucketWeek extends RunningBucket {
    public RunningBucketWeek(Date startTime, Date endTime, List<RunningActivityLog> logs) {
        super(startTime, endTime, logs);
    }

    public RunningBucketWeek(Date startTime, Date endTime, List<RunningActivityLog> logs, boolean isEnable) {
        super(startTime, endTime, logs, isEnable);
    }

    @Override
    public String getLabel() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mStartDate);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (day >= 1 && day < 8) {
            return day + "/" + (calendar.get(Calendar.MONTH)+1);
        }

        return day + "";
    }

    @Override
    public String getLiteralDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
        String ans = formatter.format(mStartDate);
        Date stopDate = mEndDate; //;TimeHelper.addDateWithDays(mStartDate, 7);
        if (TimeHelper.getMonth(stopDate) != TimeHelper.getMonth(mStartDate)) {
            ans += " - " + formatter.format(stopDate);
        } else {
            ans += " - " + TimeHelper.getDay(stopDate);
        }

        return ans;
    }
}
