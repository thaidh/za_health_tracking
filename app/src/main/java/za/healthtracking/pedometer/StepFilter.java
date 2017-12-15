package za.healthtracking.pedometer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hiepmt on 07/07/2017.
 */

public class StepFilter {
    private double minThresholdSensitive;
    private double maxThresholdSensitive;
    private double deltaSensitive;
    private double maxPositiveValue;
    private double minNegativeValue;
    private boolean isMinThreshold;
    private PedometerStatus status;
    private Date date;
    private List<Double> positiveValueList;
    private List<Double> negativeValueList;
    private float sensitive;
    private static final float GRAVITY_EARTH = 9.806650161743164f;

    private enum PedometerStatus {
        PedometerStatusNormal,
        PedometerStatusPositive,
        PedometerStatusNegative
    }

    public static float MOST_SENSITIVE = 0.04f;
    public static float MORE_SENSITIVE = 0.2f;
    public static float STANDARD = 0.35f;
    public static float LESS_SENSITIVE = 0.75f;
    public static float LEAST_SENSITIVE = 1.1f;


    public StepFilter(float sensitive) {
        this.sensitive = sensitive;
        init();
    }

    private void init() {
        this.minThresholdSensitive = 0.7d - ((double) (this.sensitive / 4.0f));
        this.maxThresholdSensitive = 1.3d + ((double) (this.sensitive / 4.0f));
        this.deltaSensitive = (double) (this.sensitive + 0.05f);
        this.status = PedometerStatus.PedometerStatusNormal;
        this.isMinThreshold = false;
        this.date = new Date();
        this.positiveValueList = new ArrayList();
        this.negativeValueList = new ArrayList();
    }

    public boolean isNewStep(float accX, float accY, float accZ) {
        double magnitude = Math.sqrt(((accX * accX) + (accY * accY)) + (accZ * accZ)) / GRAVITY_EARTH;
        boolean isNewStep = false;
        if (this.status == PedometerStatus.PedometerStatusNormal) {
            if (magnitude < this.minThresholdSensitive) {
                this.status = PedometerStatus.PedometerStatusPositive;
                this.isMinThreshold = true;
            } else if (magnitude > this.maxThresholdSensitive) {
                this.status = PedometerStatus.PedometerStatusNegative;
                if (this.isMinThreshold) {
                    this.isMinThreshold = false;
                    if (new Date().getTime() - this.date.getTime() > 300) {
                        this.date = new Date();
                        isNewStep = true;
                    } else {
                        isNewStep = false;
                    }
                }
            }
        } else if (this.status == PedometerStatus.PedometerStatusPositive) {
            if (magnitude >= this.minThresholdSensitive) {
                if (magnitude > this.maxThresholdSensitive) {
                    this.status = PedometerStatus.PedometerStatusNegative;
                    if (this.isMinThreshold) {
                        this.isMinThreshold = false;
                        if (new Date().getTime() - this.date.getTime() > 300) {
                            this.date = new Date();
                            isNewStep = true;
                        }
                    }
                } else {
                    this.status = PedometerStatus.PedometerStatusNormal;
                }
                if (this.maxPositiveValue != 5.0d) {
                    this.positiveValueList.add(this.maxPositiveValue);
                    if (this.positiveValueList.size() > 3) {
                        this.positiveValueList.remove(0);
                    }
                }
                if (this.positiveValueList.size() > 2) {
                    double doubleValue = ((this.positiveValueList.get(0).doubleValue() * 0.2d) + (this.positiveValueList.get(1).doubleValue() * 0.3d)) + (this.positiveValueList.get(2).doubleValue() * 0.5d);
                    if (this.minThresholdSensitive > 1.0d) {
                        this.minThresholdSensitive = (doubleValue * 0.65d) + (0.35d * (this.maxThresholdSensitive - 0.1d));
                    } else {
                        this.minThresholdSensitive = (doubleValue * 0.65d) + 0.315d;
                    }
                }
                this.maxPositiveValue = 5.0d;
            } else if (magnitude < this.maxPositiveValue) {
                this.maxPositiveValue = magnitude;
            }
        } else if (this.status == PedometerStatus.PedometerStatusNegative) {
            if (magnitude <= this.maxThresholdSensitive) {
                if (magnitude < this.minThresholdSensitive) {
                    this.status = PedometerStatus.PedometerStatusPositive;
                    this.isMinThreshold = true;
                }
                if (magnitude > this.minThresholdSensitive && magnitude < this.maxThresholdSensitive) {
                    this.status = PedometerStatus.PedometerStatusNormal;
                }
                if (this.minNegativeValue != 1.0d) {
                    this.negativeValueList.add(this.minNegativeValue);
                    if (this.negativeValueList.size() > 3) {
                        this.negativeValueList.remove(0);
                    }
                }
                if (this.negativeValueList.size() > 2) {
                    this.maxThresholdSensitive = (((((this.negativeValueList.get(0)).doubleValue() * 0.2d) + (( this.negativeValueList.get(1)).doubleValue() * 0.3d)) + (( this.negativeValueList.get(2)).doubleValue() * 0.5d)) * 0.65d) + 0.385d;
                }
                this.minNegativeValue = 1.0d;
            } else if (magnitude > this.minNegativeValue) {
                this.minNegativeValue = magnitude;
            }
        }
        if (this.status == PedometerStatus.PedometerStatusPositive) {
            this.minThresholdSensitive -= 0.013333333333333334d;
        } else if (this.minThresholdSensitive < this.maxThresholdSensitive - ((double) this.sensitive)) {
            this.minThresholdSensitive += 0.013333333333333334d;
            if (this.minThresholdSensitive > this.maxThresholdSensitive - ((double) this.sensitive)) {
                this.minThresholdSensitive = this.maxThresholdSensitive - ((double) this.sensitive);
            }
        }
        if (this.status == PedometerStatus.PedometerStatusNegative) {
            this.maxThresholdSensitive += 0.013333333333333334d;
        } else if (this.maxThresholdSensitive > 1.0d + this.deltaSensitive) {
            this.maxThresholdSensitive -= 0.013333333333333334d;
            if (this.maxThresholdSensitive < 1.0d + this.deltaSensitive) {
                this.maxThresholdSensitive = 1.0d + this.deltaSensitive;
            }
            if (this.minThresholdSensitive > this.maxThresholdSensitive - ((double) this.sensitive)) {
                this.minThresholdSensitive = this.maxThresholdSensitive - ((double) this.sensitive);
            }
        }
        return isNewStep;
    }

}
