package za.healthtracking.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import za.healthtracking.app.Settings;
import za.healthtracking.utils.TimeHelper;

/**
 * Created by hiepmt on 01/08/2017.
 */

@DatabaseTable(tableName = "minutelyActivityLog")
public class MinutelyActivityLog {
    public static final String ID_FIELD_NAME = "id";
    public static final String STEPS_FIELD_NAME = "steps";
    public static final String CALORIES_FIELD_NAME = "calories";
    public static final String DISTANCEINMETERS_FIELD_NAME = "distanceInMeters";
    public static final String STARTTIME_FIELD_NAME = "startTime";
    public static final String ENDTIME_FIELD_NAME = "endTime";

    @DatabaseField(columnName = ID_FIELD_NAME, generatedId = true)
    public int id;
    @DatabaseField(columnName = STEPS_FIELD_NAME)
    public int steps;
    @DatabaseField(columnName = CALORIES_FIELD_NAME)
    public float calories;
    @DatabaseField(columnName = DISTANCEINMETERS_FIELD_NAME)
    public float distanceInMeters;
    @DatabaseField(columnName = STARTTIME_FIELD_NAME, indexName = "minutely_startTime_idx")
    public int startTime;
    @DatabaseField(columnName = ENDTIME_FIELD_NAME)
    public int endTime;

    public static MinutelyActivityLog newInstance(long timestamp) {
        MinutelyActivityLog instance = new MinutelyActivityLog();
        instance.startTime = TimeHelper.MillisToSecond(TimeHelper.getCurrentMinuteBlockStartTime(timestamp));
        instance.endTime = instance.startTime + Settings.INTERVAL_ACTIVITY - 1;
        instance.calories = 0;
        instance.steps = 0;
        instance.calories = 0;

        return instance;
    }
}
