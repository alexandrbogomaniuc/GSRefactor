package com.abs.casino.gs.managers.payment.bonus.tracker;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.abs.casino.common.cache.data.payment.frb.FRBonusNotificationStatus;
import com.abs.casino.common.engine.tracker.AbstractCommonTrackingTask;
import com.abs.casino.common.exception.CannotLockException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.util.logkit.LogUtils;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.TransactionDataTracker;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusNotificationManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.TimeUnit;

public class FRBonusNotificationTrackerTask extends AbstractCommonTrackingTask<Long, FRBonusNotificationTracker> {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(FRBonusNotificationTrackerTask.class);
    public static final int DEFAULT_TASK_PENDING_TIME = 48;

    public FRBonusNotificationTrackerTask(Long accountId, FRBonusNotificationTracker tracker) {
        super(accountId, tracker);
    }

    @Override
    public void process() throws CommonException {
        process(true, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC, false);
    }

    public void process(boolean throwTimeoutException, long timeoutInMillis, boolean forced) throws CommonException {
        Long accountId = getKey();
        LOG.info("process: " + accountId);
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
            LOG.warn("Cannot lock, exit. accountId=" + accountId + ", exception=" + e);
            return;
        }

        boolean foreignTask = false;
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            int currentLocker = SessionHelper.getInstance().getTransactionData().getLastLockerId();
            if (currentLocker != GameServer.getInstance().getServerId()) {
                LOG.warn("lockerId mismatch: accountId=" + accountId + ", currentLocker=" + currentLocker +
                        ", original GS may be down");
                foreignTask = true;
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, true);
            if (accountInfo != null && !accountInfo.isLocked()) {
                FRBonusNotification frbNotification =
                        SessionHelper.getInstance().getTransactionData().getFrbNotification();
                LOG.debug("process sleep task for frbNotification: " + frbNotification);
                if (frbNotification != null) {
                    LOG.debug("process task for operation:" + frbNotification);
                    FRBonusNotificationStatus externalStatus = frbNotification.getExternalStatus();
                    FRBonusNotificationManager notificationManager = FRBonusNotificationManager.getInstance();
                    if (externalStatus == FRBonusNotificationStatus.STARTED
                            || externalStatus == FRBonusNotificationStatus.FAIL
                            || forced) {
                        notificationManager.processNotify(frbNotification);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(LogUtils.markException("handleNotifyFailure accountId:" +
                                    accountId + " bankId:" + accountInfo.getBankId() +
                                    " frbonusNotify:" + frbNotification +
                                    " status:" + externalStatus + ", default behaviour"));
                        }
                    }
                    notificationManager.processFinalize(frbNotification);
                } else {
                    LOG.debug("process task for null notification");
                    throw new CommonException("TransactionData contains no notification so far");
                }
            } else {
                LOG.warn("Account is null or locked, stop tracking");
            }
        } catch (Throwable e) {
            if (foreignTask) {
                LOG.error("Tracking failed, accountId=" + accountId + ", foreign GS task. Stop tracking", e);
            }
            throw e;
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().commitTransaction();
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

    @Override
    public void handleCommonException(boolean done, boolean fatalError, CommonException exception) {
        LOG.error("handleCommonException key:" + getKey() + ", error:", exception);
        remove(done, fatalError);
        Long accountId = getKey();
        boolean transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().lock(accountId, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
            }
        } catch (CommonException e) {
            LOG.warn("Cannot lock, exit. accountId=" + accountId + ", exception=" + e);
            return;
        }
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId, true);
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            FRBonusNotification frbNotification = transactionData.getFrbNotification();
            LOG.debug("handleCommonException key:" + getKey());
            boolean needAddTask = false;
            if (frbNotification != null) {
                if (frbNotification.isOverdue(getTimePending(accountInfo.getBankId()))
                        && !FRBonusNotificationStatus.PENDING.equals(frbNotification.getExternalStatus())) {
                    frbNotification.setExternalStatus(FRBonusNotificationStatus.PENDING);
                    LOG.info("handleCommonException key:" + getKey() + " stop task for further action, " +
                            frbNotification);
                }
                if (FRBonusNotificationStatus.PENDING.equals(frbNotification.getExternalStatus())) {
                    LOG.debug("handleCommonException key:" + getKey() + " operation is freezing: " + frbNotification);
                } else {
                    needAddTask = true;
                }
                SessionHelper.getInstance().getDomainSession().persistFrbNotification();
            }
            LOG.debug("handleCommonException key:" + getKey() + " needAddTask=" + needAddTask);
            if (needAddTask) {
                getTracker().addTask(getKey(), getTaskSleepTimeout());
            }
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Throwable e) {
            LOG.error("handleCommonException error:", e);
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
    }
}
