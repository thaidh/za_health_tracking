package za.healthtracking.sleepdetectionlib.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteException;

import za.healthtracking.sleepdetectionlib.database.model.DBTable_Screen;
import za.healthtracking.sleepdetectionlib.database.model.DBTable_SleepTime;
import za.healthtracking.sleepdetectionlib.database.model.DBTable_UserInfo;
import za.healthtracking.sleepdetectionlib.main.SleepDetectionManager;
import za.healthtracking.sleepdetectionlib.util.Log;

final class DatabaseOpenHelper_Switch {
    private static DatabaseOpenHelper_Switch mInstance = null;
    private SQLiteDatabase database;
    private Database_SQLite sqlite;

    private DatabaseOpenHelper_Switch() {
        if (SleepDetectionManager.mContext != null) {
            this.sqlite = new Database_SQLite(SleepDetectionManager.mContext);
        }
    }

    public static synchronized DatabaseOpenHelper_Switch getInstance() {
        DatabaseOpenHelper_Switch databaseOpenHelper_Switch;
        synchronized (DatabaseOpenHelper_Switch.class) {
            if (mInstance == null) {
                mInstance = new DatabaseOpenHelper_Switch();
            }
            databaseOpenHelper_Switch = mInstance;
        }
        return databaseOpenHelper_Switch;
    }

    public final synchronized SQLiteDatabase getDatabase() {
        SQLiteDatabase writableDatabase;
        synchronized (this) {
            if (this.database == null) {
                try {
                    if (this.database == null) {
                        if (this.sqlite != null) {
                            writableDatabase = this.sqlite.getWritableDatabase();
                        } else {
                            writableDatabase = null;
                        }
                        this.database = writableDatabase;
                    }
                } catch (SQLiteDatabaseLockedException e) {
                    Log.e("DatabaseOpenHelper_Switch", "Database open helper error : Lockedexception");
                } catch (SQLiteException e2) {
                    Log.e("DatabaseOpenHelper_Switch", "Database open helper error : exception");
                }
            }
            writableDatabase = this.database;
        }
        return writableDatabase;
    }

    public static void onCreate(SQLiteDatabase db) {
        DBTable_Screen.getInstance();
        DBTable_Screen.createTable(db);
        DBTable_SleepTime.getInstance();
        DBTable_SleepTime.createTable(db);
        DBTable_UserInfo.getInstance();
        DBTable_UserInfo.createTable(db);
    }

    public static void onUpgrade$621a88f2(SQLiteDatabase db, int oldVersion) {
        switch (oldVersion) {
            case 10:
                db.execSQL("ALTER TABLE sleep_time RENAME TO tmp_sleep_time");
                db.execSQL("CREATE TABLE sleep_time ( time LONG,timeText TEXT,ignoreSleep INT,startTime LONG,startTimeText TEXT,endTime LONG,endTimeText TEXT)");
                db.execSQL("INSERT INTO sleep_time (time, timeText, ignoreSleep, startTime, startTimeText, endTime, endTimeText) SELECT time, timeText, sleepState, startTime, startTimeText, endTime, endTimeText FROM tmp_sleep_time");
                db.execSQL("DROP TABLE tmp_sleep_time");
                db.execSQL("ALTER TABLE logging_data RENAME TO screen_data");
                return;
            default:
                return;
        }
    }
}
