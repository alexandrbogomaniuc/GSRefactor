package com.betsoft.casino.mp.model;

public interface ITournamentInfo {

    long getId();

    void setId(long id);

    String getName();

    void setName(String name);

    String getState();

    void setState(String state);

    long getStartDate();

    void setStartDate(long startDate);

    long getEndDate();

    void setEndDate(long endDate);

    long getBalance();

    void setBalance(long balance);

    long getBuyInPrice();

    void setBuyInPrice(long buyInPrice);

    long getBuyInAmount();

    void setBuyInAmount(long buyInAmount);

    boolean isReBuyAllowed();

    void setReBuyAllowed(boolean reBuyAllowed);

    long getReBuyPrice();

    void setReBuyPrice(long reBuyPrice);

    long getReBuyAmount();

    void setReBuyAmount(long reBuyAmount);

    int getReBuyCount();

    void setReBuyCount(int reBuyCount);

    int getReBuyLimit();

    void setReBuyLimit(int reBuyLimit);

    boolean isResetBalanceAfterRebuy();

    void setResetBalanceAfterRebuy(boolean resetBalanceAfterRebuy);
}
