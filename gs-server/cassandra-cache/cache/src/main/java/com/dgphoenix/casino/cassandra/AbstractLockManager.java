package com.abs.casino.cassandra;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.ILoadBalancer;
import com.abs.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.*;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.NtpTimeProvider;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by grien on 17.02.15.
 */
public abstract class AbstractLockManager extends AbstractCassandraPersister<String, Integer> implements ILockManager {

    private static final String LOCK_ID = "LOCK_ID";
    //locker is serverId holded this lock
    private static final String LOCKER = "LOCKER";
    private static final String LAST_LOCKER = "LLOCKER";
    private static final String LOCK_TIME = "LOCK_TIME";

    private static final long SUSPICIOUSLY_HIGH_LOCK_TIME = TimeUnit.HOURS.toMillis(1);
    private static final long TIME_TO_KEEP_LOCAL_LOCKS = TimeUnit.MINUTES.toMillis(3);
    private static final long SERVER_OFFLINE_TIMEOUT = TimeUnit.MINUTES.toMillis(1);
    private static final long MAX_LOCK_TIME = SERVER_OFFLINE_TIMEOUT + 5000;
    private static final long SLEEP_TIME = 50;
    private static final long CLEAN_INTERVAL = TimeUnit.MINUTES.toMillis(10);
    private static final long CHECK_LOCK_TIME = TimeUnit.DAYS.toMillis(7);
    private static final long LOCK_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    private static final long INTERVAL_IN_MINUTES = TimeUnit.DAYS.toMinutes(1);
    private static final long INITIAL_DELAY_IN_MINUTES = 10;

    private static final int IMMEDIATELY_DELETE_LOCK_ATTEMPS_COUNT = 5;
    private static final long DELETE_LOCK_SLEEP_TIMEOUT = 5000;

    private final ConcurrentMap<String, LocalLockInfo> localLocksCache;
    private final LoadingCache<String, ServerLockInfo> serverLocksCache;
    private final List<ChangeLockListener> changeLockListeners = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile Integer serverId;
    private volatile ILoadBalancer loadBalancer;
    private volatile IRemoteUnlocker remoteUnlocker;

    protected AbstractLockManager(int localLocksInitSize, int serverLocksInitSize, int serverLocksMaxSize,
                                  int serverLocksConcurrencyLevel) {
        this(localLocksInitSize, serverLocksInitSize, serverLocksMaxSize, serverLocksConcurrencyLevel, null, null);
    }

    protected AbstractLockManager(int localLocksInitSize, int serverLocksInitSize, int serverLocksMaxSize,
                                  int serverLocksConcurrencyLevel, Integer serverId, ILoadBalancer loadBalancer) {
        this.serverId = serverId;
        this.localLocksCache = new ConcurrentHashMap<>(localLocksInitSize);
        this.serverLocksCache = getServerLocksCache(serverLocksInitSize, serverLocksMaxSize, serverLocksConcurrencyLevel);
        this.loadBalancer = loadBalancer;
        registerStatisticsGetter();
    }

    protected int getServerId() {
        if (serverId == null) {
            throw new RuntimeException("Server ID is null. ServerIdProvider not initialized.");
        }
        return serverId;
    }

    private LoadingCache<String, ServerLockInfo> getServerLocksCache(int initSize, int maxSize, int concurrencyLevel) {
        return CacheBuilder
                .newBuilder()
                .initialCapacity(initSize)
                .maximumSize(maxSize)
                .recordStats()
                .concurrencyLevel(concurrencyLevel)
                .build(new CacheLoader<String, ServerLockInfo>() {
                    @Override
                    public ServerLockInfo load(String id) {
                        return AbstractLockManager.this.getCurrentLocker(id);
                    }
                });
    }

