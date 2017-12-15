package za.healthtracking.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import vn.zing.pedometer.R;
import za.healthtracking.app.AppController;
import za.healthtracking.app.MyApplication;
import za.healthtracking.app.Settings;
import za.healthtracking.database.DbHelper;
import za.healthtracking.database.entities.RunningActivityLog;
import za.healthtracking.models.PacePerKm;
import za.healthtracking.models.RunningSession;
import za.healthtracking.ui.RunningActivity;
import za.healthtracking.utils.ComputationUtil;
import za.healthtracking.utils.Helper;
import za.healthtracking.utils.PolyUtil;
import za.healthtracking.utils.TimeHelper;

import static android.content.ContentValues.TAG;

/**
 * Created by hiepmt on 21/07/2017.
 */

public class RunningActivityService extends Service implements GpsStatus.Listener, LocationListener {
    private NotificationManager mNM;

    private int NOTIFICATION = R.string.app_name;
    private int NOTIFICATION_CODE = 1001;

    long mDuration = 0; //in milli second
    long mStarTime = 0; // in milli second

    CountDownTimer mTimer;
    LocationManager locationManager;
    LocationListenerWrapper mLocationListenerWrapper;

    DbHelper dbHelper = new DbHelper(MyApplication.getAppContext());
    Dao<RunningActivityLog, Integer> runningActivityLogDao = null;

    public enum State {NONE, RUNNING, PAUSE, STOP}
    private State mState;
    public State getState() {
        return mState;
    }

