package za.healthtracking.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import za.healthtracking.database.entities.DailyActivityLog;
import za.healthtracking.database.entities.MinutelyActivityLog;
import za.healthtracking.database.entities.RunningActivityLog;

/**
 * Created by hiepmt on 01/08/2017.
 */

public class DbHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "MDData.db";
    private static final int DATABASE_VERSION = 2;
    private Dao<MinutelyActivityLog, Integer> minutelyActivityLogDao;
    private Dao<DailyActivityLog, Integer> dailyActivityLogDao;
    private Dao<RunningActivityLog, Integer> runningActivityLogDao;

    public DbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.d("HiepIT", "DbHelper::onCreated");

        try {
            TableUtils.createTable(connectionSource, DailyActivityLog.class);
            TableUtils.createTable(connectionSource, MinutelyActivityLog.class);
            TableUtils.createTable(connectionSource, RunningActivityLog.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (newVersion == 2) {
            upgradeDBTo2(connectionSource, oldVersion);
        }
    }

    private void upgradeDBTo2(ConnectionSource connectionSource, int oldVersion) {
//        if (oldVersion < 1) {
//            upgradeDBTo13(connectionSource, i);
//        }
        try {
            TableUtils.createTable(connectionSource, RunningActivityLog.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Dao<MinutelyActivityLog, Integer> getMinutelyActivityLogDao() throws SQLException {
        if (this.minutelyActivityLogDao == null) {
            this.minutelyActivityLogDao = getDao(MinutelyActivityLog.class);
        }
        return this.minutelyActivityLogDao;
    }

    public Dao<DailyActivityLog, Integer> getDailyActivityLogDao() throws SQLException {
        if (this.dailyActivityLogDao == null) {
            this.dailyActivityLogDao = getDao(DailyActivityLog.class);
        }
        return this.dailyActivityLogDao;
    }

    public Dao<RunningActivityLog, Integer> getRunningActivityLogDao() throws SQLException {
        if (this.runningActivityLogDao == null) {
            this.runningActivityLogDao = getDao(RunningActivityLog.class);
        }
        return this.runningActivityLogDao;
    }

    @Override
    public void close() {
        super.close();
        minutelyActivityLogDao = null;
        dailyActivityLogDao = null;
    }
}
