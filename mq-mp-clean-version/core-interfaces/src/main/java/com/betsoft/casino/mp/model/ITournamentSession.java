package com.betsoft.casino.mp.model;

public interface ITournamentSession {

    long getAccountId();

    void setAccountId(long accountId);

    long getTournamentId();

    void setTournamentId(long tournamentId);

    String getName();

    void setName(String name);

    String getState();

    boolean isActive();

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

    void setReBuyAllowed(boolean rebuyAllowed);

    long getReBuyPrice();

    void setReBuyPrice(long reBuyPrice);

    long getReBuyAmount();

    void setReBuyAmount(long rebuyAmount);

    int getReBuyCount();

    void setReBuyCount(int rebuyCount);

    int getReBuyLimit();

    void setReBuyLimit(int rebuyLimit);

    boolean isResetBalanceAfterRebuy();
}
