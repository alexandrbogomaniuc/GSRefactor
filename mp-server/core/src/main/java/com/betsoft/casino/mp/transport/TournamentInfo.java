package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ITournamentInfo;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 13.07.2020.
 */
public class TournamentInfo implements ITournamentInfo, Serializable {
    private long id;
    private String name;
    private String state;
    private long startDate;
    private long endDate;
    private long balance;
    private long buyInPrice;
    private long buyInAmount;
    private boolean reBuyAllowed;
    private long reBuyPrice;
    private long reBuyAmount;
    private int reBuyCount;
    private int reBuyLimit;
    private boolean resetBalanceAfterRebuy;

    public TournamentInfo(long id, String name, String state, long startDate, long endDate, long balance,
                          long buyInPrice, long buyInAmount, boolean reBuyAllowed, long reBuyPrice, long reBuyAmount,
                          int reBuyCount, int reBuyLimit, boolean resetBalanceAfterRebuy) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
        this.balance = balance;
        this.buyInPrice = buyInPrice;
        this.buyInAmount = buyInAmount;
        this.reBuyAllowed = reBuyAllowed;
        this.reBuyPrice = reBuyPrice;
        this.reBuyAmount = reBuyAmount;
        this.reBuyCount = reBuyCount;
        this.reBuyLimit = reBuyLimit;
        this.resetBalanceAfterRebuy = resetBalanceAfterRebuy;
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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public long getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    @Override
    public long getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(long endDate) {
        this.endDate = endDate;
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
    public long getBuyInPrice() {
        return buyInPrice;
    }

    @Override
    public void setBuyInPrice(long buyInPrice) {
        this.buyInPrice = buyInPrice;
    }

    @Override
    public long getBuyInAmount() {
        return buyInAmount;
    }

    @Override
    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    @Override
    public boolean isReBuyAllowed() {
        return reBuyAllowed;
    }

    @Override
    public void setReBuyAllowed(boolean reBuyAllowed) {
        this.reBuyAllowed = reBuyAllowed;
    }

    @Override
    public long getReBuyPrice() {
        return reBuyPrice;
    }

    @Override
    public void setReBuyPrice(long reBuyPrice) {
        this.reBuyPrice = reBuyPrice;
    }

    @Override
    public long getReBuyAmount() {
        return reBuyAmount;
    }

    @Override
    public void setReBuyAmount(long reBuyAmount) {
        this.reBuyAmount = reBuyAmount;
    }

    @Override
    public int getReBuyCount() {
        return reBuyCount;
    }

    @Override
    public void setReBuyCount(int reBuyCount) {
        this.reBuyCount = reBuyCount;
    }

    @Override
    public int getReBuyLimit() {
        return reBuyLimit;
    }

    @Override
    public void setReBuyLimit(int reBuyLimit) {
        this.reBuyLimit = reBuyLimit;
    }

    @Override
    public boolean isResetBalanceAfterRebuy() {
        return resetBalanceAfterRebuy;
    }

    @Override
    public void setResetBalanceAfterRebuy(boolean resetBalanceAfterRebuy) {
        this.resetBalanceAfterRebuy = resetBalanceAfterRebuy;
    }

    @Override
    public String toString() {
        return "TournamentInfo[" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", balance=" + balance +
                ", buyInPrice=" + buyInPrice +
                ", buyInAmount=" + buyInAmount +
                ", reBuyAllowed=" + reBuyAllowed +
                ", reBuyPrice=" + reBuyPrice +
                ", reBuyAmount=" + reBuyAmount +
                ", reBuyCount=" + reBuyCount +
                ", reBuyLimit=" + reBuyLimit +
                ", resetBalanceAfterRebuy=" + resetBalanceAfterRebuy +
                ']';
    }
}
