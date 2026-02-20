package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface ISitInResult {
    long getGameSessionId();

    void setGameSessionId(long gameSessionId);

    long getBuyInAmount();

    void setBuyInAmount(long buyInAmount);

    long getBalance();

    void setBalance(long balance);

    long getPlayerRoundId();

    void setPlayerRoundId(long playerRoundId);
}
