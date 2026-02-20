package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class TournamentSession implements ITournamentSession, KryoSerializable, Serializable {
    private static final byte VERSION = 0;

    private long accountId;
    private long tournamentId;
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

    public TournamentSession() {}

    public TournamentSession(long accountId, long tournamentId, String name, String state, long startDate, long endDate,
                             long balance, long buyInPrice, long buyInAmount, boolean reBuyAllowed, long reBuyPrice,
                             long reBuyAmount, int reBuyCount, int reBuyLimit, boolean resetBalanceAfterRebuy) {
        this.accountId = accountId;
        this.tournamentId = tournamentId;
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

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean isActive() {
        return "STARTED".equalsIgnoreCase(getState());
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getBuyInPrice() {
        return buyInPrice;
    }

    public void setBuyInPrice(long buyInPrice) {
        this.buyInPrice = buyInPrice;
    }

    public long getBuyInAmount() {
        return buyInAmount;
    }

    public void setBuyInAmount(long buyInAmount) {
        this.buyInAmount = buyInAmount;
    }

    public boolean isReBuyAllowed() {
        return reBuyAllowed;
    }

    public void setReBuyAllowed(boolean reBuyAllowed) {
        this.reBuyAllowed = reBuyAllowed;
    }

    public long getReBuyPrice() {
        return reBuyPrice;
    }

    public void setReBuyPrice(long reBuyPrice) {
        this.reBuyPrice = reBuyPrice;
    }

    public long getReBuyAmount() {
        return reBuyAmount;
    }

    public void setReBuyAmount(long reBuyAmount) {
        this.reBuyAmount = reBuyAmount;
    }

    public int getReBuyCount() {
        return reBuyCount;
    }

    public void setReBuyCount(int reBuyCount) {
        this.reBuyCount = reBuyCount;
    }

    public int getReBuyLimit() {
        return reBuyLimit;
    }

    public void setReBuyLimit(int reBuyLimit) {
        this.reBuyLimit = reBuyLimit;
    }

    @Override
    public boolean isResetBalanceAfterRebuy() {
        return resetBalanceAfterRebuy;
    }

    public void setResetBalanceAfterRebuy(boolean resetBalanceAfterRebuy) {
        this.resetBalanceAfterRebuy = resetBalanceAfterRebuy;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeLong(tournamentId, true);
        output.writeString(name);
        output.writeString(state);
        output.writeLong(startDate, true);
        output.writeLong(endDate, true);
        output.writeLong(balance, true);
        output.writeLong(buyInPrice, true);
        output.writeLong(buyInAmount, true);
        output.writeBoolean(reBuyAllowed);
        output.writeLong(reBuyPrice, true);
        output.writeLong(reBuyAmount, true);
        output.writeInt(reBuyCount, true);
        output.writeInt(reBuyLimit, true);
        output.writeBoolean(resetBalanceAfterRebuy);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        accountId = input.readLong(true);
        tournamentId = input.readLong(true);
        name = input.readString();
        state = input.readString();
        startDate = input.readLong(true);
        endDate = input.readLong(true);
        balance = input.readLong(true);
        buyInPrice = input.readLong(true);
        buyInAmount = input.readLong(true);
        reBuyAllowed = input.readBoolean();
        reBuyPrice = input.readLong(true);
        reBuyAmount = input.readLong(true);
        reBuyCount = input.readInt(true);
        reBuyLimit = input.readInt(true);
        resetBalanceAfterRebuy = input.readBoolean();
    }

    @Override
    public String toString() {
        return "TournamentSession[" +
                "accountId=" + accountId +
                ", tournamentId=" + tournamentId +
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
