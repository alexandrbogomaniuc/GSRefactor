package com.dgphoenix.casino.gs;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.DistributedLockManager;
import com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.ObjectNotFoundException;
import com.dgphoenix.casino.common.lock.LockingInfo;
import com.dgphoenix.casino.common.lock.ServerLockInfo;
import com.dgphoenix.casino.common.transactiondata.*;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.ExecutorUtils;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTracker;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTrackerTask;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusWinTracker;
import com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusWinTrackerTask;
import com.dgphoenix.casino.gs.managers.payment.transfer.tracker.PaymentTransactionTracker;
import com.dgphoenix.casino.gs.managers.payment.transfer.tracker.PaymentTransactionTrackerTask;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTrackerTask;
import com.dgphoenix.casino.gs.status.ServersStatusWatcher;
import com.dgphoenix.casino.sm.tracker.logout.LogoutTask;
import com.dgphoenix.casino.sm.tracker.logout.LogoutTracker;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: flsh
 * Date: 20.02.15.
 */
public class TransactionDataTracker {
    public final static long CHECK_TRANSACTIONS_INTERVAL_IN_SECONDS = TimeUnit.MINUTES.toSeconds(5);
    public static final long DEFAULT_SESSION_TIMEOUT = TimeUnit.MINUTES.toMillis(10);
    //need small delay for preventing "Cannot lock by id" exception in *TrackerTask
    public final static long START_TASK_PAUSE_IN_MSEC = 100;
    private static final Logger LOG = LogManager.getLogger(TransactionDataTracker.class);
    private static TransactionDataTracker instance = new TransactionDataTracker();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(100);
    private final Map<Integer, Pair<ProcessOnlineTransactionsTask, ProcessOfflineTransactionsTask>> deadServersMap =
            new HashMap<>();
    private boolean initialized = false;
    private final CassandraTransactionDataPersister transactionDataPersister;

