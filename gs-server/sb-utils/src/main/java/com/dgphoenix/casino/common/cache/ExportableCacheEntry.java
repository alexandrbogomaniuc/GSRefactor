package com.abs.casino.common.cache;

import com.abs.casino.common.cache.IDistributedCacheEntry;
import com.abs.casino.common.cache.Identifiable;

/**
 * User: flsh
 * Date: 17.09.2009
 */
public class ExportableCacheEntry {
    private String key;
    private IDistributedCacheEntry value;

    public ExportableCacheEntry() {
    }

    public ExportableCacheEntry(String key, IDistributedCacheEntry value) {
        this.key = key;
        this.value = value;
    }

    public ExportableCacheEntry(long id, IDistributedCacheEntry value) {
        this.key = String.valueOf(id);
        this.value = value;
    }

    public <T extends Identifiable & IDistributedCacheEntry> ExportableCacheEntry(T val) {
        this.key = String.valueOf(val.getId());
        this.value = val;
    }

    public <T extends Identifiable & IDistributedCacheEntry> void setNew(T val) {
        this.key = String.valueOf(val.getId());
        this.value = val;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public IDistributedCacheEntry getValue() {
        return value;
    }

    public void setValue(IDistributedCacheEntry value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ExportableCacheEntry");
        sb.append("[key='").append(key).append('\'');
        sb.append(", value=").append(value);
        sb.append(']');
        return sb.toString();
    }
}
