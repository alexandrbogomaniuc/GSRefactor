package com.betsoft.casino.mp.model;

public class BuyInResult implements IBuyInResult {

    private long amount;
    private long balance;
    private long playerRoundId;
    private long gameSessionId;
    private boolean success;
    private boolean fatalError;
    private String errorDescription;
    private int errorCode;

    public BuyInResult(long amount, long balance, long playerRoundId, long gameSessionId, boolean success,
                       boolean fatalError, String errorDescription, int errorCode) {
        this.amount = amount;
        this.balance = balance;
        this.playerRoundId = playerRoundId;
        this.gameSessionId = gameSessionId;
        this.success = success;
        this.fatalError = fatalError;
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public long getBalance() {
        return balance;
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
    public void setBalance(long balance) {
        this.balance = balance;
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
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "BuyInResult[" +
                "amount=" + amount +
                ", balance=" + balance +
                ", playerRoundId=" + playerRoundId +
                ", gameSessionId=" + gameSessionId +
                ", success=" + success +
                ", fatalError=" + fatalError +
                ", errorDescription='" + errorDescription + "'" +
                ", errorCode=" + errorCode +
                ']';
    }
}
