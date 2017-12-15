package za.healthtracking.models.FitnessBucket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hiepmt on 25/05/2017.
 */

public class FitnessBucketDay extends BaseFitnessBucket {

    public FitnessBucketDay(Date startDate, Date endDate, int nSteps, float distance, float caloriesBurned, boolean isEnable) {
        super(startDate, endDate, nSteps, distance, caloriesBurned, isEnable);
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
