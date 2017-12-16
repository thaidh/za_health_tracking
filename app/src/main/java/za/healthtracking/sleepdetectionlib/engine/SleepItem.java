package za.healthtracking.sleepdetectionlib.engine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;

public class SleepItem implements Parcelable, Serializable, Comparable<SleepItem> {
    public static final Creator<SleepItem> CREATOR = new Creator<SleepItem>() {
        @Override
        public SleepItem createFromParcel(Parcel source) {
            return new SleepItem(source);
        }

        @Override
        public SleepItem[] newArray(int size) {
            return new SleepItem[0];
        }
    };
    private long mBedTime;
    private float mEfficiency;
    private boolean mHasSleepData = false;
    private long mInternalBedTime;
    private long mInternalOriginalBedTime = 0;
    private long mInternalOriginalWakeupTime = 0;
    private long mInternalWakeupTime;
    private long mOriginalBedTime = 0;
    private float mOriginalEfficiency = 0.0f;
    private long mOriginalWakeupTime = 0;
    private int mQuality = 0;
    private SleepCondition mSleepCondition = SleepCondition.SLEEP_CONDITION_NONE;
    private SleepType mSleepType;
    private String mSource;
    private long mTimeOffset = 0;
    private String mUuid;
    private long mWakeupTime;

    public enum SleepCondition {
        SLEEP_CONDITION_NONE(0),
        SLEEP_CONDITION_STAR_1ST(50001),
        SLEEP_CONDITION_STAR_2ND(50002),
        SLEEP_CONDITION_STAR_3RD(50003),
        SLEEP_CONDITION_STAR_4TH(50004),
        SLEEP_CONDITION_STAR_5TH(50005);
        
        private final int mTypeValue;

        private SleepCondition(int value) {
            this.mTypeValue = value;
        }

        public final int toInt() {
            return this.mTypeValue;
        }

        public static SleepCondition fromInt(int value) {
            for (SleepCondition type : values()) {
                if (type.mTypeValue == value) {
                    return type;
                }
            }
            return SLEEP_CONDITION_NONE;
        }
    }

    public enum SleepType {
        SLEEP_TYPE_MANUAL(0),
        SLEEP_TYPE_BINNING(1),
        SLEEP_TYPE_STAGE(2);
        
        private final int mTypeValue;

        private SleepType(int value) {
            this.mTypeValue = value;
        }

        public final int toInt() {
            return this.mTypeValue;
        }

        public static SleepType fromInt(int value) {
            for (SleepType type : values()) {
                if (type.mTypeValue == value) {
                    return type;
                }
            }
            return SLEEP_TYPE_MANUAL;
        }
    }

    @Override
    public int compareTo(@NonNull SleepItem obj) {
        long j = ((SleepItem) obj).mBedTime;
        if (j > this.mBedTime) {
            return -1;
        }
        if (j < this.mBedTime) {
            return 1;
        }
        return 0;
    }

    public SleepItem(long internalBedTime, long internalWakeupTime, long timeOffset, int quality, float efficiency, String uuid, SleepType sleepType, String dataSource, SleepCondition sleepCondition) {
        this.mTimeOffset = timeOffset;
        this.mInternalBedTime = internalBedTime;
        this.mInternalWakeupTime = internalWakeupTime;
        if (this.mInternalBedTime > this.mInternalWakeupTime) {
            long swapper = this.mInternalBedTime;
            this.mInternalBedTime = this.mInternalWakeupTime;
            this.mInternalWakeupTime = swapper;
        }
        this.mBedTime = convertToLocalTime(dataSource, internalBedTime, this.mTimeOffset);
        this.mWakeupTime = convertToLocalTime(dataSource, internalWakeupTime, this.mTimeOffset);
        this.mQuality = 0;
        this.mEfficiency = efficiency;
        this.mUuid = uuid;
        this.mSleepType = sleepType;
        this.mHasSleepData = true;
        this.mSource = dataSource;
        this.mSleepCondition = sleepCondition;
    }

    public SleepItem(long bedTime, long wakeupTime, int quality, float efficiency, String uuid, SleepType sleepType, String dataSource, SleepCondition sleepCondition) {
        this.mBedTime = bedTime;
        this.mWakeupTime = wakeupTime;
        if (bedTime > wakeupTime) {
            long swapper = this.mBedTime;
            this.mBedTime = this.mWakeupTime;
            this.mWakeupTime = swapper;
        }
        this.mQuality = 0;
        this.mEfficiency = 0.0f;
        this.mUuid = uuid;
        this.mSleepType = sleepType;
        if (this.mSleepType == SleepType.SLEEP_TYPE_MANUAL) {
            this.mHasSleepData = false;
        } else {
            this.mHasSleepData = true;
        }
        this.mSource = dataSource;
        this.mSleepCondition = sleepCondition;
    }

