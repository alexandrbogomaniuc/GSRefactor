package com.dgphoenix.casino.common.cache;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * User: flsh
 * Date: 17.09.2009
 */
public abstract class AbstractExportableCache<T extends IDistributedCacheEntry> extends AbstractDistributedCache<T> {
    private static final Logger LOG = Logger.getLogger(AbstractExportableCache.class);

    public abstract void exportEntries(ObjectOutputStream outStream) throws IOException;

    public void exportEntries(ObjectOutputStream outStream, Long bankId) throws IOException {
        //by default export all, if need filter in overrided
        exportEntries(outStream);
    }

    public void importEntry(ExportableCacheEntry entry) {
        try {
            put((T) entry.getValue());
        } catch (Throwable e) {
            LOG.debug("Cannot put entry to cache: " + entry, e);
            throw new RuntimeException("Cannot put entry to cache", e);
        }
    }

    public boolean isNoReferenceMode() {
        return true;
    }

    public boolean isRequiredForImport() {
        return false;
    }
}
