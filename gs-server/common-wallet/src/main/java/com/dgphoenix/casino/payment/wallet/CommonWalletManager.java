package com.dgphoenix.casino.payment.wallet;

import com.dgphoenix.casino.common.DomainSession;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.payment.WalletOperationAdditionalProperties;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.DBException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.common.games.IStartGameHelper;
import com.abs.casino.common.games.StartGameHelpers;
import com.dgphoenix.casino.common.promo.PromoWinInfo;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.abs.casino.common.util.NumberUtils;
import com.abs.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletStatusResult;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletWagerResult;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.gs.managers.payment.wallet.IWalletHelper;
import com.dgphoenix.casino.gs.managers.payment.wallet.*;
import com.abs.casino.gs.managers.payment.wallet.v4.CWMType;
import com.dgphoenix.casino.gs.managers.payment.wallet.v4.ICommonWalletClient;
import com.abs.casino.payment.wallet.AbstractWalletProtocolManager;
import com.abs.casino.payment.wallet.client.v4.RESTCWClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * User: flsh
 * Date: 9/27/12
 */
public class CommonWalletManager extends AbstractWalletProtocolManager<CommonWallet, CommonWalletOperation> {
    public static final String JP_BET = "jpContribution";
    public static final String JP_WIN = "jpWin";
    private static final Logger LOG = LogManager.getLogger(CommonWalletManager.class);
    private static final long FUN88_SUBCASINO_ID = 65L;
    private static final long CAKEPOKER_SUBCASINO_ID = 84L;
    private static final String DEFAULT_REVOKE_RECORD = "routine wallet revoke";
    private static final String DEFAULT_NEGATIVE_BET_RECORD = "negative bet";
    private static final String REQUEST_PARAM_CMD = "CMD";
    private final com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient client;
    //this handler used for tracking (transaction failure processing)
    //IExternalWalletTransactionHandler implementations MUST be stateless
    protected IExternalWalletTransactionHandler extHandler;
    protected CWMType cwmType = CWMType.SEND_WIN_ONLY;
    private final boolean persistDailyWalletOperation;
    private final boolean refundSupported;
    private final boolean cw4BonusPartsSupported;
    private final boolean sendAmountInDollars;
    private final boolean notAddNegativeBetToWin;
    private final boolean saveMinMaxWallet;
    private final boolean supportPromoBalanceTransfer;


    public CommonWalletManager(long bankId) throws CommonException {
        super(bankId);
        this.client = instantiateClient();
        this.persistDailyWalletOperation = bankInfo.isDailyWalletOperation();
        this.extHandler = instantiateExtHandler();
        this.refundSupported = bankInfo.isCWRefundSupported();
        this.cw4BonusPartsSupported = bankInfo.isCW4BonusPartsSupported();
        this.sendAmountInDollars = bankInfo.isCWSendAmountInDollars();
        this.notAddNegativeBetToWin = bankInfo.isCWNotAddNegativeBetToWin();
        this.cwmType = CWMType.getCWMTypeByString(bankInfo.getCWMType());
        this.saveMinMaxWallet = bankInfo.isSaveMinMaxWallet();
        this.supportPromoBalanceTransfer = bankInfo.isSupportPromoBalanceTransfer();
    }

    public CommonWalletManager(BankInfo bankInfo, IdGenerator idGenerator) throws CommonException {
        super(bankInfo, idGenerator);
        this.client = instantiateClient();
        this.persistDailyWalletOperation = bankInfo.isDailyWalletOperation();
        this.extHandler = instantiateExtHandler();
        this.refundSupported = bankInfo.isCWRefundSupported();
        this.cw4BonusPartsSupported = bankInfo.isCW4BonusPartsSupported();
        this.sendAmountInDollars = bankInfo.isCWSendAmountInDollars();
        this.notAddNegativeBetToWin = bankInfo.isCWNotAddNegativeBetToWin();
        this.cwmType = CWMType.getCWMTypeByString(bankInfo.getCWMType());
        this.saveMinMaxWallet = bankInfo.isSaveMinMaxWallet();
        this.supportPromoBalanceTransfer = bankInfo.isSupportPromoBalanceTransfer();
    }

    @Override
    public void init(IWalletHelper helper) {
        super.init(helper);
        client.setWalletHelper(helper);
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }

    private static boolean isCDRAsyncOperation(Integer gameId, CommonWalletOperation operation) {
        return operation.getInternalStatus() == WalletOperationStatus.PENDING && gameId == 209;
    }

    protected com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient instantiateClient()
            throws CommonException {
        String klazz = bankInfo.getCWRequestClientClass();
        if (StringUtils.isTrimmedEmpty(klazz)) {
            getLog().error("instantiateClient bankId:{} can't instantiate client", bankInfo.getId());
            throw new CommonException("common wallet request client is empty");
        }

        try {
            Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(klazz);
            Constructor<?> clientConstructor = aClass.getConstructor(long.class);
            return (com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient) clientConstructor.newInstance(bankInfo.getId());
        } catch (Exception e) {
            getLog().error("instantiateClient bankId:{} can't instantiate client", bankInfo.getId(), e);
            throw new CommonException(e);
        }
    }

    protected IExternalWalletTransactionHandler instantiateExtHandler() throws CommonException {
        String klazz = bankInfo.getExternalTransactionHandlerClassName();
        if (!StringUtils.isTrimmedEmpty(klazz)) {
            try {
                Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(klazz);
                Constructor<?> clientConstructor = aClass.getConstructor();
                IExternalWalletTransactionHandler handler = (IExternalWalletTransactionHandler)
                        clientConstructor.newInstance();
                getLog().info("Created ExternalWalletTransactionHandler: {}, for bank={}", handler, bankInfo.getId());
                return handler;
            } catch (Exception e) {
                getLog().error("instantiateExtHandler bankId:{} can't instantiate handler", bankInfo.getId(), e);
                throw new CommonException("Create ExternalWalletTransactionHandler class error", e);
            }
        }
        getLog().info("instantiateExtHandler: handler not defined");
        return null;
    }

    @Override
    public com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient getClient() {
        return client;
    }

    protected boolean isIgnoreRoundFinishedParamOnWager() {
        return client.isIgnoreRoundFinishedParamOnWager();
    }

