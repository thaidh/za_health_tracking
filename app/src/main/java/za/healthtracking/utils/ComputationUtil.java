package za.healthtracking.utils;

import za.healthtracking.pedometer.SportType;

/**
 * Created by hiepmt on 10/07/2017.
 */

public class ComputationUtil {
    private static float calA(float weight) {
        return ((weight + 8.0f) * 9.81f) * 0.005f;
    }

    private static float calB(float weight) {
        return ((weight + 14.0f) * 9.81f) * ((0.0046f * ((float) Math.cos(0.04d))) + ((float) Math.sin(0.04d)));
    }

    public static float calcSpeedWithLimit(float f, long j) {
        return (float) Math.min((double) (calcSpeed(f, j) * 3.6f), 999.99d);
    }

    public static float calcSpeed(float distance, long duration) {
        if (duration > 0) {
            return distance / (duration / 1000.0f);
        }
        return 0.0f;
    }

    public static float convertKilometerToMile(float f) {
        return f / 1.609344f;
    }

    public static float convertMileToKilometer(float f) {
        return 1.609344f * f;
    }


    public static float calcCalories(float speed, long duration, float weight, int sportType, boolean isMale) {
        float f3 = speed / 3.6f;
        if (weight == 0.0f) {
            weight = 75.0f;
        }

        switch (sportType) {
            case 4:
                return (int) ((((((calB(weight) + ((0.3258626f * f3) * f3)) + (f3 * 0.099920094f)) * (1.025f * f3)) * ((float) duration)) / 1000.0f) * 0.0010857764f);
            case 22:
                return (int) ((((((calA(weight) + ((0.43857762f * f3) * f3)) + (f3 * 0.1f)) * (1.03f * f3)) * ((float) duration)) / 1000.0f) * 0.0010857764f);
            default:
                double a = SportType.a(sportType, (double) convertKilometerToMile(speed), weight, isMale);
                return (float)((a * (((double) duration) / 60000.0d)) * ((double) weight));
        }
    }
}
