package com.abs.casino.gs.managers.payment.bonus.tracker;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraBonusPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.abs.casino.common.engine.tracker.AbstractCommonTrackingTask;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;


public class BonusTrackerTask extends AbstractCommonTrackingTask<Long, BonusTracker> {
    private static final Logger LOG = Logger.getLogger(BonusTrackerTask.class);
    private final CassandraBonusPersister bonusPersister;

    public BonusTrackerTask(Long bonusId, BonusTracker tracker) {
        super(bonusId, tracker);
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        bonusPersister = persistenceManager.getPersister(CassandraBonusPersister.class);
    }

    @Override
    protected void process() throws CommonException {
        Long bonusId = getKey();
        Bonus bonus = bonusPersister.get(bonusId);
        if (bonus != null) {
            SessionHelper.getInstance().lock(bonus.getAccountId());
            try {
                SessionHelper.getInstance().openSession();
                bonus = BonusManager.getInstance().getById(bonusId);
                if (bonus != null && bonus.getStatus() != BonusStatus.RELEASED) {
                    BonusManager.getInstance().releaseBonus(bonus);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                } else {
                    LOG.debug("process, already released, bonus=" + bonus);
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } else {
            LOG.warn("run bonusId:" + bonusId + " was not found");
        }
    }

    @Override
    protected long getTaskSleepTimeout() throws CommonException {
        return GameServerConfiguration.getInstance().getBonusTrackerSleepTimeout();
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}