package com.dgphoenix.casino.sm.tracker.logout;

import com.dgphoenix.casino.common.engine.tracker.AbstractCommonTracker;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LogoutTracker extends AbstractCommonTracker<Long, LogoutTask> {
    private static final Logger LOG = Logger.getLogger(LogoutTracker.class);
    private static final LogoutTracker instance = new LogoutTracker();

    public static LogoutTracker getInstance() {
        return instance;
    }

    public void chargeTasks() {
        //tasks added from TransactionDataTracker
    }

    public boolean isRegistered(long accountId) {
        return containsKey(accountId);
    }

    @Override
    protected LogoutTask createNewTask(Long accountId) {
        return new LogoutTask(accountId, this, GameServer.getInstance().getServerId());
    }

    public void addTask(Long accountId, String extUserId, Integer bankId) {
        if (isInitialized()) {
            addNewTask(accountId, GameServer.getInstance().getServerId(), 0, extUserId, bankId);
        } else {
            getLog().warn("addTask task for key:" + accountId + " skipped, tracker is not initialized");
        }
    }

    @Override
    public void addTask(Long accountId) {
        if (isInitialized()) {
            addNewTask(accountId, GameServer.getInstance().getServerId());
        } else {
            getLog().warn("addTask task for key:" + accountId + " skipped, tracker is not initialized");
        }
    }

    protected void addNewTask(Long accountId, int gameServerId) {
        addNewTask(accountId, gameServerId, 0, null, null);
    }

    protected void addNewTask(Long accountId, int gameServerId, long delayInMillis, String extUserId, Integer bankId) {
        if (accountId == null) {
            getLog().warn("skip addNewTask task for null account");
            return;
        }

        if (containsKey(accountId)) {
            getLog().warn("addNewTask task for key:" + accountId + " already registered");
            return;
        }

        LogoutTask task = new LogoutTask(accountId, this, gameServerId, extUserId, bankId);
        if (delayInMillis <= 0) {
            keys.put(accountId, threadPool.submit(task));
        } else {
            keys.putIfAbsent(accountId, threadPool.schedule(task, delayInMillis, TimeUnit.MILLISECONDS));
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("addNewTask task for key: " + accountId + ", gameServerId=" + gameServerId + " was registered");
        }
    }

    public void addTask(Long accountId, int gameServerId) {
        if (isInitialized()) {
            addNewTask(accountId, gameServerId);
        } else {
            getLog().warn("addTask task for key:" + accountId + " skipped, tracker is not initialized");
        }
    }

    public void addTask(Long accountId, int gameServerId, long delayInMillis) {
        if (isInitialized()) {
            addNewTask(accountId, gameServerId, delayInMillis, null, null);
        } else {
            getLog().warn("addTask task for key:" + accountId + " skipped, tracker is not initialized");
        }
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    @Override
    protected ScheduledExecutorService createThreadPoolExecutor() {
        final ScheduledThreadPoolExecutor service = (ScheduledThreadPoolExecutor)
                Executors.newScheduledThreadPool(getTrackerThreadPoolSize());
        service.setMaximumPoolSize(getTrackerThreadPoolSize() + 1);
        return service;
    }

    @Override
    protected int getThreadPoolSize() throws CommonException {
        return GameServerConfiguration.getInstance().getLogoutTrackerThreadPoolSize();
    }
}
