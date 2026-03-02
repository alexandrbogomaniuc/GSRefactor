package com.abs.casino.sm.tracker.logout;

import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.engine.tracker.AbstractCommonTrackingTask;
import com.abs.casino.common.exception.CannotLockException;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.GameServer;
import com.abs.casino.gs.TransactionDataTracker;
import com.abs.casino.sm.IPlayerSessionManager;
import com.abs.casino.sm.PlayerSessionFactory;
import com.abs.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;

public class LogoutTask extends AbstractCommonTrackingTask<Long, LogoutTracker> {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(LogoutTask.class);
    private int gameServerId;
    private String extUserId;
    private Integer bankId;

    public LogoutTask(long accountId, LogoutTracker tracker, int gameServerId) {
        super(accountId, tracker);
        this.gameServerId = gameServerId;
    }

    public LogoutTask(long accountId, LogoutTracker tracker, int gameServerId, String extUserId, Integer bankId) {
        super(accountId, tracker);
        this.gameServerId = gameServerId;
        this.extUserId = extUserId;
        this.bankId = bankId;
    }

    @Override
    public void process() throws CommonException {
        Long accountId = getKey();
        long now = System.currentTimeMillis();

        if (accountId == null) {
            LOG.error("accountId is null");
            return;
        }
        boolean transactionAlreadyStarted;
        try {
            transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
            if (!transactionAlreadyStarted) {
                if (extUserId != null && bankId != null) {
                    SessionHelper.getInstance().lock(bankId, extUserId,
                            TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
                } else {
                    SessionHelper.getInstance().lock(accountId, TransactionDataTracker.START_TASK_PAUSE_IN_MSEC);
                }
            }
        } catch (CannotLockException e) {
            LOG.warn("Cannot lock, exit. accountId=" + accountId + ", exception=" + e);
            return;
        }
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            if (sessionInfo == null) {
                LOG.warn("sessionInfo is null in TransactionData, accountId=" + accountId);
                return;
            }

            int currentLocker = SessionHelper.getInstance().getTransactionData().getLastLockerId();
            if (currentLocker != gameServerId) {
                LOG.warn("lockerId mismatch: accountId=" + accountId + ", currentLocker=" + currentLocker + ", processed by gameServer=" + gameServerId);
                return;
            }
            AccountInfo accountInfo = SessionHelper.getInstance().getTransactionData().getAccount();
            if (accountInfo == null) {
                throw new CommonException("AccountInfo is null: accountId=" + accountId);
            }

            long sessionTimeOut = TransactionDataTracker.getSessionTimeOut(sessionInfo, accountInfo,
                    SessionHelper.getInstance().getTransactionData().getGameSession());

            if (sessionInfo.getLastActivityTime() + sessionTimeOut >=
                    System.currentTimeMillis()) {
                LOG.info("Stop logout task, reused session found! session=" + sessionInfo);
                return;
            }
            final IPlayerSessionManager manager = PlayerSessionFactory.getInstance().
                    getPlayerSessionManager(accountInfo.getBankId());
            manager.logout(accountInfo, "Logout task", sessionInfo);
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Throwable e) {
            LOG.error("Cannot logout: ", e);
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
            StatisticsManager.getInstance().updateRequestStatistics("LogoutTask:process 1",
                    System.currentTimeMillis() - now);
        }
    }

    @Override
    protected long getTaskSleepTimeout() throws CommonException {
        return GameServerConfiguration.getInstance().getLogoutTrackerSleepTimeout();
    }

    @Override
    public void handleCommonException(boolean done, boolean fatalError, CommonException ex) {
        LOG.error(this.getClass().getSimpleName() + "::run key:" + getKey() + " error:", ex);

        remove(done, fatalError);

        if (gameServerId != GameServer.getInstance().getServerId()) {
            return;
        }
        try {
            getTracker().addTask(getKey(), gameServerId, getTaskSleepTimeout());
        } catch (CommonException e) {
            LOG.error(this.getClass().getSimpleName() + "::handleCommonException error:", e);
        }
    }
}
