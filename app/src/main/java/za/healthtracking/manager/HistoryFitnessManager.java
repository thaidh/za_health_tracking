package za.healthtracking.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import za.healthtracking.models.FitnessBucket.BaseFitnessBucket;
import za.healthtracking.models.FitnessBucket.FitnessBucket;
import za.healthtracking.models.FitnessBucket.FitnessBucketDay;
import za.healthtracking.models.FitnessBucket.FitnessBucketMonth;
import za.healthtracking.models.FitnessBucket.FitnessBucketWeek;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 23/05/2017.
 */

public class HistoryFitnessManager {
    public final static int DISABLE_PER_DATE_OFFSET = 3;
    public final static int DISABLE_PER_WEEK_OFFSET = 3;
    public final static int DISABLE_PER_MONTH_OFFSET = 3;

    public List<BaseFitnessBucket> mFitnessPerDateDataSet;
    public List<BaseFitnessBucket> mFitnessPerWeekDataSet;
    public List<BaseFitnessBucket> mFitnessPerMonthDataSet;
    private HistoryDataManager mHistoryDataManager;

    public interface HistoryFitnessManagerListener {
        void onSuccess();
    }

    public HistoryFitnessManager() {
        mFitnessPerDateDataSet = new ArrayList<>();
        mFitnessPerWeekDataSet = new ArrayList<>();
        mFitnessPerMonthDataSet = new ArrayList<>();

        mHistoryDataManager = new HistoryDataManager();
    }

    public void getInitFitnessPerDate(final HistoryFitnessManagerListener listener) {
        if (mFitnessPerDateDataSet.size() == 0) {
            List<FitnessBucket> buckets = mHistoryDataManager.getFitnessBucketPerDays(Calendar.getInstance().getTimeInMillis(), 30);
            Date now = new Date();
            int nextDays = DISABLE_PER_DATE_OFFSET;
            for (int i = nextDays; i >= 1; i--) {
                mFitnessPerDateDataSet.add(new FitnessBucketDay(TimeHelper.addDateWithDays(now, i), TimeHelper.addDateWithDays(now, i + 1), 0, 0, 0, false));
            }

            for (int i = buckets.size() - 1; i >= 0; i--) {
                FitnessBucket item = buckets.get(i);
                mFitnessPerDateDataSet.add(new FitnessBucketDay(new Date(item.startTime), new Date(item.endTime), item.nSteps, item.distance, item.caloriesBurned, true));
            }

            listener.onSuccess();
        } else {
            listener.onSuccess();
        }
    }

    public void getInitFitnessPerWeek(final HistoryFitnessManagerListener listener) {
        if (mFitnessPerWeekDataSet.size() == 0) {
            List<FitnessBucket> buckets = mHistoryDataManager.getBucketPerWeeks(Calendar.getInstance().getTimeInMillis(), 30);

            Date now = new Date();
            int nextWeeks = DISABLE_PER_WEEK_OFFSET;
            int DAY_INTERVAL = 7;
            for (int i = nextWeeks; i >= 1; i--) {
                mFitnessPerWeekDataSet.add(new FitnessBucketWeek(TimeHelper.addDateWithDays(now, i*DAY_INTERVAL), TimeHelper.addDateWithDays(now, i*DAY_INTERVAL + DAY_INTERVAL-1 ), 0, 0, 0, false));
            }

            for (int i = buckets.size() - 1; i >= 0; i--) {
                FitnessBucket item = buckets.get(i);
                mFitnessPerWeekDataSet.add(new FitnessBucketWeek(new Date(item.startTime), new Date(item.endTime), item.getAvgSteps(), item.getAvgDistance(), item.getAvgCalories(), true));
            }

            listener.onSuccess();
        } else {
            listener.onSuccess();
        }
    }

    public void getInitFitnessPerMonths(final HistoryFitnessManagerListener listener) {
        if (mFitnessPerMonthDataSet.size() == 0) {
            List<FitnessBucket> buckets = mHistoryDataManager.getBucketPerMonths(Calendar.getInstance().getTimeInMillis(), 12);

            Date now = new Date();
            int nextMonths = DISABLE_PER_MONTH_OFFSET;
            List<Long> firstDayOfNextMonths = TimeHelper.getFirstDayOfNextMonthsTimestamp(now.getTime(), nextMonths);
            for (int i = firstDayOfNextMonths.size() - 1; i >= 0; i--) {
                mFitnessPerMonthDataSet.add(new FitnessBucketMonth(new Date(firstDayOfNextMonths.get(i)), new Date(firstDayOfNextMonths.get(i)), 0, 0, 0, false));
            }

            for (int i = buckets.size() - 1; i >= 0; i--) {
                FitnessBucket item = buckets.get(i);
                mFitnessPerMonthDataSet.add(new FitnessBucketMonth(new Date(item.startTime), new Date(item.endTime), item.getAvgSteps(), item.getAvgDistance(), item.getAvgCalories(), true));
            }

            listener.onSuccess();
        } else {
            listener.onSuccess();
        }
    }

    public void getMoreFitnessPerDates(final HistoryFitnessManagerListener listener) {
        Date nextDate = TimeHelper.addDateWithDays(mFitnessPerDateDataSet.get(mFitnessPerDateDataSet.size()-1).mStartDate, -1);
        List<FitnessBucket> buckets = mHistoryDataManager.getFitnessBucketPerDays(nextDate.getTime(), 30);
        for (int i = buckets.size() - 1; i >= 0; i--) {
            FitnessBucket item = buckets.get(i);
            mFitnessPerDateDataSet.add(new FitnessBucketDay(new Date(item.startTime), new Date(item.endTime), item.nSteps, item.distance, item.caloriesBurned, true));
        }
        listener.onSuccess();
    }

    public void getMoreFitnessPerWeeks(final HistoryFitnessManagerListener listener) {
        Date nextDate = TimeHelper.addDateWithDays(mFitnessPerWeekDataSet.get(mFitnessPerWeekDataSet.size()-1).mStartDate, -7);
        List<FitnessBucket> buckets = mHistoryDataManager.getBucketPerWeeks(nextDate.getTime(), 30);
        for (int i = buckets.size() - 1; i >= 0; i--) {
            FitnessBucket item = buckets.get(i);
            mFitnessPerWeekDataSet.add(new FitnessBucketWeek(new Date(item.startTime), new Date(item.endTime), item.getAvgSteps(), item.getAvgDistance(), item.getAvgCalories(), true));
        }

        listener.onSuccess();
    }

    public void getMoreFitnessPerMonths(final HistoryFitnessManagerListener listener) {
        Date nextDate = TimeHelper.addMonths(mFitnessPerMonthDataSet.get(mFitnessPerMonthDataSet.size()-1).mStartDate, -1);
        List<FitnessBucket> buckets = mHistoryDataManager.getBucketPerMonths(nextDate.getTime(), 12);
        for (int i = buckets.size() - 1; i >= 0; i--) {
            FitnessBucket item = buckets.get(i);
            mFitnessPerMonthDataSet.add(new FitnessBucketMonth(new Date(item.startTime), new Date(item.endTime), item.getAvgSteps(), item.getAvgDistance(), item.getAvgCalories(), true));
        }
        listener.onSuccess();
    }


}
