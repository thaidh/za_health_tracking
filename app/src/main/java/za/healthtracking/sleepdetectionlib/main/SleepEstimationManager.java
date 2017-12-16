package za.healthtracking.sleepdetectionlib.main;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import za.healthtracking.sleepdetectionlib.engine.EstimatedSleepItem;
import za.healthtracking.sleepdetectionlib.engine.SleepItem;
import za.healthtracking.sleepdetectionlib.engine.SleepTimeModel;
import za.healthtracking.utils.TimeHelper;

public class SleepEstimationManager {
    private static final String TAG = ("S HEALTH - " + SleepEstimationManager.class.getSimpleName());
    private static volatile SleepEstimationManager mSleepEstimationManager = null;
    private final LongSparseArray<RealSleepItem> mDailySleepItemMap = new LongSparseArray();
//    private HealthDataStore mDataStore = null;
    private long mEstimationEndDate = 0;
    private final EstimationItemAsc mEstimationItemAsc = new EstimationItemAsc();
    private final LongSparseArray<EstimationItem> mEstimationItemMap = new LongSparseArray();
    private long mEstimationStartDate = 0;
    private final LongSparseArray<EstimationItem> mRecommendItemMap = new LongSparseArray();
//    private HealthDataResolver mResolver = null;
    private final List<TimeGroup> mTimeGroup = new ArrayList();
    private final List<StepItem> mTotalStepItems = new ArrayList();

    private static class EstimationItem {
        private final long mEstimationBedTime;
        private final long mEstimationDate;
        private final long mEstimationWakeUpTime;

        public EstimationItem(long date, long bedTime, long wakeUpTime) {
            this.mEstimationDate = date;
            this.mEstimationBedTime = bedTime;
            this.mEstimationWakeUpTime = wakeUpTime;
        }

        public final long getEstimationDate() {
            return this.mEstimationDate;
        }

        public final long getEstimationBedTime() {
            return this.mEstimationBedTime;
        }

        public final long getEstimationWakeUpTime() {
            return this.mEstimationWakeUpTime;
        }
    }

    private static class EstimationItemAsc implements Comparator<EstimationItem> {
        private EstimationItemAsc() {
        }

        @Override
        public int compare(EstimationItem o1, EstimationItem o2) {
            EstimationItem estimationItem = (EstimationItem) o2;
            long estimationDate = ((EstimationItem) o1).getEstimationDate();
            long estimationDate2 = estimationItem.getEstimationDate();
            if (estimationDate < estimationDate2) {
                return -1;
            }
            return estimationDate > estimationDate2 ? 1 : 0;
        }
    }

    private static class RealSleepItem {
        private final long mBedTime;
        private final long mDate;
        private final long mWakeUpTime;

        public RealSleepItem(long date, long bedTime, long wakeUpTime) {
            this.mDate = date;
            this.mBedTime = bedTime;
            this.mWakeUpTime = wakeUpTime;
        }

        public final long getBedTime() {
            return this.mBedTime;
        }

        public final long getWakeUpTime() {
            return this.mWakeUpTime;
        }
    }

    private static class StepItem {
        private final long mTime;
        private final int mTotalStep;

        public StepItem(long time, int totalStep) {
            this.mTime = time;
            this.mTotalStep = totalStep;
        }

        public final long getTime() {
            return this.mTime;
        }

        public final long getTotalStep() {
            return (long) this.mTotalStep;
        }
    }

    private static class StepItemAscByTime implements Comparator<StepItem> {
        private StepItemAscByTime() {
        }

        @Override
        public int compare(StepItem o1, StepItem o2) {
            StepItem stepItem = (StepItem) o2;
            long time = ((StepItem) o1).getTime();
            long time2 = stepItem.getTime();
            if (time < time2) {
                return -1;
            }
            return time > time2 ? 1 : 0;
        }
    }

    private class TimeGroup {
        private long mAverageEstimationTime = 0;
        private final TimeItemDesc mSortDesc = new TimeItemDesc();
        private final List<TimeItem> mTimeItems = new ArrayList();
        private final int mType;

        TimeGroup(int type) {
            this.mType = type;
        }

        final List<TimeItem> getTimeItemList() {
            return this.mTimeItems;
        }

        final void addTimeItem(TimeItem timeItem) {
            boolean findItem = false;
            boolean removeItem = false;
            for (TimeItem item : this.mTimeItems) {
                if (timeItem.getRealTime() == item.getRealTime()) {
                    findItem = true;
                    if (timeItem.getDiffTime() < item.getDiffTime()) {
                        this.mTimeItems.remove(item);
                        removeItem = true;
                        break;
                    }
                }
            }
            if (!findItem) {
                this.mTimeItems.add(timeItem);
            } else if (removeItem) {
                this.mTimeItems.add(timeItem);
            }
            Collections.sort(this.mTimeItems, this.mSortDesc);
            ArrayList<Long> tempEstimationTimeList = new ArrayList();
            for (TimeItem item2 : this.mTimeItems) {
                tempEstimationTimeList.add(Long.valueOf(item2.getEstimationTime()));
            }
            if (tempEstimationTimeList.size() > 1) {
                Long avgTime = SleepEstimationManager.access$300(SleepEstimationManager.this, tempEstimationTimeList);
                if (avgTime != null) {
                    this.mAverageEstimationTime = avgTime.longValue();
                    return;
                }
                return;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(((Long) tempEstimationTimeList.get(0)).longValue());
            this.mAverageEstimationTime = (((long) cal.get(Calendar.HOUR_OF_DAY)) * 3600000) + (((long) cal.get(Calendar.MINUTE)) * 60000);
        }

        final long getAverageEstimationTime() {
            return this.mAverageEstimationTime;
        }

        final int getType() {
            return this.mType;
        }
    }

    private static class TimeItem {
        private long mDiffTime;
        private long mEstimationTime;
        private long mEstimationTimeWithoutDate;
        private long mRealTime;
        private int mType;

