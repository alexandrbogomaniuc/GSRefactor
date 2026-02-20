package com.betsoft.casino.mp.model;

public class FrbCloseResult implements IFrbCloseResult{
    private boolean hasNextFrb;
    private long nextFrbId;
    private String errorDescription;
    private int errorCode;
    private long balance;
    private long realWinSum;

    public FrbCloseResult(boolean hasNextFrb, long nextFrbId, String errorDescription, int errorCode, long balance,
                          long realWinSum) {
        this.hasNextFrb = hasNextFrb;
        this.nextFrbId = nextFrbId;
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
        this.balance = balance;
        this.realWinSum = realWinSum;
    }

    public boolean isHasNextFrb() {
        return hasNextFrb;
    }

    public void setHasNextFrb(boolean hasNextFrb) {
        this.hasNextFrb = hasNextFrb;
    }

    public long getNextFrbId() {
        return nextFrbId;
    }

    public void setNextFrbId(long nextFrbId) {
        this.nextFrbId = nextFrbId;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public long getRealWinSum() {
        return realWinSum;
    }

    public void setRealWinSum(long realWinSum) {
        this.realWinSum = realWinSum;
    }

    @Override
    public String toString() {
        return "FrbCloseResult[" +
                "hasNextFrb=" + hasNextFrb +
                ", nextFrbId=" + nextFrbId +
                ", errorDescription='" + errorDescription + '\'' +
                ", errorCode=" + errorCode +
                ", balance=" + balance +
                ", realWinSum=" + realWinSum +
                ']';
    }
}
