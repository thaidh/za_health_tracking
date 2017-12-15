package za.healthtracking.utils;

import android.location.Location;
import android.util.Log;
import android.util.TypedValue;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import vn.zing.pedometer.R;
import za.healthtracking.app.MyApplication;
import za.healthtracking.models.PacePerKm;

/**
 * Created by hiepmt on 10/07/2017.
 */

public class Helper {
    public static String formatDuration(int second) {
        return String.format("%02d:%02d:%02d", second/60/60, second/60%60, second % 60);
    }

    public static String formatDistance(float distanceInMeters) {
        return String.format("%.2f", distanceInMeters/1000);
    }

    public static String formatDistanceWithUnit(float distanceInMeters) {
        return String.format("%.2f km", distanceInMeters/1000);
    }

    public static String formatSpeed(float speed) {
        return String.format("%.1f", speed);
    }

    public static String formatPace(float pace) {
        int iPace = (int)pace;
        return String.format("%02d:%02d", iPace/60, iPace%60);
    }


    public static String formatPacePerKmPace(PacePerKm pace) {
        return Helper.formatPacePerKmDistance(pace.distanceInMeters /1000f) + " | " + Helper.formatPace(pace.durationInMillis /1000);
    }

    // Return distanceInMeters in meters
    public static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        double Radius = 6371000.0;// meter
        double dLat = Math.toRadians(lat1
                - lat2);
        double dLon = Math.toRadians(lng1
                - lng2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Radius * c;
    }

    // Return distanceInMeters in meters
    public static double calcDistance(LatLng location1, LatLng location2) {
        return calcDistance(location1.latitude, location1.longitude, location2.latitude, location2.longitude);
    }

    public static double calcDistance(Location location1, Location location2) {
        return calcDistance(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude());
    }

    public static float calcPace(long durationInMillis, double distanceInMeters) {
        if (distanceInMeters == 0)
            return 0;

        return (float)(durationInMillis/distanceInMeters);
    }

    public static String dumbData(List<PacePerKm> pacePerKmList) {
        String ans = "";
        for (PacePerKm pace : pacePerKmList) {
            ans += Helper.formatPacePerKmPace(pace);
            ans += "\r\n";
        }

        return ans;
    }

    public static String formatPacePerKmDistance(float distanceInKm) {
        return String.format("%.2f", distanceInKm);
    }

    public static String formatPacePerKmDistanceClean(float distanceInKm) {
        if (Math.round(distanceInKm) == distanceInKm) {
            return String.format("%d", Math.round(distanceInKm));
        }

        return String.format("%.2f", distanceInKm);
    }

    public static String formatCalories(float calories) {
        return String.format("%s", (int)Math.round(calories));
    }

    public static String getStaticMapUrl(List<LatLng> latLngList, int width, int height) {
        String googleMapKey = MyApplication.getAppContext().getResources().getString(R.string.google_maps_key);
        String pathData = "";
        for (LatLng latLng : latLngList) {
            pathData += "|" + latLng.latitude + ",%20" + latLng.longitude;
        }

        String size = String.format("%dx%d", width, height);
        String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?zoom=16&size=" + size + "&path=color:0xff0000ff|weight:2" + pathData + "&key=" + googleMapKey;

        Log.d("ZHealth", mapUrl);

        return mapUrl;
    }

    public static String formatStep(int steps) {
        return String.format("%s", steps);
    }

    public static String formatHourMin(long timestamp) {
        Calendar calStart = new GregorianCalendar();
        calStart.setTimeInMillis(timestamp);

        return String.format("%02d:%02d", calStart.get(Calendar.HOUR_OF_DAY), calStart.get(Calendar.MINUTE));
    }

    public static int dpToPixel(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MyApplication.getAppContext().getResources().getDisplayMetrics());
    }

    public static String formatDateTime(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, EEE, dd MMM");
        return formatter.format(timestamp);
    }
}
