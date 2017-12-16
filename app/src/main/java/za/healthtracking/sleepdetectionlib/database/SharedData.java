package za.healthtracking.sleepdetectionlib.database;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import za.healthtracking.sleepdetectionlib.main.SleepDetectionResultEnum;

public final class SharedData {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$samsung$android$sleepdetectionlib$database$SharedData$ValueClassType;
    private static SharedData instance = null;

    public enum PreferenceValue {
        UNKNOWN,
        SECUREDB_PASSWORD,
        SERVICE_STATUS,
        SLEEP
    }

    public enum ValueClassType {
        UNKNOWN,
        BOOLEAN,
        INTEGER,
        FLOAT,
        LONG,
        STRING,
        BYTE_ARRAY
    }

    private static /* synthetic */ int[] $SWITCH_TABLE$com$samsung$android$sleepdetectionlib$database$SharedData$ValueClassType() {
        int[] iArr = $SWITCH_TABLE$com$samsung$android$sleepdetectionlib$database$SharedData$ValueClassType;
        if (iArr == null) {
            iArr = new int[ValueClassType.values().length];
            try {
                iArr[ValueClassType.BOOLEAN.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[ValueClassType.BYTE_ARRAY.ordinal()] = 7;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[ValueClassType.FLOAT.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[ValueClassType.INTEGER.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[ValueClassType.LONG.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[ValueClassType.STRING.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[ValueClassType.UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            $SWITCH_TABLE$com$samsung$android$sleepdetectionlib$database$SharedData$ValueClassType = iArr;
        }
        return iArr;
    }

    public static synchronized SharedData getInstance() {
        SharedData sharedData;
        synchronized (SharedData.class) {
            if (instance == null) {
                instance = new SharedData();
            }
            sharedData = instance;
        }
        return sharedData;
    }

    private static SleepDetectionResultEnum checkKey(PreferenceValue keyType) {
        try {
            PreferenceValue.valueOf(keyType.toString());
            return SleepDetectionResultEnum.RESULT_OK;
        } catch (IllegalArgumentException e) {
            return SleepDetectionResultEnum.RESULT_ERROR_NOT_SUPPORTED;
        }
    }

    public final SleepDetectionResultEnum setSharedPreference(Context context, PreferenceValue key, Object value) {
        ValueClassType classType = ValueClassType.UNKNOWN;
        if (value instanceof Boolean) {
            classType = ValueClassType.BOOLEAN;
        } else if (value instanceof String) {
            classType = ValueClassType.STRING;
        } else if (value instanceof Integer) {
            classType = ValueClassType.INTEGER;
        } else if (value instanceof Float) {
            classType = ValueClassType.FLOAT;
        } else if (value instanceof Long) {
            classType = ValueClassType.LONG;
        }
        if (context == null || key == null) {
            return SleepDetectionResultEnum.RESULT_ERROR_INITIALIZE;
        }
        if (checkKey(key) != SleepDetectionResultEnum.RESULT_OK) {
            return SleepDetectionResultEnum.RESULT_ERROR_NOT_SUPPORTED;
        }
        Editor edit = context.getSharedPreferences("Pref_Foothold", 0).edit();
        switch ($SWITCH_TABLE$com$samsung$android$sleepdetectionlib$database$SharedData$ValueClassType()[classType.ordinal()]) {
            case 2:
                edit.putBoolean(key.toString(), ((Boolean) value).booleanValue());
                break;
            case 3:
                edit.putInt(key.toString(), ((Integer) value).intValue());
                break;
            case 4:
                edit.putFloat(key.toString(), ((Float) value).floatValue());
                break;
            case 5:
                edit.putLong(key.toString(), ((Long) value).longValue());
                break;
            case 6:
                edit.putString(key.toString(), value.toString());
                break;
            case 7:
                edit.putString(key.toString(), Base64.encodeToString((byte[]) value, 0));
                break;
            default:
                return SleepDetectionResultEnum.RESULT_ERROR_NOT_SUPPORTED;
        }
        edit.commit();
        return SleepDetectionResultEnum.RESULT_OK;
    }
}
