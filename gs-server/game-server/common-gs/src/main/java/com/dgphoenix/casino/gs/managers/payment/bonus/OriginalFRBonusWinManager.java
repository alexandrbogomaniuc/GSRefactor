package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraFRBonusWinPersister;
import com.dgphoenix.casino.common.DomainSession;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.abs.casino.common.cache.data.payment.bonus.CommonFRBonusWin;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.logkit.LogUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.managers.dblink.FRBonusDBLink;
import com.abs.casino.gs.managers.payment.bonus.client.frb.FRBonusWinResult;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.ILoggableCWClient;
import com.dgphoenix.casino.gs.managers.payment.wallet.SimpleLoggableContainer;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.Set;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

public class OriginalFRBonusWinManager extends AbstractFRBonusWinManager<FRBonusWin, FRBWinOperation> {

    private static final Logger LOG = LogManager.getLogger(OriginalFRBonusWinManager.class);
    private static final String CM_SUSPENDED_FRB_WIN_ALERTS_REPORT = "/reports/suspendedFRBWinAlerts/complete?";

    private final IFRBonusClient client;
    private final boolean persistFRBWinOperation;
    private final boolean sendZeroFrbWin;
    private final boolean sendZeroBetOnFrbWin;
    private final boolean sendNotZeroFrbBet;

    private final CassandraFRBonusWinPersister frBonusWinPersister;
    private final GameServerConfiguration gameServerConfiguration;
    private final CurrencyRatesManager currencyRatesManager;

    public OriginalFRBonusWinManager(long bankId) throws CommonException {
        super(bankId);
        this.client = instantiateClient(bankId);
        if (!isTrimmedEmpty(BankInfoCache.getInstance().getBankInfo(bankId).getPendingOperationMailList()) && client instanceof ILoggableCWClient) {
            ((ILoggableCWClient)client).setLoggableContainer(new SimpleLoggableContainer());
        }
        persistFRBWinOperation = !BankInfoCache.getInstance().getBankInfo(bankId).isNotPersistFRBWinOps();
        sendZeroFrbWin = BankInfoCache.getInstance().getBankInfo(bankId).isSendZeroFrbWin();
        sendZeroBetOnFrbWin = BankInfoCache.getInstance().getBankInfo(bankId).isSendZeroBetOnFrbWin();
        sendNotZeroFrbBet = BankInfoCache.getInstance().getBankInfo(bankId).isSendNotZeroFrbBet();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        frBonusWinPersister = persistenceManager.getPersister(CassandraFRBonusWinPersister.class);
        gameServerConfiguration = ApplicationContextHelper.getApplicationContext()
                .getBean("gameServerConfiguration", GameServerConfiguration.class);
        currencyRatesManager = ApplicationContextHelper.getBean(CurrencyRatesManager.class);
    }

    protected IFRBonusClient instantiateClient(long bankId) throws CommonException {
        String klazz = BankInfoCache.getInstance().getBankInfo(bankId).getBonusFRRequestClientClass();
        if (isTrimmedEmpty(klazz)) {
            LOG.error("instantiateClient bankId:{} can't instantiate client", bankId);
            throw new CommonException("bonus request client is empty");
        }
        try {
            Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(klazz);
            Constructor<?> clientConstructor = aClass.getConstructor(long.class);
            return (IFRBonusClient) clientConstructor.newInstance(bankId);
        } catch (Exception e) {
            LOG.error("instantiateClient bankId:{} can't instantiate client", bankId, e);
            throw new CommonException(e);
        }
    }

    public IFRBonusClient getClient() {
        return client;
    }


    @Override
    public void handleCreateFRBonusWin(AccountInfo accountInfo, long gameSessionId, long gameId) throws FRBException {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        FRBonusWin frbonusWin = transactionData.getFrbWin();
        boolean newFRBWin = false;
        if (frbonusWin == null) {
            frbonusWin = new FRBonusWin(accountInfo.getId());
            transactionData.setFrbWin(frbonusWin);
            newFRBWin = true;
        }
        if (newFRBWin || !frbonusWin.isGameFRBWinExist(gameId)) {
            frbonusWin.createFRBonusWin(gameId, gameSessionId);
        } else {
            frbonusWin.setFRBonusWinGameSessionId(gameId, gameSessionId);
        }
    }

