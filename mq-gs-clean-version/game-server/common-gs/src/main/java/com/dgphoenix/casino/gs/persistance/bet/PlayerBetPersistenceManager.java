package com.dgphoenix.casino.gs.persistance.bet;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraBetPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraBigStorageBetPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraShortBetInfoPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.bet.ShortBetInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.DBException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.Triple;
import com.dgphoenix.casino.gs.GameServerComponentsHelper;
import com.dgphoenix.casino.gs.managers.bet.PlayerBetPersister;
import com.dgphoenix.casino.gs.managers.game.history.HistoryInformerManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 20.10.11
 */
public class PlayerBetPersistenceManager {
    private static final Logger LOG = LogManager.getLogger(PlayerBetPersistenceManager.class);
    private final CassandraShortBetInfoPersister shortBetInfoPersister;
    private final CassandraBetPersister betPersister;
    private CassandraBigStorageBetPersister bigStorageBetPersister;
    private boolean useBigStoragePersister;

    public PlayerBetPersistenceManager(GameServerConfiguration gameServerConfiguration, CassandraPersistenceManager persistenceManager) {
        shortBetInfoPersister = persistenceManager.getPersister(CassandraShortBetInfoPersister.class);
        betPersister = persistenceManager.getPersister(CassandraBetPersister.class);
        useBigStoragePersister = gameServerConfiguration.isBigStorageCassandraClusterEnabled();
        if (useBigStoragePersister) {
            bigStorageBetPersister = persistenceManager.getPersister(CassandraBigStorageBetPersister.class);
        }
    }

    public boolean isUseBigStoragePersister() {
        return useBigStoragePersister;
    }

    public PlayerBet persist(GameSession gameSession, PlayerBet bet, boolean createNewBet,
                             boolean sendBetToExternalSystem, Long roundId, boolean isSaveGameSidByRound,
                             boolean saveAsShortBetInfo) {
        PlayerBet persistedBet = getPersister(gameSession).persist(gameSession, bet, createNewBet, roundId,
                isSaveGameSidByRound);
        if (sendBetToExternalSystem && gameSession.isRealMoney()) {
            LOG.debug("sendBetToExternalSystem = {}, gameSession = {} ", sendBetToExternalSystem, gameSession);
            //code removed
        }
        if (saveAsShortBetInfo) {
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            AccountInfo account = transactionData.getAccount();
            ShortBetInfo shortBetInfo = new ShortBetInfo(account.getExternalId(), account.getId(),
                    account.getBankId(), gameSession.getGameId(), gameSession.getId(), persistedBet.getId(),
                    persistedBet.getBet(), persistedBet.getWin(), bet.getBalance(), bet.getTime(),
                    gameSession.getCurrency().getCode());
            transactionData.add(StoredItemType.SHORT_BET_INFO, shortBetInfo, null);
        }
        return persistedBet;
    }

    public List<ShortBetInfo> getShortBetInfo(long bankId, long startDate, long endDate) {
        return shortBetInfoPersister.getByBank(bankId, startDate, endDate);
    }

    public void finishGameSession(GameSession gameSession, long endTime, PlayerBet bet, boolean sendBetToExternalSystem)
            throws DBException {
        getPersister(gameSession).finishGameSession(gameSession, endTime, bet);
        if (sendBetToExternalSystem) {
            //code removed
        }
    }

    public PlayerBet getCurrentBet(GameSession gameSession) {
        return getPersister(gameSession).getCurrentBet(gameSession);
    }

    public void updateBet(GameSession gameSession, PlayerBet playerBet, int gameStateId, String data,
                          String servletData, long bet, long win, long balance, byte[] archiveAdditionalData,
                          boolean saveAsShortBetInfo) {
        getPersister(gameSession).updateBet(gameSession, playerBet, gameStateId, data,
                servletData, bet, win, balance, archiveAdditionalData);
        if (saveAsShortBetInfo) {
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            AccountInfo account = transactionData.getAccount();
            ShortBetInfo shortBetInfo = new ShortBetInfo(account.getExternalId(), account.getId(),
                    account.getBankId(), gameSession.getGameId(), gameSession.getId(), playerBet.getId(),
                    bet, win, balance, playerBet.getTime(), gameSession.getCurrency().getCode());
            transactionData.add(StoredItemType.SHORT_BET_INFO, shortBetInfo, null);
        }
    }

    public PlayerBetPersister getPersister(GameSession gameSession) {
        return gameSession.isPersistBets() ? getPersister() : NoneBetPersister.getInstance();
    }

    public PlayerBetPersister getPersister() {
        return isUseBigStoragePersister() ? bigStorageBetPersister : betPersister;
    }

    public void flushGameSessionHistory(GameSession gameSession, String accountExternalId) throws CommonException {
        getPersister(gameSession).flushGameSessionHistory(gameSession);

        HistoryInformerManager historyInformerManager = GameServerComponentsHelper.getHistoryInformerManager();
        historyInformerManager.createHistoryItem(gameSession, accountExternalId);
    }

