package za.healthtracking.pedometer;

/**
 * Created by hiepmt on 07/07/2017.
 */

public class StepData {
    private long timestamp;
    private int stepCount = 1;

    public StepData(long timestamp, int stepCount) {
        this.timestamp = timestamp;
        this.stepCount = stepCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}
