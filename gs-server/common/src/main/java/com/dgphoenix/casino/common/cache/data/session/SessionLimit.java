package com.abs.casino.common.cache.data.session;

import com.abs.casino.common.cache.IDistributedCacheEntry;

/**
 * User: flsh
 * Date: 8/24/11
 */
public class SessionLimit implements IDistributedCacheEntry {
    public static final int DEFAULT_LIMIT = 500;
    private final long bankId;
    private int limit = DEFAULT_LIMIT;
    private int currentSessionsCount;

    public SessionLimit(long bankId, int limit) {
        this.bankId = bankId;
        this.limit = limit;
        this.currentSessionsCount = 0;
    }

    public long getBankId() {
        return bankId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getCurrentSessionsCount() {
        return currentSessionsCount;
    }

    public boolean isLimitExceeded() {
        return currentSessionsCount >= limit;
    }

    public void setCurrentSessionsCount(int currentSessionsCount) {
        this.currentSessionsCount = currentSessionsCount;
    }

    public void incrementCurrentSessionsCount() {
        this.currentSessionsCount++;
    }

    public void decrementCurrentSessionsCount() {
        if (this.currentSessionsCount > 0) {
            this.currentSessionsCount--;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SessionLimit");
        sb.append("[bankId=").append(bankId);
        sb.append(", limit=").append(limit);
        sb.append(", currentSessionsCount=").append(currentSessionsCount);
        sb.append(']');
        return sb.toString();
    }
}
