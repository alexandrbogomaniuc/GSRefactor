package com.dgphoenix.casino.common.promo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class RoundStat implements KryoSerializable {
    private static final byte VERSION = 0;
    private long roundId;
    private long betAmount;
    private long roundSummaryWin = 0;

    public RoundStat() {}

    public RoundStat(Long roundId, Long betAmount) {
        this.roundId = roundId;
        this.betAmount = betAmount;
    }

    public Long getRoundId() {
        return this.roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
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

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(this.roundId, true);
        output.writeLong(this.betAmount, true);
        output.writeLong(this.roundSummaryWin, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        this.roundId = input.readLong(true);
        this.betAmount = input.readLong(true);
        this.roundSummaryWin = input.readLong(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        RoundStat roundStat = (RoundStat) o;
        return this.roundId == roundStat.roundId &&
                this.betAmount == roundStat.betAmount &&
                this.roundSummaryWin == roundStat.roundSummaryWin;
    }

    @Override
    public int hashCode() {
        int result = (int) (this.roundId ^ (this.roundId >>> 32));
        result = 31 * result + (int) (this.betAmount ^ (this.betAmount >>> 32));
        result = 31 * result + (int) (this.roundSummaryWin ^ (this.roundSummaryWin >>> 32));
        return result;
    }
}
