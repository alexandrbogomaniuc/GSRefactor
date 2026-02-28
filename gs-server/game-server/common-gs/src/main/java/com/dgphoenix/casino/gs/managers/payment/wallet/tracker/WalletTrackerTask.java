package com.dgphoenix.casino.gs.managers.payment.wallet.tracker;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.engine.tracker.AbstractCommonTrackingTask;
import com.abs.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.TransactionDataTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * User: plastical
 * Date: 10.03.2010
 */
public class WalletTrackerTask extends AbstractCommonTrackingTask<Long, WalletTracker> {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(WalletTrackerTask.class);
    private boolean highPriority = false;
    private Integer gameId;
    public static final int DEFAULT_TASK_PENDING_TIME = 48;


    public WalletTrackerTask(Long accountId, WalletTracker tracker) {
        super(accountId, tracker);
        this.gameId = null;
    }

    public WalletTrackerTask(Long accountId, WalletTracker tracker, boolean highPriority) {
        super(accountId, tracker);
        this.highPriority = highPriority;
        this.gameId = null;
    }

    public WalletTrackerTask(Long accountId, int gameId, WalletTracker tracker, boolean highPriority) {
        super(accountId, tracker);
        this.highPriority = highPriority;
        this.gameId = gameId;
    }

    private Integer getGameId() {
        return gameId;
    }

    private boolean isWalletTaskStartService() {
        return getGameId() != null;
    }

