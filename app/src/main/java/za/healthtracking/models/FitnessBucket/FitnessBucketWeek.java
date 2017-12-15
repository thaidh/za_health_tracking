package za.healthtracking.models.FitnessBucket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import za.healthtracking.utils.TimeHelper;


/**
 * Created by hiepmt on 25/05/2017.
 */

public class FitnessBucketWeek extends BaseFitnessBucket {


    public FitnessBucketWeek(Date startDate, Date endDate, int nSteps, float distance, float caloriesBurned, boolean isEnable) {
        super(startDate, endDate, nSteps, distance, caloriesBurned, isEnable);
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
