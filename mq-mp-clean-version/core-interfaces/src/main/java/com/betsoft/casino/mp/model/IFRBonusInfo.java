package com.betsoft.casino.mp.model;

public interface IFRBonusInfo {

    long getId();

    void setId(long id);

    long getAwardDate();

    void setAwardDate(long awardDate);

    long getExpirationDate();

    void setExpirationDate(long expirationDate);

    int getTotalShots();

    void setTotalShots(int totalShots);

    int getCurrentShots();

    void setCurrentShots(int currentShots);

    long getWinSum();

    void setWinSum(long winSum);

    long getStake();

    void setStake(long stake);
}
