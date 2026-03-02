package com.abs.casino.gs.managers.payment.transfer.tracker;

import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;

public class PaymentTransactionTracker extends AbstractCommonTracker<Long, PaymentTransactionTrackerTask> {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentTransactionTracker.class);
    private static PaymentTransactionTracker instance = new PaymentTransactionTracker();

    public static PaymentTransactionTracker getInstance() {
        return instance;
    }

    @Override
    protected void chargeTasks() {
        //tasks added from TransactionDataTracker
    }

    @Override
    protected PaymentTransactionTrackerTask createNewTask(Long accountId) {
        return new PaymentTransactionTrackerTask(accountId, this);
    }

    @Override
    protected int getThreadPoolSize() throws CommonException {
        return GameServerConfiguration.getInstance().getWalletTrackerThreadPoolSize();
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }
}
