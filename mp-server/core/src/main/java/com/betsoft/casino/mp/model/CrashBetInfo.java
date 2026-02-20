package com.betsoft.casino.mp.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.StringJoiner;

public class CrashBetInfo implements ICrashBetInfo, KryoSerializable, Serializable {
    private static final byte VERSION = 1;

    private long crashBetAmount;
    private double multiplier;
    private boolean autoPlay;
    private Double autoPlayMultiplier;
    private boolean ejected;
    private long ejectTime;
    /** is true if sendRealBetWin=false and we collect bets before start round without processing on gs*/
    private boolean reserved;

    public CrashBetInfo() {}

    public CrashBetInfo(long crashBetAmount, double multiplier, boolean autoPlay) {
        this.crashBetAmount = crashBetAmount;
        this.multiplier = multiplier;
        this.autoPlay = autoPlay;
        if (autoPlay) {
            autoPlayMultiplier = multiplier;
        }
    }

    @Override
    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @Override
    public long getCrashBetAmount() {
        return crashBetAmount;
    }

    @Override
    public void setCrashBetAmount(long crashBetAmount) {
        this.crashBetAmount = crashBetAmount;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public boolean isAutoPlay() {
        return autoPlay;
    }

    @Override
    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    @Override
    public boolean isEjected() {
        return ejected;
    }

    @Override
    public void setEjected(boolean ejected) {
        this.ejected = ejected;
    }

    @Override
    public long getEjectTime() {
        return ejectTime;
    }

    @Override
    public void setEjectTime(long ejectTime) {
        this.ejectTime = ejectTime;
    }

    @Override
    public Double getAutoPlayMultiplier() {
        return autoPlayMultiplier;
    }

    @Override
    public void setAutoPlayMultiplier(Double autoPlayMultiplier) {
        this.autoPlayMultiplier = autoPlayMultiplier;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(crashBetAmount, true);
        output.writeDouble(multiplier);
        output.writeBoolean(autoPlay);
        output.writeBoolean(ejected);
        output.writeLong(ejectTime, true);
        output.writeDouble(autoPlayMultiplier == null ? -1 : autoPlayMultiplier);
        output.writeBoolean(reserved);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        crashBetAmount = input.readLong(true);
        multiplier = input.readDouble();
        autoPlay = input.readBoolean();
        ejected = input.readBoolean();
        ejectTime = input.readLong(true);
        double d = input.readDouble();
        autoPlayMultiplier = d >= 0 ? d : null;
        if (version >= 1) {
            reserved = input.readBoolean();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CrashBetInfo.class.getSimpleName() + "[", "]")
                .add("crashBetAmount=" + crashBetAmount)
                .add("multiplier=" + multiplier)
                .add("autoPlay=" + autoPlay)
                .add("ejected=" + ejected)
                .add("ejectTime=" + ejectTime)
                .add("autoPlayMultiplier=" + autoPlayMultiplier)
                .add("reserved=" + reserved)
                .toString();
    }
}

