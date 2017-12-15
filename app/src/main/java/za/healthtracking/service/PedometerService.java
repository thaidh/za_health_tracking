package za.healthtracking.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import za.healthtracking.database.entities.DailyActivityLog;
import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.pedometer.SessionManager;
import za.healthtracking.pedometer.StepData;

/**
 * Created by hiepmt on 31/07/2017.
 */

public class PedometerService extends Service implements SensorEventListener {
    SensorManager mSensorManager;
    SessionManager mSessionManager;
    int mOldStepCounter = 0;

    public List<FitnessBucket> readHistoryFitnessPer20MinutesToDay() {
        if (mSessionManager == null)
            return null;

        long now = Calendar.getInstance().getTimeInMillis();
        return mSessionManager.getBucketsForADay(now);
    }

    public float getDistance() {
        if (mSessionManager == null || mSessionManager.getDailyActivityLogToday() == null)
            return 0;

        DailyActivityLog dailyActivityLog = mSessionManager.getDailyActivityLogToday();
        return dailyActivityLog.distanceInMeters;
    }

    public float getCalories() {
        if (mSessionManager == null || mSessionManager.getDailyActivityLogToday() == null)
            return 0;

        DailyActivityLog dailyActivityLog = mSessionManager.getDailyActivityLogToday();
        return dailyActivityLog.calories;
    }

    public int getSteps() {
        if (mSessionManager == null || mSessionManager.getDailyActivityLogToday() == null)
            return 0;

        DailyActivityLog dailyActivityLog = mSessionManager.getDailyActivityLogToday();
        return dailyActivityLog.steps;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            StepData stepdata = new StepData(Calendar.getInstance().getTimeInMillis(), 1);
            mSessionManager.onSensorValueReceived(stepdata, event.values[0], event.values[1], event.values[2]);
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (mOldStepCounter > 0) {
                long nowInMillis = Calendar.getInstance().getTimeInMillis();

                int steps = (int)event.values[0]-mOldStepCounter;
                StepData stepdata = new StepData(nowInMillis, steps);
                mSessionManager.onSensorNewStep(stepdata);
            }
            mOldStepCounter = (int)event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public class LocalBinder extends Binder {
        public PedometerService getService() {
            return PedometerService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d("ZHealth", "OnCreate");

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Start service
        mSessionManager = new SessionManager();

        // Step counter
        if (isKitkatWithStepSensor() && isStepCounterSensorWorking()) {
            Sensor stepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            mSensorManager.registerListener(PedometerService.this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
            Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(PedometerService.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(this, "Thiết bị không hổ trợ Step Detector Sensor hoặc Accelerometer Sensor", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isStepCounterSensorWorking() {
        Sensor stepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        return stepCounterSensor.getPower() > 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ZHealth", "OnStartCommand");

        return START_STICKY;
    }


    private boolean isKitkatWithStepSensor() {
        // Check that the device supports the step counter and detector sensors
        PackageManager packageManager = getPackageManager();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mSessionManager.stopSession();

        return super.onUnbind(intent);
    }


    @Override
    public void onDestroy() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();
}
