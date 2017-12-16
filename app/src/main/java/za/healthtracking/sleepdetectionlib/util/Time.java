package za.healthtracking.sleepdetectionlib.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class Time {
    public static long timeZeroSet(long time) {
        return getTime(time, "yyyyMMdd");
    }

    public static long timeZeroSetMinute(long time) {
        return getTime(time, "yyyyMMddHHmm");
    }

    private static long getTime(long time, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String sDay = dateFormat.format(new Date(time));
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(sDay));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal.getTimeInMillis();
    }

    public static String changeTimeText(long time, String timeForamt) {
        if (time < 0) {
            return "";
        }
        return new SimpleDateFormat(timeForamt).format(new Date(time));
    }
}
