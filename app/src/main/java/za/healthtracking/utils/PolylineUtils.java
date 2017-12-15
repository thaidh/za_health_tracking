package za.healthtracking.utils;

import android.content.res.Resources;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by hiepmt on 10/08/2017.
 */

public class PolylineUtils {
    public static PolylineOptions createPolyline(Resources resources, int color, float width, List<LatLng> list) {
        PolylineOptions polyline = new PolylineOptions().color(resources.getColor(color)).width(width);
        if (!list.isEmpty()) {
            polyline.addAll(list);
        }
        return polyline;
    }
}
