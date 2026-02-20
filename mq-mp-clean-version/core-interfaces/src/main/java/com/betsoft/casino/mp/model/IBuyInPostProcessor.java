package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.exceptions.BuyInFailedException;

public interface IBuyInPostProcessor {
    default void buyInPostProcess(int serverId, long accountId, String sessionId, Money amount, long gameSessionId,
                          long roomId, int betNumber, Long tournamentId, Long currentBalance, IBuyInResult buyInResult) throws BuyInFailedException {

    }
}