    private void registerStatisticsGetter() {
        StatisticsManager.getInstance().registerStatisticsGetter(getClass().getSimpleName() + " locks size",
                () -> "localLocks size=" + localLocksCache.size() +
                        ", serverLocks=" + serverLocksCache.stats());
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return new TableDefinition(getMainColumnFamilyName(),
                Arrays.asList(
                        new ColumnDefinition(LOCK_ID, com.datastax.driver.core.DataType.text(), false, false, true),
                        new ColumnDefinition(LOCKER, com.datastax.driver.core.DataType.cint(), false, true, false),
                        new ColumnDefinition(LAST_LOCKER, com.datastax.driver.core.DataType.cint()),
                        new ColumnDefinition(LOCK_TIME, com.datastax.driver.core.DataType.bigint())
                ), LOCK_ID)
                .caching(Caching.ACTUAL_DATA)
                .compaction(CompactionStrategy.getLeveled(true, TimeUnit.HOURS.toSeconds(1)))
                .gcGraceSeconds(0);
    }

    @Override
    public abstract String getMainColumnFamilyName();

    @Override
    public void init() {
        super.init();
        scheduler.scheduleAtFixedRate(new Cleaner(), CLEAN_INTERVAL, CLEAN_INTERVAL, TimeUnit.MILLISECONDS);
        scheduler.scheduleWithFixedDelay(new CleanOldLocksTask(), INITIAL_DELAY_IN_MINUTES,
                INTERVAL_IN_MINUTES, TimeUnit.MINUTES);
        getLog().debug("CleanOldLocksTask was started, first execution will be in {} minutes",
                INITIAL_DELAY_IN_MINUTES);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        ExecutorUtils.shutdownService(getClass().getSimpleName(), scheduler, 1000);
    }

    public long getLocalLocksCacheSize() {
        return localLocksCache.size();
    }

    public long getServerLocksCacheSize() {
        return serverLocksCache.size();
    }

    public void setLoadBalancer(ILoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void setRemoteUnlocker(IRemoteUnlocker remoteUnlocker) {
        this.remoteUnlocker = remoteUnlocker;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    private ServerLockInfo getCurrentLocker(String id) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(LOCK_TIME, LOCKER, LAST_LOCKER).
                where(eq(LOCK_ID, id)).limit(1);
        com.datastax.driver.core.ResultSet resultSet = execute(select, "getCurrentLocker");
        com.datastax.driver.core.Row row = resultSet.one();
        if (row == null) {
            return null;
        }
        long lockTime = row.getLong(LOCK_TIME);
        int lockerServerId = row.getInt(LOCKER);
        int lastLocker = row.getInt(LAST_LOCKER);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getCurrentLocker",
                System.currentTimeMillis() - now, id);
        return new ServerLockInfo(id, lockerServerId, lockTime, lockTime, lastLocker);
    }

    private boolean persist(String id, ServerLockInfo currentLocker, long newLockTime) {
        com.datastax.driver.core.ResultSet resultSet;
        if (currentLocker == null) {
            com.datastax.driver.core.Statement query = getInsertQuery().
                    value(LOCK_ID, id).
                    value(LOCKER, getLockerId()).
                    value(LOCK_TIME, newLockTime).
                    value(LAST_LOCKER, getLockerId()).
                    ifNotExists();
            resultSet = executeWithCheckTimeout(query, "persist: insert");
        } else {
            com.datastax.driver.core.Statement updateQuery =
                    getUpdateQuery(eq(LOCK_ID, id)).
                            with().
                            and(set(LOCK_TIME, newLockTime)).
                            and(set(LOCKER, getLockerId())).
                            and(set(LAST_LOCKER, getLockerId())).
                            onlyIf(eq(LOCK_TIME, currentLocker.getLockTime()));
            resultSet = executeWithCheckTimeout(updateQuery, "persist: update");
        }
        return resultSet.wasApplied();
    }

