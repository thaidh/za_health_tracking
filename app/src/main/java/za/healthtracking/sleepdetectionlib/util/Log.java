package za.healthtracking.sleepdetectionlib.util;

import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public final class Log {
    public static boolean RELEASE_MODE = true;
    private static HashMap<String, Object> mLogMessengerStorage = new HashMap();
    private static boolean mWritetoFile = true;
    private static boolean mWritetoLogcat = true;

    private static final void log(String type, String tag, String msg) {
        if (RELEASE_MODE) {
            android.util.Log.v(tag, msg);
        } else if (type.equals("v")) {
            android.util.Log.v(tag, msg);
        } else if (type.equals("e")) {
            android.util.Log.e(tag, msg);
        } else if (type.equals("w")) {
            android.util.Log.e(tag, msg);
        } else if (type.equals("d")) {
            android.util.Log.d(tag, msg);
        } else if (type.equals("i")) {
            android.util.Log.i(tag, msg);
        }
    }

    public static final void v(String tag, String msg) {
        if (mWritetoLogcat) {
            msg = getMessage(tag, msg);
            log("v", "SleepDetection v1.1.0 ", msg);
        }
        if (mWritetoFile && !RELEASE_MODE) {
            writeToFile$14e1ec6d("v", msg);
        }
    }

    public static final void e(String tag, String msg) {
        if (mWritetoLogcat) {
            msg = getMessage(tag, msg);
            log("e", "SleepDetection v1.1.0 ", msg);
        }
        if (mWritetoFile && !RELEASE_MODE) {
            writeToFile$14e1ec6d("e", msg);
        }
    }

    private static String getMessage(String tag, String msg) {
        msg = "[" + tag + "::" + new Exception().fillInStackTrace().getStackTrace()[2].getMethodName() + "] " + msg;
        if (mLogMessengerStorage != null) {
            for (Entry event : mLogMessengerStorage.entrySet()) {
                event.getValue();
            }
        }
        return msg;
    }

    private static void writeToFile$14e1ec6d(String type, String text) {
    }
}