        TimeItem(long estimationTime, long realTime, int type) {
            this.mEstimationTime = estimationTime;
            this.mRealTime = realTime;
            this.mType = type;
            long j = this.mEstimationTime;
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(j);
            int i = instance.get(Calendar.HOUR_OF_DAY);
            this.mEstimationTimeWithoutDate = (((long) instance.get(Calendar.MINUTE)) * 60000) + (((long) i) * 3600000);
        }

        final long getEstimationTime() {
            return this.mEstimationTime;
        }

        final long getRealTime() {
            return this.mRealTime;
        }

        final long getDiffTime() {
            return this.mDiffTime;
        }

        final int getType() {
            return this.mType;
        }

        public final long getEstimationTimeWithoutDate() {
            return this.mEstimationTimeWithoutDate;
        }
    }

    private static class TimeItemAscWithoutDate implements Comparator<TimeItem> {
        private TimeItemAscWithoutDate() {
        }

        @Override
        public int compare(TimeItem o1, TimeItem o2) {
            TimeItem timeItem = (TimeItem) o2;
            long estimationTimeWithoutDate = ((TimeItem) o1).getEstimationTimeWithoutDate();
            long estimationTimeWithoutDate2 = timeItem.getEstimationTimeWithoutDate();
            if (estimationTimeWithoutDate < estimationTimeWithoutDate2) {
                return -1;
            }
            return estimationTimeWithoutDate > estimationTimeWithoutDate2 ? 1 : 0;
        }
    }

    private static class TimeItemDesc implements Comparator<TimeItem> {
        private TimeItemDesc() {
        }

        @Override
        public int compare(TimeItem o1, TimeItem o2) {
            TimeItem timeItem = (TimeItem) o2;
            long estimationTime = ((TimeItem) o1).getEstimationTime();
            long estimationTime2 = timeItem.getEstimationTime();
            if (estimationTime > estimationTime2) {
                return -1;
            }
            return estimationTime < estimationTime2 ? 1 : 0;
        }
    }

    private SleepEstimationManager() {
    }

