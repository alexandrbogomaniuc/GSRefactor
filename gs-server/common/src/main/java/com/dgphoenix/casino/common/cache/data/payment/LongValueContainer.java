package com.abs.casino.common.cache.data.payment;

import com.abs.casino.common.cache.IDistributedCacheEntry;

/**
 * User: flsh
 * Date: 3/15/11
 */
public class LongValueContainer implements IDistributedCacheEntry {
    private Long longValue;

    public LongValueContainer(Long longValue) {
        this.longValue = longValue;
    }

    public LongValueContainer() {
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }
}
