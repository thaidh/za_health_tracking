package za.healthtracking.pedometer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import za.healthtracking.app.MyApplication;
import za.healthtracking.app.Settings;
import za.healthtracking.database.DbHelper;
import za.healthtracking.database.entities.DailyActivityLog;
import za.healthtracking.database.entities.MinutelyActivityLog;
import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.ui.MainActivity;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 14/07/2017.
 */

public class SessionManager implements SessionCaloriesManager.Listener {
    private long stepDetectionMaxBatchReportLatencySeconds = 3000;
    private SessionCaloriesManager mSessionCaloriesManager;
    private StepData mLastStep;

    private List<StepData> mConsecutiveStepDataList;
    private float mCurrentStepLength;
    private float mDefaultStepLength;
    private StepFilter mStepFilter;

    private DbHelper mDBHelper = new DbHelper(MyApplication.getAppContext());
    private Dao<MinutelyActivityLog, Integer> mMinutelyActivityLogDao = null;
    private Dao<DailyActivityLog, Integer> mDailyActivityLogDao = null;

    private DailyActivityLog mDailyActivityLogToday; // Used to display real-time
    private MinutelyActivityLog mMinutelyActivityLogCurrent; // Used to display real-time

    private long mSessionStartTime = 0;

    private List<MinutelyActivityLog> mMinutelyActivityLogsToDay;


