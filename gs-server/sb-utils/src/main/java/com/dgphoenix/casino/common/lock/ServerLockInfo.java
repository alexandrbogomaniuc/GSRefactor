package com.dgphoenix.casino.common.lock;

import java.util.Date;

/**
 * User: flsh
 * Date: 19.03.13
 */
public class ServerLockInfo implements Comparable<ServerLockInfo> {
    private String lockId;
    private int serverId;
    private int lastLockerServerId;
    private long lockTime;
    private long timeStamp;

    public ServerLockInfo(String lockId) {
        this.lockId = lockId;
    }

    public ServerLockInfo(String lockId, int serverId, long lockTime, long timeStamp, int lastLockerServerId) {
        this.lockId = lockId;
        this.serverId = serverId;
        this.lockTime = lockTime;
        this.timeStamp = timeStamp;
        this.lastLockerServerId = lastLockerServerId;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getLastLockerServerId() {
        return lastLockerServerId;
    }

    public void setLastLockerServerId(int lastLockerServerId) {
        this.lastLockerServerId = lastLockerServerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerLockInfo that = (ServerLockInfo) o;
        if(lockId != that.lockId) return false;
        if (lockTime != that.lockTime) return false;
        if (serverId != that.serverId) return false;
        if (timeStamp != that.timeStamp) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serverId;
        result = 31 * result + (int) (lockTime ^ (lockTime >>> 32));
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
    }


    @Override
    public String toString() {
        return "ServerLockInfo[" +
                "lockId=" + lockId +
                ", serverId=" + serverId +
                ", lockTime=" + lockTime +
                ", timeStamp=" + new Date(timeStamp) +
                ", lastLockerServerId=" + lastLockerServerId +
                ']';
    }

    @Override
    public int compareTo(ServerLockInfo o) {
        long thisTime = this.lockTime;
        long anotherTime = o.lockTime;
        int result = (thisTime<anotherTime ? -1 : (thisTime==anotherTime ? 0 : 1));
        if(result != 0) {
            return result;
        }
        return serverId < o.serverId ? -1 : (serverId == o.serverId ? 0 : 1);
    }


}
