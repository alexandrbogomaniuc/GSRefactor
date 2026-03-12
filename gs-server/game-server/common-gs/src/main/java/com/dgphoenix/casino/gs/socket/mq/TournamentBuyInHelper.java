package com.abs.casino.gs.socket.mq;

import com.abs.casino.account.AccountManager;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraExternalTransactionPersister;
import com.abs.casino.cassandra.persist.CassandraLasthandPersister;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.GameMode;
import com.abs.casino.common.cache.data.payment.IWallet;
import com.abs.casino.common.cache.data.payment.PaymentMode;
import com.abs.casino.common.cache.data.payment.transfer.ExternalPaymentTransaction;
import com.abs.casino.common.cache.data.payment.transfer.TransactionStatus;
import com.abs.casino.common.cache.data.payment.transfer.TransactionType;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.exception.WalletException;
import com.abs.casino.common.promo.battleground.BattlegroundConfig;
import com.abs.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.util.IdGenerator;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.gs.GameServer;
import com.abs.casino.gs.GameServerComponentsHelper;
import com.abs.casino.gs.managers.dblink.DBLinkCache;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.abs.casino.gs.managers.dblink.TournamentDBLink;
import com.abs.casino.gs.managers.payment.wallet.*;
import com.abs.casino.gs.managers.payment.wallet.CommonWalletErrors;
import com.abs.casino.gs.socket.mq.ForceCreateDetailsException;
import com.abs.casino.gs.managers.payment.wallet.tracker.WalletTracker;
import com.abs.casino.gs.persistance.LasthandPersister;
import com.abs.casino.gs.singlegames.tools.cbservtools.IGameController;
import com.abs.casino.gs.singlegames.tools.util.LasthandHelper;
import com.abs.casino.promo.persisters.CassandraBattlegroundConfigPersister;
import com.abs.casino.promo.tournaments.messages.BattlegroundInfo;
import com.abs.casino.websocket.tournaments.ISocketClient;
import com.abs.casino.websocket.tournaments.TournamentClient;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TournamentBuyInHelper {
    private static final Logger LOG = LogManager.getLogger(TournamentBuyInHelper.class);
    private static final String DELIMITER = "+";

    private final ICurrencyRateManager currencyRateManager;
    private final CassandraExternalTransactionPersister externalTransactionPersister;
    private final CassandraLasthandPersister lasthandPersister;
    private volatile AccountManager accountManager;
    private final CassandraBattlegroundConfigPersister cassandraBattlegroundConfigPersister;

    public TournamentBuyInHelper(ICurrencyRateManager currencyRateManager, CassandraPersistenceManager cpm) {
        this.currencyRateManager = currencyRateManager;
        this.externalTransactionPersister = cpm.getPersister(CassandraExternalTransactionPersister.class);
        this.lasthandPersister = cpm.getPersister(CassandraLasthandPersister.class);
        this.cassandraBattlegroundConfigPersister = cpm.getPersister(CassandraBattlegroundConfigPersister.class);
    }

    public void performBuyIn(String sessionId, long tournamentId, long buyInAmount, String currency, int betNumber,
                             long tournamentBalance,
                             boolean isBuyIn) throws CommonException, ForceCreateDetailsException {
        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            AccountInfo accountInfo = getAccountManager().getAccountInfo(sessionInfo.getAccountId());
            GameSession gameSession = isBuyIn ? null : SessionHelper.getInstance().getTransactionData().getGameSession();
            long buyInAmountInPlayerCurrency = (long) currencyRateManager
                    .convert(buyInAmount, currency, accountInfo.getCurrency().getCode());
            if (buyInAmountInPlayerCurrency > 0) {
                if (accountInfo.getBalance() < buyInAmountInPlayerCurrency) {
                    LOG.warn("performBuyIn: insufficient funds sessionId={}, tournamentId={}, buyInAmount={}, " +
                                    "currency={}, buyInAmountInPlayerCurrency={}, accountInfo.getBalance()={}",
                            sessionId, tournamentId, buyInAmount, currency, buyInAmountInPlayerCurrency,
                            accountInfo.getBalance());
                    throw new WalletException(accountInfo.getId(), "Not enough money",
                            String.valueOf(CommonWalletErrors.INSUFFICIENT_FUNDS.getCode()));
                }
                processBuyIn(sessionId, buyInAmountInPlayerCurrency, betNumber, accountInfo, gameSession,
                        tournamentId, isBuyIn);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
            ClientType clientType = gameSession != null ? gameSession.getClientType() : sessionInfo.getClientType();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    public boolean isBuyInCorrect(String sessionId, long bankId, String playerCurrencyCode, long buyIn, int gameId)
            throws CommonException {
        MutableBoolean res = new MutableBoolean(false);
        Set<BattlegroundInfo> battlegroundInfos = getBattlegroundInfos(sessionId, bankId, playerCurrencyCode);
        battlegroundInfos.forEach(battlegroundInfo -> {
            if (battlegroundInfo.getGameId() == gameId && battlegroundInfo.getBuyIns().contains(buyIn))
                res.setTrue();
        });
        return res.getValue();
    }

    public Set<BattlegroundInfo> getBattlegroundInfos(String sessionId, long bankId, String playerCurrencyCode)
            throws CommonException {
        ISocketClient client = GameServerComponentsHelper.getTournamentManager().getSocketClientBySessionId(sessionId);
        Set<BattlegroundInfo> battlegroundInfos = new HashSet<>();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        Set<BattlegroundConfig> battlegroundConfigs = cassandraBattlegroundConfigPersister.getConfigs(bankInfo.getId());

        LOG.debug("getBattlegroundInfos: bankId = {} , battlegroundConfigs.size() = {} ",
                bankId, battlegroundConfigs != null ? battlegroundConfigs.size() : null);

        for (BattlegroundConfig config : battlegroundConfigs) {
            if (config.isEnabled()) {
                battlegroundInfos.add(new BattlegroundInfo(config.getGameId(), config.getIcon(),
                        config.getRulesLink(), getBuyIns(config.getBuyInsForDefaultCurrency(),
                        config.getBuyInsByCurrencyMap(), playerCurrencyCode, bankInfo), config.getRake()));
            }
        }
        if (client != null) {
            ((TournamentClient) client).setAvailableBuyIns(battlegroundInfos);
        }
        return battlegroundInfos;
    }

    private List<Long> getBuyIns(List<Long> buyInsForDefaultCurrency, Map<String, List<Long>> buyInsByCurrencyMap,
                                 String playerCurrencyCode, BankInfo bankInfo) throws CommonException {
        if (playerCurrencyCode.equals(bankInfo.getDefaultCurrency().getCode())) {
            return buyInsForDefaultCurrency;
        } else {
            List<Long> buyIns = buyInsByCurrencyMap.get(playerCurrencyCode);
            if (buyIns != null) {
                return buyIns;
            }
        }
        return convertBuyIns(buyInsForDefaultCurrency, playerCurrencyCode, bankInfo);
    }

    private List<Long> convertBuyIns(List<Long> buyInsForDefaultCurrency, String playerCurrencyCode, BankInfo bankInfo)
            throws CommonException {
        List<Long> result = new ArrayList<>(buyInsForDefaultCurrency.size());
        for (Long buyIn : buyInsForDefaultCurrency) {
            result.add((long) Math.ceil(currencyRateManager.convert(buyIn, bankInfo.getDefaultCurrency().getCode(),
                    playerCurrencyCode)));
        }
        return result;
    }

    public boolean checkBalance(String sessionId, long buyInAmount) throws CommonException {
        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            AccountInfo accountInfo = getAccountManager().getAccountInfo(sessionInfo.getAccountId());
            if (buyInAmount > 0) {
                if (accountInfo.getBalance() < buyInAmount) {
                    return false;
                }
                SessionHelper.getInstance().markTransactionCompleted();
            }
            return true;
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    private void processBuyIn(String sessionId, long amount, int betNumber, AccountInfo account,
                              GameSession gameSession, long tournamentId,
                              boolean isBuyIn) throws CommonException, ForceCreateDetailsException {
        LOG.debug("buyIn: sessionId={}, amount={}, betNumber={}, tournamentId={}", sessionId, amount,
                betNumber, tournamentId);
        SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
        if (sessionInfo == null) {
            LOG.warn("Unable to perform buy in - SessionInfo not found {}", sessionId);
            throw new CommonException("Session not found");
        }
        boolean walletBank = WalletProtocolFactory.getInstance().isWalletBank(account.getBankId());
        IDBLink dbLink = getDBLink(sessionInfo, gameSession, account);
        Long roundId = dbLink.getRoundId();
        gameSession = gameSession == null ? dbLink.getGameSession() : gameSession;
        if (walletBank) {
            makeWalletBet(sessionInfo, dbLink, gameSession, account, amount);
            try {
                makeWalletWin(sessionInfo, gameSession, account, dbLink, tournamentId);
            } catch (CommonException e) {
                if (isBuyIn) {
                    throw new ForceCreateDetailsException(e);
                }
            }
        } else {
            makeBet(sessionInfo, dbLink, gameSession, account, amount);
            makeWin(account, gameSession, dbLink, tournamentId);
        }
        saveExternalTransaction(amount, account, betNumber, gameSession, walletBank, roundId, tournamentId);
        LOG.debug("Buy in for {} success, roundId is {}", sessionId, roundId);
    }

    private AccountManager getAccountManager() {
        AccountManager local = accountManager;
        if (local == null) {
            synchronized (this) {
                local = accountManager;
                if (local == null) {
                    local = AccountManager.getInstance();
                    accountManager = local;
                }
            }
        }
        return local;
    }

    private void makeWalletWin(SessionInfo sessionInfo, GameSession gameSession, AccountInfo account,
                               IDBLink dbLink, long tournamentId) throws CommonException {
        sessionInfo.updateActivity();
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        IWallet wallet = transactionData.getWallet();
        MultiplayerExternalWallettransactionHandler handler;
        boolean needSendWin;
        try {
            WalletProtocolFactory.getInstance().interceptCreateWallet(account, account.getBankId(), gameSession.getId(),
                    (int) gameSession.getGameId(), com.abs.casino.common.cache.data.game.GameMode.REAL,
                    gameSession.getClientType());
            dbLink.updateLastActivity();
            Long roundId = dbLink.getRoundId();
            String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), roundId, tournamentId);
            ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(extTransactionId,
                    account.getExternalId(), account.getId(), account.getBankId(), 0,
                    gameSession.getId(), gameSession.getGameId(), System.currentTimeMillis(), null, PaymentMode.WALLET,
                    null, TransactionStatus.STARTED, TransactionType.WITHDRAWAL,
                    "", roundId, "", true);
            externalTransactionPersister.persist(transaction);
            handler = new MultiplayerExternalWallettransactionHandler(transaction.getBankId(), transaction.getExtId());
            dbLink.setWinAmount(0L);
            LOG.info("makeWalletWin: deleting online lastHand={}", transactionData.getLasthand());
            transactionData.setLasthand(null);
            lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
            if (wallet == null) {
                wallet = transactionData.getWallet();
            }
            CommonGameWallet gameWallet = wallet.getGameWallet((int) gameSession.getGameId());
            needSendWin = gameWallet.getBetAmount() > 0;
            if (needSendWin) {
                WalletProtocolFactory.getInstance().interceptCredit(account.getId(), dbLink, true,
                        sessionInfo, handler);
            } else {
                LOG.debug("makeWalletWin: skip interceptCredit, bet and win is zero, remove gameWallet");
                wallet.removeGameWallet((int) gameSession.getGameId());
            }

            processWin(gameSession, dbLink);
            gameSession.setLastPaymentOperationId(transaction.getInternalOperationId());
            SessionHelper.getInstance().commitTransaction();
        } catch (Exception e) {
            LOG.error("processing error: credit failed sessionInfo={}, gameSession={}", sessionInfo, gameSession, e);
            WalletTracker.getInstance().addTask(account.getId());
            throw new CommonException("Credit failed");
        }
        if (needSendWin) {
            try {
                WalletProtocolFactory.getInstance().interceptCreditCompleted(account.getId(), dbLink, true, handler, dbLink.getRoundId());
            } catch (WalletException e) {
                LOG.error("processing error: interceptCreditCompleted failed, sessionInfo={}, gameSession={}",
                        sessionInfo, gameSession, e);
                WalletTracker.getInstance().addTask(account.getId());
                throw new CommonException("Credit failed");
            }
        }
    }

    private void makeWin(AccountInfo account, GameSession gameSession, IDBLink dbLink,
                         long tournamentId) throws CommonException {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        LOG.info("Deleting online CT lastHand={}", transactionData.getLasthand());
        transactionData.setLasthand(null);
        String lasthandFromCassandra = lasthandPersister.get(account.getId(), gameSession.getGameId(),
                null, null);
        LOG.info("Deleting CT offline lasthandFromCassandra={}", lasthandFromCassandra);
        lasthandPersister.delete(account.getId(), gameSession.getGameId(), null, null);
        SessionHelper.getInstance().getDomainSession().persistPlayerBet();
        processWin(gameSession, dbLink);
        String extTransactionId = getExternalTransactionIdForMpGame(account.getId(), dbLink.getRoundId(), tournamentId);
        ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(extTransactionId,
                account.getExternalId(), account.getId(), account.getBankId(), 0,
                gameSession.getId(), gameSession.getGameId(), System.currentTimeMillis(), null,
                PaymentMode.COMMON_TRANSFER, null, TransactionStatus.APPROVED, TransactionType.WITHDRAWAL,
                "", dbLink.getRoundId(), "", true);
        externalTransactionPersister.persist(transaction);
    }

    private void processWin(GameSession gameSession, IDBLink dbLink) throws CommonException {
        dbLink.setRoundId(null);
        dbLink.setRoundFinished();
        gameSession.setCreateNewBet(true, false);
    }

    private void saveExternalTransaction(long amount, AccountInfo account, int betNumber, GameSession gameSession,
                                         boolean walletBank, Long roundId, long tournamentId) {
        String externalTransactionId = getDebitExternalTransactionIdForMpGame(account.getId(),
                gameSession.getId(), betNumber, tournamentId);
        ExternalPaymentTransaction transaction = new ExternalPaymentTransaction(externalTransactionId,
                account.getExternalId(), account.getId(), account.getBankId(), amount, gameSession.getId(),
                gameSession.getGameId(), System.currentTimeMillis(), null,
                walletBank ? PaymentMode.WALLET : PaymentMode.COMMON_TRANSFER, null,
                TransactionStatus.APPROVED, TransactionType.DEPOSIT,
                "", roundId, "", false);
        externalTransactionPersister.persist(transaction);
    }

    private String getDebitExternalTransactionIdForMpGame(long accountId, long gameSessionId, int betNumber,
                                                          long tournamentId) {
        return accountId + DELIMITER + gameSessionId + DELIMITER + betNumber + DELIMITER + tournamentId;
    }

    private String getExternalTransactionIdForMpGame(long accountId, long roundId, long tournamentId) {
        return accountId + DELIMITER + roundId + DELIMITER + tournamentId;
    }

    private void makeWalletBet(SessionInfo sessionInfo, IDBLink dbLink, GameSession gameSession, AccountInfo account,
                               long amount) throws CommonException {
        LOG.debug("makeWalletBet: before debit account={}", account);
        IWallet wallet = SessionHelper.getInstance().getTransactionData().getWallet();
        checkPendingOperation(wallet, gameSession.getGameId());
        sessionInfo.updateActivity();
        try {
            WalletProtocolFactory.getInstance().interceptDebit(account.getId(), account.getBankId(), amount, dbLink,
                    sessionInfo, null, dbLink.getRoundId());
            makeBet(sessionInfo, dbLink, gameSession, account, amount);
        } catch (WalletException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("processing error: debit failed sessionInfo={}, gameSession={}", sessionInfo, gameSession, e);
            throw new CommonException("Debit failed");
        }

        try {
            WalletProtocolFactory.getInstance().interceptDebitCompleted(account.getId(), dbLink, true, null);
        } catch (WalletException e) {
            LOG.error("processing error: interceptDebitCompleted failed sessionInfo={}, gameSession={}",
                    sessionInfo, gameSession, e);
            throw new CommonException("Debit completed failed");
        }
        LOG.debug("makeWalletBet: after debit account={}", account);
    }

    private void checkPendingOperation(IWallet wallet, long gameId) throws CommonException {
        CommonGameWallet cgw = wallet != null ? wallet.getGameWallet((int) gameId) : null;
        IWalletOperation pendingOperation = wallet == null ? null : wallet.getCurrentWalletOperation((int) gameId);
        if (pendingOperation != null || (cgw != null && (cgw.getBetAmount() != 0 || cgw.getWinAmount() != 0))) {
            LOG.error("processing error, previous operation is not completed or betAmount/winAmount != 0, " +
                    "operation={}, gameId={}, commonGameWallet={}", pendingOperation, gameId, cgw);
            throw new CommonException("Previous operation not completed");
        }
    }

    private void makeBet(SessionInfo sessionInfo, IDBLink dbLink, GameSession gameSession, AccountInfo account,
                         long amount) throws CommonException {
        LOG.debug("makeBet: before debit account={}", account);
        sessionInfo.updateActivity();
        try {
            dbLink.incrementBalance(-amount, 0);
            dbLink.interceptBet(amount, 0);
            dbLink.updateCurrentBetWin(amount, 0);
            dbLink.updateLastActivity();
            if (StringUtils.isTrimmedEmpty(dbLink.getLasthand())) {
                Map<String, String> lasthand1 = new HashMap<>();
                Map<String, String> lasthand2 = new HashMap<>();
                lasthand1.put(IGameController.PARAMROUNDID, Long.toString(dbLink.getRoundId()));
                lasthand2.put(IGameController.PARAMROUNDID, Long.toString(dbLink.getRoundId()));
                String data = LasthandHelper.pack(lasthand1, lasthand2, null, null);
                LOG.debug("MQ saveLasthand roundId: {}", data);
                LasthandPersister.getInstance().save(gameSession.getGameId(), data);
            }
        } catch (Exception e) {
            LOG.error("cannot make bet sessionInfo={}, gameSession={}", sessionInfo, gameSession, e);
            throw new CommonException("Debit failed");
        }
        LOG.debug("makeBet: after debit account={}", account);
    }

    private IDBLink getDBLink(SessionInfo sessionInfo, GameSession gameSession,
                              AccountInfo accountInfo) throws CommonException {
        checkInvalidDBLink(sessionInfo);
        IDBLink dbLink;
        if (gameSession == null) {
            dbLink = prepareFakeDBLink(accountInfo);
        } else {
            dbLink = DBLinkCache.getInstance().get(sessionInfo.getGameSessionId());
            if (dbLink == null) {
                try {
                    dbLink = GameServer.getInstance().restartGame(sessionInfo, gameSession);
                } catch (CommonException e) {
                    LOG.error("Unable to create dbLink", e);
                    throw new CommonException("Internal error, cannot create dbLink: " + e.getMessage());
                }
                LOG.debug("recreated dbLink={}", dbLink);
            }
            if (dbLink.getRoundId() == null) {
                dbLink.setRoundId(dbLink.generateRoundId());
            }
        }

        sessionInfo.updateActivity();
        dbLink.updateLastActivity();
        return dbLink;
    }

    private void checkInvalidDBLink(SessionInfo sessionInfo) {
        Long gameSessionId = sessionInfo.getGameSessionId();
        if (gameSessionId != null) {
            ITransactionData td = SessionHelper.getInstance().getTransactionData();
            if (td.getLastLockerId() != GameServer.getInstance().getServerId()) {
                LOG.info("Remove invalid dbLink. Current locker={}; Last locker={}",
                        GameServer.getInstance().getServerId(), td.getLastLockerId());
                DBLinkCache.getInstance().remove(gameSessionId);
            }
        }
    }

    private IDBLink prepareFakeDBLink(AccountInfo accountInfo) throws WalletException {
        long fakeId = IdGenerator.getInstance().getNext(GameSession.class);
        int gameId = 1;
        GameSession fakeGameSession = new GameSession(fakeId, accountInfo.getId(), accountInfo.getBankId(), gameId,
                System.currentTimeMillis(), System.currentTimeMillis(), 0, 0, 0, 0,
                false, true, accountInfo.getCurrency(), "", "en",
                null, null, 0, 0, 0);
        fakeGameSession.setClientType(ClientType.FLASH);
        WalletProtocolFactory.getInstance().interceptCreateWallet(accountInfo, accountInfo.getBankId(), fakeId, gameId,
                GameMode.REAL, ClientType.FLASH);
        return new TournamentDBLink(fakeGameSession, accountInfo);
    }
}

