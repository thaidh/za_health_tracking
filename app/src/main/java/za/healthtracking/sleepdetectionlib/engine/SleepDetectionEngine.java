package za.healthtracking.sleepdetectionlib.engine;

import android.content.Context;
import android.content.IntentFilter;
import android.os.PowerManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import za.healthtracking.sleepdetectionlib.alarm.ScheduleManager;
import za.healthtracking.sleepdetectionlib.alarm.ScheduleTodoWork;
import za.healthtracking.sleepdetectionlib.collector.Receiver_Screen;
import za.healthtracking.sleepdetectionlib.collector.ScreenModel;
import za.healthtracking.sleepdetectionlib.database.DatabaseManager;
import za.healthtracking.sleepdetectionlib.info.UserData;
import za.healthtracking.sleepdetectionlib.request.TimeModel;
import za.healthtracking.sleepdetectionlib.util.Log;

public final class SleepDetectionEngine {
    private static SleepDetectionEngine mInstance;
    private int MAX_MERGE_INTERVAL = 300000;
    private long MIN_MERGE_DURATION = 1800000;
    private String TAG = getClass().getSimpleName();
    private int mAsleepTotal = -1;
    private int mAsleepWeekDay = -1;
    private int mAsleepWeekEnd = -1;
    private Receiver_Screen mScreenReceiver = null;
    private int mWakeupTotal = -1;
    private int mWakeupWeekDay = -1;
    private int mWakeupWeekEnd = -1;
    private int minDayForAverage = 7;
    private ScheduleTodoWork.TodoWorkHandler todoSleepWork = new ScheduleTodoWork.TodoWorkHandler() {
        public final void execute$3b2d1350() {
            Log.v(SleepDetectionEngine.this.TAG, "execute sleep detection schedule");
            SleepDetectionEngine.this.procSleepDetection();
        }
    };

    public static synchronized SleepDetectionEngine getInstance() {
        SleepDetectionEngine sleepDetectionEngine;
        synchronized (SleepDetectionEngine.class) {
            if (mInstance == null) {
                mInstance = new SleepDetectionEngine();
            }
            sleepDetectionEngine = mInstance;
        }
        return sleepDetectionEngine;
    }

