package com.dgphoenix.casino.common.util.system;

/**
 * Created by quant on 23.12.15.
 */
public class MetricStat {
    long statTime;
    long averageValue;
    long minValueTime;
    long minValue;
    long maxValueTime;
    long maxValue;

    public MetricStat() {

    }

    public long getStatTime() {
        return statTime;
    }

    public void setStatTime(long startTime) {
        this.statTime = startTime;
    }

    public long getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(long averageValue) {
        this.averageValue = averageValue;
    }

    public long getMinValueTime() {
        return minValueTime;
    }

    public void setMinValueTime(long minValueTime) {
        this.minValueTime = minValueTime;
    }

    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public long getMaxValueTime() {
        return maxValueTime;
    }

    public void setMaxValueTime(long maxValueTime) {
        this.maxValueTime = maxValueTime;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }
}
