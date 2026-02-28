package com.abs.casino.gs.managers.payment.bonus.tracker;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.MassAwardCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BaseMassAward;
import com.dgphoenix.casino.common.cache.data.bonus.BonusMassAwardBonusTemplate;
import com.abs.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.payment.bonus.mass.MassAwardBonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExpiredMassAwardTracker {

    private final static Logger LOG = LogManager.getLogger(ExpiredMassAwardTracker.class);
    private final static String TRACKER_LOCK = "EXPIRE_MASS_AWARD_TRACKER";

    private volatile boolean initialized = true;
    private volatile int expiredCount = 0;

    private final ScheduledExecutorService scheduler;
    private final DistributedLockManager distributedLockManager;
    private final MassAwardBonusManager massAwardBonusManager;
    private ScheduledFuture<?> scheduledFuture;


    public ExpiredMassAwardTracker(ScheduledExecutorService scheduler, CassandraPersistenceManager persistenceManager, MassAwardBonusManager massAwardBonusManager) {
        this.scheduler = scheduler;
        StatisticsManager.getInstance().registerStatisticsGetter("ExpiredMassAwardTracker", () -> "Expired bonuses count: " + expiredCount);
        distributedLockManager = persistenceManager.getPersister(DistributedLockManager.class);
        this.massAwardBonusManager = massAwardBonusManager;
    }

    @PostConstruct
    public void init() {
        scheduledFuture = scheduler.scheduleAtFixedRate(this::searchAndExpireMassAwards,
                TimeUnit.MINUTES.toMillis(30), TimeUnit.HOURS.toMillis(12), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduledFuture.cancel(true);
        initialized = false;
    }

    private void searchAndExpireMassAwards() {
        try {
            LOG.debug("Start task");
            long start = System.currentTimeMillis();
            LockingInfo lock;
            try {
                lock = distributedLockManager.lock(TRACKER_LOCK, TimeUnit.SECONDS.toMillis(10));
            } catch (CommonException e) {
                LOG.warn("Can't obtain lock");
                return;
            }
            try {
                Set<Long> massAwardIds = MassAwardCache.getInstance().getAllObjects().keySet();
                LOG.info("Mass awards count = {}", massAwardIds.size());
                for (long massAwardId : massAwardIds) {
                    try {
                        BaseMassAward massBonus = MassAwardCache.getInstance().getById(massAwardId);
                        if (massBonus == null) {
                            LOG.warn("Mass award not found: {}", massAwardId);
                            continue;
                        }
                        if (massBonus.isExpired()) {
                            BonusSystemType bonusSystemType = massBonus.getTemplate() instanceof BonusMassAwardBonusTemplate
                                    ? BonusSystemType.ORDINARY_SYSTEM
                                    : BonusSystemType.FRB_SYSTEM;

                            List<Long> accounts = massBonus.getAccountIds();
                            boolean interrupted = removeMassAwardFromAccounts(massAwardId, accounts, bonusSystemType);
                            if (!interrupted) {
                                massAwardBonusManager.remove(massAwardId);
                                expiredCount++;
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error during processing mass award with id = {}", massAwardId);
                    }
                }
            } finally {
                distributedLockManager.unlock(lock);
                StatisticsManager.getInstance()
                        .updateRequestStatistics("ExpiredMassAwardTracker searchAndExpireMassAwards",
                                System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            LOG.error("Error during searchAndExpireMassAwards", e);
        }
        LOG.debug("End task");
    }

    private boolean removeMassAwardFromAccounts(long massBonusId, List<Long> accounts,
                                                BonusSystemType bonusSystemType) throws CommonException {
        boolean interrupted = false;
        for (long accountId : accounts) {
            if (!initialized) {
                LOG.debug("ExpiredMassAwardTracker is not initialized");
                interrupted = true;
                break;
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
            if (accountInfo == null) {
                continue;
            }
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
                List<Long> currentPlayerMassAwardIds = bonusSystemType == BonusSystemType.ORDINARY_SYSTEM
                        ? accountInfo.getBonusMassAwardIdsList()
                        : accountInfo.getFrbMassAwardIdsList();
                boolean changed = false;
                if (currentPlayerMassAwardIds.contains(massBonusId)) {
                    currentPlayerMassAwardIds.remove(massBonusId);
                    changed = true;
                }
                if (changed) {
                    if (bonusSystemType == BonusSystemType.ORDINARY_SYSTEM) {
                        accountInfo.setBonusMassAwardIdsList(currentPlayerMassAwardIds);
                    } else {
                        accountInfo.setFrbMassAwardIdsList(currentPlayerMassAwardIds);
                    }
                    SessionHelper.getInstance().commitTransaction();
                }
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        return interrupted;
    }
}