    public final void initSleepEstimationManager() {
        Cursor readStepDataSync;
//        this.mDataStore = SleepSdkWrapper.getInstance().getHealthDataStoreForSleep();
//        if (this.mResolver == null && this.mDataStore != null) {
//            this.mResolver = new HealthDataResolver(this.mDataStore, null);
//        }
        try {
            SleepEstimationManager instance = getInstance();
            readStepDataSync = instance.readStepDataSync();
            instance.mTotalStepItems.clear();
            if (readStepDataSync != null) {
                if (readStepDataSync.getCount() > 0) {
                    readStepDataSync.moveToFirst();
                    Calendar instance2 = Calendar.getInstance();
                    while (!readStepDataSync.isAfterLast()) {
                        String string = readStepDataSync.getString(readStepDataSync.getColumnIndex("DAY_TIMESTAMP"));
                        int i = readStepDataSync.getInt(readStepDataSync.getColumnIndex("SUM_TOTAL_STEP"));
                        try {
                            int parseInt = Integer.parseInt(string.substring(0, 4));
                            int parseInt2 = Integer.parseInt(string.substring(5, 7)) - 1;
                            int parseInt3 = Integer.parseInt(string.substring(8, 10));
                            int parseInt4 = Integer.parseInt(string.substring(11, 13));
                            int parseInt5 = Integer.parseInt(string.substring(14, 16));
                            instance2.clear();
                            instance2.set(parseInt, parseInt2, parseInt3, parseInt4, parseInt5);
                            instance.mTotalStepItems.add(new StepItem(instance2.getTimeInMillis(), i));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        readStepDataSync.moveToNext();
                    }
                    Collections.sort(instance.mTotalStepItems, new StepItemAscByTime());
                }
                readStepDataSync.close();
            }
        } catch (Exception e2) {
            Log.d(TAG, "Sleep_Estimation_Manager : RemoteException " + Arrays.toString(e2.getStackTrace()));
        } catch (Throwable th) {
        }
    }

    public static SleepEstimationManager getInstance() {
        if (mSleepEstimationManager == null) {
            synchronized (SleepEstimationManager.class) {
                if (mSleepEstimationManager == null) {
                    SleepEstimationManager sleepEstimationManager = new SleepEstimationManager();
                    mSleepEstimationManager = sleepEstimationManager;
                    sleepEstimationManager.initSleepEstimationManager();
                    mSleepEstimationManager.startSleepEstimation();
                }
            }
        }
        return mSleepEstimationManager;
    }

    public EstimatedSleepItem getEstimatedSleepItem(long selectedDate) {
        selectedDate = TimeHelper.getStartTimeOnThisDayTimestamp(selectedDate);
        if (this.mDailySleepItemMap.get(selectedDate) == null) {
            return adjustEstimatedSleepItem(selectedDate);
        }
        Log.d(TAG, "Sleep_Estimation_Manager : [~] getEstimatedSleepItem : find sleep time");
        return null;
    }

    public final int getNumberOfDaysSuccessEstimatedSleepItem(long startDate, long endDate) {
        int numberOfDays = 0;
        for (long date = startDate; date < endDate; date += 86400000) {
            EstimatedSleepItem estSleepItem;
//            if (Utils.checkFeature(2)) {
//                estSleepItem = adjustEstimatedSleepItem(date);
//            } else {
//                estSleepItem = null;
//            }
//            if (estSleepItem != null) {
//                numberOfDays++;
//            }
        }
        return numberOfDays;
    }

    private EstimatedSleepItem adjustEstimatedSleepItem(long selectedDate) {
//        selectedDate = DateTimeUtils.getStartTimeOfDay(selectedDate);
        Log.d(TAG, "Sleep_Estimation_Manager : [+] getEstimatedSleepItem " + selectedDate);
        EstimatedSleepItem estimatedSleepItem = null;
        EstimationItem recommendItem = (EstimationItem) this.mRecommendItemMap.get(selectedDate);
        if (recommendItem != null) {
            int bedTimeType;
            int wakeUpTimeType;
            long bedTime = recommendItem.getEstimationBedTime();
            long wakeUpTime = recommendItem.getEstimationWakeUpTime();
            if (recommendItem.getEstimationBedTime() == -1 || recommendItem.getEstimationBedTime() != -2) {
                bedTimeType = EstimatedSleepItem.TYPE_RECOMMEND_SLEEP_TIME;
            } else {
                bedTimeType = EstimatedSleepItem.TYPE_ESTIMATION_SLEEP_TIME;
            }
            if (recommendItem.getEstimationWakeUpTime() == -1) {
                wakeUpTimeType = EstimatedSleepItem.TYPE_RECOMMEND_SLEEP_TIME;
            } else if (recommendItem.getEstimationWakeUpTime() == -2) {
                wakeUpTimeType = EstimatedSleepItem.TYPE_ESTIMATION_SLEEP_TIME;
            } else {
                wakeUpTimeType = EstimatedSleepItem.TYPE_RECOMMEND_SLEEP_TIME;
            }
            Log.i(TAG, "Sleep_Estimation_Manager : +" + new Date(selectedDate).toString() + " :: " + bedTimeType + "/" + wakeUpTimeType + " -- " + bedTime + " :: " + wakeUpTime);
            EstimationItem estimationItem = (EstimationItem) this.mEstimationItemMap.get(selectedDate);
            if (estimationItem == null) {
                Log.d(TAG, "Sleep_Estimation_Manager : [~] getEstimatedSleepItem : can't find estimation db date " + selectedDate);
                return null;
            }
            if (bedTime == -2 && estimationItem != null) {
                bedTime = estimationItem.getEstimationBedTime();
            }
            if (wakeUpTime == -2 && estimationItem != null) {
                wakeUpTime = estimationItem.getEstimationWakeUpTime();
            }
            Log.i(TAG, "Sleep_Estimation_Manager : -+" + new Date(selectedDate).toString() + " :: " + bedTime + " :: " + wakeUpTime);
//            bedTime = convertMinute(bedTime, 0);
//            wakeUpTime = convertMinute(wakeUpTime, 1);
            long todayTime = System.currentTimeMillis();
            if (bedTime > todayTime) {
                bedTime = todayTime;
                Log.d(TAG, "Sleep_Estimation_Manager : getEstimatedSleepItem[" + selectedDate + "] : bed time is future!");
            }
            if (wakeUpTime > todayTime) {
                wakeUpTime = todayTime;
                Log.d(TAG, "Sleep_Estimation_Manager : getEstimatedSleepItem[" + selectedDate + "] : wake-up time is future!");
            }
            if (bedTime > -1 && wakeUpTime > -1 && bedTime >= wakeUpTime) {
                bedTime = -1;
                wakeUpTime = -1;
                Log.d(TAG, "Sleep_Estimation_Manager : getEstimatedSleepItem[" + selectedDate + "] : bed time is wrong!");
            }
            estimatedSleepItem = new EstimatedSleepItem(new EstimatedSleepItem.Builder().sleepDate(selectedDate).bedTime(bedTime).wakeUpTime(wakeUpTime).setBedTimeType(bedTimeType).setWakeUpTimeType(wakeUpTimeType));
            if (estimatedSleepItem != null) {
                Log.d(TAG, "Sleep_Estimation_Manager : [-]getEstimatedSleepItem" + estimatedSleepItem.getBedTime() + " ~ " + estimatedSleepItem.getWakeUpTime() + " type : " + estimatedSleepItem.getBedTimeType() + "/" + estimatedSleepItem.getWakeUpTimeType());
            } else {
                Log.d(TAG, "Sleep_Estimation_Manager : [-]getEstimatedSleepItem null");
            }
            return estimatedSleepItem;
        }
        Log.d(TAG, "Sleep_Estimation_Manager : [~] getEstimatedSleepItem : can't find recommend date");
        return null;
    }

    private static long analyzeEstimatedTime(long estimatedSleepItemOfTargetDate, TimeGroup timeGroup, int type) {
        Log.d(TAG, "Sleep_Estimation_Manager : [+] analyzeEstimatedTime: date Time" + new Date(estimatedSleepItemOfTargetDate).toString());
        long recommendedTime = -1;
        long timeDifferenceInMillis = 0;
        if (timeGroup != null && timeGroup.getTimeItemList().size() > 1) {
            long firstDiff = ((TimeItem) timeGroup.getTimeItemList().get(0)).getDiffTime();
            long secondDiff = ((TimeItem) timeGroup.getTimeItemList().get(1)).getDiffTime();
            if (firstDiff - 3600000 <= secondDiff && secondDiff <= 3600000 + firstDiff) {
                long latestDiff = (firstDiff + secondDiff) / 2;
                int validDiffCount = 1;
                for (TimeItem timeItem : timeGroup.getTimeItemList()) {
                    if (latestDiff - 3600000 <= timeItem.getDiffTime() && timeItem.getDiffTime() <= 3600000 + latestDiff) {
                        timeDifferenceInMillis += timeItem.getDiffTime();
                        validDiffCount++;
                    }
                }
                timeDifferenceInMillis /= (long) validDiffCount;
                Log.d(TAG, "Sleep_Estimation_Manager : es B= " + new Date(estimatedSleepItemOfTargetDate).toString() + " / df = " + new Date(timeDifferenceInMillis).toString());
                return recommendedTime = estimatedSleepItemOfTargetDate + timeDifferenceInMillis;
//                switch (type) {
//                    case 0:
//                        Log.d(TAG, "Sleep_Estimation_Manager : es B= " + new Date(estimatedSleepItemOfTargetDate).toString() + " / df = " + new Date(timeDifferenceInMillis).toString());
//                        recommendedTime = estimatedSleepItemOfTargetDate + timeDifferenceInMillis);
//                        break;
//                    case 1:
//                        Log.d(TAG, "Sleep_Estimation_Manager : es W= " + new Date(estimatedSleepItemOfTargetDate).toString() + " / df = " + new Date(timeDifferenceInMillis).toString());
//                        recommendedTime = DateTimeUtils.getTimeWithZeroSeconds(estimatedSleepItemOfTargetDate + timeDifferenceInMillis);
//                        break;
////                }
            }
            Log.d(TAG, "Sleep_Estimation_Manager : Diff is not pass : es = NO_RECOMMEND_TIME.");
            return -1;
        }
        if (timeGroup == null) {
            Log.d(TAG, "Sleep_Estimation_Manager : timeGroup is null : CAN_NOT_FIND_TIME_GROUP");
        } else if (timeGroup.getTimeItemList().size() <= 1) {
            Log.d(TAG, "Sleep_Estimation_Manager : timeGroup size is " + timeGroup.getTimeItemList().size() + " : CAN_NOT_FIND_TIME_GROUP");
        }
        return recommendedTime;
    }

    public final synchronized void startSleepEstimation() {
        Log.d(TAG, "Sleep_Estimation_Manager : [+] start Sleep Estimation : ");
            this.mEstimationItemMap.clear();
            this.mDailySleepItemMap.clear();
            this.mTimeGroup.clear();
            this.mRecommendItemMap.clear();
            long currentTimeMillis = System.currentTimeMillis();
//            Calendar instance = Calendar.getInstance();
//            instance.setTimeInMillis(currentTimeMillis);
//            instance.set(Calendar.HOUR_OF_DAY, 0);
//            instance.set(Calendar.MINUTE, 0);
//            instance.set(Calendar.SECOND, 0);
//            instance.set(Calendar.MILLISECOND, 0);
//            instance.setTimeInMillis((long) TimeChartUtils.getMultiplyEpochTime((double) instance.getTimeInMillis(), 8.64E7d, 1));
//            this.mEstimationEndDate = DateTimeUtils.getSleepEndTimeOfDay(instance.getTimeInMillis(), SleepDataSelectionType.SLEEP_DATA_SELECTION_TRACKER);
//            instance.setTimeInMillis((long) TimeChartUtils.getMultiplyEpochTime((double) instance.getTimeInMillis(), 8.64E7d, -100));
//            this.mEstimationStartDate = DateTimeUtils.getSleepStartTimeOfDay(instance.getTimeInMillis(), SleepDataSelectionType.SLEEP_DATA_SELECTION_TRACKER);
//            ArrayList dailySleepItems$6841d604 = SleepDataManager.getDailySleepItems$6841d604(this.mEstimationStartDate, this.mEstimationEndDate, SleepDataSelectionType.SLEEP_DATA_SELECTION_TRACKER, false);
//            Calendar instance2 = Calendar.getInstance();
//            Iterator it = dailySleepItems$6841d604.iterator();
//            while (it.hasNext()) {
//                long j;
//                DailySleepItem dailySleepItem = (DailySleepItem) it.next();
//                long goalBedTimeOfDailySleepItem = SleepDataManager.getGoalBedTimeOfDailySleepItem(dailySleepItem);
//                currentTimeMillis = SleepDataManager.getGoalBedTimeOfDailySleepItem(dailySleepItem);
//                if (goalBedTimeOfDailySleepItem == -1 || currentTimeMillis == -1) {
//                    instance2.setTimeInMillis(dailySleepItem.getDate());
//                    instance2.set(11, 0);
//                    instance2.set(12, 0);
//                    instance2.set(13, 0);
//                    instance2.set(14, 0);
//                    goalBedTimeOfDailySleepItem = 82800000 + instance2.getTimeInMillis();
//                    instance2.add(5, 1);
//                    j = goalBedTimeOfDailySleepItem;
//                    goalBedTimeOfDailySleepItem = instance2.getTimeInMillis() + 28800000;
//                } else {
//                    j = goalBedTimeOfDailySleepItem;
//                    goalBedTimeOfDailySleepItem = currentTimeMillis;
//                }
//                long j2 = -1;
//                long j3 = -1;
//                Iterator it2 = dailySleepItem.getSleepItems().iterator();
//                while (it2.hasNext()) {
//                    SleepItem sleepItem = (SleepItem) it2.next();
//                    long wakeUpTime = sleepItem.getWakeUpTime() - sleepItem.getBedTime();
//                    long bedTime = sleepItem.getBedTime();
//                    if (wakeUpTime + bedTime >= j && bedTime <= r4) {
//                        if (j2 == -1) {
//                            j2 = sleepItem.getBedTime();
//                        } else if (j2 > sleepItem.getBedTime()) {
//                            j2 = sleepItem.getBedTime();
//                        }
//                        if (j3 == -1) {
//                            j3 = sleepItem.getWakeUpTime();
//                        } else if (j3 < sleepItem.getWakeUpTime()) {
//                            j3 = sleepItem.getWakeUpTime();
//                        }
//                    }
//                }
//                if (!(j2 == -1 || j3 == -1)) {
//                    this.mDailySleepItemMap.put(dailySleepItem.getDate(), new RealSleepItem(dailySleepItem.getDate(), j2, j3));
//                }
//            }
//            SleepDetectionManager.mContext = getApplicationContext();
            SleepDetection.getInstance();
            SleepDetection.updateSleepTime();
            List<EstimationItem> sleepDetectionDbItemList = new ArrayList();
            SleepDetection.getInstance();
            this.mEstimationStartDate = TimeHelper.getStartTimeOnThisDayTimestamp(System.currentTimeMillis());
            this.mEstimationEndDate = mEstimationStartDate + 86400000;
            Iterator it = SleepDetection.getSleepTime(this.mEstimationStartDate, this.mEstimationEndDate).iterator();
            while (it.hasNext()) {
                SleepTimeModel sleepTimeModel = (SleepTimeModel) it.next();
                if (sleepTimeModel.getIgnoreSleep() != 1) {
                    SleepItem sleepItem = new SleepItem(sleepTimeModel.getStartTime(), sleepTimeModel.getEndTime(), 0, 0.0f, "TEMP_ESTIMATED_UUID_" + sleepTimeModel.getStartTime(), SleepItem.SleepType.SLEEP_TYPE_MANUAL, "TEMP_ESTIMATED_SLEEP", SleepItem.SleepCondition.SLEEP_CONDITION_NONE);
                    sleepDetectionDbItemList.add(new EstimationItem(TimeHelper.getSleepDate(sleepItem), sleepItem.getBedTime(), sleepItem.getWakeUpTime()));
                }
            }
            Log.d(TAG, "Sleep_Estimation_Manager : [+] set estimation time ");
            if (sleepDetectionDbItemList.size() > 0) {
                for (EstimationItem estimationItem : sleepDetectionDbItemList) {
                    EstimationItem tempItem = (EstimationItem) this.mEstimationItemMap.get(estimationItem.getEstimationDate());
                    if (tempItem == null || tempItem.getEstimationWakeUpTime() - tempItem.getEstimationBedTime() < estimationItem.getEstimationWakeUpTime() - estimationItem.getEstimationBedTime()) {
                        this.mEstimationItemMap.put(estimationItem.getEstimationDate(), estimationItem);
                    }
                }
                List<EstimationItem> estimationDbItemList = convertMapToList(this.mEstimationItemMap);
                if (estimationDbItemList != null) {
                    analyzeGroup(estimationDbItemList);
                }
            } else {
                Log.d(TAG, "Sleep_Estimation_Manager : can't find estimation time from db ");
            }
    }

    private void analyzeRecommendTime(EstimationItem estimationItem) {
//        TimeGroup bedTimeGroup = findTimeGroupByTime(estimationItem.getEstimationBedTime(), 0);
//        TimeGroup wakeupTimeGroup = findTimeGroupByTime(estimationItem.getEstimationWakeUpTime(), 1);
        //todo analyze here
//        EstimationItem recommendItem = new EstimationItem(estimationItem.getEstimationDate(), analyzeEstimatedTime(estimationItem.getEstimationBedTime(), bedTimeGroup, 0), analyzeEstimatedTime(estimationItem.getEstimationWakeUpTime(), wakeupTimeGroup, 1));
       /* Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : " + recommendItem.getEstimationBedTime() + " " + recommendItem.getEstimationWakeUpTime());
        if (recommendItem.getEstimationBedTime() == -1 && recommendItem.getEstimationWakeUpTime() == -1) {
            recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
        } else {
            long j;
            long j2;
            long j3;
            SleepItem sleepItem = new SleepItem(estimationItem.getEstimationBedTime(), estimationItem.getEstimationWakeUpTime(), 0, 0.0f, "", SleepItem.SleepType.SLEEP_TYPE_MANUAL, "", SleepItem.SleepCondition.SLEEP_CONDITION_NONE);
            long goalBedTimeOfSleepItem =*//* SleepDataManager.getGoalBedTimeOfSleepItem(sleepItem)*//*-1;
            long goalWakeUpTimeOfSleepItem = *//*SleepDataManager.getGoalWakeUpTimeOfSleepItem(sleepItem)*//*-1;
            if (goalBedTimeOfSleepItem == -1 || goalWakeUpTimeOfSleepItem == -1) {
                Calendar instance = Calendar.getInstance();
                instance.setTimeInMillis(TimeHelper.getStartTimeOnThisDayTimestamp(estimationItem.getEstimationBedTime())*//*DateTimeUtils.getSleepStartTimeOfDay(estimationItem.getEstimationBedTime(), SleepDataSelectionType.SLEEP_DATA_SELECTION_TRACKER)*//*);
                instance.set(Calendar.HOUR_OF_DAY, 0);
                instance.set(Calendar.MINUTE, 0);
                instance.set(Calendar.SECOND, 0);
                instance.set(Calendar.MILLISECOND, 0);
                goalBedTimeOfSleepItem = instance.getTimeInMillis() + 82800000;
                instance.add(Calendar.DAY_OF_MONTH, 1);
                goalWakeUpTimeOfSleepItem = instance.getTimeInMillis() + 28800000;
                j = goalBedTimeOfSleepItem;
            } else {
                j = goalBedTimeOfSleepItem;
            }
            if (recommendItem.getEstimationBedTime() == -1) {
                j2 = j;
            } else if (recommendItem.getEstimationBedTime() == -2) {
                j2 = estimationItem.getEstimationBedTime();
            } else {
                j2 = recommendItem.getEstimationBedTime();
            }
            if (recommendItem.getEstimationWakeUpTime() == -1) {
                j3 = goalWakeUpTimeOfSleepItem;
            } else if (recommendItem.getEstimationWakeUpTime() == -2) {
                j3 = estimationItem.getEstimationWakeUpTime();
            } else {
                j3 = recommendItem.getEstimationWakeUpTime();
            }
            Log.e(TAG, "Sleep_Estimation_Manager : [+++]" + new Date(recommendItem.getEstimationDate()) + " // " + new Date(j2) + " // " + new Date(j3));
            EstimationItem checkEstimationTimeWithStep = checkEstimationTimeWithStep(new EstimationItem(recommendItem.getEstimationDate(), j2, j3));
            j2 = checkEstimationTimeWithStep.getEstimationBedTime();
            j3 = checkEstimationTimeWithStep.getEstimationWakeUpTime();
            Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : time " + j2 + " ~ " + j3);
            if (j3 - j2 < 10800000) {
                Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : return MIN_ESTIMATION_LIMIT_DURATION_HOUR_TIME " + (j3 - j2) + " < 10800000");
                recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
            } else if (j3 - j2 > 43200000) {
                Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : return MAX_ESTIMATION_LIMIT_DURATION_HOUR_TIME " + (j3 - j2) + " > 43200000");
                recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
            } else {
                boolean z;
                boolean z2;
                if (recommendItem.getEstimationBedTime() == -2 || (recommendItem.getEstimationBedTime() == -1 && j - 10800000 <= j2 && j2 <= 10800000 + j)) {
                    z = true;
                } else if (recommendItem.getEstimationBedTime() == -1 || recommendItem.getEstimationBedTime() == -2) {
                    z = false;
                } else {
                    z = true;
                }
                if (recommendItem.getEstimationWakeUpTime() == -2 || (recommendItem.getEstimationWakeUpTime() == -1 && goalWakeUpTimeOfSleepItem - 10800000 <= j3 && j3 <= 10800000 + goalWakeUpTimeOfSleepItem)) {
                    z2 = true;
                } else if (recommendItem.getEstimationWakeUpTime() == -1 || recommendItem.getEstimationWakeUpTime() == -2) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime bed time: isPassed ? " + (j - 10800000) + " -> " + j2 + " -> " + (10800000 + j) + " goal : " + j + " 10800000");
                Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime wakeup time: isPassed ? " + (goalWakeUpTimeOfSleepItem - 10800000) + " -> " + j3 + " -> " + (10800000 + goalWakeUpTimeOfSleepItem) + " goal : " + goalWakeUpTimeOfSleepItem + " 10800000");
                Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : isPassed ? " + z + " && " + z2 + " 10800000");
                if (!z && !z2) {
                    recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
                } else if (!z || z2) {
                    if (z || !z2) {
                        recommendItem = new EstimationItem(estimationItem.getEstimationDate(), j2, j3);
                    } else if (j3 - j < 10800000) {
                        Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : return MIN_ESTIMATION_LIMIT_DURATION_HOUR_TIME : !isPassedBedTime && isPassedWakeupTime" + (j3 - j) + " < 10800000");
                        recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
                    } else if (j3 - j > 43200000) {
                        Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : return MAX_ESTIMATION_LIMIT_DURATION_HOUR_TIME  : !isPassedBedTime && isPassedWakeupTime" + (j3 - j) + " > 43200000");
                        recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
                    } else {
                        recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, recommendItem.getEstimationWakeUpTime());
                    }
                } else if (goalWakeUpTimeOfSleepItem - j2 < 10800000) {
                    Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : return MIN_ESTIMATION_LIMIT_DURATION_HOUR_TIME : isPassedBedTime && !isPassedWakeupTime" + (goalWakeUpTimeOfSleepItem - j2) + " < 10800000");
                    recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
                } else if (goalWakeUpTimeOfSleepItem - j2 > 43200000) {
                    Log.d(TAG, "Sleep_Estimation_Manager : [+]checkEstimationLimitTime : return MAX_ESTIMATION_LIMIT_DURATION_HOUR_TIME  : isPassedBedTime && !isPassedWakeupTime" + (goalWakeUpTimeOfSleepItem - j2) + " > 43200000");
                    recommendItem = new EstimationItem(estimationItem.getEstimationDate(), -1, -1);
                } else {
                    recommendItem = new EstimationItem(estimationItem.getEstimationDate(), recommendItem.getEstimationBedTime(), -1);
                }
            }
        }*/
        EstimationItem recommendItem = new EstimationItem(estimationItem.getEstimationDate(), estimationItem.getEstimationBedTime(), estimationItem.getEstimationWakeUpTime());
        if (recommendItem.getEstimationBedTime() != -1 || recommendItem.getEstimationWakeUpTime() != -1) {
            this.mRecommendItemMap.put(recommendItem.getEstimationDate(), recommendItem);
        }
    }

    private void analyzeGroup(List<EstimationItem> estimationItemList) {
        Log.d(TAG, "Sleep_Estimation_Manager : [+] analyzeGroup");
        List<TimeItem> bedTimeItems = new ArrayList();
        List<TimeItem> wakeTimeItems = new ArrayList();
        for (EstimationItem estimationItem : estimationItemList) {
            analyzeRecommendTime(estimationItem);
            Log.i(TAG, "Sleep_Estimation_Manager : " + new Date(estimationItem.getEstimationDate()).toString() + " :: " + new Date(estimationItem.getEstimationDate()).toString() + " :: " + new Date(estimationItem.getEstimationBedTime()).toString() + new Date(estimationItem.getEstimationWakeUpTime()).toString());
            RealSleepItem realSleepItem = (RealSleepItem) this.mDailySleepItemMap.get(estimationItem.getEstimationDate());
            if (realSleepItem == null) {
                Log.d(TAG, "Sleep_Estimation_Manager :  can not find daily Sleep Item ! " + new Date(estimationItem.getEstimationDate()).toString());
            } else {
                TimeItem bedTimeItem = new TimeItem(estimationItem.getEstimationBedTime(), realSleepItem.getBedTime(), 0);
                TimeItem wakeTimeItem = new TimeItem(estimationItem.getEstimationWakeUpTime(), realSleepItem.getWakeUpTime(), 1);
                bedTimeItems.add(bedTimeItem);
                wakeTimeItems.add(wakeTimeItem);
                makeGroups(bedTimeItems);
                makeGroups(wakeTimeItems);
            }
        }
    }

    private void makeGroups(List<TimeItem> timeItems) {
        Collections.sort(timeItems, new TimeItemAscWithoutDate());
        long boundaryStartTime = ((TimeItem) timeItems.get(0)).getEstimationTimeWithoutDate();
        long boundaryEndTime = boundaryStartTime + 3600000;
        int timeItemType = ((TimeItem) timeItems.get(0)).getType();
        Iterator<TimeGroup> timeGroup = this.mTimeGroup.iterator();
        while (timeGroup.hasNext()) {
            if (((TimeGroup) timeGroup.next()).getType() == timeItemType) {
                timeGroup.remove();
            }
        }
        TimeGroup timeGroup2 = makeTimeGroup((TimeItem) timeItems.get(0));
        int index = 1;
        while (index < timeItems.size()) {
            if (boundaryStartTime > ((TimeItem) timeItems.get(index)).getEstimationTimeWithoutDate() || ((TimeItem) timeItems.get(index)).getEstimationTimeWithoutDate() > boundaryEndTime) {
                boundaryStartTime = ((TimeItem) timeItems.get(index)).getEstimationTimeWithoutDate();
                boundaryEndTime = boundaryStartTime + 3600000;
                timeGroup2 = makeTimeGroup((TimeItem) timeItems.get(index));
            } else {
                timeGroup2.addTimeItem((TimeItem) timeItems.get(index));
            }
            index++;
        }
    }

    private TimeGroup findTimeGroupByTime(long time, int type) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(time);
        time = (((long) instance.get(Calendar.HOUR_OF_DAY)) * 3600000) + (((long) instance.get(Calendar.MINUTE)) * 60000);
        TimeGroup tempTimeGroup = null;
        long tempTimeDiff = 0;
        for (TimeGroup timeGroup : this.mTimeGroup) {
            if (timeGroup.getAverageEstimationTime() - 3600000 <= time && time <= timeGroup.getAverageEstimationTime() + 3600000 && timeGroup.getType() == type) {
                if (tempTimeGroup == null) {
                    tempTimeGroup = timeGroup;
                    tempTimeDiff = Math.abs(time - timeGroup.getAverageEstimationTime());
                } else if (tempTimeDiff > Math.abs(time - timeGroup.getAverageEstimationTime())) {
                    tempTimeGroup = timeGroup;
                    tempTimeDiff = Math.abs(time - timeGroup.getAverageEstimationTime());
                }
            }
        }
        return tempTimeGroup;
    }

