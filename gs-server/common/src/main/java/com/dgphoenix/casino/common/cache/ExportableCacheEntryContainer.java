package com.dgphoenix.casino.common.cache;

import com.abs.casino.common.cache.ExportableCacheEntry;
import java.io.Serializable;
import java.util.List;

/**
 * User: flsh
 * Date: 17.09.2009
 */
public class ExportableCacheEntryContainer implements Serializable {
    private List<ExportableCacheEntry> entries;

    public ExportableCacheEntryContainer(List<ExportableCacheEntry> entries) {
        this.entries = entries;
    }

    public List<ExportableCacheEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ExportableCacheEntry> entries) {
        this.entries = entries;
    }
}
