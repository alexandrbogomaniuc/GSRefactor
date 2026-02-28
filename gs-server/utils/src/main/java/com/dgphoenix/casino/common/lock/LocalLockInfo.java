package com.abs.casino.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: flsh
 * Date: 19.03.13
 */
@SuppressWarnings("UnusedDeclaration")
public class LocalLockInfo {
    private ReentrantLock localLock = new ReentrantLock();
    private volatile long lastUpdated = System.currentTimeMillis();

    public void lock() {
        touch();
        getLocalLock().lock();
    }

    public boolean tryLock() {
        touch();
        return getLocalLock().tryLock();
    }

    public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        touch();
        return getLocalLock().tryLock(timeout, unit);
    }

    public void unlock() {
        touch();
        getLocalLock().unlock();
    }

    public boolean isHeldByCurrentThread() {
        return getLocalLock().isHeldByCurrentThread();
    }

    public boolean isLocked() {
        return getLocalLock().isLocked();
    }

    public void touch() {
        lastUpdated = System.currentTimeMillis();
    }

    private ReentrantLock getLocalLock() {
        return localLock;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalLockInfo lockInfo = (LocalLockInfo) o;

        if (!localLock.equals(lockInfo.localLock)) return false;
        if (lastUpdated != lockInfo.lastUpdated) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = localLock.hashCode();
        result = 31 * result + (int) (lastUpdated ^ (lastUpdated >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LocalLockInfo[" +
                "localLock=" + localLock +
                ", lastUpdated=" + lastUpdated +
                ']';
    }
}
