package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

/**
 * User: flsh
 * Date: 16.07.2009
 */
public interface IIntegerSequencer extends IDistributedCacheEntry {
    void init();

    void setValue(int value);

    int getValue();

    int getAndIncrement();

    void setName(String name);

    String getName();

    void shutdownAllocator();
}
