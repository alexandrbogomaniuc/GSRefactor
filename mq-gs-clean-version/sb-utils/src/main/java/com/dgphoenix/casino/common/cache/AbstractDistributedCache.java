package com.dgphoenix.casino.common.cache;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 17.09.2009
 */
public abstract class AbstractDistributedCache<T extends IDistributedCacheEntry> implements IDistributedCache {
    public abstract void put(T entry) throws CommonException;
    public void remove(String id) {
        //nop by default
    }
}
