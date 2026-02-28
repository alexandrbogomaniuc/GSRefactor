package com.abs.casino.gs.managers.payment.bonus.tracker;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.abs.casino.common.engine.tracker.AbstractCommonTrackingTask;
import com.abs.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.TransactionDataTracker;
import com.abs.casino.gs.managers.payment.bonus.FRBonusWinRequestFactory;
import com.abs.casino.gs.managers.payment.bonus.IFRBonusWinManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FRBonusWinTrackerTask extends AbstractCommonTrackingTask<Long, FRBonusWinTracker> {

    private static final Logger LOG = LogManager.getLogger(FRBonusWinTrackerTask.class);
    private static final int DEFAULT_TASK_PENDING_TIME = 48;

    private Long gameId;

    public FRBonusWinTrackerTask(Long accountId, FRBonusWinTracker tracker) {
        super(accountId, tracker);
    }

    public FRBonusWinTrackerTask(Long accountId, FRBonusWinTracker tracker, Long gameId) {
        super(accountId, tracker);
        this.gameId = gameId;
    }

    @Override
    public void process() throws CommonException {
        process(false, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
    }

    public void process(boolean throwTimeoutException, long timeoutInMillis) throws CommonException {
        Long accountId = getKey();
        LOG.info("process: {}", accountId);
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
            LOG.warn("Cannot lock, exit. accountId={}, exception={}", accountId, e);
            return;
        }

        FRBWinOperation pendingOperation = null;
        boolean foreignTask = false;
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
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, true);
            if (accountInfo != null && !accountInfo.isLocked()) {
                IFRBonusWinManager winManager = FRBonusWinRequestFactory.getInstance().
                        getFRBonusWinManager(accountInfo.getBankId());
                if (isStartFromService()) {
                    FRBonusWin frBonusWin = SessionHelper.getInstance().getTransactionData().getFrbWin();
                    LOG.debug("process sleep task for frBonusWin:{}", frBonusWin);
                    if (frBonusWin != null) {
                        pendingOperation = frBonusWin.getFRBonusWinOperation(gameId);
                        LOG.debug("process task for operation:{}", pendingOperation);
                        if (pendingOperation != null &&
                                pendingOperation.getExternalStatus().equals(FRBWinOperationStatus.PEENDING_SEND_ALERT)) {
                            pendingOperation.setExternalStatus(FRBWinOperationStatus.FAIL);
                            winManager.handleFailure(accountInfo);
                        } else {
                            throw new CommonException("Cannot process pending alert, operation has wrong status");
                        }
                    }

                } else {
                    LOG.debug("process handleFailure accountId:{}", accountId);
                    winManager.handleFailure(accountInfo);
                }
                if (!transactionAlreadyStarted) {
                    SessionHelper.getInstance().commitTransaction();
                }
            } else {
                LOG.warn("Account is null or locked, stop tracking");
            }
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Throwable e) {
            if (pendingOperation != null) { //restore status for prevent additional tracking
                pendingOperation.setExternalStatus(FRBWinOperationStatus.PEENDING_SEND_ALERT);
            }
            if (foreignTask) {
                LOG.error("Tracking failed, accountId={}, foreign GS task. Stop tracking", accountId, e);
                if (isStartFromService()) {
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
        return GameServerConfiguration.getInstance().getFrbonusWinTrackerSleepTimeout();
    }

    protected long getTimePending(long bankId) {
        long time = BankInfoCache.getInstance().getBankInfo(bankId).getWalletTaskPendingTime();
        return time > 0 ? time : TimeUnit.HOURS.toMillis(DEFAULT_TASK_PENDING_TIME);
    }

    private boolean isStartFromService() {
        return gameId != null;
    }

    @Override
    public void handleCommonException(boolean done, boolean fatalError, CommonException exeption) {
        LOG.error("handleCommonException key:{}, gameId={} error:", getKey(), gameId, exeption);
        remove(done, fatalError);
        Long accountId = getKey();
        boolean transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().lock(accountId, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
            }
        } catch (CommonException e) {
            LOG.warn("Cannot lock, exit. accountId={}, exception={}", accountId, e);
            return;
        }
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, true);
            FRBonusWin frBonusWin = SessionHelper.getInstance().getTransactionData().getFrbWin();
            if (!isStartFromService()) {
                boolean needAddTask = false;
                if (frBonusWin != null) {
                    Set<String> keySet = frBonusWin.getFRBonusWins().keySet();
                    Iterator<String> iterator = keySet.iterator();
                    LOG.debug("handleCommonException key:{} keySet.size={}", getKey(), keySet.size());
                    while (iterator.hasNext()) {
                        long gameId = Long.parseLong(iterator.next());
                        FRBWinOperation operation = frBonusWin.getFRBonusWinOperation(gameId);

                        if (operation == null) {
                            LOG.debug("handleCommonException key:{} operation is null, gameId={}", getKey(), gameId);
                            continue;
                        }
                        if (FRBWinOperationStatus.PEENDING_SEND_ALERT.equals(operation.getExternalStatus())) {
                            LOG.debug("handleCommonException key:{} operation is freezing, gameId={}", getKey(), gameId);
                            continue;
                        }
                        if (operation.isOverdue(getTimePending(accountInfo.getBankId()))) {
                            operation.setExternalStatus(FRBWinOperationStatus.PEENDING_SEND_ALERT);
                            LOG.info("handleCommonException key:{} stop task for further action, gameId={}", getKey(), gameId);
                        } else {
                            needAddTask = true;
                        }
                    }
                } else {
                    LOG.warn("handleFailure frbonusWin is null for accountId:{}", accountId);
                }
                LOG.debug("handleCommonException key:{} needAddTask={}", getKey(), needAddTask);
                if (needAddTask) {
                    getTracker().addTask(getKey(), getTaskSleepTimeout());
                }
            } else {
                if (frBonusWin != null) {
                    FRBWinOperation operation = frBonusWin.getFRBonusWinOperation(gameId);
                    LOG.debug("handleCommonException key:{} PEENDING_SEND_ALERT gameId={}, operation={}", getKey(), gameId, operation);
                    if (operation != null) {
                        operation.setExternalStatus(FRBWinOperationStatus.PEENDING_SEND_ALERT);
                    }
                } else {
                    LOG.warn("handleFailure frbonusWin is null for accountId:{}", accountId);
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
}
