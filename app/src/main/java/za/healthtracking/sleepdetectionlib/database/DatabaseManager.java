package za.healthtracking.sleepdetectionlib.database;

import android.app.KeyguardManager;
import android.content.Context;

import java.util.ArrayList;

import za.healthtracking.sleepdetectionlib.collector.ScreenModel;
import za.healthtracking.sleepdetectionlib.database.model.DBTable_Screen;
import za.healthtracking.sleepdetectionlib.database.model.DBTable_SleepTime;
import za.healthtracking.sleepdetectionlib.database.model.DBTable_UserInfo;
import za.healthtracking.sleepdetectionlib.engine.SleepTimeModel;
import za.healthtracking.sleepdetectionlib.info.UserData;
import za.healthtracking.sleepdetectionlib.main.SleepDetectionManager;
import za.healthtracking.sleepdetectionlib.util.Log;

public final class DatabaseManager {
    private static DatabaseManager mInstance;

    public static synchronized DatabaseManager getInstance() {
        DatabaseManager databaseManager;
        synchronized (DatabaseManager.class) {
            if (mInstance == null) {
                mInstance = new DatabaseManager();
            }
            databaseManager = mInstance;
        }
        return databaseManager;
    }

    public static void onDestroy() {
        DatabaseOpenHelper_Switch.getInstance();
        if (mInstance != null) {
            synchronized (mInstance) {
                mInstance = null;
            }
        }
    }

    public final void insertData(ScreenModel data) {
        synchronized (this) {
            try {
               /* if (SleepDetectionManager.mContext == null) {
                    data.setUseKeyGuard(0);
                } else if (((KeyguardManager) SleepDetectionManager.mContext.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode()) {
                    data.setUseKeyGuard(1);
                } else {
                    data.setUseKeyGuard(0);
                }*/
                data.setUseKeyGuard(0);
                if (DatabaseOpenHelper_Switch.getInstance().getDatabase() != null) {
                    DBTable_Screen.getInstance().insertData(DatabaseOpenHelper_Switch.getInstance().getDatabase(), data);
                }
            } catch (IllegalStateException e) {
                Log.v("DatabaseManager", "DB insert exception.");
            }
        }
    }

    public final ArrayList<ScreenModel> getScreenData(long startTime, long endTime) {
        ArrayList<ScreenModel> data;
        synchronized (this) {
            data = DBTable_Screen.getInstance().getData(DatabaseOpenHelper_Switch.getInstance().getDatabase(), startTime, endTime);
        }
        return data;
    }

    public final ScreenModel getFirstScreenData() {
        ArrayList<ScreenModel> datas = DBTable_Screen.getInstance().getFirstData(DatabaseOpenHelper_Switch.getInstance().getDatabase());
        if (datas.size() > 0) {
            return datas.get(0);
        }
        return null;
    }

    public final ScreenModel getLastScreenData(long r4) {
        ArrayList<ScreenModel> datas = DBTable_Screen.getInstance().getLastData(DatabaseOpenHelper_Switch.getInstance().getDatabase(), r4);
        if (datas.size() > 0) {
            return datas.get(0);
        }
        return null;
    }

    public final void deletePrevScreenData(long baseTime) {
        synchronized (this) {
            DBTable_Screen.getInstance().deletePrevData(DatabaseOpenHelper_Switch.getInstance().getDatabase(), baseTime);
        }
    }

    public final void insertData(SleepTimeModel data) {
        synchronized (this) {
            try {
                DBTable_SleepTime.getInstance().insertData(DatabaseOpenHelper_Switch.getInstance().getDatabase(), data);
            } catch (IllegalStateException e) {
                Log.v("DatabaseManager", "DB insert exception.");
            }
        }
    }

    public final ArrayList<SleepTimeModel> getSleepTimeDataByStartTime(long startTime, long endTime) {
        ArrayList<SleepTimeModel> dataByStartTime;
        synchronized (this) {
            dataByStartTime = DBTable_SleepTime.getInstance().getDataByStartTime(DatabaseOpenHelper_Switch.getInstance().getDatabase(), startTime, endTime);
        }
        return dataByStartTime;
    }

    public final void deleteSleepTime() {
        synchronized (this) {
            DBTable_SleepTime.getInstance();
            DBTable_SleepTime.deleteAllData(DatabaseOpenHelper_Switch.getInstance().getDatabase());
        }
    }

    public final UserData getUserData() {
        UserData userData;
        synchronized (this) {
            userData = DBTable_UserInfo.getInstance().getUserData(DatabaseOpenHelper_Switch.getInstance().getDatabase());
        }
        return userData;
    }
}
