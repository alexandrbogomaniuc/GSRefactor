package com.dgphoenix.casino.gs.managers.game;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraPlayerGameSettingsPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.account.PlayerGameSettings;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.dgphoenix.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.*;
import com.abs.casino.common.exception.FRBException;
import com.abs.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusNotificationManager;
import com.dgphoenix.casino.gs.managers.payment.transfer.PaymentManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletPersister;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.dgphoenix.casino.gs.managers.payment.wallet.tracker.WalletTrackerTask;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.gs.persistance.LasthandPersister;
import com.abs.casino.gs.singlegames.tools.util.LasthandHelper;
import com.dgphoenix.casino.sm.IPlayerSessionManager;
import com.dgphoenix.casino.sm.PlayerSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * User: Grien
 * Date: 29.10.2013 12:44
 */
public class StartGameSessionHelper {
    private static final Logger LOG = LogManager.getLogger(StartGameSessionHelper.class);
    public static final String FOUND_WALLET_OPERATION = "Cannot start game, found wallet operation: {}";
    public static final String PREVIOUS_OPERATION_NOT_COMPLETED = "previous operation is not completed";

    private static CassandraPlayerGameSettingsPersister playerGameSettingsPersister;

    public StartGameSessionHelper() {

    }

    private static CassandraPlayerGameSettingsPersister getPlayerGameSettingsPersister() {
        if (playerGameSettingsPersister == null) {
            CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                    .getBean("persistenceManager", CassandraPersistenceManager.class);
            playerGameSettingsPersister = persistenceManager.getPersister(CassandraPlayerGameSettingsPersister.class);
        }
        return playerGameSettingsPersister;
    }

    public static Long restartGame4FRB(SessionInfo sessionInfo, long gameId, String lang,
                                       String strClient) throws CommonException {
        //ignore strClient, this bug in game clients (request.CLIENT parameter always 'FLASH'),
        //also platform/clientType cannot be changed on restartGame
        ClientType clientType = sessionInfo.getClientType();
        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(sessionInfo.getAccountId());
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
        boolean updateBalance = !StringUtils.isTrimmedEmpty(bankInfo.getStandaloneLobbyJspName());
        return startGame(sessionInfo, gameId, GameMode.REAL, lang, clientType, null, null, false,
                updateBalance, null);
    }

