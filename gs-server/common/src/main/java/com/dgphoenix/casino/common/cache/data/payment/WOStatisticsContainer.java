package com.abs.casino.common.cache.data.payment;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.data.payment.WOStatistics;

import java.util.List;

/**
 * User: flsh
 * Date: 3/15/11
 */
public class WOStatisticsContainer implements IDistributedCacheEntry {
    private List<WOStatistics> statistics;

    public WOStatisticsContainer() {
    }

    public WOStatisticsContainer(List<WOStatistics> statistics) {
        this.statistics = statistics;
    }

    public List<WOStatistics> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<WOStatistics> statistics) {
        this.statistics = statistics;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("WOStatisticsContainer");
        sb.append("[statistics=").append(statistics);
        sb.append(']');
        return sb.toString();
    }
}
