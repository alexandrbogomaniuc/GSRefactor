package com.abs.casino.gs.managers.payment.bonus.tracker;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.cassandra.persist.CassandraBonusPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraExpiredBonusTrackerInfoPersister;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.CalendarUtils;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.abs.casino.common.util.logkit.LogUtils;
import com.abs.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 28.11.14.
 */
public class ExpiredBonusTracker {
    private final static Logger LOG = LogManager.getLogger(ExpiredBonusTracker.class);
    private final static ExpiredBonusTracker instance = new ExpiredBonusTracker();
    public static final String LOCK_ID = "EXP_BONUS_TRACK";
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
    private final CassandraBonusPersister bonusPersister;
    private final CassandraExpiredBonusTrackerInfoPersister expiredBonusTrackerInfoPersister;
    private final DistributedLockManager distributedLockManager;

    private boolean initialized = false;
    private boolean stopped = true;
    private volatile int expiredCount = 0;

    public static ExpiredBonusTracker getInstance() {
        return instance;
    }

    private ExpiredBonusTracker() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        bonusPersister = persistenceManager.getPersister(CassandraBonusPersister.class);
        expiredBonusTrackerInfoPersister = persistenceManager.getPersister(CassandraExpiredBonusTrackerInfoPersister.class);
        distributedLockManager = persistenceManager.getPersister(DistributedLockManager.class);
    }

    public void init() {
        if (!initialized) {
            LOG.info("Init started");
            initialized = true;
            stopped = false;
            StatisticsManager.getInstance().registerStatisticsGetter("ExpiredFRBonusTracker", new IStatisticsGetter() {
                @Override
                public String getStatistics() {
                    return "Expired bonuses count: " + expiredCount;
                }
            });
            scheduler.scheduleAtFixedRate(new ExpirationTrackerTask(),
                    TimeUnit.MINUTES.toMillis(10), TimeUnit.HOURS.toMillis(10), TimeUnit.MILLISECONDS);

            LOG.info("Init completed");
        }
    }

    public void shutdown() {
        if (initialized) {
            LOG.info("Shutdown started");
            try {
                ExecutorUtils.shutdownService(this.getClass().getSimpleName(), scheduler, 2000);
            } catch (Exception e) {
                LOG.error("Cannot shutdown", e);
            }
            initialized = false;
            stopped = true;
            LOG.info("Shutdown completed");
        }
    }

    private void processCassandraItems() {
        Date now = new Date();
        Long lastProcessedDate = expiredBonusTrackerInfoPersister.getBonusLastProcessedDate();
        if (lastProcessedDate == null) { //first run
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2022);
            cal.set(Calendar.MONTH, 8);
            cal = CalendarUtils.getStartDay(cal);
            lastProcessedDate = cal.getTimeInMillis();
            expiredBonusTrackerInfoPersister.persistBonusTrackerInfo(lastProcessedDate);
        }
        LOG.info("processCassandraItems: lastProcessedDate=" + new Date(lastProcessedDate));
        Calendar currentProcessing = Calendar.getInstance();
        currentProcessing.setTimeInMillis(lastProcessedDate);
        currentProcessing = CalendarUtils.getStartDay(currentProcessing);
        currentProcessing.add(Calendar.DATE, -2); //need check with overlaping
        while (currentProcessing.getTime().before(now)) {
            LOG.info("processCassandraItems: currentProcessing=" + currentProcessing.getTime());
            if (!initialized) {
                LOG.debug("Processing was interrupted");
                return;
            }
            if (stopped) {
                LOG.debug(LogUtils.stackTrace("Processing was interrupted by "));
                return;
            }
            List<Long> bonusIds = bonusPersister.getByExpirationDate(currentProcessing.getTimeInMillis());
            for (Long bonusId : bonusIds) {
                if (!initialized) {
                    LOG.debug("Processing was interrupted");
                    return;
                }
                if (stopped) {
                    LOG.debug(LogUtils.stackTrace("Processing was interrupted by "));
                    return;
                }
                LOG.info("processCassandraItems:process bonus with id=" + bonusId);
                Bonus bonus = BonusManager.getInstance().getById(bonusId);
                if (bonus != null && bonus.isExpired()) {
                    boolean result = false;
                    try {
                        result = BonusManager.getInstance().expireBonus(bonus);
                    } catch (BonusException e) {
                        LOG.error("Cannot expire FRBonus", e);
                    }
                    if (result) {
                        expiredCount++;
                    } else {
                        LOG.debug("Failed to delete bonus with id: " + bonus.getId());
                    }
                } else {
                    LOG.warn("processCassandraItems: bonus not found, id=" + bonusId);
                }
            }
            expiredBonusTrackerInfoPersister.persistBonusTrackerInfo(currentProcessing.getTimeInMillis());
            currentProcessing.add(Calendar.DATE, 1);
        }
    }

    public class ExpirationTrackerTask implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            if (!initialized) {
                LOG.debug("ExpiredFRBonusTracker is not initialized");
                return;
            }
            LockingInfo lockInfo;
            try {
                lockInfo = distributedLockManager.tryLock(LOCK_ID);
            } catch (CommonException e) {
                LOG.warn("Lock failed, may be already processed on other GS", e);
                return;
            }
            try {
                processCassandraItems();
            } catch (Exception e) {
                LOG.error("ExpirationTrackerTask run finish with exception", e);
            } finally {
                LOG.debug("Processing ended");
                distributedLockManager.unlock(lockInfo);
                StatisticsManager.getInstance().updateRequestStatistics("ExpiredFRBonusTracker run",
                        System.currentTimeMillis() - start);
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.initialized = isInitialized;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }


}
