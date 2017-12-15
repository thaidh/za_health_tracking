package za.healthtracking.pedometer;

import android.util.Log;

import za.healthtracking.app.Settings;
import za.healthtracking.utils.ComputationUtil;

/**
 * Created by hiepmt on 10/07/2017.
 */

public class SessionCaloriesManager {

    private Listener mListener;
    private int mSteps = 0;
    private float mDistance = 0;
    private long mDuration = 0;
    private long mLastTime = 0;

    public interface Listener {
        void addCalories(float calories);
    }

    public SessionCaloriesManager(Listener listener) {
        mListener = listener;
    }

    public void shouldCalCalories(long newTime) {
        if (mLastTime == 0) {
            mLastTime = newTime;
            return;
        }

        this.mDuration = newTime - this.mLastTime;

        if (this.mDuration >= 10000) { //Calculate calories after a minute
            calcCalories();
        }
    }

    public void addStepCounterAndDistance(int stepDelta, float distanceDelta) {
        mSteps += stepDelta;
        mDistance += distanceDelta;
    }

    public void calcCalories() {
        float speed;
        int stepFrequency;
        float calories = 0.0f;
        if (mSteps > 0) {
            speed = calcSpeed(mDuration);
            stepFrequency = (int) calcStepFrequency(mDuration);
        } else {
            speed = 0.0f;
            stepFrequency = 0;
        }
        speed *= 3.6f; // mps to kph
        Log.d("ZHealth", "SessionCaloriesManager: Calories Phase Time elapsed, stepCount: " + mSteps + ", avgSpeed: " + speed + ", avgStepFrequency: " + stepFrequency + ", caloriesPhaseDuration: " + this.mDuration);
        if (stepFrequency < 50) {
            Log.d("ZHealth", "SessionCaloriesManager: user is very lame");
        } else if (stepFrequency < 150) {
            calories = ComputationUtil.calcCalories(speed, this.mDuration, Settings.getUserProfileWeight(), 2, Settings.getUserProfileIsMale());
            Log.d("ZHealth", "SessionCaloriesManager: user is walking");
        } else {
            calories = ComputationUtil.calcCalories(speed, this.mDuration, Settings.getUserProfileWeight(), 1, Settings.getUserProfileIsMale());
            Log.d("ZHealth", "SessionCaloriesManager: user is running");
        }
        if (calories > 0) {
            Log.d("ZHealth", "Calories: " + calories);
            this.mListener.addCalories(calories);
        }

        mSteps = 0;
        mLastTime += mDuration;
        mDuration = 0;
        mDistance = 0;
    }

    private float calcSpeed(long duration) {
        return ComputationUtil.calcSpeed(mDistance, duration);
    }

    private double calcStepFrequency(long duration) {
        if (duration > 0) {
            return (float)(mSteps)/(duration / 60000f);
        }
        return 0.0d;
    }

}
