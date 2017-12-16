package za.healthtracking.sleepdetectionlib.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class Database_SQLite extends SQLiteOpenHelper {
    public Database_SQLite(Context context) {
        super(context, "SleepDetection.db", null, 11);
    }

    public final void onCreate(SQLiteDatabase db) {
        DatabaseOpenHelper_Switch.getInstance();
        DatabaseOpenHelper_Switch.onCreate(db);
    }

    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DatabaseOpenHelper_Switch.getInstance();
        DatabaseOpenHelper_Switch.onUpgrade$621a88f2(db, oldVersion);
    }
}
