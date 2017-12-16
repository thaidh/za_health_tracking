package za.healthtracking.sleepdetectionlib.database.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import za.healthtracking.sleepdetectionlib.info.UserData;

public final class DBTable_UserInfo {
    private static DBTable_UserInfo instance = null;

    public static synchronized DBTable_UserInfo getInstance() {
        DBTable_UserInfo dBTable_UserInfo;
        synchronized (DBTable_UserInfo.class) {
            if (instance == null) {
                instance = new DBTable_UserInfo();
            }
            dBTable_UserInfo = instance;
        }
        return dBTable_UserInfo;
    }

    public static void createTable(SQLiteDatabase db) {
        if (db != null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS user_info ( id INTEGER,info TEXT,blob BLOB)");
        }
    }

    public final UserData getUserData(SQLiteDatabase readDB) {
        return loadData(readDB, "SELECT * FROM user_info WHERE id = 0");
    }

    private UserData loadData(SQLiteDatabase readDB, String query) {
        UserData userData = null;
        if (readDB != null) {
            Cursor cursor = null;
            try {
                cursor = readDB.rawQuery(query, null);
            } catch (SQLiteException e) {
                createTable(readDB);
            }
            if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
                do {
                    userData = new UserData();
                    userData.setBlobUserData(cursor.getBlob(2));
                } while (cursor.moveToNext());
                if (cursor != null) {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
        }
        return userData;
    }
}