    protected boolean delete(String lockId, long lockTime) {
        long now = System.currentTimeMillis();
        byte step = 0;
        boolean success, needSleep = false;
        do {
            long now1 = 0;
            try {
                if (needSleep) {
                    Thread.sleep(DELETE_LOCK_SLEEP_TIMEOUT);
                    needSleep = false;
                }
                now1 = System.currentTimeMillis();
                com.datastax.driver.core.Statement query =
                        getUpdateQuery(eq(LOCK_ID, lockId)).
                                with(set(LOCKER, -1)).
                                onlyIf(eq(LOCKER, getLockerId()));
                com.datastax.driver.core.ResultSet resultSet = execute(query, "delete");
                success = resultSet.wasApplied();
                break;
            } catch (InterruptedException e) {
                getLog().error("Can't delete lock: {}, interrupted", lockId, e);
                success = false;
                break;
            } catch (Throwable t) {
                Date date = new Date(lockTime);
                getLog().error("Can't delete lock: " + lockId + ", lockTime=" + date + ", exception: " + t);
                getLog().warn("Can't delete lock: " + lockId + ", lockTime=" + date, t);
                if (step >= IMMEDIATELY_DELETE_LOCK_ATTEMPS_COUNT || ++step >= IMMEDIATELY_DELETE_LOCK_ATTEMPS_COUNT) {
                    needSleep = true;
                }
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                        " delete (catch Throwable)", System.currentTimeMillis() - now1, lockId);
            }
        } while (true);

