package com.betsoft.casino.mp.model;

public interface ICashBonusInfo {

    long getId();

    void setId(long id);

    long getAwardDate();

    void setAwardDate(long awardDate);

    long getExpirationDate();

    void setExpirationDate(long expirationDate);

    long getBalance();

    void setBalance(long balance);

    long getAmount();

    void setAmount(long amount);

    long getAmountToRelease();

    void setAmountToRelease(long amountToRelease);

    String getStatus();

    void setStatus(String status);
}
