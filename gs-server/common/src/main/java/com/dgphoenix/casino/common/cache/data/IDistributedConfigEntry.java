package com.abs.casino.common.cache.data;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

/**
 * User: flsh
 * Date: 4/11/12
 */
public interface IDistributedConfigEntry<T> extends IDistributedCacheEntry {
    void copy(IDistributedConfigEntry entry);
}