    protected void debit(long accountId, long gameId, long betAmount, Long mpRoundId, CommonWallet cWallet,
                         CommonWalletOperation operation, AccountInfo accountInfo, IWalletDBLink dbLink)
            throws WalletException {
        CommonWalletOperation previousOperation = cWallet.getGameWalletBetOperation((int) gameId);
        if (previousOperation != null && previousOperation.getId() != operation.getId()) {
            getLog().warn("debit previous wallet operation:{} new operation:{}", previousOperation, operation);
            throw new WalletException("wallet has incomplete operation");
        }

        long now = System.currentTimeMillis();

        StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager debit 1",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        try {
            CommonGameWallet commonGameWallet = cWallet.getGameWallet((int) gameId);
            ClientType clientType = commonGameWallet.getClientType();
            GameSession gameSession = dbLink.getGameSession();
            if (gameSession != null && gameSession.getClientType() != null) {
                clientType = gameSession.getClientType();
                commonGameWallet.setClientType(clientType);
            }
            CommonWalletWagerResult wagerResult = getClient().wager(accountId, accountInfo.getExternalId(),
                    prepareAmount(operation.getAmount(), operation.getId()), "", isIgnoreRoundFinishedParamOnWager() ? null : false, dbLink.getRoundId(),
                mpRoundId, gameId, dbLink.getBankId(), operation, cWallet, clientType, accountInfo.getCurrency());
            StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager debit 2",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();

            operation.setExternalTransactionId(wagerResult.getExtSystemTransactionId());
            getClient().postProcessDebit(accountInfo, gameId, betAmount, cWallet, operation);
            if (wagerResult.isSuccess()) {
                operation.setExternalStatus(WalletOperationStatus.COMPLETED);
                cWallet.increaseBetAmount((int) gameId, operation.getAmount());
                long balance;
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
                if (bankInfo.isStubMode() && bankInfo.isIgnoreStubBalanceFromES()) {
                    getLog().debug("bank {} in stub mode and IgnoreStubBalanceFromES is enabled "
                            + "so ignoring balance from ES", bankInfo.getId());
                    balance = accountInfo.getBalance() - betAmount;
                } else {
                    if (bankInfo.isParseLong()) {
                        balance = (long) wagerResult.getBalance();
                    } else {
                        balance = DigitFormatter.getCentsFromCurrency(wagerResult.getBalance());
                    }
                }
                StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager debit 3",
                        System.currentTimeMillis() - now);
                now = System.currentTimeMillis();

                try { // This is ok, betAmount will be substracted later in dbLink.sendBet
                    accountInfo.setBalance(balance + betAmount);
                } catch (CommonException e) {
                    getLog().error("Cannot set balance in debit", e);
                }
                processBonusParts(wagerResult.getBonusBet(), wagerResult.getBonusWin(), accountInfo,
                        operation.getGameSessionId(), dbLink);
                getClient().postProcessSuccessDebit(accountInfo, gameId, betAmount, cWallet, operation);
                StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager debit 4",
                        System.currentTimeMillis() - now);
                now = System.currentTimeMillis();

                StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager debit 5",
                        System.currentTimeMillis() - now);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("debit set bet wallet balance = {}", balance);
                }
            } else {
                operation.setExternalStatus(WalletOperationStatus.FAIL);
                //need save for prevent additional refund/tracking
                DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
                if (isPersistWalletOperation()) {
                    walletHelper.updateWalletOperation(bankInfo, accountInfo, operation);
                }
                if (wagerResult.isHasResponseCode()) {
                    CWError cwError = null;
                    if (wagerResult.isNumericResponseCode()) {
                        cwError = CommonWalletErrors.getCWErrorByCode(Integer.parseInt(wagerResult.getResponseCode()));
                    }
                    throw new WalletException(accountId, "debit was not approved", wagerResult.getResponseCode(),
                            wagerResult.isNumericResponseCode(), cwError);
                } else {
                    throw new WalletException("debit was not approved");
                }
            }
        } catch (WalletException e) {
            throw e;
        } catch (CommonException e) {
            throw new WalletException(e);
        }
    }

    @Override
    public void handleDebit(long accountId, long originalBetAmount, IWalletDBLink dbLink, SessionInfo sessionInfo,
                            IExternalWalletTransactionHandler extHandler, long mpRoundId) throws WalletException {
        long now = System.currentTimeMillis();
        long betAmount = originalBetAmount;
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleDebit accountId:{}, betAmount:{}, dbLink.getMode()={}, cwmType={}", accountId, betAmount,
                    dbLink.getMode(), cwmType);
        }
        if (dbLink.getMode() != GameMode.REAL) {
            return;
        }
        //if balance less than bet, try use unsended win amount
        if (cwmType.isWinAccumulated()) {
            CommonWallet cWallet = (CommonWallet) dbLink.getWallet();
            getLog().debug("accountId={}, betAmount={}, dbLink={}, sessionInfo={}, cWallet={}", accountId, betAmount, dbLink, sessionInfo, cWallet);
            long accumulatedWinAmount = cWallet.getGameWalletWinAmount((int) dbLink.getGameId());
            getLog().debug("serverBalance={}, accumulatedWinAmount={}", cWallet.getServerBalance(), accumulatedWinAmount);
            if (betAmount > accumulatedWinAmount) {
                cWallet.setGameWalletWinAmount((int) dbLink.getGameId(), 0);
                betAmount = betAmount - accumulatedWinAmount;
                getLog().debug("betAmount corrected: {}", betAmount);
            } else {
                cWallet.setGameWalletWinAmount((int) dbLink.getGameId(), accumulatedWinAmount - betAmount);
                betAmount = 0l;
                getLog().debug("betAmount cleared");
            }
        }
        CommonWallet cWallet = (CommonWallet) dbLink.getWallet();
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleDebit wallet:{}", cWallet);
        }
        if (cWallet == null) {
            throw new WalletException("Wallet is null, accountId=" + accountId);
        }
        long gameId = dbLink.getGameId();
        CommonWalletOperation operation = cWallet.getGameWalletBetOperation((int) gameId);
        if (operation != null) {
            throw new WalletException("previous operation is not completed");
        }
        StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager: handleDebit 1",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        Long roundId = dbLink.getRoundId();
        if (roundId == null) {
            roundId = cWallet.getGameWalletRoundId((int) gameId);
            if (roundId == null) {
                roundId = generateRoundId();
                cWallet.setGameWalletRoundId((int) gameId, roundId);
            }
            dbLink.setRoundId(roundId);
        } else if (gameId == 209) {
            cWallet.setGameWalletRoundId((int) gameId, roundId);
        }
        StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager: handleDebit 2",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();

        if (cWallet.getGameWalletRoundId((int) gameId) == null) {
            cWallet.setGameWalletRoundId((int) gameId, roundId);
        }
        getClient().setAdditionalRoundInfo(accountId, gameId, originalBetAmount, null, cWallet, dbLink);

        long gameSessionId = dbLink.getGameSessionId();

        AccountInfo accountInfo = dbLink.getAccount();
        StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager: handleDebit 3",
                System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        if (betAmount != 0) {
            long walletBetOperationId = generateOperationId();

            if (betAmount > 0) {
                String cmd = dbLink.getRequestParameterValue(REQUEST_PARAM_CMD);
                //for common bet
                operation = createCommonWalletOperation(accountInfo, gameSessionId, roundId, betAmount,
                        WalletOperationType.DEBIT, null, cWallet, gameId, walletBetOperationId,
                        0, sessionInfo.getExternalSessionId(), dbLink, extHandler, cmd);
                ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                DomainSession.getPersister().persistWallet(transactionData);
                try {
                    debit(accountId, gameId, originalBetAmount, mpRoundId, cWallet, operation, accountInfo, dbLink);
                } catch (WalletException e) {
                    CWError walletError = e.getWalletError();
                    if (walletError != null) {
                        if (walletError.needCancelOperation()) {
                            cancelOperation(dbLink, cWallet, (int) gameId, operation, transactionData);
                        }
                    }
                    if (isPersistWalletOperation() && !operation.getExternalStatus().equals(WalletOperationStatus.FAIL)) {
                        walletHelper.updateWalletOperation(bankInfo, accountInfo, operation, true);
                    }
                    throw e;
                }
                StatisticsManager.getInstance().updateRequestStatistics(
                        "CommonWalletManager: handleDebit 5", System.currentTimeMillis() - now);
            } else {
                //for withdrawn bet (craps, ride'm poker)
                //process later, after game logic complete
            }
        } else {
            getLog().warn("handleDebit accountId:{} dbLink:{}} betAmount:{}", accountId, dbLink, betAmount);
        }

    }

    private void cancelOperation(IWalletDBLink dbLink, CommonWallet cWallet, int gameId, CommonWalletOperation operation,
                                 ITransactionData transactionData) throws WalletException {
        cWallet.getGameWallet(gameId).resetOperation(operation);
        DomainSession.getPersister().persistWallet(transactionData);
    }

    @Override
    public void handleNegativeBet(long accountId, long bankId, long betAmount, IWalletDBLink dbLink,
                                  SessionInfo sessionInfo)
            throws WalletException {
        if (dbLink.getMode() == GameMode.REAL) {
            CommonWallet cWallet = (CommonWallet) dbLink.getWallet();
            long gameSessionId = dbLink.getGameSessionId();
            long gameId = dbLink.getGameId();
            CommonWalletOperation operation = cWallet.getGameWalletBetOperation((int) gameId);
            if (operation != null) {
                throw new WalletException("previous operation is not completed");
            }
            AccountInfo accountInfo = dbLink.getAccount();
            Long roundId = dbLink.getRoundId();
            long walletBetOperationId = generateOperationId();
            String cmd = dbLink.getRequestParameterValue(REQUEST_PARAM_CMD);
            operation = createCommonWalletOperation(accountInfo, gameSessionId, roundId, betAmount,
                    WalletOperationType.DEBIT, DEFAULT_NEGATIVE_BET_RECORD, cWallet, gameId,
                    walletBetOperationId, 0, sessionInfo.getExternalSessionId(), dbLink, extHandler, cmd);
            operation.setExternalStatus(WalletOperationStatus.COMPLETED);
            processNegativeDebit(accountId, gameId, betAmount, cWallet);
            DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
        }
    }


    protected String prepareAmount(long amount, long transactionId) {
        String amountPart = sendAmountInDollars ? String.valueOf(NumberUtils.asMoney(amount / 100d)) :
                Long.toString(amount);
        return amountPart + "|" + transactionId;
    }

    protected void processNegativeDebit(long accountId, long gameId, long betAmount, CommonWallet cWallet)
            throws WalletException {
        long lastNegativeBetAmount = Math.abs(betAmount);
        long negativeBetValue = cWallet.getGameWalletNegativeBet((int) gameId);
        cWallet.setGameWalletNegativeBet((int) gameId, negativeBetValue + lastNegativeBetAmount);
        cWallet.setGameWalletLastNegativeBetAmount((int) gameId, lastNegativeBetAmount);
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleDebit negative bet stored in wallet, accountId:{} betAmount = {}, lastNegativeBetAmount = {}, " +
                    "total negative bet amount = {}", accountId, betAmount, lastNegativeBetAmount, cWallet.getGameWalletNegativeBet((int) gameId));
        }
    }

    @Override
    protected boolean isPersistDailyWalletOperation() {
        return persistDailyWalletOperation;
    }

    protected long calculateAmount(long winAmount, long negativeBetAmount) {
        return notAddNegativeBetToWin ? winAmount : winAmount + negativeBetAmount;
    }

    protected boolean isRefundBetSupported() {
        return refundSupported;
    }

    protected boolean processBonusParts(Double _bonusBet, Double _bonusWin, AccountInfo accountInfo,
                                        long gameSessionId, IWalletDBLink dbLink) {
        boolean fullCommitTransaction = false;
        //bonusBet/bonusWin is CW_4 extension, but cannot be supported on other side
        if (isRefundBetSupported() && cw4BonusPartsSupported) {
            try {
                final long bonusBet = _bonusBet == null ? 0 : _bonusBet.longValue();
                final long bonusWin = _bonusWin == null ? 0 : _bonusWin.longValue();
                if (bonusBet != 0 || bonusWin != 0) {
                    GameSession gameSession = dbLink != null ? dbLink.getGameSession() :
                            walletHelper.getOnlineGameSession(gameSessionId);
                    if (gameSession != null) {
                        gameSession.updateBonusBetAndWin(bonusBet, bonusWin);
                        fullCommitTransaction = true;
                    }
                }
            } catch (Exception e) {
                getLog().error("Cannot set real web/win in GameSession", e);
            }
        }
        return fullCommitTransaction;
    }

    @Override
    public boolean credit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished,
                          CommonWallet cWallet, CommonWalletOperation operation, boolean isSyncOperation, long mpRoundId)
            throws WalletException {
        if (operation.getExternalStatus() == WalletOperationStatus.PEENDING_SEND_ALERT) {
            throw new WalletException("Cannot process pending alert, operation has wrong status");
        }
        boolean fullCommitTransaction = false;
        //isAlwaysCompleteFailedCreditOperations() return true ONLY for GSN-clusters and free money
        if (getClient().isAlwaysCompleteFailedCreditOperations() &&
                operation.getExternalStatus() == WalletOperationStatus.FAIL) {
            //GSN mobile case
            if (getLog().isDebugEnabled()) {
                getLog().debug("credit: second call, extStatus=FAIL. Complete without call webservice");
            }
            operation.setExternalStatus(WalletOperationStatus.COMPLETED);
            return false;
        }
        CommonWalletOperation previousOperation = cWallet.getGameWalletWinOperation((int) gameId);
        if (previousOperation != null && previousOperation.getId() != operation.getId()) {
            getLog().warn("credit previous wallet operation:{} new operation:{}", previousOperation, operation);
            throw new WalletException("wallet has incomplete operation");
        }
        long accountId = accountInfo.getId();
        if (isSyncOperation) {
            try {
                CommonGameWallet commonGameWallet = cWallet.getGameWallet((int) gameId);
                ClientType clientType = commonGameWallet.getClientType();
                GameSession gameSession = null;
                try {
                    gameSession = walletHelper.getGameSession(commonGameWallet.getGameSessionId());
                } catch (Exception e) {
                    getLog().error("Cannot load gameSession for restore clientType, gameSessionId={}", commonGameWallet.getGameSessionId());
                }
                if (gameSession != null && gameSession.getClientType() != null) {
                    clientType = gameSession.getClientType();
                    commonGameWallet.setClientType(clientType);
                }
                WalletOperationStatus originalExternalStatus = operation.getExternalStatus();

                CommonWalletWagerResult wagerResult = getClient().wager(accountId, accountInfo.getExternalId(),
                        "", prepareAmount(operation.getAmount(), operation.getId()),
                        isIgnoreRoundFinishedParamOnWager() ? null : isRoundFinished, operation.getRoundId(), mpRoundId,
                    gameId, accountInfo.getBankId(), operation, cWallet, clientType, accountInfo.getCurrency());

                operation.setExternalTransactionId(wagerResult.getExtSystemTransactionId());
                getClient().postProcessCredit(accountInfo, gameId, winAmount, isRoundFinished, cWallet, operation);
                if (wagerResult.isSuccess()) {
                    operation.setExternalStatus(WalletOperationStatus.COMPLETED);
                    long balance;
                    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
                    if (bankInfo.isStubMode() && bankInfo.isIgnoreStubBalanceFromES()) {
                        getLog().debug("bank {} in stub mode and IgnoreStubBalanceFromES is enabled so ignoring balance from ES", bankInfo.getId());
                        balance = accountInfo.getBalance() + winAmount;
                    } else {
                        if (bankInfo.isParseLong()) {
                            balance = (long) wagerResult.getBalance();
                        } else {
                            balance = DigitFormatter.getCentsFromCurrency(wagerResult.getBalance());
                        }
                    }
                    cWallet.setServerBalance(balance);
                    fullCommitTransaction = processBonusParts(wagerResult.getBonusBet(), wagerResult.getBonusWin(),
                            accountInfo, operation.getGameSessionId(), null);
                    getClient().postProcessSuccessCredit(accountInfo, gameId, winAmount, isRoundFinished,
                            cWallet, operation);
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("credit set win wallet balance={}", balance);
                    }
                    if (isRoundFinished) {
                        cWallet.setGameWalletJpContribution((int) gameId, 0);
                        cWallet.clearGameWalletJpWin((int) gameId);
                    }
                    walletHelper.removePromoWin(operation.getRoundId());
                } else {
                    if (operation.getExternalStatus() == originalExternalStatus) {
                        operation.setExternalStatus(WalletOperationStatus.FAIL);
                    }
                    //need save for prevent additional tracking
                    DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
                    if (wagerResult.isHasResponseCode()) {
                        throw new WalletException(accountId, "credit was not approved", wagerResult.getResponseCode(),
                                wagerResult.isNumericResponseCode());
                    } else {
                        throw new WalletException("credit was not approved");
                    }
                }
            } catch (WalletException e) {
                getLog().error("credit ws error on operation:{} e.getErrorCode():{}", operation, e.getErrorCode(), e);
                throw e;
            } catch (CommonException e) {
                getLog().error("credit ws error on operation:{}", operation, e);
                throw new WalletException(e);
            }
        } else {
            operation.setInternalStatus(WalletOperationStatus.PENDING);
            walletHelper.addHighPriorityWalletTrackerTask(accountInfo.getId());
        }
        return fullCommitTransaction;
    }

    protected void credit(AccountInfo accountInfo, long gameId, long winAmount, Boolean isRoundFinished, CommonWallet cWallet,
                          CommonWalletOperation operation) throws WalletException {
        credit(accountInfo, gameId, winAmount, isRoundFinished, cWallet, operation, true, operation.getRoundId());
    }

    @Override
    public void handleCredit(long accountId, boolean isRoundFinished, IWalletDBLink dbLink, SessionInfo sessionInfo,
                             IExternalWalletTransactionHandler extHandler)
            throws WalletException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleCredit accountId={}, isRoundFinished={}", accountId, isRoundFinished);
        }
        if (dbLink.getMode() != GameMode.REAL) {
            return;
        }
        CommonWallet cWallet = (CommonWallet) dbLink.getWallet();

        long gameId = dbLink.getGameId();
        cWallet.setGameWalletRoundFinished((int) gameId, isRoundFinished);

        CommonWalletOperation operation = cWallet.getGameWalletWinOperation((int) gameId);
        if (operation != null) {
            throw new WalletException("previous operation is not completed");
        }

        Long roundId = dbLink.getRoundId();
        if (roundId == null) {
            roundId = cWallet.getGameWalletRoundId((int) gameId);
            if (roundId == null) {
                roundId = generateRoundId();
                cWallet.setGameWalletRoundId((int) gameId, roundId);
            }
            dbLink.setRoundId(roundId);
        }

        long gameSessionId = dbLink.getGameSessionId();
        long currentWinAmount = dbLink.getWinAmount();
        if (currentWinAmount > 0) {
            cWallet.increaseWinAmount((int) gameId, currentWinAmount);
            dbLink.setWinAmount(0L);
        }
        getClient().setAdditionalRoundInfo(accountId, gameId, null, currentWinAmount, cWallet, dbLink);

        long winAmount = cWallet.getGameWalletWinAmount((int) gameId);
        long negativeBetAmount = getNegativeBet(cWallet, (int) gameId);
        final boolean creditCondition = isCreditCondition(winAmount, negativeBetAmount, isRoundFinished, dbLink,
                supportPromoBalanceTransfer ? walletHelper.getPromoWinInfo(roundId) : null);

        AccountInfo accountInfo = dbLink.getAccount();
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleCredit wallet, accountId:{}, getNegativeBet:{}, winAmount:{}, roundId:{}", accountId, negativeBetAmount,
                    winAmount, roundId);
        }

        if (creditCondition) {
            //for withdrawn bet (craps, ride'm poker)
            if (negativeBetAmount > 0) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("handleCredit negative bet handled in wallet, accountId:{}, betAmount:{}, winAmount:{}", accountId,
                            negativeBetAmount, winAmount);
                }
                winAmount = calculateAmount(winAmount, negativeBetAmount);
                eraseNegativeBet(gameId, cWallet);
            }
            long winWalletOperationId = generateOperationId();
            String cmd = dbLink.getRequestParameterValue(REQUEST_PARAM_CMD);
            createCommonWalletOperation(accountInfo, gameSessionId, roundId, winAmount,
                    WalletOperationType.CREDIT, null, cWallet, gameId, winWalletOperationId,
                    negativeBetAmount, sessionInfo.getExternalSessionId(), dbLink, extHandler, cmd);
            getLog().debug("handleCredit sync wallet balance={}", accountInfo.getBalance());
        }
        if (isRoundFinished) {
            if (!creditCondition) {
                cWallet.setGameWalletJpContribution((int) gameId, 0);
                cWallet.clearGameWalletJpWin((int) gameId);
            }
            cWallet.setGameWalletRoundId((int) gameId, null);
            dbLink.setRoundId(null);
        }
        if (creditCondition) {
            cWallet.setGameWalletWinAmount((int) gameId, 0);
        }
        cWallet.setGameWalletBetAmount((int) gameId, 0);
    }

    private boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished, IWalletDBLink dbLink,
                                      PromoWinInfo promoWinInfo) {
        return getClient().isCreditCondition(winAmount, negativeBetAmount, isRoundFinished, dbLink) || promoWinInfo != null;
    }

    protected boolean isSyncOperation(long gameId) {
        return gameId != 209;
    }

    public long getNegativeBet(CommonWallet cWallet, int gameId) throws WalletException {
        Long lastNegativeBetAmount = cWallet.getGameWalletLastNegativeBetAmount(gameId);
        return lastNegativeBetAmount == null ? 0 : lastNegativeBetAmount;
    }

    protected void eraseNegativeBet(long gameId, CommonWallet cWallet) throws WalletException {
        cWallet.setGameWalletNegativeBet((int) gameId, 0L);
        cWallet.setGameWalletLastNegativeBetAmount((int) gameId, null);
    }

    @Override
    protected void processHandleFailure(AccountInfo accountInfo, int gameId, boolean forceRefundWithoutGetStatus,
                                        IExternalWalletTransactionHandler handler) throws WalletException {
        long accountId = accountInfo.getId();
        boolean cdrAsyncOperation = false;
        CommonWallet cWallet = (CommonWallet) WalletPersister.getInstance().getWallet(accountId);
        if (cWallet != null) {
            CommonWalletOperation operation = cWallet.getGameWalletBetOperation(gameId);
            if (operation != null) {
                handleDebitFailure(accountInfo, accountInfo.getBankId(), gameId, cWallet, operation,
                        forceRefundWithoutGetStatus, handler);
            }
            operation = cWallet.getGameWalletWinOperation(gameId);
            if (operation != null) {
                cdrAsyncOperation = isCDRAsyncOperation(gameId, operation);
                if (cdrAsyncOperation) {
                    processCDRAsyncCreditOperation(accountInfo, cWallet, gameId, operation, handler);
                } else {
                    handleCreditFailure(accountInfo, gameId, cWallet, operation);
                }
            }
            if (!cdrAsyncOperation) {
                handleDestroyWallet(accountInfo, -1, GameMode.REAL, cWallet);
            }
        }
    }

    @Override
    protected void processHandleFailure(AccountInfo accountInfo) throws WalletException {
        long accountId = accountInfo.getId();
        boolean cdrAsyncOperation = false;
        CommonWallet cWallet = (CommonWallet) WalletPersister.getInstance().getWallet(accountId);
        if (cWallet != null) {
            WalletException firstException = null;
            if (SessionHelper.getInstance().getTransactionData().getGameSession() == null) {
                clearEmptyGameWallets(cWallet);
            }
            //iterate through copy gameIds required for prevent ConcurrentModificationException
            final Set<Integer> gameIdsKeySet = cWallet.getGameWallets().keySet();
            Set<Integer> gameIds = gameIdsKeySet.isEmpty() ? Collections.emptySet() :
                    new HashSet<>(gameIdsKeySet);
            int successCount = 0;
            for (Integer gameId : gameIds) {
                CommonGameWallet unmodifiedGameWallet = cWallet.getGameWallet(gameId);
                if (unmodifiedGameWallet != null) {
                    unmodifiedGameWallet = new CommonGameWallet(unmodifiedGameWallet);
                }
                try {
                    CommonWalletOperation betOperation = cWallet.getGameWalletBetOperation(gameId);
                    if (betOperation != null && !betOperation.getExternalStatus().equals(
                            WalletOperationStatus.PEENDING_SEND_ALERT)) {
                        handleDebitFailure(accountInfo, accountInfo.getBankId(), gameId, cWallet, betOperation,
                                false, null);
                        successCount++;
                    }
                    CommonWalletOperation winOperation = cWallet.getGameWalletWinOperation(gameId);
                    if (winOperation != null && !winOperation.getExternalStatus().equals(
                            WalletOperationStatus.PEENDING_SEND_ALERT)) {
                        cdrAsyncOperation = isCDRAsyncOperation(gameId, winOperation);
                        if (cdrAsyncOperation) {
                            processCDRAsyncCreditOperation(accountInfo, cWallet, gameId, winOperation, extHandler);
                        } else {
                            handleCreditFailure(accountInfo, gameId, cWallet, winOperation);
                        }
                        successCount++;
                    }
                } catch (Exception e) {
                    if (firstException == null) {
                        if (e instanceof WalletException) {
                            firstException = (WalletException) e;
                        } else {
                            firstException = new WalletException(e);
                        }
                    }
                    cWallet.addGameWallet(unmodifiedGameWallet);
                    getLog().warn("processHandleFailure error, continue tracking other walletOps, accountId={}, gameId={}", accountId, gameId, e);
                }
            }
            if (firstException != null) { //throw for create new tracking task
                if (successCount > 0) {
                    //operation success, need save Wallet for prevent repeated (redudant) tracking if next operation failed
                    DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
                }
                throw firstException;
            }
            if (!cdrAsyncOperation) {
                handleDestroyWallet(accountInfo, -1, GameMode.REAL, cWallet);
            }
            DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
        } else {
            getLog().warn("handleFailure wallet is null for accountId:{}", accountId);
        }
    }

    private void processCDRAsyncCreditOperation(AccountInfo accountInfo, CommonWallet cWallet, Integer gameId,
                                                CommonWalletOperation operation,
                                                IExternalWalletTransactionHandler handler) throws WalletException {
        long accountId = accountInfo.getId();
        if (getLog().isDebugEnabled()) {
            getLog().debug("processCDRAsyncCreditOperation accountId:{}, gameId:{}, currentOperation:{} started", accountId, gameId, operation);
        }
        credit(accountInfo, gameId, operation.getAmount(), isRoundFinished(gameId, cWallet), cWallet, operation);
        completeOperation(accountInfo, gameId, WalletOperationStatus.COMPLETED, cWallet.getGameWalletWithCheck(gameId),
                operation, handler);
        cWallet.updateGameWallet(gameId, 0L, 0L, null);
    }

    protected void handleDebitFailure(AccountInfo accountInfo, long bankId, long gameId, CommonWallet cWallet,
                                      CommonWalletOperation debitOperation, boolean forceRefundWithoutGetStatus,
                                      IExternalWalletTransactionHandler handler) throws WalletException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleDebitFailure accountId:{}, gameId:{}, currentOperation:{} started", accountInfo.getId(), gameId, debitOperation);
        }
        boolean lasthandExist = walletHelper.isLastHandExist(-1, accountInfo.getId(), gameId, null, null);

        if (debitOperation.getExternalStatus() == WalletOperationStatus.FAIL) {
            LOG.info("handleDebitFailure: external status is FAIL, just complete");
            processDFExternalStatusFail(accountInfo, gameId, lasthandExist, cWallet, debitOperation);
        } else {
            boolean transactionSuccess = isRefundBetSupported() || forceRefundWithoutGetStatus ||
                    isTransactionSuccess(accountInfo.getId(), debitOperation.getId(), accountInfo.getExternalId(), bankId,
                            debitOperation);
            if (getLog().isDebugEnabled()) {
                getLog().debug("handleDebitFailure accountId:{}, gameId:{}, wallet operationId:{}, transactionSuccess:{}, isRefundBetSupported={}, " +
                                "lasthandExist={}", accountInfo.getId(), gameId, debitOperation.getId(), transactionSuccess, isRefundBetSupported(),
                        lasthandExist);
            }
            boolean extStatusCompleted = debitOperation.getExternalStatus() == WalletOperationStatus.COMPLETED;
            if (transactionSuccess) {
                debitOperation.setExternalStatus(WalletOperationStatus.COMPLETED);
                cWallet.setNewRound((int) gameId, false);
                processDFExternalStatusCompleted(accountInfo, bankId, gameId, lasthandExist, cWallet,
                        debitOperation, extStatusCompleted ? extHandler : handler);
            } else {
                debitOperation.setExternalStatus(WalletOperationStatus.FAIL);
                processDFExternalStatusFail(accountInfo, gameId, lasthandExist, cWallet, debitOperation);
            }
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleDebitFailure accountId:{}, gameId:{}, wallet operationId:{} completed", accountInfo.getId(), gameId,
                    debitOperation.getId());
        }
    }

    protected void processDFExternalStatusCompleted(AccountInfo accountInfo, long bankId, long gameId,
                                                    boolean lasthandExist, CommonWallet cWallet,
                                                    CommonWalletOperation debitOperation,
                                                    IExternalWalletTransactionHandler handler) throws WalletException {
        final CommonGameWallet commonGameWallet = cWallet.getGameWalletWithCheck((int) gameId);

        if (isRefundBetSupported() || !lasthandExist) {
            revokeDebit(accountInfo, bankId, gameId, cWallet, debitOperation, handler);
        } else {
            long debitOperationAmount = debitOperation.getAmount();
            completeOperation(accountInfo, gameId, WalletOperationStatus.COMPLETED, commonGameWallet,
                    debitOperation, handler);
            if (!isNegativeBetDebit(gameId, cWallet)) {
                //common game
                cWallet.increaseWinAmount((int) gameId, debitOperationAmount);
            } else {
                //for craps and ride'm poker
                rollbackNegativeBet(gameId, cWallet);
            }
        }
    }

    protected void rollbackNegativeBet(long gameId, CommonWallet cWallet) throws WalletException {
        Long lastNegativeBetAmount = cWallet.getGameWalletLastNegativeBetAmount((int) gameId);
        Long negativeBetAmount = cWallet.getGameWalletNegativeBet((int) gameId);

        //here you must never meet NullPointerException!
        cWallet.setGameWalletNegativeBet((int) gameId, negativeBetAmount - lastNegativeBetAmount);
        cWallet.setGameWalletLastNegativeBetAmount((int) gameId, null);
    }

    protected void processCredit(AccountInfo accountInfo, long gameId, Long revokeOperationId,
                                 CommonWallet cWallet, CommonWalletOperation creditOperation,
                                 IExternalWalletTransactionHandler extHandler) throws WalletException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("processCredit accountId:{}, bankId:{}, gameId:{}, wallet:{} revoke operationId:{} using credit operation:{}",
                    accountInfo.getId(), accountInfo.getBankId(), gameId, cWallet, revokeOperationId, creditOperation);
        }
        credit(accountInfo, gameId, creditOperation.getAmount(), isRoundFinished(gameId, cWallet), cWallet,
                creditOperation);

        finalizeCreditOperation(accountInfo, gameId, cWallet, creditOperation, extHandler);
    }

    protected void revokeDebit(AccountInfo accountInfo, long bankId, long gameId, CommonWallet cWallet,
                               CommonWalletOperation debitOperation,
                               IExternalWalletTransactionHandler extHandler)
            throws WalletException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("revokeDebit accountId:{}, bankId:{}, gameId:{}, wallet:{} operation:{}", accountInfo.getId(), bankId, gameId,
                    cWallet, debitOperation);
        }
        LasthandInfo lasthandInfo = walletHelper.getLasthand(-1, accountInfo.getId(), gameId, null, null);
        final String lasthand = lasthandInfo == null ? null : lasthandInfo.getLasthandData();
        final IStartGameHelper helper = StartGameHelpers.getInstance().getHelper(gameId);

        if (!isRefundBetSupported()) {
            Long gameSessionId = debitOperation.getGameSessionId();
            Long roundId = debitOperation.getRoundId();
            long amount = debitOperation.getAmount();
            long operationId = debitOperation.getId();
            final CommonGameWallet commonGameWallet = cWallet.getGameWalletWithCheck((int) gameId);
            //extHandler for debit must be null, this need for prevent complete ExtPaymentTransaction,
            //complete must be on newly created CREDIT Transaction!!!!
            completeOperation(accountInfo, gameId, WalletOperationStatus.COMPLETED, commonGameWallet, debitOperation, null);

            long walletOperationId = generateOperationId();
            String cmd = debitOperation.getCmd();
            CommonWalletOperation creditOperation = createCommonWalletOperation(accountInfo, gameSessionId, roundId,
                    amount, WalletOperationType.CREDIT, null, cWallet, gameId, walletOperationId,
                    0, debitOperation.getExternalSessionId(), null, extHandler, cmd);
            DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
            processCredit(accountInfo, gameId, operationId, cWallet, creditOperation, extHandler);
        } else {
            long operationId = debitOperation.getId();
            try {
                com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient pureClient = getClient();
                if (!(pureClient instanceof ICommonWalletClient)) {
                    throw new CommonException("CWClient is not support refundBet, pureClient=" + pureClient);
                }
                ICommonWalletClient cw4Client =
                        (ICommonWalletClient) pureClient;
                cw4Client.refundBet(debitOperation.getStartTime(), accountInfo.getId(),
                        accountInfo.getExternalId(), debitOperation, gameId);
            } catch (CommonException e) {
                getLog().error("revokeDebit error: accountId={}, operationId={}", accountInfo.getId(), operationId, e);
                throw new WalletException("revokeDebit error", e);
            }
            final CommonGameWallet commonGameWallet = cWallet.getGameWalletWithCheck((int) gameId);
            completeOperation(accountInfo, gameId, WalletOperationStatus.COMPLETED, commonGameWallet, debitOperation,
                    extHandler);
        }
        //call getClient().revokeDebit() for implementing specific integration logic, default implementation is NOP
        getClient().revokeDebit(accountInfo, bankId, gameId, cWallet, debitOperation, extHandler);
        if (getLog().isDebugEnabled()) {
            getLog().debug("revokeDebit accountId:{}, lasthand:{}", accountInfo.getId(), lasthand);
        }
        if (lasthand == null || helper.isRoundFinished(lasthand)) {
            CommonGameWallet gameWallet = cWallet.getGameWallet((int) gameId);
            if (gameWallet != null) {
                gameWallet.setRoundFinished(true);
                gameWallet.setNewRound(true);
                gameWallet.setRoundId(null);
            }
        }
        DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
    }

    @Override
    public void handleCreditCompleted(long accountId, boolean isRoundFinished, IWalletDBLink dbLink,
                                      IExternalWalletTransactionHandler extHandler, long mpRoundId)
            throws WalletException {
        if (dbLink.getMode() != GameMode.REAL) {
            return;
        }
        int gameId = (int) dbLink.getGameId();
        boolean fullCommitTransaction = false;
        CommonWallet cWallet = (CommonWallet) dbLink.getWallet();
        final CommonGameWallet commonGameWallet = cWallet.getGameWalletWithCheck(gameId);
        CommonWalletOperation operation = cWallet.getGameWalletWinOperation(gameId);
        AccountInfo accountInfo = dbLink.getAccount();

        boolean syncOperation = isSyncOperation(gameId);
        if (operation != null) {
            fullCommitTransaction = credit(accountInfo, gameId, 0, isRoundFinished, cWallet, operation, syncOperation, mpRoundId);
            if (getLog().isDebugEnabled()) {
                getLog().debug("handleCredit sync wallet balance={}", accountInfo.getBalance());
            }
        }
        commonGameWallet.setNewRound(isRoundFinished);

        if (syncOperation) {
            if (operation != null) {
                completeOperation(accountInfo, gameId, WalletOperationStatus.COMPLETED, commonGameWallet,
                        operation, extHandler);
            }
        }
        if (fullCommitTransaction) {
            try {
                SessionHelper.getInstance().commitTransaction();
            } catch (DBException ignore) {
                throw new WalletException("Transaction commit failed", ignore);
            }
        } else {
            DomainSession.getPersister().persistWallet(SessionHelper.getInstance().getTransactionData());
        }
    }

    protected void finalizeCreditOperation(AccountInfo accountInfo, long gameId, CommonWallet cWallet,
                                           CommonWalletOperation creditOperation,
                                           IExternalWalletTransactionHandler extHandler)
            throws WalletException {
        final CommonGameWallet commonGameWallet = cWallet.getGameWalletWithCheck((int) gameId);
        completeOperation(accountInfo, gameId, WalletOperationStatus.COMPLETED, commonGameWallet, creditOperation,
                extHandler);
        if (isRoundFinished(gameId, cWallet)) {
            cWallet.setGameWalletRoundId((int) gameId, null);
            cWallet.setGameWalletJpContribution((int) gameId, 0);
            cWallet.clearGameWalletJpWin((int) gameId);
            cWallet.removeGameWalletSafely((int) gameId);
        } else {
            cWallet.updateGameWallet((int) gameId, 0L, 0L);
        }
    }

    protected boolean isNegativeBetDebit(long gameId, CommonWallet cWallet) {
        return cWallet.getGameWalletLastNegativeBetAmount((int) gameId) != null;
    }

    protected void processDFExternalStatusFail(AccountInfo accountInfo, long gameId, boolean lasthandExist,
                                               CommonWallet cWallet, CommonWalletOperation debitOperation)
            throws WalletException {
        final CommonGameWallet commonGameWallet = cWallet.getGameWalletWithCheck((int) gameId);
        completeOperation(accountInfo, gameId, WalletOperationStatus.FAIL, commonGameWallet, debitOperation,
                extHandler);
        if (!lasthandExist) {
            cWallet.setGameWalletRoundId((int) gameId, null);
            cWallet.removeGameWalletSafely((int) gameId);
        }
    }

    protected void handleCreditFailure(AccountInfo accountInfo, long gameId, CommonWallet cWallet,
                                       CommonWalletOperation creditOperation) throws WalletException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleCreditFailure accountId:{}, gameId:{} wallet currentOperation:{}", accountInfo.getId(), gameId, creditOperation);
        }
        long operationId = creditOperation.getId();
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleCreditFailure accountId:{}, gameId:{}, wallet operationId:{}, externalStatus:{}", accountInfo.getId(), gameId,
                    operationId, creditOperation.getExternalStatus());
        }

        if (creditOperation.getExternalStatus().equals(WalletOperationStatus.COMPLETED)) {
            processCFExternalStatusCompleted(accountInfo, gameId, cWallet, creditOperation, extHandler);
        } else {
            creditOperation.setExternalStatus(WalletOperationStatus.FAIL);
            try {
                processCredit(accountInfo, gameId, null, cWallet, creditOperation, extHandler);
            } catch (WalletException e) {
                boolean needRethrow = getClient().postProcessCreditException(e, cWallet, creditOperation,
                        accountInfo, gameId);
                if (needRethrow) {
                    throw e;
                } else {
                    if (creditOperation.getExternalStatus() == WalletOperationStatus.COMPLETED) {
                        finalizeCreditOperation(accountInfo, gameId, cWallet, creditOperation, extHandler);
                    } else if (creditOperation.getExternalStatus() == WalletOperationStatus.PEENDING_SEND_ALERT) {
                        getLog().debug("Operation with external status PEENDING_SEND_ALERT cannot be completed: {}", creditOperation);
                    } else {
                        getLog().debug("Possible error: postProcessCreditException return false, but transaction " +
                                "is not completed, creditOperation: {}, throw original exception", creditOperation);
                        throw e;
                    }
                }
            }
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("handleCreditFailure accountId:{}, gameId:{}, wallet operationId:{} completed", accountInfo.getId(), gameId, operationId);
        }
    }

    protected void processCFExternalStatusCompleted(AccountInfo accountInfo, long gameId, CommonWallet cWallet,
                                                    CommonWalletOperation creditOperation,
                                                    IExternalWalletTransactionHandler extHandler) throws WalletException {
        finalizeCreditOperation(accountInfo, gameId, cWallet, creditOperation, extHandler);
    }

    @Override
    public boolean isWalletRemovingEnabled() {
        return true;
    }

    protected long getGameWalletRevokeAmount(long gameId, CommonWallet cWallet) throws WalletException {
        long negativeBet = cWallet.getGameWalletNegativeBet((int) gameId);
        long winAmount = cWallet.getGameWalletWinAmount((int) gameId);
        return winAmount + negativeBet;
    }

    protected boolean isTransactionSuccess(long accountId, long walletOperationId, String extUserId, long bankId,
                                           CommonWalletOperation operation)
            throws WalletException {
        try {
            CommonWalletStatusResult result = getClient().getExternalTransactionStatus(accountId, extUserId,
                    walletOperationId, bankId, operation);
            return result.isSuccess();
        } catch (CommonException e) {
            getLog().error("isTransactionSuccess error externalId:{}, walletOperationId:{}", extUserId, walletOperationId);
            throw new WalletException(e);
        }
    }

    public void completeOperation(AccountInfo accountInfo, long gameId, WalletOperationStatus internalStatus,
                                  CommonGameWallet gameWallet, CommonWalletOperation operation,
                                  IExternalWalletTransactionHandler extHandler)
            throws WalletException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("completeOperation gameId:{}, internalStatus:{}, gameWallet:{}, operation:{}", gameId, internalStatus,
                    gameWallet, operation);
        }
        getClient().completeOperation(accountInfo, gameId, internalStatus, gameWallet, operation, extHandler);
        operation.update(internalStatus, System.currentTimeMillis());
        if (operation.getExternalStatus() == WalletOperationStatus.COMPLETED &&
                operation.getInternalStatus() == WalletOperationStatus.COMPLETED &&
                !StringUtils.isTrimmedEmpty(operation.getExternalTransactionId()) &&
                !"null".equals(operation.getExternalTransactionId())) {
            setMinMaxWallet(operation, accountInfo);
        }
        if (isPersistWalletOperation()) {
            walletHelper.updateWalletOperation(bankInfo, accountInfo, operation);
        }
        if (persistDailyWalletOperation) {
            BaseGameInfo gameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameId);
            String gameName = MessageManager.getInstance().getApplicationMessage("game.name." + gameInfo.getName());
            WalletOperationAdditionalProperties.getInstance().setAdditionalProperties(operation,
                    accountInfo.getCurrency().getCode(), gameName, gameInfo.getGroup().getGroupName(),
                    accountInfo.getExternalId());
            walletHelper.persistDailyWallet(operation, accountInfo.getBankId());
        }
        if (extHandler != null) {
            extHandler.operationCompleted(operation, gameId);
        }
        gameWallet.resetOperation(operation);
    }

    private void setMinMaxWallet(CommonWalletOperation operation, AccountInfo accountInfo) {
        short subCasinoId = accountInfo.getSubCasinoId();
        if (subCasinoId == FUN88_SUBCASINO_ID || subCasinoId == CAKEPOKER_SUBCASINO_ID || saveMinMaxWallet) {
            long now = System.currentTimeMillis();
            walletHelper.persistMinMaxWallet(subCasinoId,
                    operation.getEndTime(), accountInfo.getBankId(),
                    accountInfo.getExternalId(), operation.getId(), operation.getRoundId(),
                    operation.getGameSessionId(), operation.getExternalTransactionId(),
                    operation.getEndTime());
            StatisticsManager.getInstance().updateRequestStatistics("setMinMaxWallet", System.currentTimeMillis() - now);
        }
    }

    protected CommonWalletOperation createCommonWalletOperation(AccountInfo accountInfo, long gameSessionId,
                                                                long roundId, long amount,
                                                                WalletOperationType type, String description,
                                                                CommonWallet cWallet, long gameId,
                                                                long walletOperationId, long negativeBet,
                                                                String externalSessionId, IWalletDBLink dbLink,
                                                                IExternalWalletTransactionHandler extHandler,
                                                                String cmd)
            throws WalletException {

        long now = System.currentTimeMillis();
        CommonWalletOperation commonWalletOperation;
        if (walletOperationId <= 0) {
            throw new WalletException("Bad wallet operation id: " + walletOperationId);
        }
        try {
            commonWalletOperation = cWallet.createCommonWalletOperation(walletOperationId, accountInfo.getId(),
                    gameSessionId, roundId, amount, type, description, WalletOperationStatus.STARTED,
                    WalletOperationStatus.STARTED, (int) gameId,
                    WalletOperationType.CREDIT == type ? negativeBet : 0, externalSessionId);
            commonWalletOperation.setCmd(cmd);
            if (dbLink != null) { //dbLink may be null if player offline
                dbLink.setLastPaymentOperationId(walletOperationId);
                setAdditionalOperationProperties(commonWalletOperation, dbLink);
            }
            if (getBankInfo().isSaveAndSendTokenInGameWallet() && getBankInfo().isAddTokenMode()) {
                HashMap<String, String> properties = new HashMap<>();
                properties.put(RESTCWClient.PARAM_TOKEN, accountInfo.getFinsoftSessionId());
                WalletOperationAdditionalProperties.getInstance().addAdditionalProperties(commonWalletOperation, properties);
            }
            if (extHandler != null) {
                extHandler.operationCreated(commonWalletOperation);
            }
            if (isPersistWalletOperation()) {
                walletHelper.persistWalletOperation(bankInfo, accountInfo, commonWalletOperation, gameId, accountInfo.getBalance());
            }
            StatisticsManager.getInstance().updateRequestStatistics("CommonWalletManager: createCommonWalletOperation",
                    System.currentTimeMillis() - now);
        } catch (Exception e) {
            throw new WalletException("Cannot persist", e);
        }
        return commonWalletOperation;
    }

    protected void setAdditionalOperationProperties(CommonWalletOperation operation, IWalletDBLink dbLink) {
        getClient().setAdditionalOperationProperties(operation, dbLink);
    }
}
