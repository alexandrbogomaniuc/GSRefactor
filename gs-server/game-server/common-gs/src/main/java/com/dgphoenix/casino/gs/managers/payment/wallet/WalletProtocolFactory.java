package com.dgphoenix.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.payment.wallet.IWalletHelper;
import com.abs.casino.gs.managers.dblink.EmptyDBLink;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import com.abs.casino.promo.persisters.CassandraUnsendedPromoWinInfoPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.abs.casino.common.util.ReflectionUtils.canInvokeMethod;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: plastical
 * Date: 02.03.2010
 */
public class WalletProtocolFactory {
    private static final Logger LOG = LogManager.getLogger(WalletProtocolFactory.class);
    private static final WalletProtocolFactory instance = new WalletProtocolFactory();
    private final ConcurrentMap<Long, IWalletProtocolManager> managers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, IWalletHelper> helpersMap = new ConcurrentHashMap<>();
    public static final IWalletDBLink EMPTY_WALLET_DB_LINK = new EmptyDBLink();
    private CassandraUnsendedPromoWinInfoPersister promoWinPersister;

    private WalletProtocolFactory() {
    }

    public static WalletProtocolFactory getInstance() {
        return instance;
    }

    private CassandraUnsendedPromoWinInfoPersister getPromoWinPersister() {
        if (promoWinPersister == null) {
            CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            promoWinPersister = persistenceManager.getPersister(CassandraUnsendedPromoWinInfoPersister.class);
        }
        return promoWinPersister;
    }

    public ICommonWalletClient getClient(long bankId) throws WalletException {
        return getWalletProtocolManager(bankId).getClient();
    }

    public IWallet interceptCreateWallet(AccountInfo accountInfo, long bankId, long gameSessionId, int gameId,
                                         GameMode mode, ClientType clientType) throws WalletException {
        if (isWalletBank(bankId)) {
            long now = System.currentTimeMillis();
            LOG.info("interceptCreateWallet: accountId={}, gameSessionId={}, gameId={}", accountInfo.getId(), gameSessionId, gameId);
            final IWallet wallet = getWalletProtocolManager(bankId).handleCreateWallet(accountInfo, gameSessionId,
                    gameId, mode, clientType);
            StatisticsManager.getInstance().updateRequestStatistics("WalletProtocolFactory::interceptCreateWallet",
                    System.currentTimeMillis() - now);
            return wallet;
        }
        return null;
    }