    @Override
    public void process() throws CommonException {
        process(false, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
    }

    public void process(boolean throwTimeoutException, long timeoutInMillis) throws CommonException {
        Long accountId = getKey();
        LOG.debug("process: accountId={}, gameId={}", accountId, gameId);
        boolean transactionAlreadyStarted;
        try {
            transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().lock(accountId, timeoutInMillis);
            }
        } catch (CannotLockException e) {
            if (throwTimeoutException) {
                throw e;
            }
            LOG.warn("Cannot lock, exit. accountId={}", accountId, e);
            return;
        }
        boolean foreignTask = false;
        IWalletOperation pendingOperation = null;
        LasthandInfo originalLastHand = null;
        boolean isNeedRestoreLastHand = true;
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            int currentLocker = SessionHelper.getInstance().getTransactionData().getLastLockerId();
            if (currentLocker != GameServer.getInstance().getServerId()) {
                LOG.warn("lockerId mismatch: accountId={}, currentLocker={}, original GS may be down", accountId, currentLocker);
                foreignTask = true;
                //return;
            }
            //need save originalLastHand, for preventing save foreign lasthand; handleFailure() always load own lasthand
            originalLastHand = SessionHelper.getInstance().getTransactionData().getLasthand();
            GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
            if (gameSession != null && gameSession.isRealMoney()) {
                isNeedRestoreLastHand = false;
            }

            if (isNeedRestoreLastHand) {
                SessionHelper.getInstance().getTransactionData().setLasthand(null);
            }

            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, true);
            if (accountInfo != null && !accountInfo.isLocked()) {
                IWalletProtocolManager walletManager = WalletProtocolFactory.getInstance().
                        getWalletProtocolManager(accountInfo.getBankId());
                if (isWalletTaskStartService()) {
                    IWallet wallet = WalletPersister.getInstance().getWallet(accountInfo.getId());
                    pendingOperation = wallet.getCurrentWalletOperation(getGameId());
                    if (pendingOperation != null &&
                            pendingOperation.getExternalStatus().equals(WalletOperationStatus.PEENDING_SEND_ALERT)) {
                        if (pendingOperation.getType() == WalletOperationType.DEBIT) {
                            pendingOperation.setExternalStatus(WalletOperationStatus.STARTED);
                        } else {
                            pendingOperation.setExternalStatus(WalletOperationStatus.FAIL);
                        }
                        walletManager.handleFailure(accountInfo, getGameId());
                    } else {
                        throw new CommonException("Cannot process pending alert, operation has wrong status");
                    }
                } else {
                    walletManager.handleFailure(accountInfo);
                }
                if (isNeedRestoreLastHand) {
                    SessionHelper.getInstance().getTransactionData().setLasthand(originalLastHand);
                }
                if (!transactionAlreadyStarted) {
                    SessionHelper.getInstance().commitTransaction();
                }
            }
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Throwable e) {
            if (pendingOperation != null) { //restore status for prevent additional tracking
                pendingOperation.setExternalStatus(WalletOperationStatus.PEENDING_SEND_ALERT);
            }
            if (isNeedRestoreLastHand && originalLastHand != null) {
                try {
                    SessionHelper.getInstance().getTransactionData().setLasthand(originalLastHand);
                } catch (Exception e1) {
                    LOG.error("Cannot restore lasthand", e1);
                }
            }
            if (foreignTask) {
                LOG.error("Tracking failed, accountId={}, foreign GS task. Stop tracking", accountId, e);
                if (isWalletTaskStartService()) {
                    throw e;
                }
            } else {
                throw e;
            }
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }

    }

    @Override
    protected long getTaskSleepTimeout() throws CommonException {
        return GameServerConfiguration.getInstance().getWalletTrackerSleepTimeout();
    }

    public boolean isHighPriority() {
        return highPriority;
    }

    @Override
    protected void remove(boolean done, boolean fatalError) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("remove task: {}, done={}, fatalError={}, highPriority={}", getKey(), done, fatalError, highPriority);
        }
        if (isHighPriority()) {
            getTracker().removeHighPriorityKey(getKey());
        } else {
            getTracker().remove(getKey());
        }
    }

    protected long getTimePending(long bankId) {
        long time = BankInfoCache.getInstance().getBankInfo(bankId).getWalletTaskPendingTime();
        long returnTime =  time > 0 ? time : TimeUnit.HOURS.toMillis(DEFAULT_TASK_PENDING_TIME);
        LOG.debug("getTimePending: returnTime={}", returnTime);
        return returnTime;
    }

    public void handleCommonException(boolean done, boolean fatalError, CommonException exception) {
        if (exception instanceof CannotLockException) {
            LOG.warn("handleCommonException key:{} cannot lock", getKey(), exception);
        } else {
            LOG.error("handleCommonException key:{}", getKey(), exception);
        }
        remove(done, fatalError);
        Long accountId = getKey();
        boolean transactionAlreadyStarted;
        try {
            transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().lock(accountId, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
            }
        } catch (CommonException e) {
            LOG.warn("Cannot lock, exit. accountId={}", accountId, e);
            return;
        }
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, true);
            IWallet wallet = WalletPersister.getInstance().getWallet(accountInfo.getId());
            if (!isWalletTaskStartService()) {
                boolean needAddTask = false;
                long timeout = 0;
                Collection<CommonGameWallet> gameWallets = wallet.getCommonGameWallets();
                LOG.debug("handleCommonException key:{} gameWallets.size={}", getKey(), gameWallets.size());
                for (CommonGameWallet gameWallet : gameWallets) {
                    int walletGameId = gameWallet.getGameId();
                    IWalletOperation operation = wallet.getCurrentWalletOperation(gameWallet);
                    if (!canProcessWalletOperation(walletGameId, operation)) {
                        continue;
                    }
                    if (operation.isOverdue(getTimePending(accountInfo.getBankId()))) {
                        operation.setExternalStatus(WalletOperationStatus.PEENDING_SEND_ALERT);
                        LOG.info("handleCommonException key:{} stop task for further action, gameId={}", getKey(), walletGameId);
                    } else {
                        needAddTask = true;
                        timeout = getTaskSleepTimeout();
                    }
                }
                LOG.debug("handleCommonException key:{} needAddTask={}", getKey(), needAddTask);
                if (needAddTask) {
                    getTracker().addTask(getKey(), timeout);
                }
            } else {
                IWalletOperation operation = wallet.getCurrentWalletOperation(getGameId());
                if (operation != null) {
                    operation.setExternalStatus(WalletOperationStatus.PEENDING_SEND_ALERT);
                    LOG.debug("handleCommonException key:{} PEENDING_SEND_ALERT gameId={}", getKey(), getGameId());
                } else {
                    LOG.debug("handleCommonException key:{} gameId={} operation is null", getKey(), getGameId());
                }
            }
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (CommonException e) {
            LOG.error("handleCommonException error:", e);
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
    }

    private boolean canProcessWalletOperation(int walletGameId, IWalletOperation operation) {
        boolean result = true;
        if (operation == null) {
            LOG.debug("handleCommonException key:{} operation is null, gameId={}", getKey(), walletGameId);
            result = false;
        } else if (WalletOperationStatus.PEENDING_SEND_ALERT.equals(operation.getExternalStatus())) {
            LOG.debug("handleCommonException key:{} operation is freezing, gameId={}", getKey(), walletGameId);
            result = false;
        }
        return result;
    }
}
