package com.abs.casino.actions.support.walletsmanager;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraLasthandPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTracker;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTrackerTask;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.abs.casino.gs.persistance.LasthandPersister;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by quant on 15.06.16.
 */

public class WalletsManagerAction extends Action {

    private static final Logger LOG = Logger.getLogger(WalletsManagerAction.class);
    public static final String ACCOUNT_DATA_WALLET = "wallet";
    public static final String ACCOUNT_DATA_LAST_HAND = "lastHand";
    public static final String ACCOUNT_DATA_FRB_WIN = "frbWin";
    public static final String ACCOUNT_DATA_SHOW = "show";
    public static final String ACCOUNT_DATA_DEL_FRB_NOTIFICATION = "delFrbNotification";
    public static final String ACCOUNT_DATA_RESTART_FRB_NOTIFICATION = "restartFrbNotification";

    private final CassandraLasthandPersister lasthandPersister;

    public WalletsManagerAction() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        WalletsManagerForm walletForm = (WalletsManagerForm) form;

        if (walletForm.getAccountData().equals(WalletsManagerAction.ACCOUNT_DATA_SHOW)) {
            LOG.debug("ACCOUNT_DATA_SHOW");
            return mapping.findForward("success");
        }

        long accountId = walletForm.getAccountId();
        long bankId = walletForm.getBankId();
        String extUserId = walletForm.getExtUserId();
        int gameId = walletForm.getGameId();

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        ActionMessages errors = getErrors(request);
        try {
            AccountInfo accountInfo = null;
            if (accountId != 0) {
                accountInfo = AccountManager.getInstance().getByAccountId(accountId);
            } else if (bankId != 0) {
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                accountInfo = AccountManager.getInstance().getByCompositeKey(bankInfo.getSubCasinoId(), bankInfo, extUserId);
                accountId = accountInfo.getId();
            }

            if (accountInfo == null) {
                throw new CommonException("Unable obtain AccountInfo");
            }

            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData data = SessionHelper.getInstance().getTransactionData();

                GameSession gameSession = data.getGameSession();
                if (gameSession != null && gameSession.getGameId() == gameId) {
                    String message = "Can't delete " + walletForm.getAccountData() + " for accountId=" + accountId +
                            ". Player is currently playing the game!";
                    LOG.warn(message);
                    errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.errorMessage", message));
                    return mapping.findForward("success");
                }

                String operationStatus;
                LOG.info("Trying to delete " + walletForm.getAccountData() + " for gameId=" + gameId + " from ip=" + ipAddress +
                        ", accountId=" + accountInfo.getId() + ", subcasinoId=" + accountInfo.getSubCasinoId() +
                        ", bankId=" + accountInfo.getBankId());

                switch (walletForm.getAccountData()) {
                    case ACCOUNT_DATA_WALLET:
                        operationStatus = deleteWalletOperation(gameId, data, accountInfo);
                        break;
                    case ACCOUNT_DATA_FRB_WIN:
                        operationStatus = deleteFrbWinOperation(gameId, data, accountInfo);
                        break;
                    case ACCOUNT_DATA_LAST_HAND:
                        operationStatus = deleteLasthand(walletForm, gameId, data);
                        break;
                    case ACCOUNT_DATA_DEL_FRB_NOTIFICATION:
                        operationStatus = deleteFrbNotification(data, accountInfo);
                        break;
                    case ACCOUNT_DATA_RESTART_FRB_NOTIFICATION:
                        operationStatus = restartFrbNotification(data, accountInfo);
                        break;
                    default:
                        throw new CommonException("Unsupported account data type=" + walletForm.getAccountData());
                }

                LOG.info(operationStatus);
                request.setAttribute("operationStatus", operationStatus);
                SessionHelper.getInstance().commitTransaction();
            } catch (Exception e) {
                String message = "Error occurred while " + walletForm.getAccountData() + " on accountId= " + accountId;
                LOG.error(message, e);
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.common.errorMessage", message));
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
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

    private String restartFrbNotification(ITransactionData data, AccountInfo accountInfo) throws CommonException {
        LOG.info("Adding FRB notification to tracker. " + String.valueOf(data.getFrbNotification()));

        FRBonusNotification frbNotification = data.getFrbNotification();
        if (frbNotification == null) {
            LOG.debug("FRB notification is null");
            return "FRB notification is null";
        }

        new FRBonusNotificationTrackerTask(accountInfo.getId(), FRBonusNotificationTracker.getInstance()).process(true, 5000, true);

        return "FRB notification has been successfully completed";
    }

    private String deleteFrbNotification(ITransactionData data, AccountInfo accountInfo) throws CommonException {
        LOG.info("Deleting " + String.valueOf(data.getFrbNotification()));
        if (data.getFrbNotification() == null) {
            return "FRB notification does not exists";
        }

        FRBonusNotification frbNotification = data.getFrbNotification();
        data.setFrbNotification(null);
        return "FRB notification was successfully deleted.";
    }

    private String deleteLasthand(WalletsManagerForm walletForm, int gameId, ITransactionData data) {
        long accountId = data.getAccountId();
        String operationStatus;
        String lastHand = lasthandPersister.get(accountId, gameId, null, null);
        if (StringUtils.isNotEmpty(lastHand)) {
            LOG.info("Deleting lastHand=" + lastHand);
            lasthandPersister.delete(accountId, gameId, null, null);
            if (data.getLasthand() != null && data.getLasthand().getId() == walletForm.getGameId()) {
                LasthandPersister.getInstance().clearCached();
            }
            operationStatus = "LastHand id " + gameId + " for game id " + gameId + " was successfully deleted.";
        } else {
            operationStatus = "Can't delete LastHand for game id " + gameId + ". There is no lastHand.";
        }
        return operationStatus;
    }

    private String deleteFrbWinOperation(int gameId, ITransactionData data, AccountInfo accountInfo) throws CommonException {
        String operationStatus;
        FRBonusWin frbonusWin = data.getFrbWin();
        FRBWinOperation operation = frbonusWin.getCurrentFRBonusWinOperation((long) gameId);
        if (operation != null) {
            LOG.info("Deleting frbWinOperation=" + operation.toString());
            frbonusWin.removeFRBonusWin(gameId);
            operationStatus = "FrbWin operation id " + operation.getId() + " for game id " + gameId + " was successfully deleted.";
        } else {
            operationStatus = "Can't delete frbWin operation for game id " + gameId + ". There is no frbWin operation.";
        }
        return operationStatus;
    }

    private String deleteWalletOperation(int gameId, ITransactionData data, AccountInfo accountInfo) throws CommonException {
        String operationStatus;
        CommonWallet wallet = (CommonWallet) data.getWallet();
        CommonWalletOperation operation = wallet.getCurrentWalletOperation(gameId);
        if (operation != null) {
            LOG.info("Deleting walletOperation=" + operation.toString());
            wallet.removeGameWallet(gameId);
            operationStatus = "Wallet operation id " + operation.getId() + " for game id " + gameId + " was successfully deleted.";
        } else {
            operationStatus = "Can't delete wallet operation for game id " + gameId + ". There is no wallet operation.";
        }
        return operationStatus;
    }
}
