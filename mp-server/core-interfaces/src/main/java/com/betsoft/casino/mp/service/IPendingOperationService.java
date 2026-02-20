package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IPlayerBet;
import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.IPendingOperationProcessor;
import com.betsoft.casino.mp.payment.PendingOperationType;

import java.util.Collection;

/**
 * User: flsh
 * Date: 11.08.2022.
 */
public interface IPendingOperationService {
    boolean isPendingOperation(IRoomPlayerInfo playerInfo);
    @SuppressWarnings("rawtypes")
    IPendingOperationProcessor getProcessor(PendingOperationType type);

    IPendingOperation get(long accountId);

    void create(IPendingOperation newOperation);

    IPendingOperation createBuyInPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long amount, int betNumber,
                                     Long tournamentId, Long currentBalance, long gameId, long bankId);

    IPendingOperation createWinPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId,
                                                                                   long gameId, long bankId, long winAmount, long returnedBet, long gsRound, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo);
    void save(IPendingOperation operation);

    void remove(long accountId);

    boolean isExist(long accountId);

    Collection<IPendingOperation> getAll();

    Collection<Long> getAllKeys();

    boolean tryLock(long accountId);

    void unlock(long accountId);

    void lock(long accountId);
}
