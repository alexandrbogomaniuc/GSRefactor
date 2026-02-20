package com.betsoft.casino.mp.transport;

import com.betsoft.casino.utils.TInboundObject;

import java.util.Objects;
import java.util.StringJoiner;

public class CrashBet extends TInboundObject {
    private int crashBetAmount;
    private double multiplier;
    private boolean autoPlay;
    private String betId;

    public CrashBet(long date, int rid, int crashBetAmount, double multiplier, boolean autoPlay, String betId) {
        super(date, rid);
        this.crashBetAmount = crashBetAmount;
        this.multiplier = multiplier;
        this.autoPlay = autoPlay;
        this.betId = betId;
    }

    public int getCrashBetAmount() {
        return crashBetAmount;
    }

    public void setCrashBetAmount(int crashBetAmount) {
        this.crashBetAmount = crashBetAmount;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    @Override
    public int getFrequencyLimit() {
        return 50;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CrashBet crashBet = (CrashBet) o;
        return crashBetAmount == crashBet.crashBetAmount && Double.compare(crashBet.multiplier, multiplier) == 0
                && autoPlay == crashBet.autoPlay && Objects.equals(betId, crashBet.betId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), crashBetAmount, multiplier, autoPlay, betId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashBet.class.getSimpleName() + "[", "]")
                .add("date=" + date)
                .add("rid=" + rid)
                .add("crashBetAmount=" + crashBetAmount)
                .add("multiplier=" + multiplier)
                .add("autoPlay=" + autoPlay)
                .add("betId='" + betId + "'")
                .toString();
    }
}