        if (success) {
            ServerLockInfo serverLockInfo = serverLocksCache.getIfPresent(lockId);
            if (serverLockInfo != null) {
                serverLockInfo.setServerId(getLockerId());
                serverLockInfo.setLastLockerServerId(getLockerId());
            }
        } else { //only if interrupted
            getLog().error("delete: {}, is not success", lockId);
            serverLocksCache.invalidate(lockId);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " delete",
                System.currentTimeMillis() - now, lockId);
        return success;
    }

    @Override
    public boolean isLockOwner(String id) {
        LocalLockInfo lockInfo = localLocksCache.get(id);
        return lockInfo != null && lockInfo.isHeldByCurrentThread();
    }

    @Override
    public LockingInfo lock(String id) throws CommonException {
        return lock(id, MAX_LOCK_TIME);
    }

    @Override
    public LockingInfo lock(String id, final long timeout) throws CommonException {
        final long now = System.currentTimeMillis();
        return lock(id, new WaitCondition() {
            @Override
            public boolean mustWait() {
                return System.currentTimeMillis() - now <= timeout;
            }

            @Override
            public long getWaitTimeInMillis() {
                return timeout + now - System.currentTimeMillis();
            }
        });
    }

    @Override
    public LockingInfo tryLock(String id) throws CommonException {
        return lock(id, new WaitCondition() {
            /**
             * First attempt may be unsuccessful only because of specifics of optimistic locking strategy.
             * 2 attempts must be allowed for fix this question.
             */
            private boolean firstAttempt = true;

            @Override
            public boolean mustWait() {
                if (firstAttempt) {
                    firstAttempt = false;
                    return true;
                }
                return false;
            }

            @Override
            public long getWaitTimeInMillis() {
                return 0;
            }
        });
    }

    private LockingInfo lock(String id, WaitCondition condition) throws CommonException {
        getLog().debug("try lock: {}", id);
        long now = System.currentTimeMillis();
        LocalLockInfo localLock = localLocksCache.computeIfAbsent(id, key -> new LocalLockInfo());
        boolean success = false;
        boolean locked = localLock.isHeldByCurrentThread();
        long waitTime = condition.getWaitTimeInMillis();
        if (!locked) {
            if (waitTime <= 0) {
                locked = localLock.tryLock();
            } else {
                try {
                    locked = localLock.tryLock(waitTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new CannotLockException("Cannot lock by id: " + id + ", interrupted", e);
                }
            }
        } else {
            getLog().warn("May be error, already locked: " + id);
        }
        if (!locked) {
            throw new CannotLockException("Cannot lock by id: " + id + ", timeout: " +
                    (System.currentTimeMillis() - now));
        }
        boolean triedRemoteUnlock = false;
        int locker = getLockerId();
        ServerLockInfo serverLock = null;
        try {
            do {
                serverLock = getServerLockSilent(id);

                boolean availableForLock = isAvailableForLock(locker, serverLock);
                if (!availableForLock && !triedRemoteUnlock) {
                    availableForLock = tryRemoteUnlock(serverLock);
                    triedRemoteUnlock = true;
                }

                long newLockTime = NtpTimeProvider.getInstance().getTime();
                success = availableForLock && persist(id, serverLock, newLockTime);
                if (success) {
                    if (serverLock != null) {
                        serverLock.setLockTime(newLockTime);
                        serverLock.setServerId(getLockerId());
                        notifyLockChangedListeners(serverLock);
                        serverLock.setLastLockerServerId(serverLock.getServerId());
                    } else { //if new lock, don't notify listener
                        serverLock = new ServerLockInfo(id, getLockerId(), newLockTime, newLockTime, getLockerId());
                    }
                    break;
                } else {
                    serverLocksCache.invalidate(id);
                }
                Thread.sleep(SLEEP_TIME);
            } while (condition.mustWait());
        } catch (Throwable e) {
            unlock(new LockingInfo(localLock, serverLock != null ? serverLock : new ServerLockInfo(id)), success);
            throw new CannotLockException("Cannot lock by id: " + id, e);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " lock",
                System.currentTimeMillis() - now, id);
        if (!success) {
            unlock(new LockingInfo(localLock, serverLock != null ? serverLock : new ServerLockInfo(id)), false);
            throw new CannotLockException("Cannot lock by id: " + id +
                    ", timeout: " + (System.currentTimeMillis() - now));
        }
        return new LockingInfo(localLock, serverLock);
    }

    public int getLockOwnerServerId(String lockId) {
        ServerLockInfo locker;
        try {
            locker = getServerLockSilent(lockId);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load lock: " + lockId, e);
        }
        return locker == null ? -1 : locker.getServerId();
    }

    private ServerLockInfo getServerLockSilent(String lockId) {
        try {
            return serverLocksCache.get(lockId);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            //nop, this may be if lock not found in DB
        } catch (ExecutionException e) {
            getLog().error("getServerLockSilent failed, lockId=" + lockId, e);
        }
        return null;
    }

    private int getLockerId() {
        return serverId;
    }

    @Override
    public boolean unlock(String id, long lockTime) {
        LocalLockInfo localLock = localLocksCache.get(id);
        if (localLock == null) {
            getLog().info("Unlock lock, which has been taken before shutdown, lockId: {}, lockTime: {}",
                    id, new Date(lockTime));
            return delete(id, lockTime);
        } else {
            return false;
        }
    }

    @Override
    public void unlock(LockingInfo lockInfo) {
        unlock(lockInfo, true);
    }

    private void unlock(LockingInfo lockInfo, boolean physicallyRemoveLock) {
        assert lockInfo != null : "lockInfo is null";
        long now = System.currentTimeMillis();
        final LocalLockInfo localLock = lockInfo.getLocalLockInfo();
        final String lockId = lockInfo.getLockId();
        if (physicallyRemoveLock && localLock.isHeldByCurrentThread()) {
            try {
                delete(lockId, lockInfo.getServerLockInfo().getLockTime());
            } catch (Exception e) {
                getLog().error("Cannot delete lock: {}", lockId, e);
                serverLocksCache.invalidate(lockId);
            }
        }
        if (localLock.isHeldByCurrentThread()) {
            localLock.unlock();
        } else {
            getLog().warn("Possible error, cannot unlock because not lock owner. lockId={}", lockId);
        }
        getLog().debug("unlock: {}", lockId);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " unlock",
                System.currentTimeMillis() - now, lockId);
    }

    private boolean isAvailableForLock(int lockerId, ServerLockInfo serverLockInfo) {
        if (serverLockInfo == null || serverLockInfo.getServerId() < 0 || serverLockInfo.getServerId() == lockerId) {
            return true;
        }
        if (!loadBalancer.isOnline(serverLockInfo.getServerId())) {
            getLog().warn("isAvailableForLock: found lock for down server: {}", serverLockInfo);
            if (System.currentTimeMillis() - serverLockInfo.getLockTime() > SERVER_OFFLINE_TIMEOUT) {
                getLog().warn("isAvailableForLock: lock for down server is expired: {}", serverLockInfo.getServerId());
                return true;
            }
        }
        return false;
    }

    private boolean tryRemoteUnlock(ServerLockInfo serverLockInfo) {
        boolean unlocked = false;
        if (remoteUnlocker != null) {
            Long lockerStartTime = loadBalancer.getStartTime(serverLockInfo.getServerId());
            if (lockerStartTime != null) {
                if (serverLockInfo.getLockTime() < lockerStartTime) {
                    getLog().debug("Lock time is lower than locker's start time, possibly lock before shutdown took place, " +
                            "lockInfo: {}, lockerStartTime: {}", serverLockInfo, new Date(lockerStartTime));
                    unlocked = remoteUnlocker.unlock(serverLockInfo.getServerId(), getClass(),
                            serverLockInfo.getLockId(), serverLockInfo.getLockTime());
                }
            }
        }
        return unlocked;
    }

    public void registerListener(ChangeLockListener listener) {
        changeLockListeners.add(listener);
    }

    private void notifyLockChangedListeners(ServerLockInfo lockInfo) {
        for (ChangeLockListener listener : changeLockListeners) {
            listener.lockChanged(lockInfo);
        }
    }

    private interface WaitCondition {
        boolean mustWait();

        long getWaitTimeInMillis();
    }

    private class Cleaner implements Runnable {
        @Override
        public void run() {
            int removeCount = 0;
            long now = System.currentTimeMillis();
            getLog().info("Running cleaner");
            for (Map.Entry<String, LocalLockInfo> lockEntry : localLocksCache.entrySet()) {
                if (!isInitialized()) {
                    return;
                }
                LocalLockInfo info = lockEntry.getValue();
                if (info == null) {
                    continue;
                }
                long inactivityTime = System.currentTimeMillis() - info.getLastUpdated();
                if (info.isLocked()) {
                    if (inactivityTime > SUSPICIOUSLY_HIGH_LOCK_TIME) {
                        getLog().warn("Lock is held for too long, lockId: {}, inactivity time: {}",
                                lockEntry.getKey(), inactivityTime);
                    }
                } else if (inactivityTime > TIME_TO_KEEP_LOCAL_LOCKS) {
                    localLocksCache.remove(lockEntry.getKey());
                    removeCount++;
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics(
                    AbstractLockManager.this.getClass().getSimpleName() + " cleaner",
                    System.currentTimeMillis() - now);
            getLog().info("Removed old lockInfo: {}", removeCount);
        }
    }

    private class CleanOldLocksTask implements Runnable {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            getLog().debug("Task execution started..");
            int countUnlocked = 0;
            int countFailed = 0;
            int total = 0;
            try {
                com.datastax.driver.core.ResultSet resultSet = getLockIds();
                for (com.datastax.driver.core.Row row : resultSet) {
                    long lastUpdateTime = row.getLong(LOCK_TIME);
                    if (System.currentTimeMillis() - lastUpdateTime > CHECK_LOCK_TIME) {
                        String lockId = row.getString(LOCK_ID);
                        LockingInfo lockingInfo = null;
                        try {
                            lockingInfo = lock(lockId, LOCK_TIMEOUT);
                            unlock(lockingInfo);
                            lockingInfo = null;
                            countUnlocked++;
                            getLog().debug("{} was successfully relocked", lockId);
                        } catch (CannotLockException e) {
                            countFailed++;
                        } catch (CommonException e) {
                            getLog().warn("Cannot lock {}", lockId, e);
                            countFailed++;
                        } finally {
                            total++;
                            if (lockingInfo != null) {
                                unlock(lockingInfo);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                getLog().error("CleanOldLocksTask was failed:", e);
            }

            StatisticsManager.getInstance().updateRequestStatistics(
                    AbstractLockManager.this.getClass().getSimpleName() + " CleanOldLocksTask",
                    System.currentTimeMillis() - startTime,
                    "Total=" + total + "|Failed=" + countFailed);
            getLog().debug("Task completed. Total lockIds affected = {}, unlocked = {}, failed = {}",
                    total, countUnlocked, countFailed);
        }
    }

    private com.datastax.driver.core.ResultSet getLockIds() {
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(getMainTableDefinition(), LOCK_ID, LOCK_TIME)
                .where(eq(LOCKER, getLockerId()));
        select.setFetchSize(1000);
        return execute(select, "getLockIds");
    }
}