    @Override
    protected void processHandleFailure(AccountInfo accountInfo) throws FRBException {
        long accountId = accountInfo.getId();
        FRBonusWin frBonusWin = SessionHelper.getInstance().getTransactionData().getFrbWin();
        if (frBonusWin != null) {
            FRBException firstException = null;
            int successCount = 0;
            for (String s : frBonusWin.getFRBonusWins().keySet()) {
                long gameId = Long.parseLong(s);
                CommonFRBonusWin unmodifiedFRBonusWin = frBonusWin.getFRBWin(gameId);
                if (unmodifiedFRBonusWin != null) {
                    frBonusWin.addFRBonusWin(unmodifiedFRBonusWin.copy());
                }
                FRBWinOperation operation = frBonusWin.getFRBonusWinOperation(gameId);
                if (operation != null) {
                    long bankId = accountInfo.getBankId();
                    try {
                        handleCreditFailure(accountId, bankId, gameId, frBonusWin, operation);
                        successCount++;
                    } catch (Exception e) {
                        if (firstException == null) {
                            if (e instanceof FRBException) {
                                firstException = (FRBException) e;
                            } else {
                                firstException = new FRBException(e);
                            }
                        }
                        frBonusWin.addFRBonusWin(unmodifiedFRBonusWin);
                        LOG.warn("processHandleFailure error, continue tracking other frbOps, accountId={}, gameId={}", accountId, gameId, e);
                    }
                }
            }
            if (firstException != null) { //throw for create new tracking task
                if (successCount > 0) {
                    //operation success, need save Wallet for prevent repeated (redudant) tracking if next operation failed
                    DomainSession.getPersister().persistFrbWin(SessionHelper.getInstance().getTransactionData());
                }
                throw firstException;
            }
            handleDestroyFRBonusWin(accountInfo, null);
        } else {
            LOG.warn("handleFailure frbonusWin is null for accountId:{}", accountId);
        }
    }

