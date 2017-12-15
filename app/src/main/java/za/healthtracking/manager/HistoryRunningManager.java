package za.healthtracking.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import za.healthtracking.models.RunningBucket.RunningBucket;
import za.healthtracking.models.RunningBucket.RunningBucketDay;
import za.healthtracking.models.RunningBucket.RunningBucketMonth;
import za.healthtracking.models.RunningBucket.RunningBucketWeek;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 07/08/2017.
 */

public class HistoryRunningManager {
    public final static int DISABLE_PER_DATE_OFFSET = 3;
    public final static int DISABLE_PER_WEEK_OFFSET = 3;
    public final static int DISABLE_PER_MONTH_OFFSET = 3;

    public List<RunningBucket> mRunningBucketDayList;
    public List<RunningBucket> mRunningBucketWeekList;
    public List<RunningBucket> mRunningBucketMonthList;
    HistoryDataManager mHistoryDataManager;

    public interface Listener {
        void onSuccess();
    }

    public HistoryRunningManager() {
        mHistoryDataManager = new HistoryDataManager();

        mRunningBucketDayList = new ArrayList<>();
        mRunningBucketWeekList = new ArrayList<>();
        mRunningBucketMonthList = new ArrayList<>();
    }

    public void getInitFitnessPerDate(final Listener listener) {
        if (mRunningBucketDayList.size() == 0) {
            List<RunningBucketDay> runningBucketDayList = mHistoryDataManager.getRunningBucketDays(Calendar.getInstance().getTimeInMillis(), 30);

            int nextDays = DISABLE_PER_DATE_OFFSET;
            Date now = new Date();
            for (int i = nextDays; i >= 1; i--) {
                mRunningBucketDayList.add(new RunningBucketDay(TimeHelper.addDateWithDays(now, i), TimeHelper.addDateWithDays(now, i + 1), null, false));
            }

            for (int i = runningBucketDayList.size() - 1; i >= 0; i--) {
                mRunningBucketDayList.add(runningBucketDayList.get(i));
            }

            listener.onSuccess();
        } else {
            listener.onSuccess();
        }
    }

    public void getInitFitnessPerWeek(final Listener listener) {
        if (mRunningBucketWeekList.size() == 0) {
            List<RunningBucketWeek> runningBucketWeekList = mHistoryDataManager.getRunningBucketWeeks(Calendar.getInstance().getTimeInMillis(), 30);

            int nextDays = DISABLE_PER_WEEK_OFFSET;
            int DAY_INTERVAL = 7;
            Date now = new Date();
            for (int i = nextDays; i >= 1; i--) {
                mRunningBucketWeekList.add(new RunningBucketWeek(TimeHelper.addDateWithDays(now, i*DAY_INTERVAL), TimeHelper.addDateWithDays(now, i*DAY_INTERVAL + DAY_INTERVAL-1), null, false));
            }

            for (int i = runningBucketWeekList.size() - 1; i >= 0; i--) {
                mRunningBucketWeekList.add(runningBucketWeekList.get(i));
            }

            listener.onSuccess();
        } else {
            listener.onSuccess();
        }
    }

    public void getInitFitnessPerMonths(final Listener listener) {
        if (mRunningBucketMonthList.size() == 0) {
            List<RunningBucketMonth> runningBucketMonthList = mHistoryDataManager.getRunningBucketMonths(Calendar.getInstance().getTimeInMillis(), 12);

            Date now = new Date();
            int nextMonths = DISABLE_PER_MONTH_OFFSET;
            List<Long> firstDayOfNextMonths = TimeHelper.getFirstDayOfNextMonthsTimestamp(now.getTime(), nextMonths);
            for (int i = firstDayOfNextMonths.size() - 1; i >= 0; i--) {
                mRunningBucketMonthList.add(new RunningBucketMonth(new Date(firstDayOfNextMonths.get(i)), new Date(firstDayOfNextMonths.get(i)), null, false));
            }

            for (int i = runningBucketMonthList.size() - 1; i >= 0; i--) {
                mRunningBucketMonthList.add(runningBucketMonthList.get(i));
            }

            listener.onSuccess();

        } else {
            listener.onSuccess();
        }
    }

    public void getMoreFitnessPerDates(final Listener listener) {
        Date nextDate = TimeHelper.addDateWithDays(mRunningBucketDayList.get(mRunningBucketDayList.size()-1).mStartDate, -1);
        List<RunningBucketDay> runningBucketDayList = mHistoryDataManager.getRunningBucketDays(nextDate.getTime(), 30);

        for (int i = runningBucketDayList.size() - 1; i >= 0; i--) {
            mRunningBucketDayList.add(runningBucketDayList.get(i));
        }

        listener.onSuccess();
    }

    public void getMoreFitnessPerWeeks(final Listener listener) {
        Date nextDate = TimeHelper.addDateWithDays(mRunningBucketWeekList.get(mRunningBucketWeekList.size()-1).mStartDate, -7);
        List<RunningBucketWeek> runningBucketDayList = mHistoryDataManager.getRunningBucketWeeks(nextDate.getTime(), 30);

        for (int i = runningBucketDayList.size() - 1; i >= 0; i--) {
            mRunningBucketWeekList.add(runningBucketDayList.get(i));
        }

        listener.onSuccess();
    }

    public void getMoreFitnessPerMonths(final Listener listener) {
        Date nextDate = TimeHelper.addMonths(mRunningBucketMonthList.get(mRunningBucketMonthList.size()-1).mStartDate, -1);
        List<RunningBucketMonth> runningBucketMonthsList = mHistoryDataManager.getRunningBucketMonths(nextDate.getTime(), 30);

        for (int i = runningBucketMonthsList.size() - 1; i >= 0; i--) {
            mRunningBucketMonthList.add(runningBucketMonthsList.get(i));
        }

        listener.onSuccess();
    }
}