    public SleepItem(Cursor cursor) {
        if (cursor != null) {
            long swapper;
            this.mTimeOffset = cursor.getLong(cursor.getColumnIndex("time_offset"));
            this.mInternalBedTime = cursor.getLong(cursor.getColumnIndex("start_time"));
            this.mInternalWakeupTime = cursor.getLong(cursor.getColumnIndex("end_time"));
            if (this.mInternalBedTime > this.mInternalWakeupTime) {
                swapper = this.mInternalBedTime;
                this.mInternalBedTime = this.mInternalWakeupTime;
                this.mInternalWakeupTime = swapper;
            }
            this.mQuality = cursor.getInt(cursor.getColumnIndex("quality"));
            this.mEfficiency = cursor.getFloat(cursor.getColumnIndex("efficiency"));
            this.mOriginalEfficiency = cursor.getFloat(cursor.getColumnIndex("original_efficiency"));
            this.mUuid = cursor.getString(cursor.getColumnIndex("datauuid"));
            this.mSleepType = SleepType.fromInt(cursor.getInt(cursor.getColumnIndex("has_sleep_data")));
            if (this.mSleepType == SleepType.SLEEP_TYPE_MANUAL) {
                this.mHasSleepData = false;
            } else {
                this.mHasSleepData = true;
            }
            this.mSource = cursor.getString(cursor.getColumnIndex("pkg_name")) + "##" + cursor.getString(cursor.getColumnIndex("deviceuuid"));
            this.mBedTime = convertToLocalTime(this.mSource, cursor.getLong(cursor.getColumnIndex("start_time")), this.mTimeOffset);
            this.mWakeupTime = convertToLocalTime(this.mSource, cursor.getLong(cursor.getColumnIndex("end_time")), this.mTimeOffset);
            this.mInternalOriginalBedTime = cursor.getLong(cursor.getColumnIndex("original_bed_time"));
            this.mInternalOriginalWakeupTime = cursor.getLong(cursor.getColumnIndex("original_wake_up_time"));
            if (this.mInternalOriginalBedTime > this.mInternalOriginalWakeupTime) {
                swapper = this.mInternalOriginalBedTime;
                this.mInternalOriginalBedTime = this.mInternalOriginalWakeupTime;
                this.mInternalOriginalWakeupTime = swapper;
            }
            this.mOriginalBedTime = this.mInternalOriginalBedTime;
            if (this.mOriginalBedTime != 0) {
                this.mOriginalBedTime = convertToLocalTime(this.mSource, this.mOriginalBedTime, this.mTimeOffset);
            }
            this.mOriginalWakeupTime = this.mInternalOriginalWakeupTime;
            if (this.mOriginalWakeupTime != 0) {
                this.mOriginalWakeupTime = convertToLocalTime(this.mSource, this.mOriginalWakeupTime, this.mTimeOffset);
            }
            this.mSleepCondition = SleepCondition.fromInt(this.mQuality);
        }
    }

    public SleepItem(Parcel in) {
        boolean z = true;
        this.mEfficiency = in.readFloat();
        this.mWakeupTime = in.readLong();
        this.mBedTime = in.readLong();
        this.mUuid = in.readString();
        this.mQuality = in.readInt();
        this.mOriginalWakeupTime = in.readLong();
        this.mOriginalBedTime = in.readLong();
        this.mOriginalEfficiency = in.readFloat();
        if (in.readInt() != 1) {
            z = false;
        }
        this.mHasSleepData = z;
        this.mSource = in.readString();
        this.mTimeOffset = in.readLong();
        this.mInternalWakeupTime = in.readLong();
        this.mInternalBedTime = in.readLong();
        this.mInternalOriginalWakeupTime = in.readLong();
        this.mInternalOriginalBedTime = in.readLong();
        this.mSleepType = SleepType.fromInt(in.readInt());
        this.mSleepCondition = SleepCondition.fromInt(in.readInt());
    }

    public final String getUuid() {
        return this.mUuid;
    }

    public final float getEfficiency() {
        return this.mEfficiency;
    }

    public final long getWakeUpTime() {
        return this.mWakeupTime;
    }

    public final long getBedTime() {
        return this.mBedTime;
    }

