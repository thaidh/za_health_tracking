package za.healthtracking.sleepdetectionlib.request;

import java.io.Serializable;

public final class TimeModel implements Serializable {
    private long endTime;
    private long startTime;

    public final long getStartTime() {
        return this.startTime;
    }

    public final long getEndTime() {
        return this.endTime;
    }
}
