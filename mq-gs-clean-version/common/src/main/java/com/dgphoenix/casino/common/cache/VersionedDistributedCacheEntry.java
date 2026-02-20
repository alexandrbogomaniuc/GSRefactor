package com.dgphoenix.casino.common.cache;

/**
 * User: flsh
 * Date: 6/27/11
 */
public abstract class VersionedDistributedCacheEntry implements IDistributedCacheEntry, Identifiable {
    protected long version = 0;

    protected VersionedDistributedCacheEntry() {
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void incrementVersion() {
        this.version++;
    }
}