    /**
     * @param sessionBetsMap - key is gameSessionId, value is List of bet ids
     * @return key is gameSessionId, value is List of PlayerBet
     */
    public Map<Long, List<PlayerBet>> getPlayerBets(Map<Long, List<Long>> sessionBetsMap) {
        if (isUseBigStoragePersister()) {
            Map<Long, List<PlayerBet>> bets = bigStorageBetPersister.getPlayerBets(sessionBetsMap);
            if (!bets.isEmpty()) {
                return bets;
            }
        }
        return betPersister.getPlayerBets(sessionBetsMap);
    }

    public List<PlayerBet> getBets(Long gameSessionId) {
        return getBets(gameSessionId, null, null);
    }

    public List<PlayerBet> getBets(Long gameSessionId, Integer from, Integer count) {//only from GameHistoryServlet
        return getBets(gameSessionId, from, count, false);
    }

    public List<PlayerBet> getBets(Long gameSessionId, Integer from, Integer count, boolean immutableCache) {
        if (isUseBigStoragePersister()) {
            List<PlayerBet> bets = bigStorageBetPersister.getBets(gameSessionId, from, count, immutableCache);
            if (!bets.isEmpty()) {
                return bets;
            }
        }
        return betPersister.getBets(gameSessionId, from, count, immutableCache);
    }

    public Pair<Integer, List<PlayerBet>> getBetsAndRealSize(Long gameSessionId, Integer from, Integer count,
                                                             boolean immutableCache) {
        if (isUseBigStoragePersister()) {
            Pair<Integer, List<PlayerBet>> bets = bigStorageBetPersister.getBetsAndRealSize(gameSessionId, from, count, immutableCache);
            if (!bets.getValue().isEmpty()) {
                return bets;
            }
        }
        return betPersister.getBetsAndRealSize(gameSessionId, from, count, immutableCache);
    }

    public Pair<Integer, List<PlayerBet>> getBetsAndRealSize(long gameSessionId, int from, int count, final Long roundId) {
        if (isUseBigStoragePersister()) {
            Pair<Integer, List<PlayerBet>> bets = bigStorageBetPersister.getBetsAndRealSize(gameSessionId, from, count, roundId);
            if (!bets.getValue().isEmpty()) {
                return bets;
            }
        }
        return betPersister.getBetsAndRealSize(gameSessionId, from, count, roundId);
    }

    public int getBetsCount(long gameSessionId) {
        if (isUseBigStoragePersister()) {
            int betsCount = bigStorageBetPersister.getBetsCount(gameSessionId);
            if (betsCount > 0) {
                return betsCount;
            }
        }
        return betPersister.getBetsCount(gameSessionId);
    }

    public int getRoundsCount(GameSession gameSession, long roundId) {
        if (isUseBigStoragePersister()) {
            int betsCount = bigStorageBetPersister.getRoundsCount(gameSession, roundId);
            if (betsCount > 0) {
                return betsCount;
            }
        }
        return betPersister.getRoundsCount(gameSession, roundId);
    }

    public void persist(long gameSessionId, List<PlayerBet> bets) {
        getPersister().persist(gameSessionId, bets);
    }

    public void prepareToPersistGameSessionBets(Map<Session, List<Statement>> statementsMap, long gameSessionId,
                                                int maxPlayerBetId, List<ByteBuffer> byteBuffersCollector) {
        if (isUseBigStoragePersister()) {
            bigStorageBetPersister.prepareToPersistGameSessionBets(statementsMap, gameSessionId,
                    maxPlayerBetId, byteBuffersCollector);
            return;
        }
        betPersister.prepareToPersistGameSessionBets(statementsMap, gameSessionId, maxPlayerBetId, byteBuffersCollector);
    }

    public void delete(long gameSessionId) {
        if (isUseBigStoragePersister()) {
            bigStorageBetPersister.delete(gameSessionId);
        }
        betPersister.delete(gameSessionId);
    }

    public void delete(long... gameSessionIds) {
        if (isUseBigStoragePersister()) {
            bigStorageBetPersister.delete(gameSessionIds);
        }
        betPersister.delete(gameSessionIds);
    }

    public Triple<List<Long>, Long, Long> getGameSessionsByRoundId(long roundId) {
        if (isUseBigStoragePersister()) {
            Triple<List<Long>, Long, Long> gameSessions =
                    bigStorageBetPersister.getRoundGameSessionPersister().getGameSessionsByRoundId(roundId);
            if (gameSessions != null) {
                return gameSessions;
            }
        }
        return betPersister.getRoundGameSessionPersister().getGameSessionsByRoundId(roundId);
    }

    public String getMainColumnFamilyName() {
        return isUseBigStoragePersister()
                ? bigStorageBetPersister.getMainColumnFamilyName()
                : betPersister.getMainColumnFamilyName();
    }
}
