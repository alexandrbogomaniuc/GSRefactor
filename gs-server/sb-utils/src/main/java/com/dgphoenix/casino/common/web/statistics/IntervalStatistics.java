/**
 * Author: Andrey Nazarov
 * Date: 22.03.2006
 */
package com.abs.casino.common.web.statistics;

import com.abs.casino.common.util.string.StringUtils;
import com.google.common.util.concurrent.AtomicDouble;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IntervalStatistics implements Serializable {
    private static final long serialVersionUID = -2422534193196105462L;
    private final String name;
    private AtomicLong sum = new AtomicLong();
    private AtomicLong minValue= new AtomicLong(Long.MAX_VALUE);
    private AtomicDouble avgValue = new AtomicDouble(0.0);
    private AtomicLong maxValue = new AtomicLong(Long.MIN_VALUE);
    private String maxValueInfo;
    private AtomicLong maxValueTime= new AtomicLong();
    private AtomicInteger experimentCount = new AtomicInteger();

    public IntervalStatistics(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getMinValue() {
        return minValue.get();
    }

    public long getAvgValue() {
        return Math.round(avgValue.get());
    }

    public long getMaxValue() {
        return maxValue.get();
    }

    public String getMaxValueInfo() {
        return maxValueInfo;
    }

    public long getMaxValueTime() {
        return maxValueTime.get();
    }

    public int getExperimentCount() {
        return experimentCount.get();
    }

    public long getSum() {
        return sum.get();
    }

    public void update(long value, String additionalInfo) {
        if (value < 0) {
            //throw new RuntimeException("Value cannot be negative");
            return;
        }

        if (sum.get() != -1) {
            if (sum.get() < Long.MAX_VALUE - value) {
                sum.getAndAdd(value);
            } else {
                sum.set(-1);
            }
        }
        while(true) {
            long currentMin = minValue.get();
            if(currentMin < value) {
                break;
            } else {
                if(minValue.compareAndSet(currentMin, value)) {
                    break;
                }
            }
        }
        double currentAvg = (value - avgValue.get()) / (experimentCount.get() + 1);
        avgValue.addAndGet(currentAvg);

        while(true) {
            long currentMax = maxValue.get();
            if(currentMax > value) {
                break;
            } else {
                if(maxValue.compareAndSet(currentMax, value)) {
                    maxValueTime.set(System.currentTimeMillis());
                    maxValueInfo = additionalInfo;
                    break;
                }
            }
        }

        experimentCount.getAndIncrement();
    }

    public String toString() {
        return "count=" + StringUtils.formatNumber(getExperimentCount(), 8) +
                "\tavg=" + formatNumber(getAvgValue()) +
                "\tmin=" + formatNumber(getMinValue()) +
                "\tmax=" + formatNumber(getMaxValue()) +
                "\t [" + (StringUtils.isTrimmedEmpty(maxValueInfo) ? "\t" : maxValueInfo) + "]\t\t" +
                (new Date(maxValueTime.get())) + "\t\t" + getName();
    }

    private String formatNumber(long value) {
        return StringUtils.formatNumber(value, 4);
    }
}
