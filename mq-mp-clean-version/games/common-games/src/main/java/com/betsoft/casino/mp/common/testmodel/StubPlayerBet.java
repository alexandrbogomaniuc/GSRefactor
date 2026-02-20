package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IPlayerBet;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Objects;

public class StubPlayerBet implements IPlayerBet {
    private long accountId;
    private long dateTime;
    private double bet;
    private double win;
    private String data;
    private long startRoundTime;

    public StubPlayerBet(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getDateTime() {
        return dateTime;
    }

    @Override
    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public double getBet() {
        return bet;
    }

    @Override
    public void setBet(double bet) {
        this.bet = bet;
    }

    @Override
    public double getWin() {
        return win;
    }

    @Override
    public void setWin(double win) {
        this.win = win;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getStartRoundTime() {
        return startRoundTime;
    }

    @Override
    public void setStartRoundTime(long startRoundTime) {
        this.startRoundTime = startRoundTime;
    }

    @Override
    public void addData(String newData) {
        this.data += newData;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(accountId, true);
        output.writeLong(dateTime, true);
        output.writeDouble(bet);
        output.writeDouble(win);
        output.writeString(data);
        output.writeLong(startRoundTime, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        accountId = input.readLong(true);
        dateTime = input.readLong(true);
        bet = input.readDouble();
        win = input.readDouble();
        data = input.readString();
        startRoundTime = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubPlayerBet playerBet = (StubPlayerBet) o;
        return accountId == playerBet.accountId &&
                dateTime == playerBet.dateTime &&
                Double.compare(playerBet.bet, bet) == 0 &&
                Double.compare(playerBet.win, win) == 0 &&
                Objects.equals(data, playerBet.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, dateTime, bet, win, data);
    }

    @Override
    public String toString() {
        return "PlayerBet[" +
                "accountId=" + accountId +
                ", dateTime=" + dateTime +
                ", bet=" + bet +
                ", win=" + win +
                ", data='" + data + '\'' +
                ", startRoundTime='" + startRoundTime +
                ']';
    }
}

