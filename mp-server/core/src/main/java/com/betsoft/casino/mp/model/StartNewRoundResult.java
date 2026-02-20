package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 22.04.2020.
 */
public class StartNewRoundResult implements IStartNewRoundResult {
    private long playerRoundId;
    private long gameSessionId;
    private boolean success;
    private boolean fatalError;
    private String errorDescription;
    private long accountId;

    public StartNewRoundResult(long playerRoundId, long gameSessionId, boolean success, boolean fatalError,
                               String errorDescription, long accountId) {
        this.playerRoundId = playerRoundId;
        this.gameSessionId = gameSessionId;
        this.success = success;
        this.fatalError = fatalError;
        this.errorDescription = errorDescription;
        this.accountId = accountId;
    }

    @Override
    public long getPlayerRoundId() {
        return playerRoundId;
    }

    @Override
    public void setPlayerRoundId(long playerRoundId) {
        this.playerRoundId = playerRoundId;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    @Override
    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public boolean isFatalError() {
        return fatalError;
    }

    @Override
    public void setFatalError(boolean fatalError) {
        this.fatalError = fatalError;
    }

    @Override
    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StartNewRoundResult[");
        sb.append("playerRoundId=").append(playerRoundId);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", success=").append(success);
        sb.append(", fatalError=").append(fatalError);
        sb.append(", errorDescription='").append(errorDescription).append('\'');
        sb.append(", accountId='").append(accountId).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
