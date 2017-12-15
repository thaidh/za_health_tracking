package za.healthtracking.service;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import za.healthtracking.utils.Helper;

import static android.content.ContentValues.TAG;

/**
 * Created by hiepmt on 24/07/2017.
 */

public class RunningController {
    Location mPenultimateLocation = null;
    Location mLastLocation = null;
    List<Location> locations = new ArrayList<>();
    float mDistance = 0;


    private boolean isLocationAccepted(Location location) {
        // Check accuracy
        if (0.0f >= location.getAccuracy() || location.getAccuracy() >= 150f) {
            Log.d("ZHealth", "Ignore location: " + location + " because accuracy is bad!");
            return false;
        }

        // First location
        if (mLastLocation == null) {
            return true;
        }

        // Filter distanceInMeters < 3
        if (Helper.calcDistance(mLastLocation, location) < 3.0d) {
            Log.d("ZHealth", "Ignore location: " + location + " because distanceInMeters < 3 meters!");

            return false;
        }

        // Filter change accelerometer
        if (!isAccelerationOk(location)) {
            Log.d("ZHealth", "Ignore location: " + location + " because acceleration change so fast!");

            return false;
        }

        // Filter timestamp
        if (!isTimestampOk(mLastLocation, location)) {
            Log.d("ZHealth", "Ignore location because timestamp is not ok!");

            return false;
        }

        return true;
    }

    private boolean isAccelerationOk(Location location) {
        if (this.mPenultimateLocation == null || this.mLastLocation == null) {
            return true;
        }

        Double accelerationForWaypoints = getAccelerationForLocations(location, this.mLastLocation, this.mPenultimateLocation);
        Log.d("ZHealth", "accelerationForWaypoints: " + accelerationForWaypoints);

        if (accelerationForWaypoints == null) {
            Log.e(TAG, "Division by zero when calculating acceleration between waypoints.");
            return false;
        } else if (accelerationForWaypoints >= 11.0d) {
            return false;
        } else {
            return true;
        }
    }

    private static Double getAccelerationForLocations(Location location, Location location2, Location location3) {
        Double d = null;
        try {
            double secondsBetweenWaypoints = getSecondsBetweenWaypoints(location2, location3);
            double secondsBetweenWaypoints2 = getSecondsBetweenWaypoints(location, location2);
            if ((secondsBetweenWaypoints != 0.0d && secondsBetweenWaypoints2 != 0.0d)) {
                d = Double.valueOf((getSpeedBetweenPoints(location, location2, secondsBetweenWaypoints2) - getSpeedBetweenPoints(location2, location3, secondsBetweenWaypoints)) / secondsBetweenWaypoints2);
            }
        } catch (Throwable e) {
            Log.e(TAG, "missing timestamp?", e);
        }
        return d;
    }

    private static double getSecondsBetweenWaypoints(Location location1, Location location2) {
        return ((double) Math.abs(location1.getTime() - location2.getTime())) / 1000.0d;
    }

    private static double getSpeedBetweenPoints(Location location1, Location location2, double d) {
        return Helper.calcDistance(location1, location2) / d;
    }


    private boolean isTimestampOk(Location lo1, Location lo2) {
        double deltaTimestamp = ((double)Math.abs(lo1.getTime() - lo2.getTime())) / 1000.0d;
        Log.d("ZHealth", "delta Timestamp " + deltaTimestamp);

        return deltaTimestamp != 0.0d;
    }


    public Location updateLocationChanged(Location location) {
        if (isLocationAccepted(location)) {
            if (mLastLocation != null) {
                mDistance += Helper.calcDistance(mLastLocation, location);
            }

            locations.add(location);

            mPenultimateLocation = mLastLocation;
            mLastLocation = location;
            return location;
        }
        return null;
    }

    public float getDistance() {
        return mDistance;
    }
}
