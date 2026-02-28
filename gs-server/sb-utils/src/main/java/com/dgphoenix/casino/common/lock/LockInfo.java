package com.abs.casino.common.lock;

import com.abs.casino.common.lock.ServerLockInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 19.03.13
 */
public class LockInfo {
    private ReentrantLock localLock = new ReentrantLock();
    private ConcurrentMap<Integer, ServerLockInfo> lastLockInfo = new ConcurrentHashMap<Integer, ServerLockInfo>();
    private volatile long lastUpdated = System.currentTimeMillis();

    public ReentrantLock getLocalLock() {
        return localLock;
    }

    public void setLocalLock(ReentrantLock localLock) {
        this.localLock = localLock;
    }

    public Map<Integer, ServerLockInfo> getLastLockInfo() {
        return lastLockInfo;
    }

    public void setLastLockInfo(ConcurrentMap<Integer, ServerLockInfo> lastLockInfo) {
        this.lastLockInfo = lastLockInfo;
        setLastUpdated(System.currentTimeMillis());
    }

    public void updateLastLockInfo(List<ServerLockInfo> lockInfos) {
        lastLockInfo.clear();
        for (ServerLockInfo lockInfo : lockInfos) {
            lastLockInfo.put(lockInfo.getServerId(), lockInfo);
        }
        setLastUpdated(System.currentTimeMillis());
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LockInfo lockInfo = (LockInfo) o;

        if (lastUpdated != lockInfo.lastUpdated) return false;
        if (!lastLockInfo.equals(lockInfo.lastLockInfo)) return false;
        if (!localLock.equals(lockInfo.localLock)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = localLock.hashCode();
        result = 31 * result + lastLockInfo.hashCode();
        result = 31 * result + (int) (lastUpdated ^ (lastUpdated >>> 32));
        return result;
    }

}