    private TransactionDataTracker() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        transactionDataPersister = persistenceManager.getPersister(CassandraTransactionDataPersister.class);
    }

    public static TransactionDataTracker getInstance() {
        return instance;
    }

    public static long getDefaultSessionTimeout() {
        long sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        try {
            sessionTimeout = GameServerConfiguration.getInstance().getServerSessionTimeout();
        } catch (Exception e) {
            LOG.error("static getSessionTimeout error:", e);
        }
        return sessionTimeout;
    }

    public static Integer getBankId(String sessionId) {
        try {
            Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            return pair.getKey();
        } catch (Throwable t) {
            return null;
        }
    }

    public static long getSessionTimeOut(SessionInfo info, AccountInfo account, GameSession gameSession) {
        Integer bankId = null;
        if (account != null) {
            bankId = account.getBankId();
        } else if (gameSession != null) {
            bankId = (int) gameSession.getBankId();
        } else if (info != null) {
            bankId = getBankId(info.getSessionId());
        }
        BankInfo bankInfo = bankId == null ? null : BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            return getSessionTimeout();
        }
        Long timeout = (account == null || //temporary hack
                !account.isGuest()) && (gameSession == null || gameSession.isRealMoney()) ?
                bankInfo.getRealModeSessionTimeout() : bankInfo.getFreeModeSessionTimeout();
        return timeout == null ? getSessionTimeout() : timeout;
    }

    public static long getSessionTimeout() {
        long sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        try {
            sessionTimeout = GameServerConfiguration.getInstance().getServerSessionTimeout();
        } catch (Exception e) {
            LOG.error("static getSessionTimeout error:", e);
        }
        return sessionTimeout;
    }

    public void init() {
        if (!initialized) {
            LOG.info("init started");
            final int serverId = GameServer.getInstance().getServerId();
            {
                ProcessOnlineTransactionsTask onlineTask = new ProcessOnlineTransactionsTask(serverId);
                executor.scheduleAtFixedRate(onlineTask, 10, onlineTask.getCheckInterval(), TimeUnit.SECONDS);
                ProcessOfflineTransactionsTask offlineTask = new ProcessOfflineTransactionsTask(serverId);
                executor.scheduleAtFixedRate(offlineTask, 10, offlineTask.getCheckInterval(), TimeUnit.SECONDS);
            }
            ServersStatusWatcher.getInstance().addServerStatusListener(new IGameServerStatusListener() {
                @Override
                public void notify(int gameServerId, boolean online) {
                    if (gameServerId == serverId) {
                        return;
                    }
                    LOG.info("notify: gs=" + gameServerId + ", online=" + online);
                    synchronized (deadServersMap) {
                        if (!online) {
                            Pair<ProcessOnlineTransactionsTask, ProcessOfflineTransactionsTask> pair = deadServersMap.
                                    get(gameServerId);
                            if (pair != null) {
                                ProcessOnlineTransactionsTask onTask = pair.getKey();
                                ProcessOfflineTransactionsTask offTask = pair.getValue();
                                onTask.setStopExecution(false);
                                offTask.setStopExecution(false);
                                if (!onTask.isRunning()) {
                                    LOG.warn("OnlineTrackingTask added for gs: " + gameServerId +
                                            ", and not running, schedule");
                                    executor.schedule(onTask, onTask.getCheckInterval(), TimeUnit.SECONDS);
                                } else {
                                    LOG.warn("OnlineTrackingTask added for gs: " + gameServerId +
                                            ", and running, this may be multiple server down bug");
                                }
                                if (!offTask.isRunning()) {
                                    LOG.warn("OfflineTrackingTask added for gs: " + gameServerId +
                                            ", and not running, schedule");
                                    executor.schedule(offTask, offTask.getCheckInterval(), TimeUnit.SECONDS);
                                } else {
                                    LOG.warn("OfflineTrackingTask added for gs: " + gameServerId +
                                            ", and running, this may be multiple server down bug");
                                }
                            } else {
                                ProcessOnlineTransactionsTask onTask =
                                        new ProcessOnlineTransactionsTask(gameServerId);
                                ProcessOfflineTransactionsTask offTask =
                                        new ProcessOfflineTransactionsTask(gameServerId);
                                onTask.setStopExecution(false);
                                offTask.setStopExecution(false);
                                pair = new Pair<>(onTask, offTask);
                                deadServersMap.put(gameServerId, pair);
                                executor.schedule(onTask, onTask.getCheckInterval(), TimeUnit.SECONDS);
                                executor.schedule(offTask, offTask.getCheckInterval(), TimeUnit.SECONDS);
                            }
                        } else {
                            Pair<ProcessOnlineTransactionsTask, ProcessOfflineTransactionsTask> pair =
                                    deadServersMap.get(gameServerId);
                            if (pair != null) {
                                ProcessOnlineTransactionsTask onTask = pair.getKey();
                                ProcessOfflineTransactionsTask offTask = pair.getValue();
                                onTask.setStopExecution(true);
                                offTask.setStopExecution(true);
                                deadServersMap.remove(gameServerId);
                                executor.remove(onTask);
                                executor.remove(offTask);
                            }
                        }
                    }
                }
            });
            initialized = true;
            LOG.info("init completed");
        } else {
            LOG.warn("already initialized");
        }
    }

    public void shutdown() {
        if (initialized) {
            LOG.info("shutdown started");
            initialized = false;
            ExecutorUtils.shutdownService("TransactionDataTracker", executor, 5000);
            LOG.info("shutdown completed");
        } else {
            LOG.warn("already shutdown");
        }
    }

    private abstract class ProcessTransactionsTask implements Runnable, ITransactionDataProcessor {
        private final int gameServerId;
        private volatile boolean stopExecution;
        private volatile boolean running;
        private final DistributedLockManager distributedLockManager;

        public ProcessTransactionsTask(int gameServerId) {
            assert gameServerId > 0 : ("gameServerId has illegal value=" + gameServerId);
            this.gameServerId = gameServerId;
            LOG.debug("ProcessTransactionsTask init: trackingStatus=" + getTrackingStatus() +
                    ", gameServerId=" + gameServerId);
            CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            distributedLockManager = persistenceManager.getPersister(DistributedLockManager.class);
        }

        @Override
        public void run() {
            if (!initialized || stopExecution) {
                LOG.debug("ProcessTransactionsTask: " + getTrackingStatus() + ": stopped. initialized=" + initialized +
                        "; stopExecution=" + stopExecution + ", gameServerId=" + gameServerId);
                return;
            }
            String lockName = getLockName(gameServerId);
            LOG.debug("ProcessTransactionsTask: start trackingStatus=" + getTrackingStatus() +
                    ", gameServerId=" + gameServerId);
            running = true;
            LockingInfo lock = null;
            try {
                try {
                    lock = distributedLockManager.tryLock(lockName);
                } catch (CommonException e) {
                    LOG.warn(this.getClass().getSimpleName() + ": For GS: " + gameServerId + " cannot tryLock key: " +
                            lockName + ": " + e + ", stop processing");
                    return;
                }
                transactionDataPersister.processTransactions(getTrackingStatus(),
                        gameServerId, this);
            } catch (Throwable e) {
                LOG.error("ProcessTransactionsTask: failed trackingStatus=" + getTrackingStatus() +
                        ", gameServerId=" + gameServerId, e);
            } finally {
                if (lock != null) {
                    distributedLockManager.unlock(lock);
                }
                if (gameServerId != GameServer.getInstance().getServerId()) {
                    ServerInfo serverInfo = LoadBalancerCache.getInstance().getServerInfoById(gameServerId);
                    //reschedule execution if server offline
                    if (serverInfo != null && !serverInfo.isServerOnline() && !stopExecution) {
                        LOG.debug(this.getClass().getSimpleName() + ": reschedule execution for gs=" + gameServerId);
                        executor.schedule(this, getCheckInterval(), TimeUnit.SECONDS);
                    }
                }
                running = false;
                LOG.debug("ProcessTransactionsTask: end trackingStatus=" + getTrackingStatus() +
                        ", gameServerId=" + gameServerId);
            }
        }

        abstract TrackingStatus getTrackingStatus();
        abstract String getLockName(int gameServerId);
        abstract long getCheckInterval();

        @Override
        public boolean isStopProcessing() {
            return !initialized || stopExecution;
        }

        public void setStopExecution(boolean stopExecution) {
            LOG.debug("setStopExecution: gsId=" + getGameServerId() + ", stopExecution=" + stopExecution +
                    ", trackingStatus=" + getTrackingStatus());
            this.stopExecution = stopExecution;
        }

        public boolean isRunning() {
            return running;
        }

        public int getGameServerId() {
            return gameServerId;
        }

        protected void processWallet(ServerLockInfo lock, long accountId) throws ObjectNotFoundException {
            if (!SessionHelper.getInstance().isSessionOpen()) {
                SessionHelper.getInstance().openSession();
            }
            WalletTrackerTask walletTask = new WalletTrackerTask(accountId, WalletTracker.getInstance());
            try {
                try {
                    walletTask.process();
                    SessionHelper.getInstance().commitTransaction();
                } catch (CommonException e) {
                    clearTransactionData(lock);
                    SessionHelper.getInstance().openSession();
                    walletTask.handleCommonException(false, false, e);
                    SessionHelper.getInstance().commitTransaction();
                }
            } catch (Throwable t) {
                LOG.error(getTrackingStatus() + ": Error on process wallet for account=" + accountId, t);
                clearTransactionData(lock);
            }
        }

        protected void processPaymentTransaction(ServerLockInfo lock, long accountId) throws ObjectNotFoundException {
            if (!SessionHelper.getInstance().isSessionOpen()) {
                SessionHelper.getInstance().openSession();
            }
            PaymentTransactionTrackerTask task = new PaymentTransactionTrackerTask(accountId,
                    PaymentTransactionTracker.getInstance());
            try {
                try {
                    task.process();
                    SessionHelper.getInstance().commitTransaction();
                } catch (CommonException e) {
                    clearTransactionData(lock);
                    SessionHelper.getInstance().openSession();
                    task.handleCommonException(false, false, e);
                    SessionHelper.getInstance().commitTransaction();
                }
            } catch (Throwable t) {
                LOG.error(getTrackingStatus() + ": Error on process payment transaction for account=" +
                        accountId, t);
                clearTransactionData(lock);
            }
        }

        protected void processFrbWin(ServerLockInfo lock, long accountId) throws ObjectNotFoundException {
            if (!SessionHelper.getInstance().isSessionOpen()) {
                SessionHelper.getInstance().openSession();
            }
            FRBonusWinTrackerTask frbTask = new FRBonusWinTrackerTask(accountId, FRBonusWinTracker.getInstance());
            try {
                try {
                    frbTask.process();
                    SessionHelper.getInstance().commitTransaction();
                } catch (CommonException e) {
                    clearTransactionData(lock);
                    SessionHelper.getInstance().openSession();
                    frbTask.handleCommonException(false, false, e);
                    SessionHelper.getInstance().commitTransaction();
                }
            } catch (Throwable t) {
                LOG.error(getTrackingStatus() + ": Error on process frbWin for account=" + accountId, t);
                clearTransactionData(lock);
            }
        }

        protected void processFrbNotification(ServerLockInfo lock, long accountId) throws ObjectNotFoundException {
            if (!SessionHelper.getInstance().isSessionOpen()) {
                SessionHelper.getInstance().openSession();
            }
            FRBonusNotificationTrackerTask notificationTask =
                    new FRBonusNotificationTrackerTask(accountId, FRBonusNotificationTracker.getInstance());
            try {
                try {
                    notificationTask.process();
                    SessionHelper.getInstance().commitTransaction();
                } catch (CommonException e) {
                    clearTransactionData(lock);
                    SessionHelper.getInstance().openSession();
                    notificationTask.handleCommonException(false, false, e);
                    SessionHelper.getInstance().commitTransaction();
                }
            } catch (Throwable t) {
                LOG.error(getTrackingStatus() + ": Error on process frbNotification for account=" + accountId, t);
                clearTransactionData(lock);
            }
        }

        protected void processLogout(ServerLockInfo lock, long accountId, int gameServerId,
                                     AccountInfo accountInfo)
                throws ObjectNotFoundException {
            if (!SessionHelper.getInstance().isSessionOpen()) {
                SessionHelper.getInstance().openSession();
            }
            ITransactionData trData = SessionHelper.getInstance().getTransactionData();
            SessionInfo sessionInfo = trData.getPlayerSession();
            if (sessionInfo == null) {
                LOG.error("processLogout: cannot logout, session is null, trData=" + trData);
                return;
            }
            long sessionTimeOut = TransactionDataTracker.getSessionTimeOut(sessionInfo, accountInfo,
                    trData.getGameSession());
            if (sessionInfo.getLastActivityTime() + sessionTimeOut >= System.currentTimeMillis()) {
                LOG.debug("processLogout: stop logout, session is not expired: sessionInfo.id=" +
                        sessionInfo.getSessionId() + ", lockId=" + trData.getLockId());
                return;
            }
            LogoutTask logoutTask = new LogoutTask(accountId, LogoutTracker.getInstance(), gameServerId,
                    accountInfo == null ? null : accountInfo.getExternalId(),
                    accountInfo == null ? null : accountInfo.getBankId());
            try {
                try {
                    logoutTask.process();
                    SessionHelper.getInstance().commitTransaction();
                } catch (CommonException e) {
                    clearTransactionData(lock);
                    SessionHelper.getInstance().openSession();
                    logoutTask.handleCommonException(false, false, e);
                    SessionHelper.getInstance().commitTransaction();
                }
            } catch (Throwable t) {
                LOG.error(getTrackingStatus() + ": Error on process logout for account=" + accountId, t);
                clearTransactionData(lock);
            }
        }

        /**
         * @param transactionData
         * @param state
         * @return true if state is correct, else false
         */
        protected boolean checkAndFixTransactionState(ITransactionData transactionData, TrackingState state) {
            TrackingState trackingState = transactionData.getTrackingState();
            if (!state.equals(trackingState)) {
                LOG.info(getTrackingStatus() + ": Found incorrect TransactionData state (" + state + "), " +
                        "when correct (" + trackingState + "). Remove incorrect.");
                transactionDataPersister.deleteTrackingInfo(transactionData, state);
                if (trackingState != null && state.getGameServerId() == trackingState.getGameServerId()) {
                    //not need clear cachedValue
                    SessionHelper.getInstance().markTransactionCompleted();
                }
                return false;
            }
            return true;
        }

        protected boolean isNeedProcessWallet(long accountId, TrackingInfo trackingInfo) {
            return trackingInfo.hasWallet() && !WalletTracker.getInstance().containsKey(accountId);
        }

        protected boolean isNeedProcessPaymentTransaction(long accountId, TrackingInfo trackingInfo) {
            return trackingInfo.isHasPaymentTransaction() && !PaymentTransactionTracker.getInstance().
                    containsKey(accountId);
        }

        protected boolean isNeedProcessFrbWin(long accountId, TrackingInfo trackingInfo) {
            return trackingInfo.hasFrbWin() && !FRBonusWinTracker.getInstance().containsKey(accountId);
        }

        protected boolean isNeedProcessFrbNotification(long accountId, TrackingInfo trackingInfo) {
            return trackingInfo.hasFrbNotification() &&
                    !FRBonusNotificationTracker.getInstance().containsKey(accountId);
        }

        private void clearTransactionData(ServerLockInfo lock) {
            SessionHelper.getInstance().clear();
            transactionDataPersister.invalidate(lock);
        }
    }

    private class ProcessOnlineTransactionsTask extends ProcessTransactionsTask {
        private final static String LOCK_NAME = "TD_ONLINE_LOCK_";

        public ProcessOnlineTransactionsTask(int gameServerId) {
            super(gameServerId);
        }

        @Override
        public void process(String lockId, TrackingState state, TrackingInfo trackingInfo,
                            ITransactionData cachedValue) {
            long accountId = trackingInfo.getAccountId();
            boolean needProcess = !LogoutTracker.getInstance().isRegistered(accountId);
            if (!needProcess) {
                if (cachedValue == null) {
                    needProcess = true;
                } else {
                    AccountInfo accountInfo = cachedValue.getAccount();
                    SessionInfo sessionInfo = cachedValue.getPlayerSession();
                    if (accountInfo == null || sessionInfo == null) {
                        needProcess = true;
                    } else {
                        long sessionTimeOut = TransactionDataTracker.getSessionTimeOut(sessionInfo, accountInfo,
                                cachedValue.getGameSession());
                        if (sessionInfo.getLastActivityTime() + sessionTimeOut < System.currentTimeMillis()) {
                            needProcess = true;
                        } else {
                            needProcess = false;
                        }
                    }
                }
            }
            if (!needProcess) {
                if (isNeedProcessWallet(accountId, trackingInfo)) {
                    WalletTracker.getInstance().addTask(accountId);
                }
                if (isNeedProcessPaymentTransaction(accountId, trackingInfo)) {
                    PaymentTransactionTracker.getInstance().addTask(accountId);
                }
                if (isNeedProcessFrbWin(accountId, trackingInfo)) {
                    FRBonusWinTracker.getInstance().addTask(accountId);
                }
                if (isNeedProcessFrbNotification(accountId, trackingInfo)) {
                    FRBonusNotificationTracker.getInstance().addTask(accountId);
                }
            } else {
                try {
                    SessionHelper.getInstance().tryLockByAccountHash(lockId);
                } catch (CommonException e) {
                    String message = "ProcessOnlineTransactionsTask: " + getGameServerId() + " Cannot tryLock key: " +
                            lockId + ": " + e;
                    if (e instanceof CannotLockException) {
                        LOG.warn(message);
                    } else {
                        LOG.error(message);
                    }
                    return;
                }
                try {
                    SessionHelper.getInstance().openSession();
                    ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                    if (!checkAndFixTransactionState(transactionData, state)) {
                        return;
                    }
                    AccountInfo accountInfo = transactionData.getAccount();
                    ServerLockInfo lock = SessionHelper.getInstance().getDomainSession().getLockInfo().
                            getServerLockInfo();
                    trackingInfo = transactionData.getTrackingInfo();
                    if (isNeedProcessFrbWin(accountId, trackingInfo)) {
                        processFrbWin(lock, accountId);
                    }
                    if (isNeedProcessFrbNotification(accountId, trackingInfo)) {
                        processFrbNotification(lock, accountId);
                    }
                    if (isNeedProcessWallet(accountId, trackingInfo)) {
                        processWallet(lock, accountId);
                    }
                    if (isNeedProcessPaymentTransaction(accountId, trackingInfo)) {
                        processPaymentTransaction(lock, accountId);
                    }
                    processLogout(lock, accountId, getGameServerId(), accountInfo);
                    SessionHelper.getInstance().markTransactionCompleted();
                } catch (Throwable t) {
                    LOG.error("ProcessOnlineTransactionsTask: Error on process transaction data by key=" + lockId, t);
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                }
            }
        }

        @Override
        long getCheckInterval() {
            return CHECK_TRANSACTIONS_INTERVAL_IN_SECONDS;
        }

        @Override
        protected String getLockName(int gameServerId) {
            return LOCK_NAME + gameServerId;
        }

        @Override
        protected TrackingStatus getTrackingStatus() {
            return TrackingStatus.ONLINE;
        }
    }

    private class ProcessOfflineTransactionsTask extends ProcessTransactionsTask {
        private final static String LOCK_NAME = "TD_OFFLINE_LOCK_";

        public ProcessOfflineTransactionsTask(int gameServerId) {
            super(gameServerId);
        }

        @Override
        public void process(String lockId, TrackingState state, TrackingInfo trackingInfo,
                            ITransactionData cachedValue) {
            long accountId = trackingInfo.getAccountId();
            TrackingState cachedTrackingState = cachedValue == null ? null : cachedValue.getTrackingState();
            boolean stateDifferent = !state.equals(cachedTrackingState);
            TrackingInfo cachedTrackingInfo = cachedValue == null ? null : cachedValue.getTrackingInfo();
            boolean trackingInfoDifferent = !trackingInfo.equals(cachedTrackingInfo);
            LOG.debug("ProcessOfflineTransactionsTask: " + trackingInfo + ", cachedTrackingInfo=" + cachedTrackingInfo +
                    ", trackingState=" + state + ", cachedTrackingState=" + cachedTrackingState +
                    ", stateDifferent=" + stateDifferent + ", trackingInfoDifferent=" + trackingInfoDifferent +
                    ", accountId=" + accountId + ", lockId=" + lockId);
            if (accountId == 0 || isNeedProcessFrbWin(accountId, trackingInfo) ||
                    isNeedProcessWallet(accountId, trackingInfo) || isNeedProcessFrbNotification(accountId, trackingInfo) ||
                    isNeedProcessPaymentTransaction(accountId, trackingInfo)
                    || stateDifferent || trackingInfoDifferent) {
                try {
                    SessionHelper.getInstance().tryLockByAccountHash(lockId);
                } catch (CommonException e) {
                    String message = "ProcessOfflineTransactionsTask: " + getGameServerId() + " Cannot tryLock key: " +
                            lockId + ": " + e;
                    if (e instanceof CannotLockException) {
                        LOG.warn(message);
                    } else {
                        LOG.error(message);
                    }
                    return;
                }
                try {
                    SessionHelper.getInstance().openSession();
                    ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                    if (accountId == 0) {
                        accountId = transactionData.getAccountId();
                    }
                    if (!checkAndFixTransactionState(transactionData, state)) {
                        LOG.debug("ProcessOfflineTransactionsTask: checkAndFixTransactionState case, " +
                                "accountId=" + accountId);
                        return;
                    }
                    ServerLockInfo lock = SessionHelper.getInstance().getDomainSession().getLockInfo().
                            getServerLockInfo();
                    trackingInfo = transactionData.getTrackingInfo();
                    boolean processed = false;
                    if (accountId == 0) {
                        LOG.error("ProcessOfflineTransactionsTask: cannot process, accountId unknown, " +
                                "transactionData=" + transactionData);
                        return;
                    }
                    if (isNeedProcessFrbWin(accountId, trackingInfo)) {
                        processed = true;
                        processFrbWin(lock, accountId);
                    }
                    if (isNeedProcessFrbNotification(accountId, trackingInfo)) {
                        processed = true;
                        processFrbNotification(lock, accountId);
                    }

/*                    LOG.debug("ProcessOfflineTransactionsTask: " +
                            "accountId=" + accountId +
                            ", isNeedProcessWallet=" + isNeedProcessWallet(accountId, trackingInfo) +
                            ", hasTask=" + WalletTracker.getInstance().containsKey(accountId) +
                            ", trackingInfo=" + trackingInfo);*/
                    if (isNeedProcessWallet(accountId, trackingInfo)) {
                        processed = true;
                        processWallet(lock, accountId);
                    }
                    if (isNeedProcessPaymentTransaction(accountId, trackingInfo)) {
                        processed = true;
                        processPaymentTransaction(lock, accountId);
                    }
                    if (!processed) {
                        LOG.warn("ProcessOfflineTransactionsTask: not found processing parts, need fix tracking state," +
                                "accountId=" + accountId);
                        SessionHelper.getInstance().commitTransaction();
                    }
                    SessionHelper.getInstance().markTransactionCompleted();
                } catch (Throwable t) {
                    LOG.error("Error on process transaction data by key=" + lockId, t);
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                }
            }
        }

        @Override
        long getCheckInterval() {
            return CHECK_TRANSACTIONS_INTERVAL_IN_SECONDS;
        }

        @Override
        protected String getLockName(int gameServerId) {
            return LOCK_NAME + gameServerId;
        }

        @Override
        protected TrackingStatus getTrackingStatus() {
            return TrackingStatus.TRACKING;
        }
    }
}
