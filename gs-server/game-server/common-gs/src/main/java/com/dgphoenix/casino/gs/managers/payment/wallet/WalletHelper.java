package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraCommonGameWalletPersister;
import com.abs.casino.cassandra.persist.CassandraTrackingInfoPersister;
import com.abs.casino.cassandra.persist.CassandraWalletOperationInfoPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.abs.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.PromoWinInfo;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.managers.game.session.GameSessionManager;
import com.abs.casino.gs.managers.payment.wallet.IWalletHelper;
import com.abs.casino.gs.managers.payment.currency.CurrencyRatesManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;
import com.abs.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.abs.casino.gs.managers.payment.wallet.tracker.WalletTrackerTask;
import com.abs.casino.gs.persistance.GameSessionPersister;
import com.abs.casino.gs.persistance.LasthandPersister;
import com.abs.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.abs.casino.promo.persisters.CassandraUnsendedPromoWinInfoPersister;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * User: flsh
 * Date: 15.11.14.
 */
public class WalletHelper implements IWalletHelper {
    private static final Logger LOG = LogManager.getLogger(WalletHelper.class);
    private static final String CM_WALLET_OPERATION_ALERTS_REPORT = "/reports/walletOperationAlerts/complete?";
    private final CassandraTrackingInfoPersister trackingInfoPersister;
    private final CassandraCommonGameWalletPersister commonGameWalletPersister;
    private final CassandraWalletOperationInfoPersister walletOperationInfoPersister;
    private final PlayerBetPersistenceManager betPersistenceManager;
    private final CassandraUnsendedPromoWinInfoPersister promoWinPersister;
    private final GameServerConfiguration gameServerConfiguration;
    private final CurrencyRatesManager currencyRatesManager;

    private final long bankId;

    public WalletHelper(long bankId) {
        this.bankId = bankId;
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        trackingInfoPersister = persistenceManager.getPersister(CassandraTrackingInfoPersister.class);
        commonGameWalletPersister = persistenceManager.getPersister(CassandraCommonGameWalletPersister.class);
        walletOperationInfoPersister = persistenceManager.getPersister(CassandraWalletOperationInfoPersister.class);
        betPersistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
        promoWinPersister = persistenceManager.getPersister(CassandraUnsendedPromoWinInfoPersister.class);
        gameServerConfiguration = ApplicationContextHelper.getApplicationContext()
                .getBean("gameServerConfiguration", GameServerConfiguration.class);
        currencyRatesManager = ApplicationContextHelper.getBean(CurrencyRatesManager.class);
    }

    @Override
    public long getBankId() {
        return bankId;
    }

    @Override
    public AccountInfo getAccountInfo(long accountId) {
        return AccountManager.getInstance().getAccountInfo(accountId);
    }

    @Override
    public AccountInfo getAccountInfo(short subcasinoId, int bankId, String extUserId) throws CommonException {
        return AccountManager.getInstance().getAccountInfo(subcasinoId, bankId, extUserId);
    }

    @Override
    public GameSession getOnlineGameSession(long gameSessionId) throws CommonException {
        return GameSessionPersister.getInstance().getGameSession(gameSessionId);
    }

    @Override
    public GameSession getGameSession(long gameSessionId) {
        return GameSessionManager.getInstance().getGameSessionById(gameSessionId);
    }

    @Override
    public SessionInfo getSessionInfo() {
        return SessionHelper.getInstance().getTransactionData().getPlayerSession();
    }

    @Override
    public LasthandInfo getLasthand(long id, long accountId, long gameId, Long bonusId,
                                    BonusSystemType bonusSystemType) {
        return LasthandPersister.getInstance().get(accountId, gameId);
    }

    @Override
    public boolean isLastHandExist(long id, long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType) {
        LasthandInfo lasthand = getLasthand(id, accountId, gameId, bonusId, bonusSystemType);
        if (lasthand == null) {
            return false;
        }
        String data = lasthand.getLasthandData();
        if (gameId == 199 && !StringUtils.isTrimmedEmpty(data) && !data.contains("BETS")) {
            return false;
        }
        return !StringUtils.isTrimmedEmpty(data);
    }

    @Override
    public void addWalletTrackerTask(Long accountId) {
        WalletTracker.getInstance().addTask(accountId);
    }

    @Override
    public void addHighPriorityWalletTrackerTask(Long accountId) {
        WalletTracker.getInstance().addHighPriorityTask(accountId);
    }

    @Override
    public boolean isRegisteredForTracking(String trackerName, long trackedObjectId) {
        return trackingInfoPersister.isTracking(trackerName, trackedObjectId);
    }

    @Override
    public void registerForTracking(String trackerName, long trackedObjectId) {
        trackingInfoPersister.persist(trackerName, trackedObjectId);
    }

    @Override
    public void unregisterFromTracking(String trackerName, long trackedObjectId) {
        trackingInfoPersister.delete(trackerName, trackedObjectId);
    }

