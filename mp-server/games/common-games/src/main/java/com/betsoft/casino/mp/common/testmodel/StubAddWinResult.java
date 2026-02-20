package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IAddWinResult;

public class StubAddWinResult implements IAddWinResult {
    private final boolean playerOffline;
    private final long balance;
    private final boolean success;
    private final int errorCode;
    private final String errorDetails;

    public StubAddWinResult(boolean playerOffline, long balance, boolean success, int errorCode, String errorDetails) {
        this.playerOffline = playerOffline;
        this.balance = balance;
        this.success = success;
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    @Override
    public boolean isPlayerOffline() {
        return playerOffline;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorDetails() {
        return errorDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddWinResult [");
        sb.append("playerOffline=").append(playerOffline);
        sb.append(", balance=").append(balance);
        sb.append(", success=").append(success);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorDetails=").append(errorDetails);
        sb.append(']');
        return sb.toString();
    }
}
