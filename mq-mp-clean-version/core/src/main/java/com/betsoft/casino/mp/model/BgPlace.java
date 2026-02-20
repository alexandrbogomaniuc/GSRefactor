package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.IBgPlace;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class BgPlace implements IBgPlace, KryoSerializable, Serializable {
    private static final byte VERSION = 1;

    long accountId;
    long win;
    int rank;
    long betsSum;
    long winSum;
    long gameSessionId;
    long gameScore;
    double ejectPoint;

    public BgPlace() {}

    public BgPlace(long accountId, long win, int rank, long betsSum, long winSum, long gameSessionId, long gameScore, double ejectPoint) {
        this.accountId = accountId;
        this.win = win;
        this.rank = rank;
        this.betsSum = betsSum;
        this.winSum = winSum;
        this.gameSessionId = gameSessionId;
        this.gameScore = gameScore;
        this.ejectPoint = ejectPoint;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getWin() {
        return win;
    }

    public void setWin(long win) {
        this.win = win;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public long getBetsSum() {
        return betsSum;
    }

    public void setBetsSum(long betsSum) {
        this.betsSum = betsSum;
    }

    @Override
    public long getWinSum() {
        return winSum;
    }

    public void setWinSum(long winSum) {
        this.winSum = winSum;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    @Override
    public long getGameScore() {
        return gameScore;
    }

    public void setGameScore(long gameScore) {
        this.gameScore = gameScore;
    }

    @Override
    public double getEjectPoint() {
        return ejectPoint;
    }

    @Override
    public void setEjectPoint(double ejectPoint) {
        this.ejectPoint = ejectPoint;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeLong(win, true);
        output.writeInt(rank, true);
        output.writeLong(betsSum, true);
        output.writeLong(winSum, true);
        output.writeLong(gameSessionId, true);
        output.writeLong(gameScore, true);
        output.writeDouble(ejectPoint);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        accountId = input.readLong(true);
        win = input.readLong(true);
        rank = input.readInt(true);
        betsSum = input.readLong(true);
        winSum = input.readLong(true);
        gameSessionId = input.readLong(true);
        gameScore = input.readLong(true);
        if (version > 0) {
            ejectPoint = input.readDouble();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BgPlace{");
        sb.append("accountId=").append(accountId);
        sb.append(", win=").append(win);
        sb.append(", rank=").append(rank);
        sb.append(", betsSum=").append(betsSum);
        sb.append(", winSum=").append(winSum);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", gameScore=").append(gameScore);
        sb.append(", ejectPoint=").append(ejectPoint);
        sb.append('}');
        return sb.toString();
    }
}
