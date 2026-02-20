package com.betsoft.casino.mp.transport;

import java.io.Serializable;
import java.util.StringJoiner;

public class CrashBetInfo implements Serializable {
    private long amount;
    private double mult;
    private boolean auto;
    private String name;
    private String betId;
    private long ejectTime;
    private Double autoPlayMultiplier;
    private boolean isReserved;

    public CrashBetInfo(String name, long amount, boolean auto, double mult, String betId, long ejectTime, Double autoPlayMultiplier, boolean isReserved) {
        this.amount = amount;
        this.mult = mult;
        this.auto = auto;
        this.name = name;
        this.betId = betId;
        this.ejectTime = ejectTime;
        this.autoPlayMultiplier = autoPlayMultiplier;
        this.isReserved = isReserved;
    }

    public CrashBetInfo(String name, long amount, boolean auto, double mult, String betId, long ejectTime, Double autoPlayMultiplier) {
        this.amount = amount;
        this.mult = mult;
        this.auto = auto;
        this.name = name;
        this.betId = betId;
        this.ejectTime = ejectTime;
        this.autoPlayMultiplier = autoPlayMultiplier;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public double getMult() {
        return mult;
    }

    public void setMult(double mult) {
        this.mult = mult;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public long getEjectTime() {
        return ejectTime;
    }

    public void setEjectTime(long ejectTime) {
        this.ejectTime = ejectTime;
    }

    public Double getAutoPlayMultiplier() {
        return autoPlayMultiplier;
    }

    public void setAutoPlayMultiplier(Double autoPlayMultiplier) {
        this.autoPlayMultiplier = autoPlayMultiplier;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashBetInfo.class.getSimpleName() + "[", "]")
                .add("amount=" + amount)
                .add("mult=" + mult)
                .add("auto=" + auto)
                .add("name='" + name + "'")
                .add("betId='" + betId + "'")
                .add("ejectTime=" + ejectTime)
                .add("autoPlayMultiplier=" + autoPlayMultiplier)
                .toString();
    }
}
