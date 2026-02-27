package com.abs.casino.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSessionCache {
    private static final Logger LOG = LogManager.getLogger(HttpSessionCache.class);
    private static HttpSessionCache instance = new HttpSessionCache();
    private final Map<Long, HttpSession> httpSessions = new ConcurrentHashMap<Long, HttpSession>();


    private HttpSessionCache() {
    }

    public static HttpSessionCache getInstance() {
        return instance;
    }

    public void put(long accountId, HttpSession httpSession) {
        this.httpSessions.put(accountId, httpSession);
    }

    public HttpSession get(long accountId) {
        return httpSessions.get(accountId);
    }

    public void invalidateAndRemove(long accountId) {
        invalidate(accountId);
        this.httpSessions.remove(accountId);
    }

    public void invalidate(long accountId) {
        HttpSession httpSession = this.httpSessions.get(accountId);
        if (httpSession != null) {
            try {
                httpSession.invalidate();
                LOG.debug("HttpSessionCache::invalidate httpSession for accountId:" + accountId +
                        " was invalidated");
            } catch (Throwable e) {
                LOG.error("HttpSessionCache::invalidateAndRemove error:" + e.getMessage());
            }
        }
    }

    public Map<Long, HttpSession> getHttpSessionsMap() {
        return httpSessions;
    }
}
