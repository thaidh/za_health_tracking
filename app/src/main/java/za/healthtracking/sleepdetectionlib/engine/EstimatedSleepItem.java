package za.healthtracking.sleepdetectionlib.engine;

public final class EstimatedSleepItem {
    public static int TYPE_ESTIMATION_SLEEP_TIME = 2;
    public static int TYPE_NO_VALUE = 0;
    public static int TYPE_RECOMMEND_SLEEP_TIME = 1;
    private long mBedTime;
    private int mBedTimeType;
    private long mSleepDate;
    private long mWakeUpTime;
    private int mWakeUpTimeType;

    public static class Builder {
        private long mBuilderBedTime = -1;
        private int mBuilderBedTimeType = EstimatedSleepItem.TYPE_NO_VALUE;
        private long mBuilderSleepDate = -1;
        private long mBuilderWakeUpTime = -1;
        private int mBuilderWakeUpTimeType = EstimatedSleepItem.TYPE_NO_VALUE;

        public Builder bedTime(long value) {
            this.mBuilderBedTime = value;
            return this;
        }

        public Builder wakeUpTime(long value) {
            this.mBuilderWakeUpTime = value;
            return this;
        }

        public Builder setBedTimeType(int bedTimeType) {
            this.mBuilderBedTimeType = bedTimeType;
            return this;
        }

        public Builder setWakeUpTimeType(int wakeUpTimeType) {
            this.mBuilderWakeUpTimeType = wakeUpTimeType;
            return this;
        }

        public Builder sleepDate(long value) {
            if (value <= -1) {
                throw new IllegalStateException();
            }
            this.mBuilderSleepDate = value;
            return this;
        }
    }

    public EstimatedSleepItem(Builder builder) {
        this.mBedTime = 0;
        this.mWakeUpTime = 0;
        this.mSleepDate = 0;
        this.mBedTimeType = TYPE_NO_VALUE;
        this.mWakeUpTimeType = TYPE_NO_VALUE;
        this.mSleepDate = builder.mBuilderSleepDate;
        this.mBedTime = builder.mBuilderBedTime;
        this.mWakeUpTime = builder.mBuilderWakeUpTime;
        this.mBedTimeType = builder.mBuilderBedTimeType;
        this.mWakeUpTimeType = builder.mBuilderWakeUpTimeType;
    }

    public final long getBedTime() {
        return this.mBedTime;
    }

    public final long getWakeUpTime() {
        return this.mWakeUpTime;
    }

    public final long getDate() {
        return this.mSleepDate;
    }

    public final boolean isBedTimeValid() {
        return this.mBedTime > -1;
    }

    public final boolean isWakeUpTimeValid() {
        return this.mWakeUpTime > -1;
    }

    public final int getBedTimeType() {
        return this.mBedTimeType;
    }

    public final int getWakeUpTimeType() {
        return this.mWakeUpTimeType;
    }
}
