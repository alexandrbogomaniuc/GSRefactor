package com.abs.casino.gs.persistance.bet;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.exception.DBException;
import com.abs.casino.gs.managers.bet.PlayerBetPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 20.10.11
 */
public class NoneBetPersister implements PlayerBetPersister {
    private static NoneBetPersister instance = new NoneBetPersister();
    private static final Logger LOG = LogManager.getLogger(NoneBetPersister.class);

    private NoneBetPersister() {
        super();
    }

    public static NoneBetPersister getInstance() {
        return instance;
    }

    @Override
    public PlayerBet persist(GameSession gameSession, PlayerBet bet, boolean createNewBet, Long roundId,
                             boolean isSaveGameSidByRound) {
        gameSession.setCreateNewBet(createNewBet, true);
        SessionHelper.getInstance().getTransactionData().setLastBet(bet);
        return bet;
    }

    @Override
    public void persist(long gameSessionId, List<PlayerBet> bets) {
    }

    @Override
    public void finishGameSession(GameSession gameSession, long endTime, PlayerBet bet) throws DBException {
        gameSession.finish(endTime, bet);
    }

    @Override
    public PlayerBet getCurrentBet(GameSession gameSession) {
        return SessionHelper.getInstance().getTransactionData().getLastBet();
    }

    @Override
    public void updateBet(GameSession gameSession, PlayerBet playerBet, int gameStateId, String data, String servletData, long bet, long win,
                          long balance, byte[] archiveAdditionalData) {
        if (playerBet == null) {
            LOG.error("Cannot update bet, bet is null. gameSession=" + gameSession +
                    ", gameStateId=" + gameStateId + ", data=" + data + ", servletData=" + servletData +
                    ", bet=" + bet + ", win=" + win + ", balance=" + balance + ", transactionData=" +
                    SessionHelper.getInstance().getTransactionData());
            throw new RuntimeException("Cannot update bet, current bet is null, gameSessionId=" + gameSession.getId());
        }
        playerBet.update(gameStateId, data, servletData, bet, win, balance, archiveAdditionalData);
    }

    @Override
    public void flushGameSessionHistory(GameSession gameSession) {
        SessionHelper.getInstance().getTransactionData().setLastBet(null);
    }

    @Override
    public Map<Long, List<PlayerBet>> getPlayerBets(Map<Long, List<Long>> sessionBetsMap) {
        return null;
    }
}
