package com.dgphoenix.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Objects;

public class RoundQualificationStat implements KryoSerializable {
    private static final byte VERSION = 0;
    private long betAmount;
    private long roundSummaryWin;
    private boolean betQualified;
    private boolean winQualified;

    public RoundQualificationStat() {}

    public RoundQualificationStat(Long betAmount) {
        this.betAmount = betAmount;
    }

    public Long getBetAmount() {
        return this.betAmount;
    }

    public void setBetAmount(Long betAmount) {
        this.betAmount = betAmount;
    }

    public Long getRoundSummaryWin() {
        return this.roundSummaryWin;
    }

    public void setRoundSummaryWin(Long roundSummaryWin) {
        this.roundSummaryWin = roundSummaryWin;
    }

    public void incrementRoundSummaryWin(Long winAmount) {
        this.roundSummaryWin += winAmount;
    }

    public double getRoundRtp() {
        return ((double) this.roundSummaryWin) / this.betAmount;
    }
    public boolean isBetQualified() {
        return betQualified;
    }

    public void setBetQualified(boolean betQualified) {
        this.betQualified = betQualified;
    }

    public boolean isWinQualified() {
        return winQualified;
    }

    public void setWinQualified(boolean winQualified) {
        this.winQualified = winQualified;
    }

    public void reset() {
        betAmount = 0;
        roundSummaryWin = 0;
        betQualified = false;
        winQualified = false;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(this.betAmount, true);
        output.writeLong(this.roundSummaryWin, true);
        output.writeBoolean(this.betQualified);
        output.writeBoolean(this.winQualified);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        this.betAmount = input.readLong(true);
        this.roundSummaryWin = input.readLong(true);
        this.betQualified = input.readBoolean();
        this.winQualified = input.readBoolean();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoundQualificationStat that = (RoundQualificationStat) o;
        return betAmount == that.betAmount && roundSummaryWin == that.roundSummaryWin && betQualified == that.betQualified && winQualified == that.winQualified;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(betAmount, roundSummaryWin, betQualified, winQualified);
    }
}
