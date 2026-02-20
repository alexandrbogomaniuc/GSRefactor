package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IBuyInResult {
    long getAmount();

    void setAmount(long amount);

    long getBalance();

    long getPlayerRoundId();

    void setPlayerRoundId(long playerRoundId);

    void setBalance(long balance);

    long getGameSessionId();

    void setGameSessionId(long gameSessionId);

    boolean isSuccess();

    void setSuccess(boolean success);

    boolean isFatalError();

    void setFatalError(boolean fatalError);

    String getErrorDescription();

    void setErrorDescription(String errorDescription);

    int getErrorCode();

    void setErrorCode(int errorCode);
}
