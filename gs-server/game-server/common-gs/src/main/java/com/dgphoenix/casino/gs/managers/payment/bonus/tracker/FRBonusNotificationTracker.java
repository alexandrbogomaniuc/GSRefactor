package com.abs.casino.gs.managers.payment.bonus.tracker;


import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;

public class FRBonusNotificationTracker extends AbstractCommonTracker<Long, FRBonusNotificationTrackerTask> {
    private static final Logger LOG = Logger.getLogger(FRBonusNotificationTracker.class);
    private static final FRBonusNotificationTracker instance = new FRBonusNotificationTracker();

    public static FRBonusNotificationTracker getInstance() {
        return instance;
    }

    @Override
    protected void chargeTasks() {
        //tasks added from TransactionDataTracker
    }

    @Override
    protected FRBonusNotificationTrackerTask createNewTask(Long accountId) {
        return new FRBonusNotificationTrackerTask(accountId, this);
    }

    @Override
    protected int getThreadPoolSize() throws CommonException {
        return GameServerConfiguration.getInstance().getFrbonusWinTrackerThreadPoolSize();
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }
}