    public final long getOriginalBedTime() {
        return this.mOriginalBedTime;
    }

    public final long getOriginalWakeUpTime() {
        return this.mOriginalWakeupTime;
    }

    public final float getOriginalEfficiency() {
        return this.mOriginalEfficiency;
    }

    public final boolean getHasSleepData() {
        return this.mHasSleepData;
    }

    public final long getSleepDuration() {
        return getTimeWithZeroSecondsInternal(this.mInternalWakeupTime - this.mInternalBedTime);
    }

    public final long getOriginalSleepDuration() {
        return getTimeWithZeroSecondsInternal(this.mInternalOriginalWakeupTime - this.mInternalOriginalBedTime);
    }

    public final String getSource() {
        return this.mSource;
    }

    public final long getTimeOffset() {
        return this.mTimeOffset;
    }

    public final long getInternalWakeupTime() {
        return this.mInternalWakeupTime;
    }

    public final long getInternalBedTime() {
        return this.mInternalBedTime;
    }

    public final SleepType getSleepType() {
        return this.mSleepType;
    }

    public final SleepCondition getSleepCondition() {
        return this.mSleepCondition;
    }

    public final void updateBedTime(long updatedTime) {
        if (this.mOriginalBedTime == 0) {
            this.mOriginalBedTime = this.mBedTime;
        }
        this.mBedTime = updatedTime;
    }

    public final void updateWakeUpTime(long updatedTime) {
        if (this.mOriginalWakeupTime == 0) {
            this.mOriginalWakeupTime = this.mWakeupTime;
        }
        this.mWakeupTime = updatedTime;
    }

    public final void updateEfficiency(float updatedEfficiency) {
        if (this.mOriginalEfficiency == 0.0f) {
            this.mOriginalEfficiency = this.mEfficiency;
        }
        this.mEfficiency = updatedEfficiency;
    }

    public final void updateSleepCondition(SleepCondition condition) {
        this.mSleepCondition = condition;
    }

    public final void setSleepStageType(SleepType type) {
        this.mSleepType = type;
        if (this.mSleepType == SleepType.SLEEP_TYPE_MANUAL) {
            this.mHasSleepData = false;
        } else {
            this.mHasSleepData = true;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mEfficiency);
        dest.writeLong(this.mWakeupTime);
        dest.writeLong(this.mBedTime);
        dest.writeString(this.mUuid);
        dest.writeInt(this.mQuality);
        dest.writeLong(this.mOriginalWakeupTime);
        dest.writeLong(this.mOriginalBedTime);
        dest.writeFloat(this.mOriginalEfficiency);
        dest.writeInt(this.mHasSleepData ? 1 : 0);
        dest.writeString(this.mSource);
        dest.writeLong(this.mTimeOffset);
        dest.writeLong(this.mInternalWakeupTime);
        dest.writeLong(this.mInternalBedTime);
        dest.writeLong(this.mInternalOriginalWakeupTime);
        dest.writeLong(this.mInternalOriginalBedTime);
        dest.writeInt(this.mSleepType.toInt());
        dest.writeInt(this.mSleepCondition.toInt());
    }

    private static long getTimeWithZeroSecondsInternal(long time) {
        return (time / 60000) * 60000;
    }

    private static long convertToLocalTime(String source, long remoteTimestamp, long remoteTimeOffset) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(remoteTimestamp + remoteTimeOffset);
        TimeZone tz = TimeZone.getDefault();
        int offsetFromGmt = tz.getOffset(cal.getTimeInMillis());
        Calendar currentLocaleCal = Calendar.getInstance(tz);
        currentLocaleCal.setTimeInMillis(remoteTimestamp + remoteTimeOffset);
        int getBeforeOffset = currentLocaleCal.get(16);
        currentLocaleCal.add(14, 0 - offsetFromGmt);
        int getAfterOffset = currentLocaleCal.get(16);
        if (getBeforeOffset != getAfterOffset) {
            currentLocaleCal.add(14, 0 - (getAfterOffset - getBeforeOffset));
        }
//        if ((!SleepDataManager.needAdjustTimeForDateSavingTime(ContextHolder.getContext(), source)) && TimeZone.getDefault().inDaylightTime(currentLocaleCal.getTime())) {
//            currentLocaleCal.add(11, tz.getDSTSavings() / 3600000);
//        }
        return currentLocaleCal.getTimeInMillis();
    }

    public boolean equals(Object obj) {
        if (obj instanceof SleepItem) {
            return this.mUuid.equals(((SleepItem) obj).mUuid);
        }
        return false;
    }
}
