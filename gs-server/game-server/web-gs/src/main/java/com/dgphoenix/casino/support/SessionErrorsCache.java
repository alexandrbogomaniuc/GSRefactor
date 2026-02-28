package com.abs.casino.support;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraSupportPersister;
import com.abs.casino.common.exception.ObjectNotFoundException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MINUTES;

public class SessionErrorsCache {
    private Logger LOG = Logger.getLogger(SessionErrorsCache.class);

    private final CassandraSupportPersister supportPersister;
    private static final SessionErrorsCache INSTANCE = new SessionErrorsCache();

    private LoadingCache<String, Map<Long, String>> cache;

    private SessionErrorsCache() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        supportPersister = persistenceManager.getPersister(CassandraSupportPersister.class);
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, MINUTES)
                .build(new CacheLoader<String, Map<Long, String>>() {
                    @Override
                    public Map<Long, String> load(String key) throws Exception {
                        Map<Long, String> result = supportPersister.getValuesBySessionID(key);
                        if (result == null || result.isEmpty()) {
                            throw new ObjectNotFoundException("Nothing found for key: " + key);
                        }
                        return result;
                    }
                });
    }

    public static SessionErrorsCache getInstance() {
        return INSTANCE;
    }

    public Iterable<String> getSessionIDs() {
        return supportPersister.getSessionIDs();
    }

    public Map<Long, String> getSessionErrorsAsMap(String sessionId) {
        try {
            return unmodifiableMap(cache.get(sessionId));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ObjectNotFoundException) {
                LOG.error("getSessionErrorsAsMap:: cache error: " + e.getMessage());
            } else {
                LOG.error("getSessionErrorsAsMap:: cache error", e);
            }
            return emptyMap();
        }
    }

    public Iterable<String> getSessionErrors(String sessionId) {
        return getSessionErrorsAsMap(sessionId).values();
    }

    public void invalidate(String sessionId) {
        cache.invalidate(sessionId);
    }
}
