package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IStartNewRoundResult {
    long getPlayerRoundId();

    void setPlayerRoundId(long playerRoundId);

    long getGameSessionId();

    void setGameSessionId(long gameSessionId);

    boolean isSuccess();

    void setSuccess(boolean success);

    boolean isFatalError();

    void setFatalError(boolean fatalError);

    String getErrorDescription();

    void setErrorDescription(String errorDescription);

    long getAccountId();

    void setAccountId(long accountId);
}
