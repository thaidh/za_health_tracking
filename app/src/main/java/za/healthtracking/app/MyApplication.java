package za.healthtracking.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by hiepmt on 27/07/2017.
 */

public class MyApplication extends Application {
    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return mContext;
    }
}