    @Override
    public void persistWalletOperation(AccountInfo accountInfo, CommonWalletOperation operation) {
        walletOperationInfoPersister.persist(accountInfo, operation);
    }

    @Override
    public void persistWalletOperation(BankInfo bankInfo, AccountInfo accountInfo,
                                       CommonWalletOperation operation, long gameId, long balance) {
        walletOperationInfoPersister.persist(bankInfo, accountInfo, operation, gameId, balance);
    }

    @Override
    public void updateWalletOperation(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation) {
        walletOperationInfoPersister.update(bankInfo, accountInfo, operation);
    }

    @Override
    public void updateWalletOperation(BankInfo bankInfo, AccountInfo accountInfo, CommonWalletOperation operation, boolean refunded) {
        walletOperationInfoPersister.update(bankInfo, accountInfo, operation, refunded);
    }

    @Override
    public void persistGameWallet(long accountId, CommonGameWallet gameWallet) {
        commonGameWalletPersister.createOrUpdate(accountId, gameWallet);
    }

    @Override
    @Deprecated //remove after deploy to all systems
    public void clearPersistedFameWallets(long accountId) {
        commonGameWalletPersister.removeWallet(accountId);
    }

    @Override
    public void persistCommonWallet(CommonWallet wallet) {
        commonGameWalletPersister.persistCommonWallet(wallet);
    }

    @Override
    public void persistDailyWallet(CommonWalletOperation operation, long bankId) {
        //code removed
    }

    @Override
    public void persistMinMaxWallet(long subCasinoId, long date, long bankId, String extUserId, long transactionId,
                                    long roundId, long gameSessionId, String externalTransactionId,
                                    long endTime) {
        //code removed
    }

    @Override
    public Map<Long, List<PlayerBet>> getPlayerBets(Map<Long, List<Long>> sessionBetsMap) {
        return betPersistenceManager.getPlayerBets(sessionBetsMap);
    }

    @Override
    public boolean tryResolvePendingOperations(BankInfo bankInfo, String extUserId, Consumer<IWalletOperation> beforeSend,
                                               Integer gameId) throws CommonException {
        if (!bankInfo.isTryResolvePendingOperationsOnAuth()) {
            return false;
        }
        LOG.debug("tryResolvePendingOperations:: bankId={}, extUserId={}", bankId, extUserId);
        boolean transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
        if (!transactionAlreadyStarted) {
            SessionHelper.getInstance().lock(Ints.checkedCast(bankId), extUserId);
        }
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            ITransactionData data = SessionHelper.getInstance().getTransactionData();
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(Shorts.checkedCast(bankInfo.getSubCasinoId()),
                    Ints.checkedCast(bankId), extUserId);
            if (accountInfo == null) {
                LOG.debug("tryResolvePendingOperations:: account doesn't exist, bankId={}, extUserId={}", bankId, extUserId);
                return false;
            }
            long accountId = accountInfo.getId();
            IWallet wallet = data.getWallet();
            if (wallet == null) {
                LOG.debug("tryResolvePendingOperations:: empty wallet, accountId={}", accountInfo.getId());
                return false;
            }
            boolean hasCompletedOperations = false;
            Set<Integer> gameIds = gameId != null ? Collections.singleton(gameId) : wallet.getWalletGamesIds();
            for (Integer id : gameIds) {
                IWalletOperation operation = wallet.getCurrentWalletOperation(id);
                if (operation != null && WalletOperationStatus.PEENDING_SEND_ALERT.equals(operation.getExternalStatus())) {
                    if (beforeSend != null) {
                        beforeSend.accept(operation);
                    }
                    LOG.info("tryResolvePendingOperations:: run wallet task, accountId={}, gameId={}", accountId, id);
                    try {
                        new WalletTrackerTask(accountId, id, WalletTracker.getInstance(), true).process(true, 5000);
                        LOG.debug("tryResolvePendingOperations:: task complete successful accountId={}, gameId={}", accountId, id);
                    } catch (Exception e) {
                        LOG.error("tryResolvePendingOperations:: task error, accountId={}, gameId={}", accountId, id, e);
                    }
                    hasCompletedOperations = true;
                }
            }
            if (!transactionAlreadyStarted) {
                if (hasCompletedOperations) {
                    SessionHelper.getInstance().commitTransaction();
                }
                SessionHelper.getInstance().markTransactionCompleted();
            }
            return hasCompletedOperations;
        } catch (Exception e) {
            LOG.error("tryResolvePendingOperations:: error:", e);
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        return false;
    }

    @Override
    public PromoWinInfo getPromoWinInfo(long roundId) {
        return promoWinPersister.getByRoundId(roundId);
    }

    @Override
    public void removePromoWin(long roundId) {
        promoWinPersister.removeByRoundId(roundId);
    }
}
