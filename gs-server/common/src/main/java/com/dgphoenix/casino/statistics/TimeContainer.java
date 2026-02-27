package com.abs.casino.statistics;

import java.text.DecimalFormat;

/**
 * User: dartilla
 * Date: Sep 8, 2010
 * Time: 4:58:59 PM
 */
public class TimeContainer {
    private final DecimalFormat twoDForm = new DecimalFormat("#.##");

    private String name;
    private long count = 0;
    private long time = 0;
    private long max = 0;
    private long min = Long.MAX_VALUE;

    public TimeContainer(String name) {
        this.name = name;
    }

    public synchronized void addData(String timeData) {
        String[] times = timeData.split("\\|");
        long newMin = Long.parseLong(times[0]);
        min = newMin < min ? newMin : min;
        long newMax = Long.parseLong(times[1]);
        max = newMax > max ? newMax : max;
        time += Long.parseLong(times[2]);
        count += Long.parseLong(times[3]);
    }

    private synchronized double getAverageTime() {
        if (count == 0) return 0;
        return (double) time / (double) count;
    }

    private synchronized long getMin() {
        return Long.MAX_VALUE == min ? 0 : min;
    }

    public String getName() {
        return name;
    }

    @Override
    public synchronized String toString() {
        return "avr:" + twoDForm.format(getAverageTime()) +
                ", max:" + max +
                ", min:" + getMin() +
                ", count:" + count;
    }
}
