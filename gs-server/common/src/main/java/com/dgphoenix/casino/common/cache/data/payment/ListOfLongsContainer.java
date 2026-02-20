package com.dgphoenix.casino.common.cache.data.payment;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;

import java.util.List;

/**
 * User: flsh
 * Date: 3/15/11
 */
public class ListOfLongsContainer implements IDistributedCacheEntry {
    private List<Long> longs;

    public ListOfLongsContainer(List<Long> longs) {
        this.longs = longs;
    }

    public ListOfLongsContainer() {
    }

    public List<Long> getLongs() {
        return longs;
    }

    public void setLongs(List<Long> longs) {
        this.longs = longs;
    }
}
