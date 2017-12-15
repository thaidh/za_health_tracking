package za.healthtracking.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hiepmt on 26/07/2017.
 */

public class RunningSession {
    private long duration;
    private float distance;
    private float calories;
    private float avgPace;
    private List<LatLng> latLngList = new ArrayList<>();
    private List<PacePerKm> pacePerKmList = new ArrayList<>();
    public long startTime;

    public RunningSession(long duration, float distance, float calories, float avgPace, List<LatLng> latLngList, List<PacePerKm> pacePerKmList, long startTime) {
        this.duration = duration;
        this.distance = distance;
        this.calories = calories;
        this.avgPace = avgPace;
        this.latLngList = latLngList;
        this.pacePerKmList = pacePerKmList;
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public float getAvgPace() {
        return avgPace;
    }

    public void setAvgPace(float avgPace) {
        this.avgPace = avgPace;
    }

    public List<LatLng> getLatLngList() {
        return latLngList;
    }

    public void setLatLngList(List<LatLng> latLngList) {
        this.latLngList = latLngList;
    }

    public List<PacePerKm> getPacePerKmList() {
        return pacePerKmList;
    }

    public void setPacePerKmList(List<PacePerKm> pacePerKmList) {
        this.pacePerKmList = pacePerKmList;
    }
}
