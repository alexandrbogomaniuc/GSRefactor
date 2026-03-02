package com.abs.casino.gs.managers.payment.wallet.tracker;

import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.abs.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.*;

/**
 * User: plastical
 * Date: 10.03.2010
 */
public class WalletTracker extends AbstractCommonTracker<Long, WalletTrackerTask> {
    private static final Logger LOG = Logger.getLogger(WalletTracker.class);
    private static final WalletTracker instance = new WalletTracker();
    private final Map<Long, Future<?>> highPriorityKeys;
    private final ThreadPoolExecutor highPriorityThreadPool;


    private WalletTracker() {
        super();
        this.highPriorityKeys = new ConcurrentHashMap<>(64);
        this.highPriorityThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(30);
    }

    public static WalletTracker getInstance() {
        return instance;
    }

    @Override
    public void shutdown() {
        if (isInitialized()) {
            highPriorityThreadPool.shutdown();
            try {
                highPriorityThreadPool.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                getLog().warn("shutdown interrupted");
            }
            highPriorityThreadPool.shutdownNow();
            super.shutdown();
        }
    }

    @Override
    protected void chargeTasks() {
        //tasks added from TransactionDataTracker
    }

    public void addHighPriorityTask(Long accountId) {
        if (isInitialized()) {
            if (highPriorityKeys.containsKey(accountId)) {
                getLog().warn("addHighPriorityTask task for accountId:" + accountId + " already registered");
                return;
            }
            WalletTrackerTask task = new WalletTrackerTask(accountId, this, true);
            highPriorityKeys.put(accountId, highPriorityThreadPool.submit(task));
            getLog().debug("addHighPriorityTask task for key: " + accountId + " was registered");
        } else {
            getLog().warn("addHighPriorityTask task for accountId:" + accountId +
                    " skipped, tracker is not initialized");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void removeHighPriorityKey(Long accountId) {
        highPriorityKeys.remove(accountId);
    }

    @Override
    protected WalletTrackerTask createNewTask(Long accountId) {
        return new WalletTrackerTask(accountId, this);
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    @Override
    protected int getThreadPoolSize() {
        return GameServerConfiguration.getInstance().getWalletTrackerThreadPoolSize();
    }
}
