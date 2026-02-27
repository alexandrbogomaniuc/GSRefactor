package com.dgphoenix.casino.statistics.http;

import com.abs.casino.common.util.web.HttpClientConnectionStatistics;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class LocalAccumulatedStatistics {
    private AtomicReference<ConcurrentMap<StatisticsKey, HttpClientConnectionStatistics>> holder
            = new AtomicReference<ConcurrentMap<StatisticsKey, HttpClientConnectionStatistics>>(new ConcurrentHashMap<StatisticsKey, HttpClientConnectionStatistics>());

    public HttpClientConnectionStatistics getForKey(String date, String url) {
        StatisticsKey key = new StatisticsKey(date, url);
        ConcurrentMap<StatisticsKey, HttpClientConnectionStatistics> map = getMap();
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            HttpClientConnectionStatistics newStat = new HttpClientConnectionStatistics();
            map.put(key, newStat);
            return newStat;
        }
    }

    public Map<StatisticsKey, HttpClientConnectionStatistics> getAsMap() {
        return Collections.unmodifiableMap(getMap());
    }

    public ConcurrentMap<StatisticsKey, HttpClientConnectionStatistics> flushStats() {
        return holder.getAndSet(new ConcurrentHashMap<StatisticsKey, HttpClientConnectionStatistics>());
    }


    private ConcurrentMap<StatisticsKey, HttpClientConnectionStatistics> getMap() {
        return holder.get();
    }
}
