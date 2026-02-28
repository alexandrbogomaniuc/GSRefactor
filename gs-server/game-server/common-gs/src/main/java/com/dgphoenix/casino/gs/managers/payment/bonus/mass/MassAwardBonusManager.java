package com.abs.casino.gs.managers.payment.bonus.mass;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.DistributedLockManager;
import com.abs.casino.cassandra.persist.CassandraDelayedMassAwardHistoryPersister;
import com.abs.casino.cassandra.persist.CassandraDelayedMassAwardPersister;
import com.abs.casino.cassandra.persist.CassandraMassAwardPersister;
import com.abs.casino.cassandra.persist.CassandraMassAwardRestrictionPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.MassAwardCache;
import com.dgphoenix.casino.common.cache.data.bonus.*;
import com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction;
import com.abs.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.abs.casino.gs.managers.payment.bonus.restriction.NoAwardRestriction;
import com.abs.casino.gs.managers.payment.bonus.restriction.PlayerBalanceRestriction;
import com.abs.casino.gs.persistance.remotecall.ChangeMassAwardStatusCall;
import com.abs.casino.gs.persistance.remotecall.DeleteMassAwardCall;
import com.abs.casino.gs.persistance.remotecall.RefreshConfigCall;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

public class MassAwardBonusManager implements IMassAwardBonusManager {

    private static final Logger LOG = LogManager.getLogger(MassAwardBonusManager.class);
    private static final String LOCK_ID = "MASS_AWARD_";

    private final NoAwardRestriction noAwardRestriction = new NoAwardRestriction();
    private final CassandraMassAwardPersister massAwardPersister;
    private final CassandraMassAwardRestrictionPersister massAwardRestrictionPersister;
    private final CassandraDelayedMassAwardHistoryPersister delayedMassAwardHistoryPersister;
    private final CassandraDelayedMassAwardPersister delayedMassAwardPersister;
    private LoadingCache<Long, MassAwardRestriction> restrictionCache;
    private final RemoteCallHelper remoteCallHelper;
    private final DistributedLockManager distributedLockManager;

    public MassAwardBonusManager(CassandraPersistenceManager persistenceManager, RemoteCallHelper remoteCallHelper) {
        massAwardPersister = persistenceManager.getPersister(CassandraMassAwardPersister.class);
        massAwardRestrictionPersister = persistenceManager.getPersister(CassandraMassAwardRestrictionPersister.class);
        delayedMassAwardHistoryPersister = persistenceManager.getPersister(CassandraDelayedMassAwardHistoryPersister.class);
        delayedMassAwardPersister = persistenceManager.getPersister(CassandraDelayedMassAwardPersister.class);
        distributedLockManager = persistenceManager.getPersister(DistributedLockManager.class);
        this.remoteCallHelper = remoteCallHelper;
    }

    @PostConstruct
    private void init() {
        restrictionCache = CacheBuilder.newBuilder()
                .recordStats()
                .build(new CacheLoader<Long, MassAwardRestriction>() {
                    @Override
                    public MassAwardRestriction load(@Nonnull Long massAwardId) {
                        MassAwardRestriction restriction = massAwardRestrictionPersister.get(massAwardId);
                        return restriction == null ? noAwardRestriction : restriction;
                    }
                });

        StatisticsManager.getInstance().registerStatisticsGetter("MassAwardBonusManager : restrictionCache statistics",
                () -> "size=" + restrictionCache.size() + ", stats=" + restrictionCache.stats());
    }

    public List<Long> getAwardsForBank(long bankId) {
        return MassAwardCache.getInstance().getByBankId(bankId);
    }

    @Override
    public void changeBonusStatus(long massAwardId, BonusStatus status) {
        BaseMassAward massAward = get(massAwardId);
        if (massAward != null) {
            massAward.setStatus(status);
            massAwardPersister.persist(massAwardId, massAward);
            remoteCallHelper.sendCallToAllServers(new ChangeMassAwardStatusCall(massAwardId, status));
        }
    }

    @Override
    public BaseMassAward get(long massAwardId) {
        BaseMassAward massAward = MassAwardCache.getInstance().getById(massAwardId);
        if (massAward == null) {
            massAward = massAwardPersister.get(massAwardId);
            if (massAward != null) {
                LOG.info("get: loaded massAward: {}", massAward);
                try {
                    MassAwardCache.getInstance().put(massAward);
                } catch (BonusException e) {
                    LOG.error("Cannot put massAward", e);
                }
            }
        }
        return massAward;
    }

    public void remove(long massAwardId) {
        massAwardPersister.delete(massAwardId);
        MassAwardCache.getInstance().remove(massAwardId);
        remoteCallHelper.sendCallToAllServers(new DeleteMassAwardCall(massAwardId));
    }

    protected DelayedMassAward getDelayedAward(long delayedMassAwardId) {
        DelayedMassAward delayedMassAward = delayedMassAwardHistoryPersister.get(delayedMassAwardId);
        if (delayedMassAward == null) {//try to find in active
            delayedMassAward = delayedMassAwardPersister.get(delayedMassAwardId);
        }
        return delayedMassAward;
    }

