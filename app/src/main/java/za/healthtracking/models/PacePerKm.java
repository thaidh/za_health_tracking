package za.healthtracking.models;

/**
 * Created by hiepmt on 26/07/2017.
 */

public class PacePerKm { // Second per km
    public float distanceInMeters;
    public long durationInMillis;

    public PacePerKm(float distanceInMeters, long durationInMillis) {
        this.distanceInMeters = distanceInMeters;
        this.durationInMillis = durationInMillis;
    }

    public float getPace() {
        if (distanceInMeters == 0)
            return 0;

        return durationInMillis / distanceInMeters;
    }
}