    public final void startSleepDetection(Context context) {
        if (context != null) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                if (powerManager.isScreenOn()) {
                    DatabaseManager.getInstance().insertData(new ScreenModel(System.currentTimeMillis(), 1, 0, 0));
                } else {
                    DatabaseManager.getInstance().insertData(new ScreenModel(System.currentTimeMillis(), 0, 0, 0));
                }
            }
            stopSleepDetection(context);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            this.mScreenReceiver = new Receiver_Screen();
            context.registerReceiver(this.mScreenReceiver, filter);
            if (context != null) {
                ScheduleManager.getInstance().addSchedule(context,
                        new ScheduleTodoWork(0, ScheduleTodoWork.TriggerTime.MIDNIGHT_HALF_TIME, 43200000, true, "com.samsung.android.sleepdetection.alarmtarget.sleep", this.todoSleepWork));
            }
        }
    }

    public final void stopSleepDetection(Context context) {
        if (context != null) {
            try {
                context.unregisterReceiver(this.mScreenReceiver);
            } catch (IllegalArgumentException e) {
            }
            this.mScreenReceiver = null;
            ScheduleManager.getInstance();
            ScheduleManager.removeSchedule(context, "com.samsung.android.sleepdetection.alarmtarget.sleep");
        }
    }

    public static ArrayList<SleepTimeModel> getSleepTime(long start, long end) {
        ArrayList<SleepTimeModel> returnValue = new ArrayList();
        ArrayList<SleepTimeModel> sleepTimeList = DatabaseManager.getInstance().getSleepTimeDataByStartTime(start, end);
        if (sleepTimeList != null) {
            Iterator it = sleepTimeList.iterator();
            while (it.hasNext()) {
                SleepTimeModel sleepTime = (SleepTimeModel) it.next();
                if (sleepTime.ignoreSleep == 0) {
                    returnValue.add(sleepTime);
                }
            }
        }
        return returnValue;
    }

    public final void procSleepDetection() {
        if (DatabaseManager.getInstance().getFirstScreenData() != null) {
            ArrayList<SleepTimeModel> candidateSleepList;
            long endTime = System.currentTimeMillis() + 3600000 * 24 * 30 * 12 ;
            long startTime = endTime - 2592000000L;
            ArrayList<ScreenModel> screenDataList = DatabaseManager.getInstance().getScreenData(startTime, endTime);
            if (screenDataList == null || screenDataList.size() == 0) {
                screenDataList = null;
            } else {
                long time;
                if (startTime == 0) {
                    time = ((ScreenModel) screenDataList.get(0)).getTime();
                } else {
                    time = startTime;
                }
                ScreenModel lastScreenData = DatabaseManager.getInstance().getLastScreenData(time);
                if (lastScreenData != null) {
                    lastScreenData.setTime(time);
                    screenDataList.add(0, lastScreenData);
                    if (screenDataList.size() == 1) {
                        screenDataList.add(new ScreenModel(endTime, lastScreenData.getScreenState(), lastScreenData.getUserPresent(), lastScreenData.getUseKeyGuard()));
                    }
                }
                if (screenDataList != null) {
                    int size = screenDataList.size();
                    int i = 0;
                    int counter = 0;
                    while (counter < size) {
                        if (screenDataList.get(i).getScreenState() == 1 && screenDataList.get(i).getUserPresent() == 0
                                && screenDataList.get(i).getUseKeyGuard() == 1) {
                            screenDataList.remove(i);
                        } else {
                            i++;
                        }
                        counter++;
                    }
                }
            }
            DatabaseManager.getInstance().deletePrevScreenData(startTime);
            if (screenDataList == null || screenDataList.size() == 0) {
                candidateSleepList = null;
            } else {
                candidateSleepList = new ArrayList();

                int screenState = screenDataList.get(0).getScreenState();
                long curTime = screenDataList.get(0).getTime();
                Iterator it = screenDataList.iterator();
                while (it.hasNext()) {
                    ScreenModel screenModel = (ScreenModel) it.next();
                    if (screenState != screenModel.getScreenState()) {
                        if (screenModel != screenDataList.get(screenDataList.size() - 1) || screenModel.getScreenState() != 0) {
                            if (screenState == 0) {
                                candidateSleepList.add(new SleepTimeModel(System.currentTimeMillis(), screenState, curTime, screenModel.getTime() - 1));
                            }
                            screenState = screenModel.getScreenState();
                            curTime = screenModel.getTime();
                        }
                    }
                }
            }
            ArrayList<SleepTimeModel> filteredSleepList = processSleepTime(candidateSleepList);
//            if (filteredSleepList != null && filteredSleepList.size() > 0) {
//                if (getMidnightTime(filteredSleepList.get(filteredSleepList.size() - 1).endTime)
//                        - getMidnightTime((filteredSleepList.get(0)).endTime) >= 86400000 * ((long) (this.minDayForAverage - 1))) {
//                    checkAverageSleep(filteredSleepList, 0);
//                    checkAverageSleep(filteredSleepList, 1);
//                    checkAverageSleep(filteredSleepList, 2);
//                }
//            }
            ArrayList<SleepTimeModel> insertData = filteredSleepList;
            DatabaseManager.getInstance().deleteSleepTime();
            Iterator it2 = insertData.iterator();
            while (it2.hasNext()) {
                DatabaseManager.getInstance().insertData((SleepTimeModel) it2.next());
            }
        }
    }

//    private void checkAverageSleep(ArrayList<SleepTimeModel> sleepList, int weekType) {
//        Calendar calendar = Calendar.getInstance();
//        int medianStartIndex = getMedianSleepTime(sleepList, true);
//        int medianEndIndex = getMedianSleepTime(sleepList, false);
//        if (medianStartIndex >= 0 && medianEndIndex >= 0) {
//            int offsetTime;
//            calendar.setTimeInMillis(((SleepTimeModel) sleepList.get(medianStartIndex)).startTime);
//            int medianAsleep = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//            if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                medianAsleep += 1440;
//            }
//            calendar.setTimeInMillis(((SleepTimeModel) sleepList.get(medianEndIndex)).endTime);
//            int medianWakeup = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//            if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                medianWakeup += 1440;
//            }
//            if (Math.abs(medianWakeup - medianAsleep) < 240) {
//                if (medianAsleep <= 1080 || medianAsleep >= 1800) {
//                    calendar.setTimeInMillis(((SleepTimeModel) sleepList.get(medianEndIndex)).startTime);
//                    medianAsleep = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//                    if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                        medianAsleep += 1440;
//                    }
//                } else {
//                    calendar.setTimeInMillis(((SleepTimeModel) sleepList.get(medianStartIndex)).endTime);
//                    medianWakeup = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//                    if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                        medianWakeup += 1440;
//                    }
//                }
//            }
//            if (weekType == 2) {
//                offsetTime = 480;
//            } else {
//                offsetTime = 360;
//            }
//            ArrayList<SleepTimeModel> filteredList = new ArrayList();
//            int prevDay = 0;
//            Iterator it = sleepList.iterator();
//            while (it.hasNext()) {
//                SleepTimeModel sleep = (SleepTimeModel) it.next();
//                if (sleep.ignoreSleep == 0) {
//                    if (weekType != 0) {
//                        if (checkWeekType(sleep.endTime) != weekType) {
//                        }
//                    }
//                    if (weekType == 0) {
//                        if (checkWeekType(sleep.endTime) == 2) {
//                            offsetTime = 480;
//                        } else {
//                            offsetTime = 360;
//                        }
//                    }
//                    calendar.setTimeInMillis(sleep.startTime);
//                    int curAsleep = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//                    if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                        curAsleep += 1440;
//                    }
//                    calendar.setTimeInMillis(sleep.endTime);
//                    int curWakeup = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//                    if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                        curWakeup += 1440;
//                    }
//                    boolean diffDay = true;
//                    long j = sleep.endTime;
//                    Calendar instance = Calendar.getInstance();
//                    instance.setTimeInMillis(j);
//                    int curDay = instance.get(Calendar.DAY_OF_YEAR);
//                    if (prevDay != 0 && prevDay == curDay) {
//                        diffDay = false;
//                    }
//                    if (!diffDay || Math.abs(curAsleep - medianAsleep) >= offsetTime || Math.abs(curWakeup - medianWakeup) >= offsetTime) {
//                        sleep.ignoreSleep = 1;
//                    } else {
//                        filteredList.add(sleep);
//                        prevDay = curDay;
//                    }
//                }
//            }
//            updateAvgSleepTime(filteredList, weekType);
//        }
//    }