    public SessionManager() {
        mSessionStartTime = Calendar.getInstance().getTimeInMillis();

        mStepFilter = new StepFilter(StepFilter.STANDARD);

        mSessionCaloriesManager = new SessionCaloriesManager(this);

        mDefaultStepLength = Settings.defaultStepLength;
        mCurrentStepLength = this.mDefaultStepLength;

        resetRecentlySteps();

        // Init DB
        try {
            mMinutelyActivityLogDao = mDBHelper.getMinutelyActivityLogDao();
            mDailyActivityLogDao = mDBHelper.getDailyActivityLogDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load current data from db
        loadCurrentDataFromDB();

        // Event mHandler interval 1 second
        mTimerEventHandler.sendMessageDelayed(Message.obtain(mTimerEventHandler, 0), 0);
    }

    public DailyActivityLog getDailyActivityLogToday() {
        return mDailyActivityLogToday;
    }


    private void loadCurrentDataFromDB() {
        long now = Calendar.getInstance().getTimeInMillis();

        // Get Data From Daily
        long midnightToday = TimeHelper.getStartTimeOnThisDayTimestamp(now);
        mDailyActivityLogToday = getDailyActivityLog(TimeHelper.MillisToSecond(midnightToday));

        // Get Data From Current 20 min
        long current20MinuteStartTime = TimeHelper.getCurrentMinuteBlockStartTime(now);
        mMinutelyActivityLogCurrent = loadMinutelyActivityLog(TimeHelper.MillisToSecond(current20MinuteStartTime));

        // Load minutely blocks today
        mMinutelyActivityLogsToDay = getMinutelyBockListForADay(now);
    }

    private DailyActivityLog getDailyActivityLog(int startTime) {
        if (!mDBHelper.isOpen()) {
            Log.d("ZHealth", "DB is close!");
            return null;
        }

        QueryBuilder queryBuilder = mDailyActivityLogDao.queryBuilder();
        try {
            queryBuilder.where().eq(DailyActivityLog.STARTTIME_FIELD_NAME, startTime);
            DailyActivityLog dailyActivityLog = mDailyActivityLogDao.queryForFirst(queryBuilder.prepare());
            if (dailyActivityLog == null) {
                dailyActivityLog = DailyActivityLog.newInstance(TimeHelper.SecondToMillis(startTime));
            }
            return dailyActivityLog;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<MinutelyActivityLog> getMinutelyBockListForADay(long timestamp) {
        int dailyStartTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnThisDayTimestamp(timestamp));
        int dailyEndTime = dailyStartTime + 86400 - 1;

        QueryBuilder queryBuilder = mMinutelyActivityLogDao.queryBuilder();
        try {
            queryBuilder.where().between(MinutelyActivityLog.STARTTIME_FIELD_NAME, dailyStartTime, dailyEndTime);
            return mMinutelyActivityLogDao.query(queryBuilder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FitnessBucket> getBucketsForADay(long timestamp) {
        List<MinutelyActivityLog> logs = getMinutelyBockListForADay(timestamp);
        List<FitnessBucket> buckets = new ArrayList<>();

        int minutes = 0;
        int startTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnThisDayTimestamp(timestamp));

        while (minutes < 1440) {
            boolean isOke = false;

            int currentTimeInSecond = startTime + minutes*60;
            for (MinutelyActivityLog item : logs) {
                if (item.startTime == currentTimeInSecond ) {
                    buckets.add(new FitnessBucket(item.steps, item.distanceInMeters, item.calories, TimeHelper.SecondToMillis(item.startTime), TimeHelper.SecondToMillis(item.endTime)));
                    isOke = true;
                    break;
                }

            }

            if (!isOke) {
                buckets.add(new FitnessBucket(0, 0, 0, TimeHelper.SecondToMillis(currentTimeInSecond), TimeHelper.SecondToMillis(currentTimeInSecond + 19*60+59)));
            }

            minutes += 20;
        }

        return buckets;
    }


    private MinutelyActivityLog loadMinutelyActivityLog(int startTime) {
        if (!mDBHelper.isOpen()) {
            Log.d("ZHealth", "DB is close!");
            return null;
        }

        Dao<MinutelyActivityLog, Integer> dao = mMinutelyActivityLogDao;

        QueryBuilder queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(MinutelyActivityLog.STARTTIME_FIELD_NAME, startTime);
            MinutelyActivityLog minutelyActivityLog = (MinutelyActivityLog) dao.queryForFirst(queryBuilder.prepare());
            if (minutelyActivityLog == null) {
                minutelyActivityLog = MinutelyActivityLog.newInstance(startTime * 1000);
            }
            return minutelyActivityLog;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private synchronized void updateMinutelyActivityLog() {
        Dao<MinutelyActivityLog, Integer> dao = mMinutelyActivityLogDao;

        if (!mDBHelper.isOpen()) {
            Log.d("ZHealth", "DB is close!");
            return;
        }

        QueryBuilder queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq("startTime", mMinutelyActivityLogCurrent.startTime);
            MinutelyActivityLog minutelyActivityLog = dao.queryForFirst(queryBuilder.prepare());
            if (minutelyActivityLog != null) {
                minutelyActivityLog.steps = mMinutelyActivityLogCurrent.steps;
                minutelyActivityLog.calories = mMinutelyActivityLogCurrent.calories;
                minutelyActivityLog.distanceInMeters = mMinutelyActivityLogCurrent.distanceInMeters;
                dao.update(minutelyActivityLog);
            } else {
                dao.create(mMinutelyActivityLogCurrent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateDailyActivityLog() {
        Dao<DailyActivityLog, Integer> dao = mDailyActivityLogDao;

        if (!mDBHelper.isOpen()) {
            Log.d("ZHealth", "DB is close!");
            return;
        }

        QueryBuilder queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(DailyActivityLog.STARTTIME_FIELD_NAME, mDailyActivityLogToday.startTime);
            DailyActivityLog dailyActivityLog = (DailyActivityLog) dao.queryForFirst(queryBuilder.prepare());
            if (dailyActivityLog != null) {
                dailyActivityLog.steps = mDailyActivityLogToday.steps;
                dailyActivityLog.calories = mDailyActivityLogToday.calories;
                dailyActivityLog.distanceInMeters = mDailyActivityLogToday.distanceInMeters;
                dao.update(dailyActivityLog);
            } else {
                dao.create(mDailyActivityLogToday);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mLastStep != null && msg.obj.equals(mLastStep)) {
                resetRecentlySteps();
                updateSpeed();
                updateStepFrequency();
            }
        }
    };

    private final Handler mTimerEventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            onEvent(Calendar.getInstance().getTimeInMillis());
            mTimerEventHandler.sendMessageDelayed(Message.obtain(mTimerEventHandler, 0), 1000);
        }
    };

    private void onEvent(long timestamp) {
        int second = TimeHelper.MillisToSecond(timestamp);
        Log.d("ZHealth", String.format("%s", second));

        onSessionTimeChanged(timestamp-mSessionStartTime);

        if (second >= mDailyActivityLogToday.startTime + 86400) { // New day
            Log.d("ZHealth", "on Newday!");

            // save old
            updateDailyActivityLog();
            updateMinutelyActivityLog();

            // new instance
            mMinutelyActivityLogCurrent = MinutelyActivityLog.newInstance(timestamp);
            mDailyActivityLogToday = DailyActivityLog.newInstance(timestamp);

            // load minutes blocks
            mMinutelyActivityLogsToDay = getMinutelyBockListForADay(timestamp);
        } else if (second >= mMinutelyActivityLogCurrent.startTime + Settings.INTERVAL_ACTIVITY) { // New minute block
            Log.d("ZHealth", "onNew minute block!");

            // save old
            updateMinutelyActivityLog();

            // new instance
            mMinutelyActivityLogCurrent = MinutelyActivityLog.newInstance(timestamp);

            // load minutes blocks
            mMinutelyActivityLogsToDay = getMinutelyBockListForADay(timestamp);
        }

        // update database per 1 minute
        if (second % 60 == 0) {
            Log.d("ZHealth", "Update per a minute");

            updateMinutelyActivityLog();
            updateDailyActivityLog();

            // load minutes blocks
            mMinutelyActivityLogsToDay = getMinutelyBockListForADay(timestamp);
        }
    }

    private void resetRecentlySteps() {
        mConsecutiveStepDataList = new ArrayList(10);
        mLastStep = null;
    }

    private void onSessionTimeChanged(long duration) {
        mSessionCaloriesManager.shouldCalCalories(duration);
    }

    // Input
    private void onNewStep(StepData stepData) {
        mLastStep = stepData;

        addRecentlyStepData(stepData);
        updateSpeed();
        updateStepFrequency();

        onAddStepCountAndDistance(stepData.getStepCount());

        mSessionCaloriesManager.addStepCounterAndDistance(stepData.getStepCount(), stepData.getStepCount() * mCurrentStepLength);

        mHandler.sendMessageDelayed(Message.obtain(mHandler, 0, mLastStep), this.stepDetectionMaxBatchReportLatencySeconds);
    }

    private void onAddStepCountAndDistance(int deltaStepCount) {
        // Distance
        mDailyActivityLogToday.distanceInMeters += deltaStepCount * mCurrentStepLength;
        mMinutelyActivityLogCurrent.distanceInMeters += deltaStepCount * mCurrentStepLength;

        // Step counter
        mDailyActivityLogToday.steps += deltaStepCount;
        mMinutelyActivityLogCurrent.steps += deltaStepCount;

        onCurrentMinuteBlockDataChanged();
    }


    private void updateSpeed() {
        //this.mCurrentSessionViewModel.setSpeed(calcSpeed());
    }

    private void updateStepFrequency() {
        short stepFrequency = (short) ((int) calcStepFrequency());
        if (stepFrequency <= (short) 90) {
            this.mCurrentStepLength = this.mDefaultStepLength * 0.85f;
        } else if (stepFrequency <= (short) 120) {
            this.mCurrentStepLength = this.mDefaultStepLength * 1.0f;
        } else if (stepFrequency <= (short) 140) {
            this.mCurrentStepLength = this.mDefaultStepLength * 1.11f;
        } else if (stepFrequency <= (short) 160) {
            this.mCurrentStepLength = this.mDefaultStepLength * 1.25f;
        } else {
            this.mCurrentStepLength = this.mDefaultStepLength * 1.4f;
        }

        //
        try {
            MainActivity.txtStepLength.setText(mCurrentStepLength + " m");
            MainActivity.txtStepFrequency.setText(stepFrequency + " steps/min");
            if (stepFrequency < 50) {
                MainActivity.txtUserState.setText("User is very lame");
            } else if (stepFrequency < 150) {
                MainActivity.txtUserState.setText("User is walking!");
            } else {
                MainActivity.txtUserState.setText("User is running");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRecentlyStepData(StepData stepData) {
        if (this.mConsecutiveStepDataList.size() < 10) {
            this.mConsecutiveStepDataList.add(stepData);
            return;
        }
        this.mConsecutiveStepDataList.remove(0);
        this.mConsecutiveStepDataList.add(stepData);
    }

    private void onCurrentMinuteBlockDataChanged() {
        if (mMinutelyActivityLogsToDay != null) {
            for (MinutelyActivityLog log : mMinutelyActivityLogsToDay) {
                if (mMinutelyActivityLogCurrent.startTime == log.startTime) {
                    log.steps = mMinutelyActivityLogCurrent.steps;
                    log.calories = mMinutelyActivityLogCurrent.calories;
                    log.distanceInMeters = mMinutelyActivityLogCurrent.distanceInMeters;
                    break;
                }
            }
        }
    }

    private double calcStepFrequency() { //steps per min
        if (mConsecutiveStepDataList.size() < 3) {
            return 0.0d;
        }
        long duration = getRecentlyDuration();
        if (duration > 0) {
            return (double) ((60.0f / (duration / 1000.0f)) * getRecentlySteps());
        }
        return 0.0d;
    }

    private int getRecentlySteps() {
        int steps = 1;
        for (int i = 1; i < mConsecutiveStepDataList.size(); i++) {
            steps += mConsecutiveStepDataList.get(i).getStepCount();
        }
        return steps;
    }

    private long getRecentlyDuration() {
        return mConsecutiveStepDataList.get(mConsecutiveStepDataList.size() - 1).getTimestamp() - mConsecutiveStepDataList.get(0).getTimestamp();
    }

    @Override
    public void addCalories(float deltaCalories) {
        mMinutelyActivityLogCurrent.calories += deltaCalories;
        mDailyActivityLogToday.calories += deltaCalories;

        onCurrentMinuteBlockDataChanged();
    }


    private int SKIP_STEP_INTERVAL = 1200;
    private int SKIP_STEP_COUNTER = 6;
    private long mLastStepTimestamp = 0;
    private int mTempSteps = 0;

    public void onSensorValueReceived(StepData stepdata, float x, float y, float z) {
        if (mStepFilter.isNewStep(x, y, z)) {
            Log.d("HiepIT", "isNewStep: true");

            // Filter Skip Step
            long now = Calendar.getInstance().getTimeInMillis();
            if (now - mLastStepTimestamp >= SKIP_STEP_INTERVAL) {
                mTempSteps = 1;
            } else {
                mTempSteps++;
                if (mTempSteps == SKIP_STEP_COUNTER) {
                    stepdata.setStepCount(SKIP_STEP_COUNTER);
                    onNewStep(stepdata);
                } else if (mTempSteps > SKIP_STEP_COUNTER) {
                    onNewStep(stepdata);
                }
            }
            mLastStepTimestamp = now;
        }
    }

    public void onSensorNewStep(StepData stepdata) {
        onNewStep(stepdata);
    }

    public void stopSession() {
        // Cal calories before stopping
        mSessionCaloriesManager.calcCalories();

        // Save to db
        updateMinutelyActivityLog();
        updateDailyActivityLog();
    }
}