    private void onStartRecording() {
        runningController = new RunningController();

        mState = State.RUNNING;

        updateNotification();
        mDuration = 0;
        mPaceStartTime = 0;

        mPacePerKmList = new ArrayList<>();
        mPacePerKmList.add(new PacePerKm(0, 0));

        latLngList = new ArrayList<>();

        mTimer.start();

        addLocationListener();

        // Init DB
        try {
            runningActivityLogDao = dbHelper.getRunningActivityLogDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mStarTime = Calendar.getInstance().getTimeInMillis();
    }

    public void onPauseRecording() {
        mState = State.PAUSE;

        mTimer.cancel();
        removeLocationListener();
    }

    public void onResumeRecording() {
        mState = State.RUNNING;

        mTimer.start();
        addLocationListener();
    }

    public void onStopRecording() {
        // Save to db
        saveRunningActivityToDB();

        // Data for RunningSummaryIntent
        AppController.runningSession = new RunningSession(mDuration, getDistance(), getCalories(), getAvgPace(), getLatLngList(), getPacePerKmList(), mStarTime);

        // Stop
        mState = State.STOP;

        mTimer.cancel();
        stopForeground(true);
        stopSelf();

        removeLocationListener();
    }

    public synchronized void saveRunningActivityToDB() {
        RunningActivityLog runningActivityLog = new RunningActivityLog();
        runningActivityLog.startTime = TimeHelper.MillisToSecond(mStarTime);
        runningActivityLog.endTime = TimeHelper.MillisToSecond(Calendar.getInstance().getTimeInMillis());
        runningActivityLog.calories = getCalories();
        runningActivityLog.avgPace = getAvgPace();
        runningActivityLog.durationInMillis = mDuration;
        runningActivityLog.distanceInMeters = getDistance();
        runningActivityLog.wayPoints = PolyUtil.encode(getLatLngList());
        runningActivityLog.pacePerKM = RunningActivityLog.encodePacePerKM(getPacePerKmList());

        try {
            runningActivityLogDao.create(runningActivityLog);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    LatLng mLastLatLng = null;
    RunningController runningController = new RunningController();

    public LatLng getLastLatLng() {
        return mLastLatLng;
    }

    List<LatLng> latLngList = new ArrayList<>();
    public List<LatLng> getLatLngList() {
        return latLngList;
    }

    List<PacePerKm> mPacePerKmList = new ArrayList<>();
    long mPaceStartTime = 0;

    public List<PacePerKm> getPacePerKmList() {
        return mPacePerKmList;
    }

    public float getDistance() {
        return runningController.getDistance();
    }

    public float getCalories() {
        float speed = getDistance()/(getDuration()/1000) * 3.6f; //km/h
        return ComputationUtil.calcCalories(speed, getDuration(), Settings.getUserProfileWeight(), 1, Settings.getUserProfileIsMale());
    }

    public long getDuration() {
        return mDuration;
    }

    public float getAvgPace() {
        return Helper.calcPace(mDuration, getDistance());
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("ZHealth", "Location: " + location.getLatitude() + "; " + location.getLongitude());

        Location addedLocation = runningController.updateLocationChanged(location);
        if (addedLocation != null) {
            LatLng newLatLng = new LatLng(addedLocation.getLatitude(), addedLocation.getLongitude());
            latLngList.add(newLatLng);

            mLastLatLng = newLatLng;

            updateCurrentPace();
        }
    }

    synchronized void updateCurrentPace() {
        // Cal pace
        float paceDistance = runningController.getDistance() - (mPacePerKmList.size()-1)*1000.0f;
        if (paceDistance >= 1000.0f) {  //reach 1 km
            // Update old
            PacePerKm pacePerKm = mPacePerKmList.get(mPacePerKmList.size()-1);
            pacePerKm.distanceInMeters = 1000.0f;
            pacePerKm.durationInMillis = mDuration - mPaceStartTime;

            // Create new one
            mPacePerKmList.add(new PacePerKm(paceDistance-1000.0f, 0));
            mPaceStartTime = mDuration;
        } else {
            // Update old
            PacePerKm pacePerKm = mPacePerKmList.get(mPacePerKmList.size()-1);
            pacePerKm.distanceInMeters = paceDistance;
            pacePerKm.durationInMillis = mDuration - mPaceStartTime;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public class LocalBinder extends Binder {
        public RunningActivityService getService() {
            return RunningActivityService.this;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        mState = State.NONE;

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mTimer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                mDuration += 1000;

                updateCurrentPace();

                updateNotification();
                start();
            }
        };

        // Get the location manager from the system
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Log.d("ZHealth", "Service::onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ZHealth", "Service::onStartCommand()");

        onStartRecording();
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    LocationProvider mLocationProvider = null;

    private LocationProvider getLocationProvider() {
        String bestProvider = this.locationManager.getBestProvider(getCriteria(), false);
        if (bestProvider != null) {
            return this.locationManager.getProvider(bestProvider);
        }
        Log.w(TAG, "Location provider name is not returned based on the criteria, this device does not support necessary location!");
        return null;
    }


    public static Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        return criteria;
    }


    @Override
    public void onDestroy() {
        mTimer.cancel();
        Log.d("ZHealth", "RunningActivityService::onDestroy()");

        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION_CODE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void updateNotification() {
        CharSequence appName = getResources().getString(R.string.app_name);;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RunningActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_value_steps)  // the status icon
                .setTicker(appName)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getNotificationContentTitle())  // the label of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        startForeground(NOTIFICATION_CODE, notification);
    }

    private String getNotificationContentTitle() {
        return String.format("Run · %s · %s %s", Helper.formatDuration(TimeHelper.MillisToSecond(mDuration)), Helper.formatDistance(runningController.getDistance()), "km");
    }

    private void removeLocationListener() {
        if (this.mLocationListenerWrapper != null) {
            this.locationManager.removeUpdates(mLocationListenerWrapper);
            this.locationManager.removeGpsStatusListener(mLocationListenerWrapper);
            this.mLocationListenerWrapper = null;
        }
    }

    private void addLocationListener() {
        mLocationProvider = getLocationProvider();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationListenerWrapper = new LocationListenerWrapper(this, this);

        if (locationManager.getProvider("fused") != null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationManager.requestLocationUpdates(1000, 0.0f, criteria, mLocationListenerWrapper, Looper.getMainLooper());
        } else {
            locationManager.requestLocationUpdates(mLocationProvider.getName(), 0, 0.0f, mLocationListenerWrapper);
        }

        locationManager.addGpsStatusListener(mLocationListenerWrapper);
    }
}