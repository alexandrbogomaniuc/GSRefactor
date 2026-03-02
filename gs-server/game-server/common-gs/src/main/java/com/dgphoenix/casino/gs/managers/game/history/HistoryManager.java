package com.abs.casino.gs.managers.game.history;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraGameSessionPersister;
import com.abs.casino.cassandra.persist.IGameSessionProcessor;
import com.abs.casino.common.cache.data.bet.PlayerBet;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.Pair;
import com.abs.casino.gs.persistance.GameSessionPersister;
import com.abs.casino.gs.persistance.bet.PlayerBetPersistenceManager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: plastical
 * Date: 25.05.2010
 */
public class HistoryManager {
    private static final HistoryManager instance = new HistoryManager();
    private final CassandraGameSessionPersister gameSessionPersister;
    private final PlayerBetPersistenceManager betPersistenceManager;

    public static HistoryManager getInstance() {
        return instance;
    }

    private HistoryManager() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
        betPersistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
    }

    public long getGameSessionsCount(long accountId, Long gameId, Date startDate,
                                     Date endDate, int mode) {
        return gameSessionPersister.getGameSessionsCount(accountId, gameId == null ? null : Collections.singletonList(gameId),
                startDate, endDate, mode);
    }

    public Pair<Long, Long> getGameSessionsTotals(long accountId, Long gameId, Date startDate,
                                                  Date endDate, int mode) {
        List<GameSession> sessions = gameSessionPersister.getGameSessionList(accountId,
                gameId == null ? null : Collections.singletonList(gameId), startDate,
                endDate, 0, 10000, mode);
        return calculateTotals(sessions);
    }

    public Pair<Long, Long> calculateTotals(List<GameSession> sessions) {
        Long income = 0L;
        Long payout = 0L;
        for (GameSession session : sessions) {
            income += session.getIncome();
            payout += session.getPayout();
        }
        return new Pair<>(income, payout);
    }

    public List<GameSession> getGameSessionList(long accountId, Long gameId, Date startDate,
                                                Date endDate, int from,
                                                int count, int mode) {
        return gameSessionPersister.getGameSessionList(accountId,
                gameId == null ? null : Collections.singletonList(gameId), startDate,
                endDate, from, count, mode);
    }

    public List<GameSession> getAccountGameSessionList(Long accountId, Date startDate, Date endDate) {
        return gameSessionPersister.getAccountGameSessionList(accountId, startDate, endDate);
    }

    public List<GameSession> getBankGameSessionList(int bankId, Date startDate, Date endDate) {
        List<Long> gameSessionIds = gameSessionPersister.getBankGameSessionsIds(bankId, null, startDate, endDate);
        return gameSessionPersister.getGameSessions(gameSessionIds);
    }

    public void processBankGameSessionList(int bankId, Date startDate, Date endDate, IGameSessionProcessor processor) {
        List<Long> gameSessionIds = gameSessionPersister.getBankGameSessionsIds(bankId, null, startDate, endDate);
        gameSessionPersister.processGameSessions(gameSessionIds, processor);
    }

    public GameSession getHistoryGameSession(long gameSessionId) {
        return gameSessionPersister.get(gameSessionId);
    }

    public Integer getBetsCount(long gameSessionId, long roundId) {
        GameSession gameSession = gameSessionPersister.get(gameSessionId);
        if (gameSession == null) {
            return 0;
        }
        if (!gameSession.isPersistBets()) {
            return 0;
        }
        int count = betPersistenceManager.getRoundsCount(gameSession, roundId);
        if (count > 0) {
            return count;
        }
        return 0;
    }


    public Pair<Integer, List<PlayerBet>> getBetsListAndCount(long gameSessionId, int from, int count) {
        GameSession gameSession = gameSessionPersister.get(gameSessionId);
        if (gameSession != null) {
            if (gameSession.isPersistBets()) {
                return betPersistenceManager.getBetsAndRealSize(gameSession.getId(), from, count, false);
            } else {
                new Pair<>(0, Collections.<PlayerBet>emptyList());
            }
        }
        return new Pair<>(0, Collections.<PlayerBet>emptyList());
    }

    public Pair<Integer, List<PlayerBet>> getBetsListAndCount(long gameSessionId, int from, int count, String roundId) {
        GameSession gameSession = gameSessionPersister.get(gameSessionId);
        if (gameSession != null) {
            return gameSession.isPersistBets() ? betPersistenceManager.getBetsAndRealSize(gameSession.getId(), from, count, Long.valueOf(roundId)) :
                    new Pair<>(0, Collections.<PlayerBet>emptyList());
        }
        return new Pair<>(0, Collections.<PlayerBet>emptyList());
    }


    public boolean isBetsListExist(long gameSessionId) throws CommonException {
        GameSession gameSession = gameSessionPersister.get(gameSessionId);
        if (gameSession != null) {
            return gameSession.getRoundsCount() > 0;
        }
        gameSession = GameSessionPersister.getInstance().getGameSession(gameSessionId);
        return gameSession != null
                && gameSession.isPersistBets() && betPersistenceManager.getBetsCount(gameSessionId) > 0;
    }

    public GameSession getPrevSession(long gameSessionId) {
        return gameSessionPersister.getPrevSession(gameSessionId);
    }

    public GameSession getNextSession(long gameSessionId) {
        return gameSessionPersister.getNextSession(gameSessionId);
    }
}