    @Override
    public void handleDestroyFRBonusWin(AccountInfo account, Long gameId) {

        long now = System.currentTimeMillis();
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        FRBonusWin frbonusWin = transactionData.getFrbWin();
        FRBonus frBonus = transactionData.getFrBonus();
        StatisticsManager.getInstance().updateRequestStatistics("handleDestroyFrbonusWin 1",
                System.currentTimeMillis() - now);
        SessionInfo playerSession = transactionData.getPlayerSession();
        if (frbonusWin != null) {
            now = System.currentTimeMillis();
            if (gameId != null) {
                frbonusWin.removeFRBonusWinSafely(gameId);
            } else {
                LOG.debug("handleDestroyFRBonusWin: found entire request, check all games. accountId={}", account.getId());
                GameSession gameSession = transactionData.getGameSession();
                Long activeGameId = gameSession != null ? gameSession.getGameId() : null;
                Set<String> gameIds = frbonusWin.getFRBonusWins().keySet();
                for (String sGameId : gameIds) {
                    long currentGameId = Long.parseLong(sGameId);
                    if (activeGameId != null && activeGameId.equals(currentGameId)) {
                        LOG.debug("handleDestroyFRBonusWin: found active game (skip): {}", activeGameId);
                        continue;
                    }
                    CommonFRBonusWin commonFrbWin = frbonusWin.getFRBWin(currentGameId);
                    if (commonFrbWin != null && commonFrbWin.getOperation() == null && frBonus != null &&
                            frBonus.getStatus() == BonusStatus.CLOSED && commonFrbWin.getRoundId() != null) {
                        //fix unfinished round for safe remove
                        commonFrbWin.setRoundId(null);
                    }
                    frbonusWin.removeFRBonusWinSafely(currentGameId);
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics("handleDestroyFrbonusWin 2",
                    System.currentTimeMillis() - now);
            now = System.currentTimeMillis();
            if (playerSession == null && !frbonusWin.hasAnyOperation()) {
                if (!frbonusWin.hasGameFRBonusWin()) {
                    LOG.info("handleDestroyFRBonusWin: frbWinOperations is not found and player logged out, " +
                            "set FrbWin to null in transactionData and CassandraFRBonusWinPersister: accountId={}", account.getId());
                    frBonusWinPersister.delete(account.getId());
                } else {
                    LOG.info("handleDestroyFRBonusWin: FRBonusWin is not found and player logged out, " +
                            "set FrbWin to null in transactionData and save to CassandraFRBonusWinPersister: accountId={}", account.getId());
                    frBonusWinPersister.persist(frbonusWin);
                }
                transactionData.setFrbWin(null);
            }
            StatisticsManager.getInstance().updateRequestStatistics("handleDestroyFrbonusWin 3 (remove)",
                    System.currentTimeMillis() - now);
        }
    }

    protected void handleCreditFailure(long accountId, long bankId, long gameId, FRBonusWin frbonusWin,
                                       FRBWinOperation creditOperation) throws FRBException {
        LOG.debug("handleCreditFailure accountId:{} gameId:{} frbonusWin currentOperation:{}", accountId, gameId, creditOperation);

        long operationId = creditOperation.getId();

        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
        if (accountInfo == null) {
            throw new FRBException("Account not found: " + accountId);
        }

        FRBWinOperationStatus externalStatus = creditOperation.getExternalStatus();
        if (externalStatus != FRBWinOperationStatus.COMPLETED) {
            if (externalStatus == FRBWinOperationStatus.FAIL || externalStatus == FRBWinOperationStatus.STARTED) {
                processCredit(accountId, bankId, gameId, null, accountInfo, frbonusWin, creditOperation);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(LogUtils.markException("handleCreditFailure accountId:" +
                            accountId + " gameId:" + gameId + " bankId:" + bankId +
                            " frbonusWin:" + frbonusWin.getFRBWin(gameId) +
                            " status:" + externalStatus + ", default behaviour"));
                }
            }
        } else {
            //external status completed, just save to DB
            processCFExternalStatusCompleted(gameId, frbonusWin, creditOperation);
        }
        LOG.debug("handleCreditFailure accountId:{} gameId:{} frbonusWin operationId:{} completed", accountId, gameId, operationId);
    }

    protected void processCFExternalStatusCompleted(long gameId, FRBonusWin frbonusWin,
                                                    FRBWinOperation creditOperation) throws FRBException {
        finalizeCreditOperation(gameId, frbonusWin, creditOperation);
    }

    private void processCredit(long accountId, long bankId, long gameId, Long revokeOperationId,
                               AccountInfo accountInfo, FRBonusWin frbonusWin,
                               FRBWinOperation creditOperation) throws FRBException {
        LOG.debug("processCredit accountId:{} bankId:{} gameId:{} frbonusWin:{} revoke operationId:{} using credit operation:{}",
                accountId, bankId, gameId, frbonusWin, revokeOperationId, creditOperation);

        credit(accountId, gameId, creditOperation.getAmount(), isRoundFinished(gameId, frbonusWin), frbonusWin,
                creditOperation, accountInfo);
        finalizeCreditOperation(gameId, frbonusWin, creditOperation);
    }

    protected void finalizeCreditOperation(long gameId, FRBonusWin frBonusWin, FRBWinOperation creditOperation)
            throws FRBException {
        completeOperation(gameId, FRBWinOperationStatus.COMPLETED, frBonusWin, creditOperation);
        if (isRoundFinished(gameId, frBonusWin)) {
            frBonusWin.setGameFRBonusWinRoundId(gameId, null);
            frBonusWin.removeFRBonusWinSafely(gameId);
        } else {
            frBonusWin.setNewRound(gameId, false);
            frBonusWin.updateGameFRBonusWin(gameId, 0);
        }
    }

    protected void completeOperation(long gameId, FRBWinOperationStatus internalStatus,
                                     FRBonusWin frbWin, FRBWinOperation operation) throws FRBException {
        LOG.debug("completeOperation: gameId:{} internalStatus:{} frbWin:{} operation:{}", gameId, internalStatus, frbWin, operation);
        operation.update(internalStatus, System.currentTimeMillis());
        if (persistFRBWinOperation) {
            FRBonusWinRequestFactory.getInstance().save(operation);
        }
        frbWin.setFRBonusWinOperation(gameId, null);
    }

    @Override
    public void handleCreditCompleted(long accountId, boolean isRoundFinished, FRBonusDBLink dbLink)
            throws FRBException {

        long gameId = dbLink.getGameId();

        FRBonusWin frBonusWin = (FRBonusWin) dbLink.getFrbonusWin();
        FRBWinOperation operation = frBonusWin.getCurrentFRBonusWinOperation(gameId);

        if (operation != null) {
            completeOperation(gameId, FRBWinOperationStatus.COMPLETED, frBonusWin, operation);
        }
        if (isRoundFinished) {
            frBonusWin.updateGameFRBonusWin(gameId, 0L, null);
            dbLink.setRoundId(null);
        } else {
            frBonusWin.updateGameFRBonusWin(gameId, 0L);
        }
        frBonusWin.setNewRound(gameId, isRoundFinished);
    }

    @Override
    protected void processRealDebitCompleted(FRBonusDBLink dbLink) throws FRBException {

        long gameId = dbLink.getGameId();
        long now = System.currentTimeMillis();
        FRBonusWin frBonusWin = (FRBonusWin) dbLink.getFrbonusWin();
        StatisticsManager.getInstance().updateRequestStatistics("OriginalFRBonusWinManager processRealDebitCompleted 1",
                System.currentTimeMillis() - now);

        now = System.currentTimeMillis();
        long winAmount = dbLink.getWinAmount();
        if (winAmount > 0) {
            frBonusWin.increaseWinAmount(gameId, winAmount);
            SessionHelper.getInstance().getDomainSession().persistFrbWin();
        }
        dbLink.setWinAmount(0L);
        StatisticsManager.getInstance().updateRequestStatistics("OriginalFRBonusWinManager processRealDebitCompleted 2",
                System.currentTimeMillis() - now);
    }


    private boolean isRoundFinished(long gameId, FRBonusWin frbonusWin) throws FRBException {
        return frbonusWin.isRoundFinished(gameId);
    }

    protected void credit(long accountId, long gameId, long winAmount, Boolean isRoundFinished, FRBonusWin frBonusWin,
                          FRBWinOperation operation, AccountInfo accountInfo)
            throws FRBException {
        if (operation.getExternalStatus() == FRBWinOperationStatus.PEENDING_SEND_ALERT) {
            throw new FRBException("Cannot process pending alert, operation has wrong status");
        }
        FRBWinOperation previousOperation = frBonusWin.getFRBonusWinOperation(gameId);
        if (previousOperation != null && previousOperation.getId() != operation.getId()) {
            LOG.debug("credit previous frbonusWin operation:{}, new operation:{}", previousOperation, operation);
            throw new FRBException("FRB Win has incomplete operation");
        }

        FRBWinOperationStatus originalExternalStatus = operation.getExternalStatus();
        try {
            FRBonus frBonus = getFRBonus(accountId, operation.getBonusId());
            if (WalletProtocolFactory.getInstance().isWalletBank(accountInfo.getBankId())) {
                IFRBonusClient frbRestClient = getClient();
                String extGameId = BaseGameCache.getInstance().getExternalGameId(gameId, accountInfo.getBankId());
                if (isTrimmedEmpty(extGameId)) {
                    extGameId = String.valueOf(gameId);
                }
                FRBonusWinResult frBonusWinResult = frbRestClient.bonusWin(accountInfo.getId(), accountInfo.getExternalId(), isRoundFinished,
                        operation.getBonusId(), frBonus.getExtId(), winAmount, operation, gameId, extGameId, frBonusWin);
                if (frBonusWinResult.isSuccess()) {
                    operation.setExternalStatus(FRBWinOperationStatus.COMPLETED);
                    frBonusWin.setServerBalance(frBonusWinResult.getBalance());
                    LOG.debug("credit set win frbWin balance={}", frBonusWin.getServerBalance());
                } else {
                    throw new FRBException("credit was not approved");
                }
            } else {
                operation.setExternalStatus(FRBWinOperationStatus.COMPLETED);
                frBonusWin.setServerBalance(accountInfo.getBalance());
            }
        } catch (FRBException e) {
            if (operation.getExternalStatus() == originalExternalStatus) {
                operation.setExternalStatus(FRBWinOperationStatus.FAIL);
            }
            LOG.error("credit ws error on operation (FRB round):{}", operation, e);
            throw e;
        } catch (CommonException e) {
            operation.setExternalStatus(FRBWinOperationStatus.FAIL);
            LOG.error("credit ws error on operation:{}", operation, e);
            throw new FRBException(e);
        } finally {
            if (operation.getExternalStatus() == FRBWinOperationStatus.FAIL
                    || operation.getExternalStatus() != originalExternalStatus) {
                //need save correct status for distinguish failed operations from started (not tracked)
                SessionHelper.getInstance().getDomainSession().persistFrbWin();
            }
        }
    }

    protected FRBonus getFRBonus(long accountId, long bonusId) throws FRBException {
        FRBonus frBonus = SessionHelper.getInstance().getTransactionData().getFrBonus();
        if (frBonus == null || frBonus.getId() != bonusId) {
            frBonus = FRBonusManager.getInstance().getById(bonusId);
        }
        if (frBonus == null) {
            frBonus = FRBonusManager.getInstance().getArchivedFRBonusById(bonusId);
        }
        if (frBonus == null) {
            throw new FRBException("FRBonus is null, accountId=" + accountId);
        }
        return frBonus;
    }


    @Override
    public void handleMPGameCredit(AccountInfo accountInfo, boolean isRoundFinished, long gameId,
                                   long gameSessionId, long bonusId, SessionInfo sessionInfo, long winAmount) throws FRBException {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        FRBonusWin frbonusWin = transactionData.getFrbWin();
        if (frbonusWin == null) {
            LOG.warn("FRBonusWin not found for account: {}", accountInfo.getId());
            throw new FRBException("FRBonusWin is null");
        }
        frbonusWin.setGameFRBWinRoundFinished(gameId, isRoundFinished);
        FRBWinOperation operation = frbonusWin.getFRBonusWinOperation(gameId);
        if (operation != null) {
            throw new FRBException("previous operation is not completed");
        }
        long roundId = generateRoundId();
        frbonusWin.increaseWinAmount(gameId, winAmount);
        LOG.debug("handleMPGameCredit frbonusWin, accountId:{} winAmount:{} roundId:{}", accountInfo.getId(), winAmount, roundId);
        if (isCreditCondition(gameId, frbonusWin, isRoundFinished)) {
            if (sessionInfo == null) {
                LOG.warn("SessionInfo not found for account: {}", accountInfo.getId());
            }
            String extSessionId = sessionInfo != null ? sessionInfo.getExternalSessionId() : null;
            operation = createFRBWinOperation(accountInfo.getId(), gameSessionId, roundId, winAmount, 0, null, frbonusWin,
                    gameId, extSessionId, bonusId);
            if (sessionInfo != null) {
                operation.setClientType(sessionInfo.getClientType());
            }
            processCredit(accountInfo.getId(), gameId, isRoundFinished, frbonusWin, operation, accountInfo);
            completeOperation(gameId, FRBWinOperationStatus.COMPLETED, frbonusWin, operation);
        }
        frbonusWin.updateGameFRBonusWin(gameId, 0L, null);
    }

    @Override
    public void handleCredit(long accountId, boolean isRoundFinished, FRBonusDBLink dbLink) throws FRBException {
        FRBonusWin frbonusWin = (FRBonusWin) dbLink.getFrbonusWin();
        if (frbonusWin == null) {
            LOG.warn("FRBonusWin not found for account: {}, dbLink={}", accountId, dbLink);
            throw new FRBException("FRBonusWin is null");
        }

        long gameId = dbLink.getGameId();
        frbonusWin.setGameFRBWinRoundFinished(gameId, isRoundFinished);

        FRBWinOperation operation = frbonusWin.getFRBonusWinOperation(gameId);
        if (operation != null) {
            throw new FRBException("previous operation is not completed");
        }

        Long roundId = dbLink.getRoundId();
        if (roundId == null) {
            roundId = frbonusWin.getGameFRBWinRoundId(gameId);
            if (roundId == null) {
                roundId = generateRoundId();
                frbonusWin.setGameFRBonusWinRoundId(gameId, roundId);
            }
            dbLink.setRoundId(roundId);
        }

        long gameSessionId = dbLink.getGameSessionId();
        long winAmount = frbonusWin.getGameFRBonusWinWinAmount(gameId);

        AccountInfo accountInfo = dbLink.getAccount();
        LOG.debug("handleCredit frbonusWin, accountId:{} winAmount:{} roundId:{}", accountId, winAmount, roundId);

        if (isCreditCondition(gameId, frbonusWin, isRoundFinished) || (dbLink.getCurrentBet() > 0 && sendNotZeroFrbBet)) {
            //for withdrawn bet (craps, ride'm poker)
            SessionInfo sessionInfo = dbLink.getSessionInfo();
            if (sessionInfo == null) {
                LOG.warn("SessionInfo not found for account: {}", accountId);
            }
            String extSessionId = sessionInfo != null ? sessionInfo.getExternalSessionId() : null;
            operation = createFRBWinOperation(accountId, gameSessionId, roundId, winAmount, dbLink.getCurrentBet(), null, frbonusWin,
                    gameId, extSessionId, dbLink.getBonusId());
            if (sessionInfo != null) {
                operation.setClientType(sessionInfo.getClientType());
            }

            processCredit(accountId, gameId, isRoundFinished, frbonusWin, operation, accountInfo);
        }
    }

    protected void processCredit(long accountId, long gameId, boolean isRoundFinished, FRBonusWin frbonusWin,
                                 FRBWinOperation operation, AccountInfo accountInfo) throws FRBException {
        //need save created operation for tracking
        SessionHelper.getInstance().getDomainSession().persistFrbWin();
        credit(accountId, gameId, operation.getAmount(), isRoundFinished, frbonusWin, operation, accountInfo);

        try {
            accountInfo.setBalance(frbonusWin.getServerBalance());
        } catch (CommonException e) {
            LOG.error("Cannot set balance", e);
        }
        LOG.debug("handleCredit sync frbonusWin balance = {}", accountInfo.getBalance());
    }

    protected boolean isCreditCondition(long gameId, FRBonusWin frbonusWin, boolean isRoundFinished)
            throws FRBException {

        if (frbonusWin.getGameFRBonusWinWinAmount(gameId) > 0) {
            return true;
        }
        if (isRoundFinished && (sendZeroFrbWin || sendZeroBetOnFrbWin)) {
            return true;
        }
        return false;
    }

    protected FRBWinOperation createFRBWinOperation(long accountId, long gameSessionId, long roundId, long amount, long betAmount,
                                                    String description, FRBonusWin frBonusWin, long gameId,
                                                    String extSessionId, Long bonusId) throws FRBException {
        long now = System.currentTimeMillis();
        long id = generateOperationId();
        FRBWinOperation frbWinOperation = frBonusWin.createFRBWinOperation(id, accountId, gameSessionId, roundId,
                amount, betAmount, description, FRBWinOperationStatus.STARTED, FRBWinOperationStatus.STARTED, gameId,
                extSessionId, bonusId);
        if (persistFRBWinOperation) {
            FRBonusWinRequestFactory.getInstance().save(frbWinOperation);
        }
        StatisticsManager.getInstance().updateRequestStatistics("OriginalFRBonusWinManager:" +
                "createFrbonusWinOperation", System.currentTimeMillis() - now);
        return frbWinOperation;
    }

    public boolean isSendZeroFrbWin() {
        return sendZeroFrbWin;
    }
}
