package za.healthtracking.sleepdetectionlib.util;

import java.util.Calendar;
import java.util.Date;

public final class TimeChartUtils {
    public static final float[] BARWIDTH = new float[]{23.0f, 29.0f, 36.0f};
    public static final double[] DEPTHS = new double[]{1000.0d, 60000.0d, 3600000.0d, 8.64E7d, 6.048E8d, 2.592E9d, 3.1536E10d};
    public static final double[] MONTH_INTERVALS = new double[]{2.6784E9d, 2.6784E9d, 2.4192E9d, 2.6784E9d, 2.592E9d, 2.6784E9d, 2.592E9d, 2.6784E9d, 2.6784E9d, 2.592E9d, 2.6784E9d, 2.592E9d, 2.6784E9d};
    public static final float[] SLEEPNSTEPBARWIDTH = new float[]{10.0f, 12.0f, 14.0f};

    public static double getMultiplyEpochTime(double d, double d2, int i) {
        Date date = new Date((long) d);
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        if (d2 == 60000.0d) {
            instance.add(12, i);
        }
        if (d2 == 3600000.0d) {
            instance.add(10, i);
        }
        if (d2 == 8.64E7d) {
            instance.add(5, i);
        }
        if (d2 == 6.048E8d) {
            instance.add(5, i * 7);
        }
        if (d2 == 2.592E9d) {
            instance.add(2, i);
        }
        if (d2 == 1000.0d) {
            instance.add(13, i);
        }
        if (d2 == 3.1536E10d) {
            instance.add(1, i);
        }
        if (d2 == 8.64E7d || d2 == 6.048E8d || d2 == 2.592E9d) {
            if (instance.get(11) < 0 || instance.get(11) > 12) {
                instance.add(5, 1);
                instance.set(11, 0);
                instance.set(12, 0);
                instance.set(13, 0);
                instance.set(14, 0);
            } else {
                instance.set(11, 0);
                instance.set(12, 0);
                instance.set(13, 0);
                instance.set(14, 0);
            }
        }
        return (double) instance.getTime().getTime();
    }
}