    private TimeGroup makeTimeGroup(TimeItem timeItem) {
        TimeGroup timeGroup = new TimeGroup(timeItem.getType());
        timeGroup.addTimeItem(timeItem);
        this.mTimeGroup.add(timeGroup);
        return timeGroup;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static long convertMinute(long r8, int r10) {
        return 0;
    }

    private ArrayList<EstimationItem> convertMapToList(LongSparseArray<EstimationItem> estimationItemMap) {
        ArrayList<EstimationItem> list = null;
        if (estimationItemMap != null) {
            list = new ArrayList();
            for (int i = 0; i < estimationItemMap.size(); i++) {
                list.add(estimationItemMap.valueAt(i));
            }
            Collections.sort(list, this.mEstimationItemAsc);
        }
        return list;
    }

    public static boolean isRunningSleepDetectService(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        List<RunningServiceInfo> runningServiceList = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServiceList == null) {
            return false;
        }
        for (RunningServiceInfo service : runningServiceList) {
            if (service.service.getClassName().equals("com.samsung.android.sleepdetectionlib.service.ServiceManager")) {
                return true;
            }
        }
        return false;
    }

    private EstimationItem checkEstimationTimeWithStep(EstimationItem estimationItem) {
        int index;
        long estimationBedTime = estimationItem.getEstimationBedTime();
        long estimationWakeUpTime = estimationItem.getEstimationWakeUpTime();
        List<StepItem> stepItemList = new ArrayList();
        if (this.mTotalStepItems.size() > 0) {
            for (StepItem stepItem : this.mTotalStepItems) {
                if (estimationBedTime <= stepItem.getTime() && stepItem.getTime() <= estimationWakeUpTime) {
                    stepItemList.add(stepItem);
                }
            }
        }
        long bedTime = estimationItem.getEstimationBedTime();
        long wakeUpTime = estimationItem.getEstimationWakeUpTime();
        long esBedTime = estimationItem.getEstimationBedTime();
        long esWakeUpTime = estimationItem.getEstimationWakeUpTime();
        int stepCount = 0;
        int shotSleepCountByStep = 0;
        long bedTimeByStepCount = -1;
        long bedTimeByStepInterval = -1;
        long wakeUpTimeByStepCount = -1;
        long wakeUpTimeByStepInterval = -1;
        for (index = 0; index < stepItemList.size(); index++) {
            StepItem stepItem2 = (StepItem) stepItemList.get(index);
            if (esBedTime > stepItem2.getTime() || stepItem2.getTime() > 7200000 + esBedTime) {
                break;
            }
            stepCount = (int) (((long) stepCount) + stepItem2.getTotalStep());
            if (stepCount >= 50) {
                bedTimeByStepCount = stepItem2.getTime();
                stepCount = 0;
            }
            if (index != 0) {
                if (stepItem2.getTime() - ((StepItem) stepItemList.get(index - 1)).getTime() <= 600000 && stepItem2.getTotalStep() >= 10) {
                    if (((StepItem) stepItemList.get(index - 1)).getTotalStep() >= 10) {
                        shotSleepCountByStep++;
                        if (shotSleepCountByStep >= 3) {
                            bedTimeByStepInterval = stepItem2.getTime();
                        }
                        if (bedTimeByStepCount < bedTimeByStepInterval) {
                            if (bedTimeByStepCount != -1) {
                                bedTime = bedTimeByStepCount;
                            } else if (bedTimeByStepInterval != -1) {
                                bedTime = bedTimeByStepInterval;
                            }
                        } else if (bedTimeByStepInterval != -1) {
                            bedTime = bedTimeByStepInterval;
                        } else if (bedTimeByStepCount != -1) {
                            bedTime = bedTimeByStepCount;
                        }
                    }
                }
            }
            shotSleepCountByStep = stepItem2.getTotalStep() >= 10 ? 1 : 0;
            if (bedTimeByStepCount < bedTimeByStepInterval) {
                if (bedTimeByStepInterval != -1) {
                    bedTime = bedTimeByStepInterval;
                } else if (bedTimeByStepCount != -1) {
                    bedTime = bedTimeByStepCount;
                }
            } else if (bedTimeByStepCount != -1) {
                bedTime = bedTimeByStepCount;
            } else if (bedTimeByStepInterval != -1) {
                bedTime = bedTimeByStepInterval;
            }
        }
        stepCount = 0;
        shotSleepCountByStep = 0;
        for (index = stepItemList.size() - 1; index >= 0; index--) {
            StepItem stepItem2 = (StepItem) stepItemList.get(index);
            if (esWakeUpTime - 7200000 > stepItem2.getTime() || stepItem2.getTime() > esWakeUpTime) {
                break;
            }
            stepCount = (int) (((long) stepCount) + stepItem2.getTotalStep());
            if (stepCount >= 50) {
                wakeUpTimeByStepCount = stepItem2.getTime();
                stepCount = 0;
            }
            if (index != stepItemList.size() - 1) {
                if (((StepItem) stepItemList.get(index + 1)).getTime() - stepItem2.getTime() <= 600000 && stepItem2.getTotalStep() >= 10) {
                    if (((StepItem) stepItemList.get(index + 1)).getTotalStep() >= 10) {
                        shotSleepCountByStep++;
                        if (shotSleepCountByStep >= 3) {
                            wakeUpTimeByStepInterval = stepItem2.getTime();
                        }
                        if (wakeUpTimeByStepCount > wakeUpTimeByStepInterval) {
                            if (wakeUpTimeByStepCount != -1) {
                                wakeUpTime = wakeUpTimeByStepCount;
                            } else if (wakeUpTimeByStepInterval != -1) {
                                wakeUpTime = wakeUpTimeByStepInterval;
                            }
                        } else if (wakeUpTimeByStepInterval != -1) {
                            wakeUpTime = wakeUpTimeByStepInterval;
                        } else if (wakeUpTimeByStepCount != -1) {
                            wakeUpTime = wakeUpTimeByStepCount;
                        }
                    }
                }
            }
            shotSleepCountByStep = stepItem2.getTotalStep() >= 10 ? 1 : 0;
            if (wakeUpTimeByStepCount > wakeUpTimeByStepInterval) {
                if (wakeUpTimeByStepInterval != -1) {
                    wakeUpTime = wakeUpTimeByStepInterval;
                } else if (wakeUpTimeByStepCount != -1) {
                    wakeUpTime = wakeUpTimeByStepCount;
                }
            } else if (wakeUpTimeByStepCount != -1) {
                wakeUpTime = wakeUpTimeByStepCount;
            } else if (wakeUpTimeByStepInterval != -1) {
                wakeUpTime = wakeUpTimeByStepInterval;
            }
        }
        return new EstimationItem(estimationItem.getEstimationDate(), bedTime, wakeUpTime);
    }

