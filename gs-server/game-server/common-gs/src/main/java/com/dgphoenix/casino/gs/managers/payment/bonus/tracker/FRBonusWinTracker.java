package com.abs.casino.gs.managers.payment.bonus.tracker;


import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;

public class FRBonusWinTracker extends AbstractCommonTracker<Long, FRBonusWinTrackerTask> {
    private static final Logger LOG = Logger.getLogger(FRBonusWinTracker.class);
    private static final FRBonusWinTracker instance = new FRBonusWinTracker();

    public static FRBonusWinTracker getInstance() {
        return instance;
    }

    @Override
    protected void chargeTasks() {
        //tasks added from TransactionDataTracker
    }

    @Override
    protected FRBonusWinTrackerTask createNewTask(Long accountId) {
        return new FRBonusWinTrackerTask(accountId, this);
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