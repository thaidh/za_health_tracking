package za.healthtracking.models.FitnessBucket;

/**
 * Created by hiepmt on 19/05/2017.
 */

public class FitnessBucket {
    public int nSteps;
    public float distance;
    public float caloriesBurned;
    public long startTime;
    public long endTime;
    public int activeDays = 0;

    public FitnessBucket(int nSteps, float distance, float caloriesBurned, long startTime, long endTime) {
        this.nSteps = nSteps;
        this.distance = distance;
        this.caloriesBurned = caloriesBurned;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public float getAvgDistance() {
        if (activeDays == 0)
            return 0;
        return distance/activeDays;
    }

    public float getAvgCalories() {
        if (activeDays == 0)
            return 0;
        return caloriesBurned/activeDays;
    }

    public int getAvgSteps() {
        if (activeDays == 0)
            return 0;
        return nSteps/activeDays;
    }
}
