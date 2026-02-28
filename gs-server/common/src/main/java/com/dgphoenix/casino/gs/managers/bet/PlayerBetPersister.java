package com.abs.casino.gs.managers.bet;

import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.DBException;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 19.10.11
 */
public interface PlayerBetPersister {
    PlayerBet persist(GameSession gameSession, PlayerBet bet, boolean createNewBet, Long roundId,
                      boolean isSaveGameSidByRound);

    void persist(long gameSessionId, List<PlayerBet> bets);

    void finishGameSession(GameSession gameSession, long endTime, PlayerBet bet) throws DBException;

    PlayerBet getCurrentBet(GameSession gameSession);

    void updateBet(GameSession gameSession, PlayerBet playerBet, int gameStateId, String data, String servletData,
                   long bet, long win, long balance, byte[] archiveAdditionalData);

    void flushGameSessionHistory(GameSession gameSession) throws CommonException;

    /**
     * @param sessionBetsMap - key is gameSessionId, value is List of bet ids
     * @return key is gameSessionId, value is List of PlayerBet
     */
    Map<Long, List<PlayerBet>> getPlayerBets(Map<Long, List<Long>> sessionBetsMap);
}
