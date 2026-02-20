package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IActiveFrbSession {
    long getBonusId();

    void setBonusId(long bonusId);

    long getAccountId();

    void setAccountId(long accountId);

    long getAwardDate();

    void setAwardDate(long awardDate);

    long getStartDate();

    void setStartDate(long startDate);

    Long getExpirationDate();

    void setExpirationDate(Long expirationDate);

    int getStartAmmoAmount();

    void setStartAmmoAmount(int startAmmoAmount);

    int getCurrentAmmoAmount();

    void setCurrentAmmoAmount(int currentAmmoAmount);

    long getWinSum();

    void setWinSum(long winSum);

    void incrementWinSum(long delta);

    void decrementWinSum(long delta);

    long getStake();

    void setStake(long stake);

    String getStatus();

    void setStatus(String status);

    long getMaxWinLimit();
}
