package com.dgphoenix.casino.common.cache.data.session;


import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

import java.util.Date;

public class SessionStatistics implements IDistributedCacheEntry {
    private long minCount;
    private long maxCount;
    private long currentCount;
    private long date;

    public SessionStatistics(long minCount, long maxCount, long currentCount, long currentDate) {
        super();
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.currentCount = currentCount;
        this.date = currentDate;
    }

    public long getMinCount() {
        return minCount;
    }

    public long getMaxCount() {
        return maxCount;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
        if (minCount > currentCount) {
            minCount = currentCount;
        }
        if (maxCount < currentCount) {
            maxCount = currentCount;
        }
    }

    public void rollNextPeriod(int currentCount, long date) {
        this.currentCount = currentCount;
        this.date = date;
        minCount = currentCount;
        maxCount = currentCount;
    }

    @Override
    public String toString() {
        return "SessionStatistics [minCount=" + minCount + ", maxCount="
                + maxCount + ", currentCount=" + currentCount
                + ", date=" + new Date(date) + "]";
    }


}
