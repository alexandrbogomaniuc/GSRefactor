package com.betsoft.casino.mp.model;

import com.abs.casino.common.cache.Identifiable;

/**
 * User: flsh
 * Date: 14.07.2020.
 */
public interface IActiveCashBonusSession extends Identifiable {
    long getId();

    long getAccountId();

    long getAwardDate();

    long getExpirationDate();

    long getBalance();

    void setBalance(long balance);

    long getAmount();

    void setAmount(long amount);

    long getAmountToRelease();

    long getBetSum();

    void setBetSum(long betSum);

    void incrementBetSum(long bet);

    double getRolloverMultiplier();

    String getStatus();

    void setStatus(String status);

    boolean isActive();

    long getMaxWinLimit();
}