//    private int checkWeekType(long time) {
//        Object obj;
//        Calendar instance = Calendar.getInstance();
//        instance.setTimeInMillis(time);
//        int i = instance.get(Calendar.DAY_OF_WEEK);
//        if (i == 7 || i == 1) {
//            obj = 1;
//        } else {
//            obj = null;
//        }
//        if (obj == null) {
//            UserData userData = DatabaseManager.getInstance().getUserData();
//            if (userData != null) {
//                ArrayList holiday = userData.getHoliday();
//                if (holiday != null) {
//                    Iterator it = holiday.iterator();
//                    while (it.hasNext()) {
//                        TimeModel timeModel = (TimeModel) it.next();
//                        if (time < timeModel.getStartTime() || time > timeModel.getEndTime()) {
//                            if (time < timeModel.getStartTime()) {
//                                break;
//                            }
//                        } else {
//                            i = 1;
//                            break;
//                        }
//                    }
//                }
//            }
//            obj = null;
//            if (obj == null) {
//                return 1;
//            }
//        }
//        return 2;
//    }
//
//    private static long getMidnightTime(long time) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(time);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTimeInMillis();
//    }

    private ArrayList<SleepTimeModel> processSleepTime(ArrayList<SleepTimeModel> candidateSleepList) {
        SleepTimeModel prevSleep = null;
        ArrayList<SleepTimeModel> filteredSleepList = new ArrayList();
        boolean isMerged = false;
        if (candidateSleepList != null) {
            Iterator it = candidateSleepList.iterator();
            while (it.hasNext()) {
                SleepTimeModel curSleep = (SleepTimeModel) it.next();
                if (prevSleep == null || curSleep.startTime - prevSleep.endTime >= ((long) this.MAX_MERGE_INTERVAL)) {
                    if (checkSleepDuration(curSleep)) {
                        filteredSleepList.add(curSleep);
                    }
                    isMerged = false;
                } else if ((curSleep.getDuration() >= 7200000 || prevSleep.getDuration() >= 7200000) && curSleep.getDuration() >= this.MIN_MERGE_DURATION && prevSleep.getDuration() >= this.MIN_MERGE_DURATION) {
                    long prevStartTime = prevSleep.startTime;
                    if (isMerged && filteredSleepList.size() > 0) {
                        prevStartTime = ((SleepTimeModel) filteredSleepList.get(filteredSleepList.size() - 1)).startTime;
                    }
                    SleepTimeModel newSleep = new SleepTimeModel(curSleep.checkTime, curSleep.ignoreSleep, prevStartTime, curSleep.endTime);
                    if (checkSleepDuration(prevSleep) && filteredSleepList.size() > 0) {
                        filteredSleepList.remove(filteredSleepList.size() - 1);
                    }
                    if (checkSleepDuration(newSleep)) {
                        filteredSleepList.add(newSleep);
                        isMerged = true;
                        curSleep = newSleep;
                    } else {
                        isMerged = false;
                    }
                } else {
                    if (checkSleepDuration(curSleep)) {
                        filteredSleepList.add(curSleep);
                    }
                    isMerged = false;
                }
                prevSleep = curSleep;
            }
        }
        return filteredSleepList;
    }

