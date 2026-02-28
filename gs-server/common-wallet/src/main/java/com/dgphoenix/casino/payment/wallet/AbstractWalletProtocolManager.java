package com.abs.casino.payment.wallet;

import com.dgphoenix.casino.common.DomainSession;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.abs.casino.common.cache.data.payment.WalletOperationAdditionalProperties;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.payment.wallet.IWalletHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * User: plastical
 * Date: 04.03.2010
 */
public abstract class AbstractWalletProtocolManager<T extends IWallet, V extends IWalletOperation>
        implements IWalletProtocolManager {
    public static final String TRACKER_NAME = "WT";
    protected final BankInfo bankInfo;
    private final long bankId;
    protected IWalletHelper walletHelper;
    protected String jarInfo;
    private boolean persistWalletOperation;
    private IdGenerator idGenerator;

    protected AbstractWalletProtocolManager(long bankId) {
        this.bankId = bankId;
        this.bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            throw new RuntimeException("BankInfo not found for id=" + bankId);
        }
        this.persistWalletOperation = bankInfo.isPersistWalletOps();
        this.idGenerator = IdGenerator.getInstance();
    }

    protected AbstractWalletProtocolManager(BankInfo bankInfo, IdGenerator idGenerator) {
        this.bankInfo = bankInfo;
        this.bankId = bankInfo.getId();
        this.persistWalletOperation = bankInfo.isPersistWalletOps();
        this.idGenerator = idGenerator;
    }

    protected abstract Logger getLog();

    @Override
    public void init(IWalletHelper helper) {
        walletHelper = helper;
    }

    public void setJarInfo(String jarInfo) {
        this.jarInfo = jarInfo;
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public boolean isPersistWalletOperation() {
        return persistWalletOperation;
    }

    public void setPersistWalletOperation(boolean persistWalletOperation) {
        this.persistWalletOperation = persistWalletOperation;
    }

    @Override
    public IWallet handleCreateWallet(AccountInfo accountInfo, long gameSessionId, int gameId, GameMode mode,
                                      ClientType clientType) {
        if (mode == GameMode.REAL) {
            CommonWallet cWallet = (CommonWallet) WalletPersister.getInstance().
                    getWallet(accountInfo.getId());
            if (!cWallet.isGameWalletExist(gameId)) {
                cWallet.createGameWallet(gameId, gameSessionId, clientType);
            } else {
                cWallet.setGameWalletGameSessionId(gameId, gameSessionId);
            }
            WalletPersister.getInstance().setWallet(accountInfo.getId(), cWallet);
            registerForTracking(accountInfo.getId());
            return cWallet;
        }
        return null;
    }

    @Override
    public long getWalletRoundId(IWalletDBLink dbLink) throws WalletException {
        long roundId = -1;
        long gameId = dbLink.getGameId();

        if (dbLink.getMode() == GameMode.REAL) {
            CommonWallet cWallet = (CommonWallet) dbLink.getWallet();
            Long roundId1 = cWallet.getGameWalletRoundId((int) gameId);
            if (roundId1 == null) {
                roundId1 = generateRoundId();
                cWallet.setGameWalletRoundId((int) gameId, roundId1);
                //throw new WalletException("Wallet round id not found for accountId=" + accountId);
            }
            roundId = roundId1;
        }
        return roundId;
    }

    protected boolean isPersistDailyWalletOperation() {
        return false;
    }

    protected void processRealDebitCompleted(IWalletDBLink dbLink, boolean isBet,
                                             IExternalWalletTransactionHandler extHandler)
            throws WalletException {

        long gameId = dbLink.getGameId();
        long now = System.currentTimeMillis();
        CommonWallet cWallet = (CommonWallet) dbLink.getWallet();
        final CommonGameWallet commonGameWallet = cWallet.getGameWalletWithCheck((int) gameId);
        StatisticsManager.getInstance().updateRequestStatistics("OriginalCommonManagers processRealDebitCompleted 1",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        if (isBet) {
            CommonWalletOperation operation = commonGameWallet.getBetOperation();
            if (operation != null) {
                operation.update(WalletOperationStatus.COMPLETED, System.currentTimeMillis());
                AccountInfo accountInfo = dbLink.getAccount();
                if (isPersistWalletOperation()) {
                    walletHelper.updateWalletOperation(bankInfo, accountInfo, operation);
                }
                if (isPersistDailyWalletOperation()) {
                    BaseGameInfo gameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
                    String gameName = MessageManager.getInstance().getApplicationMessage("game.name." +
                            gameInfo.getName());
                    WalletOperationAdditionalProperties.getInstance().setAdditionalProperties(operation,
                            accountInfo.getCurrency().getCode(), gameName, gameInfo.getGroup().getGroupName(),
                            accountInfo.getExternalId());
                    walletHelper.persistDailyWallet(operation, accountInfo.getBankId());
                }
                if (extHandler != null) {
                    extHandler.operationCompleted(operation, gameId);
                }
                commonGameWallet.resetOperation(operation);
            }
            StatisticsManager.getInstance().updateRequestStatistics(
                    "OriginalCommonManagers processRealDebitCompleted 2", System.currentTimeMillis() - now);
        }
    }

    protected V getCurrentOperationWithCheck(T wallet, long gameId) throws WalletException {
        V operation = (V) wallet.getCurrentWalletOperation((int) gameId);
        if (operation == null) {
            throw new WalletException("operation is null");
        }
        return operation;
    }

    protected long generateRoundId() {
        return idGenerator.getNext(IWallet.class);
    }

    public long generateOperationId() {
        return idGenerator.getNext(CommonWalletOperation.class);
    }

    @Override
    public void handleDebitCompleted(long accountId, boolean isBet, IWalletDBLink dbLink,
                                     IExternalWalletTransactionHandler extHandler)
            throws WalletException {
        if (dbLink.getMode() == GameMode.REAL) {
            processRealDebitCompleted(dbLink, isBet, extHandler);
        }
    }

    @Override
    public void handleGameLogicCompleted(long accountId, long betAmount, long winAmount)
            throws WalletException {
        //do nothing here
    }

    @Override
    public void handleFailure(AccountInfo accountInfo) throws WalletException {
        processHandleFailure(accountInfo);
    }

    protected void clearEmptyGameWallets(CommonWallet wallet) {
        final Set<Integer> gameIds = wallet.getGameWallets().keySet();
        if (gameIds.isEmpty()) {
            return;
        }
        Set<Integer> emptyWallets = new HashSet<>(gameIds.size());
        for (Integer gameId : gameIds) {
            CommonWalletOperation betOperation = wallet.getGameWalletBetOperation(gameId);
            CommonWalletOperation winOperation = wallet.getGameWalletWinOperation(gameId);
            if (betOperation == null && winOperation == null) {
                emptyWallets.add(gameId);
            }
        }
        boolean needSave = false;
        for (Integer gameId : emptyWallets) {
            getLog().debug("clearEmptyGameWallets: trying remove empty CommonGameWallet, accountId={}, gameId={}", wallet.getAccountId(), gameId);
            boolean removed = wallet.removeGameWalletSafely(gameId);
            if (!needSave && removed) {
                needSave = true;
            }
        }
        if (needSave) {
            DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
        }
    }

    protected abstract void processHandleFailure(AccountInfo accountInfo) throws WalletException;

    protected abstract void processHandleFailure(AccountInfo accountInfo, int gameId,
                                                 boolean forceRefundWithoutGetStatus,
                                                 IExternalWalletTransactionHandler handler)
            throws WalletException;

    @Override
    public boolean isWalletRemovingEnabled() {
        return false;
    }

    @Override
    public void handleFailure(AccountInfo accountInfo, int gameId) throws WalletException {
        handleFailure(accountInfo, gameId, false, null);
    }

    public void handleFailure(AccountInfo accountInfo, int gameId, boolean forceRefundWithoutGetStatus,
                              IExternalWalletTransactionHandler handler) throws WalletException {
        if (!isRegisteredForTracking(accountInfo.getId())) {
            registerForTracking(accountInfo.getId());
        }
        processHandleFailure(accountInfo, gameId, forceRefundWithoutGetStatus, handler);
        SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
        if (sessionInfo == null || sessionInfo.getGameSessionId() == null) {
            unregisterFromTracking(accountInfo.getId());
        }
    }

    protected boolean isRoundFinished(long gameId, CommonWallet cWallet) throws WalletException {
        return cWallet.isGameWalletRoundFinished((int) gameId);
    }

    protected boolean isRegisteredForTracking(long accountId) {
        return walletHelper.isRegisteredForTracking(TRACKER_NAME, accountId);
    }

    protected void registerForTracking(long accountId) {
        walletHelper.registerForTracking(TRACKER_NAME, accountId);
        getLog().debug("registerForTracking accountId: {} registered for tracking on this gameServer", accountId);
    }

    protected void unregisterFromTracking(long accountId) {
        walletHelper.unregisterFromTracking(TRACKER_NAME, accountId);
        getLog().debug("unregisterFromTracking accountId: {} unregistered from tracking", accountId);
    }

    @Override
    public boolean removeWallet(AccountInfo account, int gameId) {
        //nop: not required
        return false;
    }

    @Override
    public void handleDestroyWallet(AccountInfo account, int gameId, GameMode mode, IWallet wallet) throws WalletException {
        if (mode == GameMode.REAL) {
            getLog().debug("handleDestroyWallet: start accountId={}, gameId={}", account.getId(), gameId);
            long now = System.currentTimeMillis();
            CommonWallet cWallet = (CommonWallet) wallet;
            if (cWallet != null) {
                if (gameId > 0) {
                    CommonGameWallet commonGameWallet = cWallet.getGameWallet(gameId);
                    if (commonGameWallet != null) {
                        if (!cWallet.isAnyWalletOperationExist()) {
                            commonGameWallet.resetBetAndWinOperations();
                            cWallet.removeGameWalletSafely(gameId);
                        }
                        commonGameWallet = cWallet.getGameWallet(gameId);
                        if (commonGameWallet != null) {
                            walletHelper.persistGameWallet(account.getId(), commonGameWallet);
                        } else {
                            WalletPersister.getInstance().removeGameWallet(account.getId(), gameId);
                        }
                    } else {
                        WalletPersister.getInstance().removeGameWallet(account.getId(), gameId);
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("handleDestroyWallet 1",
                        System.currentTimeMillis() - now);
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                if (gameSession != null) {
                    getLog().debug("handleDestroyWallet: accountId={}, gameId={}: online player found, skip remove wallet", account.getId(), gameId);
                } else {
                    if (!cWallet.hasGameWallets()) { //remove completely, from trData and C*Persister
                        getLog().debug("handleDestroyWallet: accountId={}, gameId={}: empty wallet found. remove completely", account.getId(), gameId);
                        WalletPersister.getInstance().removeWallet(account.getId());
                    } else {
                        if (!cWallet.isAnyWalletOperationExist()) { //save to persistent store, remove from trData
                            getLog().debug("handleDestroyWallet: accountId={}, gameId={}: wallet operations not found. remove wallet from trData only",
                                account.getId(), gameId);
                            walletHelper.persistCommonWallet(cWallet);
                            SessionHelper.getInstance().getTransactionData().setWallet(null);
                        }
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("handleDestroyWallet 2",
                        System.currentTimeMillis() - now);
            }
        }
    }
}