    public static boolean hasActiveFrBonus(long accountId, long gameId, Long excludeBonusId)
            throws BonusException {
        List<FRBonus> frBonuses = FRBonusManager.getInstance().getActiveBonuses(accountId);
        for (FRBonus frBonus : frBonuses) {
            if (excludeBonusId != null && frBonus.getId() == excludeBonusId) {
                continue;
            }
            if (frBonus.getGameIds().contains(gameId) && frBonus.getStatus() == BonusStatus.ACTIVE) {
                if (frBonus.isNewVersion()) {
                    if (!frBonus.isReady()) {
                        continue;
                    }
                    if (frBonus.isExpired()) {
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static Long startGame(SessionInfo sessionInfo, long gameId, GameMode mode, String lang,
                                 ClientType clientType, Long gameSessionId, Long bonusId, boolean notGameFRB,
                                 boolean updateBalance, Long balance) throws CommonException {
        long now = System.currentTimeMillis();
        final long accountId = sessionInfo.getAccountId();
        AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
        long bankId = accountInfo.getBankId();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        try {
            String sessionId = sessionInfo.getSessionId();
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId,
                    accountInfo.getCurrencyFraction() == null ? accountInfo.getCurrency() : accountInfo.getCurrencyFraction());
            if (gameInfo == null) {
                LOG.error("startGame: failed: gameId not found, bankId=" + bankId + ", gameId=" + gameId +
                        ", currency=" + accountInfo.getCurrency());
                throw new CommonException("game is not defined");
            }
            if (!gameInfo.isEnabled()) {
                throw new CommonException("game is not enabled");
            }
            bonusId = validateBonusId(bonusId, mode, accountInfo);
            LOG.debug("starting game for accountId:" + accountId + " sessionId:" + sessionId +
                    ", bonusId=" + bonusId + ", mode=" + mode + ", gameId=" + gameInfo.getId() +
                    ", notGameFRB=" + notGameFRB);
            if (bonusId == null) {
                final FRBonusManager bonusManager = FRBonusManager.getInstance();
                if (bonusManager != null) {
                    bonusManager.checkMassAwardsForAccount(accountInfo);
                    bonusId = bonusManager.getEarlestActiveFRBonusId(accountInfo.getId(), gameInfo.getId());
                }
                if (notGameFRB && bonusId != null && mode.equals(GameMode.REAL)) {
                    LOG.info("Choice Real Mode Game with Not FRBonus Id:" + bonusId);
                    bonusId = null;
                }
                if (bonusId != null) {
                    LOG.info("Earlest FRBonus Id:" + bonusId);
                }
            }
            boolean isFrbGameSession = false;
            Long unclosedGameSessionId = sessionInfo.getGameSessionId();
            if (unclosedGameSessionId != null) {
                final PlayerGameSettings settings = getPlayerGameSettingsPersister().get(accountId, (int) gameId);
                GameSession gameSession = GameSessionPersister.getInstance().getGameSession(unclosedGameSessionId);
                isFrbGameSession = gameSession.isFRBonusGameSession();
                LOG.debug("startGame: need closeOnlineGame, gameSessionId: {}", unclosedGameSessionId);
                GameServer.getInstance().closeOnlineGame(accountInfo, sessionInfo,
                        gameSession, false, false);
                //restore gameSettings if removed by closeOnlineGame
                if (settings != null) {
                    getPlayerGameSettingsPersister().persist(accountId, settings, bankInfo);
                }
            }
            if (sessionInfo.getGameSessionId() != null) {
                LOG.warn("starting game for accountId:" + accountId
                        + " sessionId:" + sessionId +
                        " player tries to start new game session not closing old one, " +
                        "old gameSessionId:" + unclosedGameSessionId + " new gameSessionId:" +
                        sessionInfo.getGameSessionId());
                throw new CommonException(
                        "failed to start new game session, need to close previous");
            }
            final ITransactionData ITransactionData = SessionHelper.getInstance().getTransactionData();
            if (!WalletProtocolFactory.getInstance().isWalletBank(bankId)) {
                if (bankInfo.isCTBank()) {
                    Long transactionId = PaymentManager.getInstance().getTrackingTransactionId();
                    if (transactionId != null && mode == GameMode.REAL) {
                        LOG.warn("starting game for accountId:" + accountId
                                + " sessionId:" + sessionId +
                                " player tries to start new game session not closing old one, " +
                                "old gameSessionId:" + unclosedGameSessionId +
                                " new gameSessionId:" + sessionInfo.getGameSessionId() +
                                " player has unfinished transaction, transactionId:" + transactionId + "^^^^");
                        throw new CommonException("failed to start new game session, need to close transaction");
                    }
                }
            } else {
                checkWalletOperations((int) gameId, mode, accountInfo, bankInfo);
            }
            if (GameMode.REAL == mode) {//do not start the same frb game if win in tracking
                FRBonusWin frbonusWin = ITransactionData.getFrbWin();
                FRBonusManager.getInstance().checkPendingOperation(frbonusWin, accountInfo, gameInfo.getId());
            }
            FRBonusNotification frbonusNotification = ITransactionData.getFrbNotification();
            if (frbonusNotification != null &&
                    FRBonusNotificationManager.getInstance().isLaunchPrevented(frbonusNotification)) {
                throw new FRBException("FRB previous notification is not completed: " + frbonusNotification);
            }

            GameServer.getInstance().checkMaintenanceMode(mode, lang, accountInfo, gameId);

            if (!SubCasinoCache.getInstance().isExist(171l, bankId)) {  //not for DemoCasino
                additionalProcess(bankId, WalletProtocolFactory.getInstance().isWalletBank(bankId), accountInfo,
                        sessionInfo, gameInfo, mode, gameSessionId, updateBalance, clientType, balance);
            }
            gameSessionId = GameServer.getInstance().startGame(sessionInfo, gameInfo, gameSessionId, mode,
                    bonusId, lang, accountInfo);
            sessionInfo.setClientType(clientType);
            LOG.debug("starting game for accountId:" + accountId + " sessionId:" + sessionId + " was OK");
            return gameSessionId;
        } catch (MaintenanceModeException e) {
            throw e;
        } catch (Throwable e) {
            final IPlayerSessionManager psm = PlayerSessionFactory.getInstance().getPlayerSessionManager(bankId);
            if (e instanceof CommonException) {
                throw (CommonException) e;
            }
            throw new CommonException("Unexpected error", e);
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics(StartGameSessionHelper.class.getSimpleName() +
                    ":doPost", System.currentTimeMillis() - now);
        }
    }

    private static boolean isNeedToResumeBrokenGame(GameMode gameMode, long subCasinoId) {
        final long playTechSubCasinoId = 317L;
        return subCasinoId == playTechSubCasinoId && gameMode == GameMode.REAL;
    }

    private static String getParameterFromLasthand(LasthandInfo lasthandInfo, String parameter) throws GameException {
        LOG.debug("Getting {} from lasthandInfo={}", parameter, lasthandInfo);
        String result = null;
        if (!StringUtils.isTrimmedEmpty(parameter) && !StringUtils.isTrimmedEmpty(lasthandInfo.getLasthandData()) &&
                lasthandInfo.getLasthandData().contains(parameter)) {
            List<Map<String, String>> unpackedLasthand = LasthandHelper.unpack(lasthandInfo.getLasthandData());
            Map<String, String> privateLasthand = unpackedLasthand.get(1);
            result = privateLasthand.get(parameter);
        }

        return result;
    }

    private static void removeParameterFromLasthand(LasthandInfo lasthandInfo, long accountId, long gameId, String parameter,
                                                    Long bonusId, BonusSystemType type) throws GameException {
        LOG.debug("Try to delete {} from lasthandInfo={}", parameter, lasthandInfo);
        if (lasthandInfo.getLasthandData().contains(parameter)) {
            List<Map<String, String>> unpackedLasthand = LasthandHelper.unpack(lasthandInfo.getLasthandData());
            Map<String, String> publicLastand = unpackedLasthand.get(0);
            Map<String, String> privateLasthand = unpackedLasthand.get(1);
            Map<String, String> autoPublicLasthand = unpackedLasthand.get(2);
            Map<String, String> autoPrivateLasthand = unpackedLasthand.get(3);
            privateLasthand.remove(parameter);
            String packedLasthand = LasthandHelper.pack(publicLastand, privateLasthand, autoPublicLasthand, autoPrivateLasthand);
            lasthandInfo.setLasthandData(packedLasthand);
            LOG.debug("After deleting {}, lasthandInfo={}", parameter, lasthandInfo);
            LasthandPersister.getInstance().forcedSave(lasthandInfo, accountId, gameId, bonusId, type);
        }
    }

    private static Long validateBonusId(Long bonusId, GameMode mode, AccountInfo accountInfo) throws CommonException {
        if (bonusId == null) {
            return null;
        }
        if (mode.equals(GameMode.REAL)) {
            FRBonus frBonus = FRBonusManager.getInstance().getById(bonusId);
            if (frBonus == null) {
                throw new CommonException("FRBonus is invalid");
            }
            return frBonus.getId();
        }
        if (mode.equals(GameMode.BONUS)) {
            Bonus bonus = BonusManager.getInstance().getById(bonusId);
            if (bonus == null) {
                throw new CommonException("bonus is null");
            }
            if (!bonus.getStatus().equals(BonusStatus.ACTIVE)) {
                throw new CommonException("bonus is not active");
            }
            if (bonus.getAccountId() != accountInfo.getId()) {
                throw new CommonException("the bonusId is not found for this accountId=" + accountInfo.getId());
            }
            return bonusId;
        }
        return null;
    }

    protected static void additionalProcess(long bankId, boolean isCWBank, AccountInfo account, SessionInfo sessionInfo,
                                            IBaseGameInfo gameInfo, GameMode mode, Long gameSessionId,
                                            boolean updateBalance,
                                            ClientType clientType, Long balance) throws CommonException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (isCWBank) {
            if ((mode == GameMode.REAL) && updateBalance) {
                if (!StringUtils.isTrimmedEmpty(bankInfo.getCWBalanceUrl())) {
                    IWalletProtocolManager ocwm = WalletProtocolFactory.getInstance().getWalletProtocolManager(bankId);
                    ICommonWalletClient client = ocwm.getClient();
                    double dNewBalance = client.getBalance(account.getId(), account.getExternalId(), bankId,
                            account.getCurrency());
                    Long newBalance = (long) dNewBalance;
                    LOG.info("UpdateBalance: sessionId =  " + sessionInfo.getSessionId() +
                            " new balance = " + newBalance.toString() +
                            " old balance = " + account.getBalance() +
                            " byAccountId.getId() = " + account.getId());
                    account.setBalance(newBalance);
                } else {
                    LOG.info("UpdateBalance: forced but no CWBalanceUrl was set for bank: " + bankInfo);
                }
            }
        } else if (!StringUtils.isTrimmedEmpty(BankInfoCache.getInstance().getBankInfo(bankId).getPPClass())) {//CT bank
            if (GameMode.REAL.equals(mode)) {
                if (balance == null) {
                    throw new CommonException("transaction amount is null or empty");
                }
                if (balance < 0) {
                    throw new CommonException("transaction amount is not correct, amount:" + balance);
                }
                PaymentTransaction transaction = PaymentManager.getInstance().processDeposit(account,
                        bankInfo, gameSessionId, gameInfo.getId(), balance, null, true, clientType,
                        null);
                if (TransactionStatus.APPROVED.equals(transaction.getStatus())) {
                    account.incrementBalance(balance, false);
                } else {
                    throw new CommonException(transaction.getDescription());
                }
            }
        }
    }

    public static void checkWalletOperations(int gameId, GameMode mode, AccountInfo accountInfo, BankInfo bankInfo) throws WalletException {
        IWallet wallet = WalletPersister.getInstance().getWallet(accountInfo.getId());
        if (GameMode.REAL == mode && wallet != null) {
            if (bankInfo.isNoStartGameIfWalletOpUncompleted()) {
                handleAnyWalletOperationExist(accountInfo, wallet);
            } else {
                handleCurrentWalletOperation(gameId, accountInfo, bankInfo, wallet);
            }
        }
    }

    private static void handleAnyWalletOperationExist(AccountInfo accountInfo, IWallet wallet) throws WalletException {
        if (wallet.isAnyWalletOperationExist()) {
            LOG.warn(FOUND_WALLET_OPERATION, wallet);
            throw new WalletException(PREVIOUS_OPERATION_NOT_COMPLETED, accountInfo.getId());
        }
    }

    private static void handleCurrentWalletOperation(int gameId, AccountInfo accountInfo, BankInfo bankInfo, IWallet wallet) throws WalletException {
        IWalletOperation operation = wallet.getCurrentWalletOperation(gameId);
        if (operation != null) {
            if (operation.getType() == WalletOperationType.CREDIT) {
                if (bankInfo.isTrackWinInNewGameSession()) {
                    return;
                } else if (operation.getExternalStatus() == WalletOperationStatus.COMPLETED) {
                    try {
                        LOG.debug("Finalizing completed operation {}", operation);
                        new WalletTrackerTask(accountInfo.getId(), WalletTracker.getInstance()).process();
                        return;
                    } catch (CommonException e) {
                        LOG.error("Can not finalize operation", e);
                    }
                }
            }
            LOG.warn(FOUND_WALLET_OPERATION, wallet);
            throw new WalletException(PREVIOUS_OPERATION_NOT_COMPLETED, accountInfo.getId());
        }
    }
}
