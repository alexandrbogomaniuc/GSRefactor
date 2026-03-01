package com.abs.casino.common.util;

import com.abs.casino.common.cache.IDistributedCacheEntry;

/**
 * User: flsh
 * Date: 16.07.2009
 */
public interface ISequencer extends IDistributedCacheEntry {
    void init();

    void setValue(long value);

    long getValue();

    long getAndIncrement();

    void setName(String name);

    String getName();

    void shutdownAllocator();
}
