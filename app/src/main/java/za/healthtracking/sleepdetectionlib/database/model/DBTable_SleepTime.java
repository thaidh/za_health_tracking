package za.healthtracking.sleepdetectionlib.database.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;

import za.healthtracking.sleepdetectionlib.engine.SleepTimeModel;

public final class DBTable_SleepTime {
    private static DBTable_SleepTime instance = null;

    public static synchronized DBTable_SleepTime getInstance() {
        DBTable_SleepTime dBTable_SleepTime;
        synchronized (DBTable_SleepTime.class) {
            if (instance == null) {
                instance = new DBTable_SleepTime();
            }
            dBTable_SleepTime = instance;
        }
        return dBTable_SleepTime;
    }

    public static void createTable(SQLiteDatabase db) {
        if (db != null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS sleep_time ( time LONG,timeText TEXT,ignoreSleep INT,startTime LONG,startTimeText TEXT,endTime LONG,endTimeText TEXT)");
        }
    }

    public final void insertData(SQLiteDatabase writeDB, SleepTimeModel data) {
        if (writeDB != null && data != null) {
            try {
                writeDB.beginTransaction();
                String str = "sleep_time";
                ContentValues contentValues = new ContentValues();
                if (data != null) {
                    contentValues.put("time", Long.valueOf(data.getCheckTime()));
                    contentValues.put("timeText", data.getCheckTimeText());
                    contentValues.put("ignoreSleep", Integer.valueOf(data.getIgnoreSleep()));
                    contentValues.put("startTime", Long.valueOf(data.getStartTime()));
                    contentValues.put("startTimeText", data.getStartTimeText());
                    contentValues.put("endTime", Long.valueOf(data.getEndTime()));
                    contentValues.put("endTimeText", data.getEndTimeText());
                }
                writeDB.insert(str, null, contentValues);
                writeDB.setTransactionSuccessful();
            } catch (SQLException e) {
            } finally {
                writeDB.endTransaction();
            }
        }
    }

    public final ArrayList<SleepTimeModel> getDataByStartTime(SQLiteDatabase readDB, long startTime, long endTime) {
        return loadData(readDB, "SELECT * FROM sleep_time WHERE startTime >= " + startTime + " AND startTime" + " <= " + endTime);
    }

    public static void deleteAllData(SQLiteDatabase writeDB) {
        if (writeDB != null) {
            writeDB.execSQL("DELETE FROM sleep_time;");
        }
    }

    private ArrayList<SleepTimeModel> loadData(SQLiteDatabase readDB, String query) {
        ArrayList<SleepTimeModel> sleepTimeList = new ArrayList();
        if (!(readDB == null || query == null)) {
            Cursor cursor = null;
            try {
                cursor = readDB.rawQuery(query, null);
            } catch (SQLiteException e) {
                createTable(readDB);
            }
            if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
                cursor.close();
            } else {
                do {
                    sleepTimeList.add(new SleepTimeModel(cursor.getLong(0), cursor.getInt(2), cursor.getLong(3), cursor.getLong(5)));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return sleepTimeList;
    }
}
