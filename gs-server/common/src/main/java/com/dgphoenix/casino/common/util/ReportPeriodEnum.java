package com.abs.casino.common.util;

/**
 * User: flsh
 * Date: 05.07.13
 */

import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 7/7/11
 */
public enum ReportPeriodEnum {
    DAILY(TimeUnit.DAYS.toMillis(1)),
    WEEKLY(TimeUnit.DAYS.toMillis(7)),
    MONTHLY(TimeUnit.DAYS.toMillis(30));

    private final long periodInMillis;

    ReportPeriodEnum(long periodInMillis) {
        this.periodInMillis = periodInMillis;
    }

    public long getPeriodInMillis() {
        return periodInMillis;
    }
}