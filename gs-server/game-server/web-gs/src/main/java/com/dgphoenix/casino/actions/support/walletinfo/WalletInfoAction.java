package com.abs.casino.actions.support.walletinfo;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraLasthandPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.abs.casino.gs.managers.payment.wallet.WalletAlertStatus;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTrackerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by quant on 02.02.16.
 */
public class WalletInfoAction extends Action {
    private static final Logger LOG = LogManager.getLogger(WalletInfoAction.class);
    private final CassandraLasthandPersister lasthandPersister;

    public WalletInfoAction() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        WalletInfoForm walletForm = (WalletInfoForm) form;
        long accountId = walletForm.getAccountId();
        int changeType = walletForm.getChangeType();
        int gameId = walletForm.getGameId();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        ActionMessages errors = getErrors(request);
        try {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData data = SessionHelper.getInstance().getTransactionData();
                if (data.getGameSession() != null || data.getPlayerSession() != null) {
                    String message = "Can't perform action=" + changeType + " on accountId= " + accountId + " data="
                            + walletForm.getAccountData() + ". Player is online!";
                    LOG.warn(message);
                    errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.errorMessage", message));
                } else {
                    if (walletForm.getAccountData().equals(CassandraTransactionDataPersister.WALLET_FIELD)) {
                        CommonWallet wallet = (CommonWallet) data.getWallet();
                        CommonWalletOperation operation = wallet.getCurrentWalletOperation(gameId);
                        if (changeType == Status.DELETED) {
                            LOG.info("Deleting CommonGameWallet for gameId=" + gameId + " from ip=" + ipAddress);
                            wallet.removeGameWallet(gameId);
                        } else if (changeType == Status.UNRESOLVED) {
                            LOG.info("Suspending CommonGameWallet operationId=" + operation.getId() + " for gameId="
                                    + gameId + " from ip=" + ipAddress);
                            operation.setExternalStatus(WalletOperationStatus.PEENDING_SEND_ALERT);
                        } else if (changeType == Status.RESOLVED) {
                            if (!operation.getExternalStatus().equals(WalletOperationStatus.PEENDING_SEND_ALERT)) {
                                String message = "Can't start tracking. Operation is not pending. Resume action from ip=" + ipAddress;
                                LOG.error(message);
                                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.errorMessage", message));
                            } else {
                                LOG.info("Resuming CommonGameWallet operationId=" + operation.getId() + " for gameId="
                                        + gameId + " from ip=" + ipAddress);
                                if (startDelayWalletTask(accountId, gameId) == WalletAlertStatus.NOT_SUCCESSFUL) {
                                    operation.setExternalStatus(operation.getType().equals(WalletOperationType.DEBIT) ?
                                            WalletOperationStatus.STARTED : WalletOperationStatus.FAIL);
                                }
                            }
                        }
                    } else {
                        if (changeType == Status.DELETED) {
                            LOG.info("Deleting lastHand for accountId=" + accountId + ", gameId=" + gameId + " from ip=" + ipAddress);
                            lasthandPersister.delete(accountId, gameId, null, null);
                            //это нужно?
                            //if (data.getLasthand().getId() == walletForm.getGameId()) {
                            //    data.setLasthand(null);
                            //}
                        }
                    }
                    SessionHelper.getInstance().commitTransaction();
                }
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (CommonException e) {
            String message = "Error occurred while locking accountId=" + accountId;
            LOG.error(message, e);
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.errorMessage", message));
        }
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            return mapping.findForward("error");
        }
        return mapping.findForward("success");
    }

    private WalletAlertStatus startDelayWalletTask(long accountId, long gameId) {
        LOG.info("startDelayWalletTask run, accountId={}, gameId={}", accountId, gameId);
        try {
            new WalletTrackerTask(accountId, (int) gameId, WalletTracker.getInstance(), true).process(true, 5000);
            return WalletAlertStatus.SUCCESSFUL;
        } catch (Exception e) {
            LOG.error("startDelayWalletTask: error, accountId={}, gameId={}", accountId, gameId, e);
            return WalletAlertStatus.NOT_SUCCESSFUL;
        }
    }

    public interface Status {
        int UNRESOLVED = 0;
        int RESOLVED = 1;
        int DELETED = 2;
    }

}
