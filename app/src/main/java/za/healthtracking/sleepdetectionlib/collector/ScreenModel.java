package za.healthtracking.sleepdetectionlib.collector;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class ScreenModel {
    private int screenState = 0;
    private long time = 0;
    private int useKeyGuard = 0;
    private int userPresent = 0;

    public ScreenModel(long j, int i, int i2, int i3) {
        this.time = j;
        this.screenState = i;
        this.userPresent = i2;
        this.useKeyGuard = i3;
    }

    public final int getScreenState() {
        return this.screenState;
    }

    public final long getTime() {
        return this.time;
    }

    public final String getTimeText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.time));
    }

    public final int getUseKeyGuard() {
        return this.useKeyGuard;
    }

    public final int getUserPresent() {
        return this.userPresent;
    }

    public final void setTime(long j) {
        this.time = j;
    }

    public final void setUseKeyGuard(int i) {
        this.useKeyGuard = i;
    }
}