    private Cursor readStepDataSync() {
        return null;
//        if (this.mDataStore == null || this.mResolver == null) {
//            LOG.e(TAG, "Sleep_Estimation_Manager : [-] readStepDataSync : can't connect step DB");
//            return null;
//        }
//        long endTime = System.currentTimeMillis();
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(endTime);
//        cal.set(11, 0);
//        cal.set(12, 0);
//        cal.set(13, 0);
//        cal.set(14, 0);
//        long todayTime = cal.getTimeInMillis();
//        cal.add(5, -30);
//        long startTime = cal.getTimeInMillis();
//        Date todayDate = new Date();
//        todayDate.setTime(todayTime);
//        LOG.d(TAG, "Sleep_Estimation_Manager : readStepDataSync() : " + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(todayDate));
//        Filter startTimeFilter = Filter.greaterThanEquals("com.samsung.health.step_count.start_time", Long.valueOf(startTime));
//        Filter endTimeFilter = Filter.lessThanEquals("com.samsung.health.step_count.start_time", Long.valueOf(endTime));
//        AggregateRequest request = null;
//        try {
//            request = new AggregateRequest.Builder().setDataType("com.samsung.shealth.tracker.pedometer_step_count").setFilter(Filter.and(startTimeFilter, new Filter[]{endTimeFilter})).addFunction(AggregateFunction.SUM, "com.samsung.health.step_count.count", "SUM_TOTAL_STEP").setTimeGroup(TimeGroupUnit.MINUTELY, 1, "com.samsung.health.step_count.start_time", "com.samsung.health.step_count.time_offset", "DAY_TIMESTAMP").build();
//        } catch (IllegalArgumentException e) {
//            LOG.e(TAG, "Sleep_Estimation_Manager : readStepDataSync() - error");
//        }
//        try {
//            SleepDatabaseSyncModule sleepDatabaseSyncModule = new SleepDatabaseSyncModule(request, this.mResolver);
//            sleepDatabaseSyncModule.start();
//            synchronized (sleepDatabaseSyncModule) {
//                sleepDatabaseSyncModule.wait(3000);
//                sleepDatabaseSyncModule.mLoop.quit();
//            }
//            Cursor cursor = sleepDatabaseSyncModule.getResult();
//            LOG.d(TAG, "Sleep_Estimation_Manager : Reading health step data - Sync call");
//            return cursor;
//        } catch (Exception e2) {
//            LOG.e(TAG, "Sleep_Estimation_Manager : Reading health step data fails - Sync call");
//            return null;
//        }
    }

    static Long access$300(SleepEstimationManager x0, ArrayList x1) {
        if (x1 == null || x1.size() <= 0) {
            return null;
        }
        Calendar instance = Calendar.getInstance();
        Iterator it = x1.iterator();
        double d = 0.0d;
        double d2 = 0.0d;
        while (it.hasNext()) {
            instance.setTimeInMillis(((Long) it.next()).longValue());
            double d3 = (((((double) ((instance.get(Calendar.HOUR_OF_DAY) * 3600000) + (instance.get(Calendar.MINUTE) * 60000))) * 360.0d) / 8.64E7d) * 3.141592653589793d) / 180.0d;
            d += 10.0d * Math.cos(d3);
            d2 = (Math.sin(d3) * 10.0d) + d2;
        }
        if (x1.size() <= 0) {
            return null;
        }
        double d3 = (double) Math.round((Math.atan2(d2 / ((double) x1.size()), d / ((double) x1.size())) * 180.0d) / 3.141592653589793d);
        if (d3 < 0.0d) {
            d3 += 360.0d;
        }
        return Long.valueOf((long) ((d3 * 8.64E7d) / 360.0d));
    }
}
