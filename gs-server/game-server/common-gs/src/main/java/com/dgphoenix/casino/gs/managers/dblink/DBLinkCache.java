package com.abs.casino.gs.managers.dblink;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraTransactionDataPersister;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.transactiondata.TransactionDataInvalidatedListener;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.ExecutorUtils;
import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.abs.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.GameServer;
import com.abs.casino.gs.TransactionDataTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@com.abs.casino.common.cache.CacheKeyInfo(description = "dbLink.id")
public class DBLinkCache implements IDistributedCache<Long, IDBLink> {
    private static final DBLinkCache instance = new DBLinkCache();
    public static final long SLEEPTIME = 90000;
    private static final Logger LOG = LogManager.getLogger(DBLinkCache.class);
    // gameSessionId->IDBLink
    private static final NonBlockingHashMapLong<IDBLink> dbLinks = new NonBlockingHashMapLong<>(2048, false);
    private static long cleanerTotalCount = 0;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile CassandraTransactionDataPersister transactionDataPersister;

    static {
        StatisticsManager.getInstance()
                .registerStatisticsGetter("DBLinkCache size", new IStatisticsGetter() {
                    @Override
                    public String getStatistics() {
                        return String.valueOf(dbLinks.size());
                    }
                });
        StatisticsManager.getInstance()
                .registerStatisticsGetter("DBLinkCache cleanerTotalCount", new IStatisticsGetter() {
                    @Override
                    public String getStatistics() {
                        return String.valueOf(cleanerTotalCount);
                    }
                });
    }

    private DBLinkCache() {
        scheduler.scheduleAtFixedRate(new Cleaner(), SLEEPTIME, SLEEPTIME, TimeUnit.MILLISECONDS);
    }

    public static DBLinkCache getInstance() {
        return instance;
    }

    public void registerTDInvalidationListener() {
        getTransactionDataPersister().registerInvalidationListener(new TransactionDataInvalidatedListener() {
            @Override
            public void invalidate(ITransactionData td) {
                try {
                    GameSession gameSession = td.getGameSession();
                    if (gameSession != null) {
                        LOG.info("registerTDInvalidationListener.invalidate: " + gameSession.getId());
                        remove(gameSession.getId());
                    }
                } catch (Exception e) {
                    LOG.error("invalidate error: " + td, e);
                }
            }
        });
    }

    private CassandraTransactionDataPersister getTransactionDataPersister() {
        CassandraTransactionDataPersister persister = transactionDataPersister;
        if (persister != null) {
            return persister;
        }
        synchronized (this) {
            persister = transactionDataPersister;
            if (persister == null) {
                if (ApplicationContextHelper.getApplicationContext() == null) {
                    throw new IllegalStateException("Spring applicationContext is not initialized for DBLinkCache");
                }
                CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                        .getBean("persistenceManager", CassandraPersistenceManager.class);
                persister = persistenceManager.getPersister(CassandraTransactionDataPersister.class);
                transactionDataPersister = persister;
            }
        }
        return persister;
    }

    public boolean isExist(long gameSessionId) {
        return dbLinks.containsKey(gameSessionId);
    }

    public IDBLink putAndGetSilent(IDBLink dblink) throws CommonException {
        long now = System.currentTimeMillis();

        long accountId = dblink.getAccountId();
        long gameSessionId = dblink.getGameSessionId();
        IDBLink existDbLink = get(gameSessionId);
        if (existDbLink != null) {
            LOG.warn("putAndGetSilent: DBLink already found, accountId:" + accountId +
                    ", gameSessionId=" + gameSessionId);
            return existDbLink;
        }
        existDbLink = dbLinks.putIfAbsent(gameSessionId, dblink);
        StatisticsManager.getInstance().updateRequestStatistics("DBLinkCache: putAndGetSilent",
                System.currentTimeMillis() - now, accountId);
        return existDbLink != null ? existDbLink : dblink;
    }

    public void put(IDBLink dblink) throws CommonException {
        long now = System.currentTimeMillis();

        long accountId = dblink.getAccountId();
        long gameSessionId = dblink.getGameSessionId();
        if (isExist(gameSessionId)) {
            throw new CommonException("dbLink for accountId:" + accountId + " already exist, " +
                    "gameSessionId=" + gameSessionId);
        }
        dbLinks.putIfAbsent(gameSessionId, dblink);
        StatisticsManager.getInstance().updateRequestStatistics("DBLinkCache: put",
                System.currentTimeMillis() - now, accountId);
    }


    public IDBLink get(long gameSessionId) {
        return dbLinks.get(gameSessionId);
    }

    public void remove(long gameSessionId) {
        LOG.info("remove gameSessionId: " + gameSessionId);
        dbLinks.remove(gameSessionId);
    }

    @Override
    public IDBLink getObject(String id) {
        return dbLinks.get(Long.parseLong(id));
    }

    @Override
    public Map<Long, IDBLink> getAllObjects() {
        return dbLinks;
    }

    public int size() {
        return dbLinks.size();
    }

    @Override
    public String getAdditionalInfo() {
        return "";
    }

    @Override
    public String printDebug() {
        return "";
    }

    public void shutdown() {
        ExecutorUtils.shutdownService(this.getClass().getSimpleName(), scheduler, 1000);
    }

    class Cleaner extends Thread {
        public void run() {
            int removeCount = 0;
            long now = System.currentTimeMillis();
            LOG.info("Running cleaner");
            if (GameServer.getInstance().isInitialized() && !dbLinks.isEmpty()) {
                final Collection<IDBLink> values = dbLinks.values();
                for (IDBLink link : values) {
                    final long inactivityTime = System.currentTimeMillis() - link.getLastActivity();
                    if (inactivityTime > TransactionDataTracker.getDefaultSessionTimeout() + 10000) {
                        long gameSessionId = link.getGameSessionId();
                        remove(gameSessionId);
                        removeCount++;
                    }
                    if (!GameServer.getInstance().isInitialized()) {
                        return;
                    }
                }
            }
            cleanerTotalCount += removeCount;
            StatisticsManager.getInstance().updateRequestStatistics("DBLinkCache cleaner",
                    System.currentTimeMillis() - now);
            LOG.info("Removed old DBLinks: " + removeCount);
        }
    }
}