    @Override
    public void createdFRBMassAward(long id, MassAwardType type, long bankId, List<Long> gameIds, long rounds,
                                    String comment, String description, long startDate, long expirationDate,
                                    Long freeRoundValidity, List<Long> accountIds, String countryCode,
                                    Long registeredFrom, Long frbTableRoundChips, Long minimumBalance, BonusGameMode bonusGameMode,
                                    Long delayedMassAwardFrbId, Long maxWinLimit)
            throws CommonException {
        if (!BankInfoCache.getInstance().getBankInfo(bankId).isFRBConfigurationValid()) {
            String message = "MassAwardBonusManager createdFRBMassAward id=" + id
                    + ". Wrong FRBonus configuration for bankId=" + bankId;
            LOG.error(message);
            throw new CommonException(message);
        }
        FRBonusManager.getInstance().assertGameTypeValid(gameIds);
        LockingInfo lockInfo = null;
        try {
            lockInfo = distributedLockManager.lock(LOCK_ID + id);
            if (get(id) != null) {
                throw new BonusException("MassAward already exists");
            }
            FRBMassAwardBonusTemplate template = new FRBMassAwardBonusTemplate(startDate, expirationDate, description,
                    comment, gameIds, System.currentTimeMillis(), rounds,
                    freeRoundValidity == null || freeRoundValidity <= 0 ? null : freeRoundValidity, frbTableRoundChips);
            template.setRegisteredFrom(registeredFrom);
            template.setCountryCode(countryCode);
            BaseMassAward<FRBMassAwardBonusTemplate> massAward = new BaseMassAward<>(id, type, template, null);
            massAward.setBankIds(Collections.singletonList(bankId));
            massAward.setAccountIds(accountIds);
            massAward.setMaxWinLimit(maxWinLimit);

            massAwardPersister.persist(id, massAward);
            if (delayedMassAwardFrbId != null) {
                massAwardPersister.saveDelayedMassAwardId(delayedMassAwardFrbId, id);
            }
            if (minimumBalance != null && minimumBalance > 0) {
                createdPlayerBalanceRestriction(id, minimumBalance);
            }
            MassAwardCache.getInstance().put(massAward);
            remoteCallHelper.sendCallToAllServers(new RefreshConfigCall(MassAwardCache.class.getCanonicalName(), String.valueOf(id)));
            LOG.debug("createdFRBMassAward: {}", massAward);
        } catch (CommonException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CommonException("MassAwardBonusManager createdFRBMassAward id=" + id, e);
        } finally {
            if (lockInfo != null) {
                distributedLockManager.unlock(lockInfo);
            }
        }
    }

    @Override
    public void createdBonusMassAward(long id, MassAwardType type, long bankId, List<Long> gameIds,
                                      String comment, String description, long startDate, long expirationDate,
                                      List<Long> accountIds, String countryCode, Long registeredFrom,
                                      BonusType bonusType, long amount, double rolloverMultiplier,
                                      BonusGameMode bonusGameMode, long balance, boolean autoRelease,
                                      Long delayedMassAwardId, Double maxWinMultiplier) throws CommonException {
        if (!BankInfoCache.getInstance().getBankInfo(bankId).isBonusConfigurationValid()) {
            String message = "MassAwardBonusManager createdBonusMassAward id=" + id
                    + ". Wrong bonus configuration for bankId=" + bankId;
            LOG.error(message);
            throw new CommonException(message);
        }
        LockingInfo lockInfo = null;
        try {
            lockInfo = distributedLockManager.lock(LOCK_ID + id);
            if (get(id) != null) {
                throw new BonusException("MassAward already exists");
            }
            BonusMassAwardBonusTemplate template = new BonusMassAwardBonusTemplate(
                    startDate, expirationDate, description, comment,
                    gameIds, System.currentTimeMillis(), bonusType, amount,
                    rolloverMultiplier, bonusGameMode, balance, autoRelease
            );
            template.setRegisteredFrom(registeredFrom);
            template.setCountryCode(countryCode);
            BaseMassAward<BonusMassAwardBonusTemplate> massAward = new BaseMassAward<>(id, type, template, maxWinMultiplier);
            massAward.setBankIds(Collections.singletonList(bankId));
            massAward.setAccountIds(accountIds);

            massAwardPersister.persist(id, massAward);
            if (delayedMassAwardId != null) {
                massAwardPersister.saveDelayedMassAwardId(delayedMassAwardId, id);
            }
            MassAwardCache.getInstance().put(massAward);
            remoteCallHelper.sendCallToAllServers(new RefreshConfigCall(MassAwardCache.class.getCanonicalName(), String.valueOf(id)));
        } catch (CommonException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CommonException("MassAwardBonusManager createdBonusMassAward id=" + id, e);
        } finally {
            if (lockInfo != null) {
                distributedLockManager.unlock(lockInfo);
            }
        }
    }

    public void createdPlayerBalanceRestriction(long massAwardId, Long minimumBalance) {
        MassAwardRestriction restriction = new PlayerBalanceRestriction(massAwardId, minimumBalance);
        massAwardRestrictionPersister.persist(restriction);
    }

    public MassAwardRestriction getMassAwardRestriction(long massAwardId) {
        return restrictionCache.getUnchecked(massAwardId);
    }

    public void invalidateRestrictionCache() {
        restrictionCache.invalidateAll();
    }

    @Override
    public Long getMassAwardIdByDelayedMassAwardId(long delayedMassAwardFrbId) {
        return massAwardPersister.getMassAwardIdByDelayedMassAwardId(delayedMassAwardFrbId);
    }
}
