package com.abs.casino.cache;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.abs.casino.common.util.system.IMetricEvaluator;
import com.abs.casino.common.util.system.Metric;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.Parser;
import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.system.MetricsManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by inter on 08.12.15.
 */
public class PingSessionCache {
    protected final static ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(15);
    private static PingSessionCache instance = new PingSessionCache();
    private Cache<String, CacheEntry> cache = null;
    private static final Logger LOG = Logger.getLogger(PingSessionCache.class);
    protected static final long TERMINATION_TIMEOUT = 5000L;
    private AtomicLong statRunCount = new AtomicLong(0);
    private AtomicLong statErrorCount = new AtomicLong(0);
    private AtomicLong checkSidCount = new AtomicLong(0);

    private PingSessionCache() {

    }

    public static PingSessionCache getInstance() {
        return instance;
    }

    class CacheEntry implements Runnable {
        private int errorCount;
        private volatile boolean isRunning = false;
        private BankInfo bankInfo;
        private String userId;
        private long lastRun;

        public CacheEntry(long bankId, String userId) {
            bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            this.userId = userId;
        }

        public void schedule() {
            if (!isRunning) {
                synchronized (this) {
                    if (!isRunning) {
                        isRunning = true;
                        threadPool.schedule(this, 0, TimeUnit.MICROSECONDS);
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                statRunCount.incrementAndGet();

                long bankId = bankInfo.getId();
                String url = bankInfo.getRefreshExternalSessionUrl();
                Map<String, String> htbl = new HashMap<String, String>(1);
                htbl.put("userId", userId);
                htbl.put("bankId", String.valueOf(bankId));

                long now = System.currentTimeMillis();
                LOG.info("request, request to url:" + url + " bankId:" + bankId + " is:" + htbl);


                String sb = HttpClientConnection.newInstance(1000 * 60).
                        doRequest(bankInfo.isUsesJava8Proxy(), url, htbl, false, null, false);

                LOG.info("request, response from url:" + url + " bankId:" + bankId + " is:" + sb +
                        " time: " + (System.currentTimeMillis() - now));

                XmlRequestResult result = new XmlRequestResult();
                Parser parser = Parser.instance();
                parser.parse(sb, result);

                if (result.isSuccessful()) {
                    errorCount = 0;
                } else {
                    statErrorCount.incrementAndGet();
                    errorCount = 100;
                }
            } catch (Exception e) {
                LOG.warn("PingSessionCache::pingRequest error:", e);
                errorCount++;
                statErrorCount.incrementAndGet();
            } finally {
                lastRun = System.currentTimeMillis();
                isRunning = false;
            }
        }

        @Override
        public String toString() {
            return "CacheEntry{" +
                    "errorCount=" + errorCount +
                    ", isRunning=" + isRunning +
                    ", bankInfo=" + bankInfo +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }

    public void init() {
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

            StatisticsManager.getInstance()
                    .registerStatisticsGetter("PingSessionCache statistics", new IStatisticsGetter() {
                        @Override
                        public String getStatistics() {
                            return String.valueOf("poolSize=" + threadPool.getPoolSize() +
                                    ", taskCount=" + threadPool.getTaskCount() +
                                    ", errorCount=" + statErrorCount.get() +
                                    ", runCount=" + statRunCount.get() +
                                    ", checkSidCount=" + checkSidCount.get());
                        }
                    });

            MetricsManager.getInstance().register(Metric.PING_SESSION_CACHE_TASK_COUNT, new IMetricEvaluator() {
                @Override
                public long getValue() {
                    return threadPool.getTaskCount();
                }
            });
        }
    }

    public void shutdown() {
        LOG.info(":shutdown started:");
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), threadPool, TERMINATION_TIMEOUT);
        LOG.info("shutdown completed");
    }

    public boolean checkSid(String sessionId) {
        checkSidCount.incrementAndGet();
        if (cache == null || sessionId == null) {
            return false;
        }
        CacheEntry cacheEntry = cache.getIfPresent(sessionId);
        if (cacheEntry != null) {
            if (cacheEntry.errorCount > 3) {
                return false;
            }
            long now = System.currentTimeMillis();
            if ((now - cacheEntry.lastRun) < 3 * 60 * 1000) {
                cacheEntry.schedule();
                return true;
            }
        }
        SessionHelper sessionHelper = SessionHelper.getInstance();
        AccountInfo accountInfo = null;
        try {
            sessionHelper.lock(sessionId);
            sessionHelper.openSession();
            ITransactionData transactionData = sessionHelper.getTransactionData();
            GameSession gameSession = transactionData.getGameSession();
            if (gameSession == null) {
                cache.invalidate(sessionId);
            } else {
                accountInfo = transactionData.getAccount();
            }
        } catch (Exception ex) {
            LOG.error("Can't get lock", ex);
        } finally {
            sessionHelper.clearWithUnlock();
        }
        if (accountInfo == null) {
            return false;
        }
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry(accountInfo.getBankId(), accountInfo.getExternalId());
        }
        cache.put(sessionId, cacheEntry);
        cacheEntry.schedule();
        return true;
    }

}
