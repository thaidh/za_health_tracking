package za.healthtracking.sleepdetectionlib.info;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import za.healthtracking.sleepdetectionlib.request.TimeModel;
import za.healthtracking.sleepdetectionlib.util.Time;

public final class UserData implements Serializable {
    private ArrayList<TimeModel> mHoliday;
    private long requestHolidayTime;
    private boolean responseHoliday;

    public final String toString() {
        StringBuffer returnValue = new StringBuffer();
        if (this.mHoliday != null) {
            synchronized (this.mHoliday) {
                Iterator it = this.mHoliday.iterator();
                while (it.hasNext()) {
                    TimeModel time = (TimeModel) it.next();
                    returnValue.append("[");
                    returnValue.append(Time.changeTimeText(time.getStartTime(), "yyyyMMdd"));
                    returnValue.append("~");
                    returnValue.append(Time.changeTimeText(time.getEndTime(), "yyyyMMdd"));
                    returnValue.append("]");
                }
            }
        }
        return returnValue.toString();
    }

    public final ArrayList<TimeModel> getHoliday() {
        if (this.mHoliday == null) {
            this.mHoliday = new ArrayList();
        }
        return this.mHoliday;
    }

    public final void setBlobUserData(byte[] blobData) {
        Exception e;
        Throwable th;
        UserData loadClass = null;
        if (blobData != null) {
            ByteArrayInputStream bis = null;
            ObjectInputStream ois = null;
            try {
                ByteArrayInputStream bis2 = new ByteArrayInputStream(blobData);
                try {
                    ObjectInputStream ois2 = new ObjectInputStream(bis2);
                    try {
                        loadClass = (UserData) ois2.readObject();
                        try {
                            ois2.close();
                            bis2.close();
                        } catch (IOException ie) {
                            ie.printStackTrace();
                        }
                    } catch (Exception e2) {
                        e = e2;
                        ois = ois2;
                        bis = bis2;
                        try {
                            e.printStackTrace();
                            try {
                                ois.close();
                                bis.close();
                            } catch (IOException ie2) {
                                ie2.printStackTrace();
                            }
                            if (loadClass == null) {
                                this.responseHoliday = loadClass.responseHoliday;
                                this.requestHolidayTime = loadClass.requestHolidayTime;
                                this.mHoliday = loadClass.mHoliday;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                ois.close();
                                bis.close();
                            } catch (IOException ie22) {
                                ie22.printStackTrace();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        ois = ois2;
                        bis = bis2;
                        ois.close();
                        bis.close();
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    bis = bis2;
                    e.printStackTrace();
                    ois.close();
                    bis.close();
                    if (loadClass == null) {
                        this.responseHoliday = loadClass.responseHoliday;
                        this.requestHolidayTime = loadClass.requestHolidayTime;
                        this.mHoliday = loadClass.mHoliday;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    bis = bis2;
                    ois.close();
                    bis.close();
                }
            } catch (Exception e4) {
                e = e4;
                e.printStackTrace();
                if (loadClass == null) {
                    this.responseHoliday = loadClass.responseHoliday;
                    this.requestHolidayTime = loadClass.requestHolidayTime;
                    this.mHoliday = loadClass.mHoliday;
                }
            }
        }
        if (loadClass == null) {
            this.responseHoliday = loadClass.responseHoliday;
            this.requestHolidayTime = loadClass.requestHolidayTime;
            this.mHoliday = loadClass.mHoliday;
        }
    }
}
