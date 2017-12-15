package za.healthtracking.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import za.healthtracking.app.MyApplication;
import za.healthtracking.database.DbHelper;
import za.healthtracking.database.entities.DailyActivityLog;
import za.healthtracking.database.entities.MinutelyActivityLog;
import za.healthtracking.database.entities.RunningActivityLog;
import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.models.RunningBucket.RunningBucketDay;
import za.healthtracking.models.RunningBucket.RunningBucketMonth;
import za.healthtracking.models.RunningBucket.RunningBucketWeek;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 07/08/2017.
 */

public class HistoryDataManager {
    private DbHelper dbHelper = new DbHelper(MyApplication.getAppContext());
    private Dao<MinutelyActivityLog, Integer> minutelyActivityLogDao = null;
    private Dao<DailyActivityLog, Integer> dailyActivityLogDao = null;
    private Dao<RunningActivityLog, Integer> runningActivityLogDao = null;

    HistoryDataManager() {
        // Init DB
        try {
            minutelyActivityLogDao = dbHelper.getMinutelyActivityLogDao();
            dailyActivityLogDao = dbHelper.getDailyActivityLogDao();
            runningActivityLogDao = dbHelper.getRunningActivityLogDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<RunningActivityLog> getRunningActivityLogs(int startTime, int endTime) {
        QueryBuilder queryBuilder = runningActivityLogDao.queryBuilder();
        try {
            queryBuilder.where().between(RunningActivityLog.STARTTIME_FIELD_NAME, startTime, endTime);
            return runningActivityLogDao.query(queryBuilder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RunningBucketDay> getRunningBucketDays(long timestamp, int numberOfDays) {
        int startTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnThisDayTimestamp(timestamp)) - (numberOfDays-1) * 86400;
        int endTime = TimeHelper.MillisToSecond(timestamp);

        List<RunningActivityLog> logs = getRunningActivityLogs(startTime, endTime);

        List<RunningBucketDay> results = new ArrayList<>();
        int _startTime = startTime;
        int _endTime = _startTime + 86400 - 1;
        for (int i = 0; i < numberOfDays; i++ ) {
            List<RunningActivityLog> subLogs = new ArrayList<>();

            for (RunningActivityLog log : logs ) {
                if (log.startTime >= _startTime && log.startTime <= _endTime) {
                    subLogs.add(log);
                }
            }

            results.add(new RunningBucketDay(new Date(TimeHelper.SecondToMillis(_startTime)), new Date(TimeHelper.SecondToMillis(_endTime)), subLogs));

            // Go next day
            _startTime += 86400;
            _endTime = _startTime + 86400 - 1;
        }

        return results;
    }

    public List<RunningBucketWeek> getRunningBucketWeeks(long timestamp, int numberOfWeeks) {
        int startTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnMondayThisWeekTimestamp(timestamp)) - (numberOfWeeks-1) * 86400 * 7;
        int endTime = TimeHelper.MillisToSecond(timestamp);

        List<RunningActivityLog> logs = getRunningActivityLogs(startTime, endTime);

        List<RunningBucketWeek> results = new ArrayList<>();
        int _startTime = startTime;
        int _endTime = _startTime + 86400 * 7 - 1;
        for (int i = 0; i < numberOfWeeks; i++ ) {
            List<RunningActivityLog> subLogs = new ArrayList<>();
            for (RunningActivityLog log : logs ) {
                if (log.startTime >= _startTime && log.startTime <= _endTime) {
                    subLogs.add(log);
                }
            }

            results.add(new RunningBucketWeek(new Date(TimeHelper.SecondToMillis(_startTime)), new Date(TimeHelper.SecondToMillis(_endTime)), subLogs));

            // Go next week
            _startTime += 86400 * 7;
            _endTime = _startTime + 86400 * 7 - 1;
        }

        return results;
    }

    public List<RunningBucketMonth> getRunningBucketMonths(long timestamp, int numberOfMonths) {
        List<Long> startTimes = TimeHelper.getFirstDayOfPrevMonthsTimestampInclusive(timestamp, numberOfMonths);
        int startTime = TimeHelper.MillisToSecond(startTimes.get(0));
        int endTime = TimeHelper.MillisToSecond(timestamp);

        List<RunningActivityLog> logs = getRunningActivityLogs(startTime, endTime);

        List<RunningBucketMonth> results = new ArrayList<>();
        for (int i = 0; i < numberOfMonths; i++ ) {
            int _startTime = TimeHelper.MillisToSecond(startTimes.get(i));
            int _endTime = TimeHelper.MillisToSecond(timestamp);
            if (i < numberOfMonths - 1) {
                _endTime = TimeHelper.MillisToSecond(startTimes.get(i+1)) - 1;
            }

            List<RunningActivityLog> subLogs = new ArrayList<>();
            for (RunningActivityLog log : logs ) {
                if (log.startTime >= _startTime && log.startTime <= _endTime) {
                    subLogs.add(log);
                }
            }

            results.add(new RunningBucketMonth(new Date(TimeHelper.SecondToMillis(_startTime)), new Date(TimeHelper.SecondToMillis(_endTime)), subLogs));
        }

        return results;
    }

    // DailyActivityLog

    private List<DailyActivityLog> getDailyActivityLogs(int startTime, int endTime) {
        QueryBuilder queryBuilder = dailyActivityLogDao.queryBuilder();
        try {
            queryBuilder.where().between(DailyActivityLog.STARTTIME_FIELD_NAME, startTime, endTime);
            return dailyActivityLogDao.query(queryBuilder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<DailyActivityLog> getDailyActivityLogs(long timestamp, int numberOfDays) {
        int startTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnThisDayTimestamp(timestamp)) - 86400 * (numberOfDays-1);
        int endTime = TimeHelper.MillisToSecond(timestamp);

        return getDailyActivityLogs(startTime, endTime);
    }

    // Fitness Bucket

    public List<FitnessBucket> getFitnessBucketPerDays(long timestamp, int numberOfDays) {
        List<DailyActivityLog> dailyActivityLogList = getDailyActivityLogs(timestamp, numberOfDays);
        int startTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnThisDayTimestamp(timestamp));

        List<FitnessBucket> buckets = new ArrayList<>();
        for (int i = 0; i < numberOfDays; i++) {
            long _startTime = TimeHelper.SecondToMillis(startTime - i*86400);
            long _endTime = TimeHelper.SecondToMillis(startTime - (i-1)*86400 - 1);

            FitnessBucket bucket = new FitnessBucket(0, 0, 0, _startTime, _endTime);
            for (DailyActivityLog log : dailyActivityLogList) {
                if (log.startTime == TimeHelper.MillisToSecond(bucket.startTime)) {
                    bucket.activeDays = 1;
                    bucket.nSteps = log.steps;
                    bucket.distance = log.distanceInMeters;
                    bucket.caloriesBurned = log.calories;
                    break;
                }
            }

            buckets.add(0, bucket);
        }

        return buckets;
    }

    public List<FitnessBucket> getBucketPerWeeks(long timestamp, int numberOfWeeks) {
        // Thong ke theo tuan bat dau tu` thu 2 cho moi~ tuan

        // Step 1: Tinh startTime cua tuan cuoi cung`
        int startTimeOnMondayThisWeek = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnMondayThisWeekTimestamp(timestamp));
        int startTime = startTimeOnMondayThisWeek - (numberOfWeeks-1)*7*86400; //start time tuan cuoi cung
        int endTime = TimeHelper.MillisToSecond(timestamp); // end time

        List<DailyActivityLog> dailyActivityLogList = getDailyActivityLogs(startTime, endTime);

        List<FitnessBucket> buckets = new ArrayList<>();
        for (int i = 0; i < numberOfWeeks; i++) {
            FitnessBucket bucket = new FitnessBucket(0, 0, 0, TimeHelper.SecondToMillis(startTime + i*86400*7), TimeHelper.SecondToMillis(startTime + (i+1)*86400*7 - 1));

            for (DailyActivityLog log : dailyActivityLogList) {
                if (log.startTime >= TimeHelper.MillisToSecond(bucket.startTime) && log.startTime <= TimeHelper.MillisToSecond(bucket.endTime)) {
                    bucket.caloriesBurned += log.calories;
                    bucket.nSteps += log.steps;
                    bucket.distance += log.distanceInMeters;
                    bucket.activeDays++;
                }
            }

            buckets.add(bucket);
        }

        return buckets;
    }

    public List<FitnessBucket> getBucketPerMonths(long timestamp, int numberOfMonths) {
        // Thong ke theo thang bat dau tu ngay 1 moi thang

        // Step 1: Tao ra list startTime cua numberOfMonths cua cac thang
        List<Long> firstDayOfMonths = TimeHelper.getFirstDayOfPrevMonthsTimestampInclusive(timestamp, numberOfMonths);
        firstDayOfMonths.add(timestamp);

        // Step 2: Lay start time, && endTime
        int startTime = TimeHelper.MillisToSecond(firstDayOfMonths.get(0));
        int endTime = TimeHelper.MillisToSecond(timestamp);

        // Step 3: Dua vao list de filter
        List<DailyActivityLog> dailyActivityLogList = getDailyActivityLogs(startTime, endTime);

        List<FitnessBucket> buckets = new ArrayList<>();
        for (int i = 0; i < numberOfMonths; i++) {
            FitnessBucket bucket = new FitnessBucket(0, 0, 0, firstDayOfMonths.get(i), firstDayOfMonths.get(i+1)-1000);

            for (DailyActivityLog log : dailyActivityLogList) {
                if (log.startTime >= TimeHelper.MillisToSecond(bucket.startTime) && log.startTime <= TimeHelper.MillisToSecond(bucket.endTime)) {
                    bucket.caloriesBurned += log.calories;
                    bucket.nSteps += log.steps;
                    bucket.distance += log.distanceInMeters;
                    bucket.activeDays++;
                }
            }

            buckets.add(bucket);
        }

        return buckets;
    }

    // MinuteActivityLog
    private List<MinutelyActivityLog> getMinutelyActivityLogsForADay(long timestamp) {
        int dailyStartTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnThisDayTimestamp(timestamp));
        int dailyEndTime = dailyStartTime + 86400 - 1;

        QueryBuilder queryBuilder = minutelyActivityLogDao.queryBuilder();
        try {
            queryBuilder.where().between(MinutelyActivityLog.STARTTIME_FIELD_NAME, dailyStartTime, dailyEndTime);
            return minutelyActivityLogDao.query(queryBuilder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FitnessBucket> getFitnessBucketsPer20MinForADay(long timestamp) {
        List<MinutelyActivityLog> logs = getMinutelyActivityLogsForADay(timestamp);

        List<FitnessBucket> buckets = new ArrayList<>();
        int minutes = 0;
        int startTime = TimeHelper.MillisToSecond(TimeHelper.getStartTimeOnThisDayTimestamp(timestamp));

        while (minutes < 1440) {
            boolean isOke = false;

            int currentTimeInSecond = startTime + minutes * 60;
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
}
