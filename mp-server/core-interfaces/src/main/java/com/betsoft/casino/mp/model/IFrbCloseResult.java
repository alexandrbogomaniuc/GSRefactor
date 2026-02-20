package com.betsoft.casino.mp.model;

public interface IFrbCloseResult {
    boolean isHasNextFrb();

    void setHasNextFrb(boolean hasNextFrb);

    long getNextFrbId();

    void setNextFrbId(long nextFrbId);

    String getErrorDescription();

    void setErrorDescription(String errorDescription);

    int getErrorCode();

    void setErrorCode(int errorCode);

    long getBalance();

    void setBalance(long balance);

    long getRealWinSum();
}
