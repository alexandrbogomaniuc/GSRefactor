package com.dgphoenix.casino.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mic on 03.12.14.
 */
public class PingCache {
    private static final PingCache INSTANCE = new PingCache();

    private final Map<String, Boolean> cache = new ConcurrentHashMap<>();

    private PingCache() {
    }

    public static PingCache getInstance() {
        return INSTANCE;
    }

    public Boolean get(String urlString) {
        return cache.get(urlString);
    }

    public void put(String urlString, boolean result) {
        cache.put(urlString, result);
    }

    public void invalidate() {
        cache.clear();
    }
}
