package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ICashBonusInfo;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 13.07.2020.
 */
public class CashBonusInfo implements ICashBonusInfo, Serializable {
    private long id;
    private long awardDate;
    private long expirationDate;
    private long balance;
    private long amount;
    private long amountToRelease;
    private String status;

    public CashBonusInfo(long id, long awardDate, long expirationDate, long balance, long amount, long amountToRelease,
                         String status) {
        this.id = id;
        this.awardDate = awardDate;
        this.expirationDate = expirationDate;
        this.balance = balance;
        this.amount = amount;
        this.amountToRelease = amountToRelease;
        this.status = status;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getAwardDate() {
        return awardDate;
    }

    @Override
    public void setAwardDate(long awardDate) {
        this.awardDate = awardDate;
    }

    @Override
    public long getExpirationDate() {
        return expirationDate;
    }

    @Override
    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public void setBalance(long balance) {
        this.balance = balance;
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
    public long getAmountToRelease() {
        return amountToRelease;
    }

    @Override
    public void setAmountToRelease(long amountToRelease) {
        this.amountToRelease = amountToRelease;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CashBonusInfo [");
        sb.append("id=").append(id);
        sb.append(", awardDate=").append(awardDate);
        sb.append(", expirationDate=").append(expirationDate);
        sb.append(", balance=").append(balance);
        sb.append(", amount=").append(amount);
        sb.append(", amountToRelease=").append(amountToRelease);
        sb.append(", status='").append(status).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
