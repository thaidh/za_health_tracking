package za.healthtracking.models;

import java.util.Date;
import java.util.List;

import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.utils.TimeHelper;


/**
 * Created by hiepmt on 25/05/2017.
 */

public class FitnessDateDetail {
    private Date mDate;
    private List<FitnessBucket> buckets;

    public FitnessDateDetail(Date date, List<FitnessBucket> buckets) {
        this.mDate = date;
        this.buckets = buckets;
    }

    public List<FitnessBucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<FitnessBucket> buckets) {
        this.buckets = buckets;
    }

    public boolean matchDate(Date date) {
        return TimeHelper.getDay(mDate) == TimeHelper.getDay(date) &&
                TimeHelper.getMonth(mDate) == TimeHelper.getMonth(date) &&
                TimeHelper.getYear(mDate) == TimeHelper.getYear(date);
    }

    public float getDistance() {
        if (buckets == null)
            return 0;

        float distance = 0;
        for (int i = 0; i < buckets.size(); i++) {
            distance += buckets.get(i).distance;
        }
        return distance;
    }

    public float getCaloriesBurned() {
        if (buckets == null)
            return 0;

        float caloriesBurned = 0;
        for (int i = 0; i < buckets.size(); i++) {
            caloriesBurned += buckets.get(i).caloriesBurned;
        }
        return caloriesBurned;
    }

    public int getSteps() {
        if (buckets == null)
            return 0;

        int steps = 0;
        for (int i = 0; i < buckets.size(); i++) {
            steps += buckets.get(i).nSteps;
        }
        return steps;
    }
}
