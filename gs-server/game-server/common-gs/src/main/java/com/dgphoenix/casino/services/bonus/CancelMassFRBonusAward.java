package com.abs.casino.services.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.abs.casino.cassandra.persist.CassandraDelayedMassAwardPersister;
import com.abs.casino.cassandra.persist.CassandraFrBonusPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.MassAwardCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.*;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.abs.casino.gs.managers.payment.bonus.mass.MassAwardBonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:svvedenskiy@dgphoenix.com">Stepan Vvedenskiy</a>
 * @since 16.07.2018
 */
public class CancelMassFRBonusAward {
    private static final Logger LOG = LogManager.getLogger(CancelMassFRBonusAward.class);
    private CassandraAccountInfoPersister accountInfoPersister;
    private CassandraFrBonusPersister frBonusPersister;
    private CassandraDelayedMassAwardPersister delayedMassAwardPersister;
    private final MassAwardBonusManager massAwardBonusManager;

    public CancelMassFRBonusAward() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        accountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
        frBonusPersister = persistenceManager.getPersister(CassandraFrBonusPersister.class);
        delayedMassAwardPersister = persistenceManager.getPersister(CassandraDelayedMassAwardPersister.class);
        massAwardBonusManager = ApplicationContextHelper.getBean(MassAwardBonusManager.class);
    }

    public CancelResult cancelMassAward(long massAwardId, boolean cancelCreatedFRBonus) {
        CancelResult result = new CancelResult();
        result.setDelayedMassAwardRemoved(removeDelayedMassAward(massAwardId));
        BaseMassAward massAward = MassAwardCache.getInstance().getById(massAwardId);
        if (massAward == null || massAward.getStatus() != BonusStatus.ACTIVE
                || !(massAward.getTemplate() instanceof FRBMassAwardBonusTemplate)) {
            return result;
        }
        result.setBaseMassAwardCanceled(cancelBaseMassAward(massAward));
        if (cancelCreatedFRBonus) {
            int numberFRBCanceled = 0;
            List<Long> accountIds = massAward.getAccountIds();
            if (accountIds != null && !accountIds.isEmpty()) {
                numberFRBCanceled = cancelFrBonusesForAccounts(massAward, accountIds);
            } else {
                if (massAward.getType() == MassAwardType.NEWPLAYERS) {
                    numberFRBCanceled = cancelMassAwardForNewPlayers(massAward);
                }
            }
            result.setNumberFRBonusCanceled(numberFRBCanceled);
        }
        return result;
    }

    private int cancelMassAwardForNewPlayers(BaseMassAward massAward) {
        List<Long> allAccounts = new ArrayList<>();
        List<Long> bankIds = massAward.getBankIds();
        for (long bankId : bankIds) {
            allAccounts.addAll(accountInfoPersister.getAccountIds(bankId));
        }
        return cancelFrBonusesForAccounts(massAward, allAccounts);
    }

    private int cancelFrBonusesForAccounts(BaseMassAward massAward, List<Long> accountIds) {
        long t0 = System.currentTimeMillis();
        LOG.debug("cancelFrBonusesForAccounts(massAwardId: {}, accountIds.size: {})",
                massAward.getId(), String.valueOf(accountIds.size()));
        Map<Long, AccountInfo> accountsMap = accountInfoPersister.getByIds(accountIds);
        boolean changed;
        int count = 0;
        for (AccountInfo accountInfo : accountsMap.values()) {
            if (massAward.getType() == MassAwardType.NEWPLAYERS) {
                if (accountInfo.getRegisterTime() < massAward.getTemplate().getStartDate() ||
                        accountInfo.getRegisterTime() > massAward.getTemplate().getExpirationDate()) {
                    continue;
                }
            }
            try {
                SessionHelper.getInstance().lock(accountInfo.getId());
            } catch (CommonException e) {
                LOG.error(e);
                continue;
            }
            try {
                changed = false;
                SessionHelper.getInstance().openSession();
                accountInfo = AccountManager.getInstance().getAccountInfo(accountInfo.getId());
                List<Long> currentPlayerMassAwardIds = accountInfo.getFrbMassAwardIdsList();
                if (currentPlayerMassAwardIds.remove(massAward.getId())) {
                    accountInfo.setFrbMassAwardIdsList(currentPlayerMassAwardIds);
                    changed = true;
                }
                List<FRBonus> frBonuses = frBonusPersister.getActiveBonuses(accountInfo.getId());
                for (FRBonus frBonus : frBonuses) {
                    if (frBonus.getMassAwardId() != massAward.getId()) {
                        continue;
                    }
                    FRBonusManager.getInstance().cancelBonus(frBonus);
                    changed = true;
                    count++;
                    break;
                }
                if (changed) {
                    SessionHelper.getInstance().commitTransaction();
                }
                SessionHelper.getInstance().markTransactionCompleted();
            } catch (Throwable t) {
                LOG.error(t);
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        LOG.debug("cancelFrBonusesForAccounts finished. FRB canceled: {}, time: {} ms", count, System.currentTimeMillis() - t0);
        return count;
    }

    private boolean cancelBaseMassAward(BaseMassAward massAward) {
        try {
            massAward.setStatus(BonusStatus.CANCELLED);
            massAwardBonusManager.remove(massAward.getId());
            return true;
        } catch (Throwable e) {
            LOG.error("Error while deleting Mass Bonus Award", e);
        }
        return false;
    }

    private boolean removeDelayedMassAward(long delayedMassAwardId) {
        try {
            delayedMassAwardPersister.delete(delayedMassAwardId);
            return true;
        } catch (Throwable e) {
            LOG.error("Error while deleting delayed Mass Award FRB", e);
        }
        return false;
    }

    public class CancelResult {
        private boolean delayedMassAwardRemoved = false;
        private boolean baseMassAwardCanceled = false;
        private int numberFRBonusCanceled = 0;

        public int getNumberFRBonusCanceled() {
            return numberFRBonusCanceled;
        }

        public void setNumberFRBonusCanceled(int numberFRBonusCanceled) {
            this.numberFRBonusCanceled = numberFRBonusCanceled;
        }

        public boolean isBaseMassAwardCanceled() {
            return baseMassAwardCanceled;
        }

        public void setBaseMassAwardCanceled(boolean baseMassAwardCanceled) {
            this.baseMassAwardCanceled = baseMassAwardCanceled;
        }

        public boolean isDelayedMassAwardRemoved() {
            return delayedMassAwardRemoved;
        }

        public void setDelayedMassAwardRemoved(boolean delayedMassAwardRemoved) {
            this.delayedMassAwardRemoved = delayedMassAwardRemoved;
        }
    }
}