    public void interceptDestroyWallet(AccountInfo accountInfo, long bankId, long gameSessionId, int gameId,
                                       GameMode mode, SessionInfo sessionInfo, IWallet wallet)
            throws WalletException {
        if (isWalletBank(bankId)) {
            long now = System.currentTimeMillis();
            if (LOG.isInfoEnabled()) {
                LOG.info("interceptDestroyWallet: accountId=" + accountInfo.getId() +
                        ", gameSessionId=" + gameSessionId + ", gameId=" + gameId);
            }
            if (sessionInfo == null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("interceptDestroyWallet: found wallet for empty, accountId=" + accountInfo.getId() +
                            ", sessionInfo: " + null + ", destroy Wallet");
                }
                getWalletProtocolManager(bankId).handleDestroyWallet(accountInfo, gameId, mode, wallet);
            } else {
                Long gameSessionId1 = sessionInfo.getGameSessionId();
                if (gameSessionId1 != null && gameSessionId1 == gameSessionId) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("interceptDestroyWallet: found wallet (for this),  accountId=" + accountInfo.getId() +
                                ", sessionInfo: " + sessionInfo + ", destroy Wallet");
                    }
                    getWalletProtocolManager(bankId).handleDestroyWallet(accountInfo, gameId, mode, wallet);
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics("WalletProtocolFactory::interceptDestroyWallet",
                    System.currentTimeMillis() - now);
        }
    }

    public void handleNegativeBet(long accountId, long bankId, long betAmount, IDBLink dbLink, SessionInfo sessionInfo)
            throws WalletException {
        if (isWalletBank(bankId)) {
            if (betAmount >= 0) {
                LOG.warn("handleNegativeBet: betAmount is positive: {}", betAmount);
                return;
            }
            getWalletProtocolManager(bankId).handleNegativeBet(accountId, bankId, betAmount, dbLink, sessionInfo);
        }
    }

    public void interceptDebit(long accountId, long bankId, long betAmount, IDBLink dbLink, SessionInfo sessionInfo,
                               IExternalWalletTransactionHandler extHandler, long mpRoundId)
            throws WalletException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo.isTrackWinInNewGameSession()) {
            interceptCreateWallet(dbLink.getAccount(), bankId, dbLink.getGameSessionId(), (int) dbLink.getGameId(),
                    dbLink.getMode(), sessionInfo.getClientType());
        }
        if (isWalletBank(bankId)) {
            long now = System.currentTimeMillis();
            getWalletProtocolManager(bankId).handleDebit(accountId, betAmount, dbLink, sessionInfo, extHandler, mpRoundId);
            StatisticsManager.getInstance().updateRequestStatistics("WalletProtocolFactory::interceptDebit",
                    System.currentTimeMillis() - now);
        }
    }

    public void interceptDebitCompleted(long accountId, IDBLink dbLink, boolean isBet,
                                        IExternalWalletTransactionHandler extHandler)
            throws WalletException {
        if (isWalletBank(dbLink.getBankId())) {
            long now = System.currentTimeMillis();
            getWalletProtocolManager(dbLink.getBankId()).handleDebitCompleted(accountId, isBet, dbLink, extHandler);
            StatisticsManager.getInstance().updateRequestStatistics("WalletProtocolFactory::interceptDebitCompleted",
                    System.currentTimeMillis() - now);
        }
    }

    public void interceptCredit(long accountId, IDBLink dbLink, boolean isRoundFinished, SessionInfo sessionInfo,
                                IExternalWalletTransactionHandler extHandler)
            throws WalletException {
        if (isWalletBank(dbLink.getBankId())) {
            long now = System.currentTimeMillis();
            getWalletProtocolManager(dbLink.getBankId()).handleCredit(accountId, isRoundFinished, dbLink, sessionInfo,
                    extHandler);
            StatisticsManager.getInstance().updateRequestStatistics("WalletProtocolFactory::interceptCredit",
                    System.currentTimeMillis() - now);
        }
    }

    /**
     * Does not work for a common-wallet version lower than 1.0.9. In such cases it always returns false.
     *
     * @param dbLink          dbLink
     * @param winAmount       current win amount
     * @param isRoundFinished indicates whether round finished or not
     * @return @{code true} if the common-wallet version is 1.0.9 or higher and
     * credit can be done in the current condition, @{code false} otherwise
     * @throws WalletException
     */
    public boolean isCreditCondition(IDBLink dbLink, long winAmount, boolean isRoundFinished)
            throws WalletException {
        if (isWalletBank(dbLink.getBankId())) {
            CommonWallet wallet = (CommonWallet) dbLink.getWallet();
            Long roundId = dbLink.getRoundId();
            if (roundId == null) {
                wallet.getGameWallet((int) dbLink.getGameId()).getRoundId();
            }
            final IWalletProtocolManager manager = getWalletProtocolManager(dbLink.getBankId());
            ICommonWalletClient client = manager.getClient();
            boolean canInvokeIsCreditCondition = canInvokeMethod(client.getClass(), "isCreditCondition",
                    long.class, long.class, boolean.class, IWalletDBLink.class);
            if (canInvokeIsCreditCondition) {
                return client.isCreditCondition(winAmount, manager.getNegativeBet(wallet, (int) dbLink.getGameId()),
                        isRoundFinished, dbLink) || isNeedSendPromoWin(dbLink, roundId);
            }
        }
        return false;
    }

    public boolean isCreditCondition(IDBLink dbLink, long winAmount, long negativeBet, boolean isRoundFinished, Long roundId)
            throws WalletException {
        if (isWalletBank(dbLink.getBankId())) {
            IWalletProtocolManager manager = getWalletProtocolManager(dbLink.getBankId());
            ICommonWalletClient client = manager.getClient();
            boolean canInvokeIsCreditCondition = canInvokeMethod(client.getClass(), "isCreditCondition",
                    long.class, long.class, boolean.class, IWalletDBLink.class);
            if (canInvokeIsCreditCondition) {
                return client.isCreditCondition(winAmount, negativeBet, isRoundFinished, dbLink) || isNeedSendPromoWin(dbLink, roundId);
            }
        }
        return false;
    }

    private boolean isNeedSendPromoWin(IDBLink dbLink, Long roundId) throws WalletException {
        if (BankInfoCache.getInstance().getBankInfo(dbLink.getBankId()).isSupportPromoBalanceTransfer()) {
            IWalletProtocolManager manager = getWalletProtocolManager(dbLink.getBankId());
            if (roundId != null) {
                return getPromoWinPersister().getByRoundId(roundId) != null;
            } else {
                LOG.warn("isNeedSendPromoWin: strange error, roundId is null, dbLink={}", dbLink);
            }
        }
        return false;
    }

    public void interceptCreditCompleted(long accountId, IDBLink dbLink, boolean isRoundFinished,
                                         IExternalWalletTransactionHandler extHandler, long mpRoundId) throws WalletException {
        if (isWalletBank(dbLink.getBankId())) {
            long now = System.currentTimeMillis();
            long previousBalance = dbLink.getAccount().getBalance();
            boolean syncBalance = isSyncBalanceNeeded(dbLink, isRoundFinished);
            getWalletProtocolManager(dbLink.getBankId()).handleCreditCompleted(accountId, isRoundFinished, dbLink, extHandler, mpRoundId);
            if (syncBalance) {
                syncWalletBalance(dbLink, previousBalance);
            }
            StatisticsManager.getInstance().updateRequestStatistics("WalletProtocolFactory::interceptCreditCompleted",
                    System.currentTimeMillis() - now);
        }
    }

    private boolean isSyncBalanceNeeded(IDBLink dbLink, boolean isRoundFinished) throws WalletException {
        long currentWinAmount = 0;
        long currentNegativeBet = 0;
        CommonWallet cWallet = (CommonWallet) dbLink.getWallet();
        Long roundId = dbLink.getRoundId();
        if (cWallet != null) {
            CommonWalletOperation winOperation = cWallet.getGameWalletWinOperation((int) dbLink.getGameId());
            if (winOperation != null) {
                currentWinAmount = winOperation.getAmount();
                currentNegativeBet = winOperation.getNegativeBet();
                roundId = winOperation.getRoundId();
            }
        }
        return dbLink.getMode().equals(GameMode.REAL) &&
                isCreditCondition(dbLink, currentWinAmount, currentNegativeBet, isRoundFinished, roundId);
    }

    private void syncWalletBalance(IDBLink dbLink, long previousBalance) {
        AccountInfo accountInfo = dbLink.getAccount();
        try {
            accountInfo.setBalance(dbLink.getWallet().getServerBalance());
        } catch (CommonException e) {
            LOG.error("Cannot set balance", e);
        }
        long currentBalance = dbLink.getAccount().getBalance();
        if (previousBalance != currentBalance) {
            LOG.warn("Detected balance external changing. Expected:{}, actual:{}", previousBalance, currentBalance);
        }
    }

    public void interceptGameLogicCompleted(long accountId, long bankId, long betAmount, long winAmount)
            throws WalletException {
        if (isWalletBank(bankId)) {
            long now = System.currentTimeMillis();
            getWalletProtocolManager(bankId).handleGameLogicCompleted(accountId, betAmount, winAmount);
            StatisticsManager.getInstance()
                    .updateRequestStatistics("WalletProtocolFactory::interceptGameLogicCompleted",
                            System.currentTimeMillis() - now);
        }
    }

    private IWalletProtocolManager getWPM(long bankId) throws WalletException {
        IWalletProtocolManager manager = managers.get(bankId);
        if (manager != null && manager.getBankInfo().isDevelopmentVersion()) {
            return instantiateWPM(bankId);
        }
        return manager;
    }

    private boolean isWPMExist(long bankId) {
        return managers.containsKey(bankId);
    }

    public IWalletProtocolManager getWalletProtocolManager(long bankId) throws WalletException {
        if (!isWPMExist(bankId)) {
            synchronized (this) {
                if (!isWPMExist(bankId)) {
                    return instantiateWPM(bankId);
                }
            }
        }

        return getWPM(bankId);
    }

    public boolean isWalletBank(long bankId) {
        return managers.get(bankId) != null || BankInfoCache.getInstance().getBankInfo(bankId).getWPMClass() != null;
    }

    public boolean isWalletBankWithGetBalanceSupported(BankInfo bankInfo) {
        return (managers.get(bankInfo.getId()) != null || bankInfo.getWPMClass() != null) &&
                !StringUtils.isTrimmedEmpty(bankInfo.getCWBalanceUrl());
    }

    private IWalletProtocolManager instantiateWPM(long bankId) throws WalletException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String className = bankInfo == null ? null : bankInfo.getWPMClass();
        LOG.info("WalletProtocolFactory::instantiateWPM instantiating WPM for bankId:{}, wpmClass={}", bankId, className);
        try {
            if (!StringUtils.isTrimmedEmpty(className)) {
                Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(className);
                Constructor<?> wpmConstructor = aClass.getConstructor(long.class);
                IWalletProtocolManager manager = (IWalletProtocolManager) wpmConstructor.newInstance(bankId);
                WalletHelper walletHelper = new WalletHelper(bankId);
                helpersMap.put(bankId, walletHelper);
                manager.init(walletHelper);
                ICommonWalletClient client = manager.getClient();
                if (!isTrimmedEmpty(bankInfo.getPendingOperationMailList()) && client instanceof ILoggableCWClient) {
                    ((ILoggableCWClient)client).setLoggableContainer(new SimpleLoggableContainer());
                }
                IWalletProtocolManager existManager = managers.putIfAbsent(bankId, manager);
                return existManager != null ? existManager : manager;
            } else {
                LOG.error("WPMClass is empty, bankId: {}", bankId);
                return null;
            }
        } catch (Exception e) {
            LOG.error("instantiateWPM error:", e);
            throw new WalletException(e);
        }
    }

    public int getWalletVersion(long bankId) throws CommonException {
        ICommonWalletClient client = getClient(bankId);
        String cwVersionPattern = "\\.v\\d+\\.";
        return ReflectionUtils.findCwCtVersion(client.getClass(), cwVersionPattern);
    }

    public void invalidateManager(long bankId) {
        managers.remove(bankId);
    }

    public IWalletHelper getWalletHelper(long bankId) {
        return helpersMap.get(bankId);
    }
}
