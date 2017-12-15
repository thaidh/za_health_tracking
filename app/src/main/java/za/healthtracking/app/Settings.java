package za.healthtracking.app;

import za.healthtracking.utils.SharePrefs;

/**
 * Created by hiepmt on 10/07/2017.
 */

public class Settings {
    public static float defaultStepLength = 0.73f;

    public static int UI_UPDATE_DELAY_IN_MS = 1000;
    public static int INTERVAL_ACTIVITY = 20 * 60;

    static final String USER_PROFILE_WEIGHT = "user_profile_weight";
    public static float getUserProfileWeight() {
        return SharePrefs.getInstance().GetFloat(USER_PROFILE_WEIGHT);
    }

    public static void setUserProfileWeight(float weight) {
        SharePrefs.getInstance().SetFloat(USER_PROFILE_WEIGHT, weight);
    }

    static final String USER_PROFILE_MALE = "user_profile_male";
    public static boolean getUserProfileIsMale() {
        return SharePrefs.getInstance().GetBoolean(USER_PROFILE_MALE, true);
    }

    public static void setUserProfileIsMale(boolean isMale) {
        SharePrefs.getInstance().SetBoolean(USER_PROFILE_MALE, isMale);
    }
}
