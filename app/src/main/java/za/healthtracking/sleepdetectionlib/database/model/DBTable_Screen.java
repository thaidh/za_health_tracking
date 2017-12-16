package za.healthtracking.sleepdetectionlib.database.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;

import za.healthtracking.sleepdetectionlib.collector.ScreenModel;

public final class DBTable_Screen {
    private static DBTable_Screen instance = null;

    public static synchronized DBTable_Screen getInstance() {
        DBTable_Screen dBTable_Screen;
        synchronized (DBTable_Screen.class) {
            if (instance == null) {
                instance = new DBTable_Screen();
            }
            dBTable_Screen = instance;
        }
        return dBTable_Screen;
    }

    public static void createTable(SQLiteDatabase db) {
        if (db != null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS screen_data ( time LONG,timeText TEXT,screenState INT,userPresent INT,useKeyGuard INT);");
        }
    }

    public final void insertData(SQLiteDatabase writeDB, ScreenModel data) {
        if (writeDB != null && data != null) {
            try {
                writeDB.beginTransaction();
                String str = "screen_data";
                ContentValues contentValues = new ContentValues();
                if (data != null) {
                    contentValues.put("time", Long.valueOf(data.getTime()));
                    contentValues.put("timeText", data.getTimeText());
                    contentValues.put("screenState", Integer.valueOf(data.getScreenState()));
                    contentValues.put("userPresent", Integer.valueOf(data.getUserPresent()));
                    contentValues.put("useKeyGuard", Integer.valueOf(data.getUseKeyGuard()));
                }
                writeDB.insert(str, null, contentValues);
                writeDB.setTransactionSuccessful();
            } catch (SQLException e) {
            } finally {
                writeDB.endTransaction();
            }
        }
    }

    public final ArrayList<ScreenModel> getData(SQLiteDatabase readDB, long startTime, long endTime) {
        return loadData(readDB, "SELECT * FROM screen_data WHERE time >= " + startTime + " AND time" + " <= " + endTime);
    }

    public final ArrayList<ScreenModel> getLastData(SQLiteDatabase readDB, long baseTime) {
        return loadData(readDB, "SELECT * FROM screen_data WHERE time <= " + baseTime + " ORDER BY time" + " DESC LIMIT 1");
    }

    public final ArrayList<ScreenModel> getFirstData(SQLiteDatabase readDB) {
        return loadData(readDB, "SELECT * FROM screen_data ORDER BY time ASC LIMIT 1");
    }

    public final synchronized void deletePrevData(SQLiteDatabase writeDB, long baseTime) {
        writeDB.execSQL("DELETE FROM screen_data WHERE time < " + baseTime + ";");
    }

    private ArrayList<ScreenModel> loadData(SQLiteDatabase readDB, String query) {
        ArrayList<ScreenModel> loggingList = new ArrayList();
        if (!(readDB == null || query == null)) {
            Cursor cursor = null;
            try {
                cursor = readDB.rawQuery(query, null);
            } catch (SQLiteException e) {
                createTable(readDB);
            }
            if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
                cursor.close();
            } else {
                do {
                    loggingList.add(new ScreenModel(cursor.getLong(0), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4)));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return loggingList;
    }
}
