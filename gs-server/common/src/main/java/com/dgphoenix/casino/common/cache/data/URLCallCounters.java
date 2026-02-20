package com.dgphoenix.casino.common.cache.data;

import com.dgphoenix.casino.common.util.DateUtils;

public class URLCallCounters {
    private String date;
    private String url;
    private long successCount;
    private long failedCount;
    private long lastFailTime;

    public URLCallCounters(String date, String url, long successCount, long failedCount, long lastFailTime) {
        this.date = date;
        this.url = url;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.lastFailTime = lastFailTime;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public long getLastFailTime() {
        return lastFailTime;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("URLCallCounters");
        sb.append("[ ").append(super.toString()).append(TAB);
        sb.append("date='").append(date).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", successCount=").append(successCount);
        sb.append(", failedCount=").append(failedCount);
        sb.append(", lastFailTime=").append(DateUtils.getFormatedTime(lastFailTime));
        sb.append(']');
        return sb.toString();
    }
}
