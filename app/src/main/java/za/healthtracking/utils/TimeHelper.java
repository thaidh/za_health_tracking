package za.healthtracking.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import za.healthtracking.app.Settings;

/**
 * Created by hiepmt on 01/08/2017.
 */

public class TimeHelper {
    public static long getStartTimeOnThisDayTimestamp(long timestamp) {
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(timestamp);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        return calStart.getTimeInMillis();
    }

    public static long getCurrentMinuteBlockStartTime(long timestamp) {
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(timestamp);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        long millis = calStart.getTimeInMillis();
        return millis + ((timestamp-millis)/(Settings.INTERVAL_ACTIVITY*1000)) * Settings.INTERVAL_ACTIVITY*1000;
    }

    public static int MillisToSecond(long millis) {
        return (int)(millis/1000);
    }

    public static long SecondToMillis(int second) {
        return (long)second * 1000;
    }

    public static String getLiteralDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM dd");
        return formatter.format(date);

    }

    public static Date addDateWithDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);

        return cal.getTime();
    }

    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static long getStartTimeOnMondayThisWeekTimestamp(long timestamp) {
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(timestamp);
        calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        return calStart.getTimeInMillis();
    }


    // timeline order
    public static List<Long> getFirstDayOfPrevMonthsTimestampInclusive(long timestamp, int numberOfMonths) {
        List<Long> ans = new ArrayList<>();

        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(timestamp);
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        while (numberOfMonths > 0) {
            ans.add(0, calStart.getTimeInMillis());
            numberOfMonths--;

            //
            calStart.add(Calendar.MONTH, -1);
        }

        return ans;
    }

    // timeline order
    public static List<Long> getFirstDayOfPrevMonthsTimestamp(long timestamp, int numberOfMonths) {
        List<Long> ans = new ArrayList<>();

        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(timestamp);
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        while (numberOfMonths > 0) {
            calStart.add(Calendar.MONTH, -1);
            ans.add(0, calStart.getTimeInMillis());
            numberOfMonths--;
        }

        return ans;
    }

    public static List<Long> getFirstDayOfNextMonthsTimestamp(long timestamp, int numberOfMonths) {
        List<Long> ans = new ArrayList<>();

        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(timestamp);
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        while (numberOfMonths > 0) {
            calStart.add(Calendar.MONTH, 1);

            ans.add(calStart.getTimeInMillis());
            numberOfMonths--;
        }

        return ans;
    }

    public static Date addMonths(Date date, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);

        return cal.getTime();
    }
}
