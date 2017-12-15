package za.healthtracking.database.entities;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import za.healthtracking.models.PacePerKm;

/**
 * Created by hiepmt on 01/08/2017.
 */

@DatabaseTable(tableName = "runningActivityLog")
public class RunningActivityLog {
    public static final String ID_FIELD_NAME = "id";
    public static final String DURATIONINMILLIS_FIELD_NAME = "durationInMillis";
    public static final String DISTANCEINMETERS_FIELD_NAME = "distanceInMeters";
    public static final String CALORIES_FIELD_NAME = "calories";
    public static final String AVGPACE_FIELD_NAME = "avgPace";
    public static final String WAYPOINTS_FIELD_NAME = "wayPoints";
    public static final String PACEPERKM_FIELD_NAME = "pacePerKM";
    public static final String STARTTIME_FIELD_NAME = "startTime";
    public static final String ENDTIME_FIELD_NAME = "endTime";

    @DatabaseField(columnName = ID_FIELD_NAME, generatedId = true)
    public int id;
    @DatabaseField(columnName = DURATIONINMILLIS_FIELD_NAME)
    public long durationInMillis;
    @DatabaseField(columnName = DISTANCEINMETERS_FIELD_NAME)
    public float distanceInMeters;
    @DatabaseField(columnName = CALORIES_FIELD_NAME)
    public float calories;
    @DatabaseField(columnName = AVGPACE_FIELD_NAME)
    public float avgPace;
    @DatabaseField(columnName = WAYPOINTS_FIELD_NAME)
    public String wayPoints;
    @DatabaseField(columnName = PACEPERKM_FIELD_NAME)
    public String pacePerKM;
    @DatabaseField(columnName = STARTTIME_FIELD_NAME, indexName = "running_startTime_idx")
    public int startTime;
    @DatabaseField(columnName = ENDTIME_FIELD_NAME)
    public int endTime;

    public static String encodePacePerKM(List<PacePerKm> pacePerKmList) {
        try {
            JSONArray jArray=new JSONArray();
            for (PacePerKm pacePerKm : pacePerKmList) {
                JSONObject jObject=new JSONObject();
                jObject.put(DISTANCEINMETERS_FIELD_NAME, pacePerKm.distanceInMeters);
                jObject.put(DURATIONINMILLIS_FIELD_NAME, pacePerKm.durationInMillis);
                jArray.put(jObject);
            }
            return jArray.toString();
        } catch(JSONException ex) {

        }

        return null;
    }


    public static List<PacePerKm> decodePacePerKM(String data) {
        List<PacePerKm> pacePerKmList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                pacePerKmList.add(new PacePerKm((float)jsonObject.getDouble(DISTANCEINMETERS_FIELD_NAME), jsonObject.getLong(DURATIONINMILLIS_FIELD_NAME)));
            }
            return pacePerKmList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void test() {
        List<PacePerKm> pacePerKmList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pacePerKmList.add(new PacePerKm(1000.5f, 50000));
        }

        String encode = RunningActivityLog.encodePacePerKM(pacePerKmList);
        Log.d("ZHealth02", encode);
        List<PacePerKm> pacePerKmList1 = RunningActivityLog.decodePacePerKM(encode);
        for (PacePerKm pacePerKm : pacePerKmList1) {
            Log.d("ZHealth02", "distance: " + pacePerKm.distanceInMeters + "; duration: " + pacePerKm.durationInMillis);
        }
    }
}
