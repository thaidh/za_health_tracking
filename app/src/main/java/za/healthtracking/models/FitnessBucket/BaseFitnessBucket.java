package za.healthtracking.models.FitnessBucket;

import java.util.Date;

/**
 * Created by hiepmt on 23/05/2017.
 */

public class BaseFitnessBucket {
    public static int MAX_STEP = 10000;
    public int nSteps;
    public Date mStartDate;
    public Date mEndDate;
    public boolean mIsEnable;
    public float mDistance;
    public float mCaloriesBurned;

    public BaseFitnessBucket(Date startDate, Date endDate, int nSteps, float distance, float caloriesBurned, boolean isEnable) {
        this.nSteps = nSteps;
        this.mStartDate = startDate;
        this.mEndDate = endDate;
        this.mIsEnable = isEnable;
        this.mDistance = distance;
        this.mCaloriesBurned = caloriesBurned;
    }

    public String getLabel() {
        return "";
    }

    public String getLiteralDate() {
        return "";
    }
}
