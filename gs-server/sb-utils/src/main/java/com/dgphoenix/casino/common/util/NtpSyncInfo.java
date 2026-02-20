package com.dgphoenix.casino.common.util;

/**
 * User: flsh
 * Date: 31.10.15.
 */
public class NtpSyncInfo {
    //equals org.apache.commons.net.ntp.TimeInfo.returnTime
    private Long lastSyncTime;
    //NTP clock offset for adjust local time
    private long offset;
    //System.currentTimeMillis()
    private long currentTime;

    public NtpSyncInfo() {
    }

    public NtpSyncInfo(long lastSyncTime, long offset, long currentTime) {
        this.lastSyncTime = lastSyncTime;
        this.offset = offset;
        this.currentTime = currentTime;
    }

    public Long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    public String toString() {
        return "NtpSyncInfo[" +
                "lastSyncTime=" + lastSyncTime +
                ", offset=" + offset +
                ", currentTime=" + currentTime +
                ']';
    }
}
