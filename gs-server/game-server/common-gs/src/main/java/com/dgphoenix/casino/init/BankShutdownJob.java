package com.abs.casino.init;

import com.dgphoenix.casino.cassandra.AccountDistributedLockManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.sm.IPlayerSessionManager;
import com.abs.casino.sm.PlayerSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class BankShutdownJob implements Job {

    private static final Logger LOG = LogManager.getLogger(BankShutdownJob.class);
    private static final String LOCK_NAME = "cronShutdown%d";

    private long bankId;
    private AccountDistributedLockManager lockManager;
    private CassandraTransactionDataPersister transactionDataPersister;
    private RemoteCallHelper remoteCallHelper;

    public void init(JobExecutionContext context) {
        bankId = (long) context.getMergedJobDataMap().get("bankId");
        ApplicationContext appContext = (ApplicationContext) context.getMergedJobDataMap().get("appContext");

        CassandraPersistenceManager persistenceManager = appContext.getBean(CassandraPersistenceManager.class);
        lockManager = persistenceManager.getPersister(AccountDistributedLockManager.class);
        transactionDataPersister = persistenceManager.getPersister(CassandraTransactionDataPersister.class);
        remoteCallHelper = appContext.getBean(RemoteCallHelper.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        init(context); // init fields
        Thread.currentThread().setName("bankId: " + bankId);
        LOG.info("Started");

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            LOG.error("Bank not found: " + bankId);
            return;
        }

        IPlayerSessionManager sessionManager;
        try {
            sessionManager = PlayerSessionFactory.getInstance().getPlayerSessionManager(bankInfo.getId());
        } catch (CommonException e) {
            LOG.error("Error retrieving player session manager", e);
            return;
        }

        LockingInfo lock = null;
        try {
            lock = lockManager.tryLock(getLockName(bankInfo));
        } catch (CommonException e) {
            LOG.error("Error retrieving lock", e);
        }

        if (lock != null) {
            try {
                setBankEnabled(bankInfo, false);
                Thread.sleep(1000);
                logoutUsersAndSleep(bankInfo, sessionManager, TimeUnit.MINUTES.toMillis(10));
                logoutUsersAndSleep(bankInfo, sessionManager, TimeUnit.MINUTES.toMillis(5));
                setBankEnabled(bankInfo, true);
            } catch (CommonException e) {
                LOG.error("", e);
            } catch (InterruptedException e) {
                LOG.error("Interrupted");
            } finally {
                lockManager.unlock(lock);
            }
        } else {
            long jobTime = TimeUnit.MINUTES.toMillis(15);
            long startTstamp = System.currentTimeMillis();

            try {
                lock = lockManager.lock(getLockName(bankInfo), jobTime);
            } catch (CommonException e) {
                LOG.debug("Task ended");
            }

            if (lock != null) {
                LOG.debug("Resuming task");
                try {
                    // job time left
                    long timeLeft = jobTime - (System.currentTimeMillis() - startTstamp);
                    if (timeLeft > TimeUnit.MINUTES.toMillis(10)) {// check bank disabled
                        LOG.debug("Time left: {} minutes", TimeUnit.MILLISECONDS.toMinutes(timeLeft));
                        bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                        if (bankInfo.isEnabled()) {
                            setBankEnabled(bankInfo, false);
                        }
                        logoutUsersAndSleep(bankInfo, sessionManager, TimeUnit.MINUTES.toMillis(10));
                        timeLeft = jobTime - (System.currentTimeMillis() - startTstamp);
                    }
                    if (timeLeft > TimeUnit.MINUTES.toMillis(2)) {
                        LOG.debug("Time left: {} minutes", TimeUnit.MILLISECONDS.toMinutes(timeLeft));
                        logoutUsersAndSleep(bankInfo, sessionManager, timeLeft - TimeUnit.MINUTES.toMillis(2));
                        timeLeft = jobTime - (System.currentTimeMillis() - startTstamp);
                    }
                    if (timeLeft <= TimeUnit.MINUTES.toMillis(3)) {
                        if (timeLeft < 0) {
                            timeLeft = 0;
                        }
                        LOG.debug("Time left: {} minutes", TimeUnit.MILLISECONDS.toMinutes(timeLeft));
                        Thread.sleep(timeLeft);
                        bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                        if (!bankInfo.isEnabled()) {
                            setBankEnabled(bankInfo, true);
                        }
                    }
                } catch (CommonException e) {
                    LOG.error("", e);
                } catch (InterruptedException e) {
                    LOG.error("Interrupted");
                } finally {
                    lockManager.unlock(lock);
                }
            }
        }

        List<Long> onlineUsers = getOnlineUsers(bankInfo);
        if (!onlineUsers.isEmpty()) {
            String loggedAccounts = onlineUsers.stream()
                    .map(String::valueOf)
                    .collect(joining(", "));
            LOG.warn("{} users left logged in: [{}]", onlineUsers.size(), loggedAccounts);
        }

        LOG.info("Ended");
    }

    private List<Long> getOnlineUsers(BankInfo bankInfo) {
        return transactionDataPersister.getOnlineSessionInfos(null, (int) bankInfo.getId(), false).stream()
                .map(osi -> osi.getAccount().getId())
                .collect(toList());
    }

    private String getLockName(BankInfo bankInfo) {
        return String.format(LOCK_NAME, bankInfo.getId());
    }

    private void setBankEnabled(BankInfo bankInfo, boolean enabled) throws CommonException {
        LOG.debug("setBankEnabled - " + enabled);
        bankInfo.setEnabled(enabled);
        // invalidate caches
        remoteCallHelper.saveAndSendNotification(bankInfo);
        LOG.debug("Bank state changed");
    }

    private void logoutUsersAndSleep(BankInfo bankInfo, IPlayerSessionManager sessionManager, long timeout) throws InterruptedException {
        LOG.debug("Checking online users");
        List<Long> onlineUsers = getOnlineUsers(bankInfo);
        if (onlineUsers.isEmpty()) {
            LOG.debug("No online users");
            Thread.sleep(timeout);
            return;
        }

        LOG.debug("Online users count: " + onlineUsers.size());
        ThreadPoolExecutor executorService = null;
        try {
            executorService = new ThreadPoolExecutor(0, 73, 30L, TimeUnit.SECONDS, new SynchronousQueue<>());
            onlineUsers.stream()
                    .map(e -> new UserLogoutTask(e, sessionManager))
                    .forEach(executorService::execute);
        } finally {
            long startTstmp = System.currentTimeMillis();
            ExecutorUtils.shutdownService(getClass().getSimpleName() + ".executor", executorService,
                    timeout);
            long timeLeft = timeout - (System.currentTimeMillis() - startTstmp);
            if (timeLeft > 0) {
                Thread.sleep(timeLeft); // wait till timeout
            }
        }
    }

    private class UserLogoutTask implements Runnable {

        private final long accountId;
        private final IPlayerSessionManager sessionManager;
        private final SessionHelper sessionHelper;

        public UserLogoutTask(long accountId, IPlayerSessionManager sessionManager) {
            this.accountId = accountId;
            this.sessionManager = sessionManager;
            sessionHelper = SessionHelper.getInstance();
        }

        @Override
        public void run() {
            LOG.debug("Logout user " + accountId);
            if (logout()) {
                LOG.debug("Logout user {}: OK", accountId);
            } else {
                LOG.debug("Logout user {}: logout failed", accountId);
            }
        }

        private boolean logout() {
            try {
                sessionHelper.lock(accountId);
            } catch (CommonException e) {
                LOG.error("", e);
                return false;
            }

            try {
                sessionHelper.openSession();
                SessionInfo sessionInfo = sessionHelper.getTransactionData().getPlayerSession();
                if (sessionInfo != null) {
                    sessionManager.logout(sessionInfo.getSessionId());
                    sessionHelper.commitTransaction();
                } else {
                    LOG.debug("Account Session is null: " + accountId);
                }
                sessionHelper.markTransactionCompleted();
            } catch (CommonException e) {
                LOG.error("", e);
                return false;
            } finally {
                sessionHelper.clearWithUnlock();
            }
            return true;
        }
    }
}