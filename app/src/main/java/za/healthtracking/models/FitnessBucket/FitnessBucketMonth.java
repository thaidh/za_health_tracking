package za.healthtracking.models.FitnessBucket;

import java.text.SimpleDateFormat;
import java.util.Date;

import za.healthtracking.utils.TimeHelper;


/**
 * Created by hiepmt on 25/05/2017.
 */

public class FitnessBucketMonth extends BaseFitnessBucket {

    public FitnessBucketMonth(Date startDate, Date endDate, int nSteps, float distance, float caloriesBurned, boolean isEnable) {
        super(startDate, endDate, nSteps, distance, caloriesBurned, isEnable);
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