//    private static int getMedianSleepTime(ArrayList<SleepTimeModel> sleepList, boolean byStartTime) {
//        Calendar calendar = Calendar.getInstance();
//        List<Integer> sleepCandidateList = new ArrayList();
//        Iterator it = sleepList.iterator();
//        while (it.hasNext()) {
//            SleepTimeModel sleepTime = (SleepTimeModel) it.next();
//            if (byStartTime) {
//                calendar.setTimeInMillis(sleepTime.startTime);
//            } else {
//                calendar.setTimeInMillis(sleepTime.endTime);
//            }
//            int candidateTime = (((calendar.get(Calendar.HOUR_OF_DAY) * 60) * 60) + (calendar.get(Calendar.MINUTE) * 60)) + calendar.get(Calendar.SECOND);
//            if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                candidateTime += 86400;
//            }
//            sleepCandidateList.add(Integer.valueOf(candidateTime));
//        }
//        if (sleepCandidateList.size() <= 0) {
//            return -1;
//        }
//        int medianTime;
//        if (sleepCandidateList.size() > 1) {
//            Object[] sleepCandidateTime = sleepCandidateList.toArray();
//            Arrays.sort(sleepCandidateTime);
//            int middle = sleepCandidateTime.length / 2;
//            if (byStartTime) {
//                medianTime = ((Integer) sleepCandidateTime[middle - 1]).intValue();
//            } else if (sleepCandidateList.size() % 2 == 0) {
//                medianTime = ((Integer) sleepCandidateTime[middle]).intValue();
//            } else {
//                medianTime = ((Integer) sleepCandidateTime[middle + 1]).intValue();
//            }
//        } else {
//            medianTime = ((Integer) sleepCandidateList.get(0)).intValue();
//        }
//        for (int i = 0; i < sleepList.size(); i++) {
//         SleepTimeModel sleepTime = (SleepTimeModel) sleepList.get(i);
//            if (byStartTime) {
//                calendar.setTimeInMillis(sleepTime.startTime);
//            } else {
//                calendar.setTimeInMillis(sleepTime.endTime);
//            }
//            long candidateTime = (((calendar.get(Calendar.HOUR_OF_DAY) * 60) * 60) + (calendar.get(Calendar.MINUTE) * 60)) + calendar.get(Calendar.SECOND);
//            if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                candidateTime += 86400;
//            }
//            if (candidateTime == medianTime) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    private void updateAvgSleepTime(ArrayList<SleepTimeModel> sleepList, int weekType) {
//        if (sleepList.size() != 0) {
//            int asleepTime;
//            int wakeupTime;
//            Calendar calendar = Calendar.getInstance();
//            int sumStartTime = 0;
//            int sumEndTime = 0;
//            Iterator it = sleepList.iterator();
//            while (it.hasNext()) {
//                SleepTimeModel sleepTime = (SleepTimeModel) it.next();
//                calendar.setTimeInMillis(sleepTime.startTime);
//                int curStartTime = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//                if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                    curStartTime += 1440;
//                }
//                sumStartTime += curStartTime;
//                calendar.setTimeInMillis(sleepTime.endTime);
//                int curEndTime = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
//                if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
//                    curEndTime += 1440;
//                }
//                sumEndTime += curEndTime;
//            }
//            int avgStartTime = sumStartTime / sleepList.size();
//            int avgEndTime = sumEndTime / sleepList.size();
//            if (avgStartTime >= 1440) {
//                asleepTime = avgStartTime - 1440;
//            } else {
//                asleepTime = avgStartTime;
//            }
//            if (avgEndTime >= 1440) {
//                wakeupTime = avgEndTime - 1440;
//            } else {
//                wakeupTime = avgEndTime;
//            }
//            switch (weekType) {
//                case 0:
//                    this.mAsleepTotal = asleepTime;
//                    this.mWakeupTotal = wakeupTime;
//                    return;
//                case 1:
//                    this.mAsleepWeekDay = asleepTime;
//                    this.mWakeupWeekDay = wakeupTime;
//                    return;
//                case 2:
//                    this.mAsleepWeekEnd = asleepTime;
//                    this.mWakeupWeekEnd = wakeupTime;
//                    return;
//                default:
//                    return;
//            }
//        }
//    }

    private static boolean checkSleepDuration(SleepTimeModel sleep) {
        android.util.Log.e("HEALH", "checkSleepDuration: " + sleep.getDuration());
        if (sleep.getDuration() < 7200000 || sleep.getDuration() >= 57600000) {
            return false;
        }
        return true;
    }
}
