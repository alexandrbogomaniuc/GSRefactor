package com.abs.casino.common.cache.data.session;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 26.01.18.
 */
public class DeviationStatistics implements KryoSerializable {
    private static final byte VERSION = 0;
    private int rounds;
    private long income;
    private long payout;

    public DeviationStatistics() {
    }

    public DeviationStatistics(int rounds, long income, long payout) {
        this.rounds = rounds;
        this.income = income;
        this.payout = payout;
    }

    public void increment(int rounds, long income, long payout) {
        this.rounds += rounds;
        this.income += income;
        this.payout += payout;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public long getIncome() {
        return income;
    }

    public void setIncome(long income) {
        this.income = income;
    }

    public long getPayout() {
        return payout;
    }

    public void setPayout(long payout) {
        this.payout = payout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviationStatistics that = (DeviationStatistics) o;
        return rounds == that.rounds &&
                income == that.income &&
                payout == that.payout;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeviationStatistics [");
        sb.append("rounds=").append(rounds);
        sb.append(", income=").append(income);
        sb.append(", payout=").append(payout);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeInt(rounds, true);
        output.writeLong(income, true);
        output.writeLong(payout, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        rounds = input.readInt(true);
        income = input.readLong(true);
        payout = input.readLong(true);
    }
